package com.ast.similarity.controller;

import com.ast.similarity.dto.ParseRequest;
import com.ast.similarity.dto.ParseResponse;
import com.ast.similarity.service.ParserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ParseController {

    private final ParserService parserService;

    public ParseController(ParserService parserService) {
        this.parserService = parserService;
    }

    @PostMapping("/parse")
    public ResponseEntity<ParseResponse> parseCode(@Valid @RequestBody ParseRequest request) throws Exception {
        ParseResponse response = parserService.parse(request);
        return ResponseEntity.ok(response);
    }
}
