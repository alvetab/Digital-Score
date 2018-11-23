package com.mirhoseini.itag.pulsadores;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.mirhoseini.itag.R;
import com.mirhoseini.itag.pulsadores.database.Devices;
import com.mirhoseini.itag.pulsadores.database.Events;
import com.mirhoseini.itag.pulsadores.devices.DevicesActivity;

import java.util.HashMap;
import java.util.UUID;

/**
 * Created by sylvek on 18/05/2015.
 */
public class BluetoothLEService extends Service {

    public int pulsacion=0;
    public static final int NO_ALERT = 0x00;
    public static final int MEDIUM_ALERT = 0x01;
    public static final int HIGH_ALERT = 0x02;
    public static String DATA = "00-00 0 0";

    public static final String IMMEDIATE_ALERT_AVAILABLE = "IMMEDIATE_ALERT_AVAILABLE";
    public static final String BATTERY_LEVEL = "BATTERY_LEVEL";
    public static final String GATT_CONNECTED = "GATT_CONNECTED";
    public static final String SERVICES_DISCOVERED = "SERVICES_DISCOVERED";
    public static final String RSSI_RECEIVED = "RSSI_RECEIVED";
    public static final String SEND_DATA_AVAILABLE = "SEND_DATA_AVAILABLE";

    public static final UUID IMMEDIATE_ALERT_SERVICE = UUID.fromString("00001802-0000-1000-8000-00805f9b34fb");
    public static final UUID FIND_ME_SERVICE = UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb");
    public static final UUID LINK_LOSS_SERVICE = UUID.fromString("00001803-0000-1000-8000-00805f9b34fb");
    public static final UUID BATTERY_SERVICE = UUID.fromString("0000180f-0000-1000-8000-00805f9b34fb");

    public static final UUID CLIENT_CHARACTERISTIC_CONFIG = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    public static final UUID ALERT_LEVEL_CHARACTERISTIC = UUID.fromString("00002a06-0000-1000-8000-00805f9b34fb");
    public static final UUID FIND_ME_CHARACTERISTIC = UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb");

    public static final String TAG = BluetoothLEService.class.toString();
    public static final String ACTION_PREFIX = "com.mirhoseini.itag.pulsadores.action.";
    public static final long TRACK_REMOTE_RSSI_DELAY_MILLIS = 5000L;
    public static final int FOREGROUND_ID = 1664;
    public static final String BROADCAST_INTENT_ACTION = "BROADCAST_INTENT";



    public static final UUID SEND_DATA_SERVICE = UUID.fromString("4fafc201-1fb5-459e-8fcc-c5c9c331914b");
    public static final UUID SEND_DATA_CHARACTERISTIC = UUID.fromString("beb5483e-36e1-4688-b7f5-ea07361b26a8");

    //MIBAND2
    public static final String BASE_UUID = "0000%s-0000-1000-8000-00805f9b34fb";
    public static final UUID UUID_SERVICE_MIBAND_SERVICE = UUID.fromString(String.format(BASE_UUID, "FEE0"));
    // Miband service 1
    public static final UUID UUID_BUTTON_TOUCH = UUID.fromString("00000010-0000-3512-2118-0009af100700");


    private BluetoothDevice mDevice;

    private HashMap<String, BluetoothGatt> bluetoothGatt = new HashMap<>();

    private BluetoothGattService immediateAlertService;

    private BluetoothGattService sendData;

    private BluetoothGattService linkLossService;

    private BluetoothGattCharacteristic batteryCharacteristic;

    private BluetoothGattCharacteristic buttonCharacteristic;
    private BluetoothGattCharacteristic buttonmiband;

    private BluetoothGattCharacteristic dataCharacteristic;

    private long lastChange;

    private UUID lastUuid;

    private String lastAddress;

    private String equipo;

    private Runnable r;

    private Handler handler = new Handler();

    private Runnable trackRemoteRssi = null;
    public BroadcastReceiver br = new BluetoothLEService.MyBroadcastReceiver();

    private class CustomBluetoothGattCallback extends BluetoothGattCallback {

        private final String address;

        CustomBluetoothGattCallback(final String address) {
            this.address = address;
        }

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            Log.d(TAG, "onConnectionStateChange() address: " + address + " status => " + status);
            if (BluetoothGatt.GATT_SUCCESS == status) {
                Log.d(TAG, "onConnectionStateChange() address: " + address + " newState => " + newState);
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    broadcaster.sendBroadcast(new Intent(GATT_CONNECTED));
                    gatt.discoverServices();
                }

                if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                   gatt.close();
                }
            }

            final boolean actionOnPowerOff = Preferences.isActionOnPowerOff(BluetoothLEService.this, this.address);
            if (actionOnPowerOff || status == 8) {
                Log.d(TAG, "onConnectionStateChange() address: " + address + " newState => " + newState);
                if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    for (String action : Preferences.getActionOutOfBand(getApplicationContext(), this.address)) {
                        sendAction(Preferences.Source.out_of_range, action);
                    }
                    enablePeerDeviceNotifyMe(gatt, false);
                }
            }
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
           final Intent rssiIntent = new Intent(RSSI_RECEIVED);
            rssiIntent.putExtra(RSSI_RECEIVED, rssi);
            broadcaster.sendBroadcast(rssiIntent);
        }

        @Override
        public void onServicesDiscovered(final BluetoothGatt gatt, int status) {
            Log.d(TAG, "onServicesDiscovered()");

            //launchTrackingRemoteRssi(gatt);

            broadcaster.sendBroadcast(new Intent(SERVICES_DISCOVERED));
            if (BluetoothGatt.GATT_SUCCESS == status) {

                for (String action : Preferences.getActionConnected(getApplicationContext(), this.address)) {
                    sendAction(Preferences.Source.connected, action);
                }


                for (BluetoothGattService service : gatt.getServices()) {


                    Log.d(TAG, "service discovered: " + service.getUuid());

                    if (IMMEDIATE_ALERT_SERVICE.equals(service.getUuid())) {
                        immediateAlertService = service;
                        broadcaster.sendBroadcast(new Intent(IMMEDIATE_ALERT_AVAILABLE));
                        gatt.readCharacteristic(getCharacteristic(gatt, IMMEDIATE_ALERT_SERVICE, ALERT_LEVEL_CHARACTERISTIC));
                        setCharacteristicNotification(gatt, immediateAlertService.getCharacteristics().get(0), true);
                    }

                    if (SEND_DATA_SERVICE.equals(service.getUuid())) {
                        sendData = service;
                        broadcaster.sendBroadcast(new Intent(SEND_DATA_AVAILABLE));
                        gatt.writeCharacteristic(getCharacteristic(gatt, SEND_DATA_SERVICE,SEND_DATA_CHARACTERISTIC));
                        setCharacteristicWrite(gatt, sendData.getCharacteristics().get(0));
                    }

                    if (BATTERY_SERVICE.equals(service.getUuid())) {
                        batteryCharacteristic = service.getCharacteristics().get(0);
                        gatt.readCharacteristic(batteryCharacteristic);
                    }

                    if (FIND_ME_SERVICE.equals(service.getUuid())) {
                        if (!service.getCharacteristics().isEmpty()) {
                            buttonCharacteristic = service.getCharacteristics().get(0);
                            setCharacteristicNotification(gatt, buttonCharacteristic, true);
                        }
                    }

                    if (LINK_LOSS_SERVICE.equals(service.getUuid())) {
                        linkLossService = service;
                    }
                }
                enablePeerDeviceNotifyMe(gatt, true);
            }
        }

        private void launchTrackingRemoteRssi(final BluetoothGatt gatt) {
            if (trackRemoteRssi != null) {
                handler.removeCallbacks(trackRemoteRssi);
            }

            trackRemoteRssi = new Runnable() {
                @Override
                public void run() {
                    gatt.readRemoteRssi();
                    handler.postDelayed(this, TRACK_REMOTE_RSSI_DELAY_MILLIS);
                }
            };
            handler.post(trackRemoteRssi);
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            Log.d(TAG, "onDescriptorWrite()");
            gatt.readCharacteristic(batteryCharacteristic);
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            Log.d(TAG, "onCharacteristicChanged()");
            final long delayDoubleClick = Preferences.getDoubleButtonDelay(getApplicationContext());

            final long now = SystemClock.elapsedRealtime();
            equipo=Preferences.getCustomAction (getApplicationContext(), CustomBluetoothGattCallback.this.address,"single_click");
            if (lastChange + delayDoubleClick > now && characteristic.getUuid().equals(lastUuid) && gatt.getDevice().getAddress().equals(lastAddress)) {
                pulsacion++;
                lastChange = now + pulsacion*2*delayDoubleClick;
                handler.removeCallbacks(r);
                r = new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "onCharacteristicChanged() -  click " + pulsacion);

                        for (String action : Preferences.getActionSimpleButton(getApplicationContext(), BluetoothLEService.CustomBluetoothGattCallback.this.address)) {
                            sendclicks(Preferences.Source.single_click, action, pulsacion , equipo);
                           }
                        lastChange = 0;
                        lastUuid = null;
                        lastAddress = "";
                    }
                };
                handler.postDelayed(r, delayDoubleClick);


            } else {
                lastChange = now;
                lastUuid = characteristic.getUuid();
                lastAddress = gatt.getDevice().getAddress();

                r = new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "onCharacteristicChanged() - simple click " + pulsacion);
                        for (String action : Preferences.getActionSimpleButton(getApplicationContext(), BluetoothLEService.CustomBluetoothGattCallback.this.address)) {
                            sendclicks(Preferences.Source.single_click, action,pulsacion , equipo);
                        }
                    }
                };
                handler.postDelayed(r, delayDoubleClick);
                pulsacion=0;
            }
        }

        private void sendAction(Preferences.Source source, String action) {
            /*final Intent intent = new Intent(BROADCAST_INTENT_ACTION.equals(action) ? ACTION_PREFIX + action + "." + source : ACTION_PREFIX + action);
            intent.putExtra(Devices.ADDRESS, address);
            intent.putExtra(Devices.SOURCE, source.name());
*/


            final Intent intent = new Intent("com.mirhoseini.itag.button_pressed");
            intent.putExtra(Devices.ADDRESS, address);
            sendBroadcast(intent);
            Events.insert(getApplicationContext(), source.name(), address, action);
            Log.d(TAG, "onCharacteristicChanged() address: " + address + " - sendBroadcast action: " + intent.getAction());
        }



        private void sendclicks(Preferences.Source source, String action , int pulsaciones, String equipo) {
            /*final Intent intent = new Intent(BROADCAST_INTENT_ACTION.equals(action) ? ACTION_PREFIX + action + "." + source : ACTION_PREFIX + action);
            intent.putExtra(Devices.ADDRESS, address);
            intent.putExtra(Devices.SOURCE, source.name());
*/


            final Intent intent = new Intent("com.mirhoseini.itag.button_pressed");
            intent.putExtra(Devices.ADDRESS, address);
            intent.putExtra("CLICKS",pulsaciones);
            intent.putExtra("EQUIPO", equipo);
            sendBroadcast(intent);
            Events.insert(getApplicationContext(), source.name(), address, action);
            Log.d(TAG, "onCharacteristicChanged() address: " + address + " - sendBroadcast action: " + intent.getAction());
        }


        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            Log.d(TAG, "onCharacteristicRead()");
            if (characteristic.getValue() != null && characteristic.getValue().length > 0) {
                final Intent batteryLevel = new Intent(BATTERY_LEVEL);
                final byte level = characteristic.getValue()[0];
                batteryLevel.putExtra(BATTERY_LEVEL, Integer.valueOf(level));
                broadcaster.sendBroadcast(batteryLevel);
            }
        }
    }
    private void setCharacteristicWrite(BluetoothGatt bluetoothgatt, BluetoothGattCharacteristic bluetoothgattcharacteristic) {
        bluetoothgatt.writeCharacteristic(bluetoothgattcharacteristic);
        BluetoothGattDescriptor descriptor = bluetoothgattcharacteristic.getDescriptor(CLIENT_CHARACTERISTIC_CONFIG);
        if (descriptor != null) {
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            bluetoothgatt.writeDescriptor(descriptor);
        }
    }
    private void setCharacteristicNotification(BluetoothGatt bluetoothgatt, BluetoothGattCharacteristic bluetoothgattcharacteristic, boolean flag) {
        bluetoothgatt.setCharacteristicNotification(bluetoothgattcharacteristic, flag);
        BluetoothGattDescriptor descriptor = bluetoothgattcharacteristic.getDescriptor(CLIENT_CHARACTERISTIC_CONFIG);
        if (descriptor != null) {
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            bluetoothgatt.writeDescriptor(descriptor);
        }
    }
    private void setCharacteristicmiband2Notification(BluetoothGatt bluetoothgatt, BluetoothGattCharacteristic bluetoothgattcharacteristic, boolean flag) {
        bluetoothgatt.setCharacteristicNotification(bluetoothgattcharacteristic, flag);
        BluetoothGattDescriptor descriptor = bluetoothgattcharacteristic.getDescriptor(UUID_BUTTON_TOUCH);
        if (descriptor != null) {
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            bluetoothgatt.writeDescriptor(descriptor);
        }
    }
    public void enablePeerDeviceNotifyMe(BluetoothGatt bluetoothgatt, boolean flag) {
        BluetoothGattCharacteristic bluetoothgattcharacteristic = getCharacteristic(bluetoothgatt, FIND_ME_SERVICE, FIND_ME_CHARACTERISTIC);
        if (bluetoothgattcharacteristic != null && (bluetoothgattcharacteristic.getProperties() | 0x10) > 0) {
            setCharacteristicNotification(bluetoothgatt, bluetoothgattcharacteristic, flag);
        }
    }

    private BluetoothGattCharacteristic getCharacteristic(BluetoothGatt bluetoothgatt, UUID serviceUuid, UUID characteristicUuid) {
        if (bluetoothgatt != null) {
            BluetoothGattService service = bluetoothgatt.getService(serviceUuid);
            if (service != null) {
                return service.getCharacteristic(characteristicUuid);
            }
        }

        return null;
    }

    public class BackgroundBluetoothLEBinder extends Binder {
        public BluetoothLEService service() {
            return BluetoothLEService.this;
        }
    }

    private BackgroundBluetoothLEBinder myBinder = new BackgroundBluetoothLEBinder();

    private LocalBroadcastManager broadcaster;

    @Override
    public IBinder onBind(Intent intent) {
        return myBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        setForegroundEnabled(Preferences.isForegroundEnabled(this));
        connect();
        return START_STICKY;
    }

    public void setForegroundEnabled(boolean enabled) {
        if (enabled) {
            final Notification notification = new Notification.Builder(this)
                    .setSmallIcon(R.drawable.ic_launcher)
                    .setContentTitle(getText(R.string.app_name))
                    .setTicker(getText(R.string.foreground_started))
                    .setContentText(getText(R.string.foreground_started))
                    .setContentIntent(PendingIntent.getActivity(this, 0, new Intent(this, DevicesActivity.class), 0))
                    .setShowWhen(false).build();
            startForeground(FOREGROUND_ID, notification);
        } else {
            stopForeground(true);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate()");
        broadcaster = LocalBroadcastManager.getInstance(this);
        IntentFilter miband2filtere1 = new IntentFilter("pulsera1");
        IntentFilter miband2filtere2 = new IntentFilter("pulsera2");
        this.registerReceiver(br, miband2filtere1);
        this.registerReceiver(br, miband2filtere2);
    }

    @Override
    public void onDestroy() {
        if (trackRemoteRssi != null) {
            handler.removeCallbacks(trackRemoteRssi);
        }

        disconnect();

        super.onDestroy();
        Log.d(TAG, "onDestroy()");
    }

    public synchronized void disconnect() {
        final Cursor cursor = Devices.findDevices(this);
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            do {
                final String address = cursor.getString(0);
                if (Devices.isEnabled(this, address)) {
                    Log.d(TAG, "disconnect() - to device " + address);
                    if (bluetoothGatt.get(address) != null) {
                        bluetoothGatt.get(address).disconnect();
                    }
                    bluetoothGatt.remove(address);
                }
            } while (cursor.moveToNext());
        }
    }

    public void setLinkLossNotificationLevel(String address, int alertType) {
        Log.d(TAG, "setLinkLossNotificationLevel() - the device " + address);
        if (bluetoothGatt.get(address) == null || linkLossService == null || linkLossService.getCharacteristics() == null || linkLossService.getCharacteristics().size() == 0) {
            somethingGoesWrong();
            return;
        }
        final BluetoothGattCharacteristic characteristic = linkLossService.getCharacteristics().get(0);
        characteristic.setValue(alertType, BluetoothGattCharacteristic.FORMAT_UINT8, 0);
        bluetoothGatt.get(address).writeCharacteristic(characteristic);
    }

    public void immediateAlert(String address, int alertType) {
        Log.d(TAG, "immediateAlert() - the device " + address);
        if (bluetoothGatt.get(address) == null || immediateAlertService == null || immediateAlertService.getCharacteristics() == null || immediateAlertService.getCharacteristics().size() == 0) {
            somethingGoesWrong();
            return;
        }
        final BluetoothGattCharacteristic characteristic = immediateAlertService.getCharacteristics().get(0);
        characteristic.setValue(alertType, BluetoothGattCharacteristic.FORMAT_UINT8, 0);
        bluetoothGatt.get(address).writeCharacteristic(characteristic);
        Events.insert(getApplicationContext(), "immediate_alert", address, "" + alertType);
    }



    public void sendData(String address, String data) {
        Log.d(TAG, "send data() - the device " + address);
        if (bluetoothGatt.get(address) == null || sendData == null || sendData.getCharacteristics() == null || sendData.getCharacteristics().size() == 0) {
            somethingGoesWrong();
            return;
        }
        final BluetoothGattCharacteristic characteristic = sendData.getCharacteristics().get(0);
        //String msg = "Hello Grove BLE";
        byte b = 0x00;
        byte[] temp = data.getBytes();
        byte[] tx = new byte[temp.length + 1];
        tx[0] = b;

        for (int i = 0; i < temp.length; i++)
            tx[i + 1] = temp[i];

        characteristic.setValue(tx);
        //mBluetoothGatt.writeCharacteristic(gattCharacteristic);
        //characteristic.setValue(data, BluetoothGattCharacteristic.FORMAT_UINT8, 0);
        bluetoothGatt.get(address).writeCharacteristic(characteristic);
        Events.insert(getApplicationContext(), "sent data", address, "" + data);
    }



    private synchronized void somethingGoesWrong() {
        Toast.makeText(this, R.string.something_goes_wrong, Toast.LENGTH_LONG).show();
    }

    public synchronized void connect() {
        final Cursor cursor = Devices.findDevices(this);
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            do {
                final String address = cursor.getString(0);
                if (Devices.isEnabled(this, address)) {
                    this.connect(address);

                }
            } while (cursor.moveToNext());
        }
    }

    public synchronized void connect(final String address) {
        if (!bluetoothGatt.containsKey(address) || bluetoothGatt.get(address) == null) {
            Log.d(TAG, "connect() - (new link) to device " + address);
            mDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(address);
            bluetoothGatt.put(address, mDevice.connectGatt(this, true, new CustomBluetoothGattCallback(address)));
        } else {
            Log.d(TAG, "connect() - discovering services for " + address);
            bluetoothGatt.get(address).discoverServices();
        }
    }

    public synchronized void disconnect(final String address) {
        if (bluetoothGatt.containsKey(address)) {
            Log.d(TAG, "disconnect() - to device " + address);
            if (!Devices.isEnabled(this, address)) {
                Log.d(TAG, "disconnect() - no background linked for " + address);
                if (bluetoothGatt.get(address) != null) {
                    bluetoothGatt.get(address).disconnect();
                }
                bluetoothGatt.remove(address);
            }
        }
    }

    public synchronized void remove(final String address) {
        if (bluetoothGatt.containsKey(address)) {
            Log.d(TAG, "remove() - to device " + address);
            if (bluetoothGatt.get(address) != null) {
                bluetoothGatt.get(address).disconnect();
            }
            bluetoothGatt.remove(address);
        }
    }
    public class MyBroadcastReceiver extends BroadcastReceiver {
        int pulsaciones=1;
        private static final String TAG = "MyBroadcastReceiver";
        @Override
        public void onReceive(Context context, Intent intent) {
            int player =0;
            //String cadena=intent.getAction();
            StringBuilder sb = new StringBuilder();
            sb.append("Action: " + intent.getAction() + "\n");
            sb.append("URI: " + intent.toUri(Intent.URI_INTENT_SCHEME).toString() + "\n");

            Bundle extras =intent.getExtras();
            if (extras !=null) {
                pulsaciones = extras.getInt("CLICKS");
                pulsaciones++;
            }
            String log = sb.toString();

            if (log.contains("pulsera1")){

                player=1;
            }
            else if (log.contains("pulsera2")){
                player=2;
            }
            else {player=0;}

           /* Log.d(TAG, log);
            Toast.makeText(context, log, Toast.LENGTH_LONG).show();*/
            onpulseraPressed(context,intent,player);
        }
    }
    public void onpulseraPressed(Context context,Intent intent,int player) {
        Log.d(TAG, "onpulseraPressed()");
        final long delayDoubleClick = Preferences.getDoubleButtonDelay(getApplicationContext());

        final long now = SystemClock.elapsedRealtime();
        final int equipo=player;
        if (lastChange + delayDoubleClick > now ) {
            pulsacion++;
            lastChange = now + pulsacion*4*delayDoubleClick;
            handler.removeCallbacks(r);
            r = new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "onpulseraPressed() -  click " + pulsacion);

                    sendpulsaciones(pulsacion , equipo);

                    lastChange = 0;
                    lastUuid = null;
                    lastAddress = "";
                }
            };
            handler.postDelayed(r, delayDoubleClick);


        } else {
            lastChange = now;



            r = new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "onpulseraPressed() - simple click " + pulsacion);
                    sendpulsaciones(pulsacion , equipo);

                }
            };
            handler.postDelayed(r, delayDoubleClick);
            pulsacion=0;
        }

    }
    private void sendpulsaciones(int pulsaciones, int equipo) {
            /*final Intent intent = new Intent(BROADCAST_INTENT_ACTION.equals(action) ? ACTION_PREFIX + action + "." + source : ACTION_PREFIX + action);
            intent.putExtra(Devices.ADDRESS, address);
            intent.putExtra(Devices.SOURCE, source.name());
*/


        final Intent intent = new Intent("com.mirhoseini.itag.button_pressed");
        String team="equipo"+equipo;
        intent.putExtra("CLICKS",pulsaciones);
        intent.putExtra("EQUIPO", team);
        sendBroadcast(intent);
        //Events.insert(equipogetApplicationContext(), source.name(), address, action);
        Log.d(TAG, "onCharacteristicChanged() address: " + equipo+ " - sendBroadcast action: " + intent.getAction());
    }
}
