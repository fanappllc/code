package com.fanphotographer.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.fanphotographer.R;
import com.fanphotographer.activity.MenuScreen;
import com.fanphotographer.data.constant.Constants;
import com.fanphotographer.data.preference.AppPreference;
import com.fanphotographer.utility.AppUtils;
import com.fanphotographer.utility.ErrorUtils;
import com.fanphotographer.webservice.Api;
import com.fanphotographer.webservice.ApiFactory;
import com.special.ResideMenu.ResideMenu;
import org.json.JSONArray;
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


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if(view == null) {
            view = inflater.inflate(R.layout.fragment_contact, container, false);
            setToolBar();
            init();
            if (AppUtils.isNetworkConnected()) {
                getContactDetail();
            } else {
                final ViewGroup viewGroup = (ViewGroup) ((ViewGroup) getActivity().findViewById(android.R.id.content)).getChildAt(0);
                Constants.showSnackbar(getActivity(), viewGroup, getResources().getString(R.string.no_internet), "Retry");
            }
        }
        return view;
    }

    private void init() {
        showId = (TextView) view.findViewById(R.id.show_id);
        ImageView facebook = (ImageView) view.findViewById(R.id.facebook);
        ImageView twitter = (ImageView) view.findViewById(R.id.twitter);
        ImageView gmail = (ImageView) view.findViewById(R.id.gmail);
        ImageView linkedin = (ImageView) view.findViewById(R.id.linkedin);

        facebook.setOnClickListener(this);
        twitter.setOnClickListener(this);
        gmail.setOnClickListener(this);
        linkedin.setOnClickListener(this);
    }

    private void setToolBar() {

        RelativeLayout toolBarLeft = (RelativeLayout) view.findViewById(R.id.toolBarLeft);
        ImageView imageViewMenu = (ImageView) view.findViewById(R.id.imageView_menu);
        TextView headerTextView = (TextView) view.findViewById(R.id.hedertextview);
        ImageView toolbarbackpress = (ImageView) view.findViewById(R.id.toolbarbackpress);
        toolbarbackpress.setVisibility(View.GONE);
        imageViewMenu.setVisibility(View.VISIBLE);
        headerTextView.setText(getActivity().getResources().getString(R.string.contact_us));
        toolBarLeft.setOnClickListener(this);


    }


    @Override
    public void onClick(View view) {
        int i = view.getId();
        if (i == R.id.toolBarLeft) {
            MenuScreen.resideMenu.openMenu(ResideMenu.DIRECTION_LEFT);

        } else if (i == R.id.facebook) {
            openLink(faceUrl);

        } else if (i == R.id.twitter) {
            openLink(twiUrl);

        } else if (i == R.id.gmail) {
            openLink(googleUrl);

        } else if (i == R.id.linkedin) {
            openLink(linkUrl);

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
                        if (object.optString(Constants.SUCCESS).equalsIgnoreCase(Constants.TRUE)) {
                            JSONObject jsonObject = object.optJSONObject("data");
                            String adminEmail = jsonObject.optString("admin_email");
                            faceUrl = jsonObject.optString("facebook");
                            googleUrl = jsonObject.optString("google_plus");
                            twiUrl = jsonObject.optString("twitter");
                            linkUrl = jsonObject.optString("linkedin");
                            showId.setText(adminEmail);
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
                                AppPreference.getInstance(getActivity()).showCustomAlert(getActivity(), getResources().getString(R.string.http_401_error));
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


    public void openLink(String url){

        if(AppUtils.isNetworkConnected()){
            if(!Constants.isStringNullOrBlank(url)){
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(intent);
            }
        }else {
            final ViewGroup viewGroup = (ViewGroup) ((ViewGroup) getActivity().findViewById(android.R.id.content)).getChildAt(0);
            Constants.showSnackbar(getActivity(), viewGroup, getResources().getString(R.string.no_internet), "Retry");
        }


    }
}
