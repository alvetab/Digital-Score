package com.mirhoseini.itag;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import android.bluetooth.BluetoothAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.mirhoseini.itag.miband2.BLEMiBand2Helper;
import com.mirhoseini.itag.miband2.Consts;
import com.mirhoseini.itag.miband2.HearBeatVoice;
import com.mirhoseini.itag.miband2.SoundHelper;
import com.mirhoseini.itag.pulsadores.database.Devices;
import com.mirhoseini.itag.pulsadores.database.Events;

import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

public class Mainmiband2Activity extends AppCompatActivity implements BLEMiBand2Helper.BLEAction {
    public static final String LOG_TAG = "Yoni";
    private static final String TAG ="miband";

    Handler handler = new Handler(Looper.getMainLooper());
    BLEMiBand2Helper helper = null;

    TextView txtPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_miband2);

        helper = new BLEMiBand2Helper(Mainmiband2Activity.this, handler);
        helper.addListener(this);

       // initSoundHelper();

        // Get save path:
        txtPath = (TextView) findViewById(R.id.txtPath);
        txtPath.setText( getApplicationContext().getExternalFilesDir(null).getAbsolutePath());

        // Setup Bluetooth:
        helper.findBluetoothDevice(myBluetoothAdapter, "MI");
        helper.ConnectToGatt();

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        getTouchNotifications();
        //setupHeartBeat();

    }


    @Override
    protected void onDestroy() {
        if (helper != null)
        super.onDestroy();
    }

    // Like network card, connect to all devices in Bluetooth (like PC in Netowrk)
    final BluetoothAdapter myBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    public void btnRun(View view) {

        helper.findBluetoothDevice(myBluetoothAdapter, "MI");
        helper.ConnectToGatt();
    }


    public void setupHeartBeat() {
        /*
        Steps to read heartbeat:
            - Register Notification (like in touch press)
                - Extra step with description
            - Write predefined bytes to control_point to trigger measurement
            - Listener will get result
        */

        if (helper != null)
            helper.getNotificationsWithDescriptor(
                    Consts.UUID_SERVICE_HEARTBEAT,
                    Consts.UUID_NOTIFICATION_HEARTRATE,
                    Consts.UUID_DESCRIPTOR_UPDATE_NOTIFICATION
            );

        // Need to wait before first trigger, maybe something about the descriptor....
        /*
        Toast.makeText(MainActivity.this, "Wait for heartbeat setup...", Toast.LENGTH_LONG).show();
        try {
            Thread.sleep(5000,0);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        */
    }

    public void getNewHeartBeat()  {
        if (helper == null || !helper.isConnected()) {
            Toast.makeText(Mainmiband2Activity.this, "Please setup first!", Toast.LENGTH_SHORT).show();
            return;
        }

        helper.writeData(
                Consts.UUID_SERVICE_HEARTBEAT,
                Consts.UUID_START_HEARTRATE_CONTROL_POINT,
                Consts.BYTE_NEW_HEART_RATE_SCAN
        );
    }

    public void getTouchNotifications() {
        helper.getNotifications(
                Consts.UUID_SERVICE_MIBAND_SERVICE,
                Consts.UUID_BUTTON_TOUCH);
    }

    public void btnTest(View view) throws InterruptedException {
        getTouchNotifications();
    }

    public void btnSetuphearRate(View view) throws InterruptedException {
        setupHeartBeat();
    }

    public void btnTestHeartRate(View view) throws InterruptedException {
        getNewHeartBeat();
    }

    /* ===========  EVENTS (background thread) =============== */

    @Override
    public void onDisconnect() {

    }

    @Override
    public void onConnect() {

    }

    @Override
    public void onRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {

    }

    @Override
    public void onWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {

    }

    @Override
    public void onNotification(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        UUID alertUUID = characteristic.getUuid();
        if (alertUUID.equals(Consts.UUID_NOTIFICATION_HEARTRATE)) {
            final byte hearbeat =
                    characteristic.getValue()[1];

            handler.post(new Runnable() {

                @Override
                public void run() {
                    Toast.makeText(Mainmiband2Activity.this,
                            "Heartbeat: " + Byte.toString(hearbeat)
                            , Toast.LENGTH_SHORT).show();

                    // Set max volume and read heart beat.
                    setMaxVolume();
                    HearBeatVoice.readHeartbeat(mySounds, hearbeat);
                    mySounds.playAllAsync();
                }
            });
        }
        else if (alertUUID.equals(Consts.UUID_BUTTON_TOUCH)) {

            final Intent intent = new Intent("com.mirhoseini.itag.button_pressed");
            String address ="24";
            intent.putExtra(Devices.ADDRESS, address);
            sendBroadcast(intent);
            //Events.insert(getApplicationContext(), source.name(), address, action);
            //Log.d(TAG, "onCharacteristicChanged() address: " + address + " - sendBroadcast action: " + intent.getAction());


           /* handler.post(new Runnable() {

                @Override
                public void run() {
                   /getNewHeartBeat();
                    Toast.makeText(Mainmiband2Activity.this,
                            "Button Press! "
                            , Toast.LENGTH_SHORT).show();
                }
            });*/
        }
    }



    /* ===========  Sounds =============== */

    int currentVolume = 0;
    AudioManager audiManager;

    public void setMaxVolume() {
        currentVolume = audiManager.getStreamVolume(audiManager.STREAM_MUSIC);
        int amStreamMusicMaxVol =
                (int) (audiManager.getStreamMaxVolume(audiManager.STREAM_MUSIC) * 0.60f);

        audiManager.setStreamVolume(audiManager.STREAM_MUSIC, amStreamMusicMaxVol, 0);
    }

    public void resumeVolume() {
        audiManager.setStreamVolume(audiManager.STREAM_MUSIC, currentVolume, 0);
    }

    SoundHelper mySounds;
    private void initSoundHelper() {
        audiManager = (AudioManager)getSystemService(getApplicationContext().AUDIO_SERVICE);
        mySounds = new SoundHelper(getApplicationContext());

        mySounds.setPlaybackFinishListener(new SoundHelper.PlaybackFinishListener() {
            @Override
            public void onPlaybackFinish() {
                Log.d(LOG_TAG,"Got SoundHelper finish event!");
                mySounds.releaseAllSounds();
                resumeVolume();
            }
        });
    }
}
