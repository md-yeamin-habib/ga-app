package com.gaapp.core.model;

public class BinaryGene implements Gene {

    private int value;

    public BinaryGene(int value) {
        this.value = value;
    }


    public void flip() {
        value = value == 0 ? 1 : 0;
    }

    @Override
    public Object getValue() {
        return value;
    }

    @Override
    public Gene copy() {
        return new BinaryGene(value);
    }

    @Override
    public String toString() {
        return value == 1 ? "1" : "0";
    }
}