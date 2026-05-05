package com.gaapp.dto;

import java.util.List;
import java.util.Map;

public class GAResponse {

    private Map<String, List<List<String>>> table0; // input
    private List<GenerationDTO> generations;

    public GAResponse(Map<String, List<List<String>>> table0,
                      List<GenerationDTO> generations) {
        this.table0 = table0;
        this.generations = generations;
    }

    public Map<String, List<List<String>>> getTable0() {
        return table0;
    }

    public List<GenerationDTO> getGenerations() {
        return generations;
    }
}