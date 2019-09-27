package com.fancustomer.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.fancustomer.R;
import com.fancustomer.data.constant.Constants;
import com.fancustomer.helper.ErrorUtils;
import com.fancustomer.helper.PinEntryEditText;
import com.fancustomer.webservice.Api;
import com.fancustomer.webservice.ApiFactory;
import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

import static com.fancustomer.activity.EnterMobileActivity.enterMobileActivity;


public class VerifyMobileActivity extends BaseActivity implements View.OnClickListener {

    String mobileStr = "";
    private TextView resendTextView;
    private String countryCode;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_mobile);
        getParm();
        setToolBar();
        initView();
    }

    private void getParm() {
        countryCode = getIntent().getStringExtra("countryCode");
        mobileStr = getIntent().getStringExtra("mobile");


    }

    private void initView() {
        resendTextView = (TextView) findViewById(R.id.resendTextView);
        resendTextView.setOnClickListener(this);
        TextView mobileTextView = (TextView) findViewById(R.id.mobileTextView);
        PinEntryEditText pinVerifyCode = (PinEntryEditText) findViewById(R.id.pinVerifyCode);
        pinVerifyCode.addTextChangedListener(watch);
        mobileTextView.setText("We have sent an One Time Password on your number" + countryCode + " " + mobileStr);

//        We have sent an One Time Password on your number +1 8109824535
    }

    private void setToolBar() {

        RelativeLayout toolBarLeft = (RelativeLayout) findViewById(R.id.toolBarLeft);
        TextView headerTextView = (TextView) findViewById(R.id.hedertextview);
        headerTextView.setText(VerifyMobileActivity.this.getResources().getString(R.string.verifying_your_number));
        toolBarLeft.setOnClickListener(listener);
    }


    public void verifyOPT(String opt) {
        String deviceToken = FirebaseInstanceId.getInstance().getToken();
        if (deviceToken == null) {
            deviceToken = FirebaseInstanceId.getInstance().getToken();
        }

        Api api = ApiFactory.getClient(VerifyMobileActivity.this).create(Api.class);
        HashMap<String, String> map = new HashMap<>();
        Call<ResponseBody> call;

        map.put("mobile", mobileStr);
        map.put("otp", opt);
        map.put("device_id", deviceToken);
        map.put("device_type", "android");
        map.put("certification_type", "");

        Log.e(Constants.LOG_CAT, "deviceToken==================>>>>>>>>>>>>>>>: " + deviceToken);


        call = api.verifyOTP(map);
        Log.e(Constants.LOG_CAT, "API REQUEST USER LOGIN ------------------->>>>>:" + map + " " + call.request().url());
        Log.e(Constants.LOG_CAT, "HEADERS : " + call.request().headers());


        Constants.showProgressDialog(VerifyMobileActivity.this, "Login");
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
                            Log.e(Constants.LOG_CAT, "onResponse:" + object.toString());

                            JSONObject jsonObject = object.optJSONObject("data");

                            String accesstoken = jsonObject.optString("access-token");
                            appPreference.setString(Constants.ACCESS_TOKEN, accesstoken);
                            String unreadNotificationCount = jsonObject.optString("unread_notification_count");
                            String userId = jsonObject.optString("user_id");
                            appPreference.setString(Constants.USER_ID, userId);
                            appPreference.setString(Constants.BADGE_COUNT, unreadNotificationCount);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (Constants.isInternetOn(VerifyMobileActivity.this)) {
                                        getUser();
                                    } else {
                                        final ViewGroup viewGroup = (ViewGroup) ((ViewGroup) VerifyMobileActivity.this.findViewById(android.R.id.content)).getChildAt(0);
                                        showSnackbar(viewGroup, getResources().getString(R.string.no_internet), Constants.RETRY);
                                    }
                                }
                            });


                        } else if (object.optString(Constants.SUCCESS).equalsIgnoreCase(Constants.FALSE)) {
                            ErrorUtils.showFalseMessage(object, VerifyMobileActivity.this);
                        }
                    } else if (response.code() == 400 || response.code() == 500 || response.code() == 403 || response.code() == 404 || response.code() == 401) {
                        if (response.code() == 401) {
                            Constants.showSessionExpireAlert(VerifyMobileActivity.this);
                        } else {
                            Constants.showToastAlert(ErrorUtils.getHtttpCodeError(response.code()), VerifyMobileActivity.this);
                        }

                    } else {
                        String responseStr = ErrorUtils.getResponseBody(response);
                        JSONObject jsonObject = new JSONObject(responseStr);
                        Constants.showToastAlert(ErrorUtils.checkJosnErrorBody(jsonObject), VerifyMobileActivity.this);
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

    @Override
    protected void networkConnnectivityChange() {
        super.networkConnnectivityChange();
        if (!Constants.isInternetOn(VerifyMobileActivity.this)) {
            final ViewGroup viewGroup = (ViewGroup) ((ViewGroup) VerifyMobileActivity.this.findViewById(android.R.id.content)).getChildAt(0);
            showSnackbar(viewGroup, getResources().getString(R.string.no_internet), Constants.RETRY);
        } else {
            hideSnackbar();
        }
    }

    public void getUser() {


        Api api = ApiFactory.getClientWithoutHeader(VerifyMobileActivity.this).create(Api.class);
        Call<ResponseBody> call;

        call = api.getUserApi(appPreference.getString(Constants.ACCESS_TOKEN));
        Log.e(Constants.LOG_CAT, "API REQUEST USER LOGIN ------------------->>>>>:" + call.request().url());
        Log.e(Constants.LOG_CAT, "HEADERS : " + call.request().headers());


        Constants.showProgressDialog(VerifyMobileActivity.this, "Login");
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
                            appPreference.setString(Constants.IS_USER_REGISTERED, jsonObject.optString("is_user_registered"));
                            appPreference.setString(Constants.IS_TERMS_CONDITION_ACCEPTED, jsonObject.optString("is_terms_condition_accepted"));
                            appPreference.setString(Constants.IS_PROFILE_UPDATE, jsonObject.optString("is_profile_updated"));
                            appPreference.setString(Constants.IS_USER_ACCOUNT_ADDED, jsonObject.optString("is_user_account_added"));
                            appPreference.setString(Constants.FIRST_NAME, jsonObject.optString("first_name"));
                            appPreference.setString(Constants.LAST_NAME, jsonObject.optString("last_name"));
                            appPreference.setString(Constants.MOBILE, jsonObject.optString("mobile"));
                            appPreference.setString(Constants.EMAIL, jsonObject.optString("email"));
                            appPreference.setString(Constants.PROFILE_PIC, jsonObject.optString("profile_image"));
                            appPreference.setString(Constants.USER_ROLE, jsonObject.optString("role"));

                            if (appPreference.getString(Constants.IS_USER_REGISTERED).equals("1")) {
                                Intent intent = new Intent(VerifyMobileActivity.this, MenuScreen.class);
                                startActivity(intent);
                                finish();

                            } else {
                                Intent intent = new Intent(VerifyMobileActivity.this, TermsAndConditionsActivity.class);
                                intent.putExtra("mobile", jsonObject.optString("mobile"));
                                startActivity(intent);
                                finish();
                            }


                        } else if (object.optString(Constants.SUCCESS).equalsIgnoreCase(Constants.FALSE)) {
                            ErrorUtils.showFalseMessage(object, VerifyMobileActivity.this);
                        }
                    } else if (response.code() == 400 || response.code() == 500 || response.code() == 403 || response.code() == 404 || response.code() == 401) {
                        if (response.code() == 401) {
                            Constants.showSessionExpireAlert(VerifyMobileActivity.this);
                        } else {
                            Constants.showToastAlert(ErrorUtils.getHtttpCodeError(response.code()), VerifyMobileActivity.this);
                        }

                    } else {
                        String responseStr = ErrorUtils.getResponseBody(response);
                        JSONObject jsonObject = new JSONObject(responseStr);
                        Constants.showToastAlert(ErrorUtils.checkJosnErrorBody(jsonObject), VerifyMobileActivity.this);
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

    View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            int i = view.getId();
            if (i == R.id.toolBarLeft) {
                finish();

            }

        }
    };

    TextWatcher watch = new TextWatcher() {

        @Override
        public void afterTextChanged(Editable arg0) {
            // TODO Auto-generated method stub

        }

        @Override
        public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
            // TODO Auto-generated method stub
        }

        @Override
        public void onTextChanged(CharSequence s, int a, int b, int c) {
            // TODO Auto-generated method stub

            //

            if (a == 3) {

                if (Constants.isInternetOn(VerifyMobileActivity.this)) {
                    verifyOPT(String.valueOf(s));

                } else {
                    final ViewGroup viewGroup = (ViewGroup) ((ViewGroup) VerifyMobileActivity.this.findViewById(android.R.id.content)).getChildAt(0);
                    showSnackbar(viewGroup, getResources().getString(R.string.no_internet), Constants.RETRY);
                }


            }


        }
    };

    public void sendOTP(final String mobileStr) {

        Api api = ApiFactory.getClient(VerifyMobileActivity.this).create(Api.class);
        HashMap<String, String> map = new HashMap<>();
        Call<ResponseBody> call;

        map.put("country_code", countryCode);
        map.put("mobile", mobileStr);
        map.put("role", "customer");
        Log.e(Constants.LOG_CAT, "sendOTP: countryCode" + countryCode);

        call = api.sendOTP(map);
        Log.e(Constants.LOG_CAT, "API REQUEST USER LOGIN------------------->>>>>" + map + " " + call.request().url());
        Log.e(Constants.LOG_CAT, "HEADERS" + call.request().headers());


        Constants.showProgressDialog(VerifyMobileActivity.this, "Login");
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Constants.hideProgressDialog();
                try {
                    if (response.isSuccessful()) {

                        String output = ErrorUtils.getResponseBody(response);
                        JSONObject object = new JSONObject(output);
                        Log.d("tag", object.toString(1));

                        if (object.optString("success").equalsIgnoreCase("true")) {
                            Constants.showToastAlert("OTP send successfully!!", VerifyMobileActivity.this);
                        } else if (object.optString(Constants.SUCCESS).equalsIgnoreCase(Constants.FALSE)) {
                            ErrorUtils.showFalseMessage(object, VerifyMobileActivity.this);
                        }
                    } else if (response.code() == 400 || response.code() == 500 || response.code() == 403 || response.code() == 404 || response.code() == 401) {
                        if (response.code() == 401) {
                            Constants.showSessionExpireAlert(VerifyMobileActivity.this);
                        } else {
                            Constants.showToastAlert(ErrorUtils.getHtttpCodeError(response.code()), VerifyMobileActivity.this);
                        }

                    } else {
                        String responseStr = ErrorUtils.getResponseBody(response);
                        JSONObject jsonObject = new JSONObject(responseStr);
                        Constants.showToastAlert(ErrorUtils.checkJosnErrorBody(jsonObject), VerifyMobileActivity.this);
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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.resendTextView:
                if (Constants.isInternetOn(VerifyMobileActivity.this)) {
                    sendOTP(mobileStr);
                } else {
                    final ViewGroup viewGroup = (ViewGroup) ((ViewGroup) VerifyMobileActivity.this.findViewById(android.R.id.content)).getChildAt(0);
                    showSnackbar(viewGroup, getResources().getString(R.string.no_internet), Constants.RETRY);
                }

                break;
        }

    }
}
