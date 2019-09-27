package com.fanphotographer.helper;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.fanphotographer.AppController;
import com.fanphotographer.data.constant.Constants;
import com.fanphotographer.data.preference.AppPreference;

import java.util.Timer;
import java.util.TimerTask;


public class TimerService extends Service {
    public static Timer stimer;
    long status = 0;
    private  long time = 0;
    private long wtime = 0;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        wtime = AppPreference.getInstance(TimerService.this).getLongValue("timer_key");
        Log.e(Constants.LOG_CAT, "Service=STOPPPPPPPPPPPPPPP==>>>>>>>>>>>>8" );
        status = (wtime*1);
        time = status * 1000;

        stimer = new Timer();
        stimer.schedule(new TimerTask() {
            @Override
            public void run() {

                if(stimer!=null) {
                    try {
                        status = status - 1;
                        time = time - 1000;
                        if (time == 0 || time < 0) {
                            stimer.cancel();
                            stimer = null;
                        }

                    } catch (Exception e) {
                        stimer.cancel();

                    }

                    AppPreference.getInstance(getApplicationContext()).setLongValue("timer_key", time / 1000);
                    AppPreference.getInstance(getApplicationContext()).setLongValue("status_key", status);
                    Log.e(Constants.LOG_CAT, "Service=time=============>>>>>>>>>>>>" + time);
//                    Log.e(Constants.LOG_CAT, "Service=status=============>>>>>>>>>>>>" + status);


                }
            }
        }, 0, 1000);

        Notification notification = new Notification();
        startForeground(1, notification);

        return START_STICKY;
    }

    public static void canceltimer() {
        try {
            if (stimer != null) {
                stimer.cancel();
                stimer = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



}