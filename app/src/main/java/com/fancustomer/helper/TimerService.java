package com.fancustomer.helper;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.fancustomer.data.constant.Constants;
import com.fancustomer.data.preference.AppPreference;

import java.util.Timer;
import java.util.TimerTask;


public class TimerService extends Service {
    public static Timer timerService;
    long status = 0;
    private long time = 0;
    private long wtime = 0;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        wtime = AppPreference.getInstance(TimerService.this).getLongValue("timer_key");
        status = (wtime * 1);
        time = status * 1000;

        timerService = new Timer();
        timerService.schedule(new TimerTask() {
            @Override
            public void run() {
                try {

                    try {
                        status = status - 1;
                        time = time - 1000;

                    } catch (Exception e) {
                        ExceptionHandler.printStackTrace(e);
                    }

                    if (time == 0 || time < 0) {
                        timerService.cancel();


                    }

                } catch (Exception e) {
                    timerService.cancel();
                }

                AppPreference.getInstance(getApplicationContext()).setLongValue("timer_key", time / 1000);
                AppPreference.getInstance(getApplicationContext()).setLongValue("status_key", status);
                Log.e(Constants.LOG_CAT, "Service=time=============>>>>>>>>>>>>" + time);
                Log.e(Constants.LOG_CAT, "Service=status=============>>>>>>>>>>>>" + status);
            }
        }, 0, 1000);
        Notification notification = new Notification();
        startForeground(1, notification);

        //return super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }


    public void canceltimer() {
        try {
            if (timerService != null) {
                timerService.cancel();
            }
        } catch (Exception e) {
            ExceptionHandler.printStackTrace(e);
        }
    }
}
