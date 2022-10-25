package com.example.cyclesearch;

import androidx.annotation.NonNull;
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

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, View.OnClickListener {

    private static final String IBEACON = "m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24";
    private static String ESPMac = "EC:62:60:B2:C1";
    private double prevRSSI = Double.NEGATIVE_INFINITY;
    private double threshold = 5;
    private BeaconManager beaconManager;
    private GoogleMap mMap;
    private SensorManager sensorManager;
    private MySensor mySensor;
    private File file = new File( Environment.getExternalStorageDirectory().getPath() + "/Documents/test.csv");
    private FileWriter outputfile;
    private CSVWriter writer;
    private SupportMapFragment mapFragment;
    private Button button1;
    private Button button2;
    private Button button3;
    private ConstraintLayout frame2;
    private SensorEventListener sensorListener;
    private SensorActivity sensorActivity;
    private boolean init = false;
    private Excel excel;
    private Collection<Beacon> currentBeacons;

    /**
     * Initial method of the application, invoked on the start. Contains all of the initializers for the buttons, views, layouts and map
     * @param savedInstanceState instance used for starting the application
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_screen);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);


        frame2 = findViewById(R.id.ConstraintLayout);

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
            bluetoothSetup();

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
            excel = sensorActivity.getExcel();
            sensorManager.registerListener(sensorListener, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), 200000);
            sensorManager.registerListener(sensorListener, sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE),200000);
            init = true;
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void bluetoothSetup(){
        this.beaconManager =  BeaconManager.getInstanceForApplication(this);
        this.beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout(IBEACON));
        this.beaconManager.addRangeNotifier((beacons, region) -> {
            if (beacons.size() > 0) {

                if (ESPMac == null) {
                    this.currentBeacons = beacons;
                } else {
                    for (Beacon beacon : beacons) {
                        System.out.println("[SYSTEM] FOUND DEVICE WITH RSSI " + beacon.getRssi() + " WITH ADDRESS " + beacon.getBluetoothAddress()
                                + " WITH ID3 " + beacon.getId3());
                        if(beacon.getBluetoothAddress().equals(ESPMac)) {
                            //Found our beacon
                            double RSSI = beacon.getRssi();
                            if(prevRSSI != Double.NEGATIVE_INFINITY) {
                                if (Math.abs(RSSI - prevRSSI) <= threshold) {
                                    //no change

                                } else if (RSSI - prevRSSI > threshold) {
                                    //getting better
                                } else {
                                    //getting worse
                                }
                            }
                            prevRSSI = RSSI;
                        }
                    }
                }
            }
        });
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

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            // Button used to switch between the main page, and the measurement/map page
            case R.id.button2:
                FragmentManager fm = getSupportFragmentManager();
                mapFragment = (SupportMapFragment) fm.findFragmentById(R.id.mapView);
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
            // Button used to switch between the measurement page, and the main page
            case R.id.button_second:
                button2 = findViewById(R.id.button_second);
                button2.setOnClickListener(this);
                findViewById(R.id.includeMain).setVisibility(View.VISIBLE);
                frame2.setVisibility(View.GONE);
                break;
            default:
                System.out.println("Entered default");
                System.exit(0);
                break;
        }
    }
}


//        RadioGroup radioGroup =( findViewById(R.id.radioGroup));
//        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
//            if(checkedId == R.id.bikingButton && mySensor != null){
//                System.out.println("You are now biking");
//                mySensor.setActivity("Biking");
//            }
//            else if(checkedId == R.id.walkingButton && mySensor != null){
//                System.out.println("You are now walking");
//                mySensor.setActivity("Walking");
//            }
//            else if(checkedId == R.id.standingButton && mySensor != null){
//                System.out.println("You are standing");
//                mySensor.setActivity("Standing");
//            }
//            else if(checkedId == R.id.sittingButton && mySensor != null){
//                System.out.println("You are now sitting");
//                mySensor.setActivity("Sitting");
//            }
//
//        });