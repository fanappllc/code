package com.fancustomer.fragment;

import android.Manifest;
import android.app.ActivityManager;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.fancustomer.R;
import com.fancustomer.activity.MenuScreen;


import com.fancustomer.adapter.SlotAdapter;
import com.fancustomer.bean.SlotBean;
import com.fancustomer.data.constant.Constants;
import com.fancustomer.data.preference.AppPreference;
import com.fancustomer.helper.ErrorUtils;
import com.fancustomer.helper.ExceptionHandler;
import com.fancustomer.helper.NumberProgressBar;
import com.fancustomer.webservice.Api;
import com.fancustomer.webservice.ApiFactory;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.skyfishjy.library.RippleBackground;
import com.special.ResideMenu.ResideMenu;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {
    private String address = "";
    private View view;
    ViewGroup viewGroup;
    MenuScreen menuActivity;
    public static CountDownTimer mCountDownTimerHome;
    private TextView textViewRequestNow;
    private RelativeLayout rlFirstSession;
    private RelativeLayout rlSecondSession;
    private RelativeLayout rlThirdSession;
    private RelativeLayout rlFourSession;
    private LinearLayout llSelectSession;
    private LinearLayout llSearchProgress;
    private ImageView imageViewMenu;
    private RecyclerView recyclerView;
    private GoogleMap googleMap;
    private ArrayList<SlotBean> slotBeanArrayList;
    private NumberProgressBar bnp;
    private SlotAdapter setAdapter;
    boolean isAPI = true;
    private String lat = "";
    private String longt = "";
    private ImageView imageView;
    private int pos;
    private String orderId = "";
    private TextView noSlotTextView;
    private EditText promoCodeEditText;
    private String promoCodeStr = "";
    private ImageView imageViewRefresh;
    private double latDouble;
    private double longtDouble;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_home, container, false);
        viewGroup = container;
        setToolBar();
        initView();
        if (Constants.isInternetOn(getActivity())) {
            getSlotsApi();
        } else {
            menuActivity.showSnackbar(viewGroup, getResources().getString(R.string.no_internet), Constants.RETRY);
        }


        setupMap();
        textViewRequestNow.setOnClickListener(listener);
        rlFirstSession.setOnClickListener(listener);
        rlSecondSession.setOnClickListener(listener);
        rlThirdSession.setOnClickListener(listener);
        rlFourSession.setOnClickListener(listener);
        return view;

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        menuActivity = (MenuScreen) context;

    }

    private void setToolBar() {
        imageViewRefresh = view.findViewById(R.id.imageView_refresh);
        RelativeLayout toolBarLeft = view.findViewById(R.id.toolBarLeft);
        imageViewMenu = view.findViewById(R.id.imageView_menu);
        TextView headerTextView = view.findViewById(R.id.hedertextview);
        ImageView toolbarbackpress = view.findViewById(R.id.toolbarbackpress);
        imageViewRefresh.setVisibility(View.VISIBLE);
        toolbarbackpress.setVisibility(View.GONE);
        imageViewMenu.setVisibility(View.VISIBLE);
        headerTextView.setText(getActivity().getResources().getString(R.string.nearest_photographer));
        toolBarLeft.setOnClickListener(listener);
        imageViewRefresh.setOnClickListener(listener);
    }


    private void initView() {
        slotBeanArrayList = new ArrayList<>();
        noSlotTextView = view.findViewById(R.id.noSlotTextView);
        noSlotTextView.setVisibility(View.GONE);
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        recyclerView.setHasFixedSize(true);
        imageView = view.findViewById(R.id.imageView);
        textViewRequestNow = view.findViewById(R.id.text_view_request_now);
        rlFirstSession = view.findViewById(R.id.rl_first_session);
        rlSecondSession = view.findViewById(R.id.rl_second_session);
        rlThirdSession = view.findViewById(R.id.rl_third_session);
        rlFourSession = view.findViewById(R.id.rl_four_session);
        llSelectSession = view.findViewById(R.id.ll_select_session);
        llSearchProgress = view.findViewById(R.id.ll_search_progress);
    }

    View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            switch (v.getId()) {

                case R.id.toolBarLeft:
                    MenuScreen.resideMenu.openMenu(ResideMenu.DIRECTION_LEFT);
                    break;
                case R.id.text_view_request_now:
                    try {
                        if (slotBeanArrayList.get(pos).isSelected()) {
                            String price = slotBeanArrayList.get(pos).getPrice();
                            String minutes = slotBeanArrayList.get(pos).getSlot_minutes();
                            if (!Constants.isStringNullOrBlank(address)) {
                                if (Constants.isInternetOn(getActivity())) {
                                    getDiscountSlotApi(price, minutes);
                                } else {
                                    menuActivity.showSnackbar(viewGroup, getResources().getString(R.string.no_internet), Constants.RETRY);
                                }

                            }
                        } else {

                            Constants.showToastAlert(getString(R.string.select_session_msg), getActivity());
                        }
                    } catch (Exception e) {
                        ExceptionHandler.printStackTrace(e);
                    }


                    break;
                case R.id.imageView_refresh:
                    if (Constants.isInternetOn(getActivity())) {
                        if (latDouble != 0 && longtDouble != 0) {
                            getNearPhotographer(latDouble, longtDouble, "loader");
                        }

                    } else {
                        menuActivity.showSnackbar(viewGroup, getResources().getString(R.string.no_internet), Constants.RETRY);
                    }
                    break;
                default:
                    break;
            }
        }
    };

    private void cancelDialog() {
        final Dialog dialog = new Dialog(getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.dialog_one_button);
        TextView textviewHeader = dialog.findViewById(R.id.tv_header);
        TextView textviewMessages = dialog.findViewById(R.id.tv_messages);
        TextView buttonOk = dialog.findViewById(R.id.button_ok);
        textviewHeader.setText("ALERT");
        textviewMessages.setText("No Photographer available at the moment, try again later.");
        buttonOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                Intent intent = new Intent(getActivity(), MenuScreen.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                getActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                getActivity().finish();
            }
        });
        dialog.show();

    }

    private void getAddress(double latitude, double longitude) {

        try {
            List<Address> addresses;
            Geocoder geocoder = new Geocoder(getActivity(), Locale.getDefault());
            addresses = geocoder.getFromLocation(latitude, longitude, 1);
            address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
            lat = String.valueOf(addresses.get(0).getLatitude());
            longt = String.valueOf(addresses.get(0).getLongitude());
            Constants.hideProgressDialog();
        } catch (Exception e) {
            ExceptionHandler.printStackTrace(e);
        }


    }


    public void sendRequest(String slot, String price, String location, String lat, String lont, String promoID) {
        Api api = ApiFactory.getClientWithoutHeader(getActivity()).create(Api.class);
        HashMap<String, String> map = new HashMap<>();
        Call<ResponseBody> call;
        map.put("slot", slot);
        map.put("price", price);
        map.put("location", location);
        map.put("latitude", lat);
        map.put("longitude", lont);
        map.put("promo_code_id", promoID);

        String accessToken = AppPreference.getInstance(getActivity()).getString(Constants.ACCESS_TOKEN);
        call = api.sendRequestApi(accessToken, map);
        Log.e(Constants.LOG_CAT, "API REQUEST SEND REQUEST------------------->>>>>:" + map + " " + call.request().url());
        Log.e(Constants.LOG_CAT, "HEADERS :@@@@@@@@@@@@@@@@@@@ " + call.request().headers());


        Constants.showProgressDialog(getActivity(), Constants.LOADING);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    if (response.isSuccessful()) {
                        String output = ErrorUtils.getResponseBody(response);
                        JSONObject object = new JSONObject(output);
                        Constants.hideProgressDialog();
                        Log.d("tag", object.toString(1));

                        if (object.optString(Constants.SUCCESS).equalsIgnoreCase(Constants.TRUE)) {
                            Log.e(Constants.LOG_CAT, "API REQUEST SEND REQUEST PHOTOGRAPHER Response================>>>>>" + object.toString());
                            JSONObject jsonObject = object.optJSONObject("data");
                            orderId = jsonObject.optString("order_id");
                            llSelectSession.setVisibility(View.GONE);
                            textViewRequestNow.setVisibility(View.GONE);
                            llSearchProgress.setVisibility(View.VISIBLE);
                            imageViewMenu.setVisibility(View.GONE);
                            imageViewRefresh.setVisibility(View.GONE);
                            ripple();
                            startSearching();


                        } else if (object.optString(Constants.SUCCESS).equalsIgnoreCase(Constants.FALSE)) {
                            ErrorUtils.showFalseMessage(object, getActivity());


                        }
                    } else if (response.code() == 400 || response.code() == 422 || response.code() == 500 || response.code() == 403 || response.code() == 404 || response.code() == 401) {
                        Constants.hideProgressDialog();
                        if (response.code() == 401) {
                            Constants.showSessionExpireAlert(getActivity());
                        } else {
                            Constants.showToastAlert(ErrorUtils.getHtttpCodeError(response.code()), getActivity());
                        }

                    } else {
                        Constants.hideProgressDialog();
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

    private int total = 0;

    public void startSearching() {
        total = 0;
        bnp = view.findViewById(R.id.number_progress_bar);
        bnp.setProgress(total);
//        int oneMin = 1 * 60 * 1000;
        mCountDownTimerHome = new CountDownTimer(60000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                Log.v("Log_tag", "Tick of Progress" + total + millisUntilFinished);
                total++;
                bnp.setProgress((int) total);
                bnp.incrementProgressBy(1);
            }

            @Override
            public void onFinish() {
                try {
                    ActivityManager am = (ActivityManager) getActivity().getSystemService(Context.ACTIVITY_SERVICE);
                    ComponentName cn = am.getRunningTasks(1).get(0).topActivity;
                    System.out.println("=======" + cn.getClassName());
                    if (cn.getClassName().equals("com.fancustomer.activity.MenuScreen")) {
                        if (Constants.isInternetOn(getActivity())) {
                            proceedAndCancelApi(orderId, "cancel");
                        } else {
                            menuActivity.showSnackbar(viewGroup, getResources().getString(R.string.no_internet), Constants.RETRY);
                        }

                    } else {
                        mCountDownTimerHome.cancel();
                    }
                } catch (Exception e) {
                    ExceptionHandler.printStackTrace(e);
                }

            }
        };
        mCountDownTimerHome.start();

    }


    public void proceedAndCancelApi(String orderID, final String flag) {
        Api api = ApiFactory.getClientWithoutHeader(getActivity()).create(Api.class);
        Call<ResponseBody> call;
        call = api.getProceedCancelApi(AppPreference.getInstance(getActivity()).getString(Constants.ACCESS_TOKEN), orderID, flag);
        Log.e(Constants.LOG_CAT, "API proceedAndCancelApi------------------->>>>>:" + call.request().url());
        Log.e(Constants.LOG_CAT, "HEADERS :@####@ " + call.request().headers());
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
                            Log.e(Constants.LOG_CAT, "onResponse:API proceedAndCancelApi>>>>>>>>>>>>>>>>>>>>>>>" + object.toString());
                            cancelDialog();
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

    private void setList() {
        if (setAdapter == null) {
            setAdapter = new SlotAdapter(getContext(), slotBeanArrayList);
            recyclerView.setAdapter(setAdapter);
        } else {
            setAdapter.notifyDataSetChanged();
        }

        setAdapter.onItemClickMethod(new SlotAdapter.ItemInterFace() {
            @Override
            public void onItemClick(View view, int position) {
                pos = position;
                int i1 = view.getId();
                if (i1 == R.id.rl_first_session) {
                    for (int i = 0; i < slotBeanArrayList.size(); i++) {
                        SlotBean slottBean = slotBeanArrayList.get(i);
                        if (slotBeanArrayList.get(i).isSelected()) {
                            slottBean.setSelected(false);
                        } else {
                            slottBean.setSelected(false);
                        }
                        if (i == position) {
                            slottBean.setSelected(true);
                        }
                        slotBeanArrayList.set(i, slottBean);
                    }
                    setAdapter.notifyDataSetChanged();

                }
            }
        });
    }

    public void getSlotsApi() {


        Api api = ApiFactory.getClientWithoutHeader(getActivity()).create(Api.class);
        Call<ResponseBody> call;

        call = api.getSlots();
        Log.e(Constants.LOG_CAT, "API GET SLOTS------------------->>>>>:" + call.request().url());
        Log.e(Constants.LOG_CAT, "HEADERS @@@@@@@@@@@@@@: " + call.request().headers());


        Constants.showProgressDialog(getActivity(), Constants.LOADING);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    if (response.isSuccessful()) {
                        String output = ErrorUtils.getResponseBody(response);
                        JSONObject object = new JSONObject(output);
                        Log.d("tag", object.toString(1));

                        if (object.optString(Constants.SUCCESS).equalsIgnoreCase(Constants.TRUE)) {
                            Log.e(Constants.LOG_CAT, "onResponse: API GET SLOTS-=" + object.toString());

                            JSONArray jsonArray = object.optJSONArray("data");
                            if (jsonArray != null && jsonArray.length() > 0) {
                                noSlotTextView.setVisibility(View.GONE);
                                recyclerView.setVisibility(View.VISIBLE);
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject jsonObject = jsonArray.optJSONObject(i);
                                    String id = jsonObject.optString("id");
                                    String slotMinutes = jsonObject.optString("slot_minutes");
                                    String price = jsonObject.optString("price");
                                    String statusStr = jsonObject.optString("status");
                                    String createdAt = jsonObject.optString("created_at");
                                    SlotBean slotBean = new SlotBean(id, slotMinutes, price, statusStr, createdAt, false);
                                    slotBeanArrayList.add(slotBean);
                                }
                            } else {
                                noSlotTextView.setVisibility(View.VISIBLE);
                                recyclerView.setVisibility(View.GONE);
                            }
                            setList();
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

    private void ripple() {
        final RippleBackground rippleBackground = view.findViewById(R.id.content);
        imageView.setVisibility(View.VISIBLE);
        rippleBackground.startRippleAnimation();
    }


    private void setupMap() {

        SupportMapFragment mSupportMapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.fragmentMap1);

        if (mSupportMapFragment == null) {
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            mSupportMapFragment = SupportMapFragment.newInstance();
            fragmentTransaction.replace(R.id.fragmentMap1, mSupportMapFragment).commit();
        }

        if (mSupportMapFragment != null) {
            mSupportMapFragment.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap map) {
                    googleMap = map;
                    map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                    if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }
                    googleMap.setMyLocationEnabled(true);
                    if (googleMap != null) {
                        googleMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
                            @Override
                            public void onMyLocationChange(Location location) {
                                if (location != null) {
                                    getAddress(location.getLatitude(), location.getLongitude());
                                    if (isAPI) {
                                        try {
                                            if (Constants.isInternetOn(getActivity())) {
                                                getUpdateLocation(location.getLatitude(), location.getLongitude());
                                            } else {
                                                menuActivity.showSnackbar(viewGroup, getResources().getString(R.string.no_internet), Constants.RETRY);
                                            }
                                        } catch (Exception e) {
                                            ExceptionHandler.printStackTrace(e);
                                        }
                                    }


                                    CameraPosition cameraPosition = new CameraPosition.Builder().target(new LatLng(location.getLatitude(), location.getLongitude())).zoom(18.0f).build();
                                    CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition);
                                    googleMap.moveCamera(cameraUpdate);
                                }


                            }
                        });


                    }
                }
            });
        }
    }

    public void getUpdateLocation(final double lat, final double longt) {


        Api api = ApiFactory.getClientWithoutHeader(getActivity()).create(Api.class);
        HashMap<String, String> map = new HashMap<>();
        Call<ResponseBody> call = null;
        map.put("latitude", String.valueOf(lat));
        map.put("longitude", String.valueOf(longt));


        String accessToken = AppPreference.getInstance(getActivity()).getString(Constants.ACCESS_TOKEN);
        call = api.updateLocationAPI(accessToken, map);
        Log.e(Constants.LOG_CAT, "API REQUEST UPDATE LOCATION URL------------------->>>>>:" + map + " " + call.request().url());
        Log.e(Constants.LOG_CAT, "HEADERS : " + call.request().headers());
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    if (response.isSuccessful()) {
                        isAPI = false;
                        String output = ErrorUtils.getResponseBody(response);
                        JSONObject object = new JSONObject(output);
                        Log.d("tag", object.toString(1));
                        if (object.optString(Constants.SUCCESS).equalsIgnoreCase(Constants.TRUE)) {
                            Log.e(Constants.LOG_CAT, "API REQUEST NEAR UPDATE LOCATION================>>>>>" + object.toString());
                            try {
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (Constants.isInternetOn(getActivity())) {
                                            latDouble = lat;
                                            longtDouble = longt;
                                            getNearPhotographer(lat, longt, "");
                                        } else {
                                            menuActivity.showSnackbar(viewGroup, getResources().getString(R.string.no_internet), Constants.RETRY);
                                        }


                                    }
                                });
                            } catch (Exception e) {
                                ExceptionHandler.printStackTrace(e);
                            }


                        } else if (object.optString(Constants.SUCCESS).equalsIgnoreCase(Constants.FALSE)) {
                            ErrorUtils.showFalseMessage(object, getActivity());


                        }
                    } else if (response.code() == 400 || response.code() == 500 || response.code() == 403 || response.code() == 404 || response.code() == 401) {
                        try {
                            if (response.code() == 401) {
                                Constants.showSessionExpireAlert(getActivity());
                            } else {
                                Constants.showToastAlert(ErrorUtils.getHtttpCodeError(response.code()), getActivity());
                            }
                        } catch (Exception e) {
                            ExceptionHandler.printStackTrace(e);
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

    public void getNearPhotographer(double lat, double longt, String lodaer) {


        Api api = ApiFactory.getClientWithoutHeader(getActivity()).create(Api.class);
        HashMap<String, String> map = new HashMap<>();
        Call<ResponseBody> call;
        map.put("latitude", String.valueOf(lat));
        map.put("longitude", String.valueOf(longt));
        String accessToken = AppPreference.getInstance(getActivity()).getString(Constants.ACCESS_TOKEN);
        call = api.getNearPhoto(accessToken, map);
        Log.e(Constants.LOG_CAT, "API REQUEST NEAR PHOTOGRAPHER URL------------------->>>>>:" + map + " " + call.request().url());
        Log.e(Constants.LOG_CAT, "HEADERS : " + call.request().headers());
        if (!lodaer.equals("")) {
            Constants.showProgressDialog(getActivity(), Constants.LOADING);
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
                            Log.e(Constants.LOG_CAT, "API REQUEST NEAR PHOTOGRAPHER Response================>>>>>" + object.toString());
                            JSONArray jsonArray = object.optJSONArray("data");
                            if (jsonArray != null && jsonArray.length() > 0) {
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject jsonObject = jsonArray.optJSONObject(i);
                                    double latitude = Double.parseDouble(jsonObject.optString("latitude"));
                                    double longitude = Double.parseDouble(jsonObject.optString("longitude"));
                                    addDestinationMarker(latitude, longitude);

                                }
                            }

                            setList();
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

    private void addDestinationMarker(double lat, double lon) {

        try {

            final LatLng latLng = new LatLng(lat, lon);
            Drawable drawableOrange = getResources().getDrawable(R.mipmap.pointer);
            BitmapDescriptor markerIcon = getMarkerIconFromDrawable(drawableOrange);
            Marker perth = googleMap.addMarker(new MarkerOptions()
                    .position(latLng)
                    .icon(markerIcon)
                    .draggable(true));
        } catch (Exception e) {
            ExceptionHandler.printStackTrace(e);

        }

    }

    private BitmapDescriptor getMarkerIconFromDrawable(Drawable drawable) {
        try {
            Canvas canvas = new Canvas();
            Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
            canvas.setBitmap(bitmap);
            drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
            drawable.draw(canvas);
            return BitmapDescriptorFactory.fromBitmap(bitmap);
        } catch (Exception e) {
            ExceptionHandler.printStackTrace(e);
        }
        return null;
    }


    public void getPromoAPI(String promo) {
        Api api = ApiFactory.getClientWithoutHeader(getActivity()).create(Api.class);
        HashMap<String, String> map = new HashMap<>();
        Call<ResponseBody> call = null;
        map.put("promocode", promo);
        String accessToken = AppPreference.getInstance(getActivity()).getString(Constants.ACCESS_TOKEN);
        call = api.userPromoCodeAPi(accessToken, map);
        Log.e(Constants.LOG_CAT, "API REQUEST GET PROMO CODE------------------->>>>>:" + map + " " + call.request().url());
        Log.e(Constants.LOG_CAT, "HEADERS : " + call.request().headers());

        Constants.showProgressDialog(getActivity(), "Loading");
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Constants.hideProgressDialog();


                try {
                    if (response.isSuccessful()) {
                        isAPI = false;
                        String output = ErrorUtils.getResponseBody(response);
                        JSONObject object = new JSONObject(output);
                        Log.d("tag", object.toString(1));

                        if (object.optString(Constants.SUCCESS).equalsIgnoreCase(Constants.TRUE)) {
                            Log.e(Constants.LOG_CAT, "API REQUEST GET PROMO CODE================>>>>>" + object.toString());
                            JSONObject jsonObject = object.optJSONObject("data");
                            String id = jsonObject.optString("id");
                            String discountPercent = jsonObject.optString("discount_percent");
                            applySuccessDialog(id, discountPercent);
                        } else if (object.optString(Constants.SUCCESS).equalsIgnoreCase(Constants.FALSE)) {
                            ErrorUtils.showFalseMessage(object, getActivity());


                        }
                    } else if (response.code() == 400 || response.code() == 500 || response.code() == 403 || response.code() == 404 || response.code() == 401) {
                        try {
                            if (response.code() == 401) {
                                Constants.showSessionExpireAlert(getActivity());
                            } else {
                                String output = ErrorUtils.getResponseBody(response);
                                JSONObject object = new JSONObject(output);
                                if (object.optString(Constants.SUCCESS).equalsIgnoreCase(Constants.FALSE)) {
                                    ErrorUtils.showFalseMessage(object, getActivity());


                                }
                            }
                        } catch (Exception e) {
                            ExceptionHandler.printStackTrace(e);
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

    public void getDiscountSlotApi(final String slotPrice, final String mintes) {
        Api api = ApiFactory.getClientWithoutHeader(getActivity()).create(Api.class);
        Call<ResponseBody> call = null;


        String accessToken = AppPreference.getInstance(getActivity()).getString(Constants.ACCESS_TOKEN);
        call = api.getDiscountAPI(accessToken, slotPrice);
        Log.e(Constants.LOG_CAT, "API REQUEST getDiscount------------------->>>>>:" + slotPrice + " " + call.request().url());
        Log.e(Constants.LOG_CAT, "HEADERS : " + call.request().headers());


        Constants.showProgressDialog(getActivity(), "Loading");
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Constants.hideProgressDialog();


                try {
                    if (response.isSuccessful()) {
                        isAPI = false;
                        String output = ErrorUtils.getResponseBody(response);
                        JSONObject object = new JSONObject(output);
                        Log.d("tag", object.toString(1));

                        if (object.optString(Constants.SUCCESS).equalsIgnoreCase(Constants.TRUE)) {
                            Log.e(Constants.LOG_CAT, "API REQUEST getDiscount================>>>>>" + object.toString());
                            JSONObject jsonObject = object.optJSONObject("data");

                            String isDiscount = jsonObject.optString("is_discount");
                            String discountPercent = jsonObject.optString("discount_percent");
                            if (isDiscount.equals("1")) {
                                showDiscountDialog(slotPrice, discountPercent, mintes);
                            } else {
                                showPromocodeDialog();
                            }
                        } else if (object.optString(Constants.SUCCESS).equalsIgnoreCase(Constants.FALSE)) {
                            ErrorUtils.showFalseMessage(object, getActivity());
                        }
                    } else if (response.code() == 400 || response.code() == 500 || response.code() == 403 || response.code() == 404 || response.code() == 401) {
                        try {
                            if (response.code() == 401) {
                                Constants.showSessionExpireAlert(getActivity());
                            } else {
                                Constants.showToastAlert(ErrorUtils.getHtttpCodeError(response.code()), getActivity());
                            }
                        } catch (Exception e) {
                            ExceptionHandler.printStackTrace(e);
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


    private void showPromocodeDialog() {
        final Dialog dialog = new Dialog(getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.dialog_promocode);
        promoCodeEditText = dialog.findViewById(R.id.promoCodeEditText);
        TextView applyButton = dialog.findViewById(R.id.apply_button);
        TextView dontPromoCodeTextView = dialog.findViewById(R.id.dontPromoCodeTextView);
        TextView buttonCancel = dialog.findViewById(R.id.button_cancel);


        applyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                promoCodeStr = promoCodeEditText.getText().toString().trim();
                if (promoCodeStr.equals("")) {
                    Constants.showToastAlert("Please Enter promocode", getActivity());
                } else {
                    dialog.dismiss();
                    if (Constants.isInternetOn(getActivity())) {
                        getPromoAPI(promoCodeStr);
                    } else {
                        menuActivity.showSnackbar(viewGroup, getResources().getString(R.string.no_internet), Constants.RETRY);
                    }
                }
            }
        });
        dontPromoCodeTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                try {

                    if (slotBeanArrayList.get(pos).isSelected()) {
                        String price = slotBeanArrayList.get(pos).getPrice();
                        String minutes = slotBeanArrayList.get(pos).getSlot_minutes();
                        if (!Constants.isStringNullOrBlank(address)) {
                            if (Constants.isInternetOn(getActivity())) {
                                sendRequest(minutes, price, address, lat, longt, "");
                            } else {
                                menuActivity.showSnackbar(viewGroup, getResources().getString(R.string.no_internet), Constants.RETRY);
                            }

                        }
                    } else {

                        Constants.showToastAlert(getString(R.string.select_session_msg), getActivity());
                    }
                } catch (Exception e) {
                    ExceptionHandler.printStackTrace(e);
                }


            }
        });
        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();

            }
        });
        dialog.show();

    }

    private void applySuccessDialog(final String promoID, String percentage) {
        final Dialog dialog = new Dialog(getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.dialog_apply_success);
        TextView buttonCancel = dialog.findViewById(R.id.button_cancel);
        TextView buttonProceed = dialog.findViewById(R.id.button_proceed);
        TextView textviewMessages = dialog.findViewById(R.id.textview__messages);
        textviewMessages.setText("You have discount of " + percentage + "% on this booking.");
        buttonProceed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                try {
                    if (slotBeanArrayList.get(pos).isSelected()) {
                        String price = slotBeanArrayList.get(pos).getPrice();
                        String minutes = slotBeanArrayList.get(pos).getSlot_minutes();
                        if (!Constants.isStringNullOrBlank(address)) {
                            if (Constants.isInternetOn(getActivity())) {
                                sendRequest(minutes, price, address, lat, longt, promoID);
                            } else {
                                menuActivity.showSnackbar(viewGroup, getResources().getString(R.string.no_internet), Constants.RETRY);
                            }

                        }
                    } else {

                        Constants.showToastAlert(getString(R.string.select_session_msg), getActivity());
                    }
                } catch (Exception e) {
                    ExceptionHandler.printStackTrace(e);
                }


            }
        });
        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dialog.dismiss();
            }
        });
        dialog.show();

    }

    private void showDiscountDialog(final String price, String percentage, final String mint) {
        final Dialog dialog = new Dialog(getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.dialog_apply_success);
        TextView textView = dialog.findViewById(R.id.textview__header);
        TextView textView1 = dialog.findViewById(R.id.button_proceed);
        TextView buttonCancel = dialog.findViewById(R.id.button_cancel);
        TextView textviewMessages = dialog.findViewById(R.id.textview__messages);
        textView.setText("Discount");
        textviewMessages.setText("You have discount of " + percentage + "% on this booking.");
        textView1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                try {

                    if (!Constants.isStringNullOrBlank(address)) {
                        if (Constants.isInternetOn(getActivity())) {
                            sendRequest(mint, price, address, lat, longt, "");
                        } else {
                            menuActivity.showSnackbar(viewGroup, getResources().getString(R.string.no_internet), Constants.RETRY);
                        }
                    }
                } catch (Exception e) {
                    ExceptionHandler.printStackTrace(e);
                }


            }
        });
        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dialog.dismiss();
            }
        });
        dialog.show();

    }


}
