package com.fanphotographer.helper;


import android.util.Log;
import com.fanphotographer.R;
import com.fanphotographer.data.constant.Constants;
import com.fanphotographer.utility.ErrorUtils;
import com.fanphotographer.webservice.Api;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.Map;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserUpdateApi {

    public static final String TAG = UserUpdateApi.class.getSimpleName();
    ApiResponseListener apiResponseListener;
    Call<ResponseBody> call;
    Api api;


    public void uploadphoto( Api mapi, String accessToken, Map<String, RequestBody> map, RequestBody id, RequestBody slot_id,ApiResponseListener apiResponseListe) {

        api = mapi;
        this.apiResponseListener = apiResponseListe;
        call = api.uploadimage(accessToken, map, id, slot_id);
        Log.e(Constants.LOG_CAT, "API FAN Profile------------------->>>>>:" + map + " " + call.request().url());
        Log.e(Constants.LOG_CAT, "HEADERS : " + call.request().headers());
        Log.e(Constants.LOG_CAT, "onResponse--***----:startdoing" );
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    if (response != null && response.isSuccessful() && response.code() == 200) {
                        String output = ErrorUtils.getResponseBody(response);
                        JSONObject object = new JSONObject(output);
                        if (object.optString("success").equalsIgnoreCase("true")) {
                            Log.e(Constants.LOG_CAT, "onResponse--***----:" + object.toString());
                            apiResponseListener.onSuccess("Upload...");

                        } else if (object.optString("success").equalsIgnoreCase("false")) {
                            JSONArray jsonArray = object.optJSONArray("error");
                            if (jsonArray == null) {
                                apiResponseListener.onFaillure(object.getJSONObject("error").getString("message"));
                            } else {
                                apiResponseListener.onFaillure(jsonArray.getJSONObject(0).getString("message"));
                            }
                        }

                    } else if (response != null) {
                        if (response.code() == 400 || response.code() == 500 || response.code() == 403 || response.code() == 404 || response.code() == 401) {
                            Log.e(Constants.LOG_CAT, "onResponse--***----:" + ErrorUtils.getHtttpCodeError(response.code()));
                            apiResponseListener.onFaillure(ErrorUtils.getHtttpCodeError(response.code()));
                        } else {
                            String responseStr = ErrorUtils.getResponseBody(response);
                            JSONObject jsonObject = new JSONObject(responseStr);
                            apiResponseListener.onFaillure(ErrorUtils.checkJosnErrorBody(jsonObject));
                        }
                    }
                } catch (Exception e) {
                    Log.e(Constants.LOG_CAT,e.getMessage());
                }

            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Constants.hideProgressDialog();
                apiResponseListener.onFaillure(ErrorUtils.getString(R.string.failled));
            }
        });


    }


}
