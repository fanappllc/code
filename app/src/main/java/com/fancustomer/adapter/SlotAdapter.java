package com.fancustomer.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.RelativeLayout;
import android.widget.TextView;


import com.fancustomer.R;
import com.fancustomer.bean.SlotBean;
import com.fancustomer.helper.ExceptionHandler;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

/**
 * Created by Shubham on 6/27/2017.
 */

public class SlotAdapter extends RecyclerView.Adapter<SlotAdapter.ViewHolder> implements Filterable {

    private Context mContext;
    private View view;
    private ItemInterFace itemClick;
    private ArrayList<SlotBean> slotList;


    public SlotAdapter(Context context, ArrayList<SlotBean> slotList) {

        this.slotList = slotList;
        this.mContext = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        view = LayoutInflater.from(mContext).inflate(R.layout.item_slot, parent, false);

        ViewHolder viewHolder = new ViewHolder(view);

        return viewHolder;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {

        try {
            String priceStr = slotList.get(position).getPrice();
            holder.textViewPrice.setText("Price: $ " + "" + new DecimalFormat("#,##0.00").format(Double.parseDouble(priceStr)));
            int priceMain = Integer.parseInt(slotList.get(position).getSlot_minutes());
            if (priceMain < 60) {
                holder.textViewMin.setText("00:" + slotList.get(position).getSlot_minutes() + " " + "Min");
            } else {
                long hourNew = TimeUnit.MINUTES.toMillis(priceMain);
                String hms = String.format("%02d:%02d", TimeUnit.MILLISECONDS.toHours(hourNew),
                        TimeUnit.MILLISECONDS.toMinutes(hourNew) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(hourNew)));
                holder.textViewMin.setText("" + hms + " Hour");
            }
            if (slotList.get(position).isSelected()) {
                holder.rlFirstSession.setBackgroundResource(R.drawable.white_btn_selector);
                holder.textViewPrice.setTextColor(ContextCompat.getColor(mContext, R.color.white));
                holder.textViewMin.setTextColor(ContextCompat.getColor(mContext, R.color.white));

            } else {
                holder.textViewPrice.setTextColor(mContext.getResources().getColor(R.color.white));
                holder.rlFirstSession.setBackgroundResource(R.color.transparent);
                holder.textViewMin.setTextColor(ContextCompat.getColor(mContext, R.color.white));

            }
        } catch (Exception e) {
            ExceptionHandler.printStackTrace(e);
        }
    }


    public void onItemClickMethod(ItemInterFace itemClick) {

        this.itemClick = itemClick;

    }

    @Override
    public Filter getFilter() {
        return null;
    }

    public interface ItemInterFace {
        public void onItemClick(View view, int position);

    }

    @Override
    public int getItemCount() {
        return slotList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public TextView textViewPrice;
        public TextView textViewMin;
        public RelativeLayout rlFirstSession;

        public ViewHolder(View v) {
            super(v);
            textViewPrice = v.findViewById(R.id.text_view_price);
            textViewMin = v.findViewById(R.id.text_view_min);
            rlFirstSession = v.findViewById(R.id.rl_first_session);
            rlFirstSession.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (itemClick != null) {
                itemClick.onItemClick(v, getAdapterPosition());
            }
        }
    }

}