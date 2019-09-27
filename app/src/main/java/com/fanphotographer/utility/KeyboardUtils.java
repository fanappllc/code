package com.fanphotographer.utility;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.fanphotographer.AppController;

public class KeyboardUtils {

    /**
     * this method shows the keyboard on corresponding edittext
     * @param editText
     */
    public static void showKeyboard(EditText editText) {
        InputMethodManager imm = (InputMethodManager) AppController.getInstance().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
    }

    /**
     * this method hides the keyboard on corresponding edittext
     * @param editText
     */
    public static void hideKeyboard(EditText editText) {
        try {
            InputMethodManager imm = (InputMethodManager) AppController.getInstance().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * this method hides the keyboard on current view
     *
     * @param activity
     */
    public static void hideKeyboard(Activity activity) {

        try {
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
            //Find the currently focused view, so we can grab the correct window token from it.
            View view = activity.getCurrentFocus();
            //If no view currently has focus, create a new one, just so we can grab a window token from it
            if (view == null) {

                view = new View(activity);

            }
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

        }catch (Exception e){
            e.printStackTrace();
        }
    }


    public static boolean isActiveKeyboard(Activity activity){
        return ((InputMethodManager)activity.getSystemService(Context.INPUT_METHOD_SERVICE)).isActive();
    }

    /**
     * this method hides the keyboard on view
     * @param activity
     */
    public static void hideKeyboardOnview(Activity activity){

        try {

            activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
