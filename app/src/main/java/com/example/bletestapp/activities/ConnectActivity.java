package com.example.bletestapp.activities;

import static com.example.bletestapp.Helper.LOG_TAG_TEST;

import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import com.example.bletestapp.Helper;
import com.example.bletestapp.R;
import com.example.bletestapp.Tracker;
import com.example.bletestapp.bluetooth.HtManager;
import com.example.bletestapp.bluetooth.HtManagerCallbacks;
import com.example.bletestapp.fragments.DeviceLiveFragment;
import com.example.bletestapp.fragments.DeviceLogsFragment;
import com.example.bletestapp.fragments.DeviceProvisioningFragment;
import com.example.bletestapp.fragments.DeviceSettingsFragment;
import com.example.bletestapp.fragments.DeviceStatusFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.navigation.NavigationView.OnNavigationItemSelectedListener;

public class ConnectActivity extends AppCompatActivity implements HtManagerCallbacks, OnNavigationItemSelectedListener {
    // timeout connecting after 10 seconds
    private final static long CONNECT_TIMEOUT = 10000;

    // managing variables
    protected Tracker tracker;
    public boolean isDummy;

    // BLE connection variables
    // TODO make it a service
    private BluetoothDevice deviceToConnect;
    private HtManager manager;
    private boolean deviceConnected;
    private String deviceName;

    // GUI variables
    private TextView deviceConnStatusView;
    private DrawerLayout drawer;
    private NavigationView navigationView;
    private TextView waitBleDeviceText;
    private FrameLayout fragmentLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);

        Toolbar toolbar = findViewById(R.id.connect_toolbar);
        setSupportActionBar(toolbar);

        drawer = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        waitBleDeviceText = findViewById(R.id.wait_ble_device_text);
        fragmentLayout = findViewById(R.id.fragment_container);
        fragmentLayout.setVisibility(View.GONE);
        waitBleDeviceText.setVisibility(View.VISIBLE);

        // read the device you are connected to from intent
        Intent intent = getIntent();
        deviceToConnect = intent.getParcelableExtra("device");

        // init variables and GUI elements
        deviceConnected = false;
        View headerView = navigationView.getHeaderView(0);
        deviceConnStatusView = headerView.findViewById(R.id.device_con_status);
        TextView deviceNameView = headerView.findViewById(R.id.device_name);
        if (deviceToConnect == null) {      // dummy device
            isDummy = true;
            deviceName = "Dummy device";
        }
        else {
            isDummy = false;
            // update device name if it exists
            deviceName = deviceToConnect.getName();
            if (deviceName == null) {
                deviceName = "BLE device";
            }
        }
        deviceNameView.setText(deviceName);

        if (!isDummy) {     // real device connected
            deviceConnStatusView.setText("connecting");
            // init custom ble manager
            manager = new HtManager(getApplication());

            // connect to it
            manager.setGattCallbacks(this);
            manager.connect(deviceToConnect)
                    .timeout(CONNECT_TIMEOUT)
                    .useAutoConnect(false)  // TODO additional option to use
                    .retry(3, 100)
                    .enqueue();
        }
        else {  // dummy test device connected
            deviceConnStatusView.setText("connected");
            // init tracker class for dummy device
            tracker = new Tracker();
            tracker.addPosition(new LatLng(34,25));
            tracker.addPosition(new LatLng(35,25));
            tracker.addPosition(new LatLng(38,27));

            // display the fragment device status
            if (savedInstanceState == null) {
                openDefaultFragment();
            }
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int curSelectedItem = navigationView.getCheckedItem().getItemId();
        int clickedItem = item.getItemId();
        if (curSelectedItem != clickedItem) {   // if item is already selected, don't allow the action
            switch (clickedItem) {
                case R.id.device_status:
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new DeviceStatusFragment()).commit();
                    break;
                case R.id.logs:
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new DeviceLogsFragment()).commit();
                    break;
                case R.id.live:
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new DeviceLiveFragment()).commit();
                    break;
                case R.id.provisioning:
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new DeviceProvisioningFragment()).commit();
                    break;
                case R.id.settings:
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new DeviceSettingsFragment()).commit();
                    break;
            }
        }
        //drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /*
    @Override
    public void onTest1StateChanged(@NonNull BluetoothDevice device, boolean test1) {}
    */
    /*
    @Override
    public void onLocalTimeInfo(@NonNull final BluetoothDevice device, String time) {}
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
        Helper.displayToast(this, "Successfully connected to " + deviceName, true);
        deviceConnStatusView.setText("connected");
        // TODO read all available GATT services
        // TODO read all available GATT chars and descriptors

        tracker = new Tracker(1, 56, 2.8f, 0.012f);
        // TODO put values in the class
        openDefaultFragment();
    }

    @Override
    public void onDeviceDisconnecting(@NonNull BluetoothDevice device) {
        Log.d(LOG_TAG_TEST, "Method called: onDeviceDisconnecting");
    }

    @Override
    public void onDeviceDisconnected(@NonNull BluetoothDevice device) {
        deviceConnected = false;
        manager.close();
        Toast.makeText(this, "Device disconnected", Toast.LENGTH_SHORT).show();
        finish();
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

    public Tracker GetTracker() {
        return tracker;
    }

    private void openDefaultFragment() {
        fragmentLayout.setVisibility(View.VISIBLE);
        waitBleDeviceText.setVisibility(View.GONE);

        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new DeviceStatusFragment()).commit();
        navigationView.setCheckedItem(R.id.device_status);
    }
}
