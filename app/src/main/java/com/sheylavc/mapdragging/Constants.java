package com.sheylavc.mapdragging;

import com.google.android.gms.location.DetectedActivity;

/**
 * Created by Sheylavc on 1/03/2017.
 */

public class Constants {
    public static final String BROADCAST_ACTION = "broadcast-action";
    public static final String ACTIVITY_KEY = "activites-key";
    public static final long ACTIVITY_RECOGNITION_INTERVAL = 0;
    public static final long UPDATE_INTERVAL = 1000;
    public static final long UPDATE_FASTEST_INTERVAL = UPDATE_INTERVAL / 2;

    public static String getStringActivity(int type) {
        switch (type) {
            case DetectedActivity.IN_VEHICLE:
                return "Vehículo";
            case DetectedActivity.ON_BICYCLE:
                return "Bicicleta";
            case DetectedActivity.ON_FOOT:
                return "Caminando o corriendo";
            case DetectedActivity.RUNNING:
                return "Corriendo";
            case DetectedActivity.STILL:
                return "Sin movimiento";
            case DetectedActivity.TILTING:
                return "Inclinación brusca";
            case DetectedActivity.UNKNOWN:
                return "Desconocido";
            case DetectedActivity.WALKING:
                return "Caminando";
            default:
                return "Tipo no idenficado";
        }
    }
}
