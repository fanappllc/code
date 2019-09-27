package com.fanphotographer.activity;

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
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class TermsAndConditionsActivity extends BaseActivity {

    private WebView webViewTerms = null;
    private ProgressBar progressBar1 = null;
    private CheckBox checkbox;
    private Context mContext = this;
    private String url;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_termsandcondition);
        setToolBar();
        initView();

        if (AppUtils.isNetworkConnected()) {
            getTeramsandcondition();
        } else {
            final ViewGroup viewGroup = (ViewGroup) ((ViewGroup) TermsAndConditionsActivity.this.findViewById(android.R.id.content)).getChildAt(0);
            Constants.showSnackbar(TermsAndConditionsActivity.this, viewGroup, getResources().getString(R.string.no_internet), "Retry");
        }
    }


    private void initView() {

        checkbox = (CheckBox) findViewById(R.id.checkbox);
        webViewTerms = (WebView) findViewById(R.id.webView_terms);
        progressBar1 = (ProgressBar) findViewById(R.id.progressBar1);
        TextView tvContinue = (TextView) findViewById(R.id.txtSubmitDetail);
        tvContinue.setOnClickListener(listener);
        checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                Log.e(Constants.LOG_CAT,"onCheckedChanged");
            }
        });

    }

    private void setToolBar() {
        RelativeLayout toolBarLeft = (RelativeLayout) findViewById(R.id.toolBarLeft);
        TextView headerTextView = (TextView) findViewById(R.id.hedertextview);
        headerTextView.setText(mContext.getResources().getString(R.string.terms_and_conditions));
        toolBarLeft.setOnClickListener(listener);
    }

    View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            int i = view.getId();
            if (i == R.id.toolBarLeft) {
                finish();
            } else if (i == R.id.txtSubmitDetail) {
                if (checkbox.isChecked()) {
                    if (!Constants.isStringNullOrBlank(url)) {
                        appPreference.setString(Constants.IS_TERMS_CONDITION_ACCEPTED, "1");
                        appPreference.setString(Constants.IS_USER_REGISTERED, "0");
                        Intent intent = new Intent(TermsAndConditionsActivity.this, UpdateProfileActivity.class);
                        startActivity(intent);
//                        finish();
                    } else {
                        getTeramsandcondition();
                    }
                } else {
                    Constants.showToastAlert(getResources().getString(R.string.terms_and_conditions_message), mContext);
                }
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


    public void getTeramsandcondition() {


        Api api = ApiFactory.getClientWithoutHeader(TermsAndConditionsActivity.this).create(Api.class);
        Call<ResponseBody> call;
        call = api.getTermsAndCondition();

        Constants.showProgressDialog(TermsAndConditionsActivity.this, Constants.LOADING);
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
                             url = jsonObject.optString("url");
                            progressBar1.setMax(100);
                            webViewTerms.setWebChromeClient(new MyWebViewClient());
                            webViewTerms.getSettings().setJavaScriptEnabled(true);
                            webViewTerms.loadUrl(url);
                            progressBar1.setProgress(0);

                        } else if (object.optString(Constants.SUCCESS).equalsIgnoreCase(Constants.FALSE)) {
                            JSONArray jsonArray = object.optJSONArray(Constants.ERROR);
                            if (jsonArray == null) {
                                Constants.showToastAlert(object.getJSONObject(Constants.ERROR).getString(Constants.MESSAGE), TermsAndConditionsActivity.this);
                            } else {
                                Constants.showToastAlert(jsonArray.getJSONObject(0).getString(Constants.MESSAGE), TermsAndConditionsActivity.this);
                            }
                        }

                    } else if (response != null) {
                        if (response.code() == 400 || response.code() == 500 || response.code() == 403 || response.code() == 404 || response.code() == 401) {
                            if (response.code() == 401) {
                                appPreference.showCustomAlert(TermsAndConditionsActivity.this, getResources().getString(R.string.http_401_error));
                            } else {
                                Constants.showToastAlert(ErrorUtils.getHtttpCodeError(response.code()), TermsAndConditionsActivity.this);
                            }
                        } else {
                            String responseStr = ErrorUtils.getResponseBody(response);
                            JSONObject jsonObject = new JSONObject(responseStr);
                            Constants.showToastAlert(ErrorUtils.checkJosnErrorBody(jsonObject), TermsAndConditionsActivity.this);
                        }
                    }
                } catch (Exception e) {
                    Log.e(Constants.LOG_CAT,e.getMessage());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Constants.hideProgressDialog();
                Constants.showToastAlert(ErrorUtils.getString(R.string.failled), TermsAndConditionsActivity.this);
            }
        });


    }

}

