package com.example.bletestapp;

import static com.example.bletestapp.Helper.LOG_TAG_TEST;

import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

public class ConnectActivity extends AppCompatActivity implements HtManagerCallbacks{
    // timeout connecting after 10 seconds
    private final static long CONNECT_TIMEOUT = 10000;

    // TODO make it a service
    private BluetoothDevice deviceToConnect;
    private HtManager manager;
    private boolean deviceConnected;
    private String deviceName;

    // TODO display read characteristics in GUI

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);

        // read the device you are connected to from intent
        Intent intent = getIntent();
        deviceToConnect = intent.getParcelableExtra("device");

        // init custom ble manager
        manager = new HtManager(getApplication());

        // init variables
        deviceConnected = false;
        deviceName = deviceToConnect.getName();

        // connect to it
        manager.setGattCallbacks(this);
        manager.connect(deviceToConnect)
                .timeout(CONNECT_TIMEOUT)
                .useAutoConnect(false)  // TODO additional option to use
                .retry(3, 100)
                .enqueue();

    }
    /*
    @Override
    public void onTest1StateChanged(@NonNull BluetoothDevice device, boolean test1) {
        Log.d(LOG_TAG_TEST, "Method called: onTest1StateChanged");
    }
    */
    /*
    @Override
    public void onLocalTimeInfo(@NonNull final BluetoothDevice device, String time) {
        Log.d(LOG_TAG_TEST, "Method called: onLocalTimeInfo");
    }
    */

    @Override
    public void bodySensorLocationCharRead(@NonNull final BluetoothDevice device, int key) {
        Log.d(LOG_TAG_TEST, "Method called: bodySensorLocationCharRead");
    }

    @Override
    public void hrMeasurementNotification(@NonNull BluetoothDevice device, int measurement) {
        Log.d(LOG_TAG_TEST, "Method called: hrMeasurementNotification");
    }

    @Override
    public void onDeviceConnecting(@NonNull BluetoothDevice device) {
        Log.d(LOG_TAG_TEST, "Method called: onDeviceConnecting");
    }

    @Override
    public void onDeviceConnected(@NonNull BluetoothDevice device) {
        deviceConnected = true;
        Log.d(LOG_TAG_TEST, "Method called: onDeviceConnected");
    }

    @Override
    public void onDeviceDisconnecting(@NonNull BluetoothDevice device) {
        Log.d(LOG_TAG_TEST, "Method called: onDeviceDisconnecting");
    }

    @Override
    public void onDeviceDisconnected(@NonNull BluetoothDevice device) {
        deviceConnected = false;
        manager.close();
        Log.d(LOG_TAG_TEST, "Method called: onDeviceDisconnected");
    }

    @Override
    public void onLinkLossOccurred(@NonNull BluetoothDevice device) {
        deviceConnected = false;
        Log.d(LOG_TAG_TEST, "Method called: onLinkLossOccurred");
    }

    @Override
    public void onServicesDiscovered(@NonNull BluetoothDevice device, boolean optionalServicesFound) {
        Log.d(LOG_TAG_TEST, "Method called: onServicesDiscovered");
    }

    @Override
    public void onDeviceReady(@NonNull BluetoothDevice device) {
        Log.d(LOG_TAG_TEST, "Method called: onDeviceReady");
    }

    @Override
    public void onBondingRequired(@NonNull BluetoothDevice device) {
        Log.d(LOG_TAG_TEST, "Method called: onBondingRequired");
    }

    @Override
    public void onBonded(@NonNull BluetoothDevice device) {
        Log.d(LOG_TAG_TEST, "Method called: onBonded");
    }

    @Override
    public void onBondingFailed(@NonNull BluetoothDevice device) {
        Log.d(LOG_TAG_TEST, "Method called: onBondingFailed");
    }

    @Override
    public void onError(@NonNull BluetoothDevice device, @NonNull String message, int errorCode) {
        Log.d(LOG_TAG_TEST, "Error occurred: " + message + ", error code: " + errorCode);
    }

    @Override
    public void onDeviceNotSupported(@NonNull BluetoothDevice device) {
        Log.d(LOG_TAG_TEST, "Method called: onDeviceNotSupported");
    }

    // disconnect when back button is pressed (user returns to scan activity)
    @Override
    public void onBackPressed() {
        if (deviceConnected) {
            manager.disconnect().enqueue();
        }
        super.onBackPressed();
    }
}
