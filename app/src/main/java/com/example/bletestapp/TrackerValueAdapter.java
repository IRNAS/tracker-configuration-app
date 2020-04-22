package com.example.bletestapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import java.util.ArrayList;

public class TrackerValueAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<TrackerValue> resultList;
    private TextView name, value, unit;

    public TrackerValueAdapter(Context context, ArrayList<TrackerValue> resultList) {
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
        convertView = LayoutInflater.from(context).inflate(R.layout.device_status_row, parent, false);
        name = convertView.findViewById(R.id.parameter_name);
        value = convertView.findViewById(R.id.parameter_value);
        unit = convertView.findViewById(R.id.parameter_unit);
        // apply data to textview fields
        name.setText(resultList.get(position).getName());
        value.setText(resultList.get(position).getValue());
        unit.setText(resultList.get(position).getUnit());
        return convertView;
    }
}
