package com.ast.similarity.parser;

import com.ast.similarity.config.ParserProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

@Component
public class ParserEngineBridge {
    private static final Logger logger = LoggerFactory.getLogger(ParserEngineBridge.class);

    private final ParserProperties properties;
    private final ObjectMapper objectMapper;
    private final ExecutorService executor = Executors.newCachedThreadPool(r -> {
        Thread t = new Thread(r, "parser-bridge-worker");
        t.setDaemon(true);
        return t;
    });

    @jakarta.annotation.PreDestroy
    public void cleanup() {
        executor.shutdownNow();
    }

    public ParserEngineBridge(ParserProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    public Map<String, Object> checkHealth() {
        try {
            Map<String, Object> req = new HashMap<>();
            req.put("action", "health");
            String output = executeProcess(req);
            return objectMapper.readValue(output, Map.class);
        } catch (Exception e) {
            logger.error("Health check failed", e);
            Map<String, Object> fallback = new HashMap<>();
            fallback.put("initialized", false);
            fallback.put("error", e.getMessage());
            return fallback;
        }
    }

    public RawParseResult parse(String language, String sourceCode) throws Exception {
        if (sourceCode != null && sourceCode.getBytes(StandardCharsets.UTF_8).length > properties.getMaxSourceBytes()) {
            throw new IllegalArgumentException("Source code exceeds maximum permitted size (" +
                    properties.getMaxSourceBytes() + " bytes)");
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("action", "parse");
        payload.put("language", language);
        payload.put("sourceCode", sourceCode != null ? sourceCode : "");

        String jsonResponse = executeProcess(payload);
        return objectMapper.readValue(jsonResponse, RawParseResult.class);
    }

    private String executeProcess(Map<String, Object> payload) throws Exception {
        String jsonInput = objectMapper.writeValueAsString(payload);

        // Locate python script relative to workspace or config
        File scriptFile = resolveScriptFile(properties.getEnginePath());
        if (!scriptFile.exists()) {
            throw new IllegalStateException("Parser script not found at: " + scriptFile.getAbsolutePath());
        }

        ProcessBuilder pb = new ProcessBuilder(
                properties.getPythonExecutable(),
                scriptFile.getAbsolutePath()
        );
        pb.redirectErrorStream(false);

        Process process = pb.start();

        // Write payload to standard input
        try (OutputStream os = process.getOutputStream()) {
            os.write(jsonInput.getBytes(StandardCharsets.UTF_8));
            os.flush();
        }

        // Read standard output with timeout
        Future<String> readTask = executor.submit(() -> {
            try (InputStream is = process.getInputStream()) {
                return new String(is.readAllBytes(), StandardCharsets.UTF_8);
            }
        });

        Future<String> readErrTask = executor.submit(() -> {
            try (InputStream es = process.getErrorStream()) {
                return new String(es.readAllBytes(), StandardCharsets.UTF_8);
            }
        });

        boolean finished = process.waitFor(properties.getTimeoutMs(), TimeUnit.MILLISECONDS);
        if (!finished) {
            process.destroyForcibly();
            readTask.cancel(true);
            readErrTask.cancel(true);
            throw new TimeoutException("Parser process timed out after " + properties.getTimeoutMs() + "ms");
        }

        String stdout = readTask.get();
        String stderr = readErrTask.get();

        if (process.exitValue() != 0 && (stdout == null || stdout.trim().isEmpty())) {
            throw new RuntimeException("Parser process exited with code " + process.exitValue() + ": " + stderr);
        }

        return stdout;
    }

    private File resolveScriptFile(String path) {
        File file = new File(path);
        if (file.isAbsolute() && file.exists()) {
            return file;
        }
        // Check current working dir
        File candidate = new File(".", path);
        if (candidate.exists()) return candidate;

        // Check backend/ or parent
        candidate = new File("backend", path);
        if (candidate.exists()) return candidate;

        candidate = new File("../backend", path);
        if (candidate.exists()) return candidate;

        return file;
    }
}
