package com.example.tajir.takemehome_driver;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.net.Uri;
import android.os.Looper;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSIONS_REQUEST = 1;
    public Double longitude,latitude;
    public boolean isOnline = false;
    private FusedLocationProviderClient mFusedLocationClient;
    private SettingsClient mSettingsClient;
    private LocationRequest mLocationRequest;
    private LocationSettingsRequest mLocationSettingsRequest;
    private LocationCallback mLocationCallback;
    private Location mCurrentLocation;
 //   private Integer reqCount = 15;
    // boolean flag to toggle the ui
    private Boolean mRequestingLocationUpdates;
    private String mLastUpdateTime;
    private static final int REQUEST_CHECK_SETTINGS = 100;
    private static final String TAG = "MainActivity";
    public Integer count = 0;
    public Double cuet_longitude , cuet_latitude;
    public Double MAX_DISTANCE = 2.0;
    private String id;
    public Integer loopCount = 0;
    public boolean isFirstTime = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();


    }

    private void init() {


        Intent intent = getIntent();
        id = intent.getStringExtra("id");
        Log.d("ID",id);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mSettingsClient = LocationServices.getSettingsClient(this);

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                // location is received
                mCurrentLocation = locationResult.getLastLocation();
                mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
                longitude = mCurrentLocation.getLongitude();
                latitude = mCurrentLocation.getLatitude();
                Log.d("Location", "Longitude = " + longitude.toString() + ", Latitude = " + latitude.toString());
                Toast.makeText(MainActivity.this, "{" + longitude.toString() + "," + latitude.toString() + "}", Toast.LENGTH_SHORT).show();
                if(!inRadius()) {
                    Log.d("Location", "Longitude = " + longitude.toString() + ", Latitude = " + latitude.toString());
                    Log.d("Status","Not in the active region"+" "+count.toString());
                    mRequestingLocationUpdates = false;
                    if(count == 12) {
                        stopLocationUpdates();
                        stopBackgroundService();
                        isOnline = false;
                        mRequestingLocationUpdates = false;
                        count = 0;
                    }
                    count++;
                }

                if(isOnline) {
                    startBackgroundService();
                }
                else
                    stopBackgroundService();
            }
        };

        mRequestingLocationUpdates = false;

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(5000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        mLocationSettingsRequest = builder.build();

        updateUI();

    }

    private void requestLocationUpdates() {
        Log.d("Status","entered requestLocationUpdates()");
        mSettingsClient
                .checkLocationSettings(mLocationSettingsRequest)
                .addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
                    @SuppressLint("MissingPermission")
                    @Override
                    public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                        Log.i("Status", "All location settings are satisfied.");

                        Toast.makeText(getApplicationContext(), "Started location updates!", Toast.LENGTH_SHORT).show();

                        //noinspection MissingPermission
                        mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                                mLocationCallback, Looper.myLooper());

                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        int statusCode = ((ApiException) e).getStatusCode();
                        switch (statusCode) {
                            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                Log.i("Status", "Location settings are not satisfied. Attempting to upgrade " +
                                        "location settings ");
                                try {
                                    // Show the dialog by calling startResolutionForResult(), and check the
                                    // result in onActivityResult().
                                    ResolvableApiException rae = (ResolvableApiException) e;
                                    rae.startResolutionForResult(MainActivity.this, REQUEST_CHECK_SETTINGS);
                                } catch (IntentSender.SendIntentException sie) {
                                    Log.i("Status", "PendingIntent unable to execute request.");
                                }
                                break;
                            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                String errorMessage = "Location settings are inadequate, and cannot be " +
                                        "fixed here. Fix in Settings.";
                                Log.e("Status", errorMessage);

                                Toast.makeText(MainActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                        }

                    }
                });
    }

    public void stopLocationUpdates() {
        Log.d("Status","Stopping Location updates");

        mFusedLocationClient
                .removeLocationUpdates(mLocationCallback)
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Log.d("Status","Location updates are stopped");
                        Toast.makeText(getApplicationContext(), "Location updates stopped!", Toast.LENGTH_SHORT).show();
                    }
                });
        Button button = findViewById(R.id.go_online);
        button.setEnabled(true);
        button = findViewById(R.id.go_offline);
        button.setEnabled(false);
    }
    private void openSettings() {
        Intent intent = new Intent();
        intent.setAction(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package",
                BuildConfig.APPLICATION_ID, null);
        intent.setData(uri);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    public void onlineBtnClick(View view) {
        isOnline = true;
        Log.d("Status","Online");
        // Requesting ACCESS_FINE_LOCATION using Dexter library
        Dexter.withActivity(this)
                .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        mRequestingLocationUpdates = true;
                        Button button = findViewById(R.id.go_online);
                        button.setEnabled(false);
                        button = findViewById(R.id.go_offline);
                        button.setEnabled(true);
                        requestLocationUpdates();
                    }
                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        if (response.isPermanentlyDenied()) {
                            // open device settings when the permission is
                            // denied permanently
                            openSettings();
                        }
                    }
                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                        token.continuePermissionRequest();
                        Button button = findViewById(R.id.go_online);
                        button.setEnabled(false);
                        button = findViewById(R.id.go_offline);
                        button.setEnabled(true);
                        requestLocationUpdates();
                    }
                }).check();

    }

    public void offlineBtnClick(View view) {
        isOnline = false;
        Log.d("Status","Offline.");
        mRequestingLocationUpdates = false;
        stopLocationUpdates();
        stopBackgroundService();
    }

    public void startBackgroundService() {
        Log.d(TAG,"Starting Background Service");
        Intent intent = new Intent(getApplicationContext() , RequestCount.class);
        intent.putExtra("status","start");
        intent.putExtra("id" , id);
        intent.putExtra("loopCount",loopCount.toString());
        loopCount++;
        startService(intent);
    }
    public void stopBackgroundService() {
        Intent intent = new Intent(getApplicationContext() , RequestCount.class);
        intent.putExtra("status","stop");
        intent.putExtra("id",id);
        loopCount = 0;
        intent.putExtra("loopCount", loopCount.toString());
        startService(intent);
    }

    public Double deg2rad(Double deg) {
        return deg * (Math.PI / 180);
    }

    boolean inRadius() {
        cuet_latitude = 22.459892;
        cuet_longitude = 91.970996;

        Double R = 6371.0;
        Double dLat = deg2rad(cuet_latitude - latitude);
        Double dLon = deg2rad(cuet_longitude - longitude);
        Double a,dist,c;
        a = Math.sin(dLat/2) * Math.sin(dLat/2)
                + Math.cos(deg2rad(latitude)) * Math.cos(deg2rad(cuet_latitude))
                * Math.sin(dLon/2) * Math.sin(dLon/2);
        c = 2 * Math.atan2(Math.sqrt(a) , Math.sqrt(1-a));
        dist = R * c;
        Log.d("Distance from CUET", dist.toString()+" km");
        if(dist > MAX_DISTANCE)
            return false;

        return true;
    }
    String reqCount = "0";
    public void updateUI() {

        //JSON
        RequestQueue queue = Volley.newRequestQueue(this);
        final TextView tv = findViewById(R.id.text);

        String url = "http://6105b92d.ngrok.io/studentnumrequest";
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    reqCount = response.getString("count");
                    tv.setText(reqCount+" "+getResources().getString(R.string.textViewText));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.v("Status","Json request failed");
            }
        });
        queue.add(jsonObjectRequest);
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

}
