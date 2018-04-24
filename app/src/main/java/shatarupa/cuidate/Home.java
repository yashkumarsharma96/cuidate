package shatarupa.cuidate;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import android.telephony.SmsManager;

public class Home extends Activity implements ConnectionCallbacks,
        OnConnectionFailedListener, LocationListener {
    // LogCat tag
    private static final String TAG = Home.class.getSimpleName();

    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;

    private Location mLastLocation;

    // Google client to interact with Google API
    private GoogleApiClient mGoogleApiClient;
    private double latitude=0;
    private double longitude=0;
    private FusedLocationProviderClient mFusedLocationClient;

    // boolean flag to toggle periodic location updates
    private boolean mRequestingLocationUpdates = false;

    private LocationRequest mLocationRequest;
    private Button cameraButton;
    private FloatingActionButton helpButton;
    private SmsManager smsManager = SmsManager.getDefault();
    private String[] emerContactName;
    private String[] emerContactPhone;
    private String textMessage="I am in danger, please help me!!";
    private int numberOfEmergencyContacts;

    // Location updates intervals in sec
    private static int UPDATE_INTERVAL = 10000; // 10 sec
    private static int FATEST_INTERVAL = 5000; // 5 sec
    private static int DISPLACEMENT = 5; // 5 meters

    // UI elements
    private TextView lblLocation;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        lblLocation = (TextView) findViewById(R.id.lblLocation);

        // First we need to check availability of play services
        if (checkPlayServices()) {
            // Building the GoogleApi client
            buildGoogleApiClient();
            createLocationRequest();
        }

        //Fine Location
        if (ContextCompat.checkSelfPermission(Home.this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) //Checking if permission for GPS has been granted on not. If negative, the following code is executed
        {
            ActivityCompat.requestPermissions(Home.this, //Requesting permission to send SMS
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    0);
        }
        //Requesting permission to Send SMS
        if (ContextCompat.checkSelfPermission(Home.this,
                Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) //Checking if permission to send SMS has been granted on not. If negative, the following code is executed
        {
            ActivityCompat.requestPermissions(Home.this,//Requesting permission to send SMS
                    new String[]{Manifest.permission.SEND_SMS},
                    0);
        }
        //Contacts Access
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) //Checking if permission to send SMS has been granted on not. If negative, the following code is executed
        {
            ActivityCompat.requestPermissions(this,//Requesting permission to send SMS
                    new String[]{Manifest.permission.READ_CONTACTS},
                    0);
        }
        ///
        SharedPreferences settings = getSharedPreferences("pref_settings", 0);
        final Context context = this;
        helpButton = findViewById(R.id.helpButton);
        cameraButton = findViewById(R.id.cameraButton);
        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Launch Camera
                Log.i("Button", "Pressed");
                Intent intent = new Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA);
                context.startActivity(intent);
            }
        });
        numberOfEmergencyContacts=settings.getInt("numberOfEmergencyContacts",0);
        if(numberOfEmergencyContacts>0) {
            emerContactName = new String[numberOfEmergencyContacts+1];
            emerContactPhone = new String[numberOfEmergencyContacts+1];
            for (int i = 0; i <numberOfEmergencyContacts;i++)
            {
                emerContactName[i]=settings.getString("emConName" + i, "");
                emerContactPhone[i]=settings.getString("emConNum" + i, "");
            }
        }
        helpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Send SMS
                Log.i("Help Button", "Pressed");
                for(int i=0;i<numberOfEmergencyContacts;i++) {
                    textMessage="Please help me, I am in danger and require your help. I am currently at - http://maps.google.com/maps?q="+latitude+","+longitude;
                    smsManager.sendTextMessage(emerContactPhone[i], null, textMessage, null, null);
                }

            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        checkPlayServices();

        // Resuming the periodic location updates
        if (mGoogleApiClient.isConnected() && mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    /**
     * Method to display the location on UI
     * */
    private void displayLocation() {
        if (ContextCompat.checkSelfPermission(Home.this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) //Checking if permission for GPS has been granted on not. If negative, the following code is executed
        {
            ActivityCompat.requestPermissions(Home.this, //Requesting permission to send SMS
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    0);
        }
        mLastLocation = LocationServices.FusedLocationApi
                .getLastLocation(mGoogleApiClient);

        if (mLastLocation != null) {
            latitude = mLastLocation.getLatitude();
            longitude = mLastLocation.getLongitude();
            Log.i("Text setting called","yes");
            lblLocation.setText("You are currently at:\n("+latitude + ", " + longitude+")\n(Latitude, Longitude)");

        } else {

            lblLocation
                    .setText("(Couldn't get the location. Make sure location is enabled on the device)");
        }
    }

    /**
     * Creating google api client object
     * */
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
    }

    /**
     * Creating location request object
     * */
    protected void createLocationRequest() {
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FATEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT);
    }

    /**
     * Method to verify google play services on the device
     * */
    private boolean checkPlayServices() {
        GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
        int result = googleAPI.isGooglePlayServicesAvailable(this);
        if(result != ConnectionResult.SUCCESS) {
            if(googleAPI.isUserResolvableError(result)) {
                googleAPI.getErrorDialog(this, result,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            }
            else
            {
                Toast.makeText(getApplicationContext(),
                        "This device is not supported.", Toast.LENGTH_LONG)
                        .show();
            }
            return false;
        }
        return true;
    }


    /**
     * Starting the location updates
     * */
    protected void startLocationUpdates() {
        if (ContextCompat.checkSelfPermission(Home.this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) //Checking if permission for GPS has been granted on not. If negative, the following code is executed
        {
            ActivityCompat.requestPermissions(Home.this, //Requesting permission to send SMS
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    0);
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    /**
     * Stopping location updates
     */
    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }

    /**
     * Google api callback methods
     */
    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = "
                + result.getErrorCode());
    }

    @Override
    public void onConnected(Bundle arg0) {

        // Once connected with google api, get the location
        displayLocation();

        if (mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    @Override
    public void onConnectionSuspended(int arg0) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onLocationChanged(Location location) {
        // Assign the new location
        mLastLocation = location;

        Toast.makeText(getApplicationContext(), "Location changed!",
                Toast.LENGTH_SHORT).show();

        // Displaying the new location on UI
        displayLocation();
    }
    }