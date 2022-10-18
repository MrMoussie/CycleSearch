package com.example.cyclesearch;

import androidx.appcompat.app.AppCompatActivity;
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
import android.widget.Button;
import android.widget.ViewFlipper;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.cyclesearch.databinding.ActivityMainBinding;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, View.OnClickListener {

    private GoogleMap mMap;
    private ActivityMainBinding binding;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        button1 = (Button) findViewById(R.id.button2);
        button1.setOnClickListener(this);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

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
            // create FileWriter object with file as parameter
            outputfile = new FileWriter(file);

            // create CSVWriter object filewriter object as parameter
            writer = new CSVWriter(outputfile);

            // adding header to csv
            String[] header = { "AccX", "AccY", "AccZ", "GyroX", "GyroY", "GyroZ", "Timestamp", "Activity" };
            writer.writeNext(header);

        }
        catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        mySensor = new MySensor();
        SensorEventListener sensorListener = new SensorActivity(mySensor, this, file, outputfile, writer);
        sensorManager.registerListener(sensorListener, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), 20000);
        sensorManager.registerListener(sensorListener, sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE), 20000);
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
        switch(view.getId()) {
            case R.id.button2:
                setContentView(R.layout.fragment_second);
                FragmentManager fm = getSupportFragmentManager();
                mapFragment = (SupportMapFragment) fm.findFragmentById(R.id.mapView);
                if (mapFragment == null) {
                    mapFragment = SupportMapFragment.newInstance();
                    fm.beginTransaction().replace(R.id.mapView, mapFragment).commit();
                    mapFragment.getMapAsync(new MapsActivity());
                    button2 = (Button) findViewById(R.id.button_second);
                    button2.setOnClickListener(this);
                }
                break;
            case R.id.button_second:
                setContentView(R.layout.activity_main);
                System.out.println("This gotta be fixed");
                break;
            default:
                System.out.println("Entered default");
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