package com.fancustomer.adapter;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by Codiant on 30-Oct-17.
 */

public class    GetStaredAdapter extends PagerAdapter {

    private LayoutInflater layoutInflater;
    private Context mContext;
    private int[] layouts;

    public GetStaredAdapter(Context mContext, int[] layouts) {
        this.mContext = mContext;
        this.layouts = layouts;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        layoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View view = layoutInflater.inflate(layouts[position], container, false);
//        if (position == 2) {
//            TextView textview_privacy_terms = (TextView) view.findViewById(R.id.textview_privacy_terms);
//            ClickableSpan termsOfServicesClick = new ClickableSpan() {
//                @Override
//                public void onClick(View view) {
//                    Toast.makeText(mContext, "Terms of services Clicked", Toast.LENGTH_SHORT).show();
//                }
//            };
//
//            ClickableSpan privacyPolicyClick = new ClickableSpan() {
//                @Override
//                public void onClick(View view) {
//                    Toast.makeText(mContext, "Privacy Policy Clicked", Toast.LENGTH_SHORT).show();
//                }
//            };
//
//            makeLinks(textview_privacy_terms, new String[] { "Terms of Service", "Privacy Policy" }, new ClickableSpan[] {
//                    termsOfServicesClick, privacyPolicyClick
//            });
//        }
        container.addView(view);

        return view;
    }

    public void makeLinks(TextView textView, String[] links, ClickableSpan[] clickableSpans) {
        SpannableString spannableString = new SpannableString(textView.getText());
        for (int i = 0; i < links.length; i++) {
            ClickableSpan clickableSpan = clickableSpans[i];
            String link = links[i];

            int startIndexOfLink = textView.getText().toString().indexOf(link);
            spannableString.setSpan(clickableSpan, startIndexOfLink, startIndexOfLink + link.length(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        textView.setMovementMethod(LinkMovementMethod.getInstance());
        textView.setText(spannableString, TextView.BufferType.SPANNABLE);
    }


    @Override
    public int getCount() {
        return layouts.length;
    }

    @Override
    public boolean isViewFromObject(View view, Object obj) {
        return view == obj;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        View view = (View) object;
        container.removeView(view);
    }
}

