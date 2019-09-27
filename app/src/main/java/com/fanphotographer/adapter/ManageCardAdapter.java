package com.fanphotographer.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.daimajia.swipe.SwipeLayout;
import com.daimajia.swipe.adapters.RecyclerSwipeAdapter;
import com.fanphotographer.R;
import com.fanphotographer.data.constant.Constants;
import java.util.List;


public class ManageCardAdapter extends RecyclerSwipeAdapter<ManageCardAdapter.SimpleViewHolder> {

    private List<String> notificationList;

    public ManageCardAdapter(Context context, List<String> notificationList) {
        this.notificationList = notificationList;
    }

    @Override
    public SimpleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_manage_card, parent, false);
        return new SimpleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final SimpleViewHolder viewHolder, final int position) {



        viewHolder.swipeLayout.setShowMode(SwipeLayout.ShowMode.PullOut);
        viewHolder.swipeLayout.addDrag(SwipeLayout.DragEdge.Right, viewHolder.swipeLayout.findViewById(R.id.bottom_wrapper));
        viewHolder.swipeLayout.setLeftSwipeEnabled(false);
        viewHolder.swipeLayout.addSwipeListener(new SwipeLayout.SwipeListener() {
            @Override
            public void onClose(SwipeLayout layout) {
                Log.e(Constants.LOG_CAT,"onClose");
            }

            @Override
            public void onUpdate(SwipeLayout layout, int leftOffset, int topOffset) {
                Log.e(Constants.LOG_CAT,"onUpdate");
            }

            @Override
            public void onStartOpen(SwipeLayout layout) {
                Log.e(Constants.LOG_CAT,"onStartOpen");
            }

            @Override
            public void onOpen(SwipeLayout layout) {
                Log.e(Constants.LOG_CAT,"onOpen");
            }

            @Override
            public void onStartClose(SwipeLayout layout) {
                Log.e(Constants.LOG_CAT,"onStartClose");
            }

            @Override
            public void onHandRelease(SwipeLayout layout, float xvel, float yvel) {
                Log.e(Constants.LOG_CAT,"onHandRelease");
            }
        });

        viewHolder.swipeLayout.getSurfaceView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e(Constants.LOG_CAT,"onClick");
            }
        });

        mItemManger.bindView(viewHolder.itemView, position);
    }

    @Override
    public int getItemCount() {
        return notificationList.size();
    }

    @Override
    public int getSwipeLayoutResourceId(int position) {
        return R.id.swipe;
    }





    public class SimpleViewHolder extends RecyclerView.ViewHolder {
        SwipeLayout swipeLayout;

        public SimpleViewHolder(View itemView) {
            super(itemView);
            swipeLayout = (SwipeLayout) itemView.findViewById(R.id.swipe);
        }
    }
}