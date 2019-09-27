package com.fancustomer.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.appcompat.BuildConfig;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.fancustomer.R;
import com.fancustomer.data.constant.Constants;
import com.fancustomer.data.preference.AppPreference;
import com.fancustomer.helper.ErrorUtils;
import com.fancustomer.helper.FourDigitCardFormatWatcher;
import com.fancustomer.webservice.Api;
import com.fancustomer.webservice.ApiFactory;

import com.stripe.android.Stripe;
import com.stripe.android.TokenCallback;
import com.stripe.android.model.Card;
import com.stripe.android.model.Token;


import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;


import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;


public class AddCreditCardActivity extends BaseActivity {

    private String stripecustomerid;
    private TextView txtAddCard;
    private EditText cardNoEditText;
    private EditText cardHolderNameEditText;
    private EditText expireOnEditText;
    private EditText cvvEditText;
    private String comeAddCard = "";


    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_credit_card);
        if (Constants.isInternetOn(AddCreditCardActivity.this)) {
            getStripeApi();
        } else {
            final ViewGroup viewGroup = (ViewGroup) ((ViewGroup) AddCreditCardActivity.this.findViewById(android.R.id.content)).getChildAt(0);
            showSnackbar(viewGroup, getResources().getString(R.string.no_internet), Constants.RETRY);
        }

        setToolBar();
        initView();
        comeAddCard = getIntent().getStringExtra("COME_ADDCARD");
        stripecustomerid = AppPreference.getInstance(AddCreditCardActivity.this).getString(Constants.CUSTOMER_ID);
        txtAddCard.setOnClickListener(listener);
    }

    @Override
    protected void networkConnnectivityChange() {
        super.networkConnnectivityChange();
        if (!Constants.isInternetOn(AddCreditCardActivity.this)) {
            final ViewGroup viewGroup = (ViewGroup) ((ViewGroup) AddCreditCardActivity.this.findViewById(android.R.id.content)).getChildAt(0);
            showSnackbar(viewGroup, getResources().getString(R.string.no_internet), Constants.RETRY);
        } else {
            hideSnackbar();
        }
    }


    private void initView() {
        txtAddCard = (TextView) findViewById(R.id.txtAddCard);
        cardNoEditText = (EditText) findViewById(R.id.cardNoEditText);
        cardNoEditText.addTextChangedListener(new FourDigitCardFormatWatcher());
        cardHolderNameEditText = (EditText) findViewById(R.id.cardHolderNameEditText);
        expireOnEditText = (EditText) findViewById(R.id.expireOnEditText);
        cvvEditText = (EditText) findViewById(R.id.cvvEditText);
        expireOnEditText.addTextChangedListener(new TextWatcher() {
            int len = 0;

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // TODO Auto-generated method stub

                String str = expireOnEditText.getText().toString();

                if ((str.length() == 2 && len < str.length()) && len < str.length()) {
                    //checking length  for backspace.
                    expireOnEditText.append("/");
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
                // TODO Auto-generated method stub
                String str = expireOnEditText.getText().toString();
                len = str.length();
            }

            @Override
            public void afterTextChanged(Editable s) {
                // TODO Auto-generated method stub

            }
        });
    }

    private void setToolBar() {

        RelativeLayout toolBarLeft = (RelativeLayout) findViewById(R.id.toolBarLeft);
        TextView headerTextView = (TextView) findViewById(R.id.hedertextview);
        headerTextView.setText(AddCreditCardActivity.this.getResources().getString(R.string.add_credit_card));
        toolBarLeft.setOnClickListener(listener);
    }

    View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            int i = view.getId();
            if (i == R.id.toolBarLeft) {
                finish();

            } else if (i == R.id.txtAddCard) {
                checkAllfields();

            }

        }
    };

    public void getStripeApi() {
        Api api = ApiFactory.getClientWithoutHeader(AddCreditCardActivity.this).create(Api.class);
        Call<ResponseBody> call = null;
        call = api.getStripeKey();
        Log.e(Constants.LOG_CAT, "API Stripe key------------------->>>>>" + " " + call.request().url());
        Log.e(Constants.LOG_CAT, "HEADERS : " + call.request().headers());


        Constants.showProgressDialog(AddCreditCardActivity.this, Constants.LOADING);
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
                            Log.e(Constants.LOG_CAT, "onResponse: API Stripe key-=" + object.toString());
                            JSONObject jsonObject = object.optJSONObject("data");
                            if (jsonObject != null) {
                                AppPreference.getInstance(AddCreditCardActivity.this).setString(Constants.STRIPE_PUBLISH_KEY, jsonObject.optString("stripe_key"));
                                AppPreference.getInstance(AddCreditCardActivity.this).setString(Constants.STRIPE_SECRET_KEY, jsonObject.optString("secret_key"));
                                Log.e(Constants.LOG_CAT, "stripe key Value<<<<<<<<<<" + "stripe_key===>>>>>>>>>>" + jsonObject.optString("stripe_key") + "secret_key=========>>>>>>>>>>>>>>>" + jsonObject.optString("secret_key"));
                            }

                        } else if (object.optString(Constants.SUCCESS).equalsIgnoreCase(Constants.FALSE)) {
                            ErrorUtils.showFalseMessage(object, AddCreditCardActivity.this);
                        }
                    } else if (response.code() == 400 || response.code() == 500 || response.code() == 403 || response.code() == 404 || response.code() == 401) {
                        if (response.code() == 401) {
                            Constants.showSessionExpireAlert(AddCreditCardActivity.this);
                        } else {
                            Constants.showToastAlert(ErrorUtils.getHtttpCodeError(response.code()), AddCreditCardActivity.this);
                        }

                    } else {
                        String responseStr = ErrorUtils.getResponseBody(response);
                        JSONObject jsonObject = new JSONObject(responseStr);
                        Constants.showToastAlert(ErrorUtils.checkJosnErrorBody(jsonObject), AddCreditCardActivity.this);
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

    private void checkAllfields() {
        String cardNumber = cardNoEditText.getText().toString().trim();
        String cardHolderName = cardHolderNameEditText.getText().toString().trim();
        String expireOn = expireOnEditText.getText().toString().trim();
        String cvvNumber = cvvEditText.getText().toString().trim();
        if (!cardNumber.equalsIgnoreCase("")) {
            if (cardNumber.replace("-", "").length() == 16) {
                if (!cardHolderName.equalsIgnoreCase("")) {
                    if (!expireOn.equalsIgnoreCase("")) {
                        if (expireOn.replace("/", "").length() == 6) {
                            if (!cvvNumber.equalsIgnoreCase("")) {
                                if (cvvNumber.length() == 3) {
                                    if (Constants.isInternetOn(AddCreditCardActivity.this)) {
                                        String[] date = expireOn.split("/");
                                        Card card = new Card(cardNumber, Integer.parseInt(date[0]), Integer.parseInt(date[1]), cvvNumber);
                                        card.validateNumber();
                                        card.validateCVC();
                                        card.validateExpMonth();
                                        card.validateExpYear();
                                        card.setName(cardHolderName);
                                        if (!card.validateNumber()) {
                                            Constants.showToastAlert("Your card's number is invalid", AddCreditCardActivity.this);
                                        } else if (!card.validateCVC()) {
                                            Constants.showToastAlert("Your cvv number is invalid", AddCreditCardActivity.this);
                                        } else if (!card.validateExpMonth()) {
                                            Constants.showToastAlert("Enter valid month", AddCreditCardActivity.this);
                                        } else if (!card.validateExpYear()) {
                                            Constants.showToastAlert("Enter valid year", AddCreditCardActivity.this);
                                        } else {
                                            if (card != null) {
                                                Constants.showProgressDialog(AddCreditCardActivity.this, Constants.LOADING);
                                                final String publishableApiKey = BuildConfig.DEBUG ? AppPreference.getInstance(AddCreditCardActivity.this).getString(Constants.STRIPE_PUBLISH_KEY) : AppPreference.getInstance(AddCreditCardActivity.this).getString(Constants.STRIPE_PUBLISH_KEY);
                                                Stripe stripe = new Stripe(AddCreditCardActivity.this);
                                                stripe.createToken(card, publishableApiKey, new TokenCallback() {
                                                    public void onSuccess(Token token) {
                                                        // TODO: Send Token information to your backend to initiate a charge
                                                        if (Constants.isInternetOn(AddCreditCardActivity.this)) {
                                                            addCArdAPI(token.getId());
                                                        } else {
                                                            final ViewGroup viewGroup = (ViewGroup) ((ViewGroup) AddCreditCardActivity.this.findViewById(android.R.id.content)).getChildAt(0);
                                                            showSnackbar(viewGroup, getResources().getString(R.string.no_internet), Constants.RETRY);
                                                        }
                                                        Log.e(Constants.LOG_CAT, "Token ID" + token.getId());

                                                    }

                                                    public void onError(Exception error) {
                                                        Constants.hideProgressDialog();
                                                        Constants.showToastAlert(error.getLocalizedMessage(), AddCreditCardActivity.this);
                                                        Log.d("Stripe", error.getLocalizedMessage());
                                                    }
                                                });

                                            }
                                        }


                                    } else {
                                        final ViewGroup viewGroup = (ViewGroup) ((ViewGroup) AddCreditCardActivity.this.findViewById(android.R.id.content)).getChildAt(0);
                                        showSnackbar(viewGroup, getResources().getString(R.string.no_internet), Constants.RETRY);
                                    }


                                } else {
                                    Constants.showToastAlert(getString(R.string.please_enter_valid_cvv_number), AddCreditCardActivity.this);
                                }

                            } else {
                                Constants.showToastAlert(getString(R.string.please_enter_cvv), AddCreditCardActivity.this);
                            }
                        } else {
                            Constants.showToastAlert(getString(R.string.Please_enter_valid_date_formate), AddCreditCardActivity.this);
                        }

                    } else {
                        Constants.showToastAlert(getString(R.string.please_enter_expire_date), AddCreditCardActivity.this);
                    }

                } else {
                    Constants.showToastAlert(getString(R.string.please_enter_card_holder_name), AddCreditCardActivity.this);
                }

            } else {
                Constants.showToastAlert(getString(R.string.please_enter_valid_card_number), AddCreditCardActivity.this);
            }

        } else {
            Constants.showToastAlert(getString(R.string.please_enter_card_number), AddCreditCardActivity.this);
        }


    }


    public void addCArdAPI(String stripeToken) {
        Api api = ApiFactory.getClientWithoutHeader(AddCreditCardActivity.this).create(Api.class);
        HashMap<String, String> map = new HashMap<>();
        Call<ResponseBody> call;
        map.put("stripe_token", stripeToken);


        String accessToken = AppPreference.getInstance(AddCreditCardActivity.this).getString(Constants.ACCESS_TOKEN);
        call = api.addCardApi(accessToken, map);
        Log.e(Constants.LOG_CAT, "API REQUEST USER LOGIN ------------------->>>>>:" + map + " " + call.request().url());
        Log.e(Constants.LOG_CAT, "HEADERS : " + call.request().headers());


        Constants.showProgressDialog(AddCreditCardActivity.this, Constants.LOADING);
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
                            Log.e(Constants.LOG_CAT, "FANCUSTOMER UPDATE Profile==============>>>>>" + object.toString());
                            if (comeAddCard.equals("MANAGE_CARD")) {
                                Intent intent = new Intent();
                                setResult(RESULT_OK, intent);
                                finish();
                            } else {
                                Intent intent = new Intent(AddCreditCardActivity.this, MenuScreen.class);
                                intent.putExtra("COME_FROM_MENU", "ADD_CREDIT_CARD");
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK
                                        | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                finish();
                            }


                        } else if (object.optString(Constants.SUCCESS).equalsIgnoreCase(Constants.FALSE)) {
                            ErrorUtils.showFalseMessage(object, AddCreditCardActivity.this);
                        }
                    } else if (response.code() == 400 || response.code() == 500 || response.code() == 403 || response.code() == 404 || response.code() == 401) {
                        if (response.code() == 401) {
                            Constants.showSessionExpireAlert(AddCreditCardActivity.this);
                        } else {
                            Constants.showToastAlert(ErrorUtils.getHtttpCodeError(response.code()), AddCreditCardActivity.this);
                        }

                    } else {
                        String responseStr = ErrorUtils.getResponseBody(response);
                        JSONObject jsonObject = new JSONObject(responseStr);
                        Constants.showToastAlert(ErrorUtils.checkJosnErrorBody(jsonObject), AddCreditCardActivity.this);
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


}
