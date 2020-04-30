package com.example.bletestapp.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.bletestapp.Helper;
import com.example.bletestapp.R;
import com.example.bletestapp.activities.ConnectActivity;

public class DeviceSettingsFragment extends Fragment {
    private boolean isDummy;
    private String readConfig;

    // GUI variables
    private EditText settingsEditText;
    private Button saveChangesBtn;
    private Button undoChangesBtn;

    @Nullable
    @Override
    public View onCreateView(
            LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getActivity().setTitle("Settings");
        return inflater.inflate(R.layout.frag_device_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        isDummy = ((ConnectActivity)getActivity()).isDummy;
        settingsEditText = view.findViewById(R.id.edit_text_settings);
        saveChangesBtn = view.findViewById(R.id.save_changes_button);
        saveChangesBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                String editedData = settingsEditText.getText().toString();
                writeConfig(editedData);
            }
        });
        undoChangesBtn = view.findViewById(R.id.undo_changes_button);
        undoChangesBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                settingsEditText.setText(readConfig);
            }
        });

        readConfig();
        super.onViewCreated(view, savedInstanceState);
    }

    private void readConfig() {
        // read data
        if (isDummy) {
            readConfig = Helper.loadAssetFile(getContext(), "test_config.json");
        }
        else {  // TODO read actual data from the tracker
            readConfig = Helper.loadAssetFile(getContext(), "test_config.json");
        }
        // process data string to make it more readable
        readConfig = readConfig.replace(",", ",\n");
        readConfig = readConfig.replace("{", "{\n");
        readConfig = readConfig.replace("}", "\n}");
        readConfig = readConfig.replace("[", "[\n");
        readConfig = readConfig.replace("]", "\n\t]");

        // display in GUI
        settingsEditText.setText(readConfig);
    }

    private void writeConfig(String data) {
        // remove tab and newline chars from string
        data = data.replaceAll("([\n\t])", "");
        // write data
        boolean writeOK = true;     // TODO write actual data to the tracker
        // display result in a toast
        if (writeOK) {
            Helper.displayToast(getActivity(), "Successfully written to the device", false);
        }
        else {
            Helper.displayToast(getActivity(), "Error writing to the device...", false);
        }
    }
}
