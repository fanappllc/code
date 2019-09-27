package com.fancustomer.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.fancustomer.R;
import com.fancustomer.bean.PhotoBean;
import com.fancustomer.data.constant.Constants;

import java.util.ArrayList;

/**
 * Created by Shubham on 12/13/2017.
 */

public class CustomPhotoAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    View view;
    ProgressBar progressBar;
    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;
    CustomPhotoAdapter.ItemInterFace itemClick;
    ArrayList<PhotoBean> itemObjects;
    Context context;
    boolean isDate;
    public boolean isRadio;

    public CustomPhotoAdapter(ArrayList<PhotoBean> itemObjects) {
        this.itemObjects = itemObjects;
    }

    public CustomPhotoAdapter(Context context, ArrayList<PhotoBean> itemObjects, boolean isDate) {
        this.context = context;
        this.itemObjects = itemObjects;
        this.isDate = isDate;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEADER) {
            View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.header_layout, parent, false);
            return new HeaderViewHolder(layoutView);
        } else if (viewType == TYPE_ITEM) {
            View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_photo, parent, false);
            return new ItemViewHolder(layoutView);
        }
        throw new RuntimeException("No match for " + viewType + ".");
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {

        if (getItemViewType(position) == TYPE_HEADER) {
            if (isDate) {
                ((HeaderViewHolder) holder).dateTextView.setText(Constants.getDateInFormat("yyyy-MM-dd hh:mm:ss", "MMM dd,yyyy hh:mm aa", itemObjects.get(position).getDateText()));
            }
            ((HeaderViewHolder) holder).textViewName.setText(Constants.wordFirstCap(itemObjects.get(position).getPhotographer_name()) + " Photo");
        } else if (getItemViewType(position) == TYPE_ITEM) {


            if (isRadio) {
                if (itemObjects.get(position).isSelected()) {
                    ((ItemViewHolder) holder).checkboxImageView.setVisibility(View.VISIBLE);
                    ((ItemViewHolder) holder).checkboxImageView.setImageResource(R.mipmap.radio_on);
                } else {
                    ((ItemViewHolder) holder).checkboxImageView.setVisibility(View.VISIBLE);
                    ((ItemViewHolder) holder).checkboxImageView.setImageResource(R.mipmap.radio_off);
                }
            } else {
                ((ItemViewHolder) holder).checkboxImageView.setVisibility(View.GONE);
            }
            String picture = itemObjects.get(position).getImagePath();
            if (!picture.equals("")) {
                Glide.with(context).load(picture)
                        .thumbnail(0.5f)
                        .placeholder(R.mipmap.default_fan).dontAnimate()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(((ItemViewHolder) holder).image);
            }
        }
    }

    public void onItemClickMethod(CustomPhotoAdapter.ItemInterFace itemClick) {

        this.itemClick = itemClick;


    }

    public ArrayList<PhotoBean> getList() {


        return itemObjects;
    }

    public interface ItemInterFace {
        public void onItemClick(View view, int position);

    }


    @Override
    public int getItemCount() {
        return itemObjects.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (itemObjects.get(position).getType() == 0)
            return TYPE_HEADER;
        return TYPE_ITEM;
    }

    public class ItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView name;
        ImageView image;
        RelativeLayout imageRelativeLayout;
        ImageView checkboxImageView;

        public ItemViewHolder(View v) {

            super(v);
            name = itemView.findViewById(R.id.name);
            image = itemView.findViewById(R.id.image);
            progressBar = itemView.findViewById(R.id.progress_bar);
            imageRelativeLayout = itemView.findViewById(R.id.imageRelativeLayout);
            image.setOnClickListener(this);
            checkboxImageView = itemView.findViewById(R.id.checkboxImageView);
        }

        @Override
        public void onClick(View v) {

            if (itemClick != null) {
                itemClick.onItemClick(v, getAdapterPosition());
            }
        }

    }

    public class HeaderViewHolder extends RecyclerView.ViewHolder {
        public TextView dateTextView;
        public TextView textViewName;

        public HeaderViewHolder(View itemView) {
            super(itemView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
            textViewName = itemView.findViewById(R.id.textView_name);


        }
    }


}
