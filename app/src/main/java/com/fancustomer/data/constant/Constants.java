package com.fancustomer.data.constant;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.Dialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.util.Patterns;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import com.fancustomer.R;
import com.fancustomer.activity.EnterMobileActivity;
import com.fancustomer.data.preference.AppPreference;
import com.fancustomer.helper.CustomProgressDialog;
import com.fancustomer.helper.ExceptionHandler;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Constants {

    static Snackbar snackbar;
    public static final String LOG_CAT = "FanCustomer";

    public static final int PERMISSION_REQ = 445;
    public static final String ACCESS_TOKEN = "access_token";
    public static final String CUSTOMER_ID = "customer_id";
    public static final String APP_NAME = "FanCustomer";
    public static final int CAMERA_REQUEST = 2;
    public static final int PICK_IMAGE_REQUEST = 1;
    /*CODIANT*/
    // public static final String BASE_URL = "http://fan.codiant.com/api/";

    /*LIVE*/
    public static final String BASE_URL = "http://fanapp.us/api/";
    /**/
    public static int TYPE_WEB = 1;
    public static int TYPE_YOUTUBE = 2;
    public static int TYPE_PHONE = 3;
    public static int TYPE_EMAIL = 4;
    public static int TYPE_TEXT = 5;
    public static final String USER_ROLE = "role";
    public static final String FIRST_NAME = "first_name";
    public static final String EMAIL = "email";
    public static final String LAST_NAME = "last_name";
    public static final String PROFILE_PIC = "profile_image";
    public static final String MOBILE = "mobile";
    public static final String APP_PREFERENCE_NAME = "FanApp";
    public static final String BADGE_COUNT = "unread_notification_count";
    public static final String USER_ID = "user_id";
    public static final String IS_USER_REGISTERED = "is_user_registered";
    public static final String IS_TERMS_CONDITION_ACCEPTED = "is_terms_condition_accepted";
    public static final String IS_PROFILE_UPDATE = "is_profile_updated";
    public static final String IS_USER_ACCOUNT_ADDED = "is_user_account_added";
    public static final String TRACK_START = "track_start";
    public static final String START_PHOTO = "Start_photo";
    public static final String REQUEST_ACCEPTED = "request_accepted";
    public static final String CANCEL_SESSION_BY_PHOTOGRAPHER = "cancel_session_by_photographer";

    public static final String START_TIME_REQUEST_APPROVE = "start_time_request_approve";

    public static final String NEW_MESSAGE = "new_message";

    public static final String LATITUDE = "latitude";
    public static final String LONGITUDE = "longitude";

    public static final String BROADCAST_ACTION = "Broadcast_Action";
    public static final String BROADCAST_ACTION_ACTIVTY = "Broadcast_Action_Activity";
    public static final String ACTION_REFRESH_USER = "action_refresh_user";


    public static final String Visa = "Visa";
    public static final String American_Express = "American Express";
    public static final String American_E = "American E";
    public static final String MasterCard = "MasterCard";
    public static final String Master_Card = "Master Card";
    public static final String Discover = "Discover";
    public static final String JCB = "JCB";
    public static final String Diners_Club = "Diners Club";
    public static final String Unknown = "Unknown";

    public static final String RETRY = "Retry";
    public static final String ERROR = "error";
    public static final String MESSAGE = "message";
    public static final String LOADING = "Loading...";
    public static final String SUCCESS = "success";
    public static final String TRUE = "true";
    public static final String FALSE = "false";
    public static final String HEADDER = "Headder";
    public static final String CANCEL = "cancel";
    public static final String ACTIVE = "active";
    public static final String END = "end";



   /*String is_user_registered = jsonObject.optString("is_user_registered");
    String is_terms_condition_accepted = jsonObject.optString("is_terms_condition_accepted");
    String is_profile_updated = jsonObject.optString("is_profile_updated");
    String is_user_account_added = jsonObject.optString("is_user_account_added");*/


    public static final String STRIPE_BASE_URL = "https://api.stripe.com/v1/";
    public static final String STRIPE_SECRET_KEY = "secret_key";

    public static final String STRIPE_PUBLISH_KEY = "stripe_key";

    public static final String[] LOCATION_PERMISSION = {
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,


    };

    public static final String[] MEDIA_PERMISSION = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
    };
    // copy, search in web, share, action (visit, youtube, email and phone)

    public static boolean isStringNullOrBlank(String str) {

        try {

            if (str == null) {
                return true;
            } else if (str.equals("null") || str.equals("") || (str != null && str.isEmpty()) || (str != null && str.length() <= 0) || str.equalsIgnoreCase("null")) {
                return true;
            }

        } catch (Exception e) {
            ExceptionHandler.printStackTrace(e);

        }
        return false;
    }

    public static String wordFirstCap(String str) {
        String capStr = "";
        try {
            String[] words = str.trim().split(" ");
            StringBuilder ret = new StringBuilder();
            for (int i = 0; i < words.length; i++) {
                if (words[i].trim().length() > 0) {
                    Log.e("words[i].trim", "" + words[i].trim().charAt(0));
                    ret.append(Character.toUpperCase(words[i].trim().charAt(0)));
                    ret.append(words[i].trim().substring(1));
                    if (i < words.length - 1) {
                        ret.append(' ');
                    }
                }
            }
            capStr = ret.toString();

        } catch (Exception e) {
            ExceptionHandler.printStackTrace(e);
            capStr = "";
        }
        return capStr;
    }

    public static boolean isInternetOn(Context context) {
        try {
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();

            if (activeNetwork != null) {
                Log.v("Internet ", "GREATER THAN OR EQUAL TO LOLLIPOP" + activeNetwork.isConnectedOrConnecting());
                return activeNetwork.isConnectedOrConnecting();
            } else {
                return false;
            }
        } catch (Exception e) {
            ExceptionHandler.printStackTrace(e);
        }
        return false;

    }


    public static String getDateInFormat(String dateInput, String dateOutput, String dateString) {
        String result = "";
        DateFormat formatComingFromServer = new SimpleDateFormat(dateInput);
        DateFormat formatRequired = new SimpleDateFormat(dateOutput);

        try {
            Log.v(Constants.LOG_CAT, "COMING DATE : " + dateString);
            Date dateN = formatComingFromServer.parse(dateString);
            result = formatRequired.format(dateN);

            if (result.contains("a.m.")) {
                result = result.replace("a.m.", "AM");
            } else if (result.contains("p.m.")) {
                result = result.replace("p.m.", "PM");
            } else if (result.contains("am")) {
                result = result.replace("am", "AM");
            } else if (result.contains("pm")) {
                result = result.replace("pm", "PM");
            }

            Log.v(Constants.LOG_CAT, "! RETURNING PARSED DATE : " + result);
        } catch (Exception e) {
            Log.v(Constants.LOG_CAT, "Some Exception while parsing the date : " + e.toString());
        }
        return result;
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
        } catch (Exception e) {
            ExceptionHandler.printStackTrace(e);
        }
        return false;
    }

    public static void showToastAlert(String message, Context context) {
        try {
            Toast toast = Toast.makeText(context, message, Toast.LENGTH_LONG);
            View toastView = toast.getView();
            TextView toastMessage = (TextView) toastView.findViewById(android.R.id.message);
            toastMessage.setTextColor(Color.WHITE);
            toastMessage.setGravity(Gravity.CENTER);

            Typeface font = Typeface.createFromAsset(context.getAssets(), "fonts/Quicksand-Regular.ttf");
            toastMessage.setTypeface(font);

            toast.getView().setBackgroundResource(R.drawable.custom_toast);
            toast.show();
        } catch (Exception e) {
            ExceptionHandler.printStackTrace(e);
        }
    }

    public final static boolean isValidEmail(String target) {

        if (target == null) {

            return false;

        } else {

            return Patterns.EMAIL_ADDRESS.matcher(target).matches();

        }

    }

    public static void showSessionExpireAlert(final Context context) {
        final SharedPreferences preferences = context.getSharedPreferences(Constants.APP_NAME, context.MODE_PRIVATE);
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.dialog_one_button);

        TextView textView = dialog.findViewById(R.id.tv_header);
        TextView tvMessages = dialog.findViewById(R.id.tv_messages);
        TextView buttonOk = dialog.findViewById(R.id.button_ok);

        tvMessages.setText(context.getResources().getString(R.string.session_expired_msg));

        buttonOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();

                NotificationManager nMgr = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                nMgr.cancelAll();
                SharedPreferences.Editor editor = preferences.edit();
                editor.clear();
                editor.apply();
                AppPreference.getInstance(context).clearPref();
                Intent intent = new Intent(context, EnterMobileActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                context.startActivity(intent);
                ((Activity) context).overridePendingTransition(R.anim.right_slide_in, R.anim.right_slide_out);
                ((Activity) context).finish();
            }
        });
        dialog.show();

    }


    public static CustomProgressDialog pDialog;


    public static void showProgressDialog(Context context, String loadingText) {
        try {
            if (pDialog != null && pDialog.isShowing()) {
                return;
            }

            pDialog = CustomProgressDialog.show(context, false, loadingText);
            if (pDialog != null) {
                if (!pDialog.isShowing())
                    pDialog.show();
            }
        } catch (Exception e) {
            ExceptionHandler.printStackTrace(e);
        }

    }

    public static void hideProgressDialog() {
        try {
            if (pDialog != null) {
                if (pDialog.isShowing()) {
                    pDialog.hide();
                }

                if (pDialog.isShowing()) {
                    pDialog.dismiss();
                }
            }
        } catch (Exception e) {
            ExceptionHandler.printStackTrace(e);
        }

    }

}
