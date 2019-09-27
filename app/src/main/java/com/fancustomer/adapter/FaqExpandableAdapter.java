package com.fancustomer.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import com.fancustomer.R;
import com.fancustomer.bean.FaqGroupBean;
import com.fancustomer.data.constant.Constants;


import java.util.ArrayList;

public class FaqExpandableAdapter extends BaseExpandableListAdapter {
    private Context context;
    private LayoutInflater infalInflater;
    private ArrayList<FaqGroupBean> listParent;

    // Initialize constructor for array list
    public FaqExpandableAdapter(Context context, ArrayList<FaqGroupBean> FavouriteAddressList) {
        this.context = context;
        this.listParent = FavouriteAddressList;
        infalInflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public Object getChild(int groupPosition, int childPosititon) {
        return listParent.get(groupPosition).getChildList().get(childPosititon);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }


    // Inflate child view
    @Override
    public View getChildView(final int groupPosition, final int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = infalInflater.inflate(R.layout.faq_child, null);
        }

        TextView tvAddress = convertView.findViewById(R.id.tv_address);
        Log.e(Constants.LOG_CAT, "getChildView: " + listParent.get(groupPosition).getChildList().get(childPosition).getAnswer());
        tvAddress.setText(listParent.get(groupPosition).getChildList().get(childPosition).getAnswer());

        return convertView;
    }

    // return number of headers in list
    @Override
    public int getChildrenCount(int groupPosition) {
        return listParent.get(groupPosition).getChildList().size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return this.listParent.get(groupPosition).getQues();
    }

    @Override
    public int getGroupCount() {
        return this.listParent.size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    // inflate header view
    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        String headerTitle = (String) getGroup(groupPosition);
        if (convertView == null) {
            convertView = infalInflater.inflate(R.layout.faq_group_item, null);
        }

        TextView groupHeaderText = convertView.findViewById(R.id.group_header_text);
        groupHeaderText.setText(listParent.get(groupPosition).getQues());
        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}