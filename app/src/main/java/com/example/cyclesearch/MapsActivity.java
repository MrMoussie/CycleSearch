package com.example.cyclesearch;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.FragmentManager;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, View.OnClickListener {

    private SensorManager sensorManager;
    private MySensor mySensor;
    private final File file = new File( Environment.getExternalStorageDirectory().getPath() + "/Documents/test.csv");
    private Button button2;
    private ConstraintLayout frame2;
    private SensorEventListener sensorListener;
    private SensorActivity sensorActivity;
    private boolean init = false;
    private Excel excel;

    /**
     * Method invoked on the initial setup of the app. Creates all the views, buttons, sets layouts and takes care of initializing the
     * google map
     * @param savedInstanceState used for creating the new instance used in the app
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        Button button1 = findViewById(R.id.button2);
        button1.setOnClickListener(this);
        button2 = findViewById(R.id.button_second);
        button2.setOnClickListener(this);
        Button button3 = findViewById(R.id.button_start);
        button3.setOnClickListener(this);
        Button button4 = findViewById(R.id.button_stop);
        button4.setOnClickListener(this);
        Button button5 = findViewById(R.id.button_reset);
        button5.setOnClickListener(this);
        Button button6 = findViewById(R.id.activityButton);
        button6.setOnClickListener(this);
        Button button7 = findViewById(R.id.buttonBack);
        button7.setOnClickListener(this);

        frame2 = findViewById(R.id.ConstraintLayout);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        RadioGroup radioGroup =(findViewById(R.id.radioGroup));
        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if(checkedId == R.id.bikingButton && mySensor != null){
                System.out.println("You are now biking");
                mySensor.setActivity("Biking");
            }
            else if(checkedId == R.id.walkingButton && mySensor != null){
                System.out.println("You are now walking");
                mySensor.setActivity("Walking");
            }
            else if(checkedId == R.id.standingButton && mySensor != null){
                System.out.println("You are standing");
                mySensor.setActivity("Standing");
            }
            else if(checkedId == R.id.sittingButton && mySensor != null){
                System.out.println("You are now sitting");
                mySensor.setActivity("Sitting");
            }

        });

        Dexter.withContext(getApplicationContext())
                .withPermissions(
//                        Manifest.permission.BLUETOOTH_ADMIN,
//                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                ).withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {
                        if (multiplePermissionsReport.areAllPermissionsGranted()) {
                            System.out.println("[SYSTEM] PERMISSION GRANTED!");
                            System.out.println(Environment.getExternalStorageDirectory().getPath());
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {
                        permissionToken.continuePermissionRequest();
                    }
                }).check();
    }

    /**
     * Method for initializing the CSV file writer, Sensor Manager & Sensors (Accelerometer and Gyrometer)
     */
    private void init() {
        try {
            // create FileWriter object with file as parameter
            FileWriter outputfile = new FileWriter(file);

            // create CSVWriter object filewriter object as parameter
            CSVWriter writer = new CSVWriter(outputfile);

            // adding header to csv
            String[] header = { "AccX", "AccY", "AccZ", "GyroX", "GyroY", "GyroZ", "Timestamp", "Activity" };
            writer.writeNext(header);

            mySensor = new MySensor();
            sensorListener = new SensorActivity(mySensor, writer);
            sensorActivity = (SensorActivity) sensorListener;
            excel = sensorActivity.getExcel();
            sensorManager.registerListener(sensorListener, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), 200000);
            sensorManager.registerListener(sensorListener, sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE),200000);
            init = true;
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Method for turning the Sensors (Accelerometer and Gyrometer) ON
     */
    private void sensorON() {
        sensorManager.registerListener(sensorListener, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), 200000);
        sensorManager.registerListener(sensorListener, sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE),200000);
    }
    /**
     * Method for turning the Sensors (Accelerometer and Gyrometer) OFF
     */
    private void sensorOFF() {
        sensorManager.unregisterListener(sensorListener,sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER));
        sensorManager.unregisterListener(sensorListener,sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE));
        sensorActivity.resetAccCounter();
        sensorActivity.resetGyroCounter();
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        LatLng sydney = new LatLng(-34, 151);
        googleMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }

    /**
     * Method for monitoring whether one of the buttons was clicked
     * @param view object used to get the ID of the buttons
     */
    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        ImageView turtle;
        switch(view.getId()) {
            // Button used to go to the second layout (one with map and sensors)
            case R.id.button2:
                FragmentManager fm = getSupportFragmentManager();
                SupportMapFragment mapFragment = (SupportMapFragment) fm.findFragmentById(R.id.mapView);
                frame2 = findViewById(R.id.ConstraintLayout);
                if (mapFragment == null) {
                    mapFragment = SupportMapFragment.newInstance();
                    fm.beginTransaction().replace(R.id.mapView, mapFragment).commit();
                    mapFragment.getMapAsync(new MapsActivity());
                } else {
                    findViewById(R.id.includeMain).setVisibility(View.GONE);
                    frame2.setVisibility(View.VISIBLE);
                }
                break;
            // Button used to return to the first layout (main one)
            case R.id.button_second:
                button2 = findViewById(R.id.button_second);
                button2.setOnClickListener(this);
                findViewById(R.id.includeMain).setVisibility(View.VISIBLE);
                frame2.setVisibility(View.GONE);
                break;
            // Button used to start the measurements
            case R.id.button_start:
                if (!init) {
                    System.out.println("Sensors ON and system initialized");
                    init();
                    turtle = findViewById(R.id.turtleWantsToSpin);
                    turtle.animate().scaleX(.5f);
                    turtle.animate().scaleY(.5f);
                    System.out.println("Should be rotating");
                } else {
                    System.out.println("Sensors ON");
                    sensorON();
                    turtle = findViewById(R.id.turtleWantsToSpin);
                    turtle.animate().scaleX(.5f);
                    turtle.animate().scaleY(.5f);
                }
                break;
            case R.id.button_reset:
                if (file == null) {
                    System.out.println("Nothing to delete");
                    break;
                }
                turtle = findViewById(R.id.turtleWantsToSpin);
                //Animation for reset
                turtle.animate().rotationXBy(360f);
                System.out.println("System RESET");
                file.delete();
                sensorOFF();
                break;
            case R.id.button_stop:
                turtle = findViewById(R.id.turtleWantsToSpin);
                turtle.animate().scaleX(1f);
                turtle.animate().scaleY(1f);
                turtle.animate().rotationXBy(360f);

                System.out.println("Sensors OFF");
                sensorOFF();
                excel.writeData(new ArrayList<>(Arrays.asList(0.0f,0.0f,0.0f)), new ArrayList<>(Arrays.asList(0.0f,0.0f,0.0f)), 0, "init");
                break;
            case R.id.activityButton:
                System.out.println("System GO BACK");
                findViewById(R.id.includeWeka).setVisibility(View.VISIBLE);
                frame2.setVisibility(View.GONE);
                findViewById(R.id.includeMain).setVisibility(View.GONE);
                break;
            case R.id.buttonBack:
                System.out.println("System GO BACK");
                findViewById(R.id.includeWeka).setVisibility(View.GONE);
                frame2.setVisibility(View.GONE);
                findViewById(R.id.includeMain).setVisibility(View.VISIBLE);
                break;
            default:
                System.out.println("Entered default");
                System.exit(0);
                break;
        }
    }
}