package com.example.bletestapp.callback;

import android.bluetooth.BluetoothDevice;
import androidx.annotation.NonNull;

public interface HtNotifyCallback {
    void hrMeasurementNotification(@NonNull final BluetoothDevice device, final int measurement);
}
