package com.fanphotographer.utility;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.Transformation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.fanphotographer.R;

import java.util.List;

import static java.security.AccessController.getContext;

/**
 * Created by Ghanshyam on 01/21/2017.
 */
public class DialogUtils {



    public static void showOkDialogBox(Context mcontext, String msg){

        try {
            final Dialog dialog = new Dialog(mcontext, R.style.DialogCustomTheme);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.dialog_alert_type1);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            int width = ((Activity)mcontext).getWindowManager().getDefaultDisplay().getWidth();
            width = width - 100;
            dialog.getWindow().setLayout(width, ActionBar.LayoutParams.WRAP_CONTENT);
            dialog.setCancelable(true);
            TextView tv_message = (TextView) dialog.findViewById(R.id.tv_message);
            tv_message.setText(msg);
            final TextView tv_ok = (TextView) dialog.findViewById(R.id.tv_ok);
            tv_ok.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    dialog.dismiss();
                }
            });
            dialog.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static Dialog showOkDialogBox(Context mcontext, String msg, final AlertMessageListener listener){

        try {
            final Dialog dialog = new Dialog(mcontext, R.style.alert_dialog);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            // dialog.setTitle("Edit Profile!");
            dialog.setContentView(R.layout.dialog_alert_type1);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            int width = ((Activity)mcontext).getWindowManager().getDefaultDisplay().getWidth();
            if(width > 480)
             width = width - 140;
            else
                width = width - 100;
            dialog.getWindow().setLayout(width, ActionBar.LayoutParams.WRAP_CONTENT);
            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);
            TextView tv_message = (TextView) dialog.findViewById(R.id.tv_message);
            tv_message.setText(msg);
            final TextView tv_ok = (TextView) dialog.findViewById(R.id.tv_ok);
            tv_ok.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                    listener.onClickOk();
                }
            });
            dialog.show();
            return dialog;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }


    public static Dialog SweetAlertDialog(Context mcontext, final AlertMessageListener listener){

        try {
            final Dialog dialog = new Dialog(mcontext, R.style.alert_dialog);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            // dialog.setTitle("Edit Profile!");
            dialog.setContentView(R.layout.internet_popup);
            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);
            FrameLayout mErrorFrame = (FrameLayout)dialog.findViewById(R.id.error_frame);
            ImageView mErrorX = (ImageView)mErrorFrame.findViewById(R.id.error_x);
            final Button tv_ok = (Button) dialog.findViewById(R.id.confirm_button);
            Animation mErrorInAnim = OptAnimationLoader.loadAnimation(mcontext, R.anim.error_frame_in);
            AnimationSet mErrorXInAnim = (AnimationSet)OptAnimationLoader.loadAnimation(mcontext, R.anim.error_x_in);
            mErrorFrame.startAnimation(mErrorInAnim);
             mErrorX.startAnimation(mErrorXInAnim);

            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.GINGERBREAD_MR1) {
                List<Animation> childAnims = mErrorXInAnim.getAnimations();
                int idx = 0;
                for (;idx < childAnims.size();idx++) {
                    if (childAnims.get(idx) instanceof AlphaAnimation) {
                        break;
                    }
                }
                if (idx < childAnims.size()) {
                    childAnims.remove(idx);
                }
            }
//            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
//            int width = ((Activity)mcontext).getWindowManager().getDefaultDisplay().getWidth();
//            if(width > 480)
//             width = width - 140;
//            else
//                width = width - 100;
//            dialog.getWindow().setLayout(width, ActionBar.LayoutParams.WRAP_CONTENT);


            tv_ok.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                    listener.onClickOk();
                }
            });
            dialog.show();
            return dialog;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }



    public static Dialog showConfirmationDialog(Context context, String msg, final ConfirmationDialogListener listener) {

        try {

            final Dialog dialog = new Dialog(context);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.dialog_confirm_type1);
            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);
            int width = ((Activity)context).getWindowManager().getDefaultDisplay().getWidth();
            TextView tv_message = (TextView) dialog.findViewById(R.id.tv_message);
            tv_message.setText(msg);
            final TextView tv_ok = (TextView) dialog.findViewById(R.id.tv_ok);
            final TextView tv_cancel = (TextView) dialog.findViewById(R.id.tv_cancel);
            tv_ok.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    dialog.dismiss();
                    listener.onConfirmed();
                }
            });

            tv_cancel.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                    listener.onCanceled();

                }
            });

            dialog.show();
            return  dialog;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }






    public interface AlertMessageListener {
        public void onClickOk();
    }

    public interface ConfirmationDialogListener {
        public void onConfirmed();
        public void onCanceled();
    }
}