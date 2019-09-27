package com.fancustomer.fragment;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

import android.widget.ImageView;
import android.widget.ProgressBar;
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


public class FaqFragment extends Fragment implements View.OnClickListener {

    private View view;
    ProgressBar progressBar1 = null;
    private WebView webView;
    ViewGroup viewGroup;
    MenuScreen menuActivity;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_faq, container, false);
        viewGroup = container;
        setToolBar();
        intView();
        if (Constants.isInternetOn(getActivity())) {
            getFAQ();
        } else {
            menuActivity.showSnackbar(viewGroup, getResources().getString(R.string.no_internet), Constants.RETRY);

        }


        return view;
    }

    private void intView() {
        progressBar1 = (ProgressBar) view.findViewById(R.id.progressBar1);
        webView = (WebView) view.findViewById(R.id.webView);
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
        headerTextView.setText(getActivity().getResources().getString(R.string.faq));
        toolBarLeft.setOnClickListener(this);
    }

    public void getFAQ() {


        Api api = ApiFactory.getClientWithoutHeader(getActivity()).create(Api.class);
        Call<ResponseBody> call;
        call = api.getFaq();
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
                            String url = jsonObject.optString("url");
                            progressBar1.setMax(100);
                            webView.setWebChromeClient(new MyWebViewClient());
                            webView.getSettings().setJavaScriptEnabled(true);
                            webView.loadUrl(url);
                            progressBar1.setProgress(0);

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


    private class MyWebViewClient extends WebChromeClient {
        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            if (newProgress >= 100) {
                progressBar1.setVisibility(View.GONE);
            }
            setValue(newProgress);
            super.onProgressChanged(view, newProgress);
        }
    }

    public void setValue(int progress) {
        progressBar1.setProgress(progress);
    }

    @Override
    public void onClick(View view) {
        int i = view.getId();
        if (i == R.id.toolBarLeft) {
            MenuScreen.resideMenu.openMenu(ResideMenu.DIRECTION_LEFT);

        }
    }


}

