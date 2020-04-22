package com.example.bletestapp.callback;

import android.bluetooth.BluetoothDevice;
import androidx.annotation.NonNull;

public interface HtNotifyCallback {
    void trackerDataNotification(@NonNull final BluetoothDevice device, final int measurement);
}
