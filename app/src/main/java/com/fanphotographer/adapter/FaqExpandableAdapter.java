package com.fanphotographer.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;
import com.fanphotographer.R;
import com.fanphotographer.bean.FaqGroupBean;
import com.fanphotographer.data.constant.Constants;
import java.util.List;

public class FaqExpandableAdapter extends BaseExpandableListAdapter {

    private LayoutInflater infalInflater;
    private List<FaqGroupBean> listParent;

    public FaqExpandableAdapter(Context context, List<FaqGroupBean> FavouriteAddressList) {
        this.listParent = FavouriteAddressList;
        infalInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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

        TextView tvAddress = (TextView) convertView.findViewById(R.id.tv_address);
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
        if (convertView == null) {
            convertView = infalInflater.inflate(R.layout.faq_group_item, null);
        }

        TextView groupHeadertext = (TextView) convertView.findViewById(R.id.group_header_text);
        groupHeadertext.setText(listParent.get(groupPosition).getQues());

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