package com.example.bletestapp.fragments;

import static com.example.bletestapp.Helper.LOG_TAG_TEST;
import static com.example.bletestapp.Helper.REQUEST_ERROR_DIALOG;

import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.bletestapp.Helper;
import com.example.bletestapp.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

public class DeviceLiveFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getActivity().setTitle("Live");
        isServicesOK(); // TODO use return value
        return inflater.inflate(R.layout.frag_device_logs, container, false);
    }

    public boolean isServicesOK(){
        Log.d(LOG_TAG_TEST, "isServicesOK: checking google services version");

        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(getContext());

        if (available == ConnectionResult.SUCCESS){
            //everything is fine and the user can make map requests
            Log.d(LOG_TAG_TEST, "isServicesOK: Google Play Services is working");
            return true;
        }
        else if (GoogleApiAvailability.getInstance().isUserResolvableError(available)){
            //an error occured but we can resolve it
            Log.d(LOG_TAG_TEST, "isServicesOK: an error occured but we can fix it");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(getActivity(), available, REQUEST_ERROR_DIALOG);
            dialog.show();
        }
        else{
            Helper.displayToast(getActivity(), "You can't make map requests", false);
        }
        return false;
    }
}
