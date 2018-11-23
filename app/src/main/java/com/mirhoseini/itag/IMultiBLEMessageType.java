package com.mirhoseini.itag;

import java.util.UUID;

/**
 * Interface para Tipos de Mensaje.
 */
public interface IMultiBLEMessageType {
    // ID's del servicio acelerometro
    int ACCELEROMETER_SERVICE = 1;
    int ACCELEROMETER_MESSAGE = 10002;

    UUID ACCEL_SERVICE = UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb");
    UUID ACCEL_DATA_CHAR = UUID.fromString("f000aa11-0451-4000-b000-000000000000");
    UUID ACCEL_CONFIG_CHAR = UUID.fromString("f000aa12-0451-4000-b000-000000000000");

    UUID CONFIG_DESCRIPTOR = UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb");

    // Otros mensajes
    int PROGRESS = 201;
    int DISMISS = 202;
    int CLEAR = 301;
    int STOP_CAPTURE = 1;
    int RESUME_CAPTURE = 2;
}
