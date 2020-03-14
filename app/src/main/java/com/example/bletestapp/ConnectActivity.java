package com.example.bletestapp;

import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

public class ConnectActivity extends AppCompatActivity {

    private BluetoothDevice connectedDevice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);

        // read the device you are connected to from intent
        Intent intent = getIntent();
        connectedDevice = intent.getParcelableExtra("device");
    }
}
