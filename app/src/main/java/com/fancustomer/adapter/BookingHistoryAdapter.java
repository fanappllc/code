package com.fancustomer.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.TextView;


import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.fancustomer.R;

import com.fancustomer.bean.BookingBean;
import com.fancustomer.data.constant.Constants;

import java.text.DecimalFormat;
import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by shuubham Pancholi on 6/29/2017.
 */

public class BookingHistoryAdapter extends RecyclerView.Adapter<BookingHistoryAdapter.ViewHolder> {

    private Context mContext;
    private View view;
    private ItemInterFace itemClick;
    private ArrayList<BookingBean> historyList;

    public BookingHistoryAdapter(Context context, ArrayList<BookingBean> historyList) {

        this.historyList = historyList;
        this.mContext = context;

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        view = LayoutInflater.from(mContext).inflate(R.layout.item_booking_history, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {

        if (position == 0) {
            holder.view.setVisibility(View.INVISIBLE);

        } else {
            holder.view.setVisibility(View.VISIBLE);
        }

        if (historyList.get(position).getOrder_status().equals("completed")) {
            holder.orderStatusTextView.setText("Paid");
            holder.orderStatusTextView.setTextColor(mContext.getResources().getColor(R.color.green));
        } else if (historyList.get(position).getOrder_status().equals("cancelled")) {
            holder.orderStatusTextView.setText("Cancelled");
            holder.orderStatusTextView.setTextColor(mContext.getResources().getColor(R.color.dark_orange));
        }

        holder.dateTextView.setText(Constants.getDateInFormat("yyyy-MM-dd hh:mm:ss", "MMM dd,yyyy", historyList.get(position).getOrder_created_at()));
        holder.timeTextView.setText(Constants.getDateInFormat("yyyy-MM-dd hh:mm:ss", "hh:mm aa", historyList.get(position).getOrder_created_at()));
        holder.firstNameTextView.setText(historyList.get(position).getFirst_name() + " " + historyList.get(position).getLast_name());
        holder.totalAmountTextView.setText("Total amount: $ " + new DecimalFormat("#,##0.00").format(Double.parseDouble(historyList.get(position).getTotal_amount())));
        holder.orderIdTextView.setText("ID :" + historyList.get(position).getOrder_id());
        String profileImage = historyList.get(position).getProfile_image();
        if (!profileImage.equals("")) {
            Glide.with(mContext).load(profileImage)
                    .thumbnail(0.5f)
                    .placeholder(R.mipmap.defult_user).dontAnimate()
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .listener(new RequestListener<String, GlideDrawable>() {
                        @Override
                        public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                            return false;
                        }
                    }).into(holder.imageViewUser);

        }


    }

    public void onItemClickMethod(ItemInterFace itemClick) {

        this.itemClick = itemClick;

    }

    public interface ItemInterFace {
        public void onItemClick(View view, int position);

    }

    @Override
    public int getItemCount() {
        return historyList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView dateTextView;
        public TextView timeTextView;
        public TextView firstNameTextView;
        public TextView totalAmountTextView;
        public CircleImageView imageViewUser;
        public TextView orderIdTextView;
        public TextView orderStatusTextView;
        public View view;

        public ViewHolder(View v) {
            super(v);
            orderStatusTextView = v.findViewById(R.id.orderStatusTextView);
            orderIdTextView = v.findViewById(R.id.order_id_textView);
            dateTextView = v.findViewById(R.id.dateTextView);
            timeTextView = v.findViewById(R.id.timeTextView);
            firstNameTextView = v.findViewById(R.id.firstNameTextView);
            totalAmountTextView = v.findViewById(R.id.totalAmountTextView);
            imageViewUser = v.findViewById(R.id.imageView_user);

            view = v.findViewById(R.id.view);
        }

        @Override
        public void onClick(View v) {
            if (itemClick != null) {
                itemClick.onItemClick(v, getAdapterPosition());
            }
        }
    }

}