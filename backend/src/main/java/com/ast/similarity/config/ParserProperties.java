package com.ast.similarity.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ParserProperties {

    @Value("${parser.engine.path:parser-engine/treesitter_parser.py}")
    private String enginePath;

    @Value("${parser.python.executable:python}")
    private String pythonExecutable;

    @Value("${parser.timeout.ms:10000}")
    private int timeoutMs;

    @Value("${parser.max.source.bytes:524288}")
    private int maxSourceBytes;

    public String getEnginePath() {
        return enginePath;
    }

    public void setEnginePath(String enginePath) {
        this.enginePath = enginePath;
    }

    public String getPythonExecutable() {
        return pythonExecutable;
    }

    public void setPythonExecutable(String pythonExecutable) {
        this.pythonExecutable = pythonExecutable;
    }

    public int getTimeoutMs() {
        return timeoutMs;
    }

    public void setTimeoutMs(int timeoutMs) {
        this.timeoutMs = timeoutMs;
    }

    public int getMaxSourceBytes() {
        return maxSourceBytes;
    }

    public void setMaxSourceBytes(int maxSourceBytes) {
        this.maxSourceBytes = maxSourceBytes;
    }
}
