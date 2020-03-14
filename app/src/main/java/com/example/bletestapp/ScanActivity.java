package com.example.bletestapp;

import static com.example.bletestapp.Helper.LOG_TAG_TEST;

import android.os.Handler;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import no.nordicsemi.android.support.v18.scanner.BluetoothLeScannerCompat;
import no.nordicsemi.android.support.v18.scanner.ScanResult;
import no.nordicsemi.android.support.v18.scanner.ScanSettings;
import no.nordicsemi.android.support.v18.scanner.ScanCallback;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class ScanActivity extends AppCompatActivity {
    private final static int REQUEST_ENABLE_BT = 1;
    private final static int REQUEST_ENABLE_LOCATION = 2;

    // stop scanning after 10 seconds
    private final static long SCAN_PERIOD = 10000;

    private static boolean scanActive;
    private Button scanBtn;
    private BluetoothAdapter bluetoothAdapter;
    private ArrayList<BluetoothDevice> discoveredDevices;
    private ArrayAdapter<BluetoothDevice> discoveredDevsAdapter;
    private ListView discoveredDevsListView;
    private Handler scanHandler;

    // TODO How to detect adapter state change inside scanner BroadcastReceiver?
    // https://github.com/NordicSemiconductor/Android-Scanner-Compat-Library/issues/70

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // scan button init
        scanActive = false;
        scanBtn = findViewById(R.id.start_scan_btn);
        scanBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scanBLE(scanActive);
            }
        });

        // discovered devices holder list, list view and widget init
        discoveredDevices = new ArrayList<>();
        discoveredDevsAdapter = new ArrayAdapter<>(this, R.layout.support_simple_spinner_dropdown_item, discoveredDevices);
        // TODO make custom adapter to show more data (RSSI, device name, advertisement period) besides MAC
        discoveredDevsListView = findViewById(R.id.discoveredDevsListView);
        discoveredDevsListView.setAdapter(discoveredDevsAdapter);
        discoveredDevsListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(LOG_TAG_TEST, "Item click, position: " + position);
                // stop the ongoing scan if it's still active
                if (scanActive) {
                    scanBLE(true);
                }
                // start activity to connect with device - send object with intent
                BluetoothDevice selectedDevice = discoveredDevices.get(position);
                Intent connectIntent = new Intent(ScanActivity.this, ConnectActivity.class);
                connectIntent.putExtra("device", selectedDevice);
                startActivity(connectIntent);
            }
        });

        // TODO implement listener for pull down gesture to empty the list and start scanning again

        // get the bluetooth adapter
        final BluetoothManager bluetoothManager = (BluetoothManager)getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();

        // check if location is enabled and display popup if not
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[] {Manifest.permission.ACCESS_COARSE_LOCATION},
                    REQUEST_ENABLE_LOCATION
            );
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();

        checkBluetooth();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            Toast.makeText(this, "bluetooth needs to be turned on, app will now close", Toast.LENGTH_SHORT).show();
            finish();
        }
        // other request codes to be added
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_ENABLE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length == 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "You need to allow location services, app close", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
            // other permission cases to be added
            default: {
                // do nothing
            }
        }
    }

    private void checkBluetooth() {
        // check if bluetooth is enabled and display popup if not
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
    }

    private void scanBLE(final boolean isActive) {
        if (!isActive) {
            BluetoothLeScannerCompat scanner = BluetoothLeScannerCompat.getScanner();
            ScanSettings settings = new ScanSettings.Builder()
                    .setLegacy(false)
                    .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                    .setReportDelay(1000)
                    .setUseHardwareBatchingIfSupported(true)
                    .build();
            // to just scan for specific services filter can be added
            //List<ScanFilter> filters = new ArrayList<>();
            //filters.add(new ScanFilter.Builder().setServiceUuid(mUuid).build());
            //scanner.startScan(filters, settings, scanCallback);
            scanner.startScan(null, settings, scanCallback);
            // TODO try all different parameters of scanning

            // start scanning timeout
            scanHandler = new Handler();
            scanHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    scanBLE(true);
                }
            }, SCAN_PERIOD);

            scanActive = true;
            scanBtn.setText(R.string.stop_scan);
            // TODO add spinning icon when scanning is active
        }
        else {
            BluetoothLeScannerCompat scanner = BluetoothLeScannerCompat.getScanner();
            scanner.stopScan(scanCallback);

            scanActive = false;
            scanBtn.setText(R.string.start_scan);
        }
    }

    private ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanFailed(int errorCode) {
            Log.i(LOG_TAG_TEST, "Error, scan failed");
        }
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            Log.i(LOG_TAG_TEST,"Scan result!");
        }
        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            Log.i(LOG_TAG_TEST, "Batch scan results: " + results.size());
            updateDiscoveredDevsList(results);
        }
    };

    private void updateDiscoveredDevsList(List<ScanResult> scanResults) {
        for (ScanResult result : scanResults) {
            // add discovered devices to list of bluetooth devices
            BluetoothDevice device = result.getDevice();
            //result.toString();
            if (!discoveredDevices.contains(device)) {
                discoveredDevices.add(device);
            }
        }
        // update list view adapter
        discoveredDevsAdapter.notifyDataSetChanged();
    }
}
