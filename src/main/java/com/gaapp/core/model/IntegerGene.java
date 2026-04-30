package com.gaapp.core.model;

public class IntegerGene implements Gene {

    private int value;

    public IntegerGene(int value) {
        this.value = value;
    }

    public int getInt() {
        return value;
    }

    public void setInt(int value) {
        this.value = value;
    }

    @Override
    public Object getValue() {
        return value;
    }

    @Override
    public Gene copy() {
        return new IntegerGene(value);
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}