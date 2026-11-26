package com.ast.similarity.controller;

import com.ast.similarity.dto.HealthResponse;
import com.ast.similarity.parser.ParserEngineBridge;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class HealthController {

    private final ParserEngineBridge parserBridge;

    public HealthController(ParserEngineBridge parserBridge) {
        this.parserBridge = parserBridge;
    }

    @GetMapping("/health")
    public ResponseEntity<HealthResponse> getHealth() {
        HealthResponse response = new HealthResponse();
        response.setApplication("AST-Based Source Code Similarity Detection");
        response.setJavaVersion(System.getProperty("java.version"));
        response.setOs(System.getProperty("os.name") + " (" + System.getProperty("os.arch") + ")");

        Map<String, Object> parserHealth = parserBridge.checkHealth();
        response.setDetails(parserHealth);

        boolean initialized = Boolean.TRUE.equals(parserHealth.get("initialized"));
        Map<String, Object> langs = (Map<String, Object>) parserHealth.get("languages");

        boolean cOk = initialized && langs != null && Boolean.TRUE.equals(langs.get("C"));
        boolean cppOk = initialized && langs != null && Boolean.TRUE.equals(langs.get("CPP"));
        boolean pyOk = initialized && langs != null && Boolean.TRUE.equals(langs.get("PYTHON"));

        response.setCParserAvailable(cOk);
        response.setCppParserAvailable(cppOk);
        response.setPythonParserAvailable(pyOk);

        if (cOk && cppOk && pyOk) {
            response.setStatus("UP");
        } else {
            response.setStatus("DEGRADED");
        }

        return ResponseEntity.ok(response);
    }
}
