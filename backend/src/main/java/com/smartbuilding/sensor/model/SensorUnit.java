package com.smartbuilding.sensor.model;

public enum SensorUnit {
    CELSIUS("°C"),
    PERCENT("%"),
    PPM("ppm"),
    AQI("AQI"),
    PEOPLE("people");

    private final String symbol;

    SensorUnit(String symbol) {
        this.symbol = symbol;
    }

    public String getSymbol() {
        return symbol;
    }
}

