package com.fanphotographer.activity;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.Dialog;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.fanphotographer.R;
import com.fanphotographer.bean.GetUserBean;
import com.fanphotographer.data.constant.Constants;
import com.fanphotographer.data.preference.AppPreference;
import com.fanphotographer.fcm.MyFirebaseMessagingService;
import com.fanphotographer.fragment.BookingHistoryFragment;
import com.fanphotographer.fragment.ContactFragment;
import com.fanphotographer.fragment.FAQFragment;
import com.fanphotographer.fragment.HomeFragment;
import com.fanphotographer.fragment.NotificationFragment;
import com.fanphotographer.fragment.ProfileFragment;
import com.fanphotographer.fragment.TermsConditionFragment;
import com.fanphotographer.utility.AppUtils;
import com.fanphotographer.utility.ErrorUtils;
import com.fanphotographer.webservice.Api;
import com.fanphotographer.webservice.ApiFactory;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.special.ResideMenu.ResideMenu;
import com.special.ResideMenu.ResideMenuItem;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.HashMap;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;


public class MenuScreen extends FragmentActivity implements View.OnClickListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    @SuppressLint("StaticFieldLeak")
    public static ResideMenu resideMenu;
    private ResideMenuItem itemHome;
    private ResideMenuItem itemHistory;
    private ResideMenuItem itemFaq;
    private ResideMenuItem itemTerms;
    private ResideMenuItem itemContact;
    private ResideMenuItem itemLogout;
    public ResideMenuItem itemNotification;
    private Dialog dialog;
    private Dialog acceptDailog;
    private Context mContext = this;
    private GetUserBean getUserBean;
    private static final long DOUBLE_PRESS_INTERVAL = 250; // in millis
    private long lastPressTime;
    private boolean mHasDoubleClicked = false;
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;
    private Location mLastLocation;
    private GoogleApiClient mGoogleApiClient;
    private boolean mRequestingLocationUpdates = false;
    private LocationRequest mLocationRequest;
    private double latitude;
    private double longitude;
    private BroadcastReceiver broadcastReceiver;
    public AppPreference appPreference;
    public CountDownTimer countDownTimer;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_menu);
        appPreference = AppPreference.getInstance(MenuScreen.this);
        getparm();
        register();

        if (checkPlayServices()) {
            buildGoogleApiClient();
            createLocationRequest();
        }

        setUpMenu();
        changeFragment(new HomeFragment(),"HomeFragment");
        setResideMenuItemIconColor(true, false, false, false, false, false, false, false, false);


    }

    private void setUpMenu() {


        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = displayMetrics.widthPixels;
        resideMenu = new ResideMenu(this);
        resideMenu.setBackground(R.color.white);
        resideMenu.attachToActivity(this);
        resideMenu.setMenuListener(menuListener);
        resideMenu.setScaleValue(0.8f);
        itemHome = new ResideMenuItem(this, R.mipmap.menu_home, getResources().getString(R.string.home), width);
        itemHistory = new ResideMenuItem(this, R.mipmap.menu_history, getResources().getString(R.string.booking_history), width);
        itemNotification = new ResideMenuItem(this, R.mipmap.menu_notification, getResources().getString(R.string.notifications), width);
        itemFaq = new ResideMenuItem(this, R.mipmap.menu_faq, getResources().getString(R.string.faq), width);
        itemTerms = new ResideMenuItem(this, R.mipmap.menu_terms, getResources().getString(R.string.terms_and_conditions), width);
        itemContact = new ResideMenuItem(this, R.mipmap.menu_contact, getResources().getString(R.string.contact), width);
        itemLogout = new ResideMenuItem(this, R.mipmap.menu_logout, getResources().getString(R.string.logout), width);
        resideMenu.addMenuItem(itemHome, ResideMenu.DIRECTION_LEFT);
        resideMenu.addMenuItem(itemHistory, ResideMenu.DIRECTION_LEFT);
        resideMenu.addMenuItem(itemNotification, ResideMenu.DIRECTION_LEFT);
        resideMenu.addMenuItem(itemFaq, ResideMenu.DIRECTION_LEFT);
        resideMenu.addMenuItem(itemTerms, ResideMenu.DIRECTION_LEFT);
        resideMenu.addMenuItem(itemContact, ResideMenu.DIRECTION_LEFT);
        resideMenu.addMenuItem(itemLogout, ResideMenu.DIRECTION_LEFT);
        itemHome.setOnClickListener(this);
        itemHistory.setOnClickListener(this);
        itemNotification.setOnClickListener(this);
        itemFaq.setOnClickListener(this);
        itemTerms.setOnClickListener(this);
        itemContact.setOnClickListener(this);
        itemLogout.setOnClickListener(this);
        resideMenu.reside_headder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeFragment(new ProfileFragment(),"ProfileFragment");
                MenuScreen.resideMenu.closeMenu();
            }
        });
        resideMenu.setSwipeDirectionDisable(ResideMenu.DIRECTION_RIGHT);
        resideMenu.setSwipeDirectionDisable(ResideMenu.DIRECTION_LEFT);


    }


    private void getparm() {
        String navigation = getIntent().getStringExtra("NAVIGATE_SCREEN");
        String orderSlotid = getIntent().getStringExtra(Constants.ORDER_SLOT_ID);
        String orderId = getIntent().getStringExtra(Constants.ORDER_ID);
        if(getIntent().hasExtra("notification_id")) {
            String notificationId = getIntent().getStringExtra("notification_id");
            if (!Constants.isStringNullOrBlank(notificationId)) {
                MyFirebaseMessagingService.clearNotification(MenuScreen.this, Integer.valueOf(notificationId));
            }
        }

        if (!Constants.isStringNullOrBlank(navigation)) {

            if (navigation.equals(Constants.SEND_REQUEST)) {
                callingHome();
            } else if (navigation.equals(Constants.REQUEST_PROCEED)) {
                String customerLatitude = getIntent().getStringExtra("customer_latitude");
                String customerLongitude = getIntent().getStringExtra("customer_longitude");

                requestProceed(orderId, orderSlotid, customerLatitude,customerLongitude);
            } else if (navigation.equals(Constants.REQUEST_START_TIME)) {
                startTime();
            } else if (navigation.equals(Constants.REQUEST_CANCEL)) {
                cancelRequest();
            } else if (navigation.equals(Constants.REQUEST_END_SESSION)) {
                String obj = getIntent().getStringExtra("jsonObject");
                endSession(obj);

            } else if (navigation.equals(Constants.REQUEST_CANCEL_SESSION_BY_CUSTOMER)) {
                cancelsessionBYcustomer();
            }else if(navigation.equals(Constants.NEW_MESSAGE)){
                Intent sentintent = new Intent(MenuScreen.this, ChatActivity.class);
                sentintent.putExtra("order_id", orderId);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                startActivity(sentintent);
                finish();
            }

        }
    }


    public void setResideMenuItemIconColor(boolean itemMyHomeSelect, boolean itemPhotosSelect, boolean itemCardSelect,
                                           boolean itemHistorySelect, boolean itemNotificationSelect, boolean itemFaqSelect,
                                           boolean itemTermsSelect, boolean itemContactSelect, boolean itemLogoutSelect) {

        itemHome.setResideMenuItemSelect(itemMyHomeSelect);
        itemHistory.setResideMenuItemSelect(itemHistorySelect);
        itemNotification.setResideMenuItemSelect(itemNotificationSelect);
        itemFaq.setResideMenuItemSelect(itemFaqSelect);
        itemTerms.setResideMenuItemSelect(itemTermsSelect);
        itemContact.setResideMenuItemSelect(itemContactSelect);
        itemLogout.setResideMenuItemSelect(itemLogoutSelect);
    }


    @Override public boolean dispatchTouchEvent(MotionEvent ev)
    {
        return resideMenu.dispatchTouchEvent(ev);
    }

    @Override
    public void onClick(View view) {

        if (view == itemHome) {


            changeFragment(new HomeFragment(),"HomeFragment");
            MenuScreen.resideMenu.closeMenu();
            setResideMenuItemIconColor(true, false, false, false, false, false, false, false, false);
        } else if (view == itemHistory) {
            Fragment myFragment = getSupportFragmentManager().findFragmentByTag("BookingHistoryFragment");
            {
                if (myFragment != null && myFragment.isVisible()) {
                    MenuScreen.resideMenu.closeMenu();
                } else {
                    changeFragment(new BookingHistoryFragment(),"BookingHistoryFragment");
                    MenuScreen.resideMenu.closeMenu();
                }
            }
            setResideMenuItemIconColor(false, false, false, true, false, false, false, false, false);
        } else if (view == itemNotification) {
            Fragment myFragment = getSupportFragmentManager().findFragmentByTag("NotificationFragment");
            {
                if (myFragment != null && myFragment.isVisible()) {
                    MenuScreen.resideMenu.closeMenu();
                } else {
                    changeFragment(new NotificationFragment(),"NotificationFragment");
                    MenuScreen.resideMenu.closeMenu();
                }
            }
            setResideMenuItemIconColor(false, false, false, false, true, false, false, false, false);
        } else if (view == itemFaq) {
            Fragment myFragment = getSupportFragmentManager().findFragmentByTag("FAQFragment");
            {
                if (myFragment != null && myFragment.isVisible()) {
                    MenuScreen.resideMenu.closeMenu();
                } else {
                    changeFragment(new FAQFragment(),"FAQFragment");
                    MenuScreen.resideMenu.closeMenu();
                }
            }

            setResideMenuItemIconColor(false, false, false, false, false, true, false, false, false);
        } else if (view == itemTerms) {
            Fragment myFragment = getSupportFragmentManager().findFragmentByTag("TermsConditionFragment");
            {
                if (myFragment != null && myFragment.isVisible()) {
                    MenuScreen.resideMenu.closeMenu();
                } else {
                    changeFragment(new TermsConditionFragment(),"TermsConditionFragment");
                    MenuScreen.resideMenu.closeMenu();
                }
            }
            setResideMenuItemIconColor(false, false, false, false, false, false, true, false, false);
        } else if (view == itemContact) {
            Fragment myFragment = getSupportFragmentManager().findFragmentByTag("ContactFragment");
            {
                if (myFragment != null && myFragment.isVisible()) {
                    MenuScreen.resideMenu.closeMenu();
                } else {
                    changeFragment(new ContactFragment(),"ContactFragment");
                    MenuScreen.resideMenu.closeMenu();
                }
            }
            setResideMenuItemIconColor(false, false, false, false, false, false, false, true, false);
        } else if (view == itemLogout) {
            logoutDialog();
            setResideMenuItemIconColor(false, false, false, false, false, false, false, false, true);
        }
        resideMenu.closeMenu();
    }





    private ResideMenu.OnMenuListener menuListener = new ResideMenu.OnMenuListener() {
        @Override public void openMenu() {
            Log.e(Constants.LOG_CAT,"openMenu");
        }
        @Override public void closeMenu() {
            Log.e(Constants.LOG_CAT,"closeMenu");
        }
    };

    String str;
    private void changeFragment(Fragment targetFragment,String str) {
        resideMenu.clearIgnoredViewList();
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.main_fragment, targetFragment, str)
                .setTransitionStyle(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .commit();
    }


    @Override
    protected void onResume() {
        super.onResume();
        checkPlayServices();
        if (mGoogleApiClient.isConnected() && mRequestingLocationUpdates) {
            startLocationUpdates();
        }
        String unReadBadgeCount = AppPreference.getInstance(MenuScreen.this).getString(Constants.BADGE_COUNT);
        itemNotification.updateCount(unReadBadgeCount);


    }

    public void register() {
        IntentFilter intentFilter = new IntentFilter("ACTION_REFRESH_USER.intent.MAIN");
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent mintent) {
                String broadCastActionType = mintent.getStringExtra(Constants.BROADCAST_ACTION);
                String request = mintent.getStringExtra("REQUEST");
                String orderSlotid = mintent.getStringExtra("order_slot_id");
                String orderId = mintent.getStringExtra("order_id");

                if (broadCastActionType.equals(Constants.ACTION_REFRESH_USER)) {
                    if(!Constants.isStringNullOrBlank(request)) {
                        if (request.equals(Constants.SEND_REQUEST)) {
                            ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
                            ComponentName cn = am.getRunningTasks(1).get(0).topActivity;
                            if (cn.getClassName().equals("com.fanphotographer.activity.MenuScreen")) {
                                HomeFragment myFragment = (HomeFragment) getSupportFragmentManager().findFragmentByTag("HomeFragment");
                                if (myFragment != null && myFragment.isVisible()) {
                                    callingHome();
                                } else {
                                    changeFragment(new HomeFragment(), "HomeFragment");
                                }
                            } else {
                                Intent intent = new Intent(mContext, MenuScreen.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                                startActivity(intent);
                            }


                        } else if (request.equals(Constants.REQUEST_PROCEED)) {
                            String customerLatitude = mintent.getStringExtra("customer_latitude");
                            String customerLongitude = mintent.getStringExtra("customer_longitude");
                            requestProceed(orderId, orderSlotid, customerLatitude, customerLongitude);


                        } else if (request.equals(Constants.REQUEST_CANCEL)) {
                            cancelRequest();
                        }
                    }
                }


            }
        };
        registerReceiver(broadcastReceiver, intentFilter);
    }


    public void removeNotification(String id){
        if (!Constants.isStringNullOrBlank(id)) {
            MyFirebaseMessagingService.clearNotification(MenuScreen.this, Integer.valueOf(id));
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mGoogleApiClient.isConnected()) {
            stopLocationUpdates();
        }
    }


    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastReceiver);
    }


    public void getUserInformationApi() {

        Api api = ApiFactory.getClient(mContext).create(Api.class);
        Call<ResponseBody> call;
        call = api.getuser();

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Constants.hideProgressDialog();
                try {
                    if (response != null && response.isSuccessful() && response.code() == 200) {
                        String output = ErrorUtils.getResponseBody(response);
                        JSONObject object = new JSONObject(output);
                        if (object.optString(Constants.SUCCESS).equalsIgnoreCase(Constants.TRUE)) {
                            Log.e(Constants.LOG_CAT, "onResponse:" + object.toString());
                            JSONObject jsonObject = object.optJSONObject("data");
                            getUserBean = appPreference.parseData(jsonObject);
                            appPreference.setBoolean(Constants.IS_USER_INFORMATION_STATUS, true);
                            showProfile();
                        }
                    } else if (response != null) {
                        if (response.code() == 400 || response.code() == 500 || response.code() == 403 || response.code() == 404 || response.code() == 401) {
                            Log.e(Constants.LOG_CAT,""+response.code());

                        }
                    }
                } catch (Exception e) {
                    Log.e(Constants.LOG_CAT,e.getMessage());
                }

            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Constants.hideProgressDialog();
            }
        });
    }


    public void showProfile() {
        if (!getUserBean.profile_image.equals("")) {

            Glide.with(mContext).load(getUserBean.profile_image)
                    .thumbnail(0.5f)
                    .apply(new RequestOptions().placeholder(R.mipmap.defult_user).dontAnimate())
                    .apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL))
                    .into(resideMenu.profileImage);

        }


        resideMenu.tv_user_name.setText(getUserBean.first_name + " " + getUserBean.last_name);
        resideMenu.tv_title.setText(getUserBean.email);
    }


    public void logoutApi() {

        Api api = ApiFactory.getClientWithoutHeader(mContext).create(Api.class);
        Call<ResponseBody> call;
        String accessToken = appPreference.getString(Constants.ACCESS_TOKEN);
        call = api.logout(accessToken);
        Log.e(Constants.LOG_CAT, "HEADERS : " + call.request().headers());


        Constants.showProgressDialog(mContext, "Loading..");
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Constants.hideProgressDialog();
                try {
                    if (response != null && response.isSuccessful() && response.code() == 200) {
                        String output = ErrorUtils.getResponseBody(response);
                        JSONObject object = new JSONObject(output);
                        if (object.optString(Constants.SUCCESS).equalsIgnoreCase(Constants.TRUE)) {
                            Log.e(Constants.LOG_CAT, "onResponse:" + object.toString());
                            appPreference.clearsession();
                            AppPreference.getInstance(MenuScreen.this).setString(Constants.BADGE_COUNT, "0");
                            itemNotification.updateCount("0");
                            Intent intent = new Intent(MenuScreen.this, EnterMobileActivity.class);
                            startActivity(intent);
                            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                            finish();

                        } else if (object.optString(Constants.SUCCESS).equalsIgnoreCase(Constants.FALSE)) {
                            JSONArray jsonArray = object.optJSONArray(Constants.ERROR);
                            if (jsonArray == null) {
                                Constants.showToastAlert(object.getJSONObject(Constants.ERROR).getString(Constants.MESSAGE), mContext);
                            } else {
                                Constants.showToastAlert(jsonArray.getJSONObject(0).getString(Constants.MESSAGE), mContext);
                            }
                        }

                    } else if (response != null) {
                        if (response.code() == 400 || response.code() == 500 || response.code() == 403 || response.code() == 404 || response.code() == 401) {
                            if (response.code() == 401) {
                                appPreference.showCustomAlert(MenuScreen.this, getResources().getString(R.string.http_401_error));
                            } else {
                                Constants.showToastAlert(ErrorUtils.getHtttpCodeError(response.code()), mContext);
                            }
                        } else {
                            String responseStr = ErrorUtils.getResponseBody(response);
                            JSONObject jsonObject = new JSONObject(responseStr);
                            Constants.showToastAlert(ErrorUtils.checkJosnErrorBody(jsonObject), mContext);
                        }
                    }
                } catch (Exception e) {
                    Log.e(Constants.LOG_CAT,e.getMessage());
                }

            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Constants.hideProgressDialog();
                Constants.showToastAlert(ErrorUtils.getString(R.string.failled), mContext);
            }
        });
    }









    @Override
    public void onBackPressed() {
        long pressTime = System.currentTimeMillis();
        if (pressTime - lastPressTime <= DOUBLE_PRESS_INTERVAL) {
            finish();
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            mHasDoubleClicked = true;
        } else {     // If not double click....
            mHasDoubleClicked = false;
            @SuppressLint("HandlerLeak") Handler myHandler = new Handler( ) {
                public void handleMessage(Message m) {
                    if (!mHasDoubleClicked) {
                        Toast.makeText(getApplicationContext(), "Please tap to double for exit!!", Toast.LENGTH_SHORT).show();
                    }
                }
            };
            Message m = new Message();
            myHandler.sendMessageDelayed(m, DOUBLE_PRESS_INTERVAL);
            lastPressTime = pressTime;
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }

    }




    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        Toast.makeText(getApplicationContext(), "Location changed!", Toast.LENGTH_SHORT).show();
        displayLocation();
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        displayLocation();
        if (mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.i(Constants.LOG_CAT, "Connection failed: ConnectionResult.getErrorCode() = "
                + connectionResult.getErrorCode());

    }


    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
    }

    /**
     * Creating location request object
     */
    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        int UPDATE_INTERVAL = 10000;
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        int FATEST_INTERVAL = 5000;
        mLocationRequest.setFastestInterval(FATEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        int DISPLACEMENT = 10;
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT);
    }

    /**
     * Method to verify google play services on the device
     */
    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Toast.makeText(getApplicationContext(), "This device is not supported.", Toast.LENGTH_LONG).show();
                finish();
            }
            return false;
        }
        return true;
    }

    /**
     * Starting the location updates
     */
    protected void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);

    }

    /**
     * Stopping location updates
     */
    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient,this);
    }


    private void displayLocation() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        if (mLastLocation != null) {
             latitude = mLastLocation.getLatitude();
             longitude = mLastLocation.getLongitude();

             appPreference.setString(Constants.LATITUDE,""+latitude);
             appPreference.setString(Constants.LONGITUDE,""+longitude);
            if (AppUtils.isNetworkConnected()) {
                getUpdatelocation();
            } else {
                final ViewGroup viewGroup = (ViewGroup) ((ViewGroup) MenuScreen.this.findViewById(android.R.id.content)).getChildAt(0);
                Constants.showSnackbar(MenuScreen.this, viewGroup, getResources().getString(R.string.no_internet), Constants.RETRY);
            }

        }
    }


    public void getUpdatelocation() {

        Api api = ApiFactory.getClientWithoutHeader(mContext).create(Api.class);
        HashMap<String, String> map = new HashMap<>();
        Call<ResponseBody> call;
        map.put("latitude", String.valueOf(latitude));
        map.put("longitude", String.valueOf(longitude));

        String accessToken = appPreference.getString(Constants.ACCESS_TOKEN);
        call = api.updateLocationAPI(accessToken, map);


        Constants.showProgressDialog(mContext, Constants.LOADING);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                try {
                    if (response != null && response.isSuccessful() && response.code() == 200) {
                        String output = ErrorUtils.getResponseBody(response);
                        JSONObject object = new JSONObject(output);
                        if (object.optString(Constants.SUCCESS).equalsIgnoreCase(Constants.TRUE)) {
                            Log.e(Constants.LOG_CAT, "onResponse:" + object.toString());
                            getUserBean = appPreference.getCurrentUserInfo();
                            if (!appPreference.getBoolean(Constants.IS_USER_INFORMATION_STATUS)) {
                                getUserInformationApi();
                            } else {
                                Constants.hideProgressDialog();
                                showProfile();
                            }
                        } else if (object.optString(Constants.SUCCESS).equalsIgnoreCase(Constants.FALSE)) {
                            Constants.hideProgressDialog();
                            JSONArray jsonArray = object.optJSONArray(Constants.ERROR);
                            if (jsonArray == null) {
                                Constants.showToastAlert(object.getJSONObject(Constants.ERROR).getString(Constants.MESSAGE),mContext);
                            }else {
                                Constants.showToastAlert(jsonArray.getJSONObject(0).getString(Constants.MESSAGE), mContext);
                            }
                        }

                    }  else if (response != null) {
                        if (response.code() == 400 || response.code() == 500 || response.code() == 403 || response.code() == 404 || response.code() == 401) {
                            Constants.hideProgressDialog();
                            if(response.code()==401){
                                appPreference.showCustomAlert(MenuScreen.this,getResources().getString(R.string.http_401_error));
                            }else {
                                Constants.showToastAlert(ErrorUtils.getHtttpCodeError(response.code()), mContext);
                            }

                        } else {
                            Constants.hideProgressDialog();
                            String responseStr = ErrorUtils.getResponseBody(response);
                            JSONObject jsonObject = new JSONObject(responseStr);
                            Constants.showToastAlert(ErrorUtils.checkJosnErrorBody(jsonObject), mContext);
                        }
                    }
                }catch (Exception e){
                    Log.e(Constants.LOG_CAT,e.getMessage());
                }

            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Constants.hideProgressDialog();
                Constants.hideProgressDialog();
                Constants.showToastAlert(ErrorUtils.getString(R.string.failled), mContext);
            }
        });


    }


    public void showTimer(){
        countDownTimer =  new CountDownTimer(61 * 1000, 1000) { // adjust the milli seconds here
            public void onTick(long millisUntilFinished) {
                Log.e(Constants.LOG_CAT,"onTick");
            }
            public void onFinish() {
                hideDialog();
                callingHome();
                countDownTimer.cancel();
            }
        }.start();
    }


    public void acceptDialog() {
        acceptDailog = new Dialog(MenuScreen.this);
        acceptDailog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        acceptDailog.setCancelable(false);
        acceptDailog.setCanceledOnTouchOutside(false);
        acceptDailog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        acceptDailog.setContentView(R.layout.accept_custom_dailog);
        acceptDailog.show();
    }


    public void hideDialog() {
        if(acceptDailog!=null)
            acceptDailog.dismiss();
    }


        public void logoutDialog() {

        dialog = new Dialog(MenuScreen.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.dialog_log_out);
        TextView submit = (TextView) dialog.findViewById(R.id.button_ok);
        TextView cancel = (TextView) dialog.findViewById(R.id.button_cancel);
        submit.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dialog.dismiss();
                if (AppUtils.isNetworkConnected()) {
                    logoutApi();
                } else {
                    final ViewGroup viewGroup = (ViewGroup) ((ViewGroup) MenuScreen.this.findViewById(android.R.id.content)).getChildAt(0);
                    Constants.showSnackbar(mContext, viewGroup, getResources().getString(R.string.no_internet), "Retry");
                }



            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }


    public void callingHome(){
        if (AppUtils.isNetworkConnected()) {
            HomeFragment fragmentDemo = (HomeFragment) getSupportFragmentManager().findFragmentByTag("HomeFragment");
            if(fragmentDemo!=null) {
                fragmentDemo.AgainstillDelay();
                showTimer();
            }
        } else {
            final ViewGroup viewGroup = (ViewGroup) ((ViewGroup) MenuScreen.this.findViewById(android.R.id.content)).getChildAt(0);
            Constants.showSnackbar(mContext, viewGroup, getResources().getString(R.string.no_internet), "Retry");
        }
    }

    public void requestProceed(String morderId, String mslotId, String mcustomerLatitude,String mcustomerLongitude ){
        appPreference.setString(Constants.REQUEST_START_TIME, "");
        if(acceptDailog!=null) {
            acceptDailog.dismiss();
        }
        if(countDownTimer!=null){
            countDownTimer.cancel();
        }
        appPreference.setString(Constants.ORDER_ID,morderId);
        appPreference.setString(Constants.ORDER_SLOT_ID,mslotId);
        Intent intent = new Intent(MenuScreen.this, UserLocationActivity.class);
        appPreference.setString(Constants.CustomerLATITUDE,""+mcustomerLatitude);
        appPreference.setString(Constants.CustomerLONGITUDE,""+mcustomerLongitude);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        appPreference.setBoolean(Constants.USERLOCATION_ACTIVITY,true);
        startActivity(intent);
        finish();
    }

    public void cancelRequest(){
        appPreference.setBoolean(Constants.USERLOCATION_ACTIVITY,false);
        appPreference.setString("arriving_time","");
        appPreference.setString(Constants.ORDER_ID,"");
        appPreference.setString(Constants.ORDER_SLOT_ID,"");
        if(acceptDailog!=null)
            acceptDailog.dismiss();
        if(countDownTimer!=null){
            countDownTimer.cancel();
        }
        callingHome();
    }

    public void endSession(String mobj){
        if(!Constants.isStringNullOrBlank(mobj)) {
            Intent sentintent = new Intent(MenuScreen.this, BillDetailActivity.class);
            sentintent.putExtra("jsonObject", mobj);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            appPreference.setString(Constants.START_PHOTO, "0");
            startActivity(sentintent);
            finish();
        }
    }

    public void cancelsessionBYcustomer(){
        appPreference.setString(Constants.REQUEST_START_TIME, "");
        appPreference.setString(Constants.REQUEST_END_SESSION, "");
        appPreference.setBoolean(Constants.USERLOCATION_ACTIVITY, false);
        appPreference.setString(Constants.ORDER_ID, "");
        appPreference.setString(Constants.ORDER_SLOT_ID, "");
        Intent sentintent = new Intent(MenuScreen.this, MenuScreen.class);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        startActivity(sentintent);
        finish();
    }

    public void startTime(){
        Intent sentintent = new Intent(MenuScreen.this, UserLocationActivity.class);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        startActivity(sentintent);
        finish();
    }



}



