package com.fanphotographer.activity;

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
import com.fanphotographer.R;
import com.fanphotographer.adapter.CountryAdapter;
import com.fanphotographer.bean.CountryBean;
import com.fanphotographer.data.constant.Constants;
import com.fanphotographer.helper.MyCustomCheckboxTextView;
import com.fanphotographer.utility.AppUtils;
import com.fanphotographer.utility.ErrorUtils;
import com.fanphotographer.utility.KeyboardUtils;
import com.fanphotographer.webservice.Api;
import com.fanphotographer.webservice.ApiFactory;
import org.json.JSONArray;
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
    private TextView textViewCountryCode;
    private ArrayList<CountryBean> countryList;
    private EditText mobileNumber;
    private Dialog dialogCountry;
    private Context mContext = this;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enter_mobile);
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
                            Intent intent = new Intent(mContext, VerifyMobileActivity.class);
                            intent.putExtra(Constants.MOBILE, "" + mobileStr);
                            intent.putExtra(Constants.COUNTRY_CODE, "" + code);
                            appPreference.setBoolean(Constants.IS_USER_INFORMATION_STATUS, false);
                            startActivity(intent);

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


    String code="";
    String mobile="";
    View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            switch (view.getId()) {
                case R.id.imageViewSendCode:
                    KeyboardUtils.hideKeyboard(EnterMobileActivity.this);
                    code = textViewCountryCode.getText().toString().trim();
                    mobile = mobileNumber.getText().toString().trim();
                    if(code.equalsIgnoreCase("") && code.length() < 2){
                        Constants.showToastAlert(getResources().getString(R.string.select_code), mContext);
                        return;
                    }else if(mobile.equalsIgnoreCase("")){
                        Constants.showToastAlert(getResources().getString(R.string.Please_enter_mobile_number), mContext);
                        return;
                    }else if(mobile.length() != 10){
                        Constants.showToastAlert(getResources().getString(R.string.Please_enter_valid_number), mContext);
                        return;
                    }else if(!AppUtils.isNetworkConnected()) {
                        final ViewGroup viewGroup = (ViewGroup) ((ViewGroup) EnterMobileActivity.this.findViewById(android.R.id.content)).getChildAt(0);
                        Constants.showSnackbar(mContext, viewGroup, getResources().getString(R.string.no_internet), "Retry");
                        return;
                    }else {
                        sendOTP(mobileNumber.getText().toString().trim(),code);
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

    public String loadJSONFromAsset() {
        String json = null;
        try {
            InputStream is = mContext.getAssets().open("countrycodes.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException e) {
            Log.e(Constants.LOG_CAT,e.getMessage());
            return null;
        }
        return json;
    }

    private void getAssetss() {
        try {
            JSONObject obj = new JSONObject(loadJSONFromAsset());
            JSONArray mJArry = obj.optJSONArray("country");
            countryList = new ArrayList<>();

            for (int i = 0; i < mJArry.length(); i++) {

                JSONObject joInside = mJArry.getJSONObject(i);
                Log.d("Details-->", joInside.optString("dial_code"));
                String dialCode = joInside.optString("dial_code");
                String mcode = joInside.optString("code");
                CountryBean countryBean = new CountryBean();
                countryBean.setCode(mcode);
                countryBean.setDial_code(dialCode);
                countryList.add(countryBean);

            }
        } catch (Exception e) {
            Log.e(Constants.LOG_CAT,e.getMessage());
        }
    }


            private void countryDialog() {


                dialogCountry = new Dialog(mContext);
                dialogCountry.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialogCountry.setContentView(R.layout.countrydialog_item);
                ListView countryListView = (ListView) dialogCountry.findViewById(R.id.listView);
                TextView dialogHeaderText = (TextView) dialogCountry.findViewById(R.id.dialogHeaderText);

                dialogCountry.show();
                dialogHeaderText.setText(getResources().getString(R.string.country));
                CountryAdapter countryAdapter = new CountryAdapter(mContext, countryList);
                countryListView.setAdapter(countryAdapter);
                countryListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        ((MyCustomCheckboxTextView) view).setChecked(true);
                        String hearingNameId = (countryList.get(position).getDial_code());
                        textViewCountryCode.setText(hearingNameId);
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
