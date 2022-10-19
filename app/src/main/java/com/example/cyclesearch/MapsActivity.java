package com.example.cyclesearch;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.FrameLayout;
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

    private GoogleMap mMap;
    private AppBarConfiguration appBarConfiguration;
    private SensorManager sensorManager;
    private Sensor sensor;
    private MySensor mySensor;
    private File file = new File("/sdcard/Documents/testFile.csv");
    private FileWriter outputfile;
    private CSVWriter writer;
    private SupportMapFragment mapFragment;
    private Button button1;
    private Button button2;
    private Button button3;
    private Button button4;
    private Button button5;
    private Button button6;
    private Button button7;
    private FrameLayout frame1;
    private ConstraintLayout frame2;
    private SensorEventListener sensorListener;
    private SensorActivity sensorActivity;
    private int val;
    private boolean init = false;
    private Excel excel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        button1 = (Button) findViewById(R.id.button2);
        button1.setOnClickListener(this);
        button2 = (Button) findViewById(R.id.button_second);
        button2.setOnClickListener(this);
        button3 = (Button) findViewById(R.id.button_start);
        button3.setOnClickListener(this);
        button4 = (Button) findViewById(R.id.button_stop);
        button4.setOnClickListener(this);
        button5 = (Button) findViewById(R.id.button_reset);
        button5.setOnClickListener(this);
        button6 = (Button) findViewById(R.id.activityButton);
        button6.setOnClickListener(this);
        button7 = (Button) findViewById(R.id.buttonBack);
        button7.setOnClickListener(this);

        frame2 = findViewById(R.id.ConstraintLayout);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        RadioGroup radioGroup =((RadioGroup) findViewById(R.id.radioGroup));
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
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

            }
        });

        Dexter.withContext(getApplicationContext())
                .withPermissions(
                        Manifest.permission.BLUETOOTH_ADMIN,
                        Manifest.permission.ACCESS_FINE_LOCATION
                ).withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {
                        if (multiplePermissionsReport.areAllPermissionsGranted()) {
                            System.out.println("[SYSTEM] PERMISSION GRANTED!");
                            init();

                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    // on below line we are
                                    // creating a new intent
//                                    Intent i = new Intent(MapsActivity.this, Main.class);
//
//                                    // on below line we are
//                                    // starting a new activity.
//                                    startActivity(i);
//
//                                    // on the below line we are finishing
//                                    // our current activity.
//                                    finish();
                                }
                            }, 3000);
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {
                        permissionToken.continuePermissionRequest();
                    }
                }).check();
    }

    private void init() {
        try {
//            if (file.exists()) {
//                file = new File("/sdcard/Documents/testFile" + val++ + ".csv");
//            } else {
//                file = new File("/sdcard/Documents/testFile.csv");
//            }

            // create FileWriter object with file as parameter
            outputfile = new FileWriter(file);

            // create CSVWriter object filewriter object as parameter
            writer = new CSVWriter(outputfile);

            // adding header to csv
            String[] header = { "AccX", "AccY", "AccZ", "GyroX", "GyroY", "GyroZ", "Timestamp", "Activity" };
            writer.writeNext(header);

            mySensor = new MySensor();
            sensorListener = new SensorActivity(mySensor, this, file, outputfile, writer);
            sensorActivity = (SensorActivity) sensorListener;
            excel = sensorActivity.getExcel();
            sensorManager.registerListener(sensorListener, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), 20000);
            sensorManager.registerListener(sensorListener, sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE),20000);
            init = true;
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sensorON() {
        sensorManager.registerListener(sensorListener, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), 20000);
        sensorManager.registerListener(sensorListener, sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE),20000);
    }

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
        mMap = googleMap;
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }

    @Override
    public void onClick(View view) {
        ImageView turtle = null;
        switch(view.getId()) {
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
            case R.id.button_second:
                button2 = (Button) findViewById(R.id.button_second);
                button2.setOnClickListener(this);
                findViewById(R.id.includeMain).setVisibility(View.VISIBLE);
                frame2.setVisibility(View.GONE);
                break;
            case R.id.button_start:
                if (init == false) {
                    System.out.println("Sensors ON and system initialized");
                    init();
                    turtle = (ImageView) findViewById(R.id.turtleWantsToSpin);
                    turtle.animate().scaleX(.5f);
                    turtle.animate().scaleY(.5f);
                   // turtle.animate().rotation(360f);
                    System.out.println("Should be rotating");
                } else {
                    System.out.println("Sensors ON");
                    sensorON();
                }
                break;
            case R.id.button_reset:
                if (file == null) {
                    System.out.println("Nothing to delete");
                    break;
                }
                //Animation for reset


                turtle = (ImageView) findViewById(R.id.turtleWantsToSpin);
                //Animation for reset
                turtle.animate().rotationXBy(360f);
                System.out.println("System RESET");
             //  file.delete();
              //  sensorOFF();
                break;
            case R.id.button_stop:
                turtle = (ImageView) findViewById(R.id.turtleWantsToSpin);
                turtle.animate().scaleX(1f);
                turtle.animate().scaleY(1f);
                turtle.animate().rotationXBy(360f);

                System.out.println("Sensors OFF");
                //sensorOFF();
                //excel.writeData(new ArrayList<>(Arrays.asList(0.0f,0.0f,0.0f)), new ArrayList<>(Arrays.asList(0.0f,0.0f,0.0f)), 0, "init");

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

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_main, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }
//
//    @Override
//    public boolean onSupportNavigateUp() {
//        NavController navController = Navigation.findNavController(this, R.id.myMapView);
//        return NavigationUI.navigateUp(navController, appBarConfiguration)
//                || super.onSupportNavigateUp();
//    }
}