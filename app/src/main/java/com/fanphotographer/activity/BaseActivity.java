package com.fanphotographer.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import com.fanphotographer.R;
import com.fanphotographer.data.preference.AppPreference;
import java.util.ArrayList;
import java.util.List;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;


public abstract class BaseActivity extends AppCompatActivity {

    String[] permission;
    public AppPreference appPreference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        permission = null;
         appPreference = AppPreference.getInstance(BaseActivity.this);
    }


    public void setFont(String font) {
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath(font)
                .setFontAttrId(R.attr.fontPath)
                .build()
        );
    }

    public void checkRequiredPermission(String[] permission) {
        this.permission = permission;
        checkPermissions(permission);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    /**
     * called when required permission is granted to notify in child class need to override this
     */
    protected void invokedWhenPermissionGranted() {

    }

    /**
     * called when required permission is not or allready granted to notify in child class need to override this
     */
    protected void invokedWhenNoOrAllreadyPermissionGranted() {

    }

    protected void invokedWhenPermissionGranted(String...permission){}

    /**
     * called when required permission is not or allready granted to notify in child class need to override this
     */
        protected void invokedWhenNoOrAllreadyPermissionGranted(String...permission){}

    /**
     * check the permission
     *
     * @param permission
     */
    private void checkPermissions(String... permission) {

        if (Build.VERSION.SDK_INT >= 23 && permission != null) {

            int result;
            List<String> listPermissionsNeeded = new ArrayList<String>();
            for (String p : permission) {
                result = ContextCompat.checkSelfPermission(this, p);
                if (result != PackageManager.PERMISSION_GRANTED) {
                    listPermissionsNeeded.add(p);
                }
            }
            if (!listPermissionsNeeded.isEmpty()) {

                ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), 111);

            } else {


                invokedWhenNoOrAllreadyPermissionGranted();

            }

        } else {

            invokedWhenNoOrAllreadyPermissionGranted();

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 111 && hasAllPermissionsGranted(grantResults)) {
            allPermissionsGranted();
        } else if (requestCode == 111) {
            invokedWhenDeniedWithResult(grantResults);
        }
    }

    /**
     * called when all required permission is checked and granted
     */
    private void allPermissionsGranted() {
        invokedWhenPermissionGranted();
    }

    /**
     * check and show denied permission to notify in child class need to Override this
     *
     * @param grantResults
     */
    protected void invokedWhenDeniedWithResult(int[] grantResults) {}

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }

    /**
     * check all permission granted
     *
     * @param grantResults
     * @return
     */
    public boolean hasAllPermissionsGranted(@NonNull int[] grantResults) {
        for (int grantResult : grantResults) {
            if (grantResult == PackageManager.PERMISSION_DENIED) {
                return false;
            }
        }
        return true;
    }




    @Override
    protected void onPause() {
        super.onPause();
        unRegisterReceiver();
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver();
    }

    boolean mIsReceiverRegistered;

    private void registerReceiver() {
        if (!mIsReceiverRegistered) {
            registerReceiver(networkReceiver, new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));
            registerReceiver(gpsReceiver, new IntentFilter("android.location.PROVIDERS_CHANGED"));
            mIsReceiverRegistered = true;
        }
    }

    private void unRegisterReceiver() {
        if (mIsReceiverRegistered) {
            unregisterReceiver(networkReceiver);
            unregisterReceiver(gpsReceiver);
            mIsReceiverRegistered = false;
        }
    }

    protected void networkConnnectivityChange() {}

    private BroadcastReceiver networkReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null && intent.getAction().matches("android.net.conn.CONNECTIVITY_CHANGE")) {
                networkConnnectivityChange();
            }
        }
    };

    public void checkGpsConnectivity() {}


    private BroadcastReceiver gpsReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().matches("android.location.PROVIDERS_CHANGED")) {
                checkGpsConnectivity();
            }
        }
    };

}