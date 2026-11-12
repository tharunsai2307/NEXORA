package com.smartbuilding.analytics.dto;

import java.util.ArrayList;
import java.util.List;

public class HistoricalQueryResponse {
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private List<HistoricalDataPointDto> records = new ArrayList<>();

    public HistoricalQueryResponse() {}

    public HistoricalQueryResponse(int page, int size, long totalElements, int totalPages, List<HistoricalDataPointDto> records) {
        this.page = page;
        this.size = size;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
        this.records = records;
    }

    public int getPage() { return page; }
    public void setPage(int page) { this.page = page; }

    public int getSize() { return size; }
    public void setSize(int size) { this.size = size; }

    public long getTotalElements() { return totalElements; }
    public void setTotalElements(long totalElements) { this.totalElements = totalElements; }

    public int getTotalPages() { return totalPages; }
    public void setTotalPages(int totalPages) { this.totalPages = totalPages; }

    public List<HistoricalDataPointDto> getRecords() { return records; }
    public void setRecords(List<HistoricalDataPointDto> records) { this.records = records; }
}

