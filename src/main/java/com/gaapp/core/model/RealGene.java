package com.gaapp.core.model;

public class RealGene implements Gene {

    private double value;

    public RealGene(double value) {
        this.value = value;
    }

    public double getDouble() {
        return value;
    }

    public void setDouble(double value) {
        this.value = value;
    }

    @Override
    public Object getValue() {
        return value;
    }

    @Override
    public Gene copy() {
        return new RealGene(value);
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}