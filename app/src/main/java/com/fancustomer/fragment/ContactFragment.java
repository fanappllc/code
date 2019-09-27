package com.fancustomer.fragment;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.fancustomer.R;
import com.fancustomer.activity.MenuScreen;
import com.fancustomer.data.constant.Constants;
import com.fancustomer.helper.ErrorUtils;
import com.fancustomer.helper.ExceptionHandler;
import com.fancustomer.webservice.Api;
import com.fancustomer.webservice.ApiFactory;
import com.special.ResideMenu.ResideMenu;

import org.json.JSONObject;


import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class ContactFragment extends Fragment implements View.OnClickListener {

    private View view;
    private TextView showId;
    private String faceUrl;
    private String googleUrl;
    private String linkUrl;
    private String twiUrl;
    ViewGroup viewGroup;
    MenuScreen menuActivity;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (view == null) {
            view = inflater.inflate(R.layout.fragment_contact, container, false);
            viewGroup = container;
            setToolBar();
            init();
            if (Constants.isInternetOn(getActivity())) {
                getContactDetail();
            } else {
                menuActivity.showSnackbar(viewGroup, getResources().getString(R.string.no_internet), Constants.RETRY);

            }
        }
        return view;
    }

    private void init() {
        showId = view.findViewById(R.id.show_id);
        ImageView facebook = view.findViewById(R.id.facebook);
        ImageView twitter = view.findViewById(R.id.twitter);
        ImageView gmail = view.findViewById(R.id.gmail);
        ImageView linkedin = view.findViewById(R.id.linkedin);

        facebook.setOnClickListener(this);
        twitter.setOnClickListener(this);
        gmail.setOnClickListener(this);
        linkedin.setOnClickListener(this);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        menuActivity = (MenuScreen) context;

    }

    private void setToolBar() {

        RelativeLayout toolBarLeft = view.findViewById(R.id.toolBarLeft);
        ImageView imageViewMenu = view.findViewById(R.id.imageView_menu);
        TextView headerTextView = view.findViewById(R.id.hedertextview);
        ImageView toolbarbackpress = view.findViewById(R.id.toolbarbackpress);
        toolbarbackpress.setVisibility(View.GONE);
        imageViewMenu.setVisibility(View.VISIBLE);
        headerTextView.setText(getActivity().getResources().getString(R.string.contact_us));
        toolBarLeft.setOnClickListener(this);


    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.toolBarLeft:
                MenuScreen.resideMenu.openMenu(ResideMenu.DIRECTION_LEFT);
                break;

            case R.id.facebook:
                openLink(faceUrl);
                break;

            case R.id.twitter:
                openLink(twiUrl);
                break;

            case R.id.gmail:
                openLink(googleUrl);
                break;

            case R.id.linkedin:
                openLink(linkUrl);
                break;
            default:
                break;


        }
    }

    public void getContactDetail() {


        Api api = ApiFactory.getClientWithoutHeader(getActivity()).create(Api.class);
        Call<ResponseBody> call;
        call = api.getSettings();

        Constants.showProgressDialog(getActivity(), Constants.LOADING);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Constants.hideProgressDialog();
                try {
                    if (response != null && response.isSuccessful() && response.code() == 200) {
                        String output = ErrorUtils.getResponseBody(response);
                        JSONObject object = new JSONObject(output);
                        if (object.optString("success").equalsIgnoreCase("true")) {
                            JSONObject jsonObject = object.optJSONObject("data");
                            String adminEmail = jsonObject.optString("admin_email");
                            faceUrl = jsonObject.optString("facebook");
                            googleUrl = jsonObject.optString("google_plus");
                            twiUrl = jsonObject.optString("twitter");
                            linkUrl = jsonObject.optString("linkedin");
                            showId.setText(adminEmail);
                        } else if (object.optString(Constants.SUCCESS).equalsIgnoreCase(Constants.FALSE)) {
                            ErrorUtils.showFalseMessage(object, getActivity());
                        }

                    } else if (response != null) {
                        if (response.code() == 400 || response.code() == 500 || response.code() == 403 || response.code() == 404 || response.code() == 401) {
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
                    }
                } catch (Exception e) {
                    ExceptionHandler.printStackTrace(e);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Constants.hideProgressDialog();
            }
        });


    }


    public void openLink(String url) {
        if (Constants.isInternetOn(getActivity())) {
            if (!Constants.isStringNullOrBlank(url)) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(intent);
            }
        } else {
            menuActivity.showSnackbar(viewGroup, getResources().getString(R.string.no_internet), "Retry");

        }


    }
}
