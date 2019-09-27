package com.fanphotographer.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import com.bumptech.glide.request.RequestOptions;
import com.fanphotographer.R;
import com.fanphotographer.data.constant.Constants;
import com.fanphotographer.data.preference.AppPreference;
import com.fanphotographer.helper.TimerService;
import com.fanphotographer.utility.AppUtils;


import org.json.JSONObject;

import java.text.DecimalFormat;

import de.hdodenhof.circleimageview.CircleImageView;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class BillDetailActivity extends BaseActivity implements View.OnClickListener {

    private String data;
    private CircleImageView userImageView;
    private TextView userNameTextView;
    private TextView dateTextView;
    private TextView slotTimeTextView;
    private TextView cardNoTextView;
    private TextView amountTextView;
    private TextView addressTV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bill_detail);
        appPreference.setString(Constants.REQUEST_END_SESSION, "");
        appPreference.setString("mtime", "");
        appPreference.setString("currentTime","");
        appPreference.setString("KILLTIMENEW","");
        if(AppUtils.isServiceRunning(BillDetailActivity.this,TimerService.class)){
            stopService(new Intent(BillDetailActivity.this, TimerService.class));
        }
        if(TimerService.stimer!=null){
            TimerService.canceltimer();
            TimerService.stimer = null;
        }

        if(getIntent().hasExtra("jsonObject")) {
            data = getIntent().getStringExtra("jsonObject");
        }
        setToolbar();
        intView();
        if(getIntent().hasExtra("jsonObject")) {
            setData();
        }

    }

    private void intView() {
        TextView buttonDone = (TextView) findViewById(R.id.button_done);
        userImageView = (CircleImageView) findViewById(R.id.userImageView);
        userNameTextView = (TextView) findViewById(R.id.userNameTextView);
        dateTextView = (TextView) findViewById(R.id.dateTextView);
        slotTimeTextView = (TextView) findViewById(R.id.slotTimeTextView);
        cardNoTextView = (TextView) findViewById(R.id.cardNoTextView);
        amountTextView = (TextView) findViewById(R.id.amountTextView);
        addressTV = (TextView) findViewById(R.id.user_fan_texView);
        buttonDone.setOnClickListener(this);
    }


    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    private void setData() {
        try {
            JSONObject jsonObject = new JSONObject(data);
            String customerFirstName =  jsonObject.optString("customer_first_name");
            String customerLastName =  jsonObject.optString("customer_last_name");
            String slotPrice =  jsonObject.optString("slot_price");
            String orderCreatedAt =  jsonObject.optString("order_created_at");
            String slotTime =  jsonObject.optString("slot_time");
            String customerAddress =  jsonObject.optString("customer_address");
            String customerProfileImage =  jsonObject.optString("customer_profile_image");
            String lastDigit =  jsonObject.optString("last_4_digit");



            userNameTextView.setText(customerFirstName + " " + customerLastName);
            dateTextView.setText(Constants.getDateInFormat("yyyy-MM-dd hh:mm:ss", "MMM dd,yyyy HH:mm a", orderCreatedAt));
            slotTimeTextView.setText(slotTime + " Mins");
            cardNoTextView.setText("**** **** **** " + lastDigit);
            if(!Constants.isStringNullOrBlank(slotPrice))
                amountTextView.setText("$ "+new DecimalFormat("#,##0.00").format(Double.parseDouble(slotPrice)));
//            amountTextView.setText("$" + slotPrice);
            addressTV.setText(customerAddress);
            if (!customerProfileImage.equals("")) {

                Glide.with(BillDetailActivity.this).load(customerProfileImage)
                        .thumbnail(0.5f)
                        .apply(new RequestOptions().placeholder(R.mipmap.defult_user).dontAnimate())
                        .apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL))
                        .into(userImageView);

            }

        } catch (Exception e) {
            Log.e(Constants.LOG_CAT,e.getMessage());
        }
    }

    private void setToolbar() {

        ImageView imageViewMenu = (ImageView) findViewById(R.id.imageView_menu);
        TextView headerTextView = (TextView) findViewById(R.id.hedertextview);
        ImageView toolbarbackpress = (ImageView) findViewById(R.id.toolbarbackpress);
        toolbarbackpress.setVisibility(View.GONE);
        imageViewMenu.setVisibility(View.GONE);
        headerTextView.setText(getResources().getString(R.string.bill_detail));
        toolbarbackpress.setOnClickListener(this);

    }

    @Override
    public void onBackPressed() {
        Log.e(Constants.LOG_CAT,"backpressed");
    }

    @Override
    public void onClick(View view) {
        int i = view.getId();
        if (i == R.id.toolbarbackpress) {
            onBackPressed();

        } else if (i == R.id.button_done) {
            Intent intent = new Intent(BillDetailActivity.this, MenuScreen.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            startActivity(intent);
            finish();

        }
    }



}