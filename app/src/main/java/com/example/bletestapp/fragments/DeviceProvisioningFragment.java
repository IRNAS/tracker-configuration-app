package com.example.bletestapp.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.bletestapp.R;

public class DeviceProvisioningFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView( LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getActivity().setTitle("Provisioning");
        return inflater.inflate(R.layout.frag_device_provisioning, container, false);
    }
}
