package com.fancustomer.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.fancustomer.R;
import com.fancustomer.data.constant.Constants;
import com.fancustomer.data.preference.AppPreference;
import com.fancustomer.helper.ErrorUtils;
import com.fancustomer.utility.AppUtils;
import com.fancustomer.webservice.Api;
import com.fancustomer.webservice.ApiFactory;


import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;


public class ReviewActivity extends BaseActivity implements View.OnClickListener {


    private TextView buttonSubmit;
    private EditText editTextReview;
    float ratingValue;
    private String profileStr = "";
    private String firstNameStr = "";
    private String lastNameStr = "";
    private String rating;
    private String orderID = "";
    private String photographerId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);
        setToolBar();
        getParam();
        intView();
        setClicks();
    }

    private void intView() {

        RatingBar userRatingBar = (RatingBar) findViewById(R.id.userRatingBar);
        buttonSubmit = (TextView) findViewById(R.id.button_submit);
        TextView loveTextView = (TextView) findViewById(R.id.love_textView);
        TextView reviewHeadingTextView = (TextView) findViewById(R.id.reviewHeadingTextView);
        String header = "How would you rate your    <font color='#FE8D01'>" + "experience" + "</font>   with us";
        reviewHeadingTextView.setText(AppUtils.fromHtml(header));

        String loveStr = "<font color='#FE8D01'>" + "Love" + "</font>   to hear from you a comment  <font color='#A1A1A1'>" + "(optional)" + "</font>";
        loveTextView.setText(AppUtils.fromHtml(loveStr));

        CircleImageView userImageView = (CircleImageView) findViewById(R.id.userImageView);
        TextView userNameTextView = (TextView) findViewById(R.id.userNameTextView);
        editTextReview = (EditText) findViewById(R.id.editText_review);

        RatingBar ratingBarSend = (RatingBar) findViewById(R.id.ratingBarSend);
        userNameTextView.setText(firstNameStr + " " + lastNameStr);


        if (!Constants.isStringNullOrBlank(rating)) {
            userRatingBar.setRating(Float.parseFloat(rating));
        }


        if (!profileStr.equals("")) {
            Glide.with(ReviewActivity.this).load(profileStr)
                    .thumbnail(0.5f)
                    .placeholder(R.mipmap.defult_user).dontAnimate()
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .listener(new RequestListener<String, GlideDrawable>() {
                        @Override
                        public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                            return false;
                        }
                    }).into(userImageView);

        }

        ratingBarSend.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float v, boolean b) {
                ratingValue = v;
            }
        });


    }

    private void setValidation() {
        String editReview = editTextReview.getText().toString().trim();

        if (ratingValue == 0) {
            Constants.showToastAlert(getResources().getString(R.string.please_enter_rating), ReviewActivity.this);
        }
        if (editReview.equals("")) {
            Constants.showToastAlert(getResources().getString(R.string.please_enter_review), ReviewActivity.this);
        } else {
            showthanksDialog(editReview);
        }
    }


    @Override
    protected void networkConnnectivityChange() {
        super.networkConnnectivityChange();
        if (!Constants.isInternetOn(ReviewActivity.this)) {
            final ViewGroup viewGroup = (ViewGroup) ((ViewGroup) ReviewActivity.this.findViewById(android.R.id.content)).getChildAt(0);
            showSnackbar(viewGroup, getResources().getString(R.string.no_internet), Constants.RETRY);
        } else {
            hideSnackbar();
        }
    }

    private void getParam() {

        orderID = getIntent().getStringExtra("orderID");
        photographerId = getIntent().getStringExtra("photographer_id");
        rating = getIntent().getStringExtra("rating");
        firstNameStr = getIntent().getStringExtra("firstNameStr");
        lastNameStr = getIntent().getStringExtra("lastNameStr");
        profileStr = getIntent().getStringExtra("profileStr");
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    private void setClicks() {

        buttonSubmit.setOnClickListener(this);

    }

    private void setToolBar() {

        ImageView imageViewMenu = (ImageView) findViewById(R.id.imageView_menu);
        TextView headerTextView = (TextView) findViewById(R.id.hedertextview);
        ImageView toolbarbackpress = (ImageView) findViewById(R.id.toolbarbackpress);
        toolbarbackpress.setVisibility(View.GONE);
        imageViewMenu.setVisibility(View.GONE);
        headerTextView.setText(getResources().getString(R.string.rating));
        toolbarbackpress.setOnClickListener(this);

    }

    private void showthanksDialog(final String editReview) {
        final Dialog dialog = new Dialog(ReviewActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.dialog_thank_you);
        RelativeLayout rlOk = dialog.findViewById(R.id.rl_ok);

        rlOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();

                if (Constants.isInternetOn(ReviewActivity.this)) {
                    ratingReviewAPI(editReview);
                } else {
                    final ViewGroup viewGroup = (ViewGroup) ((ViewGroup) ReviewActivity.this.findViewById(android.R.id.content)).getChildAt(0);
                    showSnackbar(viewGroup, getResources().getString(R.string.no_internet), Constants.RETRY);
                }


            }
        });

        dialog.show();

    }


    public void ratingReviewAPI(String editReview) {
        Api api = ApiFactory.getClientWithoutHeader(ReviewActivity.this).create(Api.class);
        HashMap<String, String> map = new HashMap<>();
        Call<ResponseBody> call;
        map.put("to_user_id", photographerId);
        map.put("order_id", orderID);
        map.put("rating", String.valueOf(ratingValue));
        map.put("review", editReview);

        String accessToken = AppPreference.getInstance(ReviewActivity.this).getString(Constants.ACCESS_TOKEN);
        call = api.ratingUser(accessToken, map);
        Log.e(Constants.LOG_CAT, "API REQUEST RATING REVIEW API------------------->>>>>:" + map + " " + call.request().url());
        Log.e(Constants.LOG_CAT, "HEADERS : " + call.request().headers());


        Constants.showProgressDialog(ReviewActivity.this, "Loading");
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
                            Log.e(Constants.LOG_CAT, "API REQUEST SEND REQUEST RATING REVIEW API================>>>>>" + object.toString());
                            Intent intent = new Intent(ReviewActivity.this, MenuScreen.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK
                                    | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                            finish();
                        } else if (object.optString(Constants.SUCCESS).equalsIgnoreCase(Constants.FALSE)) {
                            ErrorUtils.showFalseMessage(object, ReviewActivity.this);
                        }
                    } else if (response.code() == 400 || response.code() == 500 || response.code() == 403 || response.code() == 404 || response.code() == 401) {
                        if (response.code() == 401) {
                            Constants.showSessionExpireAlert(ReviewActivity.this);
                        } else {
                            Constants.showToastAlert(ErrorUtils.getHtttpCodeError(response.code()), ReviewActivity.this);
                        }

                    } else {
                        String responseStr = ErrorUtils.getResponseBody(response);
                        JSONObject jsonObject = new JSONObject(responseStr);
                        Constants.showToastAlert(ErrorUtils.checkJosnErrorBody(jsonObject), ReviewActivity.this);
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
    public void onBackPressed() {
        Log.e(Constants.LOG_CAT, "onBackPressed: ");
    }

    @Override
    public void onClick(View view) {
        int i = view.getId();
        if (i == R.id.toolbarbackpress) {
            onBackPressed();

        } else if (i == R.id.button_submit) {
            setValidation();

        }
    }
}