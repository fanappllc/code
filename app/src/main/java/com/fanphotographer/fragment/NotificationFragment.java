package com.fanphotographer.fragment;

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
import com.fanphotographer.R;
import com.fanphotographer.activity.MenuScreen;
import com.fanphotographer.bean.NotificationBean;
import com.fanphotographer.data.constant.Constants;
import com.fanphotographer.data.preference.AppPreference;
import com.fanphotographer.utility.AppUtils;
import com.fanphotographer.utility.ErrorUtils;
import com.fanphotographer.webservice.Api;
import com.fanphotographer.webservice.ApiFactory;
import com.special.ResideMenu.ResideMenu;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import me.leolin.shortcutbadger.ShortcutBadger;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class NotificationFragment extends Fragment implements View.OnClickListener {

    private View view;
    private RecyclerView notificationRecyclerview;
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
    private MenuScreen act;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate( R.layout.fragment_notification, container, false);
        notificationList = new ArrayList<>();
        act = (MenuScreen) getActivity();
        setToolBar();
        intView();
        if (AppUtils.isNetworkConnected()) {
            getNotificationApi();
        } else {
            final ViewGroup viewGroup = (ViewGroup) ((ViewGroup) getActivity().findViewById(android.R.id.content)).getChildAt(0);
            Constants.showSnackbar(getActivity(), viewGroup, getResources().getString(R.string.no_internet), Constants.RETRY);
        }

        return view;
    }

    private void setToolBar() {
        headerRightText = (TextView) view.findViewById(R.id.headerRightText);
        RelativeLayout toolBarLeft = (RelativeLayout) view.findViewById(R.id.toolBarLeft);
        ImageView imageViewMenu = (ImageView) view.findViewById(R.id.imageView_menu);
        TextView headerTextView = (TextView) view.findViewById(R.id.hedertextview);
        ImageView toolbarbackpress = (ImageView) view.findViewById(R.id.toolbarbackpress);
        toolbarbackpress.setVisibility(View.GONE);
        headerRightText.setVisibility(View.GONE);
        imageViewMenu.setVisibility(View.VISIBLE);
        headerRightText.setText("CLEAR ALL");
        headerTextView.setText(getActivity().getResources().getString(R.string.notifications));
        toolBarLeft.setOnClickListener(this);
        headerRightText.setOnClickListener(this);
    }

    private void intView() {

        loadMoreData = (RelativeLayout) view.findViewById(R.id.loadMoreData);
        notificationClear = (ImageView) view.findViewById(R.id.notification_clear);
        notificationRecyclerview = (RecyclerView) view.findViewById(R.id.notification_recycler_view);
        linearLayoutManager = new LinearLayoutManager(getActivity());
        notificationRecyclerview.setLayoutManager(linearLayoutManager);
        setScroll();
    }

    private void setList() {
        if (notificationAdapter == null) {
            notificationAdapter = new NotificationAdapter(getContext(), notificationList);
            notificationRecyclerview.setAdapter(notificationAdapter);
        } else {
            notificationAdapter.notifyDataSetChanged();
        }
    }


    private void setScroll() {

        notificationRecyclerview.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0) {

                    if (AppUtils.isNetworkConnected()) {
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
                        final ViewGroup viewGroup = (ViewGroup) ((ViewGroup) getActivity().findViewById(android.R.id.content)).getChildAt(0);
                        Constants.showSnackbar(getActivity(), viewGroup, getResources().getString(R.string.no_internet), "Retry");
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
        dialog.setContentView(R.layout.dialog_clear_notification);
        TextView cancelTextView = (TextView) dialog.findViewById(R.id.cancelTextView);
        TextView yesTextView = (TextView) dialog.findViewById(R.id.yesTextView);

        yesTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (AppUtils.isNetworkConnected()) {
                    allDeleteAPI();
                } else {
                    final ViewGroup viewGroup = (ViewGroup) ((ViewGroup) getActivity().findViewById(android.R.id.content)).getChildAt(0);
                    Constants.showSnackbar(getActivity(), viewGroup, getResources().getString(R.string.no_internet), "Retry");
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

        call = api.notificationListing(act.appPreference.getString(Constants.ACCESS_TOKEN), currentPage);
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
                    if (response != null && response.isSuccessful() && response.code() == 200) {
                        String output = ErrorUtils.getResponseBody(response);
                        JSONObject object = new JSONObject(output);
                        if (object.optString(Constants.SUCCESS).equalsIgnoreCase(Constants.TRUE)) {
                            Log.e(Constants.LOG_CAT, "onResponse:" + object.toString());
                            JSONObject dataObject = object.optJSONObject("items");
                            JSONArray jsonArray = dataObject.optJSONArray("data");
                            AppPreference.getInstance(getActivity()).setString(Constants.BADGE_COUNT, "0");
                            act.itemNotification.updateCount("0");
                            ShortcutBadger.removeCount(getActivity());
                            if (jsonArray != null && jsonArray.length() > 0) {

                                notificationRecyclerview.setVisibility(View.VISIBLE);
                                notificationClear.setVisibility(View.GONE);
                                headerRightText.setVisibility(View.VISIBLE);
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
                                notificationClear.setVisibility(View.VISIBLE);
                                notificationRecyclerview.setVisibility(View.GONE);
                                headerRightText.setVisibility(View.GONE);
                            }

                            setList();

                            currentPage = dataObject.optInt("current_page");
                            pageCount = dataObject.getInt("last_page");
                            currentPage = currentPage + 1;
                            loading = true;
                            hideLoader();

                        } else if (object.optString(Constants.SUCCESS).equalsIgnoreCase(Constants.FALSE)) {
                            JSONArray jsonArray = object.optJSONArray(Constants.ERROR);
                            if (jsonArray == null) {
                                Constants.showToastAlert(object.getJSONObject(Constants.ERROR).getString(Constants.MESSAGE), getActivity());
                            } else {
                                Constants.showToastAlert(jsonArray.getJSONObject(0).getString(Constants.MESSAGE), getActivity());
                            }
                        }

                    } else if (response != null) {
                        if (response.code() == 400 || response.code() == 500 || response.code() == 403 || response.code() == 404 || response.code() == 401) {
                            if (response.code() == 401) {
                                act.appPreference.showCustomAlert(getActivity(), getResources().getString(R.string.http_401_error));
                            } else {
                                Constants.showToastAlert(ErrorUtils.getHtttpCodeError(response.code()), getActivity());
                            }
                        } else {
                            String responseStr = ErrorUtils.getResponseBody(response);
                            JSONObject jsonObject = new JSONObject(responseStr);
                            Constants.showToastAlert(ErrorUtils.checkJosnErrorBody(jsonObject), getActivity());
                        }
                    }
                } catch (Exception e) {
                    Log.e(Constants.LOG_CAT,"backpressed");
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Constants.hideProgressDialog();
                Constants.showToastAlert(ErrorUtils.getString(R.string.failled), getActivity());
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

    public void getDeleteCardApi(String notificationId, final int position) {


        Api api = ApiFactory.getClientWithoutHeader(getActivity()).create(Api.class);
        Call<ResponseBody> call;
        call = api.notificationDeleteAPI(act.appPreference.getString(Constants.ACCESS_TOKEN), notificationId);

        Constants.showProgressDialog(getActivity(), Constants.LOADING);
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
                            notificationList.remove(position);
                            Log.e(Constants.LOG_CAT, "position===========API pos: " + pos);
                            Log.e(Constants.LOG_CAT, "position===========API: " + position);
                            notificationAdapter.notifyDataSetChanged();
                            if (notificationList != null && notificationList.size() > 0) {

                            } else {
                                notificationClear.setVisibility(View.VISIBLE);
                                notificationRecyclerview.setVisibility(View.GONE);
                                headerRightText.setVisibility(View.GONE);
                            }


                        } else if (object.optString(Constants.SUCCESS).equalsIgnoreCase(Constants.FALSE)) {
                            JSONArray jsonArray = object.optJSONArray(Constants.ERROR);
                            if (jsonArray == null) {
                                Constants.showToastAlert(object.getJSONObject(Constants.ERROR).getString(Constants.MESSAGE), getActivity());
                            } else {
                                Constants.showToastAlert(jsonArray.getJSONObject(0).getString(Constants.MESSAGE), getActivity());
                            }
                        }

                    } else if (response != null) {
                        if (response.code() == 400 || response.code() == 500 || response.code() == 403 || response.code() == 404 || response.code() == 401) {
                            if (response.code() == 401) {
                                act.appPreference.showCustomAlert(getActivity(), getResources().getString(R.string.http_401_error));
                            } else {
                                Constants.showToastAlert(ErrorUtils.getHtttpCodeError(response.code()), getActivity());
                            }
                        } else {
                            String responseStr = ErrorUtils.getResponseBody(response);
                            JSONObject jsonObject = new JSONObject(responseStr);
                            Constants.showToastAlert(ErrorUtils.checkJosnErrorBody(jsonObject), getActivity());
                        }
                    }
                } catch (Exception e) {
                    Log.e(Constants.LOG_CAT,e.getMessage());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Constants.hideProgressDialog();
                Constants.showToastAlert(ErrorUtils.getString(R.string.failled), getActivity());
            }
        });


    }







    public void allDeleteAPI() {


        Api api = ApiFactory.getClientWithoutHeader(getActivity()).create(Api.class);
        Call<ResponseBody> call;
        call = api.allNotificationAPI(act.appPreference.getString(Constants.ACCESS_TOKEN));
        Log.e(Constants.LOG_CAT, "FAN MY DELETE ALL NOTIFICATION  API------------------->>>>>:" + call.request().url());
        Log.e(Constants.LOG_CAT, "HEADERS : " + call.request().headers());


        Constants.showProgressDialog(getActivity(), Constants.LOADING);
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
                            headerRightText.setVisibility(View.GONE);
                            notificationList.clear();
                            notificationClear.setVisibility(View.VISIBLE);
                            notificationRecyclerview.setVisibility(View.GONE);
                            dialog.dismiss();

                        }else if (object.optString(Constants.SUCCESS).equalsIgnoreCase(Constants.FALSE)) {
                            JSONArray jsonArray = object.optJSONArray(Constants.ERROR);
                            if (jsonArray == null) {
                                Constants.showToastAlert(object.getJSONObject(Constants.ERROR).getString(Constants.MESSAGE), getActivity());
                            } else {
                                Constants.showToastAlert(jsonArray.getJSONObject(0).getString(Constants.MESSAGE), getActivity());
                            }
                        }
                    } else if (response != null) {
                        if (response.code() == 400 || response.code() == 500 || response.code() == 403 || response.code() == 404 || response.code() == 401) {
                            if (response.code() == 401) {
                                act.appPreference.showCustomAlert(getActivity(), getResources().getString(R.string.http_401_error));
                            } else {
                                Constants.showToastAlert(ErrorUtils.getHtttpCodeError(response.code()), getActivity());
                            }
                        } else {
                            String responseStr = ErrorUtils.getResponseBody(response);
                            JSONObject jsonObject = new JSONObject(responseStr);
                            Constants.showToastAlert(ErrorUtils.checkJosnErrorBody(jsonObject), getActivity());
                        }
                    }
                } catch (Exception e) {
                    Log.e(Constants.LOG_CAT,e.getMessage());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Constants.hideProgressDialog();
                Constants.showToastAlert(ErrorUtils.getString(R.string.failled), getActivity());
            }
        });


    }

    public class NotificationAdapter extends RecyclerSwipeAdapter<NotificationAdapter.SimpleViewHolder> {

        private Context mContext;
        private ArrayList<NotificationBean> notificationList;

        NotificationAdapter(Context context, ArrayList<NotificationBean> notificationList) {
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
            viewHolder.timeTextView.setText(Constants.getDateInFormat("yyyy-MM-dd hh:mm:ss", "HH:mm a", notificationList.get(position).getCreated_at()));
            viewHolder.swipeLayout.addDrag(SwipeLayout.DragEdge.Right, viewHolder.swipeLayout.findViewById(R.id.bottom_wrapper));
            viewHolder.swipeLayout.setLeftSwipeEnabled(false);
            viewHolder.swipeLayout.addSwipeListener(new SwipeLayout.SwipeListener() {
                @Override public void onClose(SwipeLayout layout) {}
                @Override public void onUpdate(SwipeLayout layout, int leftOffset, int topOffset) {}
                @Override public void onStartOpen(SwipeLayout layout) {}
                @Override public void onOpen(SwipeLayout layout) {}
                @Override public void onStartClose(SwipeLayout layout) {}
                @Override public void onHandRelease(SwipeLayout layout, float xvel, float yvel) {}
            });
            viewHolder.deleteNotificationRelative.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String cardId = notificationList.get(pos).getId();
                    getDeleteCardApi(cardId, pos);
                    mItemManger.removeShownLayouts(viewHolder.swipeLayout);
                    notificationList.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, notificationList.size());
                    Log.e(Constants.LOG_CAT, "position===========ADAPTER pos: " + pos);
                    mItemManger.closeAllItems();
                }
            });

            viewHolder.swipeLayout.getSurfaceView().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.e(Constants.LOG_CAT,"setOnClickListener");
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


        class SimpleViewHolder extends RecyclerView.ViewHolder {
            SwipeLayout swipeLayout;
            TextView textViewMessage;
            TextView newTextView;
            RelativeLayout deleteNotificationRelative;
            TextView timeTextView;

            SimpleViewHolder(View itemView) {
                super(itemView);
                timeTextView = (TextView) itemView.findViewById(R.id.timeTextView);
                swipeLayout = (SwipeLayout) itemView.findViewById(R.id.swipe);
                textViewMessage = (TextView) itemView.findViewById(R.id.textView_message);
                newTextView = (TextView) itemView.findViewById(R.id.new_textView);
                deleteNotificationRelative = (RelativeLayout) itemView.findViewById(R.id.deleteNotificationRelative);

            }
        }

    }
}