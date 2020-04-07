package com.example.bletestapp;

import android.content.Intent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

public class WizardsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wizards);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent activityIntent;
        switch (item.getItemId()) {
            case R.id.devices_list:
                activityIntent = new Intent(this, ScanActivity.class);
                startActivity(activityIntent);
                return true;
            case R.id.wizards:
                activityIntent = new Intent(this, WizardsActivity.class);
                startActivity(activityIntent);
                return true;
            case R.id.help:
                activityIntent = new Intent(this, HelpActivity.class);
                startActivity(activityIntent);
                return true;
            case R.id.advanced:
                activityIntent = new Intent(this, AdvancedActivity.class);
                startActivity(activityIntent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
