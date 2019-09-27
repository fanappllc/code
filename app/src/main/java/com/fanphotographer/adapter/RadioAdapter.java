package com.fanphotographer.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RadioButton;
import com.fanphotographer.R;
import com.fanphotographer.bean.RadioBean;
import java.util.List;



public class RadioAdapter extends BaseAdapter {

    private Context context;
    private static List<RadioBean> arrayList;

    public RadioAdapter(Context applicationContext,List<RadioBean> mylist) {
        this.context = applicationContext;
        arrayList = mylist;
    }


    @Override
    public int getCount() {
        return arrayList.size();
    }

    @Override
    public Object getItem(int position) {
        return arrayList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHoloder mViewHoloder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_list, parent,false);
            mViewHoloder = new ViewHoloder();
            mViewHoloder. Name = (RadioButton) convertView.findViewById(R.id.radioButton1);
            convertView.setTag(mViewHoloder);
        } else {
            mViewHoloder = (ViewHoloder) convertView.getTag();
        }

        mViewHoloder.Name.setText(arrayList.get(position).title);
        Log.e("posstion.....",""+position);
        if(arrayList.get(position).status){
            mViewHoloder.Name.setChecked(true);
        }else{
            mViewHoloder.Name.setChecked(false);
        }

        return  convertView;
    }

    public class ViewHoloder {
        RadioButton Name;
    }
}
