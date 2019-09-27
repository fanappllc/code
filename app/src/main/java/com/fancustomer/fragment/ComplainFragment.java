package com.fancustomer.fragment;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.fancustomer.R;

import com.fancustomer.activity.MenuScreen;


import com.fancustomer.adapter.ComplainAdapter;

import com.fancustomer.bean.ComplainBean;

import com.fancustomer.data.constant.Constants;
import com.fancustomer.data.preference.AppPreference;
import com.fancustomer.helper.ErrorUtils;
import com.fancustomer.helper.MyCustomCheckboxTextView;
import com.fancustomer.webservice.Api;
import com.fancustomer.webservice.ApiFactory;
import com.special.ResideMenu.ResideMenu;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class ComplainFragment extends Fragment implements View.OnClickListener {

    private Dialog dialogComplain;
    private TextView headerRightText;
    private TextView textViewComapin;
    private TextView textViewSubmit;
    private RelativeLayout toolBarLeft;
    private ImageView imageViewMenu;
    private ArrayList<ComplainBean> complainBeanArrayList;
    private ComplainAdapter complainAdapter;
    private String complainId = "";
    private String orderIdStr = "";
    private String messageStr = "";
    private View view;
    private EditText edtOrderID;
    private EditText edtMessage;
    ViewGroup viewGroup;
    MenuScreen menuActivity;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_complain, container, false);
        viewGroup = container;
        complainBeanArrayList = new ArrayList<>();
        setToolBar();
        intView();
        setClicks();
        if (Constants.isInternetOn(getActivity())) {
            getComaplain();
        } else {
            menuActivity.showSnackbar(viewGroup, getResources().getString(R.string.no_internet), Constants.RETRY);
        }
        return view;
    }

    private void intView() {
        textViewSubmit = view.findViewById(R.id.text_view_submit);
        textViewComapin = view.findViewById(R.id.textViewComapin);
        edtOrderID = view.findViewById(R.id.edtOrderID);
        edtMessage = view.findViewById(R.id.edtMessage);
    }

    private void setClicks() {
        textViewComapin.setOnClickListener(this);
        textViewSubmit.setOnClickListener(this);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        menuActivity = (MenuScreen) context;

    }

    private void setData() {

        messageStr = edtMessage.getText().toString().trim();
        orderIdStr = edtOrderID.getText().toString().trim();

        if (orderIdStr.equals("")) {
            Constants.showToastAlert(getResources().getString(R.string.please_enter_order_id), getActivity());
        } else if (complainId.equals("")) {

            Constants.showToastAlert(getResources().getString(R.string.select_your_complain), getActivity());
        } else if (messageStr.equals("")) {

            Constants.showToastAlert(getResources().getString(R.string.please_enter_your_message), getActivity());
        } else {
            if (Constants.isInternetOn(getActivity())) {
                complainPostAPI();
            } else {
                menuActivity.showSnackbar(viewGroup, getResources().getString(R.string.no_internet), Constants.RETRY);
            }

        }


    }


    private void setToolBar() {
        headerRightText = view.findViewById(R.id.headerRightText);
        toolBarLeft = view.findViewById(R.id.toolBarLeft);
        imageViewMenu = view.findViewById(R.id.imageView_menu);
        TextView headerTextView = view.findViewById(R.id.hedertextview);
        ImageView toolbarbackpress = view.findViewById(R.id.toolbarbackpress);
        toolbarbackpress.setVisibility(View.GONE);
        imageViewMenu.setVisibility(View.VISIBLE);
        headerRightText.setVisibility(View.GONE);
        headerTextView.setText(getActivity().getResources().getString(R.string.complain));
        toolBarLeft.setOnClickListener(this);
        headerRightText.setOnClickListener(this);
    }

    public void getComaplain() {

        Api api = ApiFactory.getClientWithoutHeader(getActivity()).create(Api.class);
        Call<ResponseBody> call;
        call = api.getComplain(AppPreference.getInstance(getActivity()).getString(Constants.ACCESS_TOKEN));
        Log.e(Constants.LOG_CAT, "FAN Complain API------------------->>>>>:" + " " + call.request().url());
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
                            Log.e(Constants.LOG_CAT, "onResponse: FAN Complain API LIST=============>>>>>>>>>>" + object.toString());
                            JSONArray jsonArray = object.optJSONArray("data");
                            if (jsonArray != null && jsonArray.length() > 0) {
                                headerRightText.setVisibility(View.VISIBLE);
                                for (int i = 0; i < jsonArray.length(); i++) {

                                    JSONObject jsonObject = jsonArray.optJSONObject(i);
                                    String id = jsonObject.optString("id");
                                    String title = jsonObject.optString("title");
                                    ComplainBean complainBean = new ComplainBean(id, title);
                                    complainBeanArrayList.add(complainBean);


                                }
                            } else {
                                headerRightText.setVisibility(View.GONE);
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

    private void complainDilog() {
        dialogComplain = new Dialog(getActivity());
        dialogComplain.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialogComplain.setContentView(R.layout.countrydialog_item);
        ListView countryListView = dialogComplain.findViewById(R.id.listView);
        TextView dialogHeaderText = dialogComplain.findViewById(R.id.dialogHeaderText);

        dialogComplain.show();
        dialogHeaderText.setText("COMPLAIN MESSAGE");
        complainAdapter = new ComplainAdapter(getActivity(), complainBeanArrayList);
        countryListView.setAdapter(complainAdapter);
        countryListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ((MyCustomCheckboxTextView) view).setChecked(true);
                complainId = complainBeanArrayList.get(position).getId();
                String title = (complainBeanArrayList.get(position).getTitle());
                textViewComapin.setText(title);
                textViewComapin.setTextColor(getResources().getColor(R.color.black));
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        dialogComplain.dismiss();

                    }
                }, 300);

            }
        });

    }

    private void thankDialog() {
        final Dialog dialog = new Dialog(getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.dialog_one_button);
        TextView textviewHeader = dialog.findViewById(R.id.tv_header);
        TextView textviewMessages = dialog.findViewById(R.id.tv_messages);
        TextView buttonOk = dialog.findViewById(R.id.button_ok);
        textviewHeader.setText("SUCCESS");
        textviewMessages.setText("Thank you for your feedback!!");
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

    public void complainPostAPI() {
        Api api = ApiFactory.getClientWithoutHeader(getActivity()).create(Api.class);
        HashMap<String, String> map = new HashMap<>();
        Call<ResponseBody> call;
        map.put("order_id", orderIdStr);
        map.put("complain_header_id", complainId);
        map.put("description", messageStr);


        String accessToken = AppPreference.getInstance(getActivity()).getString(Constants.ACCESS_TOKEN);
        call = api.complainAPI(accessToken, map);
        Log.e(Constants.LOG_CAT, "API REQUEST COMPLAIN API------------------->>>>>:" + map + " " + call.request().url());
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
                            Log.e(Constants.LOG_CAT, "API REQUEST COMPLAIN  API================>>>>>" + object.toString());
                            thankDialog();
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
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.toolBarLeft:
                MenuScreen.resideMenu.openMenu(ResideMenu.DIRECTION_LEFT);
                break;
            case R.id.textViewComapin:
                complainDilog();
                break;
            case R.id.text_view_submit:
                setData();
                break;
            default:
                break;
        }
    }
}
