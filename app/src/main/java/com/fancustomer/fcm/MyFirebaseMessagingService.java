package com.fancustomer.fcm;

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

import com.fancustomer.R;
import com.fancustomer.activity.MenuScreen;
import com.fancustomer.data.constant.Constants;
import com.fancustomer.data.preference.AppPreference;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONObject;

import java.util.List;
import java.util.Map;

import me.leolin.shortcutbadger.ShortcutBadger;


public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";
    String unReadTotal;


    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        Log.e(TAG, "onMessageReceived: " + remoteMessage.toString());

        Map<String, String> params = remoteMessage.getData();

        JSONObject object = new JSONObject(params);
        Log.e(TAG, "onMessageReceived: " + remoteMessage.getData().toString());
        if (!AppPreference.getInstance(MyFirebaseMessagingService.this).getString(Constants.ACCESS_TOKEN).equals("")) {
            fanNotification(params);
        }
    }

    private void fanNotification(Map<String, String> message) {
        String notificationTypeStr = "";

        if (!Constants.isStringNullOrBlank(message.get("unread_notification_count"))) {
            unReadTotal = message.get("unread_notification_count");
        } else {
            unReadTotal = "0";
        }

        try {
            ShortcutBadger.applyCount(MyFirebaseMessagingService.this, Integer.parseInt(unReadTotal));
            AppPreference.getInstance(MyFirebaseMessagingService.this).setString(Constants.BADGE_COUNT, unReadTotal);
        } catch (Exception e) {
            e.printStackTrace();
        }


        notificationTypeStr = message.get("type");
        Log.e(TAG, "Notification  message" + message);
        j = (int) (System.currentTimeMillis() & 0xfffffff);

        if (isAppIsInBackground(getApplicationContext())) {

            if (notificationTypeStr.equals(Constants.REQUEST_ACCEPTED)) {
                Intent request_accept_intent = new Intent("ACTION_REFRESH_USER.intent.MAIN");
                request_accept_intent.putExtra(Constants.BROADCAST_ACTION, Constants.ACTION_REFRESH_USER);
                request_accept_intent.putExtra("REQUEST_ACCEPTED", Constants.REQUEST_ACCEPTED);
                request_accept_intent.putExtra("notification_id", "" + j);
                request_accept_intent.putExtra("photographer_id", message.get("photographer_id"));
                request_accept_intent.putExtra("order_id", message.get("order_id"));
                sendBroadcast(request_accept_intent);
                Log.e(Constants.LOG_CAT, "fanNotification: " + "log noti noti");
                // notification(message, Constants.REQUEST_ACCEPTED);
            } else if (notificationTypeStr.equals(Constants.CANCEL_SESSION_BY_PHOTOGRAPHER)) {
                AppPreference.getInstance(MyFirebaseMessagingService.this).setString(Constants.TRACK_START, "0");
                AppPreference.getInstance(MyFirebaseMessagingService.this).setString(Constants.START_PHOTO, "0");
                notification(message, Constants.CANCEL_SESSION_BY_PHOTOGRAPHER);
            } else if (notificationTypeStr.equals(Constants.NEW_MESSAGE)) {
                notification(message, Constants.NEW_MESSAGE);
            } else if (notificationTypeStr.equals(Constants.START_TIME_REQUEST_APPROVE)) {
                Intent requestAcceptIntent = new Intent("ACTION_REFRESH_USER.intent.MAIN");
                requestAcceptIntent.putExtra(Constants.BROADCAST_ACTION, Constants.ACTION_REFRESH_USER);
                requestAcceptIntent.putExtra("notification_id", "" + j);
                requestAcceptIntent.putExtra("START_TIME_REQUEST_APPROVE", Constants.START_TIME_REQUEST_APPROVE);
                sendBroadcast(requestAcceptIntent);
                notification(message, Constants.START_TIME_REQUEST_APPROVE);
            }
        } else {
            if (notificationTypeStr.equals(Constants.REQUEST_ACCEPTED)) {
                Intent request_accept_intent = new Intent("ACTION_REFRESH_USER.intent.MAIN");
                request_accept_intent.putExtra(Constants.BROADCAST_ACTION, Constants.ACTION_REFRESH_USER);
                request_accept_intent.putExtra("REQUEST_ACCEPTED", Constants.REQUEST_ACCEPTED);
                request_accept_intent.putExtra("notification_id", "" + j);
                request_accept_intent.putExtra("photographer_id", message.get("photographer_id"));
                request_accept_intent.putExtra("order_id", message.get("order_id"));
                sendBroadcast(request_accept_intent);
                notification(message, Constants.REQUEST_ACCEPTED);
            } else if (notificationTypeStr.equals(Constants.CANCEL_SESSION_BY_PHOTOGRAPHER)) {
                AppPreference.getInstance(MyFirebaseMessagingService.this).setString(Constants.TRACK_START, "0");
                AppPreference.getInstance(MyFirebaseMessagingService.this).setString(Constants.START_PHOTO, "0");
                Intent request_accept_intent = new Intent("ACTION_REFRESH_USER.intent.MAIN");
                request_accept_intent.putExtra(Constants.BROADCAST_ACTION, Constants.ACTION_REFRESH_USER);
                request_accept_intent.putExtra("notification_id", "" + j);
                request_accept_intent.putExtra("CANCEL_SESSION_BY_PHOTOGRAPHER", Constants.CANCEL_SESSION_BY_PHOTOGRAPHER);
                sendBroadcast(request_accept_intent);
                notification(message, Constants.CANCEL_SESSION_BY_PHOTOGRAPHER);
            } else if (notificationTypeStr.equals(Constants.START_TIME_REQUEST_APPROVE)) {
                Intent request_accept_intent = new Intent("ACTION_REFRESH_USER.intent.MAIN");
                request_accept_intent.putExtra(Constants.BROADCAST_ACTION, Constants.ACTION_REFRESH_USER);
                request_accept_intent.putExtra("notification_id", "" + j);
                request_accept_intent.putExtra("START_TIME_REQUEST_APPROVE", Constants.START_TIME_REQUEST_APPROVE);
                sendBroadcast(request_accept_intent);
                notification(message, Constants.START_TIME_REQUEST_APPROVE);
            } else if (notificationTypeStr.equals(Constants.NEW_MESSAGE)) {
                ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
                ComponentName cn = am.getRunningTasks(1).get(0).topActivity;
                System.out.println("=======" + cn.getClassName());
                if (cn.getClassName().equals("com.fancustomer.activity.ChatActivity")) {
                    Intent sellList_intent = new Intent("ACTION_REFRESH_USER.intent.MAIN");
                    sellList_intent.putExtra(Constants.BROADCAST_ACTION, Constants.ACTION_REFRESH_USER);
                    sellList_intent.putExtra("from_id", message.get("from_id"));
                    sellList_intent.putExtra("to_id", message.get("to_id"));
                    sellList_intent.putExtra("message", message.get("message"));
                    sellList_intent.putExtra("order_id", message.get("order_id"));
                    sellList_intent.putExtra("created_at", message.get("created_at"));
                    sellList_intent.putExtra("notification_id", "" + j);
                    sellList_intent.putExtra("unread_thread_count", message.get("unread_thread_count"));
                    sellList_intent.putExtra("from_user_profile_image", message.get("from_user_profile_image"));
                    sellList_intent.putExtra("from_user_name", message.get("from_user_name"));
                    sellList_intent.putExtra("NEW_MESSAGE", Constants.NEW_MESSAGE);
                    sendBroadcast(sellList_intent);
                } else {
                    //   notification(message, Constants.NEW_MESSAGE);
                }

            }


        }
    }

    private int getNotificationIcon() {
        boolean useWhiteIcon = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP);
        return useWhiteIcon ? R.mipmap.ic_launcher_round : R.mipmap.app_icon;
    }

    private static int j = 0;

    private void notification(Map<String, String> message, String navigationType) {

        try {

            Intent intent = null;
            String message1 = message.get("message");

            intent = new Intent(this, MenuScreen.class);
            intent.putExtra("NAVIGATE_SCREEN", navigationType);
            intent.putExtra("notification_id", "" + j);
            intent.putExtra("photographer_id", message.get("photographer_id"));
            intent.putExtra("order_id", message.get("order_id"));
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
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
            Log.e(TAG, "notification: " + navigationType);

            Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(getApplicationContext())
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
            e.printStackTrace();
        }
    }


    private boolean isAppIsInBackground(Context context) {
        boolean isInBackground = true;
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT_WATCH) {
            List<ActivityManager.RunningAppProcessInfo> runningProcesses = am.getRunningAppProcesses();
            for (ActivityManager.RunningAppProcessInfo processInfo : runningProcesses) {
                if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                    for (String activeProcess : processInfo.pkgList) {
                        if (activeProcess.equals(context.getPackageName())) {
                            isInBackground = false;
                        }
                    }
                }
            }
        } else {
            List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);
            ComponentName componentInfo = taskInfo.get(0).topActivity;
            if (componentInfo.getPackageName().equals(context.getPackageName())) {
                isInBackground = false;
            }
        }

        return isInBackground;
    }

    public static void clearNotification(Context ctx, int id) {
        try {
            NotificationManager notificationManager =
                    (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(id);
//            ShortcutBadger.removeCount(ctx);
        } catch (Exception e) {
            Log.e(TAG, "onMessageReceived: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void clearNotificationAll(Context ctx) {
        try {
            // MyFirebaseMessagingService.badgeNotificationCount = 0;
            //  MyFirebaseMessagingService.notificationCount = 0;
            NotificationManager notificationManager =
                    (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancelAll();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
//    public static boolean isAppOnForeground(Context context) {
//        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
//        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
//        if (appProcesses == null) {
//            return false;
//        }
//        final String packageName = context.getPackageName();
//        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
//            if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND && appProcess.processName.equals(packageName)) {
//                return true;
//            }
//        }
//        return false;
//    }
}