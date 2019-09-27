package com.fanphotographer.activity;

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
import com.fanphotographer.R;
import com.fanphotographer.bean.GetUserBean;
import com.fanphotographer.data.constant.Constants;
import com.fanphotographer.data.preference.AppPreference;
import com.fanphotographer.fcm.MyFirebaseInstanceIDService;
import com.fanphotographer.helper.PinEntryEditText;
import com.fanphotographer.utility.ActivityUtils;
import com.fanphotographer.utility.AppUtils;
import com.fanphotographer.utility.ErrorUtils;
import com.fanphotographer.utility.KeyboardUtils;
import com.fanphotographer.webservice.Api;
import com.fanphotographer.webservice.ApiFactory;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.HashMap;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VerifyMobileActivity extends BaseActivity {

    private String mobileStr = "";
    private String code = "";
    private Context mContext = this;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_mobile);
        getParm();
        setToolBar();
        initView();
    }

    private void getParm() {
        mobileStr = getIntent().getStringExtra(Constants.MOBILE);
        code = getIntent().getStringExtra(Constants.COUNTRY_CODE);
    }

    private void initView() {
        TextView mobileTextView = (TextView) findViewById(R.id.mobileTextView);
        TextView resendTextView = (TextView) findViewById(R.id.text_resend);
        resendTextView.setOnClickListener(listener);
        PinEntryEditText pinVerifyCode = (PinEntryEditText) findViewById(R.id.pinVerifyCode);
        pinVerifyCode.addTextChangedListener(watch);
        mobileTextView.setText("We have sent an One Time Password on your number "+code+" "+ mobileStr);

    }

    private void setToolBar() {
        RelativeLayout toolBarLeft = (RelativeLayout) findViewById(R.id.toolBarLeft);
        TextView headerTextView = (TextView) findViewById(R.id.hedertextview);
        headerTextView.setText(mContext.getResources().getString(R.string.verifying_your_number));
        toolBarLeft.setOnClickListener(listener);
    }



    View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            int i = view.getId();
            if (i == R.id.toolBarLeft) {
                finish();
            }else if(i == R.id.text_resend){
                KeyboardUtils.hideKeyboard(VerifyMobileActivity.this);
                if(!AppUtils.isNetworkConnected()) {
                    final ViewGroup viewGroup = (ViewGroup) ((ViewGroup) VerifyMobileActivity.this.findViewById(android.R.id.content)).getChildAt(0);
                    Constants.showSnackbar(mContext, viewGroup, getResources().getString(R.string.no_internet), "Retry");
                    return;
                }else {
                    sendOTP(mobileStr,code);
                }
            }

        }
    };

    TextWatcher watch = new TextWatcher() {

        @Override public void afterTextChanged(Editable arg0) {
            Log.e(Constants.LOG_CAT,"afterTextChanged");
        }
        @Override public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
            Log.e(Constants.LOG_CAT,"beforeTextChanged");
        }
        @Override
        public void onTextChanged(CharSequence s, int a, int b, int c) {
            if (a == 3) {
                KeyboardUtils.hideKeyboard(VerifyMobileActivity.this);
                if (AppUtils.isNetworkConnected()) {
                    verifyOPT(String.valueOf(s));
                } else {
                    final ViewGroup viewGroup = (ViewGroup) ((ViewGroup) VerifyMobileActivity.this.findViewById(android.R.id.content)).getChildAt(0);
                    Constants.showSnackbar(mContext, viewGroup, getResources().getString(R.string.no_internet), "Retry");
                }

            }
        }
    };


    public void verifyOPT(String opt)  {

        Api api = ApiFactory.getClient(mContext).create(Api.class);
        HashMap<String, String> map = new HashMap<>();
        Call<ResponseBody> call ;
        map.put("mobile", mobileStr);
        map.put("otp", opt);
        map.put("device_id", MyFirebaseInstanceIDService.getCustomToken());
        map.put("device_type", "android");
        map.put("certification_type", "");
        call = api.verifyOTP(map);

        Constants.showProgressDialog(mContext, Constants.LOADING);
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
                            String accessToken = jsonObject.optString("access-token");
                            String isRegistered = jsonObject.optString("is_user_registered");
                            String notificationCount = jsonObject.optString("unread_notification_count");
                            AppPreference.getInstance(VerifyMobileActivity.this).setString(Constants.BADGE_COUNT, notificationCount);
                            appPreference.setString(Constants.ACCESS_TOKEN, accessToken);
                            appPreference.setString(Constants.IS_USER_REGISTERED, isRegistered);
                             appPreference.setString(Constants.ID,jsonObject.optString("user_id"));
                            if(isRegistered.equalsIgnoreCase("0")){
                                getUserinformationapi();
                            }else {
                                Intent intent = new Intent(mContext, MenuScreen.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                                startActivity(intent);
                            }
                        } else if (object.optString(Constants.SUCCESS).equalsIgnoreCase(Constants.FALSE)) {
                            JSONArray jsonArray = object.optJSONArray(Constants.ERROR);
                            if (jsonArray == null) {
                                Constants.showToastAlert(object.getJSONObject(Constants.ERROR).getString(Constants.MESSAGE),mContext);
                            }else {
                                Constants.showToastAlert(jsonArray.getJSONObject(0).getString(Constants.MESSAGE), mContext);
                            }

                        }

                    }  else if (response != null) {
                        if (response.code() == 400 || response.code() == 500 || response.code() == 403 || response.code() == 404 || response.code() == 401) {
                            if(response.code()==401){
                                appPreference.showCustomAlert(VerifyMobileActivity.this,getResources().getString(R.string.http_401_error));

                            }else {
                                Constants.showToastAlert(ErrorUtils.getHtttpCodeError(response.code()), mContext);
                            }
                        } else {
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
                Constants.showToastAlert(ErrorUtils.getString(R.string.failled), mContext);
            }
        });
    }


    public void getUserinformationapi()  {

        Api api = ApiFactory.getClient(mContext).create(Api.class);
        Call<ResponseBody> call;
        call = api.getuser();

        Constants.showProgressDialog(mContext, Constants.LOADING);
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
                            GetUserBean getUserBean = appPreference.parseData(jsonObject);
                            String IS_TERMS_CONDITION_ACCEPTED =  appPreference.getString(Constants.IS_TERMS_CONDITION_ACCEPTED);
                            String IS_PROFILE_UPDATED =  appPreference.getString(Constants.IS_PROFILE_UPDATED);
                            String SSN_NO =  appPreference.getString(Constants.SSN_NO);
                            String DRIVING_LICENCE_IMAGE =  appPreference.getString(Constants.DRIVING_LICENCE_IMAGE);
                            String IS_USER_ACCOUNTED_ADDED =  appPreference.getString(Constants.IS_USER_ACCOUNTED_ADDED);
                            String IS_REGISTRATION_FEE_PAID =  appPreference.getString(Constants.IS_REGISTRATION_FEE_PAID);
                            String IS_USER_REGISTERED =  appPreference.getString(Constants.IS_USER_REGISTERED);
                            String is_approved =  appPreference.getString(Constants.IS_APPROVED);
//
//                            if (IS_TERMS_CONDITION_ACCEPTED.equals("0")) {
//                                ActivityUtils.getInstance().sendFlowAnother(VerifyMobileActivity.this,TermsAndConditionsActivity.class);
//                            }else
//                                if (IS_PROFILE_UPDATED.equals("0")  ){
//                                    ActivityUtils.getInstance().sendFlowAnother(VerifyMobileActivity.this,UpdateProfileActivity.class);
//                            }else  if (SSN_NO.equalsIgnoreCase("null")){
//                                    ActivityUtils.getInstance().sendFlowAnother(VerifyMobileActivity.this,SsnActivity.class);
//                            }else  if (DRIVING_LICENCE_IMAGE.equalsIgnoreCase("null")){
//                                    ActivityUtils.getInstance().sendFlowAnother(VerifyMobileActivity.this,IdProofActivity.class);
//                            }else  if (IS_USER_ACCOUNTED_ADDED.equals("0") ){
//                                    ActivityUtils.getInstance().sendFlowAnother(VerifyMobileActivity.this,AccountDetailActivity.class);
//                            }else  if (IS_REGISTRATION_FEE_PAID.equals("0") ){
//                                    ActivityUtils.getInstance().sendFlowAnother(VerifyMobileActivity.this,RegistrationFeeActivity.class);
//                            }else if (IS_USER_REGISTERED.equals("0")){
//                                if (is_approved.equals("0")){
//                                    ActivityUtils.getInstance().sendFlowAnother(VerifyMobileActivity.this,WaitingActivity.class);
//                                }
//                            }

                            if (IS_REGISTRATION_FEE_PAID.equals("1")) {
                                ActivityUtils.getInstance().sendFlow(VerifyMobileActivity.this,WaitingActivity.class);
                            }else {
                                ActivityUtils.getInstance().sendFlowAnother(VerifyMobileActivity.this,TermsAndConditionsActivity.class);
                            }

                        } else if (object.optString(Constants.SUCCESS).equalsIgnoreCase(Constants.FALSE)) {
                            JSONArray jsonArray = object.optJSONArray(Constants.ERROR);
                            if (jsonArray == null) {
                                Constants.showToastAlert(object.getJSONObject(Constants.ERROR).getString(Constants.MESSAGE),mContext);
                            }else {
                                Constants.showToastAlert(jsonArray.getJSONObject(0).getString(Constants.MESSAGE), mContext);
                            }
                        }

                    }  else if (response != null) {
                        if (response.code() == 400 || response.code() == 500 || response.code() == 403 || response.code() == 404 || response.code() == 401) {
                            if(response.code()==401){
                                appPreference.showCustomAlert(VerifyMobileActivity.this,getResources().getString(R.string.http_401_error));

                            }else {
                                Constants.showToastAlert(ErrorUtils.getHtttpCodeError(response.code()), mContext);
                            }
                        } else {
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
                Constants.showToastAlert(ErrorUtils.getString(R.string.failled), mContext);
            }
        });
    }





    public void sendOTP(final String mobileStr, final String code) {

        Api api = ApiFactory.getClient(mContext).create(Api.class);
        HashMap<String, String> map = new HashMap<>();
        Call<ResponseBody> call;
        map.put("country_code", code);
        map.put("mobile", mobileStr);
        map.put("role", "photographer");
        call = api.sendOTP(map);


        Constants.showProgressDialog(mContext, Constants.LOADING);
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
                            Constants.showToastAlert(object.optJSONObject("data").optString("message"), mContext);

                        } else if (object.optString(Constants.SUCCESS).equalsIgnoreCase(Constants.FALSE)) {
                            JSONArray jsonArray = object.optJSONArray(Constants.ERROR);
                            if (jsonArray == null) {
                                Constants.showToastAlert(object.getJSONObject(Constants.ERROR).getString(Constants.MESSAGE),mContext);
                            }else {
                                Constants.showToastAlert(jsonArray.getJSONObject(0).getString(Constants.MESSAGE), mContext);
                            }
                        }

                    } else if (response != null) {
                        if (response.code() == 400 || response.code() == 500 || response.code() == 403 || response.code() == 404 || response.code() == 401) {
                            Constants.showToastAlert(ErrorUtils.getHtttpCodeError(response.code()), mContext);

                        } else {
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
                Constants.showToastAlert(ErrorUtils.getString(R.string.failled), mContext);
            }
        });


    }



}
