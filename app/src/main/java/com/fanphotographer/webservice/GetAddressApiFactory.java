package com.fanphotographer.webservice;


import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Api Factory for REST Client Retrofit
 */
public class GetAddressApiFactory {

    private static Retrofit address_retrofit = null;
    private static OkHttpClient.Builder httpClient = new OkHttpClient.Builder();

    public static Retrofit getClient() {

        if (address_retrofit == null) {

            HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
            interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            OkHttpClient client = httpClient.addInterceptor(interceptor).build();

            address_retrofit = new Retrofit.Builder()
                    .baseUrl("https://maps.googleapis.com/maps/api/geocode/")
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return address_retrofit;
    }

}
