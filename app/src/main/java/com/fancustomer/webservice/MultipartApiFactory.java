package com.fancustomer.webservice;




import com.fancustomer.data.constant.Constants;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Api Factory for REST Client Retrofit
 */
public class MultipartApiFactory {

    public static String BASE_URL = Constants.BASE_URL;


    public static Retrofit getRetrofit() {

        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();

        interceptor.setLevel((HttpLoggingInterceptor.Level.BODY));


        OkHttpClient client = new OkHttpClient.Builder().
                connectTimeout(5, TimeUnit.MINUTES).
                readTimeout(5, TimeUnit.MINUTES).
                writeTimeout(5, TimeUnit.MINUTES).
                addInterceptor(interceptor).build();


        return new Retrofit.Builder()
                .baseUrl(MultipartApiFactory.BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();


    }
}
