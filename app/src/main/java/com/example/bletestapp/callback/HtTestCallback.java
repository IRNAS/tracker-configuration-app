package com.example.bletestapp.callback;

import android.bluetooth.BluetoothDevice;
import androidx.annotation.NonNull;

public interface HtTestCallback {

    /**
     * Called when Test characteristic changes
     *
     * @param device the target device.
     * @param key int, values between 0 and 6
     */
    //void onTest1StateChanged(@NonNull final BluetoothDevice device, final boolean test1);
    //void onLocalTimeInfo(@NonNull final BluetoothDevice device, final String time);
    void bodySensorLocationCharRead(@NonNull final BluetoothDevice device, final int key);
}
