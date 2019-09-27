package com.fanphotographer.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.daimajia.swipe.SwipeLayout;
import com.daimajia.swipe.adapters.RecyclerSwipeAdapter;
import com.fanphotographer.R;
import java.util.List;


public class NotificationAdapter extends RecyclerSwipeAdapter<NotificationAdapter.SimpleViewHolder> {

    private List<String> notificationList;

    public NotificationAdapter(Context context, List<String> notificationList) {
        Context mContext = context;
        this.notificationList = notificationList;
    }

    @Override
    public SimpleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.notification_swipe_row_item, parent, false);
        return new SimpleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final SimpleViewHolder viewHolder, final int position) {


        if (position == 0) {
            viewHolder.newTextView.setVisibility(View.VISIBLE);
        } else if (position == 1) {
            viewHolder.newTextView.setVisibility(View.VISIBLE);
        } else {
            viewHolder.newTextView.setVisibility(View.GONE);
        }
        viewHolder.swipeLayout.setShowMode(SwipeLayout.ShowMode.PullOut);
        viewHolder.textViewMessage.setText("Lorem lpsum is simply dummy text of the printing ");
        viewHolder.swipeLayout.addDrag(SwipeLayout.DragEdge.Right, viewHolder.swipeLayout.findViewById(R.id.bottom_wrapper));
        viewHolder.swipeLayout.setLeftSwipeEnabled(false);
        viewHolder.swipeLayout.addSwipeListener(new SwipeLayout.SwipeListener() {
            @Override
            public void onClose(SwipeLayout layout) {
                //when the SurfaceView totally cover the BottomView.
            }

            @Override
            public void onUpdate(SwipeLayout layout, int leftOffset, int topOffset) {
                //you are swiping.
            }

            @Override
            public void onStartOpen(SwipeLayout layout) {
                //you are onStartOpen.
            }

            @Override
            public void onOpen(SwipeLayout layout) {
                //when the BottomView totally show.
            }

            @Override
            public void onStartClose(SwipeLayout layout) {
                //you are onStartClose.
            }

            @Override
            public void onHandRelease(SwipeLayout layout, float xvel, float yvel) {
                //you are onHandRelease.
            }
        });

        viewHolder.swipeLayout.getSurfaceView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //you are onClick.
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
        TextView textViewMessage;
        TextView newTextView;


        public SimpleViewHolder(View itemView) {
            super(itemView);

            swipeLayout = (SwipeLayout) itemView.findViewById(R.id.swipe);
            textViewMessage = (TextView) itemView.findViewById(R.id.textView_message);
            newTextView = (TextView) itemView.findViewById(R.id.new_textView);

        }
    }
}