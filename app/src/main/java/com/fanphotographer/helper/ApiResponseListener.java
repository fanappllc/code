package com.fanphotographer.helper;

/**
 * Created by Ghanshyam on 01/27/2017.
 */
public interface ApiResponseListener<T>{
    public void onSuccess(T response);
    public void onFaillure(String response);
}
