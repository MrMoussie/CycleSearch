package com.example.cyclesearch;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;

public class SensorActivity implements SensorEventListener {

    // Sensor stuff
    private ArrayList<Float> Acc;
    private ArrayList<Float> Gyro;
    private MySensor sensor;

    // Excel stuff
    private MapsActivity main;
    private Workbook wb;
    private Sheet sheet;
    private FileOutputStream out;
    private File file = new File("if you see this, the filepath was not set");
    private String activity = "if you see this, the activity is not set";
    private Excel excel;

    public SensorActivity(MySensor sensor, MapsActivity map, HSSFWorkbook wb, HSSFSheet sheet) {
        this.sensor = sensor;
        this.main = map;
        this.wb = wb;
        this.sheet = sheet;

        try {
            this.out = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        this.excel = new Excel(out, wb, sheet, file);
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
//                    if (i == 0) {
//                        System.out.println("[Accelerometer value X]: " + sensorEvent.values[i]);
//                    } else if (i == 1) {
//                        System.out.println("[Accelerometer value Y]: " + sensorEvent.values[i]);
//                    } else {
//                        System.out.println("[Accelerometer value Z]: " + sensorEvent.values[i]);
//                    }
                }
                this.sensor.setAcc(Acc);
                this.sensor.setTimestamp(sensorEvent.timestamp);
                break;

            case Sensor.TYPE_GYROSCOPE:
                Gyro = new ArrayList<>();
                for (int i = 0; i < 3; i++) {
                    Gyro.add(sensorEvent.values[i]);
//                    if (i == 0) {
//                        System.out.println("[Accelerometer value X]: " + sensorEvent.values[i]);
//                    } else if (i == 1) {
//                        System.out.println("[Accelerometer value Y]: " + sensorEvent.values[i]);
//                    } else {
//                        System.out.println("[Accelerometer value Z]: " + sensorEvent.values[i]);
//                    }
                }
                this.sensor.setGyro(Gyro);
                this.sensor.setTimestamp(sensorEvent.timestamp);
                break;
            default:
                System.out.println("Invalid sensor type in class SensorActivity, method onSensorChanged");
                break;
        }

        //TODO
        // here data to excel sheet will have to be written.
        // this currently will only provide data either from gyro or acc !!
        // Currently it will not work !
//        excel.writeData(this.sensor.getAcc(), this.sensor.getGyro(), this.sensor.getTimestamp(), activity);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
