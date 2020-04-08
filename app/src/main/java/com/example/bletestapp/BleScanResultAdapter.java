package com.example.bletestapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import no.nordicsemi.android.support.v18.scanner.ScanResult;

public class BleScanResultAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<ScanResult> resultList;
    private TextView name, mac, rssi;   //advertising_interval

    public BleScanResultAdapter(Context context, ArrayList<ScanResult> resultList) {
        this.context = context;
        this.resultList = resultList;
    }

    @Override
    public int getCount() {
        return resultList.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // get textview fields
        convertView = LayoutInflater.from(context).inflate(R.layout.scan_row, parent, false);
        name = convertView.findViewById(R.id.scan_dev_name);
        mac = convertView.findViewById(R.id.scan_dev_mac);
        rssi = convertView.findViewById(R.id.scan_dev_rssi);
        //advertising_interval = convertView.findViewById(R.id.scan_dev_adv_period);
        // get data from scan result list
        String device_name = resultList.get(position).getDevice().getName();
        if (device_name == null) {  // if name was not found, try to get device alias
            try {
                Method method = resultList.get(position).getDevice().getClass().getMethod("getAliasName");
                if(method != null) {
                    device_name = (String)method.invoke(resultList.get(position).getDevice());
                }
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            // if it is still null - write N/A instead
            if (device_name == null) {
                device_name = "N/A";
            }
        }
        // apply data to textview fields    // TODO add text what field is showing (expect device name)
        name.setText(device_name);
        mac.setText(resultList.get(position).getDevice().getAddress());
        rssi.setText(String.valueOf(resultList.get(position).getRssi()));
        //advertising_interval.setText(String.valueOf(resultList.get(position).getPeriodicAdvertisingInterval()));
        return convertView;
    }
}
