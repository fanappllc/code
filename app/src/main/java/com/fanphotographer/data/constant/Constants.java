package com.fanphotographer.data.constant;

import android.Manifest;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.util.Patterns;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.fanphotographer.R;
import com.fanphotographer.helper.CustomProgressDialog;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Constants {

    public static final int PICK_IMAGE_REQUEST = 1;
    public static final int CAMERA_REQUEST = 2;
    public static final String BASE_URL = "http://fan.codiant.com/api/";

    //Live
//    public static final String BASE_URL = "http://fanapp.us/api/";
    public static final String LOG_CAT = "FanPhotographer";
    public static final String APP_PREF_NAME = "fanphotographerNew";
    public static final String START_PHOTO = "Start_photo";
    public static final String ID = "id";
    public static final String FIRST_NAME = "first_name";
    public static final String LAST_NAME = "last_name";
    public static final String DAIL_CODE = "dial_code";
    public static final String EMAIL = "email";
    public static final String COUNTRY_CODE = "country_code";
    public static final String MOBILE = "mobile";
    public static final String MOBILE_MODEL = "mobile_model";
    public static final String ADDRESS = "address";
    public static final String ZIP_CODE = "zip_code";
    public static final String ROLE = "role";
    public static final String STATUS = "status";
    public static final String IS_AVAILABLE = "is_available";
    public static final String IS_APPROVED = "is_approved";
    public static final String PROFILE_IMAGE = "profile_image";
    public static final String DRIVING_LICENCE_IMAGE = "driving_licence_image";
    public static final String SSN_NO = "ssn_no";
    public static final String PASSWORD_TOKEN = "password_token";
    public static final String IS_TERMS_CONDITION_ACCEPTED = "is_terms_condition_accepted";
    public static final String IS_PROFILE_UPDATED = "is_profile_updated";
    public static final String IS_USER_ACCOUNTED_ADDED = "is_user_account_added";
    public static final String IS_REGISTRATION_FEE_PAID = "is_registration_fee_paid";
    public static final String IS_USER_REGISTERED = "is_user_registered";
    public static final String LATITUDE = "latitude";
    public static final String LONGITUDE = "longitude";
    public static final String CREATED_AT = "created_at";
    public static final String UPDATED_AT = "updated_at";
    public static final String CUSTOMER_ID = "customer_id";
    public static final String USERLOCATION_ACTIVITY = "UserLocationActivity";
    public static final String ORDER_ID = "order_id";
    public static final String IS_BUTTON = "is_button";
    public static final String ORDER_SLOT_ID = "order_slot_id";
    public static final String ACCESS_TOKEN = "access_token";
    public static final String IS_USER_INFORMATION_STATUS = "is_user_infromation_status";
    public static final String SEND_REQUEST = "send_request";
    public static final String REQUEST_PROCEED = "request_proceed";
    public static final String REQUEST_START_TIME = "start_time";
    public static final String REQUEST_CANCEL_SESSION_BY_CUSTOMER = "cancel_session_by_customer";
    public static final String REQUEST_CANCEL = "request_cancel";
    public static final String REQUEST_END_SESSION = "end_session";
    public static final String RENEW_SESSION_TIME = "renew_session_time";
    public static final String BROADCAST_ACTION = "Broadcast_Action";
    public static final String ACTION_REFRESH_USER = "action_refresh_user";
    public static final String ACTION_UPDATE_LOCATION = "action_update_location";
    public static final String BADGE_COUNT = "unread_notification_count";


    public static final String NOTIFICATION_ID = "notification_id";
    public static final String REQUEST = "REQUEST";
    public static final String SLOT_TIME = "slot_time";
    public static final String TIMER_KEY = "timer_key";
    public static final String IMAGE_DIRECTORY_NAME = "FanPhoto";
    public static final String NEW_SLOT_TIME = "new_slot_time";
    public static final String MTIME = "mtime";
    public static final String CUSTOMER_PROFILE_IMAGE = "customer_profile_image";
    public static final String TRUE = "true";
    public static final String SUCCESS = "success";
    public static final String FALSE = "false";
    public static final String MESSAGE = "message";
    public static final String ERROR = "error";
    public static final String LOADING = "Loading..";
    public static final String MULTIPART = "multipart/form-data";
    public static final String RETRY = "Retry";
    public static final String PAID = "Paid";
    public static final String CANCELLED = "cancelled";
    public static final String ARRIVING_TIME = "arriving_time";



    public static final String NEW_MESSAGE = "new_message";

    public static final String CustomerLATITUDE = "customer_latitude";
    public static final String CustomerLONGITUDE = "customer_longitude";

    public static final String[] MEDIA_PERMISSION = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
    };


    public static final int TYPE_WEB = 1;
    public static final int TYPE_YOUTUBE = 2;
    public static final int TYPE_PHONE = 3;
    public static final int TYPE_EMAIL = 4;
    public static final int TYPE_TEXT = 5;
    private static CustomProgressDialog pDialog;
    private static Snackbar snackbar;

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
            Log.e(Constants.LOG_CAT,e.getMessage());
        }
    }

    public static boolean isValidEmail(String target) {

        if (target == null) {
            return false;

        } else {
            return Patterns.EMAIL_ADDRESS.matcher(target).matches();

        }

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
            Log.e(Constants.LOG_CAT,e.getMessage());
            capStr = "";
        }
        return capStr;
    }
    public static boolean isStringNullOrBlank(String str) {

        try {

            if (str == null) {
                return true;
            } else if (str.equals("null") || str.equals("") || (str != null && str.isEmpty()) || (str != null && str.length() <= 0) || str.equalsIgnoreCase("null")) {
                return true;
            }

        } catch (Exception e) {
            Log.e(Constants.LOG_CAT,e.getMessage());
        }
        return false;
    }

    public static void showSnackbar(final Context context, View view, String text1, String text2) {

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
                snackbar.setActionTextColor(context.getResources().getColor(R.color.yellow));
                // Changing action button text color
                View sbView = snackbar.getView();
                TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);

                textView.setTextColor(Color.WHITE);
            }
            snackbar.show();
        } catch (Exception e) {
            Log.e(Constants.LOG_CAT,e.getMessage());
        }

    }

    public static void hideSnackbar() {
        try {
            if (snackbar != null && snackbar.isShown()) {
                snackbar.dismiss();
            }
        } catch (Exception e) {
            Log.e(Constants.LOG_CAT,e.getMessage());
        }

    }

    private static void onSnackbarAction() {
       // onSnackbarAction
    }

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
            Log.e(Constants.LOG_CAT,e.getMessage());
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
            Log.e(Constants.LOG_CAT,e.getMessage());
        }
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


}
