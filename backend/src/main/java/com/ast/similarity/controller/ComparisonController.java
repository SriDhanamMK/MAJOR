package com.ast.similarity.controller;

import com.ast.similarity.dto.CompareRequest;
import com.ast.similarity.dto.CompareResponse;
import com.ast.similarity.service.ComparisonService;
import com.ast.similarity.service.ReportService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class ComparisonController {

    private final ComparisonService comparisonService;
    private final ReportService reportService;

    public ComparisonController(ComparisonService comparisonService, ReportService reportService) {
        this.comparisonService = comparisonService;
        this.reportService = reportService;
    }

    @PostMapping("/compare")
    public ResponseEntity<CompareResponse> comparePrograms(@Valid @RequestBody CompareRequest request) throws Exception {
        CompareResponse response = comparisonService.compare(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/compare/{id}/report/json")
    public ResponseEntity<String> getJsonReport(@PathVariable String id) throws Exception {
        CompareResponse response = comparisonService.getById(id);
        if (response == null) {
            return ResponseEntity.notFound().build();
        }
        String json = reportService.generateJsonReport(response);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"ast-similarity-report-" + id + ".json\"")
                .contentType(MediaType.APPLICATION_JSON)
                .body(json);
    }

    @GetMapping("/compare/{id}/report/csv")
    public ResponseEntity<String> getCsvReport(@PathVariable String id) {
        CompareResponse response = comparisonService.getById(id);
        if (response == null) {
            return ResponseEntity.notFound().build();
        }
        String csv = reportService.generateCsvSummary(response);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"ast-similarity-summary-" + id + ".csv\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv);
    }
}
