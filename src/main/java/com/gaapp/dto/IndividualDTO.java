package com.gaapp.dto;

public class IndividualDTO {
    private String name;
    private double fitness;
    private String type;

    public String getName() {
        return name.trim().toUpperCase();
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getFitness() {
        return fitness;
    }

    public void setFitness(double fitness) {
        this.fitness = fitness;
    }

    public String getType() {
        return type.trim().toLowerCase();
    }

    public void setType(String type) {
        this.type = type;
    }
}