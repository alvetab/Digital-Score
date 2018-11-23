package com.mirhoseini.itag;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.os.Message;
import android.util.Log;
import android.util.SparseArray;



import java.util.ArrayList;

/**
 * Implementación del servicio BLE,
 * con BLE Scan y funcionalidad de Data Receiver
 */
public class MultiBLEService implements BluetoothAdapter.LeScanCallback,
        IMultiBLEAccelDataReceiverDelegate {

    private static final String TAG = MultiBLEService.class.getSimpleName();

    private Context mContext;
    private Activity mActivity;
    private IMultiBLEAccelServiceDelegate mDelegate;

    // Componentes BLE
    private BluetoothAdapter mBluetoothAdapter;
    private MultiBLECallback mMultiBleCallback;
    private MultiBLEHandler mMultiBleHandler;

    private ArrayList<BluetoothGatt> mConnectedGatts;
    private ArrayList<BluetoothDevice> mSelectedDevices;
    private SparseArray<BluetoothDevice> mBluetoothDevices;


    // MultiBLEService constructor.
    public MultiBLEService(Context context) {
        this.mContext = context;
        this.mActivity = (Activity) context;
        this.mDelegate = (IMultiBLEAccelServiceDelegate) context;
        this.mConnectedGatts = new ArrayList<>();
    }

    @Override
    public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
        if (device != null) {
            if (device.getName() != null) {
                Log.e(TAG, mContext.getString(R.string.new_ble_device)
                        + device.getName() + " @ " + rssi);
                getBluetoothDevices().put(device.hashCode(), device);
            } else {
                Log.e(TAG, mContext.getString(R.string.error_not_valid_device)
                        + device.getName());
            }
        } else {
            Log.e(TAG, mContext.getString(R.string.error_null_device));
        }
    }

    // Método para conectar con una lista de dispositivos.
    public void connectToDevices(ArrayList<BluetoothDevice> devices) {
        mSelectedDevices = devices;
        for (BluetoothDevice device : devices) connectToDevice(device);
        mDelegate.updateConnectedDevices(mConnectedGatts);
    }

    // Método para conectar con un dispositivo en específico.
    public void connectToDevice(BluetoothDevice device) {
        if (null != device) {
            Log.e(TAG, String.format("Connecting to %s %s...",
                    device.getName(), device.getAddress()));

            mConnectedGatts.add(device.connectGatt(mContext,
                    false, mMultiBleCallback));
            mMultiBleHandler.sendMessage(Message.obtain(null,
                    IMultiBLEMessageType.PROGRESS,
                    String.format("Connecting to %s %s...",
                            device.getName(), device.getAddress())));
        }
    }

    // Desconectar de todos los dispositivos.
    public void disconnectFromDevices() {
        if (!mConnectedGatts.isEmpty()) {
            for (BluetoothGatt gatt : mConnectedGatts) gatt.disconnect();
            mConnectedGatts.clear();
        }
    }

    // Inicializar la conexión bluetooth.
    public void setupBluetoothConnection() {
        ProgressDialog messageNotifier = new ProgressDialog(mContext);
        mBluetoothDevices = new SparseArray<>();
        mMultiBleHandler = new MultiBLEHandler(messageNotifier, this);
        mMultiBleCallback = new MultiBLECallback(mMultiBleHandler);
        mBluetoothAdapter = ((BluetoothManager)
                mContext.getSystemService(Context.BLUETOOTH_SERVICE))
                .getAdapter();
    }

    // Comenzar el escaneo llamando a un callback al final.
    public void startScan(Runnable callback) {
        Log.e(TAG, mContext.getString(R.string.action_scanning_devices));
        getBluetoothDevices().clear();
        getBluetoothAdapter().startLeScan(this);
        mActivity.setProgressBarIndeterminateVisibility(true);
        mMultiBleHandler.postDelayed(callback, 3000L);
    }

    // Detener escaneo.
    public void stopScan() {
        getBluetoothAdapter().stopLeScan(this);
        mActivity.setProgressBarIndeterminateVisibility(false);
    }

    public BluetoothAdapter getBluetoothAdapter() {
        return mBluetoothAdapter;
    }

    public SparseArray<BluetoothDevice> getBluetoothDevices() {
        return mBluetoothDevices;
    }

    public ArrayList<BluetoothDevice> getSelectedDevices() {
        return mSelectedDevices;
    }

    // Actualizar los valores recibidos del acelerómetro.
    @Override
    public void updateAccelerometer(BluetoothGatt gatt,
                                    int accelX, int accelY, int accelZ,
                                    int gyroX, int gyroY, int gyroZ) {
        if (mDelegate != null) {
            mDelegate.updateAccelerometerValues(gatt,
                    accelX, accelY, accelZ,
                    gyroX, gyroY, gyroZ);
        }
    }
}