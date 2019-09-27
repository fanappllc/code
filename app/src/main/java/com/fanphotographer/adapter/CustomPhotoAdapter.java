package com.fanphotographer.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.fanphotographer.R;
import com.fanphotographer.bean.PhotoBean;
import java.util.List;


public class CustomPhotoAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private View view;
    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;
    private CustomPhotoAdapter.ItemInterFace itemClick;
    private List<PhotoBean> itemObjects;

    public CustomPhotoAdapter(List<PhotoBean> itemObjects) {
        this.itemObjects = itemObjects;
    }

    public CustomPhotoAdapter(Context context, List<PhotoBean> itemObjects) {
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
            itemObjects.get(position).getType();
            ((HeaderViewHolder) holder).dateTextView.setText(itemObjects.get(position).getDateText());
        } else if (getItemViewType(position) == TYPE_ITEM) {

            ((ItemViewHolder) holder).image.setImageResource(itemObjects.get(position).getImageID());
        }
    }

    public void onItemClickMethod(CustomPhotoAdapter.ItemInterFace itemClick) {

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

        ItemViewHolder(View v) {

            super(v);
            name = (TextView) itemView.findViewById(R.id.name);
            image = (ImageView) itemView.findViewById(R.id.image);

        }

        @Override
        public void onClick(View v) {
            if (itemClick != null) {
                itemClick.onItemClick(v, getPosition());
            }
        }


    }

    public class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView dateTextView;
        TextView textViewName;

        HeaderViewHolder(View itemView) {
            super(itemView);
            dateTextView = (TextView) itemView.findViewById(R.id.dateTextView);
            textViewName = (TextView) itemView.findViewById(R.id.textView_name);


        }
    }


}
