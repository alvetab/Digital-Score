package com.mirhoseini.itag;

import android.bluetooth.BluetoothGatt;

import java.util.ArrayList;

/**
 * Delegate para el servicio de Acelerómetro/girocsopio.
 */
public interface IMultiBLEAccelServiceDelegate {
    /**
     * Método para actualizar la vista con los dispositivos conectados.
     *
     * @param gatts ArrayList que contiene a los dispositivos conectados.
     */
    void updateConnectedDevices(ArrayList<BluetoothGatt> gatts);

    /**
     * Método para procesar cada dato recibido de los sensores
     * acelerómetro/giroscopio.
     *
     * @param gatt   el dispositivo que envía el mensaje.
     * @param accelX valor recibido para el eje x del acelerómetro.
     * @param accelY valor recibido para el eje y del acelerómetro.
     * @param accelZ valor recibido para el eje z del acelerómetro.
     * @param gyroX  valor recibido para el eje x del giroscopio.
     * @param gyroY  valor recibido para el eje y del giroscopio.
     * @param gyroZ  valor recibido para el eje z del giroscopio.
     */
    void updateAccelerometerValues(BluetoothGatt gatt, int accelX, int accelY, int accelZ, int gyroX, int gyroY, int gyroZ); }