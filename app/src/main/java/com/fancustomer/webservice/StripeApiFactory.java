package com.fancustomer.webservice;


import android.content.Context;
import android.content.SharedPreferences;


import com.fancustomer.activity.AddCreditCardActivity;
import com.fancustomer.data.constant.Constants;
import com.fancustomer.data.preference.AppPreference;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Api Factory for REST Client Retrofit
 */
public class StripeApiFactory {

    public static String BASE_URL = Constants.STRIPE_BASE_URL;
    private static Retrofit retrofit = null;
    private static Retrofit retrofitWithHeader = null;
    private static SharedPreferences preferences;
    private static OkHttpClient.Builder httpClient = new OkHttpClient.Builder();

    public static Retrofit getClient(final Context context) {

        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        httpClient.addInterceptor(new Interceptor() {
            @Override
            public okhttp3.Response intercept(Chain chain) throws IOException {
                Request original = chain.request();

                // Request customization: add request headers
                Request.Builder requestBuilder = original.newBuilder()
                        .addHeader("Authorization", "Bearer " + AppPreference.getInstance(context).getString(Constants.STRIPE_PUBLISH_KEY));
//                        .addHeader("Content-type", "application/x-www-form-urlencoded");
                Request request = requestBuilder.build();
                return chain.proceed(request);
            }
        });

        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = httpClient.addInterceptor(interceptor).connectTimeout(5, TimeUnit.MINUTES).
                readTimeout(5, TimeUnit.MINUTES).
                writeTimeout(5, TimeUnit.MINUTES).build();

        if (retrofitWithHeader == null) {
            retrofitWithHeader = new Retrofit.Builder().baseUrl(BASE_URL).client(client).addConverterFactory(GsonConverterFactory.create()).build();
        }
        return retrofitWithHeader;
    }

}
