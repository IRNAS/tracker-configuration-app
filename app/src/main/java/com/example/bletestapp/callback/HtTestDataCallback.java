package com.example.bletestapp.callback;

import static com.example.bletestapp.Helper.LOG_TAG_TEST;

import android.bluetooth.BluetoothDevice;
import android.util.Log;
import androidx.annotation.NonNull;
import no.nordicsemi.android.ble.callback.profile.ProfileDataCallback;
import no.nordicsemi.android.ble.data.Data;

public abstract class HtTestDataCallback implements ProfileDataCallback, HtTestCallback {
    // body sensor location callback

    @Override
    public void onDataReceived(@NonNull final BluetoothDevice device, @NonNull final Data data) {
        if (data.size() != 1) {
            onInvalidDataReceived(device, data);
            return;
        }
        final int key = data.getIntValue(Data.FORMAT_UINT8, 0);
        if (key < 7) {
            bodySensorLocationCharRead(device, key);
        }
        else {
            onInvalidDataReceived(device, data);
        }
        /*
        final int state = data.getIntValue(Data.FORMAT_UINT8, 0);
        if (state == STATE_YES) {
            onTest1StateChanged(device, true);
        }
        else if (state == STATE_NO) {
            onTest1StateChanged(device, false);
        }
        else {
            onInvalidDataReceived(device, data);
        }
         */
    }
}
