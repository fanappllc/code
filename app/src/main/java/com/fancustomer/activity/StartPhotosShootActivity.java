package com.fancustomer.activity;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.fancustomer.R;
import com.fancustomer.adapter.ReniewAdapter;
import com.fancustomer.bean.ReniewBean;
import com.fancustomer.data.constant.Constants;
import com.fancustomer.data.preference.AppPreference;
import com.fancustomer.fcm.MyFirebaseMessagingService;
import com.fancustomer.helper.ErrorUtils;
import com.fancustomer.helper.ExceptionHandler;
import com.fancustomer.helper.RingProgressBar;
import com.fancustomer.helper.TimerService;
import com.fancustomer.utility.AppUtils;
import com.fancustomer.webservice.Api;
import com.fancustomer.webservice.ApiFactory;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class StartPhotosShootActivity extends BaseActivity implements View.OnClickListener {
    private TextView imageViewLoading;
    private Timer timer;
    private long status = 0;
    private long time = 0;
    private Dialog dialogRenew;
    private String priceNew;
    private ReniewAdapter reniewAdapter;
    private String isRenew = "";
    private ArrayList<ReniewBean> reniewList;
    private RelativeLayout rlStart;
    private RelativeLayout rlEnd;
    private TextView buttonStart;
    private TextView buttonCancel;
    private TextView buttonEnd;
    private String orderID = "";
    private String price = "";
    private String orderCancelCharge = "";
    private String slotTime = "";
    private String profileImage = "";
    private TextView endTextView;
    private BroadcastReceiver broadcastReceiver;
    private long killTimerTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_photos_shoot);
        orderID = AppPreference.getInstance(StartPhotosShootActivity.this).getString("orderID");
        profileImage = AppPreference.getInstance(StartPhotosShootActivity.this).getString("profile_image_new");
        price = AppPreference.getInstance(StartPhotosShootActivity.this).getString("price");
        slotTime = AppPreference.getInstance(StartPhotosShootActivity.this).getString("slotTime");
        orderCancelCharge = AppPreference.getInstance(StartPhotosShootActivity.this).getString("order_cancel_charge");

        intView();
        setToolBar();
        AppPreference.getInstance(StartPhotosShootActivity.this).setString(Constants.TRACK_START, "0");


        setClicks();
        reniewList = new ArrayList<>();
        if (Constants.isInternetOn(StartPhotosShootActivity.this)) {
            getSlotsApi();
        } else {
            final ViewGroup viewGroup = (ViewGroup) ((ViewGroup) StartPhotosShootActivity.this.findViewById(android.R.id.content)).getChildAt(0);
            showSnackbar(viewGroup, getResources().getString(R.string.no_internet), Constants.RETRY);
        }

        register();
    }

    public void killTimerProgress() {
        try {
            String currentTime = AppUtils.getCurrentTime();
            String previousTime = appPreference.getString("currentTime");
            if (!previousTime.equals("")) {
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date date_current = simpleDateFormat.parse(currentTime);
                Date date_previous = simpleDateFormat.parse(previousTime);

                long newTime = appPreference.getLongValue("KILLTIMENEW");

                long finalTime = AppUtils.printDifference(date_current, date_previous, newTime);
                long maintime = finalTime / 1000;


                ringProgress(maintime, "renew");
            }


        } catch (Exception e) {
            e.printStackTrace();
        }


    }


    private void intView() {
        endTextView = (TextView) findViewById(R.id.end_textView);
        CircleImageView userImageView = (CircleImageView) findViewById(R.id.userImageView);
        rlStart = (RelativeLayout) findViewById(R.id.rl_start);
        rlStart.setVisibility(View.VISIBLE);
        rlEnd = (RelativeLayout) findViewById(R.id.rl_end);
        buttonStart = (TextView) findViewById(R.id.button_start);
        buttonCancel = (TextView) findViewById(R.id.button_cancel);
        buttonEnd = (TextView) findViewById(R.id.button_end);
        endTextView.setText("Session will end in " + slotTime + " minutes, or you can end it early by clicking \"END\" button");

        if (!profileImage.equals("")) {
            Glide.with(StartPhotosShootActivity.this).load(profileImage)
                    .thumbnail(0.5f)
                    .placeholder(R.mipmap.defult_user).dontAnimate()
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .listener(new RequestListener<String, GlideDrawable>() {
                        @Override
                        public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                            return false;
                        }
                    }).into(userImageView);

        }


    }


    private void setClicks() {
        buttonEnd.setOnClickListener(this);
        buttonStart.setOnClickListener(this);
        buttonCancel.setOnClickListener(this);
    }

    private void setToolBar() {
        ImageView imageViewMenu = (ImageView) findViewById(R.id.imageView_menu);
        TextView headerTextview = (TextView) findViewById(R.id.hedertextview);
        ImageView toolbarbackpress = (ImageView) findViewById(R.id.toolbarbackpress);
        toolbarbackpress.setVisibility(View.GONE);
        imageViewMenu.setVisibility(View.GONE);
        headerTextview.setText(getResources().getString(R.string.start_photoshoot));
        toolbarbackpress.setOnClickListener(this);

    }

    @Override
    protected void networkConnnectivityChange() {
        super.networkConnnectivityChange();
        if (!Constants.isInternetOn(StartPhotosShootActivity.this)) {
            final ViewGroup viewGroup = (ViewGroup) ((ViewGroup) StartPhotosShootActivity.this.findViewById(android.R.id.content)).getChildAt(0);
            showSnackbar(viewGroup, getResources().getString(R.string.no_internet), Constants.RETRY);

        } else {
            hideSnackbar();
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }


    private void ringProgress(long ringTimeSlot, final String renew) {
        if (renew.equals("")) {
            AppPreference.getInstance(StartPhotosShootActivity.this).setLongValue("statusKilled", ringTimeSlot);
            Log.e(Constants.LOG_CAT, "StartPhotosShootActivity============ringTimeSlot>>>>>>>>>" + ringTimeSlot);
        }
        AppPreference.getInstance(StartPhotosShootActivity.this).setLongValue("timer_key", ringTimeSlot);
        Intent intentService = new Intent(StartPhotosShootActivity.this, TimerService.class);
        startService(intentService);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            startForegroundService(intentService);
//        } else {
//            startService(intentService);
//        }
        final RingProgressBar mRingProgressBar = (RingProgressBar) findViewById(R.id.progress_bar_1);
        imageViewLoading = (TextView) findViewById(R.id.imageView_loading);
        mRingProgressBar.setMax((int) (AppPreference.getInstance(StartPhotosShootActivity.this).getLongValue("statusKilled")));
        status = (ringTimeSlot * 1);
        time = status * 1000;
        if (renew.equals("")) {
            mRingProgressBar.setProgress((int) status);
        } else {
            mRingProgressBar.setProgress((int) status);

        }
        timer = new Timer();
        timer.schedule(new TimerTask() {

            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            try {
                                status = status - 1;
                                mRingProgressBar.setProgress((int) status);
                                time = time - 1000;

                                if (time == 0 || time < 0) {

                                } else {
                                    String text = String.format("%02d:%02d:%02d",
                                            TimeUnit.MILLISECONDS.toHours(time) - TimeUnit.HOURS.toHours(TimeUnit.MILLISECONDS.toDays(time)),
                                            TimeUnit.MILLISECONDS.toMinutes(time) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(time)),
                                            TimeUnit.MILLISECONDS.toSeconds(time) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(time)));
                                    imageViewLoading.setText(text);
                                }


                            } catch (Exception e) {
                                ExceptionHandler.printStackTrace(e);
                            }
                            Log.e(Constants.LOG_CAT, "StartPhotosShootActivity============ring time>>>>>>>>>" + time);
                            Log.e(Constants.LOG_CAT, "StartPhotosShootActivity============ring Status>>>>>>>>>" + status);
                            if (isRenew.equals("")) {
                                if (time == 300000) {
                                    Log.e(Constants.LOG_CAT, "StartPhotosShootActivity=============" + "service again=======================");
                                    AppPreference.getInstance(StartPhotosShootActivity.this).setString("isRenew", "false");
                                    isRenew = "false";
                                    if (reniewList != null && reniewList.size() > 0) {
                                        renewDialog(reniewList);
                                    }
                                } else {
                                    if (time == 0 || time < 0) {
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                canceltimer();
                                                canceltimerService();
                                                AppPreference.getInstance(getApplicationContext()).setLongValue("timer_key", 0);
                                                AppPreference.getInstance(getApplicationContext()).setLongValue("status_key", 0);
                                                AppPreference.getInstance(StartPhotosShootActivity.this).setLongValue("statusKilled", 0);
                                                AppPreference.getInstance(StartPhotosShootActivity.this).setString("isRenew", "");
                                                AppPreference.getInstance(StartPhotosShootActivity.this).setString(Constants.START_PHOTO, "0");
                                                if (Constants.isInternetOn(StartPhotosShootActivity.this)) {
                                                    endPhotoSession(orderID);
                                                } else {
                                                    final ViewGroup viewGroup = (ViewGroup) ((ViewGroup) StartPhotosShootActivity.this.findViewById(android.R.id.content)).getChildAt(0);
                                                    showSnackbar(viewGroup, getResources().getString(R.string.no_internet), Constants.RETRY);
                                                }
                                            }
                                        });


                                    }
                                }
                            } else {
                                if (time == 0 || time < 0) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            canceltimer();
                                            canceltimerService();
                                            AppPreference.getInstance(getApplicationContext()).setLongValue("timer_key", 0);
                                            AppPreference.getInstance(getApplicationContext()).setLongValue("status_key", 0);
                                            AppPreference.getInstance(StartPhotosShootActivity.this).setLongValue("statusKilled", 0);
                                            AppPreference.getInstance(StartPhotosShootActivity.this).setString("isRenew", "");
                                            AppPreference.getInstance(StartPhotosShootActivity.this).setString(Constants.START_PHOTO, "0");
                                            if (Constants.isInternetOn(StartPhotosShootActivity.this)) {
                                                endPhotoSession(orderID);
                                            } else {
                                                final ViewGroup viewGroup = (ViewGroup) ((ViewGroup) StartPhotosShootActivity.this.findViewById(android.R.id.content)).getChildAt(0);
                                                showSnackbar(viewGroup, getResources().getString(R.string.no_internet), Constants.RETRY);
                                            }
                                        }
                                    });


                                }
                            }


                        } catch (Exception e) {
                            timer.cancel();
                        }

                    }
                });
            }

        }, 0, 1000);
    }

    public void canceltimer() {
        try {
            if (timer != null) {
                timer.cancel();
            }
        } catch (Exception e) {
            ExceptionHandler.printStackTrace(e);
        }
    }

    public void canceltimerService() {
        try {
            if (TimerService.timerService != null) {
                TimerService.timerService.cancel();
            }
        } catch (Exception e) {
            ExceptionHandler.printStackTrace(e);
        }
    }

    public void unRegister() {
        try {
            unregisterReceiver(broadcastReceiver);
        } catch (Exception e) {
            ExceptionHandler.printStackTrace(e);
        }

    }


    @Override
    protected void onPause() {
        super.onPause();
        if (timer != null) {
            AppPreference.getInstance(StartPhotosShootActivity.this).setString(Constants.START_PHOTO, "1");
            String currenttime = AppUtils.getCurrentTime();
            Log.e("time.....", "" + currenttime);
            killTimerTime = AppPreference.getInstance(getApplicationContext()).getLongValue("timer_key");
            AppPreference.getInstance(getApplicationContext()).setLongValue("KILLTIMENEW", killTimerTime);
            AppPreference.getInstance(getApplicationContext()).setString("currentTime", currenttime);
        }
    }

    public void register() {
        IntentFilter intentFilter = new IntentFilter("ACTION_REFRESH_USER.intent.MAIN");
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String broadCastActionType = intent.getStringExtra(Constants.BROADCAST_ACTION);
                String startTimeRequestApprove = intent.getStringExtra("START_TIME_REQUEST_APPROVE");
                String notificationId = intent.getStringExtra("notification_id");
                String cancelSessionByPhotographer = intent.getStringExtra("CANCEL_SESSION_BY_PHOTOGRAPHER");
                if (broadCastActionType.equals(Constants.ACTION_REFRESH_USER)) {
                    if (!Constants.isStringNullOrBlank(startTimeRequestApprove)) {
                        if (startTimeRequestApprove.equals(Constants.START_TIME_REQUEST_APPROVE)) {
                            buttonEnd.setBackgroundColor(getResources().getColor(R.color.dark_orange));
                            buttonEnd.setEnabled(true);
                            ringProgress(Long.parseLong(slotTime) * 60, "");

                            if (!Constants.isStringNullOrBlank(notificationId)) {
                                MyFirebaseMessagingService.clearNotification(StartPhotosShootActivity.this, Integer.valueOf(notificationId));
                            }

                        }
                    } else if (!Constants.isStringNullOrBlank(cancelSessionByPhotographer)) {
                        if (cancelSessionByPhotographer.equals(Constants.CANCEL_SESSION_BY_PHOTOGRAPHER)) {
                            AppPreference.getInstance(StartPhotosShootActivity.this).setString(Constants.TRACK_START, "0");
                            AppPreference.getInstance(StartPhotosShootActivity.this).setString(Constants.START_PHOTO, "0");
                            Intent requestIntent = new Intent(StartPhotosShootActivity.this, MenuScreen.class);
                            startActivity(requestIntent);
                            finish();
                            if (!Constants.isStringNullOrBlank(notificationId)) {
                                MyFirebaseMessagingService.clearNotification(StartPhotosShootActivity.this, Integer.valueOf(notificationId));
                            }

                        }


                    }
                }


            }
        };
        registerReceiver(broadcastReceiver, intentFilter);


    }


    @Override
    public void onResume() {
        super.onResume();
        canceltimer();
        canceltimerService();
        if (TimerService.timerService != null) {
            Log.e(Constants.LOG_CAT, "onResume: ");
        }
        String comeFrom = AppPreference.getInstance(StartPhotosShootActivity.this).getString(Constants.START_PHOTO);
        if (comeFrom.equals("1")) {
            long newTime = AppPreference.getInstance(getApplicationContext()).getLongValue("timer_key");
            long serviceStatus = AppPreference.getInstance(getApplicationContext()).getLongValue("status_key");
            long statusKilled = AppPreference.getInstance(StartPhotosShootActivity.this).getLongValue("statusKilled");
            long progressMin = (statusKilled - serviceStatus);

            Log.e(Constants.LOG_CAT, "StartPhotosShootActivity============service time>>>>>>>>>" + newTime);
            Log.e(Constants.LOG_CAT, "StartPhotosShootActivity============service statusKilled>>>>>>>>>" + statusKilled);
            Log.e(Constants.LOG_CAT, "StartPhotosShootActivity============service Status>>>>>>>>>" + serviceStatus);
            Log.e(Constants.LOG_CAT, "StartPhotosShootActivity============service progressMin>>>>>>>>>" + progressMin);

            if (newTime == 0) {
                if (Constants.isServiceRunning(StartPhotosShootActivity.this, TimerService.class)) {
                    stopService(new Intent(StartPhotosShootActivity.this, TimerService.class));
                }
                endPhotoSession(orderID);
                AppPreference.getInstance(getApplicationContext()).setLongValue("timer_key", 0);
                AppPreference.getInstance(getApplicationContext()).setLongValue("status_key", 0);
                AppPreference.getInstance(StartPhotosShootActivity.this).setLongValue("statusKilled", 0);
                AppPreference.getInstance(StartPhotosShootActivity.this).setString("isRenew", "");
            } else {
                if (Constants.isServiceRunning(StartPhotosShootActivity.this, TimerService.class)) {
                    stopService(new Intent(StartPhotosShootActivity.this, TimerService.class));
                }
                String isNewRenew = AppPreference.getInstance(StartPhotosShootActivity.this).getString("isRenew");

                Log.e(Constants.LOG_CAT, "StartPhotosShootActivity =====================: " + isNewRenew);
                if (isNewRenew.equals(Constants.FALSE)) {
                    isRenew = Constants.FALSE;
                } else {
                    isRenew = "";
                }
                long newTimeSecond = newTime;
                Log.e(Constants.LOG_CAT, "StartPhotosShootActivity============service newTimeSecond>>>>>>>>>" + newTimeSecond);
                canceltimer();
                canceltimerService();
                killTimerProgress();
                // ringProgress(newTimeSecond, "renew");
                rlStart.setVisibility(View.GONE);
                rlEnd.setVisibility(View.VISIBLE);

            }
        }
    }

    public void endPhotoSession(final String orderID) {
        Api api = ApiFactory.getClientWithoutHeader(StartPhotosShootActivity.this).create(Api.class);
        Call<ResponseBody> call;
        call = api.getEndSessoinApi(AppPreference.getInstance(StartPhotosShootActivity.this).getString(Constants.ACCESS_TOKEN), orderID);
        Log.e(Constants.LOG_CAT, "API endPhotoSession------------------->>>>>:" + call.request().url());
        Log.e(Constants.LOG_CAT, "HEADERS" + call.request().headers());
        Constants.showProgressDialog(StartPhotosShootActivity.this, Constants.LOADING);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Constants.hideProgressDialog();
                canceltimer();

                try {
                    if (response.isSuccessful()) {
                        String output = ErrorUtils.getResponseBody(response);


                        JSONObject object = new JSONObject(output);


                        Log.d("tag", object.toString(1));

                        if (object.optString(Constants.SUCCESS).equalsIgnoreCase(Constants.TRUE)) {
                            Log.e(Constants.LOG_CAT, "onResponse:API endPhotoSession>>>>>>>>>>>>>>>>>>>>>>>" + object.toString());
                            JSONObject jsonObject = object.optJSONObject("data");
                            AppPreference.getInstance(StartPhotosShootActivity.this).setString(Constants.START_PHOTO, "0");
                            String last4Digit = jsonObject.optString("last_4_digit");
                            String photographerFirstName = jsonObject.optString("photographer_first_name");
                            String photographerLastName = jsonObject.optString("photographer_last_name");
                            String photographerProfileImage = jsonObject.optString("photographer_profile_image");
                            String ratingAvg = jsonObject.optString("rating_avg");
                            String photographerId = jsonObject.optString("photographer_id");
                            String orderCreatedAt = jsonObject.optString("order_created_at");
                            String timeStr = jsonObject.optString("time");
                            String priceStr = jsonObject.optString("price");


                            Intent intent = new Intent(StartPhotosShootActivity.this, BillDetailActivty.class);
                            intent.putExtra("last_4_digit", last4Digit);
                            intent.putExtra("photographer_first_name", "" + photographerFirstName);
                            intent.putExtra("photographer_last_name", "" + photographerLastName);
                            intent.putExtra("photographer_profile_image", "" + photographerProfileImage);
                            intent.putExtra("rating_avg", "" + ratingAvg);
                            intent.putExtra("order_created_at", "" + orderCreatedAt);
                            intent.putExtra("time", "" + timeStr);
                            intent.putExtra("price", "" + priceStr);
                            intent.putExtra("photographer_id", "" + photographerId);
                            intent.putExtra("orderID", "" + orderID);
                            startActivity(intent);
                            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                            finish();

                        } else if (object.optString(Constants.SUCCESS).equalsIgnoreCase(Constants.FALSE)) {
                            ErrorUtils.showFalseMessage(object, StartPhotosShootActivity.this);
                        }
                    } else if (response.code() == 400 || response.code() == 500 || response.code() == 403 || response.code() == 404 || response.code() == 401) {
                        if (response.code() == 401) {
                            Constants.showSessionExpireAlert(StartPhotosShootActivity.this);
                        } else {
                            Constants.showToastAlert(ErrorUtils.getHtttpCodeError(response.code()), StartPhotosShootActivity.this);
                        }

                    } else {
                        String responseStr = ErrorUtils.getResponseBody(response);
                        JSONObject jsonObject = new JSONObject(responseStr);
                        Constants.showToastAlert(ErrorUtils.checkJosnErrorBody(jsonObject), StartPhotosShootActivity.this);
                    }
                } catch (JSONException e) {
                    Constants.hideProgressDialog();
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Constants.hideProgressDialog();
            }
        });


    }


    public void startTimeRequest(String orderID) {
        Api api = ApiFactory.getClientWithoutHeader(StartPhotosShootActivity.this).create(Api.class);
        HashMap<String, String> map = new HashMap<>();
        Call<ResponseBody> call = null;
        map.put("order_id", orderID);
        String accessToken = AppPreference.getInstance(StartPhotosShootActivity.this).getString(Constants.ACCESS_TOKEN);
        call = api.StartTimeApi(accessToken, map);
        Log.e(Constants.LOG_CAT, "API REQUEST startTimeRequest------------------->>>>>:" + map + " " + call.request().url());
        Log.e(Constants.LOG_CAT, "HEADERS : @@@@@@@@@@@@@@@@@@@@@@@@@@@@@" + call.request().headers());


        Constants.showProgressDialog(StartPhotosShootActivity.this, Constants.LOADING);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Constants.hideProgressDialog();

                try {
                    if (response.isSuccessful()) {
                        String output = ErrorUtils.getResponseBody(response);
                        JSONObject object = new JSONObject(output);


                        Log.d("tag", object.toString(1));

                        if (object.optString(Constants.SUCCESS).equalsIgnoreCase(Constants.TRUE)) {
                            Log.e(Constants.LOG_CAT, "API REQUEST SEND RstartTimeRequest================>>>>>" + object.toString());
                            AppPreference.getInstance(StartPhotosShootActivity.this).setString(Constants.START_PHOTO, "1");
                            rlStart.setVisibility(View.GONE);
                            rlEnd.setVisibility(View.VISIBLE);
                            buttonEnd.setBackgroundColor(getResources().getColor(R.color.gray_color));
                            buttonEnd.setEnabled(false);
                        } else if (object.optString(Constants.SUCCESS).equalsIgnoreCase(Constants.FALSE)) {
                            ErrorUtils.showFalseMessage(object, StartPhotosShootActivity.this);
                        }
                    } else if (response.code() == 400 || response.code() == 500 || response.code() == 403 || response.code() == 404 || response.code() == 401) {
                        if (response.code() == 401) {
                            Constants.showSessionExpireAlert(StartPhotosShootActivity.this);
                        } else {
                            Constants.showToastAlert(ErrorUtils.getHtttpCodeError(response.code()), StartPhotosShootActivity.this);
                        }

                    } else {
                        String responseStr = ErrorUtils.getResponseBody(response);
                        JSONObject jsonObject = new JSONObject(responseStr);
                        Constants.showToastAlert(ErrorUtils.checkJosnErrorBody(jsonObject), StartPhotosShootActivity.this);
                    }
                } catch (JSONException e) {
                    Constants.hideProgressDialog();
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Constants.hideProgressDialog();
            }
        });


    }


    public void renewStart(String orderID, final String slot, String price) {
        Api api = ApiFactory.getClientWithoutHeader(StartPhotosShootActivity.this).create(Api.class);
        HashMap<String, String> map = new HashMap<>();
        Call<ResponseBody> call;
        map.put("order_id", orderID);
        map.put("slot", slot);
        map.put("price", price);
        String accessToken = AppPreference.getInstance(StartPhotosShootActivity.this).getString(Constants.ACCESS_TOKEN);
        call = api.renewTimeRequest(accessToken, map);
        Log.e(Constants.LOG_CAT, "API REQUEST renewTimeRequest------------------->>>>>:" + map + " " + call.request().url());
        Log.e(Constants.LOG_CAT, "HEADERS @@@@: " + call.request().headers());


        Constants.showProgressDialog(StartPhotosShootActivity.this, Constants.LOADING);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Constants.hideProgressDialog();

                try {
                    if (response.isSuccessful()) {

                        String output = ErrorUtils.getResponseBody(response);

                        JSONObject object = new JSONObject(output);


                        Log.d("tag", object.toString(1));

                        if (object.optString(Constants.SUCCESS).equalsIgnoreCase(Constants.TRUE)) {
                            JSONObject jsonObject = object.optJSONObject("data");
                            String totalSlotTime = jsonObject.optString("total_slot_time");
                            Log.e(Constants.LOG_CAT, "API REQUEST SEND renewTimeRequest================>>>>>" + object.toString());
                            long extandTime = time / 1000;
                            Log.e(Constants.LOG_CAT, "renewTimeRequest============APP extandTime========: " + extandTime);

                            canceltimerService();
                            try {
                                if (timer != null) {
                                    timer.cancel();
                                }
                            } catch (Exception e) {
                                ExceptionHandler.printStackTrace(e);
                            }
                            Log.e(Constants.LOG_CAT, "SlotTime==================" + slot + "   " + slotTime);
                            int priceMain = Integer.parseInt(totalSlotTime);
                            if (priceMain < 60) {
                                endTextView.setText("Session will end in " + totalSlotTime + " minutes, or you can end it early by clicking \"END\" button");
                            } else {
                                long hourNew = TimeUnit.MINUTES.toMillis(priceMain);
                                String hms = String.format("%02d:%02d", TimeUnit.MILLISECONDS.toHours(hourNew),
                                        TimeUnit.MILLISECONDS.toMinutes(hourNew) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(hourNew)));
                                endTextView.setText("Session will end in " + hms + " Hour, or you can end it early by clicking \"END\" button");
                            }
                            ringProgress(Long.parseLong(tittle) * 60 + extandTime, "");
                        } else if (object.optString(Constants.SUCCESS).equalsIgnoreCase(Constants.FALSE)) {
                            ErrorUtils.showFalseMessage(object, StartPhotosShootActivity.this);
                        }
                    } else if (response.code() == 400 || response.code() == 500 || response.code() == 403 || response.code() == 404 || response.code() == 401) {
                        if (response.code() == 401) {
                            Constants.showSessionExpireAlert(StartPhotosShootActivity.this);
                        } else {
                            Constants.showToastAlert(ErrorUtils.getHtttpCodeError(response.code()), StartPhotosShootActivity.this);
                        }

                    } else {
                        String responseStr = ErrorUtils.getResponseBody(response);
                        JSONObject jsonObject = new JSONObject(responseStr);
                        Constants.showToastAlert(ErrorUtils.checkJosnErrorBody(jsonObject), StartPhotosShootActivity.this);
                    }
                } catch (JSONException e) {
                    Constants.hideProgressDialog();
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Constants.hideProgressDialog();
            }
        });


    }


    public void cancelApi(final String orderID) {


        Api api = ApiFactory.getClientWithoutHeader(StartPhotosShootActivity.this).create(Api.class);
        Call<ResponseBody> call = null;

        call = api.cancelPhoto(AppPreference.getInstance(StartPhotosShootActivity.this).getString(Constants.ACCESS_TOKEN), orderID);
        Log.e(Constants.LOG_CAT, "API  CancelApi------------------->>>>>:" + call.request().url());
        Log.e(Constants.LOG_CAT, "HEADERS : " + call.request().headers());


        Constants.showProgressDialog(StartPhotosShootActivity.this, "Loading");
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Constants.hideProgressDialog();


                try {
                    if (response.isSuccessful()) {

                        String output = ErrorUtils.getResponseBody(response);

                        JSONObject object = new JSONObject(output);


                        Log.d("tag", object.toString(1));

                        if (object.optString(Constants.SUCCESS).equalsIgnoreCase(Constants.TRUE)) {
                            Log.e(Constants.LOG_CAT, "onResponse:API  CancelApi>>>>>>>>>>>>>>>>>>>>>>>" + object.toString());
                            AppPreference.getInstance(StartPhotosShootActivity.this).setString(Constants.TRACK_START, "0");
                            Intent intent = new Intent(StartPhotosShootActivity.this, MenuScreen.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK
                                    | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                            finish();
                        } else if (object.optString(Constants.SUCCESS).equalsIgnoreCase(Constants.FALSE)) {
                            ErrorUtils.showFalseMessage(object, StartPhotosShootActivity.this);
                        }
                    } else if (response.code() == 400 || response.code() == 500 || response.code() == 403 || response.code() == 404 || response.code() == 401) {
                        if (response.code() == 401) {
                            Constants.showSessionExpireAlert(StartPhotosShootActivity.this);
                        } else {
                            Constants.showToastAlert(ErrorUtils.getHtttpCodeError(response.code()), StartPhotosShootActivity.this);
                        }

                    } else {
                        String responseStr = ErrorUtils.getResponseBody(response);
                        JSONObject jsonObject = new JSONObject(responseStr);
                        Constants.showToastAlert(ErrorUtils.checkJosnErrorBody(jsonObject), StartPhotosShootActivity.this);
                    }
                } catch (JSONException e) {
                    Constants.hideProgressDialog();
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Constants.hideProgressDialog();
            }
        });


    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.toolbarbackpress:
                onBackPressed();
                break;

            case R.id.button_start:
                showPaymentDialog();
                break;

            case R.id.button_cancel:
                showCancelDialog();
                break;

            case R.id.button_end:
                if (Constants.isInternetOn(StartPhotosShootActivity.this)) {
                    showEndSessionDialog();
                } else {
                    final ViewGroup viewGroup = (ViewGroup) ((ViewGroup) StartPhotosShootActivity.this.findViewById(android.R.id.content)).getChildAt(0);
                    showSnackbar(viewGroup, getResources().getString(R.string.no_internet), Constants.RETRY);
                }
                break;
            default:
                break;


        }
    }


    @Override
    public void onBackPressed() {
        Log.e(Constants.LOG_CAT, "onBackPressed: ");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        canceltimer();
        unRegister();
    }

    private void showPaymentDialog() {
        final Dialog dialog = new Dialog(StartPhotosShootActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);

        dialog.setCanceledOnTouchOutside(false);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.dialog_payment);
        TextView buttonConfirm = dialog.findViewById(R.id.button_confirm);
        TextView textViewNo = dialog.findViewById(R.id.button_cancel);
        TextView textviewMessages = dialog.findViewById(R.id.textview__messages);
        textviewMessages.setText("$ " + new DecimalFormat("#,##0.00").format(Double.parseDouble(price)));

        buttonConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();

                if (Constants.isInternetOn(StartPhotosShootActivity.this)) {
                    startTimeRequest(orderID);
                } else {
                    final ViewGroup viewGroup = (ViewGroup) ((ViewGroup) StartPhotosShootActivity.this.findViewById(android.R.id.content)).getChildAt(0);
                    showSnackbar(viewGroup, getResources().getString(R.string.no_internet), Constants.RETRY);
                }
            }
        });
        textViewNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dialog.dismiss();
            }
        });
        dialog.show();

    }


    private void showCancelDialog() {
        final Dialog dialog = new Dialog(StartPhotosShootActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);

        dialog.setCanceledOnTouchOutside(false);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.dialog_payment);
        TextView buttonConfirm = dialog.findViewById(R.id.button_confirm);
        TextView textViewNo = dialog.findViewById(R.id.button_cancel);
        TextView textviewNewMessage = dialog.findViewById(R.id.textview__new_message);
        TextView textviewMessages = dialog.findViewById(R.id.textview__messages);
        TextView textviewHeader = dialog.findViewById(R.id.textview__header);
        textviewHeader.setText(getResources().getString(R.string.cancel_photoshoot));
        buttonConfirm.setText(getResources().getString(R.string.yes));
        textViewNo.setText(getResources().getString(R.string.no));
        textviewNewMessage.setText(getResources().getString(R.string.you_wil_be_charged_a_cancellation));
        textviewMessages.setText("$ " + new DecimalFormat("#,##0.00").format(Double.parseDouble(orderCancelCharge)));
        buttonConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();

                if (Constants.isInternetOn(StartPhotosShootActivity.this)) {
                    cancelApi(orderID);
                } else {
                    final ViewGroup viewGroup = (ViewGroup) ((ViewGroup) StartPhotosShootActivity.this.findViewById(android.R.id.content)).getChildAt(0);
                    showSnackbar(viewGroup, getResources().getString(R.string.no_internet), Constants.RETRY);
                }

            }
        });
        textViewNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dialog.dismiss();
            }
        });
        dialog.show();

    }


    public void getSlotsApi() {


        Api api = ApiFactory.getClientWithoutHeader(StartPhotosShootActivity.this).create(Api.class);
        Call<ResponseBody> call;

        call = api.getSlots();
        Log.e(Constants.LOG_CAT, "API GET SLOTS------------------->>>>>:" + call.request().url());
        Log.e(Constants.LOG_CAT, "HEADERS : " + call.request().headers());


        Constants.showProgressDialog(StartPhotosShootActivity.this, "Loading");
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Constants.hideProgressDialog();
                try {
                    if (response.isSuccessful()) {
                        String output = ErrorUtils.getResponseBody(response);
                        JSONObject object = new JSONObject(output);

                        Log.d("tag", object.toString(1));

                        if (object.optString(Constants.SUCCESS).equalsIgnoreCase(Constants.TRUE)) {
                            Log.e(Constants.LOG_CAT, "onResponse: API GET SLOTS-=" + object.toString());


                            JSONArray jsonArray = object.optJSONArray("data");
                            if (jsonArray != null && jsonArray.length() > 0) {
                                for (int i = 0; i < jsonArray.length(); i++) {

                                    JSONObject jsonObject = jsonArray.optJSONObject(i);
                                    String id = jsonObject.optString("id");
                                    String slotMinutes = jsonObject.optString("slot_minutes");
                                    String priceStr = jsonObject.optString("price");
                                    String createdAt = jsonObject.optString("created_at");
                                    ReniewBean reniewBean = new ReniewBean();
                                    reniewBean.id = id;
                                    reniewBean.slot_minutes = slotMinutes;
                                    reniewBean.price = priceStr;
                                    reniewBean.status = false;
                                    reniewBean.created_at = createdAt;
                                    reniewList.add(reniewBean);

                                }
                            }


                        } else if (object.optString(Constants.SUCCESS).equalsIgnoreCase(Constants.FALSE)) {
                            ErrorUtils.showFalseMessage(object, StartPhotosShootActivity.this);
                        }
                    } else if (response.code() == 400 || response.code() == 500 || response.code() == 403 || response.code() == 404 || response.code() == 401) {
                        if (response.code() == 401) {
                            Constants.showSessionExpireAlert(StartPhotosShootActivity.this);
                        } else {
                            Constants.showToastAlert(ErrorUtils.getHtttpCodeError(response.code()), StartPhotosShootActivity.this);
                        }

                    } else {
                        String responseStr = ErrorUtils.getResponseBody(response);
                        JSONObject jsonObject = new JSONObject(responseStr);
                        Constants.showToastAlert(ErrorUtils.checkJosnErrorBody(jsonObject), StartPhotosShootActivity.this);
                    }
                } catch (JSONException e) {
                    Constants.hideProgressDialog();
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Constants.hideProgressDialog();
            }
        });


    }

    private void showEndSessionDialog() {
        final Dialog dialog = new Dialog(StartPhotosShootActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);

        dialog.setCanceledOnTouchOutside(false);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.dialog_end);
        TextView buttonConfirm = dialog.findViewById(R.id.button_confirm);
        TextView textViewNo = dialog.findViewById(R.id.button_cancel);

        buttonConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                if (Constants.isInternetOn(StartPhotosShootActivity.this)) {
                    canceltimer();
                    canceltimerService();
                    AppPreference.getInstance(getApplicationContext()).setLongValue("timer_key", 0);
                    AppPreference.getInstance(getApplicationContext()).setLongValue("status_key", 0);
                    AppPreference.getInstance(StartPhotosShootActivity.this).setLongValue("statusKilled", 0);
                    AppPreference.getInstance(StartPhotosShootActivity.this).setString("isRenew", "");
                    endPhotoSession(orderID);
                } else {
                    final ViewGroup viewGroup = (ViewGroup) ((ViewGroup) StartPhotosShootActivity.this.findViewById(android.R.id.content)).getChildAt(0);
                    showSnackbar(viewGroup, getResources().getString(R.string.no_internet), Constants.RETRY);
                }
            }
        });
        textViewNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dialog.dismiss();
            }
        });
        dialog.show();

    }

    String tittle;
    String resionsId;
    ListView listview;

    public void renewDialog(final ArrayList<ReniewBean> arrayList) {
        resionsId = "";
        tittle = "";
        dialogRenew = new Dialog(StartPhotosShootActivity.this);
        dialogRenew.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialogRenew.setCancelable(false);
        dialogRenew.setCanceledOnTouchOutside(false);
        dialogRenew.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialogRenew.setContentView(R.layout.review_dialog);
        listview = dialogRenew.findViewById(R.id.listview);
        TextView yes = dialogRenew.findViewById(R.id.yes);
        TextView no = dialogRenew.findViewById(R.id.no);
        if (arrayList != null && arrayList.size() > 0) {
            reniewAdapter = new ReniewAdapter(StartPhotosShootActivity.this, arrayList);
            listview.setAdapter(reniewAdapter);
        }
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long sid) {
                tittle = arrayList.get(position).slot_minutes;
                priceNew = arrayList.get(position).price;
                resionsId = arrayList.get(position).id;
                for (int j = 0; j < arrayList.size(); j++) {
                    if (position == j) {
                        arrayList.get(j).status = true;
                    } else {
                        arrayList.get(j).status = false;
                    }
                }
                reniewAdapter.notifyDataSetChanged();
            }
        });

        yes.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (tittle.equals("")) {

                }

                renewStart(orderID, tittle, priceNew);
                dialogRenew.dismiss();


            }


        });
        no.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dialogRenew.dismiss();
            }
        });
        dialogRenew.show();
    }


}