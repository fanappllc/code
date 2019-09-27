package com.fanphotographer.activity;

import android.content.Context;
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

public class AccountDetailActivity extends BaseActivity {

    private EditText holderName;
    private EditText routingNo;
    private EditText accNumber;
    private EditText confirmAccno;
    private String strHolder;
    private String strAccno;
    private String strConfirmacc;
    private String strRouting;
    private Context mContext = this;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_detail);
        setToolbar();
        initView();
    }

    private void initView() {
        TextView txtSubmitDetail = (TextView) findViewById(R.id.txtSubmitDetail);
        holderName = (EditText) findViewById(R.id.holder_name);
        routingNo = (EditText) findViewById(R.id.routing_no);
        accNumber = (EditText) findViewById(R.id.acc_number);
        confirmAccno = (EditText) findViewById(R.id.confirm_acc_no);

        txtSubmitDetail.setOnClickListener(listener);
    }

    private void setToolbar() {

        RelativeLayout toolBarLeft = (RelativeLayout) findViewById(R.id.toolBarLeft);
        TextView headerTextView = (TextView) findViewById(R.id.hedertextview);
        headerTextView.setText(AccountDetailActivity.this.getResources().getString(R.string.account_deatil));
        toolBarLeft.setOnClickListener(listener);
    }

    private void setValidation() {
        KeyboardUtils.hideKeyboard(AccountDetailActivity.this);
        strHolder = holderName.getText().toString().trim();
        strAccno = accNumber.getText().toString().trim();
        strConfirmacc = confirmAccno.getText().toString().trim();
        strRouting = routingNo.getText().toString().trim();
        if (strHolder.equals("")) {
            Constants.showToastAlert(getResources().getString(R.string.full_name), AccountDetailActivity.this);
        }else if (strRouting.equals("")) {
            Constants.showToastAlert(getResources().getString(R.string.routing), AccountDetailActivity.this);
        }else if (strAccno.equals("")) {
            Constants.showToastAlert(getResources().getString(R.string.account), AccountDetailActivity.this);
        }else if (strConfirmacc.equals("")) {
            Constants.showToastAlert(getResources().getString(R.string.confirm_account), AccountDetailActivity.this);
        }else if (!strAccno.equalsIgnoreCase(strConfirmacc)) {
            Constants.showToastAlert(getResources().getString(R.string.confirm_account), AccountDetailActivity.this);
        } else {

            if (AppUtils.isNetworkConnected()) {
                accountDetailapi();
            } else {
                final ViewGroup viewGroup = (ViewGroup) ((ViewGroup) AccountDetailActivity.this.findViewById(android.R.id.content)).getChildAt(0);
                Constants.showSnackbar(mContext, viewGroup, getResources().getString(R.string.no_internet), "Retry");
            }

        }
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


    public void accountDetailapi() {


        Api api = ApiFactory.getClient(AccountDetailActivity.this).create(Api.class);
        HashMap<String, String> map = new HashMap<>();
        Call<ResponseBody> call;
        map.put("account_holder_name", strHolder);
        map.put("account_no", strAccno);
        map.put("account_no_confirmation", strConfirmacc);
        map.put("routing_no", strRouting);
        call = api.addaccount(map);
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
                            String isUserregistered = jsonObject.optString("is_user_registered");
                            if (isUserregistered.equals("0")) {
                                appPreference.setString(Constants.IS_USER_ACCOUNTED_ADDED, "1");
                                Intent intent = new Intent(AccountDetailActivity.this, RegistrationFeeActivity.class);
                                startActivity(intent);
//                                finish();

                            }

                        } else if (object.optString(Constants.SUCCESS).equalsIgnoreCase(Constants.FALSE)) {
                            appPreference.setString(Constants.IS_USER_ACCOUNTED_ADDED, "0");
                            JSONArray jsonArray = object.optJSONArray(Constants.ERROR);
                            if (jsonArray == null) {
                                Constants.showToastAlert(object.getJSONObject(Constants.ERROR).getString(Constants.MESSAGE),mContext);
                            }else {
                                Constants.showToastAlert(jsonArray.getJSONObject(0).getString(Constants.MESSAGE), mContext);
                            }
                        }

                    } else if (response != null) {
                        if (response.code() == 400 || response.code() == 500 || response.code() == 403 || response.code() == 404 || response.code() == 401 ) {
                            appPreference.setString(Constants.IS_USER_ACCOUNTED_ADDED, "1");
                            if(response.code()==401){
                                appPreference.showCustomAlert(AccountDetailActivity.this,getResources().getString(R.string.http_401_error));
                            }else {
                                Constants.showToastAlert(ErrorUtils.getHtttpCodeError(response.code()), mContext);
                            }

                        } else {
                            appPreference.setString(Constants.IS_USER_ACCOUNTED_ADDED, "1");
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
