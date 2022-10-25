package com.example.cyclesearch;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.fragment.app.Fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.text.Layout;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;

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
import java.util.List;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, View.OnClickListener {

    private GoogleMap mMap;
    private SensorManager sensorManager;
    private MySensor mySensor;
    private File file = new File( Environment.getExternalStorageDirectory().getPath() + "/Documents/test.csv");
    private FileWriter outputfile;
    private CSVWriter writer;
    private SupportMapFragment mapFragment;
    private ListView listView;
    private ImageButton findBike;
    private ImageButton findBeacon;
    private ImageButton exitToMain;
    private View getFind_beacon;
    private View getFind_bike;
    private View buttons;
    private View map;
    private SensorEventListener sensorListener;
    private SensorActivity sensorActivity;
    private View find_beacon;
    private View find_bike;
    private ArrayAdapter arrayAdapter;
    private AppCompatImageButton previousButton;
    private ArrayList macList = new ArrayList();

    /**
     * Initial method of the application, invoked on the start. Contains all of the initializers for the buttons, views, layouts and map
     * @param savedInstanceState instance used for starting the application
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_screen);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        findBike = findViewById(R.id.findBike);
        findBike.setOnClickListener(this);
        findBeacon = findViewById(R.id.findBeacon);
        findBeacon.setOnClickListener(this);
        exitToMain = findViewById(R.id.exitButton);
        exitToMain.setOnClickListener(this);

        getFind_beacon = findViewById(R.id.includeFind_beacon);
        getFind_bike = findViewById(R.id.includeFind_bike);
        buttons = findViewById(R.id.differentButtons);
        map = findViewById(R.id.map);

        find_beacon = findViewById(R.id.includeFind_beacon);
        previousButton = find_beacon.findViewById(R.id.previousButton);
        previousButton.setOnClickListener(this);

        listView = find_beacon.findViewById(R.id.listView);
        arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, macList);
        listView.setAdapter(arrayAdapter);

        SupportMapFragment mapFragment = (SupportMapFragment)
                getSupportFragmentManager().findFragmentById(R.id.mapView);
        mapFragment.getMapAsync(this);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

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
     * Method used for initializing the main components of the system: CSV writer & Sensors
     */
    private void init() {
        try {
            // create FileWriter object with file as parameter
            outputfile = new FileWriter(file);

            // create CSVWriter object filewriter object as parameter
            writer = new CSVWriter(outputfile);

            // adding header to csv
            String[] header = { "AccX", "AccY", "AccZ", "GyroX", "GyroY", "GyroZ", "Timestamp", "Activity" };
            writer.writeNext(header);

            mySensor = new MySensor();
            sensorListener = new SensorActivity(mySensor, writer);
            sensorActivity = (SensorActivity) sensorListener;
            Excel excel = sensorActivity.getExcel();
            sensorManager.registerListener(sensorListener, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), 200000);
            sensorManager.registerListener(sensorListener, sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE),200000);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Method used to turn the Accelerometer and Gyrometer sensors ON
     */
    private void sensorON() {
        sensorManager.registerListener(sensorListener, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), 200000);
        sensorManager.registerListener(sensorListener, sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE),200000);
    }

    /**
     * Method used to turn the Accelerometer and Gyrometer sensors OFF
     */
    private void sensorOFF() {
        sensorManager.unregisterListener(sensorListener,sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER));
        sensorManager.unregisterListener(sensorListener,sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE));

        if (sensorActivity != null) {
            sensorActivity.resetAccCounter();
            sensorActivity.resetGyroCounter();
        }
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
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }

    /**
     * Method used to monitor whether one of the buttons was clicked.
     * @param view object that allows to control the view of the layout
     */
    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.findBeacon:
                getFind_beacon.setVisibility(View.VISIBLE);
                buttons.setVisibility(View.INVISIBLE);
                // if (beaconIsSet == true) {
                findViewById(R.id.findBeacon).setVisibility(View.INVISIBLE);
                // }
                break;
            case R.id.exitButton:
                findViewById(R.id.mapView).setVisibility(View.INVISIBLE);
                findViewById(R.id.exitButton).setVisibility(View.INVISIBLE);
                getFind_bike.findViewById(R.id.Phrases).setVisibility(View.INVISIBLE);
                buttons.setVisibility(View.VISIBLE);
                buttons.bringToFront();
                System.out.println("does this work !!");
            case R.id.findBike:
                getFind_bike.setVisibility(View.VISIBLE);
                map.setVisibility(View.VISIBLE);
                buttons.setVisibility(View.INVISIBLE);
                break;
            case R.id.previousButton:
                getFind_beacon.setVisibility(View.INVISIBLE);
                buttons.setVisibility(View.VISIBLE);
            default:
                System.out.println("Entered default");
                break;
        }
    }
}