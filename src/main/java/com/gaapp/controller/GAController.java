package com.gaapp.controller;

import com.gaapp.service.GAService;

import com.gaapp.dto.GAResponse;

import com.gaapp.service.GARequest;
import com.gaapp.core.factory.PopulationBundle;

import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/ga")
@CrossOrigin
public class GAController {

    private final GAService service;

    public GAController(GAService service) {
        this.service = service;
    }

    // =========================================================
    // RUN
    // =========================================================
    @PostMapping("/run")
    public GAResponse run(@RequestBody GARequest request) {

        var generations = service.run(request);

        return new GAResponse(
            request.getTables(),
            generations
        );
    }

    // =========================================================
    // GENERATE 
    // =========================================================
    @PostMapping("/generate")
    public Map<String, Object> generate(@RequestBody GARequest request) {

        PopulationBundle bundle = service.generate(request);

        if (bundle.getProblemData() == null) {
            return Map.of(
            "problem", service.convertPopulation(bundle.getPopulation()),
            "population", service.convertPopulation(bundle.getPopulation())
            );
        }

        return Map.of(
            "problem", service.convertProblemData(bundle.getProblemData()),
            "population", service.convertPopulation(bundle.getPopulation())
        );
    }
}