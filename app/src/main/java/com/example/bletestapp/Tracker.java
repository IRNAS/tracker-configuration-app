package com.example.bletestapp;

public class Tracker {
    private int configVersion;
    private int firmwareVersion;
    private int bleFirmwareVersion;

    private int batteryLevel;
    private int logFileSize;

    public Tracker(int configVersion) {
        this.configVersion = configVersion;
    }

    public String toString() {
        return "status={'cfg_version': " + configVersion
                + ", 'ble_fw_version': " + bleFirmwareVersion
                + ", 'fw_version': " + firmwareVersion + "}"
                + " battery={'battery_level': " + batteryLevel + "}"
                + " log_file={'fileSize': " + logFileSize + "}";
    }
}
