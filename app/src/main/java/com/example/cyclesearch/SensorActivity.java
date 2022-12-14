package com.example.cyclesearch;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import com.opencsv.CSVWriter;

import java.util.ArrayList;

public class SensorActivity implements SensorEventListener {

    public MySensor sensor;
    public MySensor Mysensor;

    private MapsActivity main;

    public SensorActivity(MySensor sensor, MapsActivity main) {
        this.sensor = sensor;
        Mysensor = sensor;
        this.main = main;
    }
    /**
     * Method that is invoked each time a new sensor measurement is taken
     * @param sensorEvent object that allows for determining what kind of sensor provided the daty
     * (Accelerometer or Gyrometer)
     */
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        Sensor sensor = sensorEvent.sensor;

        switch(sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                // Sensor stuff
                ArrayList<Float> acc = new ArrayList<>();
                for (int i = 0; i < 3; i++) {
                    acc.add(sensorEvent.values[i]);
                }

                this.sensor.setAcc(acc);
                this.sensor.setTimestamp(sensorEvent.timestamp);
                this.main.update();
                break;

            case Sensor.TYPE_GYROSCOPE:
                ArrayList<Float> gyro = new ArrayList<>();
                for (int i = 0; i < 3; i++) {
                    gyro.add(sensorEvent.values[i]);
                }

                this.sensor.setGyro(gyro);
                this.sensor.setTimestamp(sensorEvent.timestamp);
                break;
            default:
                System.out.println("Invalid sensor type in class SensorActivity, method onSensorChanged");
                break;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
