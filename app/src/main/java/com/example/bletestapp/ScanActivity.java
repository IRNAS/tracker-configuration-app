package com.example.bletestapp;

import static com.example.bletestapp.Helper.LOG_TAG_TEST;

import android.os.Handler;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener;
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

    // GUI elements
    private Menu mainMenu;
    private SwipeRefreshLayout refreshLayout;
    private ListView discoveredDevsListView;

    // BLE globals
    private static boolean scanActive;
    private BluetoothAdapter bluetoothAdapter;
    private ArrayList<ScanResult> discoveredDevices;
    private BleScanResultAdapter discoveredDevsAdapter;
    private Handler scanHandler;

    // TODO How to detect adapter state change inside scanner BroadcastReceiver?
    // https://github.com/NordicSemiconductor/Android-Scanner-Compat-Library/issues/70

    // TODO move all toasts to a single function

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            // TODO: handle specifics if fresh app run
        }

        scanActive = false;
        // discovered devices holder list, list view and widget init
        discoveredDevices = new ArrayList<>();
        // use custom adapter to show desired data (device name, MAC, RSSI, advertisement period)
        discoveredDevsAdapter = new BleScanResultAdapter(this, discoveredDevices);
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
                BluetoothDevice selectedDevice = discoveredDevices.get(position).getDevice();
                Intent connectIntent = new Intent(ScanActivity.this, ConnectActivity.class);
                connectIntent.putExtra("device", selectedDevice);
                startActivity(connectIntent);
            }
        });

        // refresh layout init, pull down gesture to empty the list and start scanning again
        refreshLayout = findViewById(R.id.refreshScanLayout);
        refreshLayout.setOnRefreshListener(() -> {
            // ignore refresh trigger if discoveredDevices is already empty
            if (!discoveredDevices.isEmpty()) {     // TODO fix this
                discoveredDevices.clear();
                // update list view adapter
                discoveredDevsAdapter.notifyDataSetChanged();
                if (scanActive) {   // stop the scan if running
                    scanBLE(true);
                }
                scanBLE(false); // and start new scan
            }
            else {
                refreshLayout.setRefreshing(false);
            }
        });

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
        if (!scanActive) {      // start new scan if not already running
            scanBLE(false);
        }
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        mainMenu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent activityIntent;
        switch (item.getItemId()) {
            case R.id.menu_button:  // TODO hide this in other activities
                scanBLE(scanActive);
                return true;
            case R.id.devices_list:
                activityIntent = new Intent(this, ScanActivity.class);  // TODO kill current activity ?
                startActivity(activityIntent);
                return true;
            case R.id.wizards:
                activityIntent = new Intent(this, WizardsActivity.class);
                startActivity(activityIntent);
                return true;
            case R.id.help:
                activityIntent = new Intent(this, HelpActivity.class);
                startActivity(activityIntent);
                return true;
            case R.id.advanced:
                activityIntent = new Intent(this, AdvancedActivity.class);
                startActivity(activityIntent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
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
            if (mainMenu != null) {
                mainMenu.findItem(R.id.menu_button).setTitle(R.string.scan_running);
            }
        }
        else {
            BluetoothLeScannerCompat scanner = BluetoothLeScannerCompat.getScanner();
            scanner.stopScan(scanCallback);

            scanActive = false;
            mainMenu.findItem(R.id.menu_button).setTitle(R.string.scan_stopped);    // TODO put this to function
        }
        // stop refresh animation
        refreshLayout.setRefreshing(false);
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
            boolean found = false;
            for (ScanResult old_result : discoveredDevices) {   // check if new or old device
                if (result.getDevice().equals(old_result.getDevice())) {
                    found = true;   // device was already found in previous scans
                    if (result.getRssi() != old_result.getRssi()) {
                        // old device, but new scan values: replace it
                        int position = discoveredDevices.indexOf(old_result);
                        discoveredDevices.remove(old_result);
                        discoveredDevices.add(position, result);
                    }
                    break;
                }
            }
            if (!found) {   // new device, add to list
                discoveredDevices.add(result);
            }
        }
        // update list view adapter
        discoveredDevsAdapter.notifyDataSetChanged();
    }
}
