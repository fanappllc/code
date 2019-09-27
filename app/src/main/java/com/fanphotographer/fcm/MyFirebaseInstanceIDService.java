package com.fanphotographer.fcm;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import com.fanphotographer.AppController;
import com.fanphotographer.data.constant.Constants;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService {


    @Override
    public void onTokenRefresh() {
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        saveToken(refreshedToken);
        Log.d(Constants.LOG_CAT, "Refreshed token: " + refreshedToken);
        sendRegistrationToServer(refreshedToken);
    }

    private void sendRegistrationToServer(String token) {
        Log.e(Constants.LOG_CAT,"sendRegistrationToServer"+token);
    }

    public static void saveToken(String token){

        try{
            SharedPreferences sharedPreferences = AppController.getInstance().getSharedPreferences("gcm_token", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("token",token);
            editor.commit();

        }catch (Exception e){
            Log.e(Constants.LOG_CAT,e.getMessage());
        }
    }


    public static String getCustomToken(){

        try{

            SharedPreferences sharedPreferences = AppController.getInstance().getSharedPreferences("gcm_token", Context.MODE_PRIVATE);
            return sharedPreferences.getString("token","");

        }catch (Exception e){
            Log.e(Constants.LOG_CAT,e.getMessage());
        }
        return "";
    }
}