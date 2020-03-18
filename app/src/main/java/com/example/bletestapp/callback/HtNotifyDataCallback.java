package com.example.bletestapp.callback;

import android.bluetooth.BluetoothDevice;
import androidx.annotation.NonNull;
import no.nordicsemi.android.ble.callback.profile.ProfileDataCallback;
import no.nordicsemi.android.ble.data.Data;

public abstract class HtNotifyDataCallback implements ProfileDataCallback, HtNotifyCallback {
    // heart rate measurement value callback

    @Override
    public void onDataReceived(@NonNull final BluetoothDevice device, @NonNull final Data data) {
        if (data.size() != 1) {
            onInvalidDataReceived(device, data);
            return;
        }
        final int value = data.getIntValue(Data.FORMAT_UINT8, 0);
        if (value > 0 && value < 200) {
            hrMeasurementNotification(device, value);
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
