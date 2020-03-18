package com.example.bletestapp;

import static com.example.bletestapp.Helper.LOG_TAG_TEST;
import static com.example.bletestapp.Helper.convertUuidFromInt;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;
import com.example.bletestapp.callback.HtNotifyCallback;
import com.example.bletestapp.callback.HtNotifyDataCallback;
import com.example.bletestapp.callback.HtTestDataCallback;
import java.util.UUID;
import no.nordicsemi.android.ble.BleManager;
import no.nordicsemi.android.ble.data.Data;

public class HtManager extends BleManager<HtManagerCallbacks> {
    //final static UUID SERVICE_UUID = UUID.fromString("0000aaa0-0000-1000-8000-aabbccddeeff");
    //final static UUID FIRST_CHAR = UUID.randomUUID();
    //final static UUID SECOND_CHAR = UUID.fromString("0000aaa1-0000-1000-8000-aabbccddeeff");

    final static UUID HEART_RATE_SERVICE = convertUuidFromInt(0x180d);
    final static UUID HR_MEASUREMENT_CHAR = convertUuidFromInt(0x2a37);
    final static UUID BODY_SENSOR_LOCATION_CHAR = convertUuidFromInt(0x2a38);
    final static UUID HR_CONTROL_POINT_CHAR = convertUuidFromInt(0x2a39);

    // client characteristics (N - notify, R - read, W - write)
    private BluetoothGattCharacteristic hrMeasurementCharN, bodySensorLocationCharR, hrControlPointCharW;

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
        Log.i(LOG_TAG_TEST,"LOG msg: " + message);
        // log only in debug build
        if (BuildConfig.DEBUG || priority == Log.ERROR)     // TODO fix this
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
    private final HtTestDataCallback bodySensorLocationCharRCallback = new HtTestDataCallback() {
        /*
        @Override
        public void onTest1StateChanged(@NonNull BluetoothDevice device, boolean test1) {
            Log.i(LOG_TAG_TEST,"Test 1 new state: " + test1);

        }

        @Override
        public void onLocalTimeInfo(@NonNull final BluetoothDevice device, String time) {
            Log.i(LOG_TAG_TEST,"Time received: " + time);
            mCallbacks.onLocalTimeInfo(device, time);
        }
        */

        @Override
        public void bodySensorLocationCharRead(@NonNull final BluetoothDevice device, final int key) {
            Log.i(LOG_TAG_TEST,"Sensor location: " + key);
            // Note that data is not parsed (1 means Chest)
            mCallbacks.bodySensorLocationCharRead(device, key);
        }

        @Override
        public void onInvalidDataReceived(@NonNull final BluetoothDevice device, @NonNull final Data data) {
            Log.i(LOG_TAG_TEST,"Invalid data received: " + data.toString());
        }
    };

    private final HtNotifyDataCallback hrMeasurementCharNCallback = new HtNotifyDataCallback() {
        @Override
        public void hrMeasurementNotification(@NonNull BluetoothDevice device, int measurement) {
            Log.i(LOG_TAG_TEST,"Current HR: " + measurement);
            mCallbacks.hrMeasurementNotification(device, measurement);
        }

        @Override
        public void onInvalidDataReceived(@NonNull final BluetoothDevice device, @NonNull final Data data) {
            Log.i(LOG_TAG_TEST,"Invalid data received: " + data.toString());
        }
    };

    // BluetoothGatt callbacks object
    private final BleManagerGattCallback htGattCallback = new BleManagerGattCallback() {
        // This method will be called when the device is connected and services are discovered.
        // You need to obtain references to the characteristics and descriptors that you will use.
        // Return true if all required services are found, false otherwise.
        @Override
        public boolean isRequiredServiceSupported(@NonNull final BluetoothGatt gatt) {
            final BluetoothGattService service = gatt.getService(HEART_RATE_SERVICE);
            if (service != null) {
                hrMeasurementCharN = service.getCharacteristic(HR_MEASUREMENT_CHAR);
                bodySensorLocationCharR = service.getCharacteristic(BODY_SENSOR_LOCATION_CHAR);
                hrControlPointCharW = service.getCharacteristic(HR_CONTROL_POINT_CHAR);
            }
            // Validate properties
            boolean notify = false;
            if (hrMeasurementCharN != null) {
                final int properties = hrMeasurementCharN.getProperties();
                notify = (properties & BluetoothGattCharacteristic.PROPERTY_NOTIFY) != 0;
            }

            boolean writeRequest = false;
            if (hrControlPointCharW != null) {
                final int properties = hrControlPointCharW.getProperties();
                writeRequest = (properties & BluetoothGattCharacteristic.PROPERTY_WRITE) != 0;
                hrControlPointCharW.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
            }

            // Return true if all required services have been found
            isSupported = hrMeasurementCharN != null && bodySensorLocationCharR != null && notify && writeRequest;
            //isSupported = hrMeasurementCharN != null && bodySensorLocationCharR != null && hrControlPointCharW != null;
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
            setNotificationCallback(bodySensorLocationCharR)
                    .with(bodySensorLocationCharRCallback);
            readCharacteristic(bodySensorLocationCharR)
                    .with(bodySensorLocationCharRCallback)
                    .enqueue();

            writeCharacteristic(hrControlPointCharW, new byte[] {1, 1})
                    .done(device -> log(Log.INFO, "HR control point data send OK"))
                    .enqueue();

            setNotificationCallback(hrMeasurementCharN)
                    .with(hrMeasurementCharNCallback);
            enableNotifications(hrMeasurementCharN)
                    .enqueue();
        }

        @Override
        protected void onDeviceDisconnected() {
            // Device disconnected. Release your references here
            hrMeasurementCharN = null;
            bodySensorLocationCharR = null;
            hrControlPointCharW = null;
        }
    };
}
