package com.fanphotographer.activity;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import com.fanphotographer.R;
import com.fanphotographer.adapter.GetStaredAdapter;
import com.fanphotographer.data.constant.Constants;
import com.fanphotographer.helper.LinePageIndicator;
import com.fanphotographer.utility.ActivityUtils;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;


public class GetStartedActivity extends AppCompatActivity {

    private LinePageIndicator titleIndicator;
    private ViewPager viewPagergetstarted;
    private View view;
    private TextView textViewstarted;

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

        viewPagergetstarted.setOffscreenPageLimit(1);
        viewPagergetstarted.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                Log.e(Constants.LOG_CAT,"onPageScrolled");
            }

            @Override
            public void onPageSelected(int position) {
                Log.e(Constants.LOG_CAT,"onPageSelected");
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                Log.e(Constants.LOG_CAT,"onPageScrollStateChanged");
            }
        });

        viewPagergetstarted.setAdapter(mAdapter);
        titleIndicator.setViewPager(viewPagergetstarted);
        textViewstarted.setOnClickListener(listener);
    }

    private void initView() {

        titleIndicator =  findViewById(R.id.lineIndicator_get_started);
        viewPagergetstarted =  findViewById(R.id.view_pager_get_started);
        textViewstarted =  findViewById(R.id.text_view_started);

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
