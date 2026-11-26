package com.ast.similarity.service;

import com.ast.similarity.comparison.ComparisonEngine;
import com.ast.similarity.dto.CompareRequest;
import com.ast.similarity.dto.CompareResponse;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ComparisonService {

    private final ComparisonEngine comparisonEngine;
    private final Map<String, CompareResponse> cache = new ConcurrentHashMap<>();

    public ComparisonService(ComparisonEngine comparisonEngine) {
        this.comparisonEngine = comparisonEngine;
    }

    public CompareResponse compare(CompareRequest request) throws Exception {
        CompareResponse response = comparisonEngine.compare(request);
        if (response.getComparisonId() != null) {
            cache.put(response.getComparisonId(), response);
        }
        return response;
    }

    public CompareResponse getById(String id) {
        return cache.get(id);
    }
}
