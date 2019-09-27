package com.fanphotographer.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.fanphotographer.BuildConfig;
import com.fanphotographer.R;
import com.fanphotographer.data.constant.Constants;
import com.fanphotographer.utility.AppUtils;
import com.fanphotographer.utility.ErrorUtils;
import com.fanphotographer.utility.KeyboardUtils;
import com.fanphotographer.webservice.Api;
import com.fanphotographer.webservice.ApiFactory;
import com.stripe.android.Stripe;
import com.stripe.android.TokenCallback;
import com.stripe.android.model.Card;
import com.stripe.android.model.Token;
import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.HashMap;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class RegistrationFeeActivity extends BaseActivity {

    private TextView registrationFee;
    private EditText cardNoEditText;
    private EditText cardHolderNameEditText;
    private EditText expireOnEditText;
    private EditText cvvEditText;
    private Context mContext = this;
    private String stripeKey,secret_key;


    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration_fee);
        setToolbar();
        initView();

        if (AppUtils.isNetworkConnected()) {
            getKey();
        } else {
            final ViewGroup viewGroup = (ViewGroup) ((ViewGroup) RegistrationFeeActivity.this.findViewById(android.R.id.content)).getChildAt(0);
            Constants.showSnackbar(RegistrationFeeActivity.this, viewGroup, getResources().getString(R.string.no_internet), Constants.RETRY);
        }

    }

    private void initView() {
        cardNoEditText = (EditText) findViewById(R.id.cardNoEditText);
        registrationFee = (TextView) findViewById(R.id.registration_fee);
        cardHolderNameEditText = (EditText) findViewById(R.id.cardHolderNameEditText);
        expireOnEditText = (EditText) findViewById(R.id.expireOnEditText);
        cvvEditText = (EditText) findViewById(R.id.cvvEditText);
        TextView txtSubmitDetail = (TextView) findViewById(R.id.txtSubmitDetail);
        txtSubmitDetail.setOnClickListener(listener);


        cardNoEditText.addTextChangedListener(new TextWatcher() {
            int len = 0;
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                String str = cardNoEditText.getText().toString();
                if ((str.length() == 4 && len < str.length()) || (str.length() == 9) || (str.length() == 14) && len < str.length()) {
                    cardNoEditText.append("-");
                }
            }

            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                String str = cardNoEditText.getText().toString();
                len = str.length();
            }
            @Override public void afterTextChanged(Editable s) {
                Log.e(Constants.LOG_CAT,"afterTextChanged");
            }
        });


        expireOnEditText.addTextChangedListener(new TextWatcher() {
            int len = 0;
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                String str = expireOnEditText.getText().toString();
                if ((str.length() == 2 && len < str.length()) && len < str.length()) {
                    expireOnEditText.append("/");
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                String str = expireOnEditText.getText().toString();
                len = str.length();
            }

            @Override public void afterTextChanged(Editable s) {
                Log.e(Constants.LOG_CAT,"afterTextChanged");
            }
        });
    }

    private void setToolbar() {

        RelativeLayout toolBarLeft = (RelativeLayout) findViewById(R.id.toolBarLeft);
        TextView headerTextView = (TextView) findViewById(R.id.hedertextview);
        headerTextView.setText(RegistrationFeeActivity.this.getResources().getString(R.string.registrarion_fee));
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


    private void setValidation() {
        if(!Constants.isStringNullOrBlank(stripeKey)) {
            String cardNumber = cardNoEditText.getText().toString().trim();
            String cardHoldername = cardHolderNameEditText.getText().toString().trim();
            String expireOn = expireOnEditText.getText().toString().trim();
            String cvvNumber = cvvEditText.getText().toString().trim();


            if (cardNumber.equals("")) {
                Constants.showToastAlert(getResources().getString(R.string.please_enter_card_number), mContext);
            } else if (cardNumber.replace("-", "").length() != 16) {
                Constants.showToastAlert(getResources().getString(R.string.please_enter_valid_card_number), mContext);
            } else if (cardHoldername.equalsIgnoreCase("")) {
                Constants.showToastAlert(getResources().getString(R.string.please_enter_card_holder_name), mContext);
            } else if (expireOn.equalsIgnoreCase("")) {
                Constants.showToastAlert(getResources().getString(R.string.please_enter_expire_date), mContext);
            } else if (expireOn.replace("/", "").length() != 6) {
                Constants.showToastAlert(getResources().getString(R.string.Please_enter_valid_date_formate), mContext);
            } else if (cvvNumber.equalsIgnoreCase("")) {
                Constants.showToastAlert(getResources().getString(R.string.please_enter_cvv), mContext);
            } else if (cvvNumber.length() != 3) {
                Constants.showToastAlert(getResources().getString(R.string.please_enter_valid_cvv_number), mContext);
            } else {
                KeyboardUtils.hideKeyboard(RegistrationFeeActivity.this);
                if (AppUtils.isNetworkConnected()) {
                    String[] date = expireOn.split("/");
                    Card card = new Card(cardNumber, Integer.parseInt(date[0]), Integer.parseInt(date[1]), cvvNumber);
                    card.validateNumber();
                    card.validateCVC();
                    card.validateExpMonth();
                    card.validateExpYear();
                    card.setName(cardHoldername);
                    if (!card.validateNumber()) {
                        Constants.showToastAlert("Your card's number is invalid", RegistrationFeeActivity.this);
                    } else if (!card.validateCVC()) {
                        Constants.showToastAlert("Your cvv number is invalid", RegistrationFeeActivity.this);
                    } else if (!card.validateExpMonth()) {
                        Constants.showToastAlert("Enter valid month", RegistrationFeeActivity.this);
                    } else if (!card.validateExpYear()) {
                        Constants.showToastAlert("Enter valid year", RegistrationFeeActivity.this);
                    } else {
                        if (card != null) {
//                        pk_test_PXJXzT4pZI6A1cMC4MtNB0ph
                            Constants.showProgressDialog(RegistrationFeeActivity.this, Constants.LOADING);
                            final String publishableApiKey = BuildConfig.DEBUG ? stripeKey : stripeKey;

                            Stripe stripe = new Stripe(RegistrationFeeActivity.this);
                            stripe.createToken(card, publishableApiKey, new TokenCallback() {
                                public void onSuccess(Token token) {

                                    if (AppUtils.isNetworkConnected()) {
                                        String price = registrationFee.getText().toString();
                                        registrationApi(token.getId(),price);
                                    } else {
                                        final ViewGroup viewGroup = (ViewGroup) ((ViewGroup) RegistrationFeeActivity.this.findViewById(android.R.id.content)).getChildAt(0);
                                        Constants.showSnackbar(RegistrationFeeActivity.this, viewGroup, getResources().getString(R.string.no_internet), Constants.RETRY);
                                    }

                                    Log.e(Constants.LOG_CAT, "Token ID: " + token.getId().toString());

                                }

                                public void onError(Exception error) {
                                    appPreference.setString(Constants.IS_REGISTRATION_FEE_PAID, "0");
                                    Log.d("Stripe", error.getLocalizedMessage());
                                    Constants.hideProgressDialog();
                                    Constants.showToastAlert(error.getLocalizedMessage(), mContext);
                                }

                            });

                        }
                    }
                } else {
                    final ViewGroup viewGroup = (ViewGroup) ((ViewGroup) RegistrationFeeActivity.this.findViewById(android.R.id.content)).getChildAt(0);
                    Constants.showSnackbar(mContext, viewGroup, getResources().getString(R.string.no_internet), "Retry");
                }
            }
        }else {
            getKey();
        }

    }


    public void registrationApi(String id,String price) {


        Api api = ApiFactory.getClient(RegistrationFeeActivity.this).create(Api.class);
        HashMap<String, String> map = new HashMap<>();
        String accessToken = appPreference.getString(Constants.ACCESS_TOKEN);
        Call<ResponseBody> call;
        map.put("access-token", accessToken);
        map.put("amount",price);
        map.put("stripe_token", id);
        call = api.registration_payment(map);
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
                            String transactionId = jsonObject.optString("transaction_id");
                            if (!TextUtils.isEmpty(transactionId)) {
                                appPreference.setString(Constants.IS_REGISTRATION_FEE_PAID, "1");
                                Intent intent = new Intent(RegistrationFeeActivity.this, WaitingActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                                startActivity(intent);
//                                finish();
                            }

                        } else if (object.optString(Constants.SUCCESS).equalsIgnoreCase(Constants.FALSE)) {
                            appPreference.setString(Constants.IS_REGISTRATION_FEE_PAID, "0");
                            JSONArray jsonArray = object.optJSONArray(Constants.ERROR);
                            if (jsonArray == null) {
                                Constants.showToastAlert(object.getJSONObject(Constants.ERROR).getString(Constants.MESSAGE),mContext);
                            }else {
                                Constants.showToastAlert(jsonArray.getJSONObject(0).getString(Constants.MESSAGE), mContext);
                            }
                        }

                    } else if (response != null) {
                        if (response.code() == 400 || response.code() == 500 || response.code() == 403 || response.code() == 404 || response.code() == 401) {
                            appPreference.setString(Constants.IS_REGISTRATION_FEE_PAID, "0");
                            if(response.code()==401){
                                appPreference.showCustomAlert(RegistrationFeeActivity.this,getResources().getString(R.string.http_401_error));
                            }else {
                                Constants.showToastAlert(ErrorUtils.getHtttpCodeError(response.code()), mContext);
                            }

                        } else {
                            appPreference.setString(Constants.IS_REGISTRATION_FEE_PAID, "0");
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


    public void getRegistrationfee() {


        Api api = ApiFactory.getClientWithoutHeader(RegistrationFeeActivity.this).create(Api.class);
        Call<ResponseBody> call;
        call = api.getRegistrationfee();

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
                            String value = jsonObject.optString("value");
                            if(!Constants.isStringNullOrBlank(value))
                                registrationFee.setText("$ "+new DecimalFormat("#,##0.00").format(Double.parseDouble(value)));
//                            registrationFee.setText("$"+value);

                        } else if (object.optString(Constants.SUCCESS).equalsIgnoreCase(Constants.FALSE)) {
                            JSONArray jsonArray = object.optJSONArray(Constants.ERROR);
                            if (jsonArray == null) {
                                Constants.showToastAlert(object.getJSONObject(Constants.ERROR).getString(Constants.MESSAGE), RegistrationFeeActivity.this);
                            } else {
                                Constants.showToastAlert(jsonArray.getJSONObject(0).getString(Constants.MESSAGE), RegistrationFeeActivity.this);
                            }
                        }

                    } else if (response != null) {
                        if (response.code() == 400 || response.code() == 500 || response.code() == 403 || response.code() == 404 || response.code() == 401) {
                            if (response.code() == 401) {
                                appPreference.showCustomAlert(RegistrationFeeActivity.this, getResources().getString(R.string.http_401_error));
                            } else {
                                Constants.showToastAlert(ErrorUtils.getHtttpCodeError(response.code()), RegistrationFeeActivity.this);
                            }
                        } else {
                            String responseStr = ErrorUtils.getResponseBody(response);
                            JSONObject jsonObject = new JSONObject(responseStr);
                            Constants.showToastAlert(ErrorUtils.checkJosnErrorBody(jsonObject), RegistrationFeeActivity.this);
                        }
                    }
                } catch (Exception e) {
                    Log.e(Constants.LOG_CAT,e.getMessage());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Constants.hideProgressDialog();
                Constants.showToastAlert(ErrorUtils.getString(R.string.failled), RegistrationFeeActivity.this);
            }
        });


    }




    public void getKey() {


        Api api = ApiFactory.getClientWithoutHeader(RegistrationFeeActivity.this).create(Api.class);
        Call<ResponseBody> call;
        call = api.getstripekey();

        Constants.showProgressDialog(RegistrationFeeActivity.this, "Loading");
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                try {
                    if (response != null && response.isSuccessful() && response.code() == 200) {
                        String output = ErrorUtils.getResponseBody(response);
                        JSONObject object = new JSONObject(output);
                        if (object.optString(Constants.SUCCESS).equalsIgnoreCase(Constants.TRUE)) {
                            JSONObject jsonObject = object.optJSONObject("data");
                            stripeKey = jsonObject.optString("stripe_key");
                            secret_key = jsonObject.optString("secret_key");
                            getRegistrationfee();

                        } else if (object.optString(Constants.SUCCESS).equalsIgnoreCase(Constants.FALSE)) {
                            Constants.hideProgressDialog();
                            JSONArray jsonArray = object.optJSONArray(Constants.ERROR);
                            if (jsonArray == null) {
                                Constants.showToastAlert(object.getJSONObject(Constants.ERROR).getString(Constants.MESSAGE), RegistrationFeeActivity.this);
                            } else {
                                Constants.showToastAlert(jsonArray.getJSONObject(0).getString(Constants.MESSAGE), RegistrationFeeActivity.this);
                            }
                        }

                    } else if (response != null) {
                        Constants.hideProgressDialog();
                        if (response.code() == 400 || response.code() == 500 || response.code() == 403 || response.code() == 404 || response.code() == 401) {
                            if (response.code() == 401) {
                                appPreference.showCustomAlert(RegistrationFeeActivity.this, getResources().getString(R.string.http_401_error));
                            } else {
                                Constants.showToastAlert(ErrorUtils.getHtttpCodeError(response.code()), RegistrationFeeActivity.this);
                            }
                        } else {
                            String responseStr = ErrorUtils.getResponseBody(response);
                            JSONObject jsonObject = new JSONObject(responseStr);
                            Constants.showToastAlert(ErrorUtils.checkJosnErrorBody(jsonObject), RegistrationFeeActivity.this);
                        }
                    }
                } catch (Exception e) {
                    Constants.hideProgressDialog();
                    Log.e(Constants.LOG_CAT,e.getMessage());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Constants.hideProgressDialog();
                Constants.showToastAlert(ErrorUtils.getString(R.string.failled), RegistrationFeeActivity.this);
            }
        });


    }

}
