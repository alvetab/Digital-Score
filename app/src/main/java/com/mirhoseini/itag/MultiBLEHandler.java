package com.mirhoseini.itag;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

/**
 * Implementación personalizada de Handler para procesar Mensajes y Runnables
 * de los BLECallbacks para múltiples dispositivos.
 */
public class MultiBLEHandler extends Handler {

    private static final String TAG = MultiBLEHandler.class.getSimpleName();

    private IMultiBLEAccelDataReceiverDelegate mDelegate;
    private ProgressDialog mProgressDialog;
    private boolean mStopCapture;

    // MultiBLEHandler constructor.
    public MultiBLEHandler(ProgressDialog dialog,
                           IMultiBLEAccelDataReceiverDelegate delegate) {
        /*
         * Delegate de la clase IMultiBLEAccelDataReceiver
         * para actualizar la vista.
         */
        this.mDelegate = delegate;

        this.mProgressDialog = dialog;
        this.mStopCapture = false;
    }

    @Override
    public void handleMessage(Message msg) {
        BluetoothGattDto bluetoothGattDto;
        BluetoothGatt gatt = null;
        BluetoothGattCharacteristic characteristic = null;
        int subject = msg.what;

        if (subject == IMultiBLEMessageType.STOP_CAPTURE && !mStopCapture) {
            mStopCapture = true;
        } else if (subject == IMultiBLEMessageType.RESUME_CAPTURE) {
            mStopCapture = false;
        }

        if (!mStopCapture) {
            if (msg.obj instanceof BluetoothGattDto) {
                /* DTO personalizado para guardar los datos del dispositivo
                 * y la característica leída.
                 */
                bluetoothGattDto = (BluetoothGattDto) msg.obj;
                gatt = bluetoothGattDto.getBluetoothGatt();
                characteristic = bluetoothGattDto.getBluetoothCharacteristic();
            }

            switch (msg.what) {
                case IMultiBLEMessageType.PROGRESS:
                    mProgressDialog.setMessage((String) msg.obj);
                    if (!mProgressDialog.isShowing()) {
                        mProgressDialog.show();
                    }
                    break;

                case IMultiBLEMessageType.ACCELEROMETER_MESSAGE:
                    if (characteristic == null
                            || characteristic.getValue() == null) {
                        Log.w(TAG, "Error obtaining accelerometer value");
                        return;
                    }
                    updateAccelerometerValue16(gatt, characteristic);
                    break;

                case IMultiBLEMessageType.DISMISS:
                    mProgressDialog.hide();
                    break;
            }
        }
    }

    /**
     * Actualizar el valor del acelerómetro en base 16, si está implementado.
     *
     * @param characteristic la característica leída.
     */
    private void updateAccelerometerValue16(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic
                                                    characteristic) {
        if (mDelegate != null) {
            int[] accelerometer = getAccelData16(characteristic);
            mDelegate.updateAccelerometer(gatt, accelerometer[0],
                    accelerometer[1], accelerometer[2], accelerometer[3],
                    accelerometer[4], accelerometer[5]);
        }
    }

    public int[] getAccelData16(BluetoothGattCharacteristic characteristic) {
        int[] result = new int[6];
        byte[] value = characteristic.getValue();

        // Tres primeros valores de los datos del acelerómetro.
        result[0] = getIntFromByteArray(value, 0);
        result[1] = getIntFromByteArray(value, 2);
        result[2] = getIntFromByteArray(value, 4);
        // Tres primeros valores de los datos del giroscopio.
        result[3] = getIntFromByteArray(value, 6);
        result[4] = getIntFromByteArray(value, 8);
        result[5] = getIntFromByteArray(value, 10);

        return result;
    }

    private int getIntFromByteArray(byte[] byteArray, int offset) {
        int result, high, low;

        high = byteArray[offset];
        low = byteArray[offset + 1];
        result = ((high & 0x000000FF) << 8) | (low & 0x000000FF);
        result = (result > 32767) ? result - 65536 : result;

        return result;
    }

}