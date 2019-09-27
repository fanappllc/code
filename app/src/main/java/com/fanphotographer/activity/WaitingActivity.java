package com.fanphotographer.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import com.fanphotographer.R;
import com.fanphotographer.data.constant.Constants;
import com.fanphotographer.utility.AppUtils;
import com.fanphotographer.utility.ErrorUtils;
import com.fanphotographer.webservice.Api;
import com.fanphotographer.webservice.ApiFactory;
import org.json.JSONArray;
import org.json.JSONObject;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class WaitingActivity extends BaseActivity  {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_waiting);

        if (AppUtils.isNetworkConnected()) {
            getStatus();
        } else {
            final ViewGroup viewGroup = (ViewGroup) ((ViewGroup) WaitingActivity.this.findViewById(android.R.id.content)).getChildAt(0);
            Constants.showSnackbar(WaitingActivity.this, viewGroup, getResources().getString(R.string.no_internet), Constants.RETRY);
        }

    }



    public void getStatus() {


        Api api = ApiFactory.getClientWithoutHeader(WaitingActivity.this).create(Api.class);
        Call<ResponseBody> call;
        call = api.getStatus(appPreference.getString(Constants.ACCESS_TOKEN));
        Constants.showProgressDialog(WaitingActivity.this, Constants.LOADING);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Constants.hideProgressDialog();
                try {
                    if (response != null && response.isSuccessful() && response.code() == 200) {
                        String output = ErrorUtils.getResponseBody(response);
                        JSONObject object = new JSONObject(output);
                        if (object.optString(Constants.SUCCESS).equalsIgnoreCase(Constants.TRUE)) {
                            JSONObject jsonObject = object.optJSONObject("data");
                            String accessToken = jsonObject.optString("access-token");
                            String isUserregistered = jsonObject.optString("is_user_registered");
                            appPreference.setString(Constants.ACCESS_TOKEN, accessToken);
                            appPreference.setString(Constants.IS_USER_REGISTERED, isUserregistered);
                            if(isUserregistered.equalsIgnoreCase("0")){

                            }else {
                                Intent intent = new Intent(WaitingActivity.this, MenuScreen.class);
//                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                                startActivity(intent);
                                finish();
                            }

                        } else if (object.optString(Constants.SUCCESS).equalsIgnoreCase(Constants.FALSE)) {
                            JSONArray jsonArray = object.optJSONArray(Constants.ERROR);
                            if (jsonArray == null) {
                                Constants.showToastAlert(object.getJSONObject(Constants.ERROR).getString(Constants.MESSAGE), WaitingActivity.this);
                            } else {
                                Constants.showToastAlert(jsonArray.getJSONObject(0).getString(Constants.MESSAGE), WaitingActivity.this);
                            }
                        }

                    } else if (response != null) {
                        if (response.code() == 400 || response.code() == 500 || response.code() == 403 || response.code() == 404 || response.code() == 401) {
                            Constants.showToastAlert(ErrorUtils.getHtttpCodeError(response.code()), WaitingActivity.this);
                        } else {
                            String responseStr = ErrorUtils.getResponseBody(response);
                            JSONObject jsonObject = new JSONObject(responseStr);
                            Constants.showToastAlert(ErrorUtils.checkJosnErrorBody(jsonObject), WaitingActivity.this);
                        }
                    }
                } catch (Exception e) {
                    Log.e(Constants.LOG_CAT,e.getMessage());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Constants.hideProgressDialog();
                Constants.showToastAlert(ErrorUtils.getString(R.string.failled), WaitingActivity.this);
            }
        });


    }







}
