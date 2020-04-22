package com.example.bletestapp.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.bletestapp.Helper;
import com.example.bletestapp.R;
import com.example.bletestapp.Tracker;
import com.example.bletestapp.TrackerValue;
import com.example.bletestapp.TrackerValueAdapter;
import com.example.bletestapp.activities.ConnectActivity;
import java.util.ArrayList;

public class DeviceStatusFragment extends Fragment {
    Tracker tracker;
    private TrackerValueAdapter trackerValueAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getActivity().setTitle("Device status");
        return inflater.inflate(R.layout.frag_device_status, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        tracker = ((ConnectActivity)getActivity()).GetTracker();
        updateGUI(view);
        super.onViewCreated(view, savedInstanceState);
    }

    private void updateGUI(View view) {
        int batteryLevel = tracker.getBatteryLevel();
        TextView batteryTextView = view.findViewById(R.id.battery_progress_textview);
        String batteryString = batteryLevel + " %";
        batteryTextView.setText(batteryString);
        ProgressBar batteryProgressBar = view.findViewById(R.id.battery_progress_bar);
        batteryProgressBar.setProgress(batteryLevel);

        TextView configTextView = view.findViewById(R.id.config_version_view);
        TextView firmwareTextView = view.findViewById(R.id.firm_version_view);
        TextView bleTextView = view.findViewById(R.id.ble_version_view);
        configTextView.setText(String.valueOf(tracker.getConfigVersion()));
        firmwareTextView.setText(String.valueOf(tracker.getFirmwareVersion()));
        bleTextView.setText(String.valueOf(tracker.getBleFirmwareVersion()));

        trackerValueAdapter = new TrackerValueAdapter(view.getContext(), tracker.getTrackerValues());
        ListView trackerValuesListView = view.findViewById(R.id.trackerValuesListView);
        trackerValuesListView.setAdapter(trackerValueAdapter);
    }
}
