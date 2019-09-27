package com.fanphotographer.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.fanphotographer.R;
import com.fanphotographer.data.constant.Constants;
import org.json.JSONObject;
import java.util.List;
import java.util.concurrent.TimeUnit;
import de.hdodenhof.circleimageview.CircleImageView;


public class HomeAdapter extends RecyclerView.Adapter<HomeAdapter.MyViewHolder> {

    private List<JSONObject> myCardsList;
    private Context context;
    private ItemEvenListener itemEvenListener;

    public interface ItemEvenListener {

        public void onAcceptclick(int position);

        public void onDeclineclick(int position);

        public void onFullviewClecked(int position);
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        CircleImageView userImage;
        TextView tvUsername;
        TextView tvTitle;
        TextView tvDatewithtime;
        TextView tvHours;
        TextView tvMinute;
        private TextView acceptTextView;
        private TextView declineTexView;

        public MyViewHolder(View view) {
            super(view);

            userImage = (CircleImageView) view.findViewById(R.id.user_image);
            acceptTextView = (TextView) view.findViewById(R.id.accept_textView);
            declineTexView = (TextView) view.findViewById(R.id.declineTexView);

            tvUsername = (TextView) view.findViewById(R.id.tv_user_name);
            tvTitle = (TextView) view.findViewById(R.id.tv_title);
            tvDatewithtime = (TextView) view.findViewById(R.id.tv_date_with_time);
            tvHours = (TextView) view.findViewById(R.id.tv_hours);
            tvMinute = (TextView) view.findViewById(R.id.tv_minute);
        }
    }

    public HomeAdapter(Context context, List<JSONObject> orderlist, ItemEvenListener itemEvenListe) {
        this.myCardsList = orderlist;
        this.context = context;
        this.itemEvenListener = itemEvenListe;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_home_list, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {

        JSONObject jsonObject = myCardsList.get(position);
        JSONObject getCustomer = jsonObject.optJSONObject("get_customer");
        JSONObject orderSlot = jsonObject.optJSONObject("order_slot");
        String image = getCustomer.optString("profile_img");

        holder.tvUsername.setText(getCustomer.optString("first_name")+" "+ getCustomer.optString("last_name"));
        holder.tvTitle.setText(jsonObject.optString("location"));
        holder.tvDatewithtime.setText(Constants.getDateInFormat("yyyy-MM-dd hh:mm:ss","MMM dd,yyyy hh:mm aa",jsonObject.optString("created_at")));
        holder.tvHours.setText(jsonObject.optString(orderSlot.optString("")));
        holder.tvMinute.setText(jsonObject.optString("arriving_time"));

        if(!Constants.isStringNullOrBlank(image)){
            showImage(holder.userImage,getCustomer.optString("profile_img"));
        }

        String slots = orderSlot.optString("slot");
        if(!Constants.isStringNullOrBlank(slots)){
            int timeSlots = Integer.parseInt(slots);
            if (timeSlots < 60) {
                holder.tvHours.setText("00:" + timeSlots + " " + "Min");
            } else {
                long hourNew = TimeUnit.MINUTES.toMillis(timeSlots);
                String hms = String.format("%02d:%02d", TimeUnit.MILLISECONDS.toHours(hourNew),
                        TimeUnit.MILLISECONDS.toMinutes(hourNew) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(hourNew)));
                holder.tvHours.setText("" + hms + " Hour");
            }
        }

        holder.acceptTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                itemEvenListener.onAcceptclick(position);
            }
        });

        holder.declineTexView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                itemEvenListener.onDeclineclick(position);
            }
        });
    }

    private void showImage(ImageView imageview, String imagepath) {
        Glide.with(context).load(imagepath)
                .thumbnail(0.5f)
                .apply(new RequestOptions().placeholder(R.mipmap.defult_user).dontAnimate())
                .apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL))
                .into(imageview);
//        Glide.with(context)
//                .load(imagepath)
//                .placeholder(R.mipmap.default_user) // optional
//                .error(R.mipmap.default_user)
//                .into(imageview);
    }

    @Override
    public int getItemCount() {
        return myCardsList.size();
    }
}