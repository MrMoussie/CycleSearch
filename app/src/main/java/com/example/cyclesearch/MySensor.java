package com.example.cyclesearch;

import java.util.ArrayList;

/**
 * Object class for storing sensor values received from the sensors inside the mobile device
 */
public class MySensor {
    private ArrayList<Float> Acc = new ArrayList<>();
    private ArrayList<Float> Gyro = new ArrayList<>();
    private long timestamp;
    private long lastTimestamp;

    public ArrayList<Float> getAcc() {
        return Acc;
    }

    public void setAcc(ArrayList<Float> acc) {
        Acc = acc;
    }

    public ArrayList<Float> getGyro() {
        return Gyro;
    }

    public void setGyro(ArrayList<Float> gyro) {
        Gyro = gyro;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public MySensor(ArrayList<Float> A, ArrayList<Float> L, ArrayList<Float> G, long timestamp) {
        this.Acc = A;
        this.Gyro = G;
        this.timestamp = timestamp;
    }



    public MySensor() {}

}