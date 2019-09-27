package com.fancustomer.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import android.util.Log;
import android.view.ViewGroup;

import com.fancustomer.R;

import com.fancustomer.data.constant.Constants;
import com.fancustomer.data.preference.AppPreference;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;

import java.util.ArrayList;

import java.util.List;


public class SplashActivity extends BaseActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    /**
     * Duration of wait
     **/
    private final int splashDisplayLength = 2000;
    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
          checkPermissions(Constants.LOCATION_PERMISSION);

//        if (Build.MANUFACTURER.equalsIgnoreCase("oppo")) {
//            try {
//                Intent intent = new Intent();
//                intent.setClassName("com.coloros.safecenter",
//                        "com.coloros.safecenter.permission.startup.StartupAppListActivity");
//                startActivity(intent);
//            } catch (Exception e) {
//                try {
//                    Intent intent = new Intent();
//                    intent.setClassName("com.oppo.safe",
//                            "com.oppo.safe.permission.startup.StartupAppListActivity");
//                    startActivity(intent);
//
//                } catch (Exception ex) {
//                    try {
//                        Intent intent = new Intent();
//                        intent.setClassName("com.coloros.safecenter",
//                                "com.coloros.safecenter.startupapp.StartupAppListActivity");
//                        startActivity(intent);
//                    } catch (Exception exx) {
//
//                    }
//                }
//            }
//        }
    }


    private void checkPermissions(String... permission) {

        if (Build.VERSION.SDK_INT >= 23 && permission != null) {

            int result;
            List<String> listPermissionsNeeded = new ArrayList<>();
            for (String p : permission) {
                result = ContextCompat.checkSelfPermission(this, p);
                if (result != PackageManager.PERMISSION_GRANTED) {
                    listPermissionsNeeded.add(p);
                }
            }
            if (!listPermissionsNeeded.isEmpty()) {

                ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), 111);

            } else {
                enableGPS();

            }

        } else {
            enableGPS();

        }
    }

    private void getStart() {

        /* and close this Splash-Screen after some seconds.*/
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (AppPreference.getInstance(SplashActivity.this).getString(Constants.START_PHOTO).equals("1")) {
                    Intent intent = new Intent(SplashActivity.this, StartPhotosShootActivity.class);
                    startActivity(intent);
                    finish();
                } else if (AppPreference.getInstance(SplashActivity.this).getString(Constants.TRACK_START).equals("1")) {
                    Intent intent = new Intent(SplashActivity.this, TrackPhotographer.class);
                    startActivity(intent);
                    finish();


                } else if (AppPreference.getInstance(SplashActivity.this).getString(Constants.IS_USER_REGISTERED).equals("1")) {
                    Intent intent = new Intent(SplashActivity.this, MenuScreen.class);
                    startActivity(intent);
                    finish();
                } else {
                    Intent intent = new Intent(SplashActivity.this, EnterMobileActivity.class);
                    startActivity(intent);
                    finish();
                }

            }
        }, splashDisplayLength);


    }


    private void enableGPS() {
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(SplashActivity.this)
                    .addApi(LocationServices.API).addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(SplashActivity.this).build();
            mGoogleApiClient.connect();

            LocationRequest locationRequest = LocationRequest.create();
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            locationRequest.setInterval(30 * 1000);
            locationRequest.setFastestInterval(5 * 1000);
            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                    .addLocationRequest(locationRequest);

            // **************************
            builder.setAlwaysShow(true); // this is the key ingredient
            // **************************

            PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi
                    .checkLocationSettings(mGoogleApiClient, builder.build());
            result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
                @Override
                public void onResult(@NonNull LocationSettingsResult result) {
                    final Status status = result.getStatus();
                    switch (status.getStatusCode()) {
                        case LocationSettingsStatusCodes.SUCCESS:

                            Log.v(Constants.LOG_CAT, "LocationSettingsStatusCodes.SUCCESS");
                            getStart();
                            break;

                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:

                            try {
                                Log.v(Constants.LOG_CAT, "LocationSettingsStatusCodes.RESOLUTION_REQUIRED");
                                status.startResolutionForResult(SplashActivity.this, 1000);
                            } catch (IntentSender.SendIntentException e) {
                                // Ignore the error.
                            }
                            break;

                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            Log.v(Constants.LOG_CAT, "LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE");
                            break;
                        default:
                            break;
                    }
                }
            });
        }
    }

    @Override
    protected void networkConnnectivityChange() {
        super.networkConnnectivityChange();
        if (!Constants.isInternetOn(SplashActivity.this)) {
            final ViewGroup viewGroup = (ViewGroup) ((ViewGroup) SplashActivity.this.findViewById(android.R.id.content)).getChildAt(0);
            showSnackbar(viewGroup, getResources().getString(R.string.no_internet), Constants.RETRY);

        } else {
            hideSnackbar();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 111 && hasAllPermissionsGranted(grantResults)) {
            enableGPS();
        } else {

            android.util.Log.e("tagtemporarygspatidar", "" + grantResults);
        }
    }

    private boolean hasAllPermissionsGranted(@NonNull int[] grantResults) {
        for (int grantResult : grantResults) {
            if (grantResult == PackageManager.PERMISSION_DENIED) {
                return false;
            }
        }
        return true;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1000) {
            switch (resultCode) {
                case Activity.RESULT_OK:
                    getStart();
                    break;
                case Activity.RESULT_CANCELED:
                    finish();
                    break;
                default:
                    break;
            }
        }
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.e(Constants.LOG_CAT, "onConnected: ");

    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.e(Constants.LOG_CAT, "onConnectionSuspended: ");

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e(Constants.LOG_CAT, "onConnectionFailed: ");

    }
}
