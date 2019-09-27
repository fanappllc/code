package com.fancustomer.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.fancustomer.R;
import com.fancustomer.activity.MenuScreen;
import com.fancustomer.activity.TrackPhotographer;
import com.fancustomer.activity.UpdateProfileActivity;
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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class ProfileFragment extends Fragment implements View.OnClickListener {


    private View view;
    private ProgressBar progressBar;
    private TextView txtEditDetail;
    private CircleImageView userImageView;
    private TextView firstNameTextView;
    private TextView lastNameTextView;
    private TextView emailTextView;
    private TextView addressTextView;
    private TextView zipCodeTextView;
    private TextView mobileTextView;
    private String firstName = "";
    private String lastName = "";
    private String email = "";
    private String mobile = "";
    private String profileImage = "";
    private String address = "";
    private String zipCode = "";
    ViewGroup viewGroup;
    MenuScreen menuActivity;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_profile, container, false);
        viewGroup = container;
        setToolBar();
        intView();
        setClicks();
        if (Constants.isInternetOn(getActivity())) {
            getUser();
        } else {
            menuActivity.showSnackbar(viewGroup, getResources().getString(R.string.no_internet), Constants.RETRY);
        }
        return view;
    }

    private void setToolBar() {
        RelativeLayout toolBarLeft = view.findViewById(R.id.toolBarLeft);
        ImageView imageViewMenu = view.findViewById(R.id.imageView_menu);
        TextView headerTextView = view.findViewById(R.id.hedertextview);
        ImageView toolbarbackpress = view.findViewById(R.id.toolbarbackpress);
        toolbarbackpress.setVisibility(View.GONE);
        imageViewMenu.setVisibility(View.VISIBLE);
        headerTextView.setText(getActivity().getResources().getString(R.string.profile_View));
        toolBarLeft.setOnClickListener(this);
    }

    private void intView() {
        userImageView = view.findViewById(R.id.userImageView);
        progressBar = view.findViewById(R.id.progress_bar);
        firstNameTextView = view.findViewById(R.id.firstNameTextView);
        lastNameTextView = view.findViewById(R.id.lastNameTextView);
        emailTextView = view.findViewById(R.id.emailTextView);
        addressTextView = view.findViewById(R.id.addressTextView);
        zipCodeTextView = view.findViewById(R.id.zipCodeTextView);
        mobileTextView = view.findViewById(R.id.mobileTextView);
        txtEditDetail = view.findViewById(R.id.txtEditDetail);
    }

    private void setClicks() {

        txtEditDetail.setOnClickListener(this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 201 && resultCode == getActivity().RESULT_OK) {
            if (Constants.isInternetOn(getActivity())) {
                getUser();

            } else {
                menuActivity.showSnackbar(viewGroup, getResources().getString(R.string.no_internet), Constants.RETRY);
            }


        }


    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        menuActivity = (MenuScreen) context;

    }

    public void getUser() {


        Api api = ApiFactory.getClientWithoutHeader(getActivity()).create(Api.class);
        Call<ResponseBody> call;

        call = api.getUserApi(AppPreference.getInstance(getActivity()).getString(Constants.ACCESS_TOKEN));
        Log.e(Constants.LOG_CAT, "API REQUEST USER LOGIN ------------------->>>>>:" + " " + call.request().url());
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
                            Log.e(Constants.LOG_CAT, "onResponse: USRE PROFILE=" + object.toString());
                            JSONObject jsonObject = object.optJSONObject("data");
                            firstName = jsonObject.optString("first_name");
                            lastName = jsonObject.optString("last_name");
                            email = jsonObject.optString("email");
                            mobile = jsonObject.optString("mobile");
                            profileImage = jsonObject.optString("profile_image");
                            address = jsonObject.optString("address");
                            zipCode = jsonObject.optString("zip_code");


                            firstNameTextView.setText(firstName);
                            lastNameTextView.setText(lastName);
                            emailTextView.setText(email);
                            addressTextView.setText(address);
                            zipCodeTextView.setText(zipCode);
                            mobileTextView.setText(mobile);


                            if (!profileImage.equals("")) {
                                Glide.with(getActivity()).load(profileImage)
                                        .thumbnail(0.5f)
                                        .placeholder(R.mipmap.defult_user).dontAnimate()
                                        .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                                        .listener(new RequestListener<String, GlideDrawable>() {
                                            @Override
                                            public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                                                return false;
                                            }

                                            @Override
                                            public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                                                progressBar.setVisibility(View.GONE);
                                                return false;
                                            }
                                        }).into(userImageView);
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
                } catch (Exception e) {
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
        } else if (i == R.id.txtEditDetail) {
            Intent intent = new Intent(getActivity(), UpdateProfileActivity.class);
            intent.putExtra("COME_FROM", "profileFragment");
            intent.putExtra("first_name", firstName);
            intent.putExtra("last_name", lastName);
            intent.putExtra("email", email);
            intent.putExtra("profile_image", profileImage);
            intent.putExtra("mobile", mobile);
            intent.putExtra("zip_code", zipCode);
            intent.putExtra("address", address);
            startActivityForResult(intent, 201);
        }
    }
}
