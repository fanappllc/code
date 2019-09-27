/**
 * Copyright 2017 Google Inc. All Rights Reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.fanphotographer.helper;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import com.fanphotographer.AppController;
import com.fanphotographer.data.constant.Constants;
import com.fanphotographer.data.preference.AppPreference;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;


/**
 * A bound and started service that is promoted to a foreground service when location updates have
 * been requested and all clients unbind.
 * <p>
 * For apps running in the background on "O" devices, location is computed only once every 10
 * minutes and delivered batched every 30 minutes. This restriction applies even to apps
 * targeting "N" or lower which are run on "O" devices.
 * <p>
 * This sample show how to use a long-running service for location updates. When an activity is
 * bound to this service, frequent location updates are permitted. When the activity is removed
 * from the foreground, the service promotes itself to a foreground service, and location updates
 * continue. When the activity comes back to the foreground, the foreground service stops, and the
 * notification associated with that service is removed.
 */
public class LocationUpdatesService extends Service implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    /**
     * The desired interval for location updates. Inexact. Updates may be more or less frequent.
     */
    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 1000;
    /**
     * The fastest rate for active location updates. Updates will never be more frequent
     * than this value.
     */
    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;
    /**
     * The identifier for the notification displayed for the foreground service.
     */
    private double latitude = 0.0;
    private AppPreference appPreference;
    /**
     * Used to check whether the bound activity has really gone away and not unbound as part of an
     * orientation change. We create a foreground service notification only if the former takes
     * place.
     */

    /**
     * The entry point to Google Play Services.
     */
    private GoogleApiClient mGoogleApiClient;

    /**
     * Contains parameters used by {@link com.google.android.gms.location.FusedLocationProviderApi}.
     */
    private LocationRequest mLocationRequest;

    /**
     * The current location.
     */
    private Location mLocation;


    @Override
    public void onCreate() {
        Log.v(Constants.LOG_CAT, "LocationUpdatesService in onCreate()");
        appPreference = AppPreference.getInstance(LocationUpdatesService.this);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();

        createLocationRequest();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Tells the system to not try to recreate the service after it has been killed.
        Log.v(Constants.LOG_CAT, "LocationUpdatesService in onStartCommand()");
        return START_STICKY;
    }

    @Override

    public IBinder onBind(Intent intent) {
        Log.v(Constants.LOG_CAT, "LocationUpdatesService in onBind()");
        return null;
    }


    @Override
    public void onDestroy() {
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i(Constants.LOG_CAT, "GoogleApiClient connected");
        try {
            mLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            requestLocationUpdates();
        } catch (SecurityException unlikely) {
            Log.e(Constants.LOG_CAT, "Lost location permission." + unlikely);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.e(Constants.LOG_CAT, "GoogleApiClient connection suspended.");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e(Constants.LOG_CAT, "GoogleApiClient connection failed.");
    }

    @Override
    public void onLocationChanged(Location location) {
        mLocation = location;
        try {
            latitude = location.getLatitude();
            double longitude = location.getLongitude();
            appPreference.setString(Constants.LATITUDE, latitude + "");
            appPreference.setString(Constants.LONGITUDE, longitude + "");

            Log.v(Constants.LOG_CAT, "New location getLatitude: " + location.getLatitude());
            Log.v(Constants.LOG_CAT, "New location getLongitude: " + location.getLongitude());
            Intent buyIntent = new Intent("ACTION_REFRESH_USER.intent.MAIN");
            buyIntent.putExtra(Constants.BROADCAST_ACTION, Constants.ACTION_UPDATE_LOCATION);
            buyIntent.putExtra("bearing", mLocation.getBearing());
            sendBroadcast(buyIntent);
        } catch (Exception e) {
            Log.e(Constants.LOG_CAT,e.getMessage());
        }
        if (location != null) {

            try {


                AppController.getInstance().updateLocation(location, new AppController.TrackLocationListener() {

                    @Override
                    public void onLocationGet(double latitude, double longitude) {
                        Log.e(Constants.LOG_CAT,"onLocationGet");
                    }

                    @Override
                    public void onConnected() {

                        Log.e(Constants.LOG_CAT, "onConnected: ");

                    }
                });

            } catch (Exception e) {

                Log.e(Constants.LOG_CAT,e.getMessage());

            }

        }
    }

    /**
     * Sets the location request parameters.
     */
    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setSmallestDisplacement(1.0f);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    public void requestLocationUpdates() {
        Log.i(Constants.LOG_CAT, "Requesting location updates");
        Utils.setRequestingLocationUpdates(this, true);
        startService(new Intent(getApplicationContext(), LocationUpdatesService.class));
        try {
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, mLocationRequest, LocationUpdatesService.this);
        } catch (SecurityException unlikely) {
            Utils.setRequestingLocationUpdates(this, false);
            Log.e(Constants.LOG_CAT, "Lost location permission. Could not request updates. " + unlikely);
        }
    }

}