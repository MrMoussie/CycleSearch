package com.example.cyclesearch;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Object class for storing sensor values received from the sensors inside the mobile device
 */
public class MySensor {
    private ArrayList<Float> Acc = new ArrayList<>(Arrays.asList(0.0f,0.0f,0.0f));
    private ArrayList<Float> Gyro = new ArrayList<>(Arrays.asList(0.0f,0.0f,0.0f));
    private long timestamp;
    private boolean accReady = false;
    private boolean gyroReady = false;
    private String activity = "Init";

    public ArrayList<Float> getAcc() {
        return Acc;
    }

    public void setAcc(ArrayList<Float> acc) {

        Acc = acc;
        accReady = true;
    }

    public ArrayList<Float> getGyro() {
        return Gyro;
    }

    public void setGyro(ArrayList<Float> gyro) {
        Gyro = gyro;
        gyroReady = true;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Method that checks whether both Accelerometer and Gyrometer have acquired data to be sent to CSV file
     * @return boolean that determines whether both Gyro and Acc have data to send
     */
    public boolean isReady(){
        return this.Acc.size() == 3 && this.Gyro.size() == 3;
    }


    public MySensor() {}

}