package com.fancustomer.activity;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.fancustomer.R;
import com.fancustomer.adapter.GetStaredAdapter;
import com.fancustomer.data.constant.Constants;
import com.fancustomer.helper.LinePageIndicator;
import com.fancustomer.utility.ActivityUtils;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;


public class GetStartedActivity extends BaseActivity {

    private LinePageIndicator titleIndicator;
    private ViewPager viewPagerGetStarted;
    private TextView textViewStarted;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_started);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        initView();

        int[] layouts = new int[]{R.layout.get_started_slide1, R.layout.get_started_slide2, R.layout.get_started_slide3};
        GetStaredAdapter mAdapter = new GetStaredAdapter(GetStartedActivity.this, layouts);

        viewPagerGetStarted.setOffscreenPageLimit(1);
        viewPagerGetStarted.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                Log.e(Constants.LOG_CAT, "onPageScrolled: ");
            }

            @Override
            public void onPageSelected(int position) {
                Log.e(Constants.LOG_CAT, "onPageSelected: ");
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                Log.e(Constants.LOG_CAT, "onPageScrollStateChanged: ");

            }
        });

        viewPagerGetStarted.setAdapter(mAdapter);
        titleIndicator.setViewPager(viewPagerGetStarted);
        textViewStarted.setOnClickListener(listener);
    }

    private void initView() {

        titleIndicator = (LinePageIndicator) findViewById(R.id.lineIndicator_get_started);
        viewPagerGetStarted = (ViewPager) findViewById(R.id.view_pager_get_started);
        textViewStarted = (TextView) findViewById(R.id.text_view_started);

    }

    @Override
    protected void networkConnnectivityChange() {
        super.networkConnnectivityChange();
        if (!Constants.isInternetOn(GetStartedActivity.this)) {
            final ViewGroup viewGroup = (ViewGroup) ((ViewGroup) GetStartedActivity.this.findViewById(android.R.id.content)).getChildAt(0);
            showSnackbar(viewGroup, getResources().getString(R.string.no_internet), Constants.RETRY);
        } else {
            hideSnackbar();
        }
    }

    View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            int i = view.getId();
            if (i == R.id.text_view_started) {
                ActivityUtils.getInstance().invokeActivity(GetStartedActivity.this, EnterMobileActivity.class, true);
            }


        }
    };

}
