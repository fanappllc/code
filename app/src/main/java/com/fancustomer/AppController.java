package com.fancustomer;

import android.app.Application;
import android.util.Log;

import com.fancustomer.data.preference.AppPreference;

import org.json.JSONObject;

import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.SSLContext;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

/**
 * Created by Codiant on 13-Nav-2017.
 */

public class AppController extends Application {
    /*CODIANT*/
    //  private static final String SOCKET_URL = "http://prortc.com:6018";

    /*LIVE*/
    private static final String SOCKET_URL = "http://fanapp.us:6018";


    private static final String LOG_SOCKET = "AppController";
    public Socket mSocket;
    private String orderID = "";
    static AppController instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        initCalligraphy();

    }

    public static synchronized AppController getInstance() {
        return instance;
    }

    public interface TrackLocationListener {
        public void onLocationGet(double latitude, double longitude);

        public void onConnected();
    }

    public void publicSocket(final TrackLocationListener listener) {
        try {

            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, null, null);
            IO.Options opts = new IO.Options();
            IO.setDefaultSSLContext(sc);
            opts.forceNew = false;
            opts.secure = true;
            opts.reconnection = true;
            mSocket = IO.socket(SOCKET_URL, opts);
            mSocket.connect();
            mSocket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    orderID = AppPreference.getInstance(AppController.this).getString("orderID");
                    try {
//                        Log.d(LOG_SOCKET, "socket connected " + +"  port = ");
                        Log.d(LOG_SOCKET, "socket connected");
                        JSONObject json = new JSONObject();
                        json.put("orderId", orderID);
                        mSocket.emit("start_tracking", json);
                        listener.onConnected();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    try {
                        Log.d(LOG_SOCKET, "socket disconnected");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).on(Socket.EVENT_ERROR, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    try {
                        Log.d(LOG_SOCKET, "socket error");
                        //  publicSocket();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            }).on(Socket.EVENT_CONNECT_TIMEOUT, new Emitter.Listener() {

                @Override
                public void call(Object... args) {
                    try {
                        Log.d(LOG_SOCKET, "socket connect time out error");
                        //  publicSocket();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            }).on("new_location", new Emitter.Listener() {

                @Override
                public void call(Object... args) {

                    try {

                        Log.d(LOG_SOCKET, "socket update location " + args.toString());
                        // publicSocket();
                        JSONObject object = new JSONObject(args[0].toString());
                        Log.d(LOG_SOCKET, "socket update location " + object.toString());
                        double latitude = object.optDouble("latitude");
                        double longitude = object.optDouble("longitude");
//                        float bearing = (float) object.optDouble("bearing");
                        listener.onLocationGet(latitude, longitude);


                    } catch (Exception e) {
                        e.printStackTrace();

                    }
                }
            });

        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }
    }

    private void initCalligraphy() {
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/JosefinSans-Regular_2.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );
    }


//    public void updateLocation(Location location, TrackLocationListener tackingStatus) {
//        try {
//            if (mSocket != null) {
//                if (mSocket.connected() == true) {
//                    JSONObject json = new JSONObject();
//                    json.put("latitude", location.getLatitude());
//                    json.put("longitude", location.getLongitude());
//                    json.put("userID", "");
//                    mSocket.emit("update-location", json);
//                    Log.e(Constants.LOG_CAT, "updateLocation: " + location.getLatitude() + "  " + location.getLongitude());
//                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//
//        }
//    }
}
