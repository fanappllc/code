package com.fanphotographer.utility;

import android.app.ActivityManager;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.util.Patterns;
import com.fanphotographer.AppController;
import com.fanphotographer.data.constant.Constants;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class AppUtils {


    public static boolean isNetworkConnected() {

        ConnectivityManager connectivityManager = (ConnectivityManager) AppController.getInstance().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifi = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo mobile = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        NetworkInfo bluetooth = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_BLUETOOTH);
        NetworkInfo wimax = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIMAX);

        if (wifi == null && mobile == null && bluetooth == null && wimax == null) {
            return false;
        }

        if (wifi != null && wifi.isConnected()) {
            return true;
        }

        if (mobile != null && mobile.isConnected()) {
            return true;
        }

        if (bluetooth != null && bluetooth.isConnected()) {
            return true;
        }

        if (wimax != null && wimax.isConnected()) {
            return true;
        }

        return false;
    }



    public static int getResourceType(String result) {
        if (result != null && (result.contains("https://youtu.be") || result.contains("https://www.youtube.com"))) {
            return Constants.TYPE_YOUTUBE;
        } else if (result != null && Patterns.WEB_URL.matcher(result).matches()) {
            return Constants.TYPE_WEB;
        } else if (result != null && Patterns.PHONE.matcher(result).matches()) {
            return Constants.TYPE_PHONE;
        } else if (result != null && Patterns.EMAIL_ADDRESS.matcher(result).matches()) {
            return Constants.TYPE_EMAIL;
        } else {
            return Constants.TYPE_TEXT;
        }
    }


    public static boolean isServiceRunning(Context context, Class serviceClass) {
        try {
            if (context != null) {
                Log.d("", "contextIsNotNull: ");
            }
            ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            if (manager == null) {
                return false;
            }
            for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
                if (serviceClass.getName().equals(service.service.getClassName())) {
                    return true;
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }


   public static String getCurrentTime(){

       Calendar c = Calendar.getInstance();
       System.out.println("Current time => "+c.getTime());
       SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
       String current_time = df.format(c.getTime());
       return  current_time;
   }


    public static long printDifference(Date currentDate, Date previousDate, long time) {
        //milliseconds
        long remaningTime = time*1000;
        long different = currentDate.getTime() - previousDate.getTime();

        if(different>=remaningTime){

            return 0;
        }else {

            long finaldifference = remaningTime - different;
            return finaldifference;
        }


    }




}
