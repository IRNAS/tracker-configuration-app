package com.example.bletestapp.callback;

import android.bluetooth.BluetoothDevice;
import androidx.annotation.NonNull;
import no.nordicsemi.android.ble.callback.profile.ProfileDataCallback;
import no.nordicsemi.android.ble.data.Data;

public abstract class HtTestDataCallback implements ProfileDataCallback, HtTestCallback {
    private static final int STATE_YES = 0x00;
    private static final int STATE_NO = 0x01;

    @Override
    public void onDataReceived(@NonNull final BluetoothDevice device, @NonNull final Data data) {
        if (data.size() != 1) {
            onInvalidDataReceived(device, data);
            return;
        }

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
    }
}
