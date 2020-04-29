package com.example.bletestapp;

import android.app.Activity;
import android.widget.Toast;
import java.util.UUID;

public class Helper {
    public final static String LOG_TAG_TEST = "HEH";

    // global constants
    public final static int REQUEST_ENABLE_BT = 1001;
    public final static int REQUEST_ENABLE_LOCATION = 1002;
    public final static int REQUEST_ERROR_DIALOG = 1003;
    public final static String MAP_VIEW_BUNDLE_KEY = "MapViewBundleKey";

    public static UUID convertUuidFromInt(int i) {
        final long MSB = 0x0000000000001000L;
        final long LSB = 0x800000805f9b34fbL;
        long value = i & 0xFFFFFFFF;
        return new UUID(MSB | (value << 32), LSB);
    }

    public static void displayToast(Activity activity, String message, boolean length_long) {
        if (length_long) {
            Toast.makeText(activity, message, Toast.LENGTH_LONG).show();
        }
        else {
            Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
        }
    }
}
