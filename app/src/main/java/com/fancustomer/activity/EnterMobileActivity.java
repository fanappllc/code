package com.fancustomer.activity;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.fancustomer.R;
import com.fancustomer.adapter.CountryAdapter;
import com.fancustomer.bean.CountryBean;
import com.fancustomer.data.constant.Constants;
import com.fancustomer.helper.ErrorUtils;
import com.fancustomer.helper.ExceptionHandler;
import com.fancustomer.helper.MyCustomCheckboxTextView;
import com.fancustomer.webservice.Api;
import com.fancustomer.webservice.ApiFactory;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;


public class EnterMobileActivity extends BaseActivity {

    private ImageView imageViewSendCode;
    private Dialog dialogCountry;
    private EditText mobileNumber;
    ArrayList<CountryBean> countryList;
    private TextView textViewCountryCode;
    public static Activity enterMobileActivity;
    String countryCode;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enter_mobile);
        enterMobileActivity = this;
        getAssetss();
        initView();
        imageViewSendCode.setOnClickListener(listener);
        textViewCountryCode.setOnClickListener(listener);
    }

    private void initView() {
        textViewCountryCode = (TextView) findViewById(R.id.textViewCountryCode);
        RelativeLayout toolBarLeft = (RelativeLayout) findViewById(R.id.toolBarLeft);
        toolBarLeft.setVisibility(View.GONE);
        TextView headerTextView = (TextView) findViewById(R.id.hedertextview);
        headerTextView.setText(this.getResources().getString(R.string.mobile_verification));
        imageViewSendCode = (ImageView) findViewById(R.id.imageViewSendCode);
        mobileNumber = (EditText) findViewById(R.id.mobile_number);

    }

    public void sendOTP(final String mobileStr) {

        countryCode = textViewCountryCode.getText().toString().trim();
        Api api = ApiFactory.getClient(EnterMobileActivity.this).create(Api.class);
        HashMap<String, String> map = new HashMap<>();
        Call<ResponseBody> call;

        map.put("country_code", countryCode);
        map.put("mobile", mobileStr);
        map.put("role", "customer");
        Log.e(Constants.LOG_CAT, "sendOTP: countryCode" + countryCode);

        call = api.sendOTP(map);
        Log.e(Constants.LOG_CAT, "API REQUEST USER LOGIN------------------->>>>>" + map + " " + call.request().url());
        Log.e(Constants.LOG_CAT, "HEADERS" + call.request().headers());


        Constants.showProgressDialog(EnterMobileActivity.this, "Login");
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
                            Log.e(Constants.LOG_CAT, "onResponse:" + object.toString());
                            Intent intent = new Intent(EnterMobileActivity.this, VerifyMobileActivity.class);
                            intent.putExtra("mobile", "" + mobileStr);
                            intent.putExtra("countryCode", "" + countryCode);
                            startActivity(intent);
                        } else if (object.optString(Constants.SUCCESS).equalsIgnoreCase(Constants.FALSE)) {
                            ErrorUtils.showFalseMessage(object, EnterMobileActivity.this);
                        }
                    } else if (response.code() == 400 || response.code() == 500 || response.code() == 403 || response.code() == 404 || response.code() == 401) {
                        if (response.code() == 401) {
                            Constants.showSessionExpireAlert(EnterMobileActivity.this);
                        } else {
                            Constants.showToastAlert(ErrorUtils.getHtttpCodeError(response.code()), EnterMobileActivity.this);
                        }

                    } else {
                        String responseStr = ErrorUtils.getResponseBody(response);
                        JSONObject jsonObject = new JSONObject(responseStr);
                        Constants.showToastAlert(ErrorUtils.checkJosnErrorBody(jsonObject), EnterMobileActivity.this);
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


    public String loadJSONFromAsset() {
        String json = null;
        try {
            InputStream is = EnterMobileActivity.this.getAssets().open("countrycodes.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException e) {
            ExceptionHandler.printStackTrace(e);
            return null;
        }
        return json;
    }

    @Override
    protected void networkConnnectivityChange() {
        super.networkConnnectivityChange();
        if (!Constants.isInternetOn(EnterMobileActivity.this)) {
            final ViewGroup viewGroup = (ViewGroup) ((ViewGroup) EnterMobileActivity.this.findViewById(android.R.id.content)).getChildAt(0);
            showSnackbar(viewGroup, getResources().getString(R.string.no_internet), "Retry");
        } else {
            hideSnackbar();
        }
    }

    private void getAssetss() {
        try {
            JSONObject obj = new JSONObject(loadJSONFromAsset());
            JSONArray mJArry = obj.getJSONArray("country");
            countryList = new ArrayList<>();
            for (int i = 0; i < mJArry.length(); i++) {
                JSONObject jsonObject = mJArry.getJSONObject(i);
                Log.e("FAN_CUSTOMER", "getAssetss" + jsonObject.toString());
                Log.d("Details-->", jsonObject.getString("dial_code"));
                String code = jsonObject.getString("code");
                String dialCode = jsonObject.getString("dial_code");
                CountryBean countryBean = new CountryBean();
                countryBean.setCode(code);
                countryBean.setDial_code(dialCode);
                countryList.add(countryBean);

            }
        } catch (JSONException e) {
            ExceptionHandler.printStackTrace(e);
        }
    }


    View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            switch (view.getId()) {
                case R.id.imageViewSendCode:
                    if (!mobileNumber.getText().toString().trim().equalsIgnoreCase("")) {
                        if (mobileNumber.getText().toString().trim().length() == 10 && !mobileNumber.getText().toString().trim().contains(" ")) {

                            if (Constants.isInternetOn(EnterMobileActivity.this)) {
                                sendOTP(mobileNumber.getText().toString().trim());

                            } else {
                                final ViewGroup viewGroup = (ViewGroup) ((ViewGroup) EnterMobileActivity.this.findViewById(android.R.id.content)).getChildAt(0);
                                showSnackbar(viewGroup, getResources().getString(R.string.no_internet), Constants.RETRY);
                            }
                        } else {
                            Constants.showToastAlert(getResources().getString(R.string.Please_enter_valid_number), EnterMobileActivity.this);
                        }
                    } else {
                        Constants.showToastAlert(getResources().getString(R.string.Please_enter_mobile_number), EnterMobileActivity.this);
                    }
                    break;
                case R.id.textViewCountryCode:

                    countryDialog();
                    break;
                default:
                    break;

            }

        }
    };


    private void countryDialog() {


        dialogCountry = new Dialog(EnterMobileActivity.this);
        dialogCountry.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialogCountry.setContentView(R.layout.countrydialog_item);
        ListView countryListView = dialogCountry.findViewById(R.id.listView);
        TextView dialogHeaderText = dialogCountry.findViewById(R.id.dialogHeaderText);

        dialogCountry.show();
        dialogHeaderText.setText("COUNTRY");
        CountryAdapter countryAdapter = new CountryAdapter(EnterMobileActivity.this, countryList);
        countryListView.setAdapter(countryAdapter);
        countryListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ((MyCustomCheckboxTextView) view).setChecked(true);
                String dialCode = (countryList.get(position).getDial_code());
                textViewCountryCode.setText(dialCode);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        dialogCountry.dismiss();

                    }
                }, 300);

            }
        });

    }


}
