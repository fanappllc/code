package com.fancustomer.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.fancustomer.R;
import com.fancustomer.bean.PhotoBean;

import java.util.ArrayList;

/**
 * Created by Shubham on 12/13/2017.
 */

public class SavePhotoAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    View view;
    ProgressBar progress_bar;
    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;
    SavePhotoAdapter.ItemInterFace itemClick;
    ArrayList<PhotoBean> itemObjects;
    Context context;

    public SavePhotoAdapter(ArrayList<PhotoBean> itemObjects) {
        this.itemObjects = itemObjects;
    }

    public SavePhotoAdapter(Context context, ArrayList<PhotoBean> itemObjects) {
        this.context = context;
        this.itemObjects = itemObjects;
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
            //((HeaderViewHolder) holder).dateTextView.setText(Constants.getDateInFormat("yyyy-MM-dd hh:mm:ss", "MMM dd,yyyy HH:mm a", itemObjects.get(position).getDateText()));
            ((HeaderViewHolder) holder).textViewName.setText(itemObjects.get(position).getPhotographer_name() + " Photo");
        } else if (getItemViewType(position) == TYPE_ITEM) {
            String picture = itemObjects.get(position).getImagePath();
            if (!picture.equals("")) {
                Glide.with(context).load(picture)
                        .thumbnail(0.5f)
                        .placeholder(R.mipmap.defult_user).dontAnimate()
                        .diskCacheStrategy(DiskCacheStrategy.SOURCE).listener(new RequestListener<String, GlideDrawable>() {
                    @Override
                    public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                        progress_bar.setVisibility(View.GONE);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        //progress_bar.setVisibility(View.GONE);
                        return false;
                    }
                })
                        .into(((ItemViewHolder) holder).image);
            }
//            ((ItemViewHolder) holder).image.setImageResource(Integer.parseInt(url));
        }
    }

    public void onItemClickMethod(SavePhotoAdapter.ItemInterFace itemClick) {

        this.itemClick = itemClick;


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

        public ItemViewHolder(View v) {

            super(v);
            name = itemView.findViewById(R.id.name);
            image = itemView.findViewById(R.id.image);
            progress_bar = itemView.findViewById(R.id.progress_bar);
            image.setOnClickListener(this);
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
