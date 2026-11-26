package com.ast.similarity.parser;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ParserFactory {
    private final Map<String, LanguageParserAdapter> adapterMap = new HashMap<>();

    public ParserFactory(List<LanguageParserAdapter> adapters) {
        for (LanguageParserAdapter adapter : adapters) {
            adapterMap.put(adapter.getLanguage().toUpperCase(), adapter);
        }
    }

    public LanguageParserAdapter getAdapter(String language) {
        if (language == null) {
            throw new IllegalArgumentException("Language must not be null");
        }
        String key = language.trim().toUpperCase();
        if (key.equals("C++") || key.equals("CXX")) key = "CPP";
        if (key.equals("PY")) key = "PYTHON";

        LanguageParserAdapter adapter = adapterMap.get(key);
        if (adapter == null) {
            throw new IllegalArgumentException("Unsupported language: " + language +
                    ". Supported languages: C, CPP, PYTHON");
        }
        return adapter;
    }
}
