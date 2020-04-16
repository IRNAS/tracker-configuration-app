package com.example.bletestapp.bluetooth;

import com.example.bletestapp.callback.HtNotifyCallback;
import com.example.bletestapp.callback.HtTestCallback;
import no.nordicsemi.android.ble.BleManagerCallbacks;

public interface HtManagerCallbacks extends BleManagerCallbacks, HtTestCallback, HtNotifyCallback { }
