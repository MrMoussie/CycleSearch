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
    private boolean isFaceBackward = false;
    private boolean isChargerUp = false;

    public void setIsFaceBackward(boolean input){
        isFaceBackward = input;
    }

    public void setIsChargerUp(boolean input){
        isChargerUp = input;
    }

    public ArrayList<Float> getAcc() {
        ArrayList<Float> returnAcc = new ArrayList<>(Acc);
        if(isFaceBackward){
            returnAcc.set(0, returnAcc.get(0)*-1);
            returnAcc.set(2, returnAcc.get(2)*-1);
        }
        if(isChargerUp){
            returnAcc.set(0, returnAcc.get(0)*-1);
            returnAcc.set(1, returnAcc.get(1)*-1);
        }
        return returnAcc;
    }

    public void setAcc(ArrayList<Float> acc) {

        Acc = acc;
        accReady = true;
    }

    public ArrayList<Float> getGyro() {
        ArrayList<Float> returnGyro = new ArrayList<>(Gyro);
        if(isFaceBackward){
            returnGyro.set(0, returnGyro.get(0)*-1);
            returnGyro.set(2, returnGyro.get(2)*-1);
        }
        if(isChargerUp){
            returnGyro.set(0, returnGyro.get(0)*-1);
            returnGyro.set(1, returnGyro.get(1)*-1);
        }
        return returnGyro;
    }

    public void setGyro(ArrayList<Float> gyro) {
        Gyro = gyro;
        gyroReady = true;
    }

    public void setActivity(String activity) {
        this.activity = activity;
    }

    public String getActivity() {
        return this.activity;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public boolean getScreen() {
        return isFaceBackward;
    }

    public boolean getCharger() {
        return isChargerUp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Method that checks whether both Accelerometer and Gyrometer have acquired data to be sent to CSV file
     * @return boolean that determines whether both Gyro and Acc have data to send
     */
    public boolean checkReady(){
        if(accReady&&gyroReady){
            accReady = false;
            gyroReady = false;
            return true;
        }
        return false;
    }


    public MySensor() {}

}