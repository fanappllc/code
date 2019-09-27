package com.fanphotographer.data.preference;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import com.fanphotographer.activity.EnterMobileActivity;
import com.fanphotographer.bean.GetUserBean;
import com.fanphotographer.data.constant.Constants;
import com.fanphotographer.utility.ActivityUtils;
import com.fanphotographer.utility.DialogUtils;
import org.json.JSONObject;


public class AppPreference {


    private static Context mContext;  // declare context
    private static AppPreference appPreference = null; // singleton
    private SharedPreferences sharedPreferences, settingsPreferences;  // common
    private SharedPreferences.Editor editor;

    public static AppPreference getInstance(Context context) {
        if (appPreference == null) {
            mContext = context;
            appPreference = new AppPreference();
        }
        return appPreference;
    }

    private AppPreference() {
        sharedPreferences = mContext.getSharedPreferences(Constants.APP_PREF_NAME, Context.MODE_PRIVATE);
        settingsPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        editor = sharedPreferences.edit();
    }

    public void setString(String key, String value) {
        editor.putString(key, value);
        editor.commit();
    }

    public String getString(String key) {
        String str = sharedPreferences.getString(key, "");
        if(key.equalsIgnoreCase(Constants.DRIVING_LICENCE_IMAGE)|| key.equalsIgnoreCase(Constants.SSN_NO)){
            return str;
        }else {
            if (str.equalsIgnoreCase("null") || str.equalsIgnoreCase("") || TextUtils.isEmpty(str)) {
                str = "";
            }
        }
        return str;
    }

    public void setLongValue(String key, long value) {
        editor.putLong(key,value);
        editor.commit();
    }


    public long getLongValue(String key) {
        return sharedPreferences.getLong(key,-1);
    }


    public void setBoolean(String key, boolean value) {
        editor.putBoolean(key, value);
        editor.commit();
    }

    public Boolean getBoolean(String key) {
        return sharedPreferences.getBoolean(key, false);
    }


    public void showCustomAlert(final Activity activity,String mss){
        AppPreference.getInstance(mContext).clearsession();
        DialogUtils.showOkDialogBox(activity,  mss, new DialogUtils.AlertMessageListener() {
            @Override
            public void onClickOk() {
                ActivityUtils.getInstance().sendFlowwithfinish(activity,EnterMobileActivity.class);
            }
        });
    }



    public void setCurrentUserInfo(GetUserBean userInfo){
//        setString(Constants.USER_ID,userInfo.user_id);
        setString(Constants.ID,userInfo.id);
        setString(Constants.FIRST_NAME,userInfo.first_name);
        setString(Constants.LAST_NAME,userInfo.last_name);
        setString(Constants.DAIL_CODE,userInfo.dial_code);
        setString(Constants.EMAIL,userInfo.email);
        setString(Constants.COUNTRY_CODE,userInfo.country_code);
        setString(Constants.MOBILE,userInfo.mobile);
        setString(Constants.MOBILE_MODEL,userInfo.mobile_model);
        setString(Constants.ADDRESS,userInfo.address);
        setString(Constants.ZIP_CODE,userInfo.zip_code);
        setString(Constants.ROLE,userInfo.role);
        setString(Constants.STATUS,userInfo.status);
        setString(Constants.IS_AVAILABLE,userInfo.is_available);
        setString(Constants.IS_APPROVED,userInfo.is_approved);
        setString(Constants.PROFILE_IMAGE,userInfo.profile_image);
        setString(Constants.DRIVING_LICENCE_IMAGE,userInfo.driving_licence_image);
        setString(Constants.SSN_NO,userInfo.ssn_no);
        setString(Constants.PASSWORD_TOKEN,userInfo.password_token);
        setString(Constants.IS_TERMS_CONDITION_ACCEPTED,userInfo.is_terms_condition_accepted);
        setString(Constants.IS_PROFILE_UPDATED,userInfo.is_profile_updated);
        setString(Constants.IS_USER_ACCOUNTED_ADDED,userInfo.is_user_account_added);
        setString(Constants.IS_REGISTRATION_FEE_PAID,userInfo.is_registration_fee_paid);
        setString(Constants.IS_USER_REGISTERED,userInfo.is_user_registered);
        setString(Constants.LATITUDE,userInfo.latitude);
        setString(Constants.LONGITUDE,userInfo.longitude);
        setString(Constants.CREATED_AT,userInfo.created_at);
        setString(Constants.UPDATED_AT,userInfo.updated_at);
        setString(Constants.CUSTOMER_ID,userInfo.customer_id);
        setString(Constants.ACCESS_TOKEN,userInfo.access_token);
    }

    public void clearsession(){
//        setString(Constants.USER_ID,"");
        setString(Constants.ID,"");
        setString(Constants.FIRST_NAME,"");
        setString(Constants.LAST_NAME,"");
        setString(Constants.DAIL_CODE,"");
        setString(Constants.EMAIL,"");
        setString(Constants.COUNTRY_CODE,"");
        setString(Constants.MOBILE,"");
        setString(Constants.MOBILE_MODEL,"");
        setString(Constants.ADDRESS,"");
        setString(Constants.ZIP_CODE,"");
        setString(Constants.ROLE,"");
        setString(Constants.STATUS,"");
        setString(Constants.IS_AVAILABLE,"");
        setString(Constants.IS_APPROVED,"");
        setString(Constants.PROFILE_IMAGE,"");
        setString(Constants.DRIVING_LICENCE_IMAGE,"");
        setString(Constants.SSN_NO,"");
        setString(Constants.PASSWORD_TOKEN,"");
        setString(Constants.IS_TERMS_CONDITION_ACCEPTED,"");
        setString(Constants.IS_PROFILE_UPDATED,"");
        setString(Constants.IS_USER_ACCOUNTED_ADDED,"");
        setString(Constants.IS_REGISTRATION_FEE_PAID,"");
        setString(Constants.IS_USER_REGISTERED,"");
        setString(Constants.LATITUDE,"");
        setString(Constants.LONGITUDE,"");
        setString(Constants.CREATED_AT,"");
        setString(Constants.UPDATED_AT,"");
        setString(Constants.CUSTOMER_ID,"");
        setString(Constants.ACCESS_TOKEN,"");
    }

    public GetUserBean getCurrentUserInfo(){

        GetUserBean userInfo = new GetUserBean();
//        userInfo.user_id = getString(Constants.USER_ID);
        userInfo.id = getString(Constants.ID);
        userInfo.first_name = getString(Constants.FIRST_NAME);
        userInfo.last_name = getString(Constants.LAST_NAME);
        userInfo.dial_code = getString(Constants.DAIL_CODE);
        userInfo.email = getString(Constants.EMAIL);
        userInfo.country_code = getString(Constants.COUNTRY_CODE);
        userInfo.mobile = getString(Constants.MOBILE);
        userInfo.mobile_model = getString(Constants.MOBILE_MODEL);
        userInfo.address = getString(Constants.ADDRESS);
        userInfo.zip_code = getString(Constants.ZIP_CODE);
        userInfo.role = getString(Constants.ROLE);
        userInfo.status = getString(Constants.STATUS);
        userInfo.is_available = getString(Constants.IS_AVAILABLE);
        userInfo.is_approved = getString(Constants.IS_APPROVED);
        userInfo.profile_image = getString(Constants.PROFILE_IMAGE);
        userInfo.driving_licence_image = getString(Constants.DRIVING_LICENCE_IMAGE);
        userInfo.ssn_no = getString(Constants.SSN_NO);
        userInfo.password_token = getString(Constants.PASSWORD_TOKEN);
        userInfo.is_terms_condition_accepted = getString(Constants.IS_TERMS_CONDITION_ACCEPTED);
        userInfo.is_profile_updated = getString(Constants.IS_PROFILE_UPDATED);
        userInfo.is_user_account_added = getString(Constants.IS_USER_ACCOUNTED_ADDED);
        userInfo.is_registration_fee_paid = getString(Constants.IS_REGISTRATION_FEE_PAID);
        userInfo.is_user_registered = getString(Constants.IS_USER_REGISTERED);
        userInfo.latitude = getString(Constants.LATITUDE);
        userInfo.longitude = getString(Constants.LONGITUDE);
        userInfo.created_at = getString(Constants.CREATED_AT);
        userInfo.updated_at = getString(Constants.UPDATED_AT);
        userInfo.customer_id = getString(Constants.CUSTOMER_ID);
        userInfo.access_token = getString(Constants.ACCESS_TOKEN);
        return userInfo;
    }
    public GetUserBean parseData(JSONObject jsonObject){
        GetUserBean getUserBean = new GetUserBean();
//        getUserBean.user_id = jsonObject.optString("user_id");
        getUserBean.id = jsonObject.optString("id");
        getUserBean.first_name = jsonObject.optString("first_name");
        getUserBean.last_name = jsonObject.optString("last_name");
        getUserBean.dial_code = jsonObject.optString("dial_code");
        getUserBean.email = jsonObject.optString("email");
        getUserBean.country_code = jsonObject.optString("country_code");
        getUserBean.mobile = jsonObject.optString("mobile");
        getUserBean.mobile_model = jsonObject.optString("mobile_model");
        getUserBean.address = jsonObject.optString("address");
        getUserBean.zip_code = jsonObject.optString("zip_code");
        getUserBean.role = jsonObject.optString("role");
        getUserBean.status = jsonObject.optString("status");
        getUserBean.is_available = jsonObject.optString("is_available");
        getUserBean.is_approved = jsonObject.optString("is_approved");
        getUserBean.profile_image = jsonObject.optString("profile_image");
        getUserBean.driving_licence_image = jsonObject.optString("driving_licence_image");
        getUserBean.ssn_no = jsonObject.optString("ssn_no");
        getUserBean.password_token = jsonObject.optString("password_token");
        getUserBean.is_terms_condition_accepted = jsonObject.optString("is_terms_condition_accepted");
        getUserBean.is_profile_updated = jsonObject.optString("is_profile_updated");
        getUserBean.is_user_account_added = jsonObject.optString("is_user_account_added");
        getUserBean.is_registration_fee_paid = jsonObject.optString("is_registration_fee_paid");
        getUserBean.is_user_registered = jsonObject.optString("is_user_registered");
        getUserBean.latitude = jsonObject.optString("latitude");
        getUserBean.longitude = jsonObject.optString("longitude");
        getUserBean.created_at = jsonObject.optString("created_at");
        getUserBean.updated_at = jsonObject.optString("updated_at");
        getUserBean.customer_id = jsonObject.optString("customer_id");
        getUserBean.access_token = jsonObject.optString("access-token");
        AppPreference.getInstance(mContext).setCurrentUserInfo(getUserBean);
        return getUserBean;
    }
}
