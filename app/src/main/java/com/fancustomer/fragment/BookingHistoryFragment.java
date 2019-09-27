package com.fancustomer.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.fancustomer.R;
import com.fancustomer.activity.MenuScreen;
import com.fancustomer.adapter.BookingHistoryAdapter;
import com.fancustomer.bean.BookingBean;

import com.fancustomer.data.constant.Constants;
import com.fancustomer.data.preference.AppPreference;
import com.fancustomer.helper.ErrorUtils;
import com.fancustomer.webservice.Api;
import com.fancustomer.webservice.ApiFactory;
import com.special.ResideMenu.ResideMenu;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class BookingHistoryFragment extends Fragment implements View.OnClickListener {

    private View view;
    private RecyclerView recyclerViewHistory;
    private LinearLayoutManager linearLayoutManager;
    private BookingHistoryAdapter historyAdapter;
    private ArrayList<BookingBean> historyList;
    private TextView headerTextView;
    private RelativeLayout toolBarLeft;
    private TextView bookingClear;
    ViewGroup viewGroup;
    MenuScreen menuActivity;
    private int pastVisiblesItems;
    private int visibleItemCount;
    private int totalItemCount;
    private int currentPage = 0;
    private int pageCount = 0;
    private boolean loading = true;
    private boolean isFirstTimeLoading = false;
    private RelativeLayout loadMoreData;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_booking_history, container, false);
        viewGroup = container;
        historyList = new ArrayList<>();
        setToolBar();
        intView();

        if (Constants.isInternetOn(getActivity())) {
            isFirstTimeLoading = true;
            getBookingHistory();
        } else {
            menuActivity.showSnackbar(viewGroup, getResources().getString(R.string.no_internet), Constants.RETRY);
        }
        return view;

    }

    private void setToolBar() {
        toolBarLeft = view.findViewById(R.id.toolBarLeft);
        ImageView imageViewMenu = view.findViewById(R.id.imageView_menu);
        headerTextView = view.findViewById(R.id.hedertextview);
        ImageView toolbarbackpress = view.findViewById(R.id.toolbarbackpress);
        toolbarbackpress.setVisibility(View.GONE);
        imageViewMenu.setVisibility(View.VISIBLE);
        headerTextView.setText(getActivity().getResources().getString(R.string.booking_history));
        toolBarLeft.setOnClickListener(this);
    }

    private void intView() {
        loadMoreData = view.findViewById(R.id.loadMoreData);
        bookingClear = view.findViewById(R.id.booking_clear);
        recyclerViewHistory = view.findViewById(R.id.historyRecycleView);
        linearLayoutManager = new LinearLayoutManager(getActivity());
        recyclerViewHistory.setLayoutManager(linearLayoutManager);
        setScroll();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        menuActivity = (MenuScreen) context;

    }

    private void setList() {
        if (historyAdapter == null) {
            historyAdapter = new BookingHistoryAdapter(getContext(), historyList);
            recyclerViewHistory.setAdapter(historyAdapter);
        } else {
            historyAdapter.notifyDataSetChanged();
        }
    }

    private void setScroll() {

        recyclerViewHistory.addOnScrollListener(new RecyclerView.OnScrollListener() {
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
                                getBookingHistory();
                                showLoader();
                            }
                        }

                    } else {
                        menuActivity.showSnackbar(viewGroup,

                                getResources().getString(R.string.no_internet), Constants.RETRY);
                    }
                }
            }
        });
    }

    public void getBookingHistory() {


        Api api = ApiFactory.getClientWithoutHeader(getActivity()).create(Api.class);
        Call<ResponseBody> call;

        call = api.myOrder(AppPreference.getInstance(getActivity()).getString(Constants.ACCESS_TOKEN), currentPage);
        Log.e(Constants.LOG_CAT, "FAN BookingHistory API------------------->>>>>:" + call.request().url());
        Log.e(Constants.LOG_CAT, "HEADERS : " + call.request().headers());
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
                            Log.e(Constants.LOG_CAT, "onResponse: FAN BookingHistory API LIST=============>>>>>>>>>>" + object.toString());
                            JSONObject itemsObject = object.getJSONObject("items");
                            JSONArray jsonArray = itemsObject.optJSONArray("data");
                            if (jsonArray != null && jsonArray.length() > 0) {
                                recyclerViewHistory.setVisibility(View.VISIBLE);
                                bookingClear.setVisibility(View.GONE);
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject jsonObject = jsonArray.optJSONObject(i);
                                    BookingBean bookingBean = new BookingBean();
                                    bookingBean.setOrder_id(jsonObject.optString("order_id"));
                                    bookingBean.setFirst_name(jsonObject.optString("first_name"));
                                    bookingBean.setLast_name(jsonObject.optString("last_name"));
                                    bookingBean.setProfile_image(jsonObject.optString("profile_image"));
                                    bookingBean.setOrder_created_at(jsonObject.optString("order_created_at"));
                                    bookingBean.setOrder_status(jsonObject.optString("order_status"));
                                    bookingBean.setTotal_amount(jsonObject.optString("total_amount"));
                                    bookingBean.setTransaction_type(jsonObject.optString("transaction_type"));
                                    bookingBean.setTransaction_status(jsonObject.optString("transaction_status"));
                                    historyList.add(bookingBean);
                                }
                            } else {
                                recyclerViewHistory.setVisibility(View.GONE);
                                bookingClear.setVisibility(View.VISIBLE);
                            }

                            setList();
                            currentPage = itemsObject.optInt("current_page");
                            pageCount = itemsObject.getInt("last_page");
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

    public void showLoader() {
        loadMoreData.setVisibility(View.VISIBLE);
    }

    public void hideLoader() {
        loadMoreData.setVisibility(View.GONE);
    }

    @Override
    public void onClick(View view) {
        int i = view.getId();
        if (i == R.id.toolBarLeft) {
            MenuScreen.resideMenu.openMenu(ResideMenu.DIRECTION_LEFT);

        }
    }
}