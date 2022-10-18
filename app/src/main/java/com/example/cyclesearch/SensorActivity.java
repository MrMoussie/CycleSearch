package com.example.cyclesearch;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.os.Environment;


import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.util.ArrayList;

public class SensorActivity implements SensorEventListener {

    // Sensor stuff
    private ArrayList<Float> Acc;
    private ArrayList<Float> Gyro;
    public MySensor sensor;
    public MySensor Mysensor;

    // Excel stuff
    private MapsActivity main;
    private File file;
    private String activity = "if you see this, the activity is not set";
    private Excel excel;

    public SensorActivity(MySensor sensor, MapsActivity map, File file, FileWriter outputfile, CSVWriter writer) {
        this.sensor = sensor;
        Mysensor = sensor;
        this.main = map;
        this.file = file;
        excel = new Excel(this.file, outputfile, writer);
    }

    public SensorActivity() {}

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        Sensor sensor = sensorEvent.sensor;

        switch(sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                Acc = new ArrayList<>();
                for (int i = 0; i < 3; i++) {
                    Acc.add(sensorEvent.values[i]);
                    if (i == 0) {
                        System.out.println("[Accelerometer value X]: " + sensorEvent.values[i]);
                    } else if (i == 1) {
                        System.out.println("[Accelerometer value Y]: " + sensorEvent.values[i]);
                    } else {
                        System.out.println("[Accelerometer value Z]: " + sensorEvent.values[i]);
                    }
                }
                this.sensor.setAcc(Acc);
                this.sensor.setTimestamp(sensorEvent.timestamp);
                break;

            case Sensor.TYPE_GYROSCOPE:
                Gyro = new ArrayList<>();
                for (int i = 0; i < 3; i++) {
                    Gyro.add(sensorEvent.values[i]);
                    if (i == 0) {
                        System.out.println("[Accelerometer value X]: " + sensorEvent.values[i]);
                    } else if (i == 1) {
                        System.out.println("[Accelerometer value Y]: " + sensorEvent.values[i]);
                    } else {
                        System.out.println("[Accelerometer value Z]: " + sensorEvent.values[i]);
                    }
                }
                this.sensor.setGyro(Gyro);
                this.sensor.setTimestamp(sensorEvent.timestamp);
                break;
            default:
                System.out.println("Invalid sensor type in class SensorActivity, method onSensorChanged");
                break;
        }

        if (this.sensor.checkReady()) {
            excel.writeData(this.sensor.getAcc(), this.sensor.getGyro(), this.sensor.getTimestamp(), Mysensor.getActivity());
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
