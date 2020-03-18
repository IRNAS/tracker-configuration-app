package com.example.bletestapp.callback;

import android.bluetooth.BluetoothDevice;
import androidx.annotation.NonNull;
import no.nordicsemi.android.ble.callback.profile.ProfileDataCallback;
import no.nordicsemi.android.ble.data.Data;

public abstract class HtNotifyDataCallback implements ProfileDataCallback, HtNotifyCallback {
    // heart rate measurement value callback

    @Override
    public void onDataReceived(@NonNull final BluetoothDevice device, @NonNull final Data data) {
        if (data.size() != 2) {
            onInvalidDataReceived(device, data);
            return;
        }
        final int value = data.getIntValue(Data.FORMAT_UINT8, 1);
        if (value > 0 && value < 200) {
            hrMeasurementNotification(device, value);
        }
        else {
            onInvalidDataReceived(device, data);
        }
    }
}
