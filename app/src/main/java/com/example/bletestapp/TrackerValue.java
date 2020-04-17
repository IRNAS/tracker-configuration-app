package com.example.bletestapp;

public class TrackerValue {
    private String name;
    private String value;
    private String unit;

    TrackerValue() {
        name = "";
        value = "";
        unit = "";
    }

    TrackerValue(String name, String value, String unit) {
        this.name = name;
        this.value = value;
        this.unit = unit;
    }

    String getName() { return name;}
    String getValue() { return value;}
    String getUnit() { return unit;}
}
