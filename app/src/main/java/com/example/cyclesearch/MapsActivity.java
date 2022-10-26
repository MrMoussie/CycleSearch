package com.example.cyclesearch;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.FragmentManager;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.Layout;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
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
import org.altbeacon.beacon.Region;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Array;
import java.util.Arrays;
import java.util.Collection;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, View.OnClickListener {

    private static final String IBEACON = "m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24";
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
    private ListView listView;
    private ImageButton findBike;
    private ImageButton findBeacon;
    private ImageButton exitToMain;
    private Button cookieButton;
    private View getFind_beacon;
    private View getFind_bike;
    private View buttons;
    private View map;
    private SensorEventListener sensorListener;
    private SensorActivity sensorActivity;
    private boolean init = false;
    private Excel excel;
    private static final ArrayList<Beacon> currentBeacons = new ArrayList<>();
    private static Beacon selectedBeacon;
    private View find_beacon;
    private View find_bike;
    private ArrayAdapter arrayAdapter;
    private AppCompatImageButton previousButton;
    private ArrayList macList = new ArrayList();
    private static MapStyleOptions coldStyle;
    private static MapStyleOptions warmStyle;
    private static MapStyleOptions hotStyle;

    /**
     * Initial method of the application, invoked on the start. Contains all of the initializers for the buttons, views, layouts and map
     * @param savedInstanceState instance used for starting the application
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_screen);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        coldStyle = MapStyleOptions.loadRawResourceStyle(this, R.raw.colder);
        warmStyle = MapStyleOptions.loadRawResourceStyle(this, R.raw.warmer);
        hotStyle = MapStyleOptions.loadRawResourceStyle(this, R.raw.hot);

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
        listView.setClickable(true);
        listView.setOnItemClickListener((parent, view, position, id) -> {
            // Set selected beacon
            String macAddress = parent.getItemAtPosition(position).toString().split(": ")[1].split(" ")[0];
            selectedBeacon = currentBeacons.stream().filter(x -> x.getBluetoothAddress().equals(macAddress)).collect(Collectors.toList()).get(0);
        });
        arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, new ArrayList<>()) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView text = (TextView) view.findViewById(android.R.id.text1);

                text.setTextColor(Color.BLACK);
                return view;
            }
        };
        listView.setAdapter(arrayAdapter);

        SupportMapFragment mapFragment = (SupportMapFragment)
                getSupportFragmentManager().findFragmentById(R.id.mapView);
        mapFragment.getMapAsync(this);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        Dexter.withContext(getApplicationContext())
                .withPermissions(
                        Manifest.permission.BLUETOOTH_ADMIN,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                ).withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {
                        if (multiplePermissionsReport.areAllPermissionsGranted()) {
                            System.out.println("[SYSTEM] PERMISSION GRANTED!");
                            bluetoothSetup();
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
            excel = sensorActivity.getExcel();
            sensorManager.registerListener(sensorListener, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), 200000);
            sensorManager.registerListener(sensorListener, sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE),200000);
            init = true;
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void bluetoothSetup(){
        this.beaconManager =  BeaconManager.getInstanceForApplication(this);
        this.beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout(IBEACON));
        this.beaconManager.addRangeNotifier((beacons, region) -> {
            if (beacons.size() > 0) {

                if (selectedBeacon == null) {
                    List<String> currentMacs = currentBeacons.stream().map(Beacon::getBluetoothAddress).collect(Collectors.toList());
                    for (Beacon beacon : beacons) {
                        if (!currentMacs.contains(beacon.getBluetoothAddress())) {
                            currentBeacons.add(beacon);
                            arrayAdapter.add(beacon.getBluetoothName() + ": " + beacon.getBluetoothAddress() + " " + Math.round(beacon.getDistance() *100)/100);
                        }
                        arrayAdapter.notifyDataSetChanged();
                    }
                } else {
                    for (Beacon beacon : beacons) {
                        System.out.println("[SYSTEM] FOUND DEVICE WITH RSSI " + beacon.getRssi() + " WITH ADDRESS " + beacon.getBluetoothAddress()
                                + " WITH DISTANCE " + beacon.getDistance() + " WITH NAME " + beacon.getBluetoothName());
                        if(beacon.getBluetoothAddress().equals(selectedBeacon.getBluetoothAddress())) {
                            System.out.println("[FOUND OUR BEACON]");
                            //Found our beacon
                            double RSSI = beacon.getRssi();
                            if(prevRSSI != Double.NEGATIVE_INFINITY) {

                                clearTurtleText();

                                if (RSSI < -85) {
                                    // TODO change turtle text based on RSSI
                                    setColdMap(mMap);

                                    if (RSSI < -95) {
                                        findViewById(R.id.phrase1).setVisibility(View.VISIBLE);
                                    } else {
                                        findViewById(R.id.phrase2).setVisibility(View.VISIBLE);
                                    }
                                } else if (RSSI < -60) {
                                    // TODO change turtle text based on RSSI
                                    setWarmMap(mMap);

                                    if (RSSI < -75) {
                                        findViewById(R.id.phrase3).setVisibility(View.VISIBLE);
                                    } else {
                                        findViewById(R.id.phrase4).setVisibility(View.VISIBLE);
                                    }
                                }  else {
                                    // TODO change turtle text based on RSSI
                                    setHotMap(mMap);

                                    if (RSSI < -50) {
                                        findViewById(R.id.phrase5).setVisibility(View.VISIBLE);
                                    } else {
                                        findViewById(R.id.phrase6).setVisibility(View.VISIBLE);
                                    }
                                }

//                                if (Math.abs(RSSI - prevRSSI) <= threshold) {
//                                    //no change
//
//                                } else if (RSSI - prevRSSI > threshold) {
//                                    //getting better
//                                } else {
//                                    //getting worse
//
//                                }
                            }
                            prevRSSI = RSSI;
                        }
                    }
                }
            }
        });

        this.beaconManager.startRangingBeacons(new Region("myRangingUniqueId", null, null, null));
    }

    private void clearTurtleText() {
        findViewById(R.id.phrase1).setVisibility(View.INVISIBLE);
        findViewById(R.id.phrase2).setVisibility(View.INVISIBLE);
        findViewById(R.id.phrase3).setVisibility(View.INVISIBLE);
        findViewById(R.id.phrase4).setVisibility(View.INVISIBLE);
        findViewById(R.id.phrase5).setVisibility(View.INVISIBLE);
        findViewById(R.id.phrase6).setVisibility(View.INVISIBLE);
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

    public void setColdMap(GoogleMap map) { if (map != null && coldStyle != null) map.setMapStyle(coldStyle); }

    public void setWarmMap(GoogleMap map) { if (map != null && warmStyle != null) map.setMapStyle(warmStyle); }

    public void setHotMap(GoogleMap map) { if (map != null && hotStyle != null) map.setMapStyle(hotStyle); }

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
                //TODO the switch between visibilities does not work here help !
            case R.id.exitButton:

                if(buttons.getVisibility() == View.INVISIBLE) {
                    buttons.setVisibility(View.VISIBLE);
                    findViewById(R.id.background_home).setVisibility(View.VISIBLE);
                    getFind_bike.setVisibility(View.INVISIBLE);
                    getFind_bike.findViewById(R.id.Phrases).setVisibility(View.INVISIBLE);
                    findViewById(R.id.mapView).setVisibility(View.INVISIBLE);
                    findViewById(R.id.exitButton).setVisibility(View.INVISIBLE);
                }
                //buttons.setVisibility(View.VISIBLE);
                System.out.println("does this work !!");
                break;
            case R.id.findBike:
                if(getFind_bike.getVisibility() == View.INVISIBLE){
                    getFind_bike.setVisibility(View.VISIBLE);
                    map.setVisibility(View.VISIBLE);
                    findViewById(R.id.mapView).setVisibility(View.VISIBLE);
                    buttons.setVisibility(View.INVISIBLE);
                    findViewById(R.id.background_home).setVisibility(View.INVISIBLE);

                    getFind_bike.findViewById(R.id.Phrases).setVisibility(View.VISIBLE);
                    findViewById(R.id.mapView).setVisibility(View.VISIBLE);
                    findViewById(R.id.exitButton).setVisibility(View.VISIBLE);

                /*} else if(buttons.getVisibility() == View.VISIBLE){
                    buttons.setVisibility(View.INVISIBLE);
                    findViewById(R.id.background_home).setVisibility(View.INVISIBLE);*/
                }

                break;
            case R.id.previousButton:
                getFind_beacon.setVisibility(View.INVISIBLE);
                buttons.setVisibility(View.VISIBLE);
                break;

           /* case R.id.button_cookie:
                findViewById(R.id.includeCookieTest).setVisibility(View.INVISIBLE);
                buttons.setVisibility(View.VISIBLE);*/
            default:
                System.out.println("Entered default");
                break;
        }
    }
}