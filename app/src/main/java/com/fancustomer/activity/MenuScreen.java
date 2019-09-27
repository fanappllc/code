package com.fancustomer.activity;


import android.app.Dialog;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import com.fancustomer.R;
import com.fancustomer.data.constant.Constants;
import com.fancustomer.data.preference.AppPreference;
import com.fancustomer.fcm.MyFirebaseMessagingService;
import com.fancustomer.fragment.BookingHistoryFragment;
import com.fancustomer.fragment.ComplainFragment;
import com.fancustomer.fragment.ContactFragment;
import com.fancustomer.fragment.FaqFragment;
import com.fancustomer.fragment.HomeFragment;
import com.fancustomer.fragment.ManageCardFragment;
import com.fancustomer.fragment.NotificationFragment;
import com.fancustomer.fragment.PhotoFragment;
import com.fancustomer.fragment.ProfileFragment;
import com.fancustomer.fragment.TeramsAndConditionFragment;
import com.fancustomer.helper.ErrorUtils;
import com.fancustomer.helper.ExceptionHandler;
import com.fancustomer.webservice.Api;
import com.fancustomer.webservice.ApiFactory;
import com.special.ResideMenu.ResideMenu;
import com.special.ResideMenu.ResideMenuItem;

import org.json.JSONException;
import org.json.JSONObject;

import me.leolin.shortcutbadger.ShortcutBadger;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;


public class MenuScreen extends BaseActivity implements View.OnClickListener {
    private static final long DOUBLE_PRESS_INTERVAL = 250; // in millis
    private String unReadBadgeCount = "";
    public static ResideMenu resideMenu;
    private ResideMenuItem itemHome;
    private ResideMenuItem itemPhotos;
    private ResideMenuItem itemCard;
    private ResideMenuItem itemHistory;
    public static ResideMenuItem itemNotification;
    private ResideMenuItem itemFaq;
    private ResideMenuItem itemTerms;
    private ResideMenuItem itemContact;
    private ResideMenuItem itemComplain;
    private ResideMenuItem itemLogout;
    private BroadcastReceiver broadcastReceiver;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_menu);
        if (getIntent().hasExtra("COME_FROM_MENU")) {
            String comeFrom = getIntent().getStringExtra("COME_FROM_MENU");
            if (comeFrom.equals("ADD_CREDIT_CARD")) {
                if (Constants.isInternetOn(MenuScreen.this)) {
                    getUser();
                } else {
                    final ViewGroup viewGroup = (ViewGroup) ((ViewGroup) this
                            .findViewById(android.R.id.content)).getChildAt(0);
                    showSnackbar(viewGroup, getResources().getString(R.string.no_internet), Constants.RETRY);
                }
            }

        }
        getparm();
        setUpMenu();
        changeFragment(new HomeFragment(), "HomeFragment");
        setResideMenuItemIconColor(true, false, false, false, false, false, false, false, false, false);
        register();

    }


    private void getparm() {

        String navigation = getIntent().getStringExtra("NAVIGATE_SCREEN");
        Log.e(Constants.LOG_CAT, "notification:navigation " + navigation);
        String photographerId = getIntent().getStringExtra("photographer_id");
        String orderId = getIntent().getStringExtra("order_id");


        if (!Constants.isStringNullOrBlank(navigation)) {
            if (navigation.equals(Constants.REQUEST_ACCEPTED)) {
                Intent requestIntent = new Intent(MenuScreen.this, BookingConfirmed.class);
                requestIntent.putExtra("photographer_id", photographerId);
                requestIntent.putExtra("order_id", orderId);
                startActivity(requestIntent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

            } else if (navigation.equals(Constants.NEW_MESSAGE)) {
                Intent requestIntent = new Intent(MenuScreen.this, ChatActivity.class);
                requestIntent.putExtra("photographer_id", photographerId);
                requestIntent.putExtra("order_id", orderId);
                startActivity(requestIntent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

            }
        }


    }

    private void setUpMenu() {

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = displayMetrics.widthPixels;

        resideMenu = new ResideMenu(this);
        resideMenu.setBackground(R.color.white);
        resideMenu.attachToActivity(this);
        resideMenu.setMenuListener(menuListener);
        //valid scale factor is between 0.0f and 1.0f. left menu'width is 150dip.
        resideMenu.setScaleValue(0.8f);

        itemHome = new ResideMenuItem(this, R.mipmap.menu_home, getResources().getString(R.string.home), width);
        itemPhotos = new ResideMenuItem(this, R.mipmap.menu_photos, getResources().getString(R.string.my_photos), width);
        itemCard = new ResideMenuItem(this, R.mipmap.menu_my_card, getResources().getString(R.string.manage_card), width);
        itemHistory = new ResideMenuItem(this, R.mipmap.menu_history, getResources().getString(R.string.booking_history), width);
        itemNotification = new ResideMenuItem(this, R.mipmap.menu_notification, getResources().getString(R.string.notifications), width);
        itemNotification.updateCount(AppPreference.getInstance(MenuScreen.this).getString(Constants.BADGE_COUNT));
        itemFaq = new ResideMenuItem(this, R.mipmap.menu_faq, getResources().getString(R.string.faq), width);
        itemTerms = new ResideMenuItem(this, R.mipmap.menu_terms, getResources().getString(R.string.terms_and_conditions), width);
        itemContact = new ResideMenuItem(this, R.mipmap.menu_contact, getResources().getString(R.string.contact), width);
        itemComplain = new ResideMenuItem(this, R.mipmap.complaint, getResources().getString(R.string.complain), width);
        itemLogout = new ResideMenuItem(this, R.mipmap.menu_logout, getResources().getString(R.string.logout), width);

        resideMenu.addMenuItem(itemHome, ResideMenu.DIRECTION_LEFT);
        resideMenu.addMenuItem(itemPhotos, ResideMenu.DIRECTION_LEFT);
        resideMenu.addMenuItem(itemCard, ResideMenu.DIRECTION_LEFT);
        resideMenu.addMenuItem(itemHistory, ResideMenu.DIRECTION_LEFT);
        resideMenu.addMenuItem(itemNotification, ResideMenu.DIRECTION_LEFT);
        resideMenu.addMenuItem(itemFaq, ResideMenu.DIRECTION_LEFT);
        resideMenu.addMenuItem(itemTerms, ResideMenu.DIRECTION_LEFT);
        resideMenu.addMenuItem(itemContact, ResideMenu.DIRECTION_LEFT);
        resideMenu.addMenuItem(itemComplain, ResideMenu.DIRECTION_LEFT);
        resideMenu.addMenuItem(itemLogout, ResideMenu.DIRECTION_LEFT);

        itemHome.setOnClickListener(this);
        itemPhotos.setOnClickListener(this);
        itemCard.setOnClickListener(this);
        itemHistory.setOnClickListener(this);
        itemNotification.setOnClickListener(this);
        itemFaq.setOnClickListener(this);
        itemTerms.setOnClickListener(this);
        itemContact.setOnClickListener(this);
        itemComplain.setOnClickListener(this);
        itemLogout.setOnClickListener(this);
        resideMenu.setSwipeDirectionDisable(ResideMenu.DIRECTION_RIGHT);
        resideMenu.setSwipeDirectionDisable(ResideMenu.DIRECTION_LEFT);
        resideMenu.profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeFragment(new ProfileFragment(), "ProfileFragment");
                resideMenu.closeMenu();
            }
        });


    }

    public void setResideMenuItemIconColor(boolean itemMyHomeSelect, boolean itemPhotosSelect
            , boolean itemCardSelect, boolean itemHistorySelect, boolean itemNotificationSelect,
                                           boolean itemFaqSelect, boolean itemTermsSelect, boolean itemContactSelect, boolean itemComplainSelect, boolean itemLogoutSelect) {

        itemHome.setResideMenuItemSelect(itemMyHomeSelect);
        itemPhotos.setResideMenuItemSelect(itemPhotosSelect);
        itemCard.setResideMenuItemSelect(itemCardSelect);
        itemHistory.setResideMenuItemSelect(itemHistorySelect);
        itemNotification.setResideMenuItemSelect(itemNotificationSelect);
        itemFaq.setResideMenuItemSelect(itemFaqSelect);
        itemTerms.setResideMenuItemSelect(itemTermsSelect);
        itemContact.setResideMenuItemSelect(itemContactSelect);
        itemComplain.setResideMenuItemSelect(itemComplainSelect);
        itemLogout.setResideMenuItemSelect(itemLogoutSelect);

    }


    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return resideMenu.dispatchTouchEvent(ev);
    }

    @Override
    public void onClick(View view) {
        if (view == itemHome) {
            changeFragment(new HomeFragment(), "HomeFragment");
            resideMenu.closeMenu();

            setResideMenuItemIconColor(true, false, false, false, false, false, false, false, false, false);
        } else if (view == itemPhotos) {
            Fragment myFragment = getSupportFragmentManager().findFragmentByTag("PhotoFragment");

            if (myFragment != null && myFragment.isVisible()) {
                resideMenu.closeMenu();
            } else {
                changeFragment(new PhotoFragment(), "PhotoFragment");
                resideMenu.closeMenu();
            }

            setResideMenuItemIconColor(false, true, false, false, false, false, false, false, false, false);
        } else if (view == itemCard) {
            Fragment fragment = getSupportFragmentManager()
                    .findFragmentByTag("ManageCardFragment");

            if (fragment != null && fragment.isVisible()) {
                resideMenu.closeMenu();

            } else {
                changeFragment(new ManageCardFragment(), "ManageCardFragment");
                resideMenu.closeMenu();
            }

            setResideMenuItemIconColor(false, false, true, false, false, false, false, false, false, false);
        } else if (view == itemHistory) {
            Fragment fragment = getSupportFragmentManager()
                    .findFragmentByTag("BookingHistoryFragment");

            if (fragment != null && fragment.isVisible()) {
                resideMenu.closeMenu();
            } else {
                changeFragment(new BookingHistoryFragment(), "BookingHistoryFragment");
                resideMenu.closeMenu();
            }

            setResideMenuItemIconColor(false, false, false, true, false, false, false, false, false, false);
        } else if (view == itemNotification) {
            Fragment fragment = getSupportFragmentManager()
                    .findFragmentByTag("NotificationFragment");

            if (fragment != null && fragment.isVisible()) {
                resideMenu.closeMenu();
            } else {
                changeFragment(new NotificationFragment(), "NotificationFragment");
                resideMenu.closeMenu();
            }

            setResideMenuItemIconColor(false, false, false, false, true, false, false, false, false, false);
        } else if (view == itemFaq) {

            Fragment fragment = getSupportFragmentManager()
                    .findFragmentByTag("FaqFragment");

            if (fragment != null && fragment.isVisible()) {
                resideMenu.closeMenu();
            } else {
                changeFragment(new FaqFragment(), "FaqFragment");
                resideMenu.closeMenu();
            }

            setResideMenuItemIconColor(false, false, false, false, false, true, false, false, false, false);
        } else if (view == itemTerms) {
            Fragment fragment = getSupportFragmentManager()
                    .findFragmentByTag("TeramsAndConditionFragment");

            if (fragment != null && fragment.isVisible()) {
                resideMenu.closeMenu();
            } else {
                changeFragment(new TeramsAndConditionFragment(), "TeramsAndConditionFragment");
                resideMenu.closeMenu();
            }

            setResideMenuItemIconColor(false, false, false, false, false, false, true, false, false, false);
        } else if (view == itemContact) {

            Fragment fragment = getSupportFragmentManager()
                    .findFragmentByTag("ContactFragment");

            if (fragment != null && fragment.isVisible()) {
                resideMenu.closeMenu();
            } else {
                changeFragment(new ContactFragment(), "ContactFragment");
                resideMenu.closeMenu();
            }

            setResideMenuItemIconColor(false, false, false, false, false, false, false, true, false, false);
        } else if (view == itemComplain) {
            Fragment fragment = getSupportFragmentManager()
                    .findFragmentByTag("ComplainFragment");

            if (fragment != null && fragment.isVisible()) {
                resideMenu.closeMenu();
            } else {
                changeFragment(new ComplainFragment(), "ComplainFragment");
                resideMenu.closeMenu();
            }

            setResideMenuItemIconColor(false, false, false, false, false, false, false, false, true, false);
        } else if (view == itemLogout) {
            showLogoutDialog();
            setResideMenuItemIconColor(false, false, false, false, false, false, false, false, false, true);
        }
        resideMenu.closeMenu();
    }


    @Override
    protected void onResume() {
        super.onResume();
        resideMenu.updateProfile(AppPreference.getInstance(MenuScreen.this).getString(Constants.PROFILE_PIC),
                AppPreference.getInstance(MenuScreen.this).getString(Constants.FIRST_NAME)
                        + " " + AppPreference.getInstance(MenuScreen.this).getString(Constants.LAST_NAME),
                AppPreference.getInstance(MenuScreen.this).getString(Constants.MOBILE));

    }

    public void unRegister() {
        try {
            unregisterReceiver(broadcastReceiver);
        } catch (Exception e) {
            ExceptionHandler.printStackTrace(e);
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unRegister();
    }


    public void register() {
        IntentFilter intentFilter = new IntentFilter("ACTION_REFRESH_USER.intent.MAIN");
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String broadCastActionType = intent.getStringExtra(Constants.BROADCAST_ACTION);
                String requestAccepted = intent.getStringExtra("REQUEST_ACCEPTED");
                String photographerId = intent.getStringExtra("photographer_id");
                String orderId = intent.getStringExtra("order_id");
                String notificationId = intent.getStringExtra("notification_id");
                if (!Constants.isStringNullOrBlank(broadCastActionType)) {
                    if (broadCastActionType.equals(Constants.ACTION_REFRESH_USER)) {
                        unReadBadgeCount = AppPreference.getInstance(MenuScreen.this).getString(Constants.BADGE_COUNT);
                        itemNotification.updateCount(unReadBadgeCount);
                        if (!Constants.isStringNullOrBlank(requestAccepted)) {
                            if (requestAccepted.equals(Constants.REQUEST_ACCEPTED)) {
                                Intent requestIntent = new Intent(MenuScreen.this, BookingConfirmed.class);
                                requestIntent.putExtra("photographer_id", photographerId);
                                requestIntent.putExtra("order_id", orderId);
                                startActivity(requestIntent);
                                if (!Constants.isStringNullOrBlank(notificationId)) {
                                    MyFirebaseMessagingService.clearNotification(MenuScreen.this, Integer.valueOf(notificationId));
                                }

                            }
                        }


                    }
                }


            }
        };
        registerReceiver(broadcastReceiver, intentFilter);


    }

    private void showLogoutDialog() {
        final Dialog dialog = new Dialog(MenuScreen.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.dialog_log_out);
        TextView textViewYes = dialog.findViewById(R.id.button_ok);
        TextView textViewNo = dialog.findViewById(R.id.button_cancel);
        textViewYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                if (Constants.isInternetOn(MenuScreen.this)) {
                    logOutApi();
                } else {
                    final ViewGroup viewGroup = (ViewGroup) ((ViewGroup) MenuScreen.this.findViewById(android.R.id.content)).getChildAt(0);
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


    public void logOutApi() {
        Api api = ApiFactory.getClientWithoutHeader(MenuScreen.this).create(Api.class);
        Call<ResponseBody> call;
        String accessToken = AppPreference.getInstance(MenuScreen.this).getString(Constants.ACCESS_TOKEN);
        call = api.logOutApi(accessToken);
        Log.e(Constants.LOG_CAT, "API REQUEST LOG OUT ------------------->>>>>" + " " + call.request().url());
        Log.e(Constants.LOG_CAT, "HEADERS : " + call.request().headers());
        Constants.showProgressDialog(MenuScreen.this, Constants.LOADING);
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
                            Log.e(Constants.LOG_CAT, "API REQUEST LOG OUT Response================>>>>>" + object.toString());
                            AppPreference.getInstance(MenuScreen.this).clearPref();
                            ShortcutBadger.removeCount(MenuScreen.this);
                            Intent intent = new Intent(MenuScreen.this, EnterMobileActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK
                                    | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                            finish();

                        } else if (object.optString(Constants.SUCCESS).equalsIgnoreCase(Constants.FALSE)) {
                            ErrorUtils.showFalseMessage(object, MenuScreen.this);
                        }
                    } else if (response.code() == 400 || response.code() == 500 || response.code() == 403 || response.code() == 404 || response.code() == 401) {
                        if (response.code() == 401) {
                            Constants.showSessionExpireAlert(MenuScreen.this);
                        } else {
                            Constants.showToastAlert(ErrorUtils.getHtttpCodeError(response.code()), MenuScreen.this);
                        }

                    } else {
                        String responseStr = ErrorUtils.getResponseBody(response);
                        JSONObject jsonObject = new JSONObject(responseStr);
                        Constants.showToastAlert(ErrorUtils.checkJosnErrorBody(jsonObject), MenuScreen.this);
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

    public void getUser() {


        Api api = ApiFactory.getClientWithoutHeader(MenuScreen.this).create(Api.class);
        Call<ResponseBody> call;
        call = api.getUserApi(AppPreference.getInstance(MenuScreen.this).getString(Constants.ACCESS_TOKEN));
        Log.e(Constants.LOG_CAT, "API REQUEST USER LOGIN ------------------->>>>>:" + " " + call.request().url());
        Log.e(Constants.LOG_CAT, "HEADERS : " + call.request().headers());


        Constants.showProgressDialog(MenuScreen.this, "Loading");
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
                            Log.e(Constants.LOG_CAT, "onResponse: USRE PROFILE=" + object.toString());
                            JSONObject jsonObject = object.optJSONObject("data");
                            AppPreference.getInstance(MenuScreen.this).setString(Constants.IS_USER_REGISTERED, jsonObject.optString("is_user_registered"));
                            AppPreference.getInstance(MenuScreen.this).setString(Constants.IS_TERMS_CONDITION_ACCEPTED, jsonObject.optString("is_terms_condition_accepted"));
                            AppPreference.getInstance(MenuScreen.this).setString(Constants.IS_PROFILE_UPDATE, jsonObject.optString("is_profile_updated"));
                            AppPreference.getInstance(MenuScreen.this).setString(Constants.IS_USER_ACCOUNT_ADDED, jsonObject.optString("is_user_account_added"));

                            AppPreference.getInstance(MenuScreen.this).setString(Constants.FIRST_NAME, jsonObject.optString("first_name"));
                            AppPreference.getInstance(MenuScreen.this).setString(Constants.LAST_NAME, jsonObject.optString("last_name"));
                            AppPreference.getInstance(MenuScreen.this).setString(Constants.MOBILE, jsonObject.optString("mobile"));
                            AppPreference.getInstance(MenuScreen.this).setString(Constants.EMAIL, jsonObject.optString("email"));
                            AppPreference.getInstance(MenuScreen.this).setString(Constants.PROFILE_PIC, jsonObject.optString("profile_image"));
                            AppPreference.getInstance(MenuScreen.this).setString(Constants.USER_ROLE, jsonObject.optString("role"));
                            resideMenu.updateProfile(AppPreference.getInstance(MenuScreen.this).getString(Constants.PROFILE_PIC),
                                    AppPreference.getInstance(MenuScreen.this).getString(Constants.FIRST_NAME)
                                            + " " + AppPreference.getInstance(MenuScreen.this).getString(Constants.LAST_NAME),
                                    AppPreference.getInstance(MenuScreen.this).getString(Constants.MOBILE));

                        } else if (object.optString(Constants.SUCCESS).equalsIgnoreCase(Constants.FALSE)) {
                            ErrorUtils.showFalseMessage(object, MenuScreen.this);
                        }
                    } else if (response.code() == 400 || response.code() == 500 || response.code() == 403 || response.code() == 404 || response.code() == 401) {
                        if (response.code() == 401) {
                            Constants.showSessionExpireAlert(MenuScreen.this);
                        } else {
                            Constants.showToastAlert(ErrorUtils.getHtttpCodeError(response.code()), MenuScreen.this);
                        }

                    } else {
                        String responseStr = ErrorUtils.getResponseBody(response);
                        JSONObject jsonObject = new JSONObject(responseStr);
                        Constants.showToastAlert(ErrorUtils.checkJosnErrorBody(jsonObject), MenuScreen.this);
                    }
                } catch (Exception e) {
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

    boolean doubleBackToExitPressedOnce = false;

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(getApplicationContext(), "Double tap to exit.", Toast.LENGTH_SHORT).show();
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 2000);
    }


    private ResideMenu.OnMenuListener menuListener = new ResideMenu.OnMenuListener() {
        @Override
        public void openMenu() {
            Log.e(Constants.LOG_CAT, "openMenu: ");
        }

        @Override
        public void closeMenu() {
            Log.e(Constants.LOG_CAT, "openMenu: ");
        }
    };

    private void changeFragment(Fragment targetFragment, String str) {
        resideMenu.clearIgnoredViewList();
        Log.e("Done...................", "onWorked: resume ");
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.main_fragment, targetFragment, str)
                .setTransitionStyle(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .commit();
    }

    @Override
    protected void networkConnnectivityChange() {
        super.networkConnnectivityChange();
        if (!Constants.isInternetOn(MenuScreen.this)) {
            final ViewGroup viewGroup = (ViewGroup) ((ViewGroup) this
                    .findViewById(android.R.id.content)).getChildAt(0);
            showSnackbar(viewGroup, getResources().getString(R.string.no_internet), Constants.RETRY);
        } else {

            hideSnackbar();
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.main_fragment);
        fragment.onActivityResult(requestCode, resultCode, data);
    }

    // What good method is to access resideMenu？
    public ResideMenu getResideMenu() {
        return resideMenu;
    }

}

