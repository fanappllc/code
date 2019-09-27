package com.fancustomer.activity;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.fancustomer.R;
import com.fancustomer.data.constant.Constants;
import com.fancustomer.data.preference.AppPreference;
import com.fancustomer.helper.ErrorUtils;
import com.fancustomer.webservice.Api;
import com.fancustomer.webservice.ApiFactory;

import org.json.JSONException;
import org.json.JSONObject;


import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;


public class TermsAndConditionsActivity extends BaseActivity {


    CheckBox checkbox;
    private String mobile = "";
    ProgressBar progressBar1 = null;
    private WebView webView;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terms_and_conditions);
        setToolBar();
        initView();
        if (Constants.isInternetOn(TermsAndConditionsActivity.this)) {
            getTeramsAndCondition();
        } else {
            final ViewGroup viewGroup = (ViewGroup) ((ViewGroup) TermsAndConditionsActivity.this.findViewById(android.R.id.content)).getChildAt(0);
            showSnackbar(viewGroup, getResources().getString(R.string.no_internet), Constants.RETRY);
        }
        mobile = getIntent().getStringExtra("mobile");
    }


    private void initView() {

        checkbox = (CheckBox) findViewById(R.id.checkbox);
        webView = (WebView) findViewById(R.id.webView);
        progressBar1 = (ProgressBar) findViewById(R.id.progressBar1);
        TextView tVcontinue = (TextView) findViewById(R.id.txtSubmitDetail);
        tVcontinue.setOnClickListener(listener);
        checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                Log.e(Constants.LOG_CAT, "onCheckedChanged: ");
            }
        });

    }

    public void getTeramsAndCondition() {
        Api api = ApiFactory.getClientWithoutHeader(TermsAndConditionsActivity.this).create(Api.class);
        Call<ResponseBody> call;

        call = api.getTermsAndCondition();
        Log.e(Constants.LOG_CAT, "API TERMS AND CONDITION------------------->>>>>:" + " " + call.request().url());
        Log.e(Constants.LOG_CAT, "HEADERS : " + call.request().headers());


        Constants.showProgressDialog(TermsAndConditionsActivity.this, Constants.LOADING);
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
                            Log.e(Constants.LOG_CAT, "onResponse: API TERMS AND CONDITION-=" + object.toString());
                            JSONObject jsonObject = object.optJSONObject("data");
                            String url = jsonObject.optString("url");
                            progressBar1.setMax(100);
                            webView.setWebChromeClient(new MyWebViewClient());
                            webView.getSettings().setJavaScriptEnabled(true);
                            webView.loadUrl(url);
                            progressBar1.setProgress(0);
                        } else if (object.optString(Constants.SUCCESS).equalsIgnoreCase(Constants.FALSE)) {
                            ErrorUtils.showFalseMessage(object, TermsAndConditionsActivity.this);
                        }
                    } else if (response.code() == 400 || response.code() == 500 || response.code() == 403 || response.code() == 404 || response.code() == 401) {
                        if (response.code() == 401) {
                            Constants.showSessionExpireAlert(TermsAndConditionsActivity.this);
                        } else {
                            Constants.showToastAlert(ErrorUtils.getHtttpCodeError(response.code()), TermsAndConditionsActivity.this);
                        }

                    } else {
                        String responseStr = ErrorUtils.getResponseBody(response);
                        JSONObject jsonObject = new JSONObject(responseStr);
                        Constants.showToastAlert(ErrorUtils.checkJosnErrorBody(jsonObject), TermsAndConditionsActivity.this);
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


    @Override
    protected void networkConnnectivityChange() {
        super.networkConnnectivityChange();
        if (!Constants.isInternetOn(TermsAndConditionsActivity.this)) {
            final ViewGroup viewGroup = (ViewGroup) ((ViewGroup) TermsAndConditionsActivity.this.findViewById(android.R.id.content)).getChildAt(0);
            showSnackbar(viewGroup, getResources().getString(R.string.no_internet), Constants.RETRY);
        } else {
            hideSnackbar();
        }
    }

    private void setToolBar() {

        RelativeLayout toolBarLeft = (RelativeLayout) findViewById(R.id.toolBarLeft);
        TextView headerTextView = (TextView) findViewById(R.id.hedertextview);
        headerTextView.setText(TermsAndConditionsActivity.this.getResources().getString(R.string.terms_and_conditions));
        toolBarLeft.setOnClickListener(listener);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.toolBarLeft:
                    onBackPressed();
                    break;
                case R.id.txtSubmitDetail:
                    if (checkbox.isChecked()) {
                        AppPreference.getInstance(TermsAndConditionsActivity.this).setString(Constants.IS_TERMS_CONDITION_ACCEPTED, "1");
                        Intent intent1 = new Intent(TermsAndConditionsActivity.this, UpdateProfileActivity.class);
                        intent1.putExtra("COME_FROM", "VerifyMobileActivity");
                        intent1.putExtra("mobile", mobile);
                        startActivity(intent1);
                    } else {
                        Constants.showToastAlert(getResources().getString(R.string.terms_and_conditions_message), TermsAndConditionsActivity.this);
                    }
                    break;
                default:
                    break;
            }

        }
    };

    private class MyWebViewClient extends WebChromeClient {
        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            if (newProgress >= 100) {
                progressBar1.setVisibility(View.GONE);
            }
            setValue(newProgress);
            super.onProgressChanged(view, newProgress);
        }
    }

    public void setValue(int progress) {
        progressBar1.setProgress(progress);
    }
}

