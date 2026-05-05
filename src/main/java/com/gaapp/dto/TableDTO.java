package com.gaapp.dto;

import java.util.List;

public class TableDTO {

    private List<List<String>> rows;

    public TableDTO() {}

    public TableDTO(List<List<String>> rows) {
        this.rows = rows;
    }

    public List<List<String>> getRows() {
        return rows;
    }

    public void setRows(List<List<String>> rows) {
        this.rows = rows;
    }
}