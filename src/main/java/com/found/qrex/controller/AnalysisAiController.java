package com.found.qrex.controller;

import com.found.qrex.service.AnalysisService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/analysis/ai")
public class AnalysisAiController {

    private final AnalysisService analysisService;

    public AnalysisAiController(AnalysisService analysisService) {
        this.analysisService = analysisService;
    }

    @PatchMapping("/title")
    public void updateTitle(@RequestBody Map<String, String> req) {

        Integer analysisId = null;
        try {
            analysisId = Integer.valueOf(req.getOrDefault("analysisId", null));
        } catch (Exception ignore) {}

        String userId = req.get("userId");
        String newTitle = req.get("newTitle");

        analysisService.updateTitleByAi(analysisId, null, userId, newTitle);
    }
}
