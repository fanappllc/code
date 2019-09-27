package com.fanphotographer.fragment;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
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
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.fanphotographer.R;
import com.fanphotographer.activity.BillDetailActivity;
import com.fanphotographer.activity.MenuScreen;
import com.fanphotographer.activity.UpdateProfileActivity;
import com.fanphotographer.bean.GetUserBean;
import com.fanphotographer.data.constant.Constants;
import com.fanphotographer.utility.ActivityUtils;
import com.fanphotographer.utility.AppUtils;
import com.fanphotographer.utility.ErrorUtils;
import com.fanphotographer.webservice.Api;
import com.fanphotographer.webservice.ApiFactory;
import com.special.ResideMenu.ResideMenu;
import org.json.JSONArray;
import org.json.JSONObject;
import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class ProfileFragment extends Fragment implements View.OnClickListener {


    private View view;
    private TextView txtEditDetail;
    private Context mContext;
    private CircleImageView userImageView;
    private TextView firstNameTextView;
    private TextView lastNameTextView;
    private TextView emailTextView;
    private TextView addressTextView;
    private TextView zipCodeTextView;
    private TextView mobileTextView;
    private TextView mobileModalTextView;
    private ProgressBar progressBar;
    private MenuScreen act;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_profile, container, false);
        mContext = getActivity();
        act = (MenuScreen) mContext;
        setToolBar();
        intView();
        setClicks();
        return view;
    }

    private void setToolBar() {

        RelativeLayout toolBarLeft = (RelativeLayout) view.findViewById(R.id.toolBarLeft);
        ImageView imageViewMenu = (ImageView) view.findViewById(R.id.imageView_menu);
        TextView headerTextView = (TextView) view.findViewById(R.id.hedertextview);
        ImageView toolbarbackpress = (ImageView) view.findViewById(R.id.toolbarbackpress);
        toolbarbackpress.setVisibility(View.GONE);
        imageViewMenu.setVisibility(View.VISIBLE);


        headerTextView.setText(getActivity().getResources().getString(R.string.profile_View));
        toolBarLeft.setOnClickListener(this);
    }

    private void intView() {

        userImageView = (CircleImageView) view.findViewById(R.id.userImageView);
        progressBar = (ProgressBar) view.findViewById(R.id.progress_bar);
        firstNameTextView = (TextView) view.findViewById(R.id.firstNameTextView);
        lastNameTextView = (TextView) view.findViewById(R.id.lastNameTextView);
        emailTextView = (TextView) view.findViewById(R.id.emailTextView);
        addressTextView = (TextView) view.findViewById(R.id.addressTextView);
        zipCodeTextView = (TextView) view.findViewById(R.id.zipCodeTextView);
        mobileTextView = (TextView) view.findViewById(R.id.mobileTextView);
        mobileModalTextView = (TextView) view.findViewById(R.id.mobileModalTextView);
        txtEditDetail = (TextView) view.findViewById(R.id.txtEditDetail);

    }

    private void setClicks() {
        txtEditDetail.setOnClickListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (AppUtils.isNetworkConnected()) {
            getUserInformationApi();
        } else {
            Constants.showToastAlert(getResources().getString(R.string.no_internet), mContext);
        }

    }

    @Override
    public void onClick(View view) {
        int i = view.getId();
        if (i == R.id.toolBarLeft) {
            MenuScreen.resideMenu.openMenu(ResideMenu.DIRECTION_LEFT);

        } else if (i == R.id.txtEditDetail) {
            ActivityUtils.getInstance().invokeActivity(getActivity(), UpdateProfileActivity.class, false);

        }

    }

    public void getUserInformationApi()  {

        Api api = ApiFactory.getClient(mContext).create(Api.class);
        Call<ResponseBody> call;
        call = api.getuser();
        Log.e(Constants.LOG_CAT, "HEADERS : " + call.request().headers());


        Constants.showProgressDialog(mContext, "Loading..");
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
                            GetUserBean getUserBean = act.appPreference.parseData(jsonObject);

                            firstNameTextView.setText(getUserBean.first_name);
                            lastNameTextView.setText(getUserBean.last_name);
                            emailTextView.setText(getUserBean.email);
                            addressTextView.setText(getUserBean.address);
                            zipCodeTextView.setText(getUserBean.zip_code);
                            mobileTextView.setText(getUserBean.country_code +" "+getUserBean.mobile);
                            mobileModalTextView.setText(act.appPreference.getString(Constants.MOBILE_MODEL));


                            if (!Constants.isStringNullOrBlank(getUserBean.profile_image)) {


                                Glide.with(getActivity()).load(getUserBean.profile_image)
                                        .thumbnail(0.5f)
                                        .apply(new RequestOptions().placeholder(R.mipmap.defult_user).dontAnimate())
                                        .apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL)).listener(new RequestListener<Drawable>() {
                                    @Override
                                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                        return false;
                                    }

                                    @Override
                                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                        progressBar.setVisibility(View.GONE);
                                        return false;
                                    }
                                })
                                        .into(userImageView);



//                                Glide.with(getActivity()).load(getUserBean.profile_image)
//                                        .thumbnail(0.5f)
//                                        .placeholder(R.mipmap.defult_user).dontAnimate()
//                                        .diskCacheStrategy(DiskCacheStrategy.SOURCE)
//                                        .listener(new RequestListener<String, GlideDrawable>() {
//                                            @Override
//                                            public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
//                                                return false;
//                                            }
//
//                                            @Override
//                                            public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
//                                                progressBar.setVisibility(View.GONE);
//                                                return false;
//                                            }
//                                        }).into(userImageView);
                            }

                        } else if (object.optString(Constants.SUCCESS).equalsIgnoreCase(Constants.FALSE)) {
                            JSONArray jsonArray = object.optJSONArray(Constants.ERROR);
                            if (jsonArray != null) {
                                Constants.showToastAlert(object.getJSONObject(Constants.ERROR).getString(Constants.MESSAGE), getActivity());
                            }
                        }

                    }  else if (response != null) {
                        if (response.code() == 400 || response.code() == 500 || response.code() == 403 || response.code() == 404 || response.code() == 401) {
                            if(response.code()==401){
                                act.appPreference.showCustomAlert(act,getResources().getString(R.string.http_401_error));
                            }else {
                                Constants.showToastAlert(ErrorUtils.getHtttpCodeError(response.code()), mContext);
                            }
                        } else {
                            String responseStr = ErrorUtils.getResponseBody(response);
                            JSONObject jsonObject = new JSONObject(responseStr);
                            Constants.showToastAlert(ErrorUtils.checkJosnErrorBody(jsonObject), mContext);
                        }
                    }
                }catch (Exception e){
                    Log.e(Constants.LOG_CAT,e.getMessage());
                }

            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Constants.hideProgressDialog();
                act.appPreference.showCustomAlert(act,ErrorUtils.getString(R.string.failled));
            }
        });
    }


}
