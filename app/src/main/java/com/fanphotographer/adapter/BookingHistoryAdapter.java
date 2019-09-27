package com.fanphotographer.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.fanphotographer.R;
import com.fanphotographer.activity.BillDetailActivity;
import com.fanphotographer.bean.BookingBean;
import com.fanphotographer.data.constant.Constants;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;


public class BookingHistoryAdapter extends RecyclerView.Adapter<BookingHistoryAdapter.ViewHolder> {

    private Context mContext;
    private ItemInterFace itemClick;
    private List<BookingBean> historyList;

    public BookingHistoryAdapter(Context context, List<BookingBean> historyList) {

       this.historyList = historyList;
        this.mContext = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_booking_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {

        if (position == 0) {
            holder.view.setVisibility(View.INVISIBLE);
        } else {
            holder.view.setVisibility(View.VISIBLE);
        }

        holder.dateTextView.setText(Constants.getDateInFormat("yyyy-MM-dd hh:mm:ss", "MMM dd,yyyy", historyList.get(position).getOrder_created_at()));
        holder.timeTextView.setText(Constants.getDateInFormat("yyyy-MM-dd hh:mm:ss", "hh:mm aa", historyList.get(position).getOrder_created_at()));
        holder.firstNameTextView.setText(historyList.get(position).getFirst_name() + " " + historyList.get(position).getLast_name());
//        holder.totalAmountTextView.setText("Total amount: $ " + historyList.get(position).getTotal_amount());
        if(!Constants.isStringNullOrBlank( historyList.get(position).getTotal_amount()))
            holder.totalAmountTextView.setText("Total amount: $ "+new DecimalFormat("#,##0.00").format(Double.parseDouble( historyList.get(position).getTotal_amount())));
        holder.orderIdtextView.setText("ID :" + historyList.get(position).getOrder_id());
        if(historyList.get(position).getOrder_status().equalsIgnoreCase("completed")){
            holder.status.setText(Constants.PAID);
            holder.status.setTextColor(Color.parseColor("#19DEB1"));
        }else {
            holder.status.setText(Constants.CANCELLED);
            holder.status.setTextColor(Color.parseColor("#FE8D01"));
        }

        String profileImage = historyList.get(position).getProfile_image();
        if (!profileImage.equals("")) {


            Glide.with(mContext).load(profileImage)
                    .thumbnail(0.5f)
                    .apply(new RequestOptions().placeholder(R.mipmap.defult_user).dontAnimate())
                    .apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL))
                    .into(holder.imageViewUser);

//            Glide.with(mContext).load(profileImage)
//                    .thumbnail(0.5f)
//                    .placeholder(R.mipmap.defult_user).dontAnimate()
//                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
//                    .listener(new RequestListener<String, GlideDrawable>() {
//                        @Override
//                        public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
//                            return false;
//                        }
//
//                        @Override
//                        public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
//                            return false;
//                        }
//                    }).into(holder.imageViewUser);

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

        TextView dateTextView;
        TextView timeTextView;
        TextView status;
        TextView firstNameTextView;
        TextView orderIdtextView;
        TextView totalAmountTextView;
        CircleImageView imageViewUser;
        View view;

        ViewHolder(View v) {
            super(v);
            dateTextView = (TextView) v.findViewById(R.id.dateTextView);
            timeTextView = (TextView) v.findViewById(R.id.timeTextView);
            firstNameTextView = (TextView) v.findViewById(R.id.firstNameTextView);
            totalAmountTextView = (TextView) v.findViewById(R.id.totalAmountTextView);
            status = (TextView) v.findViewById(R.id.status);
            orderIdtextView = (TextView) v.findViewById(R.id.order_id_textView);
            imageViewUser = (CircleImageView) v.findViewById(R.id.imageView_user);

            view = (View) v.findViewById(R.id.view);
        }

        @Override
        public void onClick(View v) {
            if (itemClick != null) {
                itemClick.onItemClick(v, getPosition());
            }
        }
    }

}