package com.fancustomer.helper;

import android.app.Dialog;
import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.TextView;

import com.fancustomer.R;
import com.fancustomer.data.constant.Constants;


public class CustomProgressDialog extends Dialog {
    public CustomProgressDialog(Context context) {
        super(context);
    }

    public CustomProgressDialog(Context context, int theme) {
        super(context, theme);
    }


    public void onWindowFocusChanged(boolean hasFocus) {
        Log.e(Constants.LOG_CAT, "onWindowFocusChanged: ");
    }

    public void setMessage(CharSequence message) {
        if (message != null && message.length() > 0) {
            TextView txt = findViewById(R.id.textOfLoader);
            txt.setText(message);
            txt.invalidate();
        }
    }

    public static CustomProgressDialog show(Context context, boolean cancelable, String message) {

        CustomProgressDialog dialog = new CustomProgressDialog(context, R.style.CustomProgressDialog);
        dialog.setTitle("");
        dialog.setContentView(R.layout.custom_progress_dialog);
        dialog.setCancelable(cancelable);
        dialog.setCanceledOnTouchOutside(cancelable);
        TextView textOfLoader = dialog.findViewById(R.id.textOfLoader);
        textOfLoader.setText(message);
        dialog.getWindow().getAttributes().gravity = Gravity.CENTER;
        WindowManager.LayoutParams lp = dialog.getWindow().getAttributes();
        lp.dimAmount = 0.4f;
        dialog.getWindow().setAttributes(lp);
        return dialog;
    }
}
