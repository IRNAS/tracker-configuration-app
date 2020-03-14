package com.example.bletestapp;

import static com.example.bletestapp.Helper.LOG_TAG_TEST;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;
import com.example.bletestapp.callback.HtTestDataCallback;
import java.util.UUID;
import no.nordicsemi.android.ble.BleManager;
import no.nordicsemi.android.ble.data.Data;

public class HtManager extends BleManager<HtManagerCallbacks> {
    final static UUID SERVICE_UUID = UUID.randomUUID();
    final static UUID FIRST_CHAR = UUID.randomUUID();
    final static UUID SECOND_CHAR = UUID.randomUUID();

    // client characteristics
    private BluetoothGattCharacteristic firstCharacteristic, secondCharacteristic, thirdCharacteristic;

    private boolean isSupported;

    HtManager(@NonNull final Context context) {
        super(context);
    }

    @NonNull
    @Override
    protected BleManagerGattCallback getGattCallback() {
        return htGattCallback;
    }

    @Override
    public void log(final int priority, @NonNull final String message) {
        // log only in debug build
        if (BuildConfig.DEBUG || priority == Log.ERROR)
            Log.println(priority, "HtBleManager", message);
    }

    @Override
    protected boolean shouldClearCacheWhenDisconnected() {
        return !isSupported;
    }

    /**
     * The HtTest callback will be notified when a notification from Test characteristic
     * has been received, or its data was read.
     * <p>
     * If the data received are valid (single byte equal to 0x00 or 0x01), the
     * HtTestDataCallback onTest1CharacteristicChanged will be called.
     * Otherwise, the HtTestDataCallback onInvalidDataReceived(BluetoothDevice, Data)
     * will be called with the data received.
     */
    private final HtTestDataCallback secondTestCallback = new HtTestDataCallback() {
        @Override
        public void onTest1StateChanged(@NonNull BluetoothDevice device, boolean test1) {
            Log.i(LOG_TAG_TEST,"Test 1 new state: " + test1);

        }
        @Override
        public void onInvalidDataReceived(@NonNull final BluetoothDevice device, @NonNull final Data data) {
            Log.i(LOG_TAG_TEST,"Invalid data received: " + data);
        }
    };

    // BluetoothGatt callbacks object
    private final BleManagerGattCallback htGattCallback = new BleManagerGattCallback() {
        // This method will be called when the device is connected and services are discovered.
        // You need to obtain references to the characteristics and descriptors that you will use.
        // Return true if all required services are found, false otherwise.
        @Override
        public boolean isRequiredServiceSupported(@NonNull final BluetoothGatt gatt) {
            final BluetoothGattService service = gatt.getService(SERVICE_UUID);
            if (service != null) {
                //firstCharacteristic = service.getCharacteristic(FIRST_CHAR);
                secondCharacteristic = service.getCharacteristic(SECOND_CHAR);
            }
            // Validate properties
            /*
            boolean notify = false;
            if (firstCharacteristic != null) {
                final int properties = dataCharacteristic.getProperties();
                notify = (properties & BluetoothGattCharacteristic.PROPERTY_NOTIFY) != 0;
            }

            boolean writeRequest = false;
            if (secondCharacteristic != null) {
                final int properties = controlPointCharacteristic.getProperties();
                writeRequest = (properties & BluetoothGattCharacteristic.PROPERTY_WRITE) != 0;
                secondCharacteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
            }
            */
            // Return true if all required services have been found
            //isSupported = firstCharacteristic != null && secondCharacteristic != null && notify && writeRequest;
            isSupported = secondCharacteristic != null;
            return isSupported;
        }

        // If you have any optional services, allocate them here. Return true only if
        // they are found.
        @Override
        protected boolean isOptionalServiceSupported(@NonNull final BluetoothGatt gatt) {
            return super.isOptionalServiceSupported(gatt);
        }

        // Initialize your device here. Often you need to enable notifications and set required
        // MTU or write some initial data. Do it here.
        @Override
        protected void initialize() {
            //setNotificationCallback(firstCharacteristic).with(firstCharacteristicCallback);
            readCharacteristic(secondCharacteristic).with(secondTestCallback);  //.enqueue();

            //byte[] bytes = "test send".getBytes();
            //writeCharacteristic(thirdCharacteristic, bytes).with(thirdCharacteristicCallback).enqueue();

            //enableNotifications(firstCharacteristic).enqueue();
        }

        @Override
        protected void onDeviceDisconnected() {
            // Device disconnected. Release your references here
            //firstCharacteristic = null;
            secondCharacteristic = null;
            //thirdCharacteristic = null;
        }
    };
}
