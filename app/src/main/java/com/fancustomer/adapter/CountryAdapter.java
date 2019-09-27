package com.fancustomer.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;


import com.fancustomer.R;
import com.fancustomer.bean.CountryBean;
import com.fancustomer.helper.MyCustomCheckboxTextView;

import java.util.ArrayList;

/**
 * Created by Shubham on 7/12/2017.
 */

public class CountryAdapter extends BaseAdapter {
    Context context;
    ArrayList<CountryBean> countryListing;
    MyCustomCheckboxTextView checkbox = null;


    public CountryAdapter(Context context, ArrayList<CountryBean> countryListing) {
        this.context = context;
        this.countryListing = countryListing;
    }


    @Override
    public int getCount() {
        return countryListing.size();
    }

    @Override
    public Object getItem(int i) {
        return countryListing.get(i);
    }

    @Override
    public long getItemId(int i) {
        return countryListing.indexOf(getItem(i));
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view = inflater.inflate(R.layout.row_department, viewGroup, false);

        checkbox = (MyCustomCheckboxTextView) view.findViewById(R.id.checkbox);
        checkbox.setText(countryListing.get(position).getCode() + "  " + countryListing.get(position).getDial_code());


        return view;
    }


}


