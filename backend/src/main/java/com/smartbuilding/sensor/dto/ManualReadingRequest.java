package com.smartbuilding.sensor.dto;

import jakarta.validation.constraints.NotNull;

public class ManualReadingRequest {

    @NotNull(message = "Reading value is required")
    private Double readingValue;

    public ManualReadingRequest() {}

    public ManualReadingRequest(Double readingValue) {
        this.readingValue = readingValue;
    }

    public Double getReadingValue() {
        return readingValue;
    }

    public void setReadingValue(Double readingValue) {
        this.readingValue = readingValue;
    }
}
