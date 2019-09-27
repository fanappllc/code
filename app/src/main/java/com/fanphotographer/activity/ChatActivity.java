package com.fanphotographer.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.fanphotographer.R;
import com.fanphotographer.bean.ChatPojo;
import com.fanphotographer.data.constant.Constants;
import com.fanphotographer.fcm.MyFirebaseMessagingService;
import com.fanphotographer.utility.AppUtils;
import com.fanphotographer.utility.ErrorUtils;
import com.fanphotographer.webservice.Api;
import com.fanphotographer.webservice.ApiFactory;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;
import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;
import static com.fanphotographer.data.constant.Constants.hideSnackbar;

public class ChatActivity extends BaseActivity implements View.OnClickListener {


    private ChattingAdapter mAdapter;
    private RecyclerView recyclerViewChat;
    private ArrayList<ChatPojo> chatList;
    private String orderId;
    private String customerId;
    private String accessToken;
    private String msg;
    private String uid;
    private String name;
    private String pic="";
    private TextView noConversationTextView;
    private RelativeLayout progressRelative;
    private EditText editTextMessage;
    private BroadcastReceiver broadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        if(getIntent().hasExtra(Constants.ORDER_ID)) {
            orderId = getIntent().getStringExtra(Constants.ORDER_ID);
        }
        String json = appPreference.getString("customer_json");
        if(!Constants.isStringNullOrBlank(json)){
            try {
                JSONObject jsonObject = new JSONObject(json);
                customerId = jsonObject.optString("id");
                name = jsonObject.optString("first_name")+" "+jsonObject.optString("last_name");
                pic = jsonObject.optString("profile_img");
                accessToken = appPreference.getString(Constants.ACCESS_TOKEN);
                uid = appPreference.getString(Constants.ID);
            } catch (Exception e) {
                Log.e(Constants.LOG_CAT,e.getMessage());
            }
        }
        intView();
        if (AppUtils.isNetworkConnected()) {
            getMsglist();
        }else {
            final ViewGroup viewGroup = (ViewGroup) ((ViewGroup) ChatActivity.this.findViewById(android.R.id.content)).getChildAt(0);
            Constants.showSnackbar(ChatActivity.this, viewGroup, getResources().getString(R.string.no_internet), Constants.RETRY);
        }


    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    private void intView() {
        noConversationTextView = (TextView) findViewById(R.id.no_conversation_textView);
        progressRelative = (RelativeLayout) findViewById(R.id.progressRelative);
        progressRelative.setVisibility(View.GONE);
        TextView textViewPhotoName = (TextView) findViewById(R.id.textView_photo_name);
        CircleImageView imageViewChatUser = (CircleImageView) findViewById(R.id.imageView_chat_user);
        editTextMessage = (EditText) findViewById(R.id.editTextMessage);
        ImageView sentMsgBtn = (ImageView) findViewById(R.id.sent_msg_btn);
        ImageView backImageviewchat = (ImageView) findViewById(R.id.backImageView_chat);
        recyclerViewChat = (RecyclerView) findViewById(R.id.listViewChat);
        LinearLayoutManager recylerViewLayoutManager = new LinearLayoutManager(ChatActivity.this);
        recyclerViewChat.setLayoutManager(recylerViewLayoutManager);
        backImageviewchat.setOnClickListener(this);
        sentMsgBtn.setOnClickListener(this);

        chatList = new ArrayList<>();
        textViewPhotoName.setText(Constants.wordFirstCap(name));
        if (!pic.equals("")) {
//            Glide.with(ChatActivity.this).load(pic)
//                    .thumbnail(0.5f)
//                    .placeholder(R.mipmap.defult_user).dontAnimate()
//                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
//                    .into(imageViewChatUser);


            Glide.with(ChatActivity.this).load(pic)
                    .thumbnail(0.5f)
                    .apply(new RequestOptions().placeholder(R.mipmap.defult_user).dontAnimate())
                    .apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL))
                    .into(imageViewChatUser);
        }

    }


    private void setList() {
        try {
            if (mAdapter == null) {
                mAdapter = new ChattingAdapter(ChatActivity.this, chatList);
                recyclerViewChat.setAdapter(mAdapter);

            } else {
                mAdapter.notifyDataSetChanged();
            }

        } catch (Exception e) {
            Log.e(Constants.LOG_CAT,e.getMessage());
        }
    }


    @Override
    protected void networkConnnectivityChange() {
        super.networkConnnectivityChange();
        if (!AppUtils.isNetworkConnected()) {
            final ViewGroup viewGroup = (ViewGroup) ((ViewGroup) ChatActivity.this.findViewById(android.R.id.content)).getChildAt(0);
            Constants.showSnackbar(ChatActivity.this, viewGroup, getResources().getString(R.string.no_internet), Constants.RETRY);
        } else {
            hideSnackbar();
        }
    }

    @Override
    public void onClick(View view) {
        int i = view.getId();
        if (i == R.id.backImageView_chat) {
            Intent sentintent = new Intent(ChatActivity.this, UserLocationActivity.class);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            startActivity(sentintent);
            finish();

        } else if (i == R.id.sent_msg_btn) {
            msg = editTextMessage.getText().toString();
            if (msg.trim().length() == 0) {
                Constants.showToastAlert("Type your message here...", ChatActivity.this);
                return;
            }
            if (msg.length() == 0) {
                Constants.showToastAlert("Type your message here...", ChatActivity.this);
            } else {

                if (AppUtils.isNetworkConnected()) {
                    sendMsg();
                } else {
                    final ViewGroup viewGroup = (ViewGroup) ((ViewGroup) ChatActivity.this.findViewById(android.R.id.content)).getChildAt(0);
                    Constants.showSnackbar(ChatActivity.this, viewGroup, getResources().getString(R.string.no_internet), Constants.RETRY);
                }

            }

        }
    }



    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter("ACTION_REFRESH_USER.intent.MAIN");
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String broadCastActionType = intent.getStringExtra(Constants.BROADCAST_ACTION);
                if (broadCastActionType.equals(Constants.ACTION_REFRESH_USER)) {
                String newMessage = intent.getStringExtra("NEW_MESSAGE");
                String fromId = intent.getStringExtra("from_id");
                String toId = intent.getStringExtra("to_id");
                String message = intent.getStringExtra("message");
                String orderID = intent.getStringExtra("order_id");
                String createdAt = intent.getStringExtra("created_at");
                String notificationId = intent.getStringExtra("notification_id");
                uid = appPreference.getString(Constants.ID);

                    if (!Constants.isStringNullOrBlank(newMessage)) {
                        if (newMessage.equals(Constants.NEW_MESSAGE)) {
                            ChatPojo chatPojo;
                            if (uid.contains(fromId)) {
                                chatPojo = new ChatPojo(false, orderID, toId,
                                        fromId, message, createdAt,
                                        "", "");
                            } else {
                                chatPojo = new ChatPojo(true, orderID, fromId,
                                        toId, message, createdAt,
                                        "", "");
                            }

                            chatList.add(chatPojo);
                            if (chatList.size() > 0) {
                                noConversationTextView.setVisibility(View.GONE);
                                recyclerViewChat.setVisibility(View.VISIBLE);
                            } else {
                                noConversationTextView.setVisibility(View.VISIBLE);
                                recyclerViewChat.setVisibility(View.GONE);
                            }
                            setList();
                            recyclerViewChat.scrollToPosition(mAdapter.getItemCount() - 1);


                            if (!Constants.isStringNullOrBlank(notificationId)) {
                                MyFirebaseMessagingService.clearNotification(ChatActivity.this, Integer.valueOf(notificationId));
                            }

                        }
                    }


                }


            }
        };
        registerReceiver(broadcastReceiver, intentFilter);


    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            unregisterReceiver(broadcastReceiver);
        } catch (Exception e) {
            Log.e(Constants.LOG_CAT,e.getMessage());
        }
    }


    public class ChattingAdapter extends RecyclerView.Adapter<ChattingAdapter.ViewHolder> {
        Context mContext;
        View view;
        ArrayList<ChatPojo> chatList;


        public ChattingAdapter(Context context, ArrayList<ChatPojo> chatList) {
            this.chatList = chatList;
            mContext = context;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            view = LayoutInflater.from(mContext).inflate(R.layout.row_chat_list, parent, false);
            ViewHolder viewHolder = new ViewHolder(view);

            return viewHolder;
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, final int position) {
            final ChatPojo coment;
            coment = chatList.get(position);

            holder.commentTextRight.setText(coment.getMessage());
            holder.commentTextLeft.setText(coment.getMessage());
            holder.messageTimeLeft.setText(Constants.getDateInFormat("yyyy-MM-dd hh:mm:ss", "hh:mm aa", coment.getTime()));
            holder.messageTimeRight.setText(Constants.getDateInFormat("yyyy-MM-dd hh:mm:ss", "hh:mm aa", coment.getTime()));
            holder.layoutLeftUser.setVisibility(coment.isLeft() ? View.VISIBLE : View.GONE);
            holder.layoutRightUser.setVisibility(coment.isLeft() ? View.GONE : View.VISIBLE);


        }


        @Override
        public int getItemCount() {
            return chatList.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {


            private TextView messageTimeLeft;
            private TextView messageTimeRight;
            private TextView commentTextLeft;
            private TextView commentTextRight;
            private LinearLayout layoutLeftUser;
            private LinearLayout layoutRightUser;


            public ViewHolder(View v) {

                super(v);
                commentTextRight = (TextView) v.findViewById(R.id.commenttext_right);
                commentTextLeft = (TextView) v.findViewById(R.id.commenttext_left);
                messageTimeRight = (TextView) v.findViewById(R.id.MessageTimeRight);
                messageTimeLeft = (TextView) v.findViewById(R.id.MessageTimeleft);
                layoutLeftUser = (LinearLayout) v.findViewById(R.id.layout_left_user);
                layoutRightUser = (LinearLayout) v.findViewById(R.id.layout_right_user);
                progressRelative = (RelativeLayout) v.findViewById(R.id.progressRelative);

            }

            @Override
            public void onClick(View v) {
                Log.e(Constants.LOG_CAT,"onClick");
            }
        }

    }


    public void sendMsg() {


        Api api = ApiFactory.getClientWithoutHeader(ChatActivity.this).create(Api.class);
        HashMap<String, String> map = new HashMap<>();
        Call<ResponseBody> call;
        map.put("to_id", customerId);
        map.put("order_id", orderId);
        map.put("message", msg);
        call = api.save_message(accessToken, map);

        progressRelative.setVisibility(View.VISIBLE);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                progressRelative.setVisibility(View.GONE);

                try {
                    if (response != null && response.isSuccessful() && response.code() == 200) {
                        String output = ErrorUtils.getResponseBody(response);
                        JSONObject object = new JSONObject(output);
                        if (object.optString(Constants.SUCCESS).equalsIgnoreCase(Constants.TRUE)) {
                            Log.e(Constants.LOG_CAT, "onResponse:" + object.toString());
                            JSONObject dataObject = object.optJSONObject("data");
                            editTextMessage.setText(null);
                            if (dataObject != null) {

                                String toId = dataObject.optString("to_id");
                                String fromId = dataObject.optString("from_id");
                                String orderIid = dataObject.optString("order_id");
                                String message = dataObject.optString("message");
                                String createdAt = dataObject.optString("created_at");
                                ChatPojo chatPojo;
                                if (uid.contains(fromId)) {
                                    chatPojo = new ChatPojo(false, orderIid, toId,
                                            fromId, message, createdAt,
                                            "", "");
                                } else {
                                    chatPojo = new ChatPojo(true, orderIid, fromId,
                                            toId, message, createdAt,
                                            "", "");
                                }

                                chatList.add(chatPojo);
                                if (chatList.size() > 0) {
                                    noConversationTextView.setVisibility(View.GONE);

                                    recyclerViewChat.setVisibility(View.VISIBLE);
                                } else {
                                    noConversationTextView.setVisibility(View.VISIBLE);
                                    recyclerViewChat.setVisibility(View.GONE);
                                }
                                setList();
                                recyclerViewChat.scrollToPosition(mAdapter.getItemCount() - 1);
                            }
                            }
                            else if (object.optString(Constants.SUCCESS).equalsIgnoreCase(Constants.FALSE)) {
                            JSONArray jsonArray = object.optJSONArray(Constants.ERROR);
                            if (jsonArray == null) {
                                Constants.showToastAlert(object.getJSONObject(Constants.ERROR).getString(Constants.MESSAGE),ChatActivity.this);
                            }else {
                                Constants.showToastAlert(jsonArray.getJSONObject(0).getString(Constants.MESSAGE), ChatActivity.this);
                            }
                        }

                    } else if (response != null) {
                        if (response.code() == 400 || response.code() == 500 || response.code() == 403 || response.code() == 404 || response.code() == 401 ) {
                            if(response.code()==401){
                                appPreference.showCustomAlert(ChatActivity.this,getResources().getString(R.string.http_401_error));
                            }else {
                                Constants.showToastAlert(ErrorUtils.getHtttpCodeError(response.code()), ChatActivity.this);
                            }

                        } else {
                            String responseStr = ErrorUtils.getResponseBody(response);
                            JSONObject jsonObject = new JSONObject(responseStr);
                            Constants.showToastAlert(ErrorUtils.checkJosnErrorBody(jsonObject), ChatActivity.this);
                        }
                    }
                }catch (Exception e){
                    Log.e(Constants.LOG_CAT,e.getMessage());
                }

            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                progressRelative.setVisibility(View.GONE);
                Constants.showToastAlert(ErrorUtils.getString(R.string.failled), ChatActivity.this);
            }
        });

    }

    public void getMsglist() {

        Api api = ApiFactory.getClientWithoutHeader(ChatActivity.this).create(Api.class);
        Call<ResponseBody> call;
        call = api.get_msg(accessToken, Integer.valueOf(orderId));
        Log.e(Constants.LOG_CAT, "HEADERS : " + call.request().headers());
        Constants.showProgressDialog(ChatActivity.this, Constants.LOADING);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Constants.hideProgressDialog();

                try {
                    if (response != null && response.isSuccessful() && response.code() == 200) {
                        String output = ErrorUtils.getResponseBody(response);
                        JSONObject object = new JSONObject(output);
                        if (object.optString(Constants.SUCCESS).equalsIgnoreCase(Constants.TRUE)) {
                            Log.e(Constants.LOG_CAT, "onResponse:" + object.toString());
                             JSONArray jsonArray = object.optJSONArray("data");
                            chatList.clear();
                            if (jsonArray != null && jsonArray.length() > 0) {
                                noConversationTextView.setVisibility(View.GONE);
                                recyclerViewChat.setVisibility(View.VISIBLE);
                                for (int i = 0; i < jsonArray.length(); i++) {

                                    JSONObject jsonObject = jsonArray.optJSONObject(i);
                                    String toId = jsonObject.optString("to_id");
                                    String fromId = jsonObject.optString("from_id");
                                    String orderIid = jsonObject.optString("order_id");
                                    String message = jsonObject.optString("message");
                                    String createdAt = jsonObject.optString("created_at");
                                    ChatPojo chatPojo;
                                    if (uid.contains(fromId)) {
                                        chatPojo = new ChatPojo(false, orderIid, toId,
                                                fromId, message, createdAt,
                                                "", "");
                                    } else {
                                        chatPojo = new ChatPojo(true, orderIid, fromId,
                                                toId, message, createdAt,
                                                "", "");
                                    }


                                    chatList.add(i, chatPojo);

                                }
                                setList();
                                recyclerViewChat.scrollToPosition(mAdapter.getItemCount() - 1);
                            }else {
                                noConversationTextView.setVisibility(View.VISIBLE);
                                recyclerViewChat.setVisibility(View.GONE);
                            }

                        } else if (object.optString(Constants.SUCCESS).equalsIgnoreCase(Constants.FALSE)) {
                            JSONArray jsonArray = object.optJSONArray(Constants.ERROR);
                            if (jsonArray == null) {
                                Constants.showToastAlert(object.getJSONObject(Constants.ERROR).getString(Constants.MESSAGE),ChatActivity.this);
                            }else {
                                Constants.showToastAlert(jsonArray.getJSONObject(0).getString(Constants.MESSAGE), ChatActivity.this);
                            }
                        }

                    } else if (response != null) {
                        if (response.code() == 400 || response.code() == 500 || response.code() == 403 || response.code() == 404 || response.code() == 401 ) {
                            if(response.code()==401){
                                appPreference.showCustomAlert(ChatActivity.this,getResources().getString(R.string.http_401_error));
                            }else {
                                Constants.showToastAlert(ErrorUtils.getHtttpCodeError(response.code()), ChatActivity.this);
                            }

                        } else {
                            String responseStr = ErrorUtils.getResponseBody(response);
                            JSONObject jsonObject = new JSONObject(responseStr);
                            Constants.showToastAlert(ErrorUtils.checkJosnErrorBody(jsonObject), ChatActivity.this);
                        }
                    }
                }catch (Exception e){
                    Log.e(Constants.LOG_CAT,e.getMessage());
                }

            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Constants.hideProgressDialog();
                Constants.showToastAlert(ErrorUtils.getString(R.string.failled), ChatActivity.this);
            }
        });

    }
}
