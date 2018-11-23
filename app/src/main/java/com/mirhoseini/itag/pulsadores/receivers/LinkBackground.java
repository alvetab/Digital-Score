package com.mirhoseini.itag.pulsadores.receivers;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.mirhoseini.itag.pulsadores.BluetoothLEService;

/**
 * Created by sylvek on 20/05/2015.
 */
public class LinkBackground extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent)
    {
        final int bluetoothState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);
        Log.d(BluetoothLEService.TAG, "bluetooth change state: " + bluetoothState);
        final Intent bleService = new Intent(context, BluetoothLEService.class);

        if (bluetoothState == BluetoothAdapter.STATE_ON) {
            context.startService(bleService);
        }

        if (bluetoothState == BluetoothAdapter.STATE_OFF) {
            context.stopService(bleService);
        }
    }
}
