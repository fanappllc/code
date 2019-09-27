package com.fanphotographer.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.fanphotographer.R;
import com.fanphotographer.activity.MenuScreen;
import com.fanphotographer.adapter.HomeAdapter;
import com.fanphotographer.data.constant.Constants;
import com.fanphotographer.fcm.MyFirebaseMessagingService;
import com.fanphotographer.helper.RecyclerItemClickListener;
import com.fanphotographer.switchbutton.SwitchButton;
import com.fanphotographer.utility.AppUtils;
import com.fanphotographer.utility.ErrorUtils;
import com.fanphotographer.webservice.Api;
import com.fanphotographer.webservice.ApiFactory;
import com.special.ResideMenu.ResideMenu;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class HomeFragment extends Fragment {

    private String onOffstatus = "0";
    private ArrayList<JSONObject> arrayList = new ArrayList<>();
    private View parentView;
    private RecyclerView recyclerView;
    private RelativeLayout onlineLayout;
    private TextView noClear;
    private MenuScreen act;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        parentView = inflater.inflate(R.layout.fragment_home, container, false);
        act = (MenuScreen) getActivity();
        setToolBar();
        initView();
        if (AppUtils.isNetworkConnected()) {
            if (act.appPreference.getBoolean(Constants.IS_USER_INFORMATION_STATUS)) {
                getNotificationListing();
            } else {
                stillDelay();
            }

        } else {
            final ViewGroup viewGroup = (ViewGroup) ((ViewGroup) getActivity().findViewById(android.R.id.content)).getChildAt(0);
            Constants.showSnackbar(getActivity(), viewGroup, getResources().getString(R.string.no_internet), Constants.RETRY);
        }
        return parentView;
    }


    public void stillDelay(){
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Constants.showProgressDialog(act, Constants.LOADING);
                getNotificationListing();
            }
        }, 1300);
    }

    private void setToolBar() {
        ImageView toolbarbackpress = (ImageView) parentView.findViewById(R.id.toolbarbackpress);
        ImageView imageViewmenu = (ImageView) parentView.findViewById(R.id.imageView_menu);
        RelativeLayout toolBarLeft = (RelativeLayout) parentView.findViewById(R.id.toolBarLeft);
        TextView headerTextView = (TextView) parentView.findViewById(R.id.hedertextview);
        headerTextView.setText(HomeFragment.this.getResources().getString(R.string.home));
        toolBarLeft.setOnClickListener(listener);
        toolbarbackpress.setVisibility(View.GONE);
        imageViewmenu.setVisibility(View.VISIBLE);
    }

    View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int i = v.getId();
            if (i == R.id.toolBarLeft) {
                MenuScreen.resideMenu.openMenu(ResideMenu.DIRECTION_LEFT);

            }
        }
    };

    private void initView() {

        noClear = (TextView) parentView.findViewById(R.id.no_clear);
        onlineLayout = (RelativeLayout) parentView.findViewById(R.id.onlineLayout);
        SwitchButton switchButton = (SwitchButton) parentView.findViewById(R.id.switchButton);
        switchButton.setVisibility(View.VISIBLE);
        recyclerView = (RecyclerView) parentView.findViewById(R.id.recycler_view);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setLayoutManager(mLayoutManager);
        setAdapter(arrayList, recyclerView);
        if(act.appPreference.getBoolean(Constants.IS_USER_INFORMATION_STATUS)){
            onOffstatus = act.appPreference.getString(Constants.IS_AVAILABLE);
            if (onOffstatus.equalsIgnoreCase("0")) {
                switchButton.setChecked(false);
                recyclerView.setVisibility(View.GONE);
                noClear.setVisibility(View.GONE);
                onlineLayout.setVisibility(View.VISIBLE);
            } else if(onOffstatus.equalsIgnoreCase("1")) {
                switchButton.setChecked(true);
                setData();
            }else {
                switchButton.setChecked(false);
                recyclerView.setVisibility(View.GONE);
                noClear.setVisibility(View.GONE);
                onlineLayout.setVisibility(View.VISIBLE);
            }

        }else {
            switchButton.setChecked(true);
            setData();
        }

        recyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(getContext(), new RecyclerItemClickListener.OnItemClickListener() {
                    @Override public void onItemClick(View view, int position) {
                        Log.e(Constants.LOG_CAT,"onItemClick");
                    }
                })
        );


        if (switchButton != null) {
            switchButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (AppUtils.isNetworkConnected()) {
                        if (isChecked) {
                            if (arrayList != null && arrayList.size() > 0) {
                                setAdapter(arrayList, recyclerView);
                                noClear.setVisibility(View.GONE);
                                recyclerView.setVisibility(View.VISIBLE);
                                onlineLayout.setVisibility(View.GONE);
                            } else {
                                recyclerView.setVisibility(View.GONE);
                                onlineLayout.setVisibility(View.GONE);
                                noClear.setVisibility(View.VISIBLE);
                            }
                            onOffstatus = "1";
                            useravailabilityapi();
                        } else {
                            recyclerView.setVisibility(View.GONE);
                            onlineLayout.setVisibility(View.VISIBLE);
                            noClear.setVisibility(View.GONE);
                            onOffstatus = "0";
                            useravailabilityapi();

                        }
                    } else {
                        final ViewGroup viewGroup = (ViewGroup) ((ViewGroup) getActivity().findViewById(android.R.id.content)).getChildAt(0);
                        Constants.showSnackbar(getActivity(), viewGroup, getResources().getString(R.string.no_internet), Constants.RETRY);
                    }

                }
            });
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if(AppUtils.isNetworkConnected()){
            MyFirebaseMessagingService.clearNotificationAll(act);
        }

    }

    public void setData(){
        if (arrayList != null && arrayList.size() > 0) {
            noClear.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            onlineLayout.setVisibility(View.GONE);
        } else {
            recyclerView.setVisibility(View.GONE);
            onlineLayout.setVisibility(View.GONE);
            noClear.setVisibility(View.VISIBLE);
        }
    }
    HomeAdapter adapter;
    public void setAdapter(ArrayList<JSONObject> narrayList, RecyclerView recycler) {
        if(adapter == null) {
            adapter = new HomeAdapter(getActivity(), narrayList, new ItemEventListener());
            recycler.setAdapter(adapter);
        }else {
            adapter.notifyDataSetChanged();
        }
    }

    public void useravailabilityapi() {

        Api api = ApiFactory.getClientWithoutHeader(getActivity()).create(Api.class);
        HashMap<String, String> map = new HashMap<>();
        Call<ResponseBody> call;
        String accessToken = act.appPreference.getString(Constants.ACCESS_TOKEN);
        map.put("is_available", onOffstatus);
        call = api.user_availability(accessToken, map);
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
                            act.appPreference.setString(Constants.IS_AVAILABLE, onOffstatus);
                        } else if (object.optString(Constants.SUCCESS).equalsIgnoreCase(Constants.FALSE)) {
                            JSONArray jsonArray = object.optJSONArray(Constants.ERROR);
                            if (jsonArray != null) {
                                Constants.showToastAlert(object.getJSONObject(Constants.ERROR).getString(Constants.MESSAGE), getActivity());
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


    public void AgainstillDelay(){
        Constants.showProgressDialog(act, Constants.LOADING);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                getNotificationListing();
            }
        }, 3000);
    }

    public void getNotificationListing() {

        Api api = ApiFactory.getClientWithoutHeader(getActivity()).create(Api.class);
        Call<ResponseBody> call;
        String accessToken = act.appPreference.getString(Constants.ACCESS_TOKEN);
        call = api.getorderapi(accessToken);
        Log.e(Constants.LOG_CAT, "HEADERS : " + call.request().headers());
        Constants.showProgressDialog(act,  Constants.LOADING);
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
                            arrayList.clear();
                            MyFirebaseMessagingService.clearNotificationAll(act);
                            for (int i = 0; i < jsonArray.length(); i++) {
                                arrayList.add(jsonArray.optJSONObject(i));
                            }
                            if(act.appPreference.getBoolean(Constants.IS_USER_INFORMATION_STATUS)){
                                onOffstatus = act.appPreference.getString(Constants.IS_AVAILABLE);
                                if(onOffstatus.equalsIgnoreCase("1")) {
                                    if (arrayList != null && arrayList.size() > 0) {
                                        setAdapter(arrayList, recyclerView);
                                        noClear.setVisibility(View.GONE);
                                        recyclerView.setVisibility(View.VISIBLE);
                                        onlineLayout.setVisibility(View.GONE);
                                    } else {
                                        recyclerView.setVisibility(View.GONE);
                                        onlineLayout.setVisibility(View.GONE);
                                        noClear.setVisibility(View.VISIBLE);
                                    }
                                }else if(onOffstatus.equalsIgnoreCase("0")){
                                    recyclerView.setVisibility(View.GONE);
                                    onlineLayout.setVisibility(View.VISIBLE);
                                    noClear.setVisibility(View.GONE);
                                }else {
                                    recyclerView.setVisibility(View.GONE);
                                    onlineLayout.setVisibility(View.VISIBLE);
                                    noClear.setVisibility(View.GONE);
                                }
                            }else {
                                setData();
                            }


                        }else if (object.optString(Constants.SUCCESS).equalsIgnoreCase(Constants.FALSE)) {
                            showLayout();
                            JSONArray jsonArray = object.optJSONArray(Constants.ERROR);
                            if (jsonArray == null) {
                                Constants.showToastAlert(object.getJSONObject(Constants.ERROR).getString(Constants.MESSAGE), getActivity());
                            } else {
                                Constants.showToastAlert(jsonArray.getJSONObject(0).getString(Constants.MESSAGE), getActivity());
                            }
                        }

                    } else if (response != null) {
                        if (response.code() == 400 || response.code() == 500 || response.code() == 403 || response.code() == 404 || response.code() == 401) {
                            showLayout();
                            if (response.code() == 401) {
                                act.appPreference.showCustomAlert(getActivity(), getResources().getString(R.string.http_401_error));
                            } else {
                                Constants.showToastAlert(ErrorUtils.getHtttpCodeError(response.code()), getActivity());
                            }

                        } else {
                            showLayout();
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

    public void showLayout(){
        recyclerView.setVisibility(View.GONE);
        onOffstatus = act.appPreference.getString(Constants.IS_AVAILABLE);
        if(onOffstatus.equalsIgnoreCase("1")) {
            onlineLayout.setVisibility(View.GONE);
            noClear.setVisibility(View.VISIBLE);
        }else {
            onlineLayout.setVisibility(View.VISIBLE);
            noClear.setVisibility(View.GONE);
        }
    }

    public void getAcceptRequestApi(String id, final JSONObject get_customerjson, final String arriving_time) {

        Api api = ApiFactory.getClientWithoutHeader(getActivity()).create(Api.class);
        Call<ResponseBody> call;
        String accessToken = act.appPreference.getString(Constants.ACCESS_TOKEN);
        call = api.acceptRequest(accessToken, Integer.parseInt(id));
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
                            JSONObject jsonObject = object.optJSONObject("data");
                            String msg = jsonObject.optString("message");
                            if(msg.equalsIgnoreCase("Order successfully accepted.")){
                                if(act.countDownTimer!=null){
                                    act.countDownTimer.cancel();
                                }
                            MyFirebaseMessagingService.clearNotificationAll(act);
                                act.acceptDialog();
                                act.appPreference.setString("customer_json",get_customerjson.toString());
                                act.appPreference.setString("arriving_time",arriving_time);
                                act.showTimer();
                            }else {
                                Constants.showToastAlert(msg, getActivity());
                            }

                        }
                        else if (object.optString(Constants.SUCCESS).equalsIgnoreCase(Constants.FALSE)) {
                            JSONArray jsonArray = object.optJSONArray(Constants.ERROR);
                            if (jsonArray == null) {
                                Constants.showToastAlert(object.getJSONObject(Constants.ERROR).getString(Constants.MESSAGE), getActivity());
                            } else {
                                Constants.showToastAlert(jsonArray.getJSONObject(0).getString(Constants.MESSAGE), getActivity());
                            }
                        }

                    }
                    else if (response != null) {
                        if (response.code() == 400 || response.code() == 500 || response.code() == 403 || response.code() == 404 || response.code() == 401) {
                            if (response.code() == 401) {
                                act.appPreference.showCustomAlert(getActivity(), getResources().getString(R.string.http_401_error));
                            }else if(response.code() == 403){
                                Constants.showToastAlert("This request already accepted by another photographer.", getActivity());
                                getNotificationListing();
                            }else {
                                Constants.showToastAlert(ErrorUtils.getHtttpCodeError(response.code()), getActivity());
                            }

                        }
                        else {
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



    public class ItemEventListener implements HomeAdapter.ItemEvenListener {

        @Override
        public void onAcceptclick(int position) {
            JSONObject js = arrayList.get(position);
            String orderId = js.optJSONObject("order_slot").optString("order_id");
            String arrivingTime = js.optString("arriving_time");
            JSONObject getCustomerjson = js.optJSONObject("get_customer");
            if (AppUtils.isNetworkConnected()) {
                getAcceptRequestApi(orderId,getCustomerjson,arrivingTime);
            } else {
                final ViewGroup viewGroup = (ViewGroup) ((ViewGroup) getActivity().findViewById(android.R.id.content)).getChildAt(0);
                Constants.showSnackbar(getActivity(), viewGroup, getResources().getString(R.string.no_internet), "Retry");
            }
        }

        @Override
        public void onDeclineclick(int position) {
            arrayList.remove(position);
            if (arrayList != null && arrayList.size() > 0) {
                setAdapter(arrayList, recyclerView);
                noClear.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
                onlineLayout.setVisibility(View.GONE);
            } else {
                recyclerView.setVisibility(View.GONE);
                onlineLayout.setVisibility(View.GONE);
                noClear.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void onFullviewClecked(int position) {
            Log.e(Constants.LOG_CAT,"onFullviewClecked");
        }
    }

}
