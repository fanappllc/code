package com.fancustomer.fragment;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.daimajia.swipe.SwipeLayout;
import com.daimajia.swipe.adapters.RecyclerSwipeAdapter;
import com.fancustomer.R;
import com.fancustomer.activity.MenuScreen;

import com.fancustomer.bean.NotificationBean;
import com.fancustomer.data.constant.Constants;
import com.fancustomer.data.preference.AppPreference;
import com.fancustomer.helper.ErrorUtils;
import com.fancustomer.helper.ExceptionHandler;
import com.fancustomer.webservice.Api;
import com.fancustomer.webservice.ApiFactory;
import com.special.ResideMenu.ResideMenu;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import me.leolin.shortcutbadger.ShortcutBadger;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class NotificationFragment extends Fragment implements View.OnClickListener {

    private View view;
    private RecyclerView notificationRecyclerView;
    private LinearLayoutManager linearLayoutManager;
    private NotificationAdapter notificationAdapter;
    private ArrayList<NotificationBean> notificationList;
    private TextView headerRightText;
    private Dialog dialog;
    private int pos;
    private ImageView notificationClear;
    private int pastVisiblesItems;
    private int visibleItemCount;
    private int totalItemCount;
    private int currentPage = 0;
    private int pageCount = 0;
    private boolean loading = true;
    private boolean isFirstTimeLoading = false;
    private RelativeLayout loadMoreData;
    ViewGroup viewGroup;
    MenuScreen menuActivity;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_notification, container, false);
        viewGroup = container;
        notificationList = new ArrayList<>();
        setToolBar();
        intView();
        if (Constants.isInternetOn(getActivity())) {
            isFirstTimeLoading = true;
            getNotificationApi();
        } else {
            menuActivity.showSnackbar(viewGroup, getResources().getString(R.string.no_internet), Constants.RETRY);
        }

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        menuActivity = (MenuScreen) context;

    }

    private void setToolBar() {
        headerRightText = view.findViewById(R.id.headerRightText);
        RelativeLayout toolBarLeft = view.findViewById(R.id.toolBarLeft);
        ImageView imageViewMenu = view.findViewById(R.id.imageView_menu);
        TextView headerTextView = view.findViewById(R.id.hedertextview);
        ImageView toolbarbackpress = view.findViewById(R.id.toolbarbackpress);
        toolbarbackpress.setVisibility(View.GONE);
        imageViewMenu.setVisibility(View.VISIBLE);
        headerRightText.setText("CLEAR ALL");
        headerRightText.setVisibility(View.GONE);
        headerTextView.setText(getActivity().getResources().getString(R.string.notifications));
        toolBarLeft.setOnClickListener(this);
        headerRightText.setOnClickListener(this);
    }

    private void intView() {

        loadMoreData = view.findViewById(R.id.loadMoreData);
        notificationClear = view.findViewById(R.id.notification_clear);
        notificationRecyclerView = view.findViewById(R.id.notification_recycler_view);
        linearLayoutManager = new LinearLayoutManager(getActivity());
        notificationRecyclerView.setLayoutManager(linearLayoutManager);
        setScroll();
    }

    private void setList() {
        if (notificationAdapter == null) {
            notificationAdapter = new NotificationAdapter(getContext(), notificationList);
            notificationRecyclerView.setAdapter(notificationAdapter);
        } else {
            notificationAdapter.notifyDataSetChanged();
        }


    }


    private void setScroll() {
        notificationRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0) {
                    if (Constants.isInternetOn(getActivity())) {
                        visibleItemCount = linearLayoutManager.getChildCount();
                        totalItemCount = linearLayoutManager.getItemCount();
                        pastVisiblesItems = linearLayoutManager.findFirstVisibleItemPosition();

                        if (loading) {
                            if ((visibleItemCount + pastVisiblesItems) >= totalItemCount && currentPage <= pageCount) {
                                loading = false;
                                isFirstTimeLoading = false;
                                Log.e(Constants.LOG_CAT, "Pagination: currentPage " + currentPage + " \t pageCount" + pageCount);
                                getNotificationApi();
                                showLoader();
                            }
                        }

                    } else {
                        menuActivity.showSnackbar(viewGroup, getResources().getString(R.string.no_internet), Constants.RETRY);
                    }
                }
            }
        });
    }

    public void showLoader() {
        loadMoreData.setVisibility(View.VISIBLE);
    }

    public void hideLoader() {
        loadMoreData.setVisibility(View.GONE);
    }

    private void showDeleteDialog() {
        dialog = new Dialog(getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.dialog_notification);
        TextView cancelTextView = dialog.findViewById(R.id.cancelTextView);
        TextView yesTextView = dialog.findViewById(R.id.yesTextView);
        yesTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                if (Constants.isInternetOn(getActivity())) {
                    allDeleteAPI();
                } else {
                    menuActivity.showSnackbar(viewGroup, getResources().getString(R.string.no_internet), Constants.RETRY);
                }
            }
        });
        cancelTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dialog.dismiss();
            }
        });
        dialog.show();

    }

    public void getNotificationApi() {
        Api api = ApiFactory.getClientWithoutHeader(getActivity()).create(Api.class);
        Call<ResponseBody> call;
        call = api.notificationListing(AppPreference.getInstance(getActivity()).getString(Constants.ACCESS_TOKEN), currentPage);
        Log.e(Constants.LOG_CAT, "FAN NOTIFICATION API------------------->>>>>:" + " " + call.request().url());
        Log.e(Constants.LOG_CAT, "HEADERS @#@#@#@: " + call.request().headers());
        if (isFirstTimeLoading) {
            hideLoader();
            Constants.showProgressDialog(getActivity(), Constants.LOADING);
        } else {
            Constants.hideProgressDialog();
        }

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
                            AppPreference.getInstance(getActivity()).setString(Constants.BADGE_COUNT, "0");
                            MenuScreen.itemNotification.updateCount("0");
                            ShortcutBadger.removeCount(getActivity());
                            Log.e(Constants.LOG_CAT, "onResponse: FAN NOTIFICATION API LIST=============>>>>>>>>>>" + object.toString());
                            JSONObject dataObject = object.optJSONObject("items");
                            JSONArray jsonArray = dataObject.optJSONArray("data");
                            if (jsonArray != null && jsonArray.length() > 0) {
                                headerRightText.setVisibility(View.VISIBLE);
                                notificationRecyclerView.setVisibility(View.VISIBLE);
                                notificationClear.setVisibility(View.GONE);
                                for (int i = 0; i < jsonArray.length(); i++) {

                                    JSONObject jsonObject = jsonArray.optJSONObject(i);
                                    String id = jsonObject.optString("id");
                                    String toId = jsonObject.optString("to_id");
                                    String fromId = jsonObject.optString("from_id");
                                    String type = jsonObject.optString("type");
                                    String message = jsonObject.optString("message");
                                    String isRead = jsonObject.optString("is_read");
                                    String createdAt = jsonObject.optString("created_at");
                                    NotificationBean notificationBean = new NotificationBean(id, toId, fromId, type, message, isRead, createdAt);
                                    notificationList.add(notificationBean);


                                }
                            } else {
                                headerRightText.setVisibility(View.GONE);
                                notificationClear.setVisibility(View.VISIBLE);
                                notificationRecyclerView.setVisibility(View.GONE);
                            }

                            setList();

                            currentPage = dataObject.optInt("current_page");
                            pageCount = dataObject.getInt("last_page");
                            currentPage = currentPage + 1;
                            loading = true;
                            hideLoader();

                        } else if (object.optString(Constants.SUCCESS).equalsIgnoreCase(Constants.FALSE)) {
                            ErrorUtils.showFalseMessage(object, getActivity());
                        }
                    } else if (response.code() == 400 || response.code() == 500 || response.code() == 403 || response.code() == 404 || response.code() == 401) {
                        if (response.code() == 401) {
                            Constants.showSessionExpireAlert(getActivity());
                        } else {
                            Constants.showToastAlert(ErrorUtils.getHtttpCodeError(response.code()), getActivity());
                        }

                    } else {
                        String responseStr = ErrorUtils.getResponseBody(response);
                        JSONObject jsonObject = new JSONObject(responseStr);
                        Constants.showToastAlert(ErrorUtils.checkJosnErrorBody(jsonObject), getActivity());
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
    public void onClick(View view) {
        int i = view.getId();
        if (i == R.id.toolBarLeft) {
            MenuScreen.resideMenu.openMenu(ResideMenu.DIRECTION_LEFT);
        } else if (i == R.id.headerRightText) {
            showDeleteDialog();
        }
    }

    public void getDeleteNotificationApi(String notificationId, final int position) {


        Api api = ApiFactory.getClientWithoutHeader(getActivity()).create(Api.class);
        Call<ResponseBody> call = null;


        call = api.notificationDeleteAPI(AppPreference.getInstance(getActivity()).getString(Constants.ACCESS_TOKEN), notificationId);
        Log.e(Constants.LOG_CAT, "FAN MY DELETE NOTIFICATION API------------------->>>>>:" + call.request().url());
        Log.e(Constants.LOG_CAT, "HEADERS : " + call.request().headers());


        Constants.showProgressDialog(getActivity(), Constants.LOADING);
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
                            Log.e(Constants.LOG_CAT, "onResponse: DELETE NOTIFICATION API=============>>>>>>>>>>" + object.toString());
                            try {
                                notificationList.remove(position);
                                Log.e(Constants.LOG_CAT, "position===========API pos: " + pos);
                                Log.e(Constants.LOG_CAT, "position===========API: " + position);
                                notificationAdapter.notifyDataSetChanged();
                                if (notificationList != null && notificationList.size() > 0) {
                                    Log.e(Constants.LOG_CAT, "onResponse: ");

                                } else {
                                    headerRightText.setVisibility(View.GONE);
                                    notificationClear.setVisibility(View.VISIBLE);
                                    notificationRecyclerView.setVisibility(View.GONE);
                                    headerRightText.setVisibility(View.GONE);
                                }

                            } catch (Exception e) {
                                ExceptionHandler.printStackTrace(e);
                            }
                        } else if (object.optString(Constants.SUCCESS).equalsIgnoreCase(Constants.FALSE)) {
                            ErrorUtils.showFalseMessage(object, getActivity());
                        }
                    } else if (response.code() == 400 || response.code() == 500 || response.code() == 403 || response.code() == 404 || response.code() == 401) {
                        if (response.code() == 401) {
                            Constants.showSessionExpireAlert(getActivity());
                        } else {
                            Constants.showToastAlert(ErrorUtils.getHtttpCodeError(response.code()), getActivity());
                        }

                    } else {
                        String responseStr = ErrorUtils.getResponseBody(response);
                        JSONObject jsonObject = new JSONObject(responseStr);
                        Constants.showToastAlert(ErrorUtils.checkJosnErrorBody(jsonObject), getActivity());
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


    public void allDeleteAPI() {


        Api api = ApiFactory.getClientWithoutHeader(getActivity()).create(Api.class);
        Call<ResponseBody> call = null;


        call = api.allNotificationAPI(AppPreference.getInstance(getActivity()).getString(Constants.ACCESS_TOKEN));
        Log.e(Constants.LOG_CAT, "FAN MY DELETE ALL NOTIFICATION  API------------------->>>>>:" + call.request().url());
        Log.e(Constants.LOG_CAT, "HEADERS : " + call.request().headers());


        Constants.showProgressDialog(getActivity(), "Loading");
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
                            Log.e(Constants.LOG_CAT, "onResponse: DELETE  ALL NOTIFICATION=============>>>>>>>>>>" + object.toString());
                            headerRightText.setVisibility(View.GONE);
                            notificationList.clear();
                            headerRightText.setVisibility(View.GONE);
                            notificationClear.setVisibility(View.VISIBLE);
                            notificationRecyclerView.setVisibility(View.GONE);
                            dialog.dismiss();
                            Log.e(Constants.LOG_CAT, "position===========API pos: " + pos);
                        } else if (object.optString(Constants.SUCCESS).equalsIgnoreCase(Constants.FALSE)) {
                            ErrorUtils.showFalseMessage(object, getActivity());


                        }
                    } else if (response.code() == 400 || response.code() == 500 || response.code() == 403 || response.code() == 404 || response.code() == 401) {
                        if (response.code() == 401) {
                            Constants.showSessionExpireAlert(getActivity());
                        } else {
                            Constants.showToastAlert(ErrorUtils.getHtttpCodeError(response.code()), getActivity());
                        }

                    } else {
                        String responseStr = ErrorUtils.getResponseBody(response);
                        JSONObject jsonObject = new JSONObject(responseStr);
                        Constants.showToastAlert(ErrorUtils.checkJosnErrorBody(jsonObject), getActivity());
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

    public class NotificationAdapter extends RecyclerSwipeAdapter<NotificationAdapter.SimpleViewHolder> {

        private Context mContext;
        private ArrayList<NotificationBean> notificationList;

        public NotificationAdapter(Context context, ArrayList<NotificationBean> notificationList) {
            this.mContext = context;
            this.notificationList = notificationList;
        }

        @Override
        public NotificationAdapter.SimpleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.notification_swipe_row_item, parent, false);
            return new NotificationAdapter.SimpleViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final NotificationAdapter.SimpleViewHolder viewHolder, final int position) {
            final int pos = position;

            viewHolder.newTextView.setVisibility(View.GONE);
            viewHolder.swipeLayout.setShowMode(SwipeLayout.ShowMode.PullOut);
            viewHolder.textViewMessage.setText(notificationList.get(position).getMessage());
            viewHolder.timeTextView.setText(Constants.getDateInFormat("yyyy-MM-dd hh:mm:ss", "hh:mm aa", notificationList.get(position).getCreated_at()));

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

                    Log.e(Constants.LOG_CAT, "onStartOpen: ");

                }

                @Override
                public void onOpen(SwipeLayout layout) {
                    Log.e(Constants.LOG_CAT, "onOpen: ");
                }

                @Override
                public void onStartClose(SwipeLayout layout) {
                    Log.e(Constants.LOG_CAT, "onStartClose: ");

                }

                @Override
                public void onHandRelease(SwipeLayout layout, float xvel, float yvel) {
                    Log.e(Constants.LOG_CAT, "onHandRelease: ");
                }
            });
            viewHolder.deleteNotificationRelative.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String cardId = notificationList.get(pos).getId();
                    getDeleteNotificationApi(cardId, pos);
                    mItemManger.removeShownLayouts(viewHolder.swipeLayout);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, notificationList.size());
                    Log.e(Constants.LOG_CAT, "position===========ADAPTER pos: " + pos);
                    mItemManger.closeAllItems();
                }
            });

            viewHolder.swipeLayout.getSurfaceView().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.e(Constants.LOG_CAT, "onClick: ");
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
            RelativeLayout deleteNotificationRelative;
            TextView timeTextView;

            public SimpleViewHolder(View itemView) {
                super(itemView);
                timeTextView = itemView.findViewById(R.id.timeTextView);
                swipeLayout = itemView.findViewById(R.id.swipe);
                textViewMessage = itemView.findViewById(R.id.textView_message);
                newTextView = itemView.findViewById(R.id.new_textView);
                deleteNotificationRelative = itemView.findViewById(R.id.deleteNotificationRelative);

            }
        }

    }
}