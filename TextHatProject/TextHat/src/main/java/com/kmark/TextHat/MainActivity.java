package com.kmark.TextHat;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.widget.Adapter;
import android.widget.CompoundButton;
import android.widget.Switch;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();

        final SharedPreferences sharedPreferences = getSharedPreferences("com.kmark.TextHat_preferences", MODE_PRIVATE);
        sharedPreferences.edit().putBoolean("enabled", sharedPreferences.getBoolean("enabled", false)).commit();
        sharedPreferences.edit().putBoolean("filter", sharedPreferences.getBoolean("filter", false)).commit();
        final Switch mainToggle = (Switch)findViewById(R.id.mainToggle);
        mainToggle.setChecked(sharedPreferences.getBoolean("enabled", false));
        mainToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                sharedPreferences.edit().putBoolean("enabled", isChecked).commit();
                getPackageManager().setComponentEnabledSetting(new ComponentName(MainActivity.this, SMSBroadcastReceiver.class),
                        isChecked ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED : PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                        PackageManager.DONT_KILL_APP);
            }
        });

        final Switch filterSwitch = (Switch)findViewById(R.id.filterSwitch);
        filterSwitch.setChecked(sharedPreferences.getBoolean("filter", false));
        filterSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                sharedPreferences.edit().putBoolean("filter", isChecked).commit();
            }
        });

        if(!adapter.isEnabled()) {
            Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBluetooth, 0);
        }
    }
}
