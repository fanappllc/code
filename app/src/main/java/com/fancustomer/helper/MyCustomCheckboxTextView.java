package com.fancustomer.helper;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.CheckedTextView;


public class MyCustomCheckboxTextView extends CheckedTextView {
    public MyCustomCheckboxTextView(Context context) {
        super(context);
    }

    public MyCustomCheckboxTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyCustomCheckboxTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public MyCustomCheckboxTextView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }
}
