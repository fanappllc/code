package com.fanphotographer.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.ImageView;

import com.fanphotographer.R;
import com.fanphotographer.data.constant.Constants;
import com.fanphotographer.helper.LocationUpdatesService;
import com.fanphotographer.utility.ActivityUtils;
import com.fanphotographer.utility.AppUtils;
import com.fanphotographer.utility.DialogUtils;
import com.fanphotographer.utility.Logger;
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
import com.google.firebase.iid.FirebaseInstanceId;

import java.io.File;


public class SplashActivity extends BaseActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private GoogleApiClient mGoogleApiClient;
    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        check();
    }

    public void check(){
        if (AppUtils.isNetworkConnected()) {
                checkRequiredPermission(ActivityUtils.LOCATION_PERMISSION);
            String refreshedToken = FirebaseInstanceId.getInstance().getToken();
            Logger.d(Constants.LOG_CAT, "Refreshed token: " + refreshedToken);
        } else {
            DialogUtils.SweetAlertDialog(SplashActivity.this,  new DialogUtils.AlertMessageListener() {
                @Override
                public void onClickOk() {
                    Log.e(Constants.LOG_CAT,"onClickOk");
                }
            });
        }
    }


    @Override
    protected void invokedWhenNoOrAllreadyPermissionGranted() {
        enableGPS();
    }

    @Override
    protected void invokedWhenNoOrAllreadyPermissionGranted(String... permission) {
        super.invokedWhenNoOrAllreadyPermissionGranted(permission);
        enableGPS();
    }


    /*
    Duration of wait
   */
    private void getStart() {


        int splashDisplayLenght = 3000;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {


                String isUserRegistered = appPreference.getString(Constants.IS_USER_REGISTERED);
                String isTermsConditionAccepted = appPreference.getString(Constants.IS_TERMS_CONDITION_ACCEPTED);
                String isProfileUpdated = appPreference.getString(Constants.IS_PROFILE_UPDATED);
                String ssnNo = appPreference.getString(Constants.SSN_NO);
                String drivingLicenceImage = appPreference.getString(Constants.DRIVING_LICENCE_IMAGE);
                String isUserAccountedAdded = appPreference.getString(Constants.IS_USER_ACCOUNTED_ADDED);
                String isRegistrationFeePaid = appPreference.getString(Constants.IS_REGISTRATION_FEE_PAID);
                boolean userLocationActivity = appPreference.getBoolean(Constants.USERLOCATION_ACTIVITY);
                String comeFrom = appPreference.getString(Constants.START_PHOTO);

//                if(comeFrom.equalsIgnoreCase("1")){
//                    intent = new Intent(SplashActivity.this, PhotoShootStarted.class);
//                }else if (userLocationActivity) {
//                    intent = new Intent(SplashActivity.this, UserLocationActivity.class);
//                } else {
//                    if (isUserRegistered.equalsIgnoreCase("1")) {
//                        intent = new Intent(SplashActivity.this, MenuScreen.class);
//                    } else if (isTermsConditionAccepted.equalsIgnoreCase("0")) {
//                        intent = new Intent(SplashActivity.this, TermsAndConditionsActivity.class);
//                    } else if (isProfileUpdated.equalsIgnoreCase("0")) {
//                        intent = new Intent(SplashActivity.this, UpdateProfileActivity.class);
//                    } else if (ssnNo.equalsIgnoreCase("null")) {
//                        intent = new Intent(SplashActivity.this, SsnActivity.class);
//                    } else if (drivingLicenceImage.equalsIgnoreCase("null")) {
//                        intent = new Intent(SplashActivity.this, IdProofActivity.class);
//                    } else if (isUserAccountedAdded.equalsIgnoreCase("0")) {
//                        intent = new Intent(SplashActivity.this, AccountDetailActivity.class);
//                    } else if (isRegistrationFeePaid.equalsIgnoreCase("0")) {
//                        intent = new Intent(SplashActivity.this, RegistrationFeeActivity.class);
//                    } else {
//                        if (isUserRegistered.equalsIgnoreCase("0")) {
//                            intent = new Intent(SplashActivity.this, WaitingActivity.class);
//                        } else {
//                            intent = new Intent(SplashActivity.this, GetStartedActivity.class);
//                        }
//
//                    }
//                }


                if(comeFrom.equalsIgnoreCase("1")){
                    intent = new Intent(SplashActivity.this, PhotoShootStarted.class);
                }else if (userLocationActivity) {
                    intent = new Intent(SplashActivity.this, UserLocationActivity.class);
                }else if (isUserRegistered.equalsIgnoreCase("1")) {
                    intent = new Intent(SplashActivity.this, MenuScreen.class);
                }else if (isRegistrationFeePaid.equalsIgnoreCase("1")) {
                    intent = new Intent(SplashActivity.this, WaitingActivity.class);
                }else {
                    intent = new Intent(SplashActivity.this, GetStartedActivity.class);
                }



                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                startActivity(intent);
                finish();

            }
        }, splashDisplayLenght);


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
                public void onResult(LocationSettingsResult result) {
                    final Status status = result.getStatus();
                    if (status.getStatusCode() == LocationSettingsStatusCodes.SUCCESS) {
                        Log.v(Constants.LOG_CAT, "LocationSettingsStatusCodes.SUCCESS");
                        getStart();

                    } else if (status.getStatusCode() == LocationSettingsStatusCodes.RESOLUTION_REQUIRED) {
                        try {
                            status.startResolutionForResult(SplashActivity.this, 1000);
                        } catch (Exception e) {
                             Log.e(Constants.LOG_CAT,e.getMessage());
                        }

                    } else if (status.getStatusCode() == LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE) {
                        Log.v(Constants.LOG_CAT, "LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE");
                    }
                }
            });
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 111 && hasAllPermissionsGranted(grantResults)) {
                enableGPS();
        } else {
            finish();
            android.util.Log.e("tagtemporarygspatidar", "" + grantResults);
        }
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
    public void onBackPressed() {
        super.onBackPressed();
        Log.e(Constants.LOG_CAT,"backpressed");
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.e(Constants.LOG_CAT,"onConnected");
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.e(Constants.LOG_CAT,"onConnectionSuspended");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e(Constants.LOG_CAT,"onConnectionFailed");
    }
}
