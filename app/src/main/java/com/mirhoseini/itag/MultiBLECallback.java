package com.mirhoseini.itag;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothProfile;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.SparseArray;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Implementación personalizada de Bluetooth GATT Callbacks
 * para conexión con múltiples dispositivos.
 */
public class MultiBLECallback extends BluetoothGattCallback {

    private static final String TAG = MultiBLECallback.class.getSimpleName();

    // Seguimiento de la máquina de estados
    private Handler mHandler;
    private int mBleServiceId;
    private List<Integer> mBleSensors;
    private SparseArray<Integer> mCurrentSensors;


    // MultiBLECallback constructor.
    public MultiBLECallback(Handler handler) {
        mHandler = handler;
        mCurrentSensors = new SparseArray<>();

        /*
         * Arreglo que contiene los sensores a leer por dispositivo.
         * En nuestro caso, solo acelerómetro.
         */
        mBleSensors = new ArrayList<>();
        mBleSensors.add(IMultiBLEMessageType.ACCELEROMETER_SERVICE);
        // El ID del primer sensor en el arreglo.
        mBleServiceId = mBleSensors.get(0);
    }

    @Override
    public void onConnectionStateChange(BluetoothGatt gatt,
                                        int status,
                                        int newState) {
        Log.e(TAG, String.format("Connection State Change: %d -> %s",
                status, connectionState(newState)));
        if (status == BluetoothGatt.GATT_SUCCESS
                && newState == BluetoothProfile.STATE_CONNECTED) {
            /*
             * Una vez que se conectó exitosamente, debemos descubrir
             * todos los servicios en el dispositivo antes de poder leer
             * y escribir sus características.
             */
            gatt.discoverServices();
            mHandler.sendMessage(Message.obtain(null,
                    IMultiBLEMessageType.PROGRESS,
                    "Discovering Services..."));
        } else if (status == BluetoothGatt.GATT_SUCCESS
                && newState == BluetoothProfile.STATE_DISCONNECTED) {
            /*
             * Si en cualquier punto nos desconectamos, se envía un
             * mensaje para limpiar los valores de la interfaz de usuario.
             */
            mHandler.sendEmptyMessage(IMultiBLEMessageType.CLEAR);
        } else if (status != BluetoothGatt.GATT_SUCCESS) {
            /*
             * Si hay un fallo en cualquier etapa, simplemente desconectamos.
             */
            gatt.disconnect();
        }
    }

    @Override
    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
        Log.d(TAG, "Services Discovered: " + status);
        mHandler.sendMessage(Message.obtain(null,
                IMultiBLEMessageType.PROGRESS,
                "Enabling Sensors..."));
        /*
         * Una vez descubiertos los servicios, vamos a resetear
         * nuestra máquina de estados y comenzar trabajando en los
         * sensores que necesitamos habilitar.
         */
        bleServiceReset();
        enableNextSensor(gatt);
    }

    @Override
    public void onCharacteristicRead(BluetoothGatt gatt,
                                     BluetoothGattCharacteristic
                                             characteristic,
                                     int status) {
        // Nada aquí.
    }

    @Override
    public void onCharacteristicWrite(BluetoothGatt gatt,
                                      BluetoothGattCharacteristic
                                              characteristic,
                                      int status) {
        // Después de habilitar, leemos el valor inicial.
        setNotifyNextSensor(gatt);
    }

    @Override
    public void onCharacteristicChanged(BluetoothGatt gatt,
                                        BluetoothGattCharacteristic
                                                characteristic) {
        /*
         * Después de que las notificaciones están habilitadas, todas
         * las actualizaciones del dispositivo en cambios de características
         * serán publicadas aquí. Las enviamos al thread de la interfaz
         * de usuario para actualizarla.
         */
        BluetoothGattDto bluetoothGattDto = new BluetoothGattDto(gatt,
                characteristic);

        if (isAccelerometerChar(characteristic.getUuid())) {
            mHandler.sendMessage(Message.obtain(null,
                    IMultiBLEMessageType.ACCELEROMETER_MESSAGE,
                    bluetoothGattDto));
        }
    }

    @Override
    public void onDescriptorWrite(BluetoothGatt gatt,
                                  BluetoothGattDescriptor descriptor,
                                  int status) {
        /*
         * Una vez que las notificaciones están habilitadas, nos movemos
         * al siguiente sensor para habilitarlo.
         */
        bleNextService(gatt);
        enableNextSensor(gatt);
    }

    @Override
    public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
        Log.d(TAG, "Remote RSSI: " + rssi);
    }

    /*
     * Enviar un comando para habilitar cada sensor escribiendo una
     * característica de configuración. Esto es específico para el SensorTag,
     * para mantener la energía baja deshabilitando los sensores no usados.
     */
    private void enableNextSensor(BluetoothGatt gatt) {
        BluetoothGattCharacteristic characteristic;
        switch (mBleServiceId) {
            case IMultiBLEMessageType.ACCELEROMETER_SERVICE:
                Log.e(TAG, "Enabling accelerometer service...");
                characteristic = gatt.getService(
                        IMultiBLEMessageType.ACCEL_SERVICE)
                        .getCharacteristic(
                                IMultiBLEMessageType.ACCEL_CONFIG_CHAR);
                characteristic.setValue(new byte[]{0x01});
                break;

            default:
                mHandler.sendEmptyMessage(IMultiBLEMessageType.DISMISS);
                Log.e(TAG, String.format("All Sensors Enabled for %s!",
                        gatt.getDevice()));
                return;
        }
        gatt.writeCharacteristic(characteristic);
    }

    /*
     * Habilitar notificaciones de cambios en las características para cada
     * sensor escribiendo la bandera ENABLE_NOTIFICATION_VALUE al descriptor
     * de dicha característica.
     */
    private void setNotifyNextSensor(BluetoothGatt gatt) {
        BluetoothGattCharacteristic characteristic;
        switch (mBleServiceId) {
            case IMultiBLEMessageType.ACCELEROMETER_SERVICE:
                Log.e(TAG, "Set notify accelerometer sensor.");
                characteristic = gatt.getService(
                        IMultiBLEMessageType.ACCEL_SERVICE)
                        .getCharacteristic(
                                IMultiBLEMessageType.ACCEL_DATA_CHAR);
                break;

            default:
                mHandler.sendEmptyMessage(IMultiBLEMessageType.DISMISS);
                Log.e(TAG, String.format("All Sensors Notified for %s!",
                        gatt.getDevice()));
                return;
        }

        // Habilitar notificaciones locales
        gatt.setCharacteristicNotification(characteristic, true);

        // Habilitar notificaciones remotas
        BluetoothGattDescriptor descriptor =
                characteristic.getDescriptor(
                        IMultiBLEMessageType.CONFIG_DESCRIPTOR);
        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        gatt.writeDescriptor(descriptor);
    }

    private String connectionState(int status) {
        switch (status) {
            case BluetoothProfile.STATE_CONNECTED:
                return "Connected";
            case BluetoothProfile.STATE_DISCONNECTED:
                return "Disconnected";
            case BluetoothProfile.STATE_CONNECTING:
                return "Connecting...";
            case BluetoothProfile.STATE_DISCONNECTING:
                return "Disconnecting...";
            default:
                return String.valueOf(status);
        }
    }

    private void bleServiceReset() {
        // Limpiar la lista de sensores leídos y comenzar con el primero
        mCurrentSensors.clear();
        mBleServiceId = mBleSensors.get(0);
    }

    /*
     * Para cada dispositivo conectado, guardar la lista de sus sensores
     */
    private void bleNextService(BluetoothGatt gatt) {
        /*
         * Contador que indica la posición del sensor en nuestra lista
         * de sensores por leer.
         */
        Integer currentSensor;
        // El hash code del dispositivo actual.
        int gattCode = gatt.hashCode();

        if ((currentSensor = mCurrentSensors.get(gattCode)) != null) {
            /*
             * Si el dispositivo está ya en la lista, incrementa el contador
             * de sus sensores leídos.
             */
            mCurrentSensors.put(gattCode, ++currentSensor);
        } else {
            // Si el dispositivo no está en la lista, inicializa sus valores.
            currentSensor = 0;
            mCurrentSensors.put(gattCode, currentSensor);
        }

        /*
         * Cuando todos los sensores de un dispositivo fueron leídos,
         * asigna un valor inalcanzable a la variable mBleServiceId.
         */
        if (currentSensor < mBleSensors.size()) {
            mBleServiceId = mBleSensors.get(mCurrentSensors.get(gattCode));
        } else {
            mBleServiceId = 100;
        }
    }

    private boolean isAccelerometerChar(UUID UuidChar) {
        return UuidChar.toString().equals(
                IMultiBLEMessageType.ACCEL_DATA_CHAR.toString());
    }
}
