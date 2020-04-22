package com.example.bletestapp.callback;

import android.bluetooth.BluetoothDevice;
import android.util.Log;
import androidx.annotation.NonNull;
import com.example.bletestapp.Helper;
import no.nordicsemi.android.ble.callback.profile.ProfileDataCallback;
import no.nordicsemi.android.ble.data.Data;

public abstract class HtNotifyDataCallback implements ProfileDataCallback, HtNotifyCallback {
    // notify data value callback

    @Override
    public void onDataReceived(@NonNull final BluetoothDevice device, @NonNull final Data data) {
        Log.d(Helper.LOG_TAG_TEST, data.toString());
        if (data.size() != 2) {
            onInvalidDataReceived(device, data);
            return;
        }
        final int value = data.getIntValue(Data.FORMAT_UINT8, 1);
        if (value > 0 && value < 200) {
            trackerDataNotification(device, value);
        }
        else {
            onInvalidDataReceived(device, data);
        }
    }
}
