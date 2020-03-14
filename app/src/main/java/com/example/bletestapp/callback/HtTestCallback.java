package com.example.bletestapp.callback;

import android.bluetooth.BluetoothDevice;
import androidx.annotation.NonNull;

public interface HtTestCallback {

    /**
     * Called when Test characteristic changes
     *
     * @param device the target device.
     * @param test1 true if yes, false if no
     */
    void onTest1StateChanged(@NonNull final BluetoothDevice device, final boolean test1);
}
