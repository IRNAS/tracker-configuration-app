package com.example.bletestapp;

import java.util.ArrayList;

public class Tracker {
    // class fields that have only value - displayed directly
    private int configVersion;
    private int firmwareVersion;
    private int bleFirmwareVersion;

    private int batteryLevel;
    /*
    private int logFileSize;
    private TrackerValue batteryVoltage;
    private TrackerValue batteryCurrent;
     */

    // list of class fields that have name, unit and value - displayed in listview
    private ArrayList<TrackerValue> trackerValues;

    public Tracker() {  // dummy device - put fake values
        configVersion = 1;
        firmwareVersion = 4;
        bleFirmwareVersion = 65704;
        batteryLevel = 69;

        trackerValues = new ArrayList<>();
        trackerValues.add(new TrackerValue("Log file size", String.valueOf(123456), ""));
        trackerValues.add(new TrackerValue("Battery voltage", String.valueOf(3.3), "V"));
        trackerValues.add(new TrackerValue("Battery current", String.valueOf(0.09), "A"));
    }

    public Tracker(int configVersion, int batteryLevel, float voltage, float current) {     // TODO real tracker connected
        this.configVersion = configVersion;
        this.batteryLevel = batteryLevel;

        firmwareVersion = 4;
        bleFirmwareVersion = 65704;

        trackerValues = new ArrayList<>();
        trackerValues.add(new TrackerValue("Log file size", String.valueOf(123456), ""));
        trackerValues.add(new TrackerValue("Battery voltage", String.valueOf(voltage), "V"));
        trackerValues.add(new TrackerValue("Battery current", String.valueOf(current), "A"));
    }

    public String toString() {
        return "status={'cfg_version': " + configVersion
                + ", 'ble_fw_version': " + bleFirmwareVersion
                + ", 'fw_version': " + firmwareVersion + "}"
                + " battery={'battery_level': " + batteryLevel
                + " log_file={'fileSize': " + trackerValues.get(0).getValue() + "}";
    }

    public int getConfigVersion() { return configVersion; }
    public int getFirmwareVersion() { return firmwareVersion; }
    public int getBleFirmwareVersion() { return bleFirmwareVersion; }
    public int getBatteryLevel() { return batteryLevel; }
    public ArrayList<TrackerValue> getTrackerValues() { return trackerValues; }
}
