package com.fanphotographer;

import android.app.Application;
import android.location.Location;
import android.net.Uri;
import android.util.Log;
import android.webkit.MimeTypeMap;
import com.fanphotographer.data.constant.Constants;
import com.fanphotographer.data.preference.AppPreference;
import com.fanphotographer.helper.ApiResponseListener;
import com.fanphotographer.helper.UserUpdateApi;
import com.fanphotographer.utility.AppUtils;
import com.fanphotographer.webservice.Api;
import com.fanphotographer.webservice.ApiFactory;
import org.json.JSONObject;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import javax.net.ssl.SSLContext;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;


public class AppController extends Application {


    private static final String SOCKET_URL = "http://prortc.com:6018";

    //Live
//    private static final String SOCKET_URL = "http://fanapp.us:6018";
    private static final String LOG_SOCKET = "AppController";
    private Socket mSocket;
    private String orderId = "";
    private static AppController instance;

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
                    orderId = AppPreference.getInstance(AppController.this).getString(Constants.ORDER_ID);
                    try {
                        Log.d(LOG_SOCKET, "socket connected");
                        JSONObject json = new JSONObject();
                        json.put("orderId", orderId);
                        mSocket.emit("start_tracking", json);
                        listener.onConnected();
                    } catch (Exception e) {
                        Log.e(Constants.LOG_CAT,e.getMessage());
                    }
                }
            }).on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    try {
                        Log.d(LOG_SOCKET, "socket disconnected");
                    } catch (Exception e) {
                        Log.e(Constants.LOG_CAT,e.getMessage());
                    }
                }
            }).on(Socket.EVENT_ERROR, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    try {
                        Log.d(LOG_SOCKET, "socket error");

                    } catch (Exception e) {
                        Log.e(Constants.LOG_CAT,e.getMessage());
                    }
                }

            }).on(Socket.EVENT_CONNECT_TIMEOUT, new Emitter.Listener() {

                @Override
                public void call(Object... args) {
                    try {
                        Log.d(LOG_SOCKET, "socket connect time out error");

                    } catch (Exception e) {
                        Log.e(Constants.LOG_CAT,e.getMessage());
                    }
                }

            }).on("new_location", new Emitter.Listener() {

                @Override
                public void call(Object... args) {

                    try {

                        Log.d(LOG_SOCKET, "socket update location " + args.toString());
                        JSONObject object = new JSONObject(args[0].toString());
                        Log.d(LOG_SOCKET, "socket update location " + object.toString());





                    } catch (Exception e) {
                        Log.e(Constants.LOG_CAT,e.getMessage());

                    }
                }
            });

        } catch (Exception e) {
            Log.e(Constants.LOG_CAT,e.getMessage());
        }
    }

    private void initCalligraphy() {
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/JosefinSans-Regular_2.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );
    }


    public void updateLocation(Location location, TrackLocationListener tackingStatus) {
        try {
            if (mSocket != null) {
                if (mSocket.connected() == true) {
                    JSONObject json = new JSONObject();
                    json.put("latitude", location.getLatitude());
                    json.put("longitude", location.getLongitude());
                    json.put("orderId", orderId);
                    json.put("bearing", location.getBearing());
                    mSocket.emit("update_location", json);
                    Log.e(Constants.LOG_CAT, "update_location: " + location.getLatitude() + "  " + location.getLongitude());
                }
            }
        } catch (Exception e) {
            Log.e(Constants.LOG_CAT,e.getMessage());

        }
    }


    public void upload(String accessToken, String orderId, String orderSlotid, final File file) {


        Api api = ApiFactory.getClientWithoutHeader(getApplicationContext()).create(Api.class);
        Map<String, RequestBody> map = new HashMap<>();
        RequestBody id = RequestBody.create(MediaType.parse("multipart/form-data"), orderId);
        RequestBody slot_id = RequestBody.create(MediaType.parse("multipart/form-data"), orderSlotid);

            Uri selectedUri = Uri.fromFile(file);
            String fileExtension = MimeTypeMap.getFileExtensionFromUrl(selectedUri.toString());
            String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension);
            RequestBody reqBodyImageOfUser = RequestBody.create(MediaType.parse(mimeType), file);
            map.put("photo\"; filename=\"" + file.getName() + "\"", reqBodyImageOfUser);

            Log.e(Constants.LOG_CAT, "FanEditProfile:" + map.toString());

        Log.e(Constants.LOG_CAT, "onResponse--***----:start" );
        new UserUpdateApi().uploadphoto(api,accessToken,map,id,slot_id, new ApiResponseListener<String>() {
            @Override
            public void onSuccess(String response) {
                Log.e(Constants.LOG_CAT, "onResponse:success" );
                Constants.showToastAlert(response, getApplicationContext());
//                if (file.exists()) {
//                     file.delete();
//                }
//                File directory = new File(ImageUtils.getRootDirPath());
//                File[] files = directory.listFiles();
//                if(file.length()==0){
//                    stopService(new Intent(AppController.this, PhotoService.class));
//                }
            }

            @Override
            public void onFaillure(String response) {
                Log.e(Constants.LOG_CAT, "onResponse--***----:error" );
                Constants.showToastAlert(response, getApplicationContext());

            }
        });
    }

}
