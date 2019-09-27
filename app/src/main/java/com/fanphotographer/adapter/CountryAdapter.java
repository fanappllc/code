package com.fanphotographer.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import com.fanphotographer.R;
import com.fanphotographer.bean.CountryBean;
import com.fanphotographer.helper.MyCustomCheckboxTextView;
import java.util.List;


public class CountryAdapter extends BaseAdapter {
    private Context context;
    private List<CountryBean> countryListing;


    public CountryAdapter(Context context, List<CountryBean> countryListing) {
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
        MyCustomCheckboxTextView checkbox = (MyCustomCheckboxTextView) view.findViewById(R.id.checkbox);
        checkbox.setText(countryListing.get(position).getCode() +"  "+ countryListing.get(position).getDial_code());

        return view;
    }
}