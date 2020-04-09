package com.example.bletestapp;

import android.app.Activity;
import android.widget.Toast;
import java.util.UUID;

public class Helper {
    public final static String LOG_TAG_TEST = "HEH";

    public static UUID convertUuidFromInt(int i) {
        final long MSB = 0x0000000000001000L;
        final long LSB = 0x800000805f9b34fbL;
        long value = i & 0xFFFFFFFF;
        return new UUID(MSB | (value << 32), LSB);
    }

    public static void displayToast(Activity activity, String message) {
        Toast.makeText(activity, message, Toast.LENGTH_LONG).show();
    }
}
