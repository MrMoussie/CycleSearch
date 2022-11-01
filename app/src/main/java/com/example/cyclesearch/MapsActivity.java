package com.example.cyclesearch;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
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
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

import weka.classifiers.Classifier;
import weka.core.DenseInstance;
import weka.core.Instances;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, View.OnClickListener, LocationListener {

    private static final String IBEACON = "m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24";
    private static String selectedBeaconAddress;
    private double prevRSSI = Double.NEGATIVE_INFINITY;
    private double threshold = 5;
    private BeaconManager beaconManager;
    private GoogleMap mMap;
    private SensorManager sensorManager;
    private MySensor mySensor;
//    private File file = new File( Environment.getExternalStorageDirectory().getPath() + "/Documents/test.csv");
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
    private ImageButton reset;
    private Context mContext;
    private PowerManager powerManager;
    private Classifier cls;
    private InputStream fileStream;
    private final ArrayList<String> previousValues = new ArrayList<>();
    private Queue queue;
    private File addressFile = new File (Environment.getExternalStorageDirectory() + "/Download", "address.txt");
    private LocationManager locationManager;
    private LatLng location;
    private static final int ZOOM = 15;
    private static String distance;

    private Attribute lastActivity;

    private boolean isHot = false;

    // FILES
    private final static String FILE_J48 = "treesJ48.model";

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
        reset = findViewById(R.id.resetButton);
        reset.setOnClickListener(this);

        mContext = getApplicationContext();
        powerManager = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
        final PowerManager.WakeLock wakeLock =  powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,"cyclesearch:keepAwake");
        wakeLock.acquire();

        this.locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

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
            findViewById(R.id.selectedTurtle).setVisibility(View.VISIBLE);
            findViewById(R.id.selectedTurtle).setX(parent.getX() - 110);
            findViewById(R.id.selectedTurtle).setY(parent.getY() + 20);
            // Set selected beacon
            selectedBeaconAddress = parent.getItemAtPosition(position).toString().split(": ")[1].split(" ")[0];
            selectedBeacon = currentBeacons.stream().filter(x -> x.getBluetoothAddress().equals(selectedBeaconAddress)).collect(Collectors.toList()).get(0);
            if (addressFile.length() == 0 || !addressFile.exists()) {
                try {
                    FileWriter writer = new FileWriter(addressFile);
                    writer.write(selectedBeacon.getBluetoothAddress());
                    System.out.println("This the address that was saved: " + selectedBeacon.getBluetoothAddress());
                    writer.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

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
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                ).withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {
                        if (multiplePermissionsReport.areAllPermissionsGranted()) {
                            System.out.println("[SYSTEM] PERMISSION GRANTED!");
                            init();
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
    @SuppressLint("MissingPermission")
    private void init() {
        try {
            initClassifier(FILE_J48);
            this.queue = new Queue();
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 500, 10, this);


            if (this.addressFile.exists()) {
                try (Scanner scanner = new Scanner(this.addressFile)) {
                    selectedBeaconAddress = scanner.nextLine();

                    if (selectedBeaconAddress != null) {
                        getFind_beacon.setVisibility(View.INVISIBLE);
                        buttons.setVisibility(View.VISIBLE);
                        reset.setVisibility(View.VISIBLE);
                        findViewById(R.id.findBeacon).setVisibility(View.INVISIBLE);
                        findViewById(R.id.findBike).setVisibility(View.VISIBLE);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            mySensor = new MySensor();
            sensorListener = new SensorActivity(mySensor, writer, this);
            sensorActivity = (SensorActivity) sensorListener;
            sensorManager.registerListener(sensorListener, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), 200000);
            sensorManager.registerListener(sensorListener, sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE),200000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * This function closes the input file streams
     * @throws IOException
     */
    private void closeStream() throws IOException {
        if (this.fileStream != null) this.fileStream.close();
    }

    /**
     * This function is called to update the user activity.
     * This function adds the activity to the queue and tallies the queue when it is ready for the final decision on the user activity.
     */
    public void update() {
        this.queue.addToQueue(this.getActivityRightPocket());
        if (this.queue.isReady()) {
            Attribute activity = this.queue.tallyQueue();

            if (previousValues.size() > 0 && previousValues.get(0).contains(activity.toString())) return;

            // Get current timestamp
            Date date = new Date(System.currentTimeMillis());
            @SuppressLint("SimpleDateFormat") DateFormat formatter = new SimpleDateFormat("HH:mm:ss");
            String dateFormatted = formatter.format(date);

            // Add
            previousValues.add(0, dateFormatted + ' ' + this.queue.tallyQueue().toString());
            this.arrayAdapter.notifyDataSetChanged();

            if (this.location != null) {
                if (this.lastActivity == Attribute.BIKING && activity != Attribute.BIKING) this.mMap.addMarker(new MarkerOptions().position(this.location).title("Location of your bike!"));
                if (this.lastActivity != Attribute.BIKING && activity == Attribute.BIKING && this.isHot) this.mMap.clear();
            }

            this.lastActivity = activity;

            // DEVELOPER DEBUG PURPOSE
            System.out.println("[SYSTEM] Activity detected: " + this.queue.tallyQueue());
        }
    }

    /**
     * This function initializes the classifier of the model from the file.
     * @param file file name to be used, from the assets folder
     * @throws Exception
     */
    private void initClassifier(String file) throws Exception {
        this.fileStream = getAssets().open(file);
        this.cls = (Classifier) weka.core.SerializationHelper
                .read(this.fileStream);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void bluetoothSetup(){
        this.beaconManager =  BeaconManager.getInstanceForApplication(this);
        this.beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout(IBEACON));
        this.beaconManager.addRangeNotifier((beacons, region) -> {
            if (beacons.size() > 0) {
                if (selectedBeaconAddress == null) {
                    List<String> currentMacs = currentBeacons.stream().map(Beacon::getBluetoothAddress).collect(Collectors.toList());
                    for (Beacon beacon : beacons) {
                        if (!currentMacs.contains(beacon.getBluetoothAddress())) {
                            currentBeacons.add(beacon);
                            arrayAdapter.add(beacon.getBluetoothName() + ": " + beacon.getBluetoothAddress() + " \ndistance: " + Math.round(beacon.getDistance() * 100.0) / 100.0 + "m");
                        }
                        arrayAdapter.notifyDataSetChanged();
                    }
                } else {
                    for (Beacon beacon : beacons) {
                        System.out.println("[SYSTEM] FOUND DEVICE WITH RSSI " + beacon.getRssi() + " WITH ADDRESS " + beacon.getBluetoothAddress()
                                + " WITH DISTANCE " + beacon.getDistance() + " WITH NAME " + beacon.getBluetoothName());
                        if(beacon.getBluetoothAddress().equals(selectedBeaconAddress)) {
                            System.out.println("[FOUND OUR BEACON]");
                            //Found our beacon
                            double RSSI = beacon.getRssi();
                            if(prevRSSI != Double.NEGATIVE_INFINITY) {

                                clearTurtleText();

                                if (RSSI < -85) {
                                    setColdMap(mMap);
                                    if (RSSI < -95) {
                                        findViewById(R.id.phrase1).setVisibility(View.VISIBLE);
                                    } else {
                                        findViewById(R.id.phrase2).setVisibility(View.VISIBLE);
                                    }
                                } else if (RSSI < -60) {
                                    setWarmMap(mMap);

                                    if (RSSI < -75) {
                                        findViewById(R.id.phrase3).setVisibility(View.VISIBLE);
                                    } else {
                                        findViewById(R.id.phrase4).setVisibility(View.VISIBLE);
                                    }
                                }  else {
                                    setHotMap(mMap);

                                    if (RSSI < -50) {
                                        findViewById(R.id.phrase5).setVisibility(View.VISIBLE);
                                    } else {
                                        findViewById(R.id.phrase6).setVisibility(View.VISIBLE);
                                    }
                                }
                            }

                            distance = String.valueOf(Math.round(beacon.getDistance() * 100.0) / 100.0) + 'm';
                            TextView distanceView = (TextView) findViewById(R.id.distance);
                            distanceView.setText(distance);
                            distanceView.setTextColor(Color.BLACK);

                            prevRSSI = RSSI;
                        }
                    }
                }
            }
        });

        this.beaconManager.startRangingBeacons(new Region("myRangingUniqueId", null, null, null));
    }

    private void clearTurtleText() {
        findViewById(R.id.loading_speech).setVisibility(View.INVISIBLE);
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
    }

    public void setColdMap(GoogleMap map) { if (map != null && coldStyle != null) this.isHot = false; map.setMapStyle(coldStyle); }

    public void setWarmMap(GoogleMap map) { if (map != null && warmStyle != null) this.isHot = false; map.setMapStyle(warmStyle); }

    public void setHotMap(GoogleMap map) { if (map != null && hotStyle != null) this.isHot = true; map.setMapStyle(hotStyle); }

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
                findViewById(R.id.selectedTurtle).setVisibility(View.INVISIBLE);
                break;
            case R.id.exitButton:
                if(buttons.getVisibility() == View.INVISIBLE) {
                    buttons.setVisibility(View.VISIBLE);
                    findViewById(R.id.background_home).setVisibility(View.VISIBLE);
                    getFind_bike.setVisibility(View.INVISIBLE);
                    getFind_bike.findViewById(R.id.Phrases).setVisibility(View.INVISIBLE);
                    findViewById(R.id.mapView).setVisibility(View.INVISIBLE);
                    findViewById(R.id.exitButton).setVisibility(View.INVISIBLE);
                }
                break;
            case R.id.resetButton:
                if (findViewById(R.id.findBeacon).getVisibility() == View.INVISIBLE) {
                    findViewById(R.id.findBeacon).setVisibility(View.VISIBLE);
                    selectedBeacon = null;
                    if (addressFile.exists()) {
                        addressFile.delete();
                    }
                }
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
                }
                break;
            case R.id.previousButton:
                getFind_beacon.setVisibility(View.INVISIBLE);
                buttons.setVisibility(View.VISIBLE);
                if (selectedBeacon != null) {
                    findViewById(R.id.findBeacon).setVisibility(View.INVISIBLE);
                    findViewById(R.id.findBike).setVisibility(View.VISIBLE);
                    findViewById(R.id.resetButton).setVisibility(View.VISIBLE);
                }
                break;
            default:
                System.out.println("Entered default");
                break;
        }
    }

    /**
     * This function uses the model and the sensor data to predict the activity of the user.
     * @return Attribute with the activity of the user
     */
    private Attribute getActivityRightPocket() {
        if (!this.mySensor.isReady()) return null;

        // Attributes for the prediction model
        // Right pocket
        final weka.core.Attribute attributeRightPocketAx = new weka.core.Attribute(Attribute.RIGHT_POCKET_AX.toString());
        final weka.core.Attribute attributeRightPocketAy = new weka.core.Attribute(Attribute.RIGHT_POCKET_AY.toString());
        final weka.core.Attribute attributeRightPocketAz = new weka.core.Attribute(Attribute.RIGHT_POCKET_AZ.toString());
        final weka.core.Attribute attributeRightPocketGx = new weka.core.Attribute(Attribute.RIGHT_POCKET_GX.toString());
        final weka.core.Attribute attributeRightPocketGy = new weka.core.Attribute(Attribute.RIGHT_POCKET_GY.toString());
        final weka.core.Attribute attributeRightPocketGz = new weka.core.Attribute(Attribute.RIGHT_POCKET_GZ.toString());

        final List<String> classes = new ArrayList<String>() {
            {
                add(Attribute.WALKING.toString());
                add(Attribute.STANDING.toString());
                add(Attribute.SITTING.toString());
                add(Attribute.BIKING.toString());
            }
        };

        // Instances(...) requires ArrayList<> instead of List<>...
        ArrayList<weka.core.Attribute> attributeListRightPocket = new ArrayList<weka.core.Attribute>(2) {
            {
                add(attributeRightPocketAx);
                add(attributeRightPocketAy);
                add(attributeRightPocketAz);
                add(attributeRightPocketGx);
                add(attributeRightPocketGy);
                add(attributeRightPocketGz);
                weka.core.Attribute attributeClass = new weka.core.Attribute("@@class@@", classes);
                add(attributeClass);
            }
        };

        // unpredicted data sets (reference to sample structure for new instances)
        Instances dataUnpredicted = new Instances("TestInstances",
                attributeListRightPocket, 1);
        // last feature is target variable
        dataUnpredicted.setClassIndex(dataUnpredicted.numAttributes() - 1);

        DenseInstance instanceRightPocket = new DenseInstance(dataUnpredicted.numAttributes()) {
            {
                setValue(attributeRightPocketAx, mySensor.getAcc().get(0));
                setValue(attributeRightPocketAy, mySensor.getAcc().get(1));
                setValue(attributeRightPocketAz, mySensor.getAcc().get(2));
                setValue(attributeRightPocketGx, mySensor.getGyro().get(0));
                setValue(attributeRightPocketGy, mySensor.getGyro().get(1));
                setValue(attributeRightPocketGz, mySensor.getGyro().get(2));
            }
        };

        // instance to use in prediction
        instanceRightPocket.setDataset(dataUnpredicted);

        // predict new sample
        try {
            double result = cls.classifyInstance(instanceRightPocket);
            return Attribute.valueOf(classes.get(Double.valueOf(result).intValue()).toUpperCase());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        System.out.println("Entered with " + location);
        this.location = new LatLng(location.getLatitude(), location.getLongitude());
        this.mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(this.location, ZOOM));
    }
}