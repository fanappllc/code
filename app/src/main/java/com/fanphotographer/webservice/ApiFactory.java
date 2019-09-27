package com.fanphotographer.webservice;


import android.content.Context;
import android.content.SharedPreferences;
import com.fanphotographer.data.constant.Constants;
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

public class ApiFactory {

    public static String BASE_URL = Constants.BASE_URL;
    private static Retrofit retrofit = null;
    private static Retrofit retrofitWithHeader = null;
    private static SharedPreferences preferences;
    private static String access_token = "";

    private static OkHttpClient.Builder httpClient = new OkHttpClient.Builder();

    public static Retrofit getClientWithoutHeader(Context context) {

        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = httpClient.addInterceptor(interceptor).connectTimeout(5, TimeUnit.MINUTES).
                readTimeout(5, TimeUnit.MINUTES).
                writeTimeout(5, TimeUnit.MINUTES)
                .build();
        if (retrofit == null) {

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    public static Retrofit getClient(Context context) {

        preferences = context.getSharedPreferences(Constants.APP_PREF_NAME, Context.MODE_PRIVATE);
        access_token = preferences.getString(Constants.ACCESS_TOKEN, "");

        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        httpClient.addInterceptor(new Interceptor() {
            @Override public okhttp3.Response intercept(Chain chain) throws IOException {
                Request original = chain.request();
                Request.Builder requestBuilder = original.newBuilder().addHeader("access-token", access_token);  // Request customization: add request headers
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
