package com.fanphotographer.utility;

import android.support.annotation.StringRes;
import com.fanphotographer.AppController;
import com.fanphotographer.R;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import okhttp3.ResponseBody;
import retrofit2.Response;

/**
 * Created by Ghanshyam on 2/10/2017.
 */
public class ErrorUtils {


    public static String getString(@StringRes int stringId) {
        return AppController.getInstance().getString(stringId);
    }

    public static String getHtttpCodeError(int statusCode) {

        String error = "Error";

        switch (statusCode) {

            case 400:
                error = getString(R.string.https_400_error);
                break;
            case 401:
                error = getString(R.string.http_401_error);
                break;
            case 403:
                error = getString(R.string.http_403_error);
                break;
            case 404:
                error = getString(R.string.http_404_error);
                break;
            case 500:
                error = getString(R.string.http_500_error);
                break;
    }
        return error;
    }

    public static String checkJosnErrorBody(JSONObject jobject){
        try {
            if (jobject.has("error")) {
                Object error = jobject.get("error");
                return ErrorUtils.getJsonErrorBody(error,jobject);
            } else {
                return jobject.optString("message");
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return "";
    }


    public static String getResponseBody(Response<ResponseBody> response) {

        BufferedReader reader = null;
        String output = null;
        try {

            if (response.body() != null) {
                reader = new BufferedReader(new InputStreamReader(response.body().byteStream()));
            } else if (response.errorBody() != null) {
                reader = new BufferedReader(new InputStreamReader(response.errorBody().byteStream()));
            }
            output = reader.readLine();
        }
         catch (Exception e) {
           e.printStackTrace();
        }

        return output;
    }

    public static String getJsonErrorBody(Object error,JSONObject jobject) {
        try {

            if (error instanceof JSONObject) {
                String message = "";
                String name = "";
                String field = "";
                JSONObject errObj = (JSONObject) error;
                if(errObj.has("field"))
                    field = errObj.optString("field");

                if(errObj.has("message")) {
                    Object msgObj = errObj.get("message");
                    if (msgObj instanceof JSONObject) {
                        name = ((JSONObject) msgObj).optString("name");
                        message = ((JSONObject) msgObj).optString("message");
                    } else {
                        message = errObj.optString("message");
                    }
                }else{
                    try{
                        message = jobject.optString("message");
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
                return field + "\n" + name + "\n" + message;

            } else if (error instanceof JSONArray) {

                try {
                    JSONArray errArray = (JSONArray) error;
                    return errArray.getJSONObject(0).optString("message");
                }catch (Exception e){
                    e.printStackTrace();
                }
            }

        }catch (Exception je) {
            je.printStackTrace();
        }
        return "";
    }


    public static String getJsonErrorBody(Object error) {

        try {

            if (error instanceof JSONObject) {
                String message = "";
                String name = "";
                JSONObject errObj = (JSONObject) error;
                String field = errObj.optString("field");

                Object msgObj = errObj.get("message");
                if (msgObj instanceof JSONObject) {
                    name = ((JSONObject) msgObj).optString("name");
                    message = ((JSONObject) msgObj).optString("message");
                } else {
                    message = errObj.optString("message");
                }

                return field + "\n" + name + "\n" + message;

            } else if (error instanceof JSONArray) {

                JSONArray errArray = (JSONArray) error;
                return errArray.getJSONObject(0).optString("message");
            }

        } catch (Exception je) {
            je.printStackTrace();
        }
        return "";
    }

}
