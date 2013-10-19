package com.kmark.TextHat;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telephony.PhoneNumberUtils;
import android.telephony.SmsMessage;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.UUID;

public class SMSBroadcastReceiver extends BroadcastReceiver {

    private static final String SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";
    private static final String TAG = "SMSBroadcastReceiver";

    public void onReceive(Context context, Intent intent) {
        //Log.i(TAG, "Intent received: " + intent.getAction());

        if(!SMS_RECEIVED.equals(intent.getAction())) {
            return;
        }

        if(!context.getSharedPreferences("com.kmark.TextHat_preferences", Context.MODE_PRIVATE).getBoolean("enabled", false)) {
            return;
        }

        Bundle b = intent.getExtras();
        if(b == null) {
            return;
        }
        Object[] pdus = (Object[])b.get("pdus");
        final SmsMessage[] messages = new SmsMessage[pdus.length];
        for(int i = 0; i < pdus.length; i++) {
            messages[i] = SmsMessage.createFromPdu((byte[])pdus[i]);
        }
        Uri u = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_FILTER_URI, Uri.encode(messages[0].getDisplayOriginatingAddress().trim()));
        Cursor mapContact = context.getContentResolver().query(u, new String[] {ContactsContract.PhoneLookup.DISPLAY_NAME}, null, null, null);

        String fromWhere = messages[0].getOriginatingAddress();
        if(fromWhere != null) {
            fromWhere = PhoneNumberUtils.formatNumber(fromWhere);
        } else {
            fromWhere = messages[0].getDisplayOriginatingAddress();
        }

        if(mapContact.moveToNext()) {
            fromWhere = mapContact.getString(mapContact.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
        }
        if(messages.length > -1) {
            //Log.i(TAG, "Message from " + fromWhere + " received: " + messages[0].getDisplayMessageBody());
        }

        sendMessage(context, fromWhere, messages[0].getDisplayMessageBody());
    }

    private void sendMessage(Context ctx, String from, String message) {
        if(!message.startsWith("!")) {
            return;
        }
        message = message.substring(1).trim();

        // Allowed chars
        String stripRegex = "[^A-Za-z0-9 !?,@#&*\\-+=()_$'\":;/\\\\.%|<>{\\[}\\]^]";

        from = from.trim().replaceAll(stripRegex, "");

        message = message.replaceAll(stripRegex, "");

        if(message.length() > 58) {
            return;
        }

        if(ctx.getSharedPreferences("com.kmark.TextHat_preferences", Context.MODE_PRIVATE).getBoolean("filter", false)) {
            for(String word : TextHat.badWords) {
                if(message.toLowerCase().contains(word.toLowerCase())) {
                    return;
                }
            }
        }

        BluetoothDevice bd = null;
        Set<BluetoothDevice> pairedDevices = ((TextHat)ctx.getApplicationContext()).adapter.getBondedDevices();
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
            try {
                Method m = bd.getClass().getMethod("createRfcommSocket", new Class[] {int.class});
                socket = (BluetoothSocket)m.invoke(bd, 1);
            } catch (Exception ex) {
                Log.wtf(TAG, ex);
            }
            socket.connect();
            OutputStream os = socket.getOutputStream();
            InputStream is = socket.getInputStream();
            os.write((from + "\n" + message + "\n").getBytes("US-ASCII"));
            os.close();
            is.close();
            socket.close();

        } catch (IOException ex) {
            Log.wtf(TAG, ex);
        }
    }
}
