package com.fanphotographer.activity;

import  android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.fanphotographer.R;
import com.fanphotographer.data.constant.Constants;
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
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;


public class SsnActivity extends BaseActivity{

    private EditText socialEditText;
    private String socialStr;
    private Context mContext = this;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ssn);
        setToolBar();
        initView();
    }


    private void initView() {
        socialEditText = (EditText) findViewById(R.id.socialEditText);
    }

    private void setValidation() {
        socialStr = socialEditText.getText().toString().trim();
        if (socialStr.equals("")) {
            Constants.showToastAlert(getResources().getString(R.string.Please_social_security), SsnActivity.this);
        } else {
            KeyboardUtils.hideKeyboard(SsnActivity.this);
            if (AppUtils.isNetworkConnected()) {
                updatessnApi();
            } else {
                final ViewGroup viewGroup = (ViewGroup) ((ViewGroup) SsnActivity.this.findViewById(android.R.id.content)).getChildAt(0);
                Constants.showSnackbar(mContext, viewGroup, getResources().getString(R.string.no_internet), "Retry");
            }

        }
    }

    private void setToolBar() {
        RelativeLayout toolBarLeft = (RelativeLayout) findViewById(R.id.toolBarLeft);
        TextView headerTextView = (TextView) findViewById(R.id.hedertextview);
        headerTextView.setText(SsnActivity.this.getResources().getString(R.string.ssn));
        TextView tvContinue = (TextView) findViewById(R.id.txtSubmitDetail);
        tvContinue.setOnClickListener(listener);
        toolBarLeft.setOnClickListener(listener);
    }

    View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            int i = view.getId();
            if (i == R.id.toolBarLeft) {
                finish();

            } else if (i == R.id.txtSubmitDetail) {
                setValidation();

            }
        }
    };


    public void updatessnApi() {


        Api api = ApiFactory.getClientWithoutHeader(SsnActivity.this).create(Api.class);
        HashMap<String, String> map = new HashMap<>();
        Call<ResponseBody> call;
        String accessToken = appPreference.getString(Constants.ACCESS_TOKEN);
        map.put("ssn_no", socialStr);
        call = api.updatessn(accessToken,map);

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
                            String userRegistered = jsonObject.optString("is_user_registered");
                            if (userRegistered.equals("0")) {
                                appPreference.setString(Constants.SSN_NO, socialStr);
                                Intent intent = new Intent(SsnActivity.this, IdProofActivity.class);
                                startActivity(intent);
//                                finish();

                            }

                        } else if (object.optString(Constants.SUCCESS).equalsIgnoreCase(Constants.FALSE)) {
                            appPreference.setString(Constants.SSN_NO, "");
                            JSONArray jsonArray = object.optJSONArray(Constants.ERROR);
                            if (jsonArray == null) {
                                Constants.showToastAlert(object.getJSONObject(Constants.ERROR).getString(Constants.MESSAGE),mContext);
                            }else {
                                Constants.showToastAlert(jsonArray.getJSONObject(0).getString(Constants.MESSAGE), mContext);
                            }
                        }

                    }  else if (response != null) {
                        if (response.code() == 400 || response.code() == 500 || response.code() == 403 || response.code() == 404 || response.code() == 401) {
                            appPreference.setString(Constants.SSN_NO, "");
                            if(response.code()==401){
                                appPreference.showCustomAlert(SsnActivity.this,getResources().getString(R.string.http_401_error));
                            }else {
                                Constants.showToastAlert(ErrorUtils.getHtttpCodeError(response.code()), mContext);
                            }

                        } else {
                            appPreference.setString(Constants.SSN_NO, "");
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

