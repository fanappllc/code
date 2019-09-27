package com.fancustomer.helper;

import android.content.Context;
import android.support.annotation.StringRes;
import android.util.Log;

import com.fancustomer.AppController;
import com.fancustomer.R;
import com.fancustomer.data.constant.Constants;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import okhttp3.ResponseBody;
import retrofit2.Response;

/**
 * Created by User on 2/10/2017.
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
            case 422:
                error = getString(R.string.http_422_error);
                break;
            default:
                break;
        }
        return error;
    }

    public static String checkJosnErrorBody(JSONObject jobject) {
        try {
            if (jobject.has(Constants.ERROR)) {
                Object error = jobject.get(Constants.ERROR);
                return ErrorUtils.getJsonErrorBody(error, jobject);
            } else {
                return jobject.optString(Constants.MESSAGE).trim();
            }
        } catch (Exception e) {
            ExceptionHandler.printStackTrace(e);
        }
        return "";
    }

    public static void showFalseMessage(JSONObject object, Context mcontext) {
        try {
            JSONArray jsonArray = object.optJSONArray(Constants.ERROR);
            if (jsonArray == null) {
                Constants.showToastAlert(object.getJSONObject(Constants.ERROR).getString(Constants.MESSAGE), mcontext);
            } else {
                Constants.showToastAlert(jsonArray.getJSONObject(0).getString(Constants.MESSAGE), mcontext);
            }
        } catch (Exception e) {
            Log.e(Constants.LOG_CAT, "onResponse:", e);
        }
    }

    //  public static String getResponseBody(Response<ResponseBody> response) {
//
//        BufferedReader reader;
//        String output = null;
//        try {
//            reader = new BufferedReader(new InputStreamReader(response.body().byteStream()));
//            StringBuilder sb = new StringBuilder();
//            String line = null;
//            while ((line = reader.readLine()) != null) {
//                sb.append(line + "\n");
//            }
//            output = sb.toString();
//            Log.e(Constants.LOG_CAT, "API Stripe key=====???????" + output);
//        } catch (Exception e) {
//            ExceptionHandler.printStackTrace(e);
//        }
//
//        return output;
//    }
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
        } catch (Exception e) {
            e.printStackTrace();
        }

        return output;
    }

    public static String getJsonErrorBody(Object error, JSONObject jobject) {
        try {

            if (error instanceof JSONObject) {
                String message = "";
                String field = "";
                JSONObject errObj = (JSONObject) error;
                if (errObj.has("field"))
                    field = errObj.optString("field");
                if (errObj.has("message")) {
                    Object msgObj = errObj.get(Constants.MESSAGE);
                    if (msgObj instanceof JSONObject) {
                        message = ((JSONObject) msgObj).optString(Constants.MESSAGE).trim();
                        if (message.contains("]")) {
                            int newStr = message.lastIndexOf("]");
                            message = message.substring(newStr + 1);
                        }

                    } else {
                        message = errObj.optString(Constants.MESSAGE).trim();
                        if (message.contains("]")) {
                            int newStr = message.lastIndexOf("]");
                            message = message.substring(newStr + 1);
                        }
                    }
                } else {
                    try {
                        message = jobject.optString(Constants.MESSAGE).trim();
                        if (message.contains("]")) {
                            int newStr = message.lastIndexOf("]");
                            message = message.substring(newStr + 1);
                        }
                    } catch (Exception e) {
                        ExceptionHandler.printStackTrace(e);
                    }
                }
                return message;

            } else if (error instanceof JSONArray) {
                try {
                    JSONArray errArray = (JSONArray) error;
                    return errArray.getJSONObject(0).optString(Constants.MESSAGE).trim();
                } catch (Exception e) {
                    ExceptionHandler.printStackTrace(e);
                }
            }

        } catch (Exception e) {
            ExceptionHandler.printStackTrace(e);
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

                Object msgObj = errObj.get(Constants.MESSAGE);
                if (msgObj instanceof JSONObject) {
                    name = ((JSONObject) msgObj).optString("name");
                    message = ((JSONObject) msgObj).optString(Constants.MESSAGE);
                } else {
                    message = errObj.optString(Constants.MESSAGE);
                }

                return field + "\n" + name + "\n" + message;

            } else if (error instanceof JSONArray) {

                JSONArray errArray = (JSONArray) error;
                return errArray.getJSONObject(0).optString(Constants.MESSAGE);
            }

        } catch (Exception je) {
            ExceptionHandler.printStackTrace(je);
        }
        return "";
    }

}
