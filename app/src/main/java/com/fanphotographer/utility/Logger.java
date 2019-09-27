package com.fanphotographer.utility;

import android.util.Log;

/**
 * Created by Ghanshyam on 2/10/2017.
 */
public class Logger {

    public static void d(String tag,String msg){
        Log.d(tag,msg);
    }

    public static void i(String tag,String msg){
        Log.i(tag,msg);
    }

    public static void w(String tag,String msg){
        Log.w(tag,msg);
    }

    public static void e(String tag,String msg){
        Log.e(tag,msg);
    }

    public static void v(String tag,String msg){
        Log.v(tag,msg);
    }
}
