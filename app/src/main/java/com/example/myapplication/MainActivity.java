package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.os.Bundle;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.view.View.OnClickListener;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;



//Quelle für Accelerometer: https://www.vogella.com/tutorials/AndroidSensor/article.
//Quelle für Barometer: https://www.ssaurel.com/blog/creating-a-barometer-application-for-android/
//Quelle für GPS-Sensor: https://www.aeq-web.com/android-gps-daten-auslesen/
//Es werden Sensorwerte vom Accelerometer, Barometer und GPS-Sensor ausgelesen. Die Aktualisierung
//erfolgt über Listener, die in onResume() registriert und in onPause() abgemeldet werden.
//Die Werte werden über TextViews angezeigt. In der onSensorChanged()-Methode werden die Werte aktualisiert.

public class MainActivity extends AppCompatActivity implements SensorEventListener, OnClickListener {
    private SensorManager sensorManager;
    private boolean color = false;
    private View view;
    private long lastUpdate;
    private TextView accelerometertv;
    private LocationManager locationMangaer = null;
    private LocationListener locationListener = null;
    private Button btnGetLocation = null;
    private TextView v_longitude = null;
    private TextView v_latitude = null;
    private TextView v_location = null;
    private TextView v_speed = null;
    private TextView v_altitude = null;
    private TextView v_sat = null;
    private TextView v_gps_status = null;
    private TextView v_update_status = null;
    String longitude = "";
    String latitude = "";
    String location = "";
    String speed = "";
    String altitude = "";
    String sat_nr = "";

    public GoogleSignInClient mGoogleSignInClient;
    private int RC_SIGN_IN = 9001;
    //Auf TAG wird stets referenziert
    private String TAG ="MainActivity";

    private TextView txt;
    private SensorManager sensorManagerPressure;
    private Sensor pressureSensor;
    private SensorEventListener sensorEventListener = new SensorEventListener() { // callback listener der auf das Android device lauscht





        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            float[] values = sensorEvent.values; // das Array kommt von dem Sensor eventobjekt über das Setzer der Parameter
            txt.setText(String.format("%.3f Milibar", values[0])); // in 3 Dezimalstellen umforamtieren zur besseren darstellung in Milibar --> 1 Bar = 1000 Milibar
            android.util.Log.v("Drucksensor", "Druck: " + values[0]);
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {
        }
    };


    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        view = findViewById(R.id.tv);
        view.setBackgroundColor(Color.GREEN);
        accelerometertv = findViewById(R.id.accelerometer);
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        Log.v("Sensor","Sensormanager got initialised");
        lastUpdate = System.currentTimeMillis();


        txt = findViewById(R.id.txt); // die Referenz für den txtview
        sensorManagerPressure = (SensorManager) getSystemService(SENSOR_SERVICE); // den sonsormanager durch einen call auf systemservice bekommen
        pressureSensor = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE); // default sensor für den sensoren type

        android.util.Log.v("Drucksensor", "Initialisierung abgeschlossen");




        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); // always in portrait mode
        v_longitude = (TextView) findViewById(R.id.Lon_view);
        v_latitude = (TextView) findViewById(R.id.Lat_view);
        v_location = (TextView) findViewById(R.id.Loc_view);
        v_speed = (TextView) findViewById(R.id.Spd_view);
        v_altitude = (TextView) findViewById(R.id.Alt_view);
        v_sat = (TextView) findViewById(R.id.Sat_view);
        v_gps_status = (TextView) findViewById(R.id.Gps_status);
        v_update_status = (TextView) findViewById(R.id.Update_status);

        btnGetLocation = (Button) findViewById(R.id.button);
        btnGetLocation.setOnClickListener(this);
        locationMangaer = (LocationManager)
                getSystemService(Context.LOCATION_SERVICE);

        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        findViewById(R.id.sign_in_button).setOnClickListener(this);

    }

    @Override
    protected void onStart() {
        super.onStart();
        // Check for existing Google Sign In account, if the user is already signed in
        // the GoogleSignInAccount will be non-null.
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        updateUI(account);
    }

    @Override
    public void onClick(View v) {
        //Check if GPS is enabled
        locationListener = new APPLocationListener();
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED) {
            v_gps_status.setText("Wait for signal");
            v_gps_status.setTextColor(Color.parseColor("#0066ff"));
            locationMangaer.requestLocationUpdates(LocationManager
                    .GPS_PROVIDER, 5000, 10, locationListener);
        } else {
            v_gps_status.setText("No GPS-Access!!!");
            v_gps_status.setTextColor(Color.parseColor("#ff0000"));
        }

        switch (v.getId()) {
            case R.id.sign_in_button:
                signIn();
                break;
            case R.id.button_sign_out:
                signOut();
                break;
            // ...
        }
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }
    private void signOut() {
        mGoogleSignInClient.signOut()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        // ...
                    }
                });
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);

            // Signed in successfully, show authenticated UI.
            updateUI(account);
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
            updateUI(null);
        }
    }

    private void updateUI(GoogleSignInAccount account) {
    }

    private class APPLocationListener implements LocationListener {
        @Override
        public void onLocationChanged(Location loc) {
            //Try to get city name
            String city_name = null;
            Geocoder gcd = new Geocoder(getBaseContext(),
                    Locale.getDefault());
            List<Address> addresses;
            try {
                addresses = gcd.getFromLocation(loc.getLatitude(), loc
                        .getLongitude(), 1);
                if (addresses.size() > 0)
                    city_name = addresses.get(0).getLocality();
            } catch (IOException e) {
                city_name = "unknown";
                e.printStackTrace();
            }
            latitude = "" + loc.getLatitude(); //Get latitude
            longitude = "" + loc.getLongitude(); //Get longitude
            location = "" + city_name; //Get city name
            speed = "" + loc.getSpeed() + " m/s"; //Get speed in meters per second
            altitude = "" + loc.getAltitude() + " m"; //Get height in meters
            sat_nr = "" + loc.getExtras().getInt("satellites"); //get number of available satellites
            v_latitude.setText(latitude);
            v_longitude.setText(longitude);
            v_location.setText(location);
            v_speed.setText(speed);
            v_altitude.setText(altitude);
            v_sat.setText(sat_nr);
            v_gps_status.setText("GPS working");
            v_gps_status.setTextColor(Color.parseColor("#33cc33"));
            Calendar c = Calendar.getInstance(); //Get time on system
            Log.v("GPS-Sensor","Latitude: " + loc.getLatitude());
            Log.v("GPS-Sensor","Longitude: " + loc.getLongitude());
            Log.v("GPS-Sensor","Location: " + city_name);
            Log.v("GPS-Sensor","Speed: " + loc.getSpeed());
            Log.v("GPS-Sensor","Altitude: " + loc.getAltitude());
            Log.v("GPS-Sensor","Sat_nr: " + loc.getExtras().getInt("satellites"));
            v_update_status.setText("Last update: " + c.get(Calendar.HOUR_OF_DAY) + ":" +
                    c.get(Calendar.MINUTE) + ":" + c.get(Calendar.SECOND));
        }

        @Override
        public void onProviderDisabled(String provider) {
            // TODO Auto-generated method stub
        }

        @Override
        public void onProviderEnabled(String provider) {
            // TODO Auto-generated method stub
        }

        @Override
        public void onStatusChanged(String provider,
                                    int status, Bundle extras) {
            // TODO Auto-generated method stub
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            //Log.v("Sensor","Sensor Value changed");
            getAccelerometer(event);
        }
        /*else if(event.sensor.getType() == Sensor.TYPE_PROXIMITY){

        }*/
    }


    private void getAccelerometer(SensorEvent event) {
        float[] values = event.values;
        // Movement
        float x = values[0];
        float y = values[1];
        float z = values[2];

        float accelationSquareRoot = (x * x + y * y + z * z)
                / (SensorManager.GRAVITY_EARTH * SensorManager.GRAVITY_EARTH);
        long actualTime = event.timestamp;
        accelerometertv.setText("Acceleration: " + String.valueOf(accelationSquareRoot));
        Log.v("Sensor","Acceleration and Time calculated" + accelationSquareRoot);
        if (accelationSquareRoot >= 2)
        {
            if (actualTime - lastUpdate < 200) {
                return;
            }
            lastUpdate = actualTime;
            Toast.makeText(this, "Device was shuffed", Toast.LENGTH_SHORT)
                    .show();
            if (color) {
                view.setBackgroundColor(Color.GREEN);
            } else {
                view.setBackgroundColor(Color.RED);
            }
            color = !color;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    protected void onResume() {
        super.onResume();
        // register this class as a listener for the orientation and
        // accelerometer sensors
        sensorManager.registerListener(this,
                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_NORMAL);


        sensorManagerPressure.registerListener(sensorEventListener, pressureSensor, SensorManager.SENSOR_DELAY_UI); // um die umgebungs daten immer wieder zu verarbeiten
        android.util.Log.v("Drucksensor", "Registrieren des Drucksensors");
        Log.v("Sensor","Sensorlistener got registered");
    }

    @Override
    protected void onPause() {
        // unregister listener
        super.onPause();
        sensorManager.unregisterListener(this);

        sensorManagerPressure.unregisterListener(sensorEventListener);

        Log.v("Sensor","Sensormanager got unregistered");
    }
}
