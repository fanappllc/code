package com.fanphotographer.fcm;

import android.app.ActivityManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import com.fanphotographer.R;
import com.fanphotographer.activity.MenuScreen;
import com.fanphotographer.data.constant.Constants;
import com.fanphotographer.data.preference.AppPreference;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import org.json.JSONObject;
import java.util.List;
import java.util.Map;
import me.leolin.shortcutbadger.ShortcutBadger;


public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";
    private static int badgeNotificationCount;
    private static int notificationCount;
    private static int j = 0;
    private String unReadTotal;
    private AppPreference appPreference;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        Log.e(TAG, "onMessageReceived: " + remoteMessage.toString());
        Map<String, String> params = remoteMessage.getData();
        JSONObject object = new JSONObject(params);
        appPreference = AppPreference.getInstance(MyFirebaseMessagingService.this);
        if (!appPreference.getString(Constants.ACCESS_TOKEN).equals("")) {
            fanNotification(object);
        }

    }

    private void fanNotification(JSONObject jsonObject ) {
               String notificationTypeStr = "";
        notificationTypeStr = jsonObject.optString("type");
        Log.e(TAG, "Notification  message" + notificationTypeStr);
        j = (int) (System.currentTimeMillis() & 0xfffffff);

        try {
        if (!Constants.isStringNullOrBlank(jsonObject.optString("unread_notification_count"))) {
            unReadTotal = jsonObject.optString("unread_notification_count");
        } else {
            unReadTotal = "0";
        }
            ShortcutBadger.applyCount(MyFirebaseMessagingService.this, Integer.parseInt(unReadTotal));
            AppPreference.getInstance(MyFirebaseMessagingService.this).setString(Constants.BADGE_COUNT, unReadTotal);
        } catch (Exception e) {
            Log.e(Constants.LOG_CAT,e.getMessage());
        }


        if (!isAppOnForeground(getApplicationContext())) {

            if (notificationTypeStr.equals(Constants.SEND_REQUEST)) {
                notification(jsonObject, Constants.SEND_REQUEST);
                Intent requestAcceptintent = sendIntent(Constants.SEND_REQUEST,jsonObject,false,false,false);
                sendBroadcast(requestAcceptintent);
            }else if(notificationTypeStr.equals(Constants.REQUEST_PROCEED)){
                appPreference.setString(Constants.REQUEST_PROCEED, jsonObject.toString());
                notification(jsonObject, Constants.REQUEST_PROCEED);
                Intent requestAcceptintent = sendIntent(Constants.REQUEST_PROCEED,jsonObject,false,false,true);
                sendBroadcast(requestAcceptintent);
            }else if(notificationTypeStr.equals(Constants.REQUEST_START_TIME)){
                appPreference.setString(Constants.REQUEST_START_TIME, jsonObject.toString());
                notification(jsonObject, Constants.REQUEST_START_TIME);
                Intent requestAcceptintent = sendIntent(Constants.REQUEST_START_TIME,jsonObject,false,true,false);
                sendBroadcast(requestAcceptintent);
            }else if(notificationTypeStr.equals(Constants.REQUEST_CANCEL_SESSION_BY_CUSTOMER)){
                appPreference.setString(Constants.REQUEST_START_TIME, jsonObject.toString());
                notification(jsonObject, Constants.REQUEST_CANCEL_SESSION_BY_CUSTOMER);
            }else if(notificationTypeStr.equals(Constants.REQUEST_CANCEL)){
                notification(jsonObject, Constants.REQUEST_CANCEL);
                Intent requestAcceptintent = sendIntent(Constants.REQUEST_CANCEL,jsonObject,false,false,false);
                sendBroadcast(requestAcceptintent);
            }else if(notificationTypeStr.equals(Constants.REQUEST_END_SESSION)){
                appPreference.setString(Constants.REQUEST_END_SESSION, jsonObject.toString());
                notification(jsonObject, Constants.REQUEST_END_SESSION);
                Intent requestAcceptintent = sendIntent(Constants.REQUEST_END_SESSION,jsonObject,true,false,false);
                sendBroadcast(requestAcceptintent);
            }else if(notificationTypeStr.equals(Constants.RENEW_SESSION_TIME)){
                notification(jsonObject, Constants.REQUEST_END_SESSION);
                Intent requestAcceptintent = sendIntent(Constants.RENEW_SESSION_TIME,jsonObject,false,false,false);
                sendBroadcast(requestAcceptintent);
            }else if (notificationTypeStr.equals(Constants.NEW_MESSAGE)) {
                notification(jsonObject, Constants.NEW_MESSAGE);
            }

        } else {
            if (notificationTypeStr.equals(Constants.SEND_REQUEST)) {
                notification(jsonObject, Constants.SEND_REQUEST);
                Intent requestAcceptintent = sendIntent(Constants.SEND_REQUEST,jsonObject,false,false,false);
                sendBroadcast(requestAcceptintent);
            }else if(notificationTypeStr.equals(Constants.REQUEST_PROCEED)){
                appPreference.setString(Constants.REQUEST_PROCEED, jsonObject.toString());
                Intent requestAcceptintent = sendIntent(Constants.REQUEST_PROCEED,jsonObject,false,false,true);
                sendBroadcast(requestAcceptintent);
            }else if(notificationTypeStr.equals(Constants.REQUEST_START_TIME)){
                Intent requestAcceptintent = sendIntent(Constants.REQUEST_START_TIME,jsonObject,false,true,false);
                appPreference.setString(Constants.REQUEST_START_TIME, jsonObject.toString());
                sendBroadcast(requestAcceptintent);
            }else if(notificationTypeStr.equals(Constants.REQUEST_CANCEL_SESSION_BY_CUSTOMER)){
                Intent requestAcceptintent = sendIntent(Constants.REQUEST_CANCEL_SESSION_BY_CUSTOMER,jsonObject,false,false,false);
                appPreference.setString(Constants.REQUEST_START_TIME, jsonObject.toString());
                sendBroadcast(requestAcceptintent);
            }else if(notificationTypeStr.equals(Constants.REQUEST_CANCEL)){
                Intent requestAcceptintent = sendIntent(Constants.REQUEST_CANCEL,jsonObject,false,false,false);
                sendBroadcast(requestAcceptintent);
            }else if(notificationTypeStr.equals(Constants.REQUEST_END_SESSION)){
                Intent requestAcceptintent = sendIntent(Constants.REQUEST_END_SESSION,jsonObject,true,false,false);
                appPreference.setString(Constants.REQUEST_END_SESSION, jsonObject.toString());
                sendBroadcast(requestAcceptintent);
            }else if(notificationTypeStr.equals(Constants.RENEW_SESSION_TIME)){
                Intent requestAcceptintent = sendIntent(Constants.RENEW_SESSION_TIME,jsonObject,false,false,false);
                sendBroadcast(requestAcceptintent);
            } else if (notificationTypeStr.equals(Constants.NEW_MESSAGE)) {
                ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
                ComponentName cn = am.getRunningTasks(1).get(0).topActivity;
                if (cn.getClassName().equals("com.fanphotographer.activity.ChatActivity")) {
                    Intent sellListIntent = new Intent("ACTION_REFRESH_USER.intent.MAIN");
                    sellListIntent.putExtra(Constants.BROADCAST_ACTION, Constants.ACTION_REFRESH_USER);
                    sellListIntent.putExtra("REQUEST", Constants.NEW_MESSAGE);
                    sellListIntent.putExtra("from_id", jsonObject.optString("from_id"));
                    sellListIntent.putExtra("to_id", jsonObject.optString("to_id"));
                    sellListIntent.putExtra("message", jsonObject.optString("message"));
                    sellListIntent.putExtra("order_id", jsonObject.optString("order_id"));
                    sellListIntent.putExtra("created_at", jsonObject.optString("created_at"));
                    sellListIntent.putExtra("notification_id", "" + j);
                    sellListIntent.putExtra("unread_thread_count", jsonObject.optString("unread_thread_count"));
                    sellListIntent.putExtra("from_user_profile_image", jsonObject.optString("from_user_profile_image"));
                    sellListIntent.putExtra("from_user_name", jsonObject.optString("from_user_name"));
                    sellListIntent.putExtra("NEW_MESSAGE", Constants.NEW_MESSAGE);
                    sendBroadcast(sellListIntent);
                }

            }
        }
    }



    private int getNotificationIcon() {
        boolean useWhiteIcon = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP);
        return useWhiteIcon ? R.mipmap.ic_launcher_round : R.mipmap.app_icon;
    }



    private void notification(JSONObject mjsonObject, String navigationType) {

        try {

            Intent  intent = new Intent(this, MenuScreen.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra("notification_id",""+j);
            intent.putExtra("NAVIGATE_SCREEN", navigationType);
            intent.putExtra("order_slot_id", mjsonObject.optString("order_slot_id"));
            intent.putExtra("order_id", mjsonObject.optString("order_id"));
            intent.putExtra("slot_time", mjsonObject.optString("slot_time"));

            if(navigationType.equals(Constants.REQUEST_END_SESSION)){
                intent.putExtra("jsonObject", mjsonObject.toString());
            }else if (navigationType.equals(Constants.REQUEST_START_TIME)){
                intent.putExtra("customer_profile_image", mjsonObject.optString("customer_profile_image"));
            }

            String CHANNEL_ID = "my_channel_01";// The id of the channel.
            CharSequence name = getString(R.string.app_name);// The user-visible name of the channel.
            int importance = NotificationManager.IMPORTANCE_HIGH;
            PendingIntent pendingIntent = PendingIntent.getActivity(this, j, intent, 0);
            Log.e(TAG, "notification: " + navigationType);
            NotificationChannel mChannel = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                mChannel = new NotificationChannel(CHANNEL_ID, name, importance);
                mChannel.enableLights(true);
                // Sets the notification light color for notifications posted to this
                // channel, if the device supports this feature.
                mChannel.setLightColor(Color.RED);
            }

//            PendingIntent pendingIntent = PendingIntent.getActivity(this, j, intent, 0);
            String message1 =  mjsonObject.optString("message");
            Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this,CHANNEL_ID)
                    .setSmallIcon(getNotificationIcon())
                    .setContentTitle(getResources().getString(R.string.fan))
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(message1))
                    .setContentText(message1)
                    .setAutoCancel(true)
                    .setSound(defaultSoundUri)
                    .setContentIntent(pendingIntent);

            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                assert notificationManager != null;
                notificationManager.createNotificationChannel(mChannel);
            }
            notificationManager.notify(j, notificationBuilder.build());

        } catch (Exception e) {

            Log.e(Constants.LOG_CAT,e.getMessage());
        }
    }



    public static boolean isAppOnForeground(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
        if (appProcesses == null) {
            return false;
        }
        final String packageName = context.getPackageName();
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND && appProcess.processName.equals(packageName)) {
                return true;
            }
        }
        return false;
    }

    public static void clearNotification(Context ctx,int id){
        try {
            NotificationManager notificationManager = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(id);
        }catch (Exception e){
            Log.e(Constants.LOG_CAT,e.getMessage());
        }
    }

    public static void clearNotificationAll(Context ctx){
        try {
            MyFirebaseMessagingService.badgeNotificationCount = 0;
            MyFirebaseMessagingService.notificationCount = 0;
            NotificationManager notificationManager =
                    (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancelAll();
        }catch (Exception e){
            Log.e(Constants.LOG_CAT,e.getMessage());
        }
    }


    public Intent sendIntent(String action,JSONObject mobj,boolean bJson,boolean cImage,boolean locating){
        Intent nIntent = new Intent("ACTION_REFRESH_USER.intent.MAIN");
        nIntent.putExtra(Constants.BROADCAST_ACTION, Constants.ACTION_REFRESH_USER);
        nIntent.putExtra("REQUEST", action);
        nIntent.putExtra("order_id",mobj.optString("order_id"));
        nIntent.putExtra("order_slot_id", mobj.optString("order_slot_id"));
        nIntent.putExtra("slot_time", mobj.optString("slot_time"));
        nIntent.putExtra("notification_id",""+j);
        if(bJson){
            nIntent.putExtra("jsonObject", mobj.toString());
        }
        if(cImage){
            nIntent.putExtra("customer_profile_image", mobj.optString("customer_profile_image"));
        }
        if(locating){
            nIntent.putExtra("customer_latitude", mobj.optString("customer_latitude"));
            nIntent.putExtra("customer_longitude", mobj.optString("customer_longitude"));

        }

        return nIntent;
    }
}