package com.fanphotographer.utility;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;


public class ActivityUtils {

    private static ActivityUtils sActivityUtils = null;

    public static ActivityUtils getInstance() {
        if (sActivityUtils == null) {
            sActivityUtils = new ActivityUtils();
        }
        return sActivityUtils;
    }

    public void invokeActivity(Activity activity, Class<?> tClass, boolean shouldFinish) {
        Intent intent = new Intent(activity, tClass);
        activity.startActivity(intent);
        if (shouldFinish) {
            activity.finish();
        }
    }

    public static final String[] LOCATION_PERMISSION = {
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE

};



    public void sendFlow(Activity activity, Class<?> tClass){
        Intent intent = new Intent(activity, tClass);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        activity.startActivity(intent);
    }

    public void sendFlowAnother(Activity activity, Class<?> tClass){
        Intent intent = new Intent(activity, tClass);
        activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        activity.startActivity(intent);
        activity.finish();
    }

    public void sendFlowwithfinish(Activity activity, Class<?> tClass){
        Intent intent = new Intent(activity, tClass);
        activity.startActivity(intent);
        activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        activity.finish();
    }

}
