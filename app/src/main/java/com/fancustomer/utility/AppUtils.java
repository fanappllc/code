package com.fancustomer.utility;

import android.Manifest;
import android.app.Activity;
import android.app.SearchManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Vibrator;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.util.Patterns;
import android.widget.Toast;

import com.fancustomer.data.constant.Constants;
import com.fancustomer.helper.ExceptionHandler;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


/**
 * Created by Ashiq on 4/12/2017.
 */

public class AppUtils {

    private static long backPressed = 0;

    public static void tapToExit(Activity activity) {
        if (backPressed + 2500 > System.currentTimeMillis()) {
            activity.finish();
        } else {
            Log.e(Constants.LOG_CAT, "tapToExit: ");
        }
        backPressed = System.currentTimeMillis();
    }

    public static void showToast(Context context, String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE));
        return connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected();
    }

    public static void share(Activity activity, String text) {
        try {
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, text);
            sendIntent.setType("text/plain");
            activity.startActivity(sendIntent);
        } catch (Exception e) {
            ExceptionHandler.printStackTrace(e);
        }
    }


    public static void rateThisApp(Activity activity) {
        try {
            activity.startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://play.google.com/store/apps/details?id=" + activity.getPackageName())));
        } catch (Exception e) {
            ExceptionHandler.printStackTrace(e);
        }
    }


    @SuppressWarnings("deprecation")
    public static Spanned fromHtml(String html) {
        Spanned result;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            result = Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY);
        } else {
            result = Html.fromHtml(html);
        }
        return result;
    }

    public static void vibrateDevice(Context context) {
        Vibrator v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        // Vibrate for 500 milliseconds
        v.vibrate(500);
    }


    public static void copyToClipboard(Context context, String text) {
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("label", text);
        clipboard.setPrimaryClip(clip);
    }

    public static void searchInWeb(Activity activity, String text) {
        Intent intent = new Intent(Intent.ACTION_WEB_SEARCH);
        intent.putExtra(SearchManager.QUERY, text);
        activity.startActivity(intent);
    }

    public static void executeAction(Activity activity, String result, int type) {
        if (type == Constants.TYPE_WEB || type == Constants.TYPE_YOUTUBE) {
            activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(result)));
        } else if (type == Constants.TYPE_PHONE) {
            if ((ContextCompat.checkSelfPermission(activity, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED)) {
                activity.startActivity(new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", result, null)));
            }
        } else if (type == Constants.TYPE_EMAIL) {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_EMAIL, new String[]{result});
            activity.startActivity(Intent.createChooser(intent, "Send Email"));
        }
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


    public static String getCurrentTime() {

        Calendar c = Calendar.getInstance();
        System.out.println("Current time => " + c.getTime());
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String current_time = df.format(c.getTime());
        return current_time;
    }


    public static long printDifference(Date currentDate, Date previousDate, long time) {
        //milliseconds
        long remaningTime = time * 1000;
        long different = currentDate.getTime() - previousDate.getTime();

        if (different >= remaningTime) {

            return 0;
        } else {

            long finaldifference = remaningTime - different;
            return finaldifference;
        }


    }


}
