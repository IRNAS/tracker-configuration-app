package com.example.bletestapp.activities;

import static com.example.bletestapp.Helper.LOG_TAG_TEST;
import static com.example.bletestapp.Helper.REQUEST_ENABLE_BT;
import static com.example.bletestapp.Helper.REQUEST_ENABLE_LOCATION;
import static com.example.bletestapp.Helper.displayToast;

import android.Manifest.permission;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.example.bletestapp.Helper;
import com.example.bletestapp.R;
import com.example.bletestapp.bluetooth.BleScanResultAdapter;
import no.nordicsemi.android.support.v18.scanner.BluetoothLeScannerCompat;
import no.nordicsemi.android.support.v18.scanner.ScanResult;
import no.nordicsemi.android.support.v18.scanner.ScanSettings;
import no.nordicsemi.android.support.v18.scanner.ScanCallback;

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

import java.util.ArrayList;
import java.util.List;

public class ScanActivity extends AppCompatActivity {
    // stop scanning after 30 seconds
    private final static long SCAN_PERIOD = 30000;

    // GUI elements
    private Menu mainMenu;
    private SwipeRefreshLayout refreshLayout;
    private SwipeRefreshLayout refreshLayoutEmpty;
    private ListView discoveredDevsListView;

    // location globals
    private boolean locationPermissionGranted;

    // BLE globals
    private static boolean bluetoothPermissionGranted;
    private static boolean scanActive;
    private BluetoothAdapter bluetoothAdapter;
    private ArrayList<ScanResult> discoveredDevices;
    private BleScanResultAdapter discoveredDevsAdapter;
    private Handler scanHandler;

    // TODO How to detect adapter state change inside scanner BroadcastReceiver?
    // https://github.com/NordicSemiconductor/Android-Scanner-Compat-Library/issues/70
    // TODO BLE device names are sometimes not displayed
    // TODO add filter to show only tracker devices

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            // TODO: handle specifics if fresh app run
        }

        bluetoothPermissionGranted = false;
        locationPermissionGranted = false;
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
                // stop the scan if running
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
        refreshLayout.setOnRefreshListener(this::RefreshListViewAndStartScan);

        refreshLayoutEmpty = findViewById(R.id.refreshScanLayoutEmpty);
        refreshLayoutEmpty.setOnRefreshListener(this::RefreshListViewAndStartScan);
        discoveredDevsListView.setEmptyView(refreshLayoutEmpty);

        // get the bluetooth adapter
        final BluetoothManager bluetoothManager = (BluetoothManager)getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();

        // check if bluetooth is enabled and display popup if not
        if (!bluetoothPermissionGranted) {
            checkBluetooth();
        }

        // check if location is enabled and display popup if not
        if (!locationPermissionGranted) {
            getLocationPermission();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // update list view adapter
        discoveredDevsAdapter.notifyDataSetChanged();
        // check if it is enabled and start scan
        if (!scanActive && bluetoothPermissionGranted && locationPermissionGranted) {
                scanBLE(false);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        // stop the ongoing scan if it's still active
        if (scanActive) {
            scanBLE(true);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_ENABLE_BT: {
                if (resultCode == Activity.RESULT_CANCELED) {
                    scanBLE(true);  // stop the scan
                    bluetoothPermissionGranted = false;
                    Helper.displayToast(this,
                            "Bluetooth needs to be enabled to discover actual BLE devices.", true);
                } else if (resultCode == Activity.RESULT_OK) {
                    bluetoothPermissionGranted = true;
                    if (!scanActive) {      // start new scan if not already running
                        scanBLE(false);
                    }
                }
                break;
            }
            default: {
                // do nothing
            }
        }
        // other request codes to be added
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_ENABLE_LOCATION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    locationPermissionGranted = true;
                }
                else {  // If request is cancelled, the result arrays are empty.
                    Helper.displayToast(
                            this,
                            "Location services need to be enabled to discover actual BLE devices.",
                            true
                    );
                }
                break;
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
            case R.id.menu_button:
                // TODO check for permissions also here
                if (!bluetoothPermissionGranted) {
                    displayToast(this, "Bluetooth is disabled, enable it or use dummy device option from the main menu.", true);
                }
                else if (!locationPermissionGranted) {
                    displayToast(this, "Location access is disabled, enable it or use dummy device option from the main menu.", true);
                }
                else {
                    scanBLE(scanActive);
                }
                return true;
            case R.id.devices_list:
                activityIntent = new Intent(this, ScanActivity.class);
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
            case R.id.dummy:
                Intent connectIntent = new Intent(ScanActivity.this, ConnectActivity.class);
                connectIntent.putExtra("device", "dummy");
                startActivity(connectIntent);
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
        else {  // bluetooth is enabled
            bluetoothPermissionGranted = true;
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
            // TODO handle exception: java.lang.IllegalArgumentException: scanner already started with given callback
            scanActive = true;

            // start scanning timeout
            scanHandler = new Handler();
            scanHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    scanBLE(true);
                }
            }, SCAN_PERIOD);

            updateMenuButtonTitle();
        }
        else {
            BluetoothLeScannerCompat scanner = BluetoothLeScannerCompat.getScanner();
            scanner.stopScan(scanCallback);

            scanActive = false;
            updateMenuButtonTitle();
        }
        // stop refresh animation
        refreshLayout.setRefreshing(false);
        refreshLayoutEmpty.setRefreshing(false);
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

    private void updateMenuButtonTitle() {
        if (mainMenu != null) {
            MenuItem item = mainMenu.findItem(R.id.menu_button);
            if (scanActive) {
                item.setTitle(R.string.scan_running);
            }
            else {
                item.setTitle(R.string.scan_stopped);
            }
        }
    }

    private void RefreshListViewAndStartScan() {
        if (bluetoothPermissionGranted && locationPermissionGranted) {
            if (!discoveredDevices.isEmpty()) {
                // ignore refresh trigger if discoveredDevices is already empty
                discoveredDevices.clear();
                // update list view adapter
                discoveredDevsAdapter.notifyDataSetChanged();
            }
            if (scanActive) {   // stop the scan if running
                scanBLE(true);
            }
            scanBLE(false); // and start new scan
        }
        else {
            onOptionsItemSelected(mainMenu.findItem(R.id.menu_button));
            refreshLayoutEmpty.setRefreshing(false);
        }
    }

    private void getLocationPermission() {
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true;
        }
        else {
            ActivityCompat.requestPermissions(
                    this,
                    new String[] {permission.ACCESS_FINE_LOCATION},
                    REQUEST_ENABLE_LOCATION
            );
        }
    }
}
