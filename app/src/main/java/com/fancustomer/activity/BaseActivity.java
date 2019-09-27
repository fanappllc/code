package com.fancustomer.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.fancustomer.R;
import com.fancustomer.data.preference.AppPreference;
import com.fancustomer.helper.ExceptionHandler;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by Ghanshyam on 02/01/2017.
 */
public abstract class BaseActivity extends AppCompatActivity {

    String[] permission;
    Snackbar snackbar;
    public AppPreference appPreference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        permission = null;
        appPreference = AppPreference.getInstance(BaseActivity.this);
    }

    public void checkRequiredPermission(String[] permission) {
        this.permission = permission;
        checkPermissions(permission);
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

    /**
     * check the permission
     *
     * @param permission
     */
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
    protected void invokedWhenDeniedWithResult(int[] grantResults) {

    }

    /**
     * check all permission granted
     *
     * @param grantResults
     * @return
     */
    private boolean hasAllPermissionsGranted(@NonNull int[] grantResults) {
        for (int grantResult : grantResults) {
            if (grantResult == PackageManager.PERMISSION_DENIED) {
                return false;
            }
        }
        return true;
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

    /**
     * override this method when you need to check connectivity in child class
     */
    protected void networkConnnectivityChange() {


    }

    private BroadcastReceiver networkReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null && intent.getAction().matches("android.net.conn.CONNECTIVITY_CHANGE")) {
                networkConnnectivityChange();
            }
        }
    };


    /**
     * override this method when you need to check location enabelity in child class
     */
    public void checkGpsConnectivity() {


    }


    private BroadcastReceiver gpsReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().matches("android.location.PROVIDERS_CHANGED")) {
                checkGpsConnectivity();
            }
        }
    };

    protected void onSnackbarAction() {
    }

    public void showSnackbar(View view, String text1, String text2) {

        try {
            if (snackbar == null) {
                snackbar = Snackbar
                        .make(view, text1, Snackbar.LENGTH_INDEFINITE)
                        .setAction(text2, new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                onSnackbarAction();
                            }
                        });

                // Changing message text color
                snackbar.setActionTextColor(getResources().getColor(R.color.yellow));

                // Changing action button text color
                View sbView = snackbar.getView();
                TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);

                textView.setTextColor(Color.WHITE);
            }
            snackbar.show();
        } catch (Exception e) {
            ExceptionHandler.printStackTrace(e);


        }

    }

    public void hideSnackbar() {
        try {
            if (snackbar != null && snackbar.isShown()) {
                snackbar.dismiss();
            }
        } catch (Exception e) {
            ExceptionHandler.printStackTrace(e);

        }

    }
}
