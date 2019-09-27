package com.fancustomer.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RadioButton;

import com.fancustomer.R;
import com.fancustomer.bean.ReniewBean;


import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

/**
 * Created by Shubham on 7/12/2017.
 */

public class ReniewAdapter extends BaseAdapter {

    Context context;
    public ArrayList<ReniewBean> mylist;

    public ReniewAdapter(Context applicationContext, ArrayList<ReniewBean> mylist) {
        this.context = applicationContext;
        this.mylist = mylist;
    }


    @Override
    public int getCount() {
        return mylist.size();
    }

    @Override
    public Object getItem(int position) {
        return mylist.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHoloder mViewHoloder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_reniew, parent, false);
            mViewHoloder = new ViewHoloder();
            mViewHoloder.Name = (RadioButton) convertView.findViewById(R.id.radioButton1);
            convertView.setTag(mViewHoloder);
        } else {
            mViewHoloder = (ViewHoloder) convertView.getTag();
        }

        int priceMain = Integer.parseInt(mylist.get(position).slot_minutes);
        if (priceMain < 60) {
            mViewHoloder.Name.setText("00:" + mylist.get(position).slot_minutes + " " + "Min");
        } else {
            long hourNew = TimeUnit.MINUTES.toMillis(priceMain);
            String hms = String.format("%02d:%02d", TimeUnit.MILLISECONDS.toHours(hourNew),
                    TimeUnit.MILLISECONDS.toMinutes(hourNew) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(hourNew)));
            mViewHoloder.Name.setText("" + hms + " Hour");
        }
        Log.e("posstion.....", "" + position);
        if (mylist.get(position).status) {
            mViewHoloder.Name.setChecked(true);
        } else {
            mViewHoloder.Name.setChecked(false);
        }
        return convertView;
    }

    public class ViewHoloder {
        RadioButton Name;
    }
}