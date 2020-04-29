package com.example.bletestapp.fragments;

import static com.example.bletestapp.Helper.LOG_TAG_TEST;
import static com.example.bletestapp.Helper.MAP_VIEW_BUNDLE_KEY;
import static com.example.bletestapp.Helper.REQUEST_ERROR_DIALOG;

import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.bletestapp.Helper;
import com.example.bletestapp.R;
import com.example.bletestapp.Tracker;
import com.example.bletestapp.activities.ConnectActivity;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import java.util.ArrayList;
import java.util.List;

public class DeviceLiveFragment extends Fragment implements OnMapReadyCallback {
    Tracker tracker;

    // GUI variables
    private MapView mapView;
    private GoogleMap mGoogleMap;
    private LatLngBounds.Builder boundsBuilder;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_device_live, container, false);
        getActivity().setTitle("Live");
        if(!isServicesOK()) {
            // TODO display Maps not working in GUI
        }
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        mapView = view.findViewById(R.id.tracker_map);
        tracker = ((ConnectActivity)getActivity()).GetTracker();
        initGoogleMap(savedInstanceState);
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        Bundle mapViewBundle = outState.getBundle(MAP_VIEW_BUNDLE_KEY);
        if (mapViewBundle == null) {
            mapViewBundle = new Bundle();
            outState.putBundle(MAP_VIEW_BUNDLE_KEY, mapViewBundle);
        }
        mapView.onSaveInstanceState(mapViewBundle);
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onPause() {
        mapView.onPause();
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onDestroy() {
        mapView.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        //googleMap.addMarker(new MarkerOptions().position(new LatLng(0,0)).title("Test Marker"));
        googleMap.setMyLocationEnabled(true);
        mGoogleMap = googleMap;
        setCameraView();
        mGoogleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
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
            Helper.displayToast(getActivity(), "Google Play services error, cannot display Google Maps", false);
        }
        return false;
    }

    private void initGoogleMap(Bundle savedInstanceState) {
        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAP_VIEW_BUNDLE_KEY);
        }
        mapView.onCreate(mapViewBundle);
        mapView.getMapAsync(this);
    }

    private void setCameraView() {
        boundsBuilder = new LatLngBounds.Builder();
        List<Marker> markersList = new ArrayList<>();
        int i = 1;
        for (LatLng loc : tracker.getPositions()) {
            Marker marker = mGoogleMap.addMarker(new MarkerOptions().position(loc).title("Loc " + i));
            markersList.add(marker);
            i++;
            boundsBuilder.include(loc);
        }
        int padding = 100;
        LatLngBounds bounds = boundsBuilder.build();
        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding));
    }
}
