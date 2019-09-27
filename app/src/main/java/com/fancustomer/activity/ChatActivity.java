package com.fancustomer.activity;

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
import com.fancustomer.R;
import com.fancustomer.bean.ChatPojo;
import com.fancustomer.data.constant.Constants;
import com.fancustomer.data.preference.AppPreference;
import com.fancustomer.fcm.MyFirebaseMessagingService;
import com.fancustomer.helper.ErrorUtils;
import com.fancustomer.helper.ExceptionHandler;
import com.fancustomer.webservice.Api;
import com.fancustomer.webservice.ApiFactory;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

import static com.fancustomer.activity.TrackPhotographer.trackPhotographer;


public class ChatActivity extends BaseActivity implements View.OnClickListener {


    private ChattingAdapter mAdapter;
    private RecyclerView recyclerViewChat;
    private ArrayList<ChatPojo> chatList;
    private LinearLayoutManager recylerViewLayoutManager;
    private ImageView backImageView_chat;
    private String orderID = "";
    private String photographerId = "";
    private ImageView sent_msg_btn;
    private EditText editTextMessage;
    private String messageStr = "";
    private String userID = "";
    private String photographerName = "";
    private String photographerImage = "";
    private TextView textViewPhotoName;
    private CircleImageView imageViewChatUser;
    private BroadcastReceiver broadcastReceiver;
    private RelativeLayout progressRelative;
    private TextView noConversationTextView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        orderID = AppPreference.getInstance(ChatActivity.this).getString("orderID");
        photographerId = AppPreference.getInstance(ChatActivity.this).getString("photographer_id");
        photographerName = AppPreference.getInstance(ChatActivity.this).getString("photographer_name");
        photographerImage = AppPreference.getInstance(ChatActivity.this).getString("profile_image_new");
        userID = AppPreference.getInstance(ChatActivity.this).getString(Constants.USER_ID);
        intView();
        if (Constants.isInternetOn(ChatActivity.this)) {
            getMessageAPI();
        } else {
            final ViewGroup viewGroup = (ViewGroup) ((ViewGroup) this
                    .findViewById(android.R.id.content)).getChildAt(0);
            showSnackbar(viewGroup, getResources().getString(R.string.no_internet), Constants.RETRY);
        }

    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    private void intView() {
        noConversationTextView  = (TextView) findViewById(R.id.no_conversation_textView);
        progressRelative = (RelativeLayout) findViewById(R.id.progressRelative);
        progressRelative.setVisibility(View.GONE);
        textViewPhotoName = (TextView) findViewById(R.id.textView_photo_name);
        imageViewChatUser = (CircleImageView) findViewById(R.id.imageView_chat_user);
        editTextMessage = (EditText) findViewById(R.id.editTextMessage);
        sent_msg_btn = (ImageView) findViewById(R.id.sent_msg_btn);
        backImageView_chat = (ImageView) findViewById(R.id.backImageView_chat);
        recyclerViewChat = (RecyclerView) findViewById(R.id.listViewChat);
        recylerViewLayoutManager = new LinearLayoutManager(ChatActivity.this);
        recyclerViewChat.setLayoutManager(recylerViewLayoutManager);
        backImageView_chat.setOnClickListener(this);
        sent_msg_btn.setOnClickListener(this);

        textViewPhotoName.setText(Constants.wordFirstCap(photographerName));
        if (!photographerImage.equals("")) {
            Glide.with(ChatActivity.this).load(photographerImage)
                    .thumbnail(0.5f)
                    .placeholder(R.mipmap.defult_user).dontAnimate()
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .into(imageViewChatUser);
        }


        chatList = new ArrayList<>();

    }

    @Override
    protected void networkConnnectivityChange() {
        super.networkConnnectivityChange();
        if (!Constants.isInternetOn(ChatActivity.this)) {
            final ViewGroup viewGroup = (ViewGroup) ((ViewGroup) ChatActivity.this.findViewById(android.R.id.content)).getChildAt(0);
            showSnackbar(viewGroup, getResources().getString(R.string.no_internet), Constants.RETRY);
        } else {
            hideSnackbar();
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
            ExceptionHandler.printStackTrace(e);
        }
    }

    @Override
    public void onClick(View view) {
        int i = view.getId();
        if (i == R.id.backImageView_chat) {
            onBackPressed();
        } else if (i == R.id.sent_msg_btn) {
            sendData();

        }
    }


    private void sendData() {
        messageStr = editTextMessage.getText().toString().trim();

        if (messageStr.equals("")) {
            Constants.showToastAlert("Type your message here...", ChatActivity.this);
        } else {
            if (Constants.isInternetOn(ChatActivity.this)) {
                sendMessageAPI(messageStr);
            } else {
                final ViewGroup viewGroup = (ViewGroup) ((ViewGroup) this
                        .findViewById(android.R.id.content)).getChildAt(0);
                showSnackbar(viewGroup, getResources().getString(R.string.no_internet), Constants.RETRY);
            }
        }


    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(ChatActivity.this, TrackPhotographer.class);
        startActivity(intent);
        finish();

    }

    public void getMessageAPI() {


        Api api = ApiFactory.getClientWithoutHeader(ChatActivity.this).create(Api.class);
        Call<ResponseBody> call;

        call = api.getMessage(AppPreference.getInstance(ChatActivity.this).getString(Constants.ACCESS_TOKEN), orderID);
        Log.e(Constants.LOG_CAT, "FAN Message API------------------->>>>>:" + " " + call.request().url());
        Log.e(Constants.LOG_CAT, "HEADERS : " + call.request().headers());

        Constants.showProgressDialog(ChatActivity.this, "Loading");


        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Constants.hideProgressDialog();

                try {
                    if (response.isSuccessful()) {
                        String output = ErrorUtils.getResponseBody(response);
                        JSONObject object = new JSONObject(output);


                        Log.d("tag", object.toString(1));

                        if (object.optString(Constants.SUCCESS).equalsIgnoreCase(Constants.TRUE)) {

                            Log.e(Constants.LOG_CAT, "onResponse: FAN Message API LIST=============>>>>>>>>>>" + object.toString());
                            JSONArray jsonArray = object.optJSONArray("data");
                            if (jsonArray != null && jsonArray.length() > 0) {
                                noConversationTextView.setVisibility(View.GONE);
                                recyclerViewChat.setVisibility(View.VISIBLE);
                                for (int i = 0; i < jsonArray.length(); i++) {

                                    JSONObject jsonObject = jsonArray.optJSONObject(i);
                                    String toId = jsonObject.optString("to_id");
                                    String fromId = jsonObject.optString("from_id");
                                    String orderId = jsonObject.optString("order_id");
                                    String message = jsonObject.optString("message");
                                    String createdAt = jsonObject.optString("created_at");
                                    ChatPojo chatPojo;
                                    if (userID.contains(fromId)) {
                                        chatPojo = new ChatPojo(false, orderId, toId,
                                                fromId, message, createdAt,
                                                "", "");
                                    } else {
                                        chatPojo = new ChatPojo(true, orderId, fromId,
                                                toId, message, createdAt,
                                                "", "");
                                    }


                                    chatList.add(i, chatPojo);

                                }
                                setList();
                                recyclerViewChat.scrollToPosition(mAdapter.getItemCount() - 1);

                            } else {
                                noConversationTextView.setVisibility(View.VISIBLE);
                                recyclerViewChat.setVisibility(View.GONE);
                            }

                        } else if (object.optString(Constants.SUCCESS).equalsIgnoreCase(Constants.FALSE)) {
                            ErrorUtils.showFalseMessage(object, ChatActivity.this);
                        }
                    } else if (response.code() == 400 || response.code() == 500 || response.code() == 403 || response.code() == 404 || response.code() == 401) {
                        if (response.code() == 401) {
                            Constants.showSessionExpireAlert(ChatActivity.this);
                        } else {
                            Constants.showToastAlert(ErrorUtils.getHtttpCodeError(response.code()), ChatActivity.this);
                        }

                    } else {
                        String responseStr = ErrorUtils.getResponseBody(response);
                        JSONObject jsonObject = new JSONObject(responseStr);
                        Constants.showToastAlert(ErrorUtils.checkJosnErrorBody(jsonObject), ChatActivity.this);
                    }
                } catch (JSONException e) {
                    Constants.hideProgressDialog();
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Constants.hideProgressDialog();
            }
        });


    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            IntentFilter intentFilter = new IntentFilter("ACTION_REFRESH_USER.intent.MAIN");
            broadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    String broadCastActionType = intent.getStringExtra(Constants.BROADCAST_ACTION);
                    String newMessage = intent.getStringExtra("NEW_MESSAGE");
                    String cancelSessionByPhotographer = intent.getStringExtra("CANCEL_SESSION_BY_PHOTOGRAPHER");
                    String fromId = intent.getStringExtra("from_id");
                    String toId = intent.getStringExtra("to_id");
                    String message = intent.getStringExtra("message");
                    String orderId = intent.getStringExtra("order_id");
                    String createdAt = intent.getStringExtra("created_at");
                    String notificationId = intent.getStringExtra("notification_id");
                    if (broadCastActionType.equals(Constants.ACTION_REFRESH_USER)) {
                        if (!Constants.isStringNullOrBlank(newMessage)) {
                            if (newMessage.equals(Constants.NEW_MESSAGE)) {
                                ChatPojo chatPojo;
                                if (userID.contains(fromId)) {
                                    chatPojo = new ChatPojo(false, orderId, toId,
                                            fromId, message, createdAt,
                                            "", "");
                                } else {
                                    chatPojo = new ChatPojo(true, orderId, fromId,
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
                        } else if (!Constants.isStringNullOrBlank(cancelSessionByPhotographer)) {
                            if (cancelSessionByPhotographer.equals(Constants.CANCEL_SESSION_BY_PHOTOGRAPHER)) {
                                AppPreference.getInstance(ChatActivity.this).setString(Constants.TRACK_START, "0");
                                Intent requestIntent = new Intent(ChatActivity.this, MenuScreen.class);
                                startActivity(requestIntent);
                                trackPhotographer.finish();
                                finish();
                                if (!Constants.isStringNullOrBlank(notificationId)) {
                                    MyFirebaseMessagingService.clearNotification(ChatActivity.this, Integer.valueOf(notificationId));
                                }

                            }
                        }


                    }


                }
            };
            registerReceiver(broadcastReceiver, intentFilter);
        } catch (Exception e) {
            ExceptionHandler.printStackTrace(e);
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            unregisterReceiver(broadcastReceiver);
        } catch (Exception e) {
            ExceptionHandler.printStackTrace(e);
        }
    }

    public void sendMessageAPI(String message) {


        Api api = ApiFactory.getClientWithoutHeader(ChatActivity.this).create(Api.class);
        HashMap<String, String> map = new HashMap<String, String>();
        Call<ResponseBody> call = null;
        map.put("to_id", photographerId);
        map.put("order_id", orderID);
        map.put("message", message);

        call = api.sendMessage(AppPreference.getInstance(ChatActivity.this).getString(Constants.ACCESS_TOKEN), map);
        Log.e(Constants.LOG_CAT, "FAN SEND MESSAGE API------------------->>>>>:" + map + " " + call.request().url());
        Log.e(Constants.LOG_CAT, "HEADERS : " + call.request().headers());
        progressRelative.setVisibility(View.VISIBLE);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                progressRelative.setVisibility(View.GONE);

                try {
                    if (response.isSuccessful()) {
                        String output = ErrorUtils.getResponseBody(response);
                        JSONObject object = new JSONObject(output);
                        Log.d("tag", object.toString(1));

                        if (object.optString("success").equalsIgnoreCase("true")) {

                            Log.e(Constants.LOG_CAT, "onResponse: FAN SEND MESSAGE API=============>>>>>>>>>>" + object.toString());
                            JSONObject dataObject = object.optJSONObject("data");
                            editTextMessage.setText("");
                            if (dataObject != null) {
                                String toId = dataObject.optString("to_id");
                                String fromId = dataObject.optString("from_id");
                                String orderId = dataObject.optString("order_id");
                                String message = dataObject.optString("message");
                                String createdAt = dataObject.optString("created_at");
                                ChatPojo chatPojo;
                                if (userID.contains(fromId)) {
                                    chatPojo = new ChatPojo(false, orderId, toId,
                                            fromId, message, createdAt,
                                            "", "");
                                } else {
                                    chatPojo = new ChatPojo(true, orderId, fromId,
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
                            } else {
                                Log.e(Constants.LOG_CAT, "onResponse: ");
                            }

                        } else if (object.optString(Constants.SUCCESS).equalsIgnoreCase(Constants.FALSE)) {
                            ErrorUtils.showFalseMessage(object, ChatActivity.this);
                        }
                    } else if (response.code() == 400 || response.code() == 500 || response.code() == 403 || response.code() == 404 || response.code() == 401) {
                        if (response.code() == 401) {
                            Constants.showSessionExpireAlert(ChatActivity.this);
                        } else {
                            Constants.showToastAlert(ErrorUtils.getHtttpCodeError(response.code()), ChatActivity.this);
                        }

                    } else {
                        String responseStr = ErrorUtils.getResponseBody(response);
                        JSONObject jsonObject = new JSONObject(responseStr);
                        Constants.showToastAlert(ErrorUtils.checkJosnErrorBody(jsonObject), ChatActivity.this);
                    }
                } catch (JSONException e) {
                    Constants.hideProgressDialog();
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Constants.hideProgressDialog();
            }
        });


    }


    // TODO=========Chat Adapter =====

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
            holder.MessageTimeLeft.setText(Constants.getDateInFormat("yyyy-MM-dd hh:mm:ss", "hh:mm aa", coment.getTime()));
            holder.MessageTimeRight.setText(Constants.getDateInFormat("yyyy-MM-dd hh:mm:ss", "hh:mm aa", coment.getTime()));
            holder.layoutLeftUser.setVisibility(coment.isLeft() ? View.VISIBLE : View.GONE);
            holder.layoutRightUser.setVisibility(coment.isLeft() ? View.GONE : View.VISIBLE);


        }


        @Override
        public int getItemCount() {
            return chatList.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

            private TextView MessageTimeLeft;
            private TextView MessageTimeRight;
            private TextView commentTextLeft;
            private TextView commentTextRight;
            private TextView commenttext_right_image;
            private TextView commenttext_left_image;
            private LinearLayout layoutLeftUser;
            private LinearLayout layoutRightUser;
            private ImageView image_right;
            private ImageView image_left;
            private ImageView img_left_pdf;
            private ImageView img_right_pdf;
            private RelativeLayout progressRelativeRight;
            private RelativeLayout progressRelative;

            public ViewHolder(View v) {
                super(v);
                commentTextRight = v.findViewById(R.id.commenttext_right);
                commentTextLeft = v.findViewById(R.id.commenttext_left);
                MessageTimeRight = v.findViewById(R.id.MessageTimeRight);
                MessageTimeLeft = v.findViewById(R.id.MessageTimeleft);
                layoutLeftUser = v.findViewById(R.id.layout_left_user);
                layoutRightUser = v.findViewById(R.id.layout_right_user);
                progressRelativeRight = v.findViewById(R.id.progressRelativeRight);
                progressRelative = v.findViewById(R.id.progressRelative);
                img_left_pdf = v.findViewById(R.id.img_left_pdf);
                commenttext_right_image = v.findViewById(R.id.commenttext_right_image);
                commenttext_left_image = v.findViewById(R.id.commenttext_left_image);

            }


            @Override
            public void onClick(View v) {
                Log.e(Constants.LOG_CAT, "onClick: ");
            }
        }

    }
}
