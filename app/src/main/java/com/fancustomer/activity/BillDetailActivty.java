package com.fancustomer.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;

import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.fancustomer.R;
import com.fancustomer.data.constant.Constants;

import com.fancustomer.data.preference.AppPreference;
import com.fancustomer.helper.ExceptionHandler;
import com.fancustomer.helper.TimerService;

import java.text.DecimalFormat;

import de.hdodenhof.circleimageview.CircleImageView;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;


public class BillDetailActivty extends BaseActivity implements View.OnClickListener {


    private TextView buttonDone;
    private String firstNameStr = "";
    private String orderID = "";
    private String lastNameStr = "";
    private String profileStr = "";
    private String dateTimeStr = "";
    private String photographerId = "";
    private String slotTimeStr = "";
    private String cardNoStr = "";
    private String totalBillStr = "";
    private String rating = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bill_detail_activty);
        if (Constants.isServiceRunning(BillDetailActivty.this, TimerService.class)) {
            stopService(new Intent(BillDetailActivty.this, TimerService.class));
        }
        AppPreference.getInstance(BillDetailActivty.this).setString(Constants.START_PHOTO, "0");
        getParam();
        setToolBar();
        intView();
        setClicks();
    }

    private void getParam() {
        orderID = getIntent().getStringExtra("orderID");
        firstNameStr = getIntent().getStringExtra("photographer_first_name");
        lastNameStr = getIntent().getStringExtra("photographer_last_name");
        cardNoStr = getIntent().getStringExtra("last_4_digit");
        profileStr = getIntent().getStringExtra("photographer_profile_image");
        rating = getIntent().getStringExtra("rating_avg");
        dateTimeStr = getIntent().getStringExtra("order_created_at");
        slotTimeStr = getIntent().getStringExtra("time");
        photographerId = getIntent().getStringExtra("photographer_id");
        totalBillStr = getIntent().getStringExtra("price");
    }

    @SuppressLint("SetTextI18n")
    private void intView() {
        RatingBar userRatingBar = (RatingBar) findViewById(R.id.userRatingBar);
        CircleImageView userImageView = (CircleImageView) findViewById(R.id.userImageView);
        buttonDone = (TextView) findViewById(R.id.button_done);
        TextView userNameTextView = (TextView) findViewById(R.id.userNameTextView);
        TextView dateTextView = (TextView) findViewById(R.id.dateTextView);
        TextView slotTimeTextView = (TextView) findViewById(R.id.slotTimeTextView);
        TextView cardNoTextView = (TextView) findViewById(R.id.cardNoTextView);
        TextView amountTextView = (TextView) findViewById(R.id.amountTextView);
        try {
            if (!Constants.isStringNullOrBlank(rating)) {
                userRatingBar.setRating(Float.parseFloat(rating));
            }
            userNameTextView.setText(firstNameStr + " " + lastNameStr);
            dateTextView.setText(Constants.getDateInFormat("yyyy-MM-dd hh:mm:ss", "MMM dd,yyyy hh:mm aa", dateTimeStr));
            slotTimeTextView.setText(slotTimeStr + " Mins");
            cardNoTextView.setText("**** **** **** " + cardNoStr);
            amountTextView.setText("$ " + new DecimalFormat("#,##0.00").format(Double.parseDouble(totalBillStr)));
            if (!profileStr.equals("")) {
                Glide.with(BillDetailActivty.this).load(profileStr)
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
        } catch (Exception e) {
            ExceptionHandler.printStackTrace(e);
        }

    }


    @Override
    protected void networkConnnectivityChange() {
        super.networkConnnectivityChange();
        if (!Constants.isInternetOn(BillDetailActivty.this)) {
            final ViewGroup viewGroup = (ViewGroup) ((ViewGroup) BillDetailActivty.this.findViewById(android.R.id.content)).getChildAt(0);
            showSnackbar(viewGroup, getResources().getString(R.string.no_internet), "Retry");
        } else {
            hideSnackbar();
        }
    }


    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    private void setClicks() {

        buttonDone.setOnClickListener(this);

    }

    private void setToolBar() {
        ImageView imageViewMenu = (ImageView) findViewById(R.id.imageView_menu);
        TextView headerTextView = (TextView) findViewById(R.id.hedertextview);
        ImageView toolbarbackpress = (ImageView) findViewById(R.id.toolbarbackpress);
        toolbarbackpress.setVisibility(View.VISIBLE);
        imageViewMenu.setVisibility(View.GONE);
        headerTextView.setText(getResources().getString(R.string.bill_detail));
        toolbarbackpress.setOnClickListener(this);

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(BillDetailActivty.this, MenuScreen.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }

    @Override
    public void onClick(View view) {
        int i = view.getId();
        if (i == R.id.toolbarbackpress) {
            onBackPressed();
        } else if (i == R.id.button_done) {
            Intent intent = new Intent(BillDetailActivty.this, ReviewActivity.class);
            intent.putExtra("profileStr", profileStr);
            intent.putExtra("firstNameStr", firstNameStr);
            intent.putExtra("lastNameStr", lastNameStr);
            intent.putExtra("rating", rating);
            intent.putExtra("photographer_id", photographerId);
            intent.putExtra("orderID", orderID);
            startActivity(intent);
            finish();

        }
    }
}
