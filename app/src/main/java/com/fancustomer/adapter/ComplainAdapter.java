package com.fancustomer.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.fancustomer.R;
import com.fancustomer.bean.ComplainBean;
import com.fancustomer.helper.MyCustomCheckboxTextView;

import java.util.ArrayList;

/**
 * Created by Shubham on 7/12/2017.
 */

public class ComplainAdapter extends BaseAdapter {
    Context context;
    ArrayList<ComplainBean> ComplainBeanList;
    MyCustomCheckboxTextView checkbox = null;


    public ComplainAdapter(Context context, ArrayList<ComplainBean> countryListing) {
        this.context = context;
        this.ComplainBeanList = countryListing;
    }


    @Override
    public int getCount() {
        return ComplainBeanList.size();
    }

    @Override
    public Object getItem(int i) {
        return ComplainBeanList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return ComplainBeanList.indexOf(getItem(i));
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view = inflater.inflate(R.layout.row_department, viewGroup, false);
        checkbox = view.findViewById(R.id.checkbox);
        checkbox.setText(ComplainBeanList.get(position).getTitle());
        return view;
    }
}