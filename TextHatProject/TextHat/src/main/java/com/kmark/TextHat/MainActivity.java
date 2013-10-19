package com.kmark.TextHat;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.SharedPreferences;
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

    TextHat th;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        th = TextHat.getInstance();

        final SharedPreferences sharedPreferences = getSharedPreferences("com.kmark.TextHat_preferences", MODE_PRIVATE);
        sharedPreferences.edit().putBoolean("enabled", sharedPreferences.getBoolean("enabled", false)).commit();
        sharedPreferences.edit().putBoolean("filter", sharedPreferences.getBoolean("filter", false)).commit();
        final Switch mainToggle = (Switch)findViewById(R.id.mainToggle);
        mainToggle.setChecked(sharedPreferences.getBoolean("enabled", false));
        mainToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                sharedPreferences.edit().putBoolean("enabled", isChecked).commit();
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

        if(!th.adapter.isEnabled()) {
            Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBluetooth, 0);
            return;
        }
        //onBluetoothEnabled();
    }

    void onBluetoothEnabled() {
        BluetoothDevice bd = null;
        Set<BluetoothDevice> pairedDevices = th.adapter.getBondedDevices();
        for(BluetoothDevice pd : pairedDevices) {
            if(pd.getName().equals("arduino")) {
                bd = pd;
            }
        }
        if(bd == null) {
            Log.wtf("MainActivity", "Arduino not found.");
            return;
        }
        UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
        try {
            BluetoothSocket socket = bd.createRfcommSocketToServiceRecord(uuid);
            socket.connect();
            OutputStream os = socket.getOutputStream();
            os.write("Kevin Mark|WASSUP?|".getBytes("US-ASCII"));
        } catch (IOException ex) {
            Log.wtf("MainActivity", ex);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
}
