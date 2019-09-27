package com.fancustomer.helper;

/**
 * Created by Ghanshyam on 2/10/2017.
 */
public class ExceptionHandler extends Exception {
    public static void printStackTrace(Exception e) {
        if (e != null)
            e.printStackTrace();
    }

    @Override
    public StackTraceElement[] getStackTrace() {
        return super.getStackTrace();
    }
}
