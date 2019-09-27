package com.fanphotographer.activity;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.job.JobScheduler;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.SystemClock;
import android.support.design.widget.FloatingActionButton;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import com.fanphotographer.AppController;
import com.fanphotographer.R;
import com.fanphotographer.adapter.RadioAdapter;
import com.fanphotographer.bean.RadioBean;
import com.fanphotographer.data.constant.Constants;
import com.fanphotographer.fcm.MyFirebaseMessagingService;
import com.fanphotographer.helper.LocationUpdatesService;
import com.fanphotographer.utility.ActivityUtils;
import com.fanphotographer.utility.AppUtils;
import com.fanphotographer.utility.DialogUtils;
import com.fanphotographer.utility.ErrorUtils;
import com.fanphotographer.utility.ImageUtils;
import com.fanphotographer.webservice.Api;
import com.fanphotographer.webservice.ApiFactory;
import com.firebase.jobdispatcher.Constraint;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;


public class UserLocationActivity extends BaseActivity implements View.OnClickListener {


    String tittle;
    String resionsId;
    ListView listview;
    private GoogleMap googleMap;
    private String orderId;
    private String slotTime;
    private String orderSlotid;
    private ArrayList<RadioBean> arrayList;
    private BroadcastReceiver broadcastReceiver;
    private Dialog cancelDailog;
     private RadioAdapter radioAdapter;
    private String customerProfileimage;
    private double customerLat;
    private double customerLong;
    private double photographerLat;
    private double photographerLong;
    private Marker photoMarker;
    private MarkerOptions photoMarkeroption;
    private String customerLatitude;
    private String customerLongitude;
    private  TextView headerRightText;
    private Context mContext = this;
     Dialog starttimeDailog;
//    private FirebaseJobDispatcher dispatcher;
//    private Job myJob;
//    private boolean flagService;
//    JobScheduler jobScheduler;


    public static List<LatLng> decodePoly(String encoded) {

        List<LatLng> poly = new ArrayList<>();
        int index = 0;
        int len = encoded.length();
        int lat = 0;
        int lng = 0;

        while (index < len) {
            int b;
            int shift = 0;
            int result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }

        return poly;
    }

    private static float computeRotation(float fraction, float start, float end) {
        float normalizeEnd = end - start; // rotate start to 0
        float normalizedEndAbs = (normalizeEnd + 360) % 360;

        float direction = (normalizedEndAbs > 180) ? -1 : 1; // -1 = anticlockwise, 1 = clockwise
        float rotation;
        if (direction > 0) {
            rotation = normalizedEndAbs;
        } else {
            rotation = normalizedEndAbs - 360;
        }

        float result = fraction * rotation + start;
        return (result + 360) % 360;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_location);
//        dispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(mContext));

        if(AppUtils.isServiceRunning(UserLocationActivity.this,LocationUpdatesService.class)){
            stopService(new Intent(UserLocationActivity.this, LocationUpdatesService.class));
        }
//        if(!flagService){
//            createJobService();
//        }
        startService(new Intent(this, LocationUpdatesService.class));
        customerLatitude = appPreference.getString(Constants.CustomerLATITUDE);
        customerLongitude = appPreference.getString(Constants.CustomerLONGITUDE);

        if(Constants.isStringNullOrBlank(customerLatitude)){
            getData();
        }
        String photographerLatitude = appPreference.getString(Constants.LATITUDE);
        String photographerLongitude = appPreference.getString(Constants.LONGITUDE);
        if (!Constants.isStringNullOrBlank(customerLatitude)) {
            customerLat = Double.valueOf(customerLatitude);
            customerLong = Double.valueOf(customerLongitude);

            photographerLat = Double.valueOf(photographerLatitude);
            photographerLong = Double.valueOf(photographerLongitude);

        }
        orderId = appPreference.getString(Constants.ORDER_ID);
        setupMap();
        intView();
        setToolbar();
        register();

        AppController.getInstance().publicSocket(new AppController.TrackLocationListener() {
            @Override
            public void onLocationGet(double latitude, double longitude) {
                Log.e(Constants.LOG_CAT,"onLocationGet");
            }

            @Override
            public void onConnected() {
                Log.e(Constants.LOG_CAT,"onConnected");
            }
        });

        MyFirebaseMessagingService.clearNotificationAll(UserLocationActivity.this);



        starttimeDailog = new Dialog(UserLocationActivity.this);
        starttimeDailog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        starttimeDailog.setCancelable(false);
        starttimeDailog.setCanceledOnTouchOutside(false);
        starttimeDailog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        starttimeDailog.setContentView(R.layout.dailoglayout);





    }
    public void getData(){
        String str = appPreference.getString(Constants.REQUEST_PROCEED);
        try {
            JSONObject jsonObject = new JSONObject(str);
            customerLatitude = jsonObject.optString("customer_latitude");
            customerLongitude = jsonObject.optString("customer_longitude");
            orderId = jsonObject.optString(Constants.ORDER_ID);
            orderSlotid = jsonObject.optString(Constants.ORDER_SLOT_ID);
            appPreference.setString(Constants.ORDER_ID,orderId);
            appPreference.setString(Constants.ORDER_SLOT_ID,orderSlotid);
            appPreference.setString(Constants.CustomerLATITUDE,""+customerLatitude);
            appPreference.setString(Constants.CustomerLONGITUDE,""+customerLongitude);
            appPreference.setBoolean(Constants.USERLOCATION_ACTIVITY,true);

        } catch (Exception e) {
            Log.e(Constants.LOG_CAT,e.getMessage());
        }

    }
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    private void intView() {

        TextView buttonCancel = (TextView) findViewById(R.id.button_cancel);
        buttonCancel.setOnClickListener(this);
    }

    private void setToolbar() {
        FloatingActionButton fabChatbutton = (FloatingActionButton) findViewById(R.id.fab_chat_button);
         headerRightText = (TextView) findViewById(R.id.headerRightText);
        ImageView imageViewMenu = (ImageView) findViewById(R.id.imageView_menu);
        TextView headerTextView = (TextView) findViewById(R.id.hedertextview);
        ImageView toolbarbackpress = (ImageView) findViewById(R.id.toolbarbackpress);
        TextView textViewmint = (TextView) findViewById(R.id.textView_mint);
         if(!Constants.isStringNullOrBlank(appPreference.getString(Constants.ARRIVING_TIME)))
             textViewmint.setText(appPreference.getString(Constants.ARRIVING_TIME)+" to reach");

        toolbarbackpress.setVisibility(View.GONE);
        imageViewMenu.setVisibility(View.GONE);

        headerRightText.setText(getResources().getString(R.string.start));
        if(appPreference.getBoolean(Constants.IS_BUTTON)){
            headerRightText.setVisibility(View.VISIBLE);
        }else {
            headerRightText.setVisibility(View.GONE);
        }
        headerTextView.setText(getResources().getString(R.string.user_location));
        toolbarbackpress.setOnClickListener(this);
        headerRightText.setOnClickListener(this);
        fabChatbutton.setOnClickListener(this);
    }

    private void setupMap() {

        SupportMapFragment mSupportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.fragmentMap1);
        if (mSupportMapFragment != null) {
            mSupportMapFragment.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap map) {
                    googleMap = map;
                    photoMarkeroption = new MarkerOptions();
                    if (googleMap != null) {
                        addDestinationMarker(customerLat, customerLong);
                        photoMarker = googleMap.addMarker(photoMarkeroption.position(new LatLng(photographerLat, photographerLong))
                                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.current_location)));
                        String url = makeURL(
                                customerLat,
                                customerLong,
                                photographerLat,
                                photographerLong);
                        getJsonFromUrl(url);
                        CameraPosition cameraPosition = new CameraPosition.Builder().target(new LatLng(customerLat, customerLong)).zoom(13.0f).build();
                        CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition);
                        googleMap.moveCamera(cameraUpdate);

                    }
                }
            });
        }
    }


    @SuppressLint("StaticFieldLeak")
    public void getJsonFromUrl(String url) {

        url = url.replace(" ", "%20").trim();
        new AsyncTask<String, Void, String>() {

            @Override
            protected String doInBackground(String... url) {
                String data = "";
                try {
                    data = downloadUrl(url[0]);
                    Log.d("Background Task data", data);
                } catch (Exception e) {
                    Log.d("Background Task", e.toString());
                }
                return data;
            }

            @Override
            protected void onPostExecute(String result) {
                super.onPostExecute(result);
                drawPath(result);
            }
        }.execute(url);
    }

    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.connect();
            iStream = urlConnection.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            data = sb.toString();

            Log.e("url", "downloadUrl: " + data);
            br.close();

        } catch (Exception e) {
            Log.e(Constants.LOG_CAT,e.getMessage());
        } finally {
            if (iStream != null) {
                iStream.close();
            }
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
        return data;
    }

    public void drawPath(String result) {

        try {

            final JSONObject json = new JSONObject(result);
            JSONArray routeArray = json.getJSONArray("routes");
            JSONObject routes = routeArray.getJSONObject(0);
            JSONObject overviewPolylines = routes.getJSONObject("overview_polyline");
            String encodedString = overviewPolylines.getString("points");
            List<LatLng> list = decodePoly(encodedString);

            int size = list.size();
            PolylineOptions optline = new PolylineOptions();
            PolylineOptions optline2 = new PolylineOptions();
            optline.geodesic(true);
            optline.width(8);
            optline2.geodesic(true);
            optline2.width(5);

            for (int i = 0; i < size; i++) {

                optline.add(list.get(i)).width(8).color(getResources().getColor(R.color.colorPrimary)).geodesic(true);
            }

            googleMap.addPolyline(optline);

        } catch (Exception e) {
            Log.e(Constants.LOG_CAT,e.getMessage());
        }
    }

    public String makeURL(double sourcelat, double sourcelog, double destlat, double destlog) {

        StringBuilder urlString = new StringBuilder();
        urlString.append("https://maps.googleapis.com/maps/api/directions/json");
        urlString.append("?origin=");// from
        urlString.append(Double.toString(sourcelat));
        urlString.append(",");
        urlString.append(Double.toString(sourcelog));
        urlString.append("&destination=");// to
        urlString.append(Double.toString(destlat));
        urlString.append(",");
        urlString.append(Double.toString(destlog));
        urlString.append("&sensor=true&alternatives=false");//&mode=driving
        urlString.append("&key=" + getResources().getString(R.string.google_browser_key));
        Log.i("Json direction url = ", "" + urlString.toString());

        return urlString.toString();
    }

    @Override
    public void onBackPressed() {
        Log.e(Constants.LOG_CAT,"backpressed");
    }

    private void addDestinationMarker(double lat, double lon) {

        try {

            final LatLng perth = new LatLng(lat, lon);
            Drawable drawableOrange = getResources().getDrawable(R.mipmap.pointer);
            BitmapDescriptor markerIcon = getMarkerIconFromDrawable(drawableOrange);
            googleMap.addMarker(new MarkerOptions()
                    .position(perth)
                    .icon(markerIcon)
                    .draggable(true));
        } catch (Exception e) {
            Log.e(Constants.LOG_CAT,e.getMessage());

        }

    }

    private BitmapDescriptor getMarkerIconFromDrawable(Drawable drawable) {
        try {
            Canvas canvas = new Canvas();
            Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
            canvas.setBitmap(bitmap);
            drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
            drawable.draw(canvas);
            return BitmapDescriptorFactory.fromBitmap(bitmap);
        } catch (Exception e) {
            Log.e(Constants.LOG_CAT,e.getMessage());
        }
        return null;
    }

    @Override
    public void onClick(View view) {
        int i = view.getId();
        if (i == R.id.toolbarbackpress) {
            onBackPressed();

        } else if (i == R.id.headerRightText) {
            if (!Constants.isStringNullOrBlank(orderId)) {
                if (AppUtils.isNetworkConnected()) {
                    startTimedialog(orderId);
                } else {
                    final ViewGroup viewGroup = (ViewGroup) ((ViewGroup) UserLocationActivity.this.findViewById(android.R.id.content)).getChildAt(0);
                    Constants.showSnackbar(UserLocationActivity.this, viewGroup, getResources().getString(R.string.no_internet), Constants.RETRY);
                }
            }

        } else if (i == R.id.button_cancel) {
            if (!Constants.isStringNullOrBlank(orderId)) {
                if (AppUtils.isNetworkConnected()) {
                    getCancelresons(orderId);
                } else {
                    final ViewGroup viewGroup = (ViewGroup) ((ViewGroup) UserLocationActivity.this.findViewById(android.R.id.content)).getChildAt(0);
                    Constants.showSnackbar(UserLocationActivity.this, viewGroup, getResources().getString(R.string.no_internet), Constants.RETRY);
                }
            }

        } else if (i == R.id.fab_chat_button) {
            Intent sentintent = new Intent(UserLocationActivity.this, ChatActivity.class);
            sentintent.putExtra(Constants.ORDER_ID, orderId);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            startActivity(sentintent);
            finish();

        }
    }

    public void getCancelresons(final String order_id) {

        Api api = ApiFactory.getClientWithoutHeader(UserLocationActivity.this).create(Api.class);
        Call<ResponseBody> call;
        call = api.getCancelresons();

        Constants.showProgressDialog(UserLocationActivity.this, Constants.LOADING);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Constants.hideProgressDialog();
                try {
                    if (response != null && response.isSuccessful() && response.code() == 200) {
                        String output = ErrorUtils.getResponseBody(response);
                        JSONObject object = new JSONObject(output);
                        if (object.optString(Constants.SUCCESS).equalsIgnoreCase(Constants.TRUE)) {
                            Log.e(Constants.LOG_CAT, "onResponse:" + object.toString());
                            JSONArray jsonArray = object.optJSONArray("data");
                            arrayList = new ArrayList();
                            for (int i = 0; i < jsonArray.length(); i++) {
                                RadioBean bean = new RadioBean();
                                JSONObject obj = jsonArray.getJSONObject(i);
                                bean.id = obj.optString("id");
                                bean.title = obj.optString("title");
                                bean.created_at = obj.optString("created_at");
                                bean.updated_at = obj.optString("updated_at");
                                bean.status = false;
                                arrayList.add(bean);
                            }
                            if (!arrayList.isEmpty())
                                cancelDialog(arrayList, order_id);
                        } else if (object.optString(Constants.SUCCESS).equalsIgnoreCase(Constants.FALSE)) {
                            JSONArray jsonArray = object.optJSONArray(Constants.ERROR);
                            if (jsonArray == null) {
                                Constants.showToastAlert(object.getJSONObject(Constants.ERROR).getString(Constants.MESSAGE), UserLocationActivity.this);
                            } else {
                                Constants.showToastAlert(jsonArray.getJSONObject(0).getString(Constants.MESSAGE), UserLocationActivity.this);
                            }
                        }

                    } else if (response != null) {
                        if (response.code() == 400 || response.code() == 500 || response.code() == 403 || response.code() == 404 || response.code() == 401) {
                            if (response.code() == 401) {
                                appPreference.clearsession();
                                DialogUtils.showOkDialogBox(UserLocationActivity.this, getResources().getString(R.string.http_401_error), new DialogUtils.AlertMessageListener() {
                                    @Override
                                    public void onClickOk() {
                                        ActivityUtils.getInstance().sendFlow(UserLocationActivity.this, EnterMobileActivity.class);
                                    }
                                });

                            } else {
                                Constants.showToastAlert(ErrorUtils.getHtttpCodeError(response.code()), UserLocationActivity.this);
                            }
                        } else {
                            String responseStr = ErrorUtils.getResponseBody(response);
                            JSONObject jsonObject = new JSONObject(responseStr);
                            Constants.showToastAlert(ErrorUtils.checkJosnErrorBody(jsonObject), UserLocationActivity.this);
                        }
                    }
                } catch (Exception e) {
                    Log.e(Constants.LOG_CAT,e.getMessage());
                }

            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Constants.hideProgressDialog();
                Constants.showToastAlert(ErrorUtils.getString(R.string.failled), UserLocationActivity.this);
            }
        });
    }

    public void register() {
        IntentFilter intentFilter = new IntentFilter("ACTION_REFRESH_USER.intent.MAIN");

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String broadCastActionType = intent.getStringExtra(Constants.BROADCAST_ACTION);


                if (broadCastActionType.equals(Constants.ACTION_REFRESH_USER)) {
                    String request = intent.getStringExtra("REQUEST");
                    orderSlotid = intent.getStringExtra(Constants.ORDER_SLOT_ID);
                    orderId = intent.getStringExtra(Constants.ORDER_ID);
                    slotTime = intent.getStringExtra("slot_time");
                    String notificationId = intent.getStringExtra("notification_id");
                    if(request==null)
                        request="";
                    if (request.equals(Constants.REQUEST_START_TIME)) {
                        customerProfileimage = intent.getStringExtra(Constants.CUSTOMER_PROFILE_IMAGE);

                            if (!Constants.isStringNullOrBlank(orderId)) {
                                if (AppUtils.isNetworkConnected()) {
                                    reomveNotification(notificationId);
                                    appPreference.setString(Constants.ORDER_ID, orderId);
                                    appPreference.setString(Constants.ORDER_SLOT_ID, orderSlotid);
                                    startTimedialog(orderId);
                                } else {
                                    final ViewGroup viewGroup = (ViewGroup) ((ViewGroup) UserLocationActivity.this.findViewById(android.R.id.content)).getChildAt(0);
                                    Constants.showSnackbar(UserLocationActivity.this, viewGroup, getResources().getString(R.string.no_internet), Constants.RETRY);
                                }
                            }
                        } else if (request.equals(Constants.REQUEST_CANCEL_SESSION_BY_CUSTOMER)) {
//                          if(flagService){
//                            stopJobService();
//                          }
                            if (AppUtils.isServiceRunning(UserLocationActivity.this, LocationUpdatesService.class)) {
                                stopService(new Intent(UserLocationActivity.this, LocationUpdatesService.class));
                            }
                            reomveNotification(notificationId);
                            appPreference.setString(Constants.REQUEST_START_TIME, "");
                            appPreference.setString(Constants.REQUEST_END_SESSION, "");
                            appPreference.setBoolean(Constants.USERLOCATION_ACTIVITY, false);
                            appPreference.setBoolean(Constants.IS_BUTTON, false);
                            appPreference.setString(Constants.ORDER_ID, "");
                            appPreference.setString(Constants.ORDER_SLOT_ID, "");
                            Intent sentintent = new Intent(UserLocationActivity.this, MenuScreen.class);
                            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                            startActivity(sentintent);
                            finish();

                        }

                } else if (broadCastActionType.equals(Constants.ACTION_UPDATE_LOCATION)) {

                    try {
                        Log.v(Constants.LOG_CAT, "Driver Marker Update");


                        String photographerLatitude = appPreference.getString(Constants.LATITUDE);
                        String photographerLongitude = appPreference.getString(Constants.LONGITUDE);

                        if (!Constants.isStringNullOrBlank(photographerLongitude)) {
                            photographerLat = Double.parseDouble(photographerLatitude);
                            photographerLong = Double.parseDouble(photographerLongitude);
                            float bearing = intent.getFloatExtra("bearing", 0.0f);
                            rotateMarker(photoMarker, new LatLng(photographerLat, photographerLong), bearing);
                            animateMarker(photoMarker, new LatLng(photographerLat, photographerLong), false);

                        }
                    } catch (Exception e) {
                        Log.e(Constants.LOG_CAT, "Exception In Driver Marker Update :" + e);

                    }


                }

            }
        };
        registerReceiver(broadcastReceiver, intentFilter);

    }


    public void animateMarker(final Marker marker, final LatLng toPosition, final boolean hideMarker) {
        if (marker == null) {
            Log.v("Update", "Target Marker is null");
            return;
        }
        Log.v(Constants.LOG_CAT, "Tar get Marker is not null");
        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        Projection projection = googleMap.getProjection();

        Point startPoint = projection.toScreenLocation(marker.getPosition());
        final LatLng startLatLng = projection.fromScreenLocation(startPoint);
        final long duration = 1500;
        final Interpolator interpolator = new LinearInterpolator();
        handler.post(new Runnable() {
            @Override
            public void run() {
                long elapsed = SystemClock.uptimeMillis() - start;
                float t = interpolator.getInterpolation((float) elapsed / duration);
                double lng = t * toPosition.longitude + (1 - t) * startLatLng.longitude;
                double lat = t * toPosition.latitude + (1 - t) * startLatLng.latitude;
                marker.setPosition(new LatLng(lat, lng));
                if (t < 1.0) {
                    // Post again 16ms later.
                    handler.postDelayed(this, 16);
                } else {
                    if (hideMarker) {
                        marker.setVisible(false);
                    } else {
                        marker.setVisible(true);
                    }
                }
            }
        });
    }

    private void rotateMarker(final Marker marker, final LatLng destination, final float rotation) {

        if (marker != null) {

            final LatLng startPosition = marker.getPosition();
            final float startRotation = marker.getRotation();

            final LatLngInterpolator latLngInterpolator = new LatLngInterpolator.LinearFixed();
            ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, 1);
            valueAnimator.setDuration(1000); // duration 3 second
            valueAnimator.setInterpolator(new LinearInterpolator());
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {

                    try {
                        float v = animation.getAnimatedFraction();
                        LatLng newPosition = latLngInterpolator.interpolate(v, startPosition, destination);
                        float bearing = computeRotation(v, startRotation, rotation);

                        marker.setRotation(bearing);
                        marker.setPosition(newPosition);

                    } catch (Exception e) {
                        Log.e(Constants.LOG_CAT,e.getMessage());
                    }
                }
            });
            valueAnimator.start();
        }
    }

    public void reomveNotification(String id) {
        if (!Constants.isStringNullOrBlank(id)) {
            MyFirebaseMessagingService.clearNotification(UserLocationActivity.this, Integer.valueOf(id));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            String obj = appPreference.getString(Constants.REQUEST_START_TIME);
            if (!Constants.isStringNullOrBlank(obj)) {
                JSONObject j = new JSONObject(obj);
                MyFirebaseMessagingService.clearNotificationAll(UserLocationActivity.this);
                orderSlotid = j.optString("order_slot_id");
                orderId = j.optString(Constants.ORDER_ID);
                slotTime = j.optString(Constants.SLOT_TIME);
                if (j.optString("type").equalsIgnoreCase(Constants.REQUEST_START_TIME)) {
                    customerProfileimage = j.optString(Constants.CUSTOMER_PROFILE_IMAGE);
                    if (!Constants.isStringNullOrBlank(orderId)) {
                        if (AppUtils.isNetworkConnected()) {
                            appPreference.setString(Constants.ORDER_ID, orderId);
                            appPreference.setString(Constants.ORDER_SLOT_ID, orderSlotid);
                            startTimedialog(orderId);
                        } else {
                            final ViewGroup viewGroup = (ViewGroup) ((ViewGroup) UserLocationActivity.this.findViewById(android.R.id.content)).getChildAt(0);
                            Constants.showSnackbar(UserLocationActivity.this, viewGroup, getResources().getString(R.string.no_internet), Constants.RETRY);
                        }
                    }


                } else if (j.optString("type").equalsIgnoreCase(Constants.REQUEST_CANCEL_SESSION_BY_CUSTOMER)) {
//                    if(flagService){
//                        stopJobService();
//                    }
                    if(AppUtils.isServiceRunning(UserLocationActivity.this,LocationUpdatesService.class)){
                        stopService(new Intent(UserLocationActivity.this, LocationUpdatesService.class));
                    }
                    appPreference.setString(Constants.REQUEST_START_TIME, "");
                    appPreference.setString(Constants.REQUEST_END_SESSION, "");
                    appPreference.setBoolean(Constants.USERLOCATION_ACTIVITY, false);
                    appPreference.setBoolean(Constants.IS_BUTTON,false);
                    appPreference.setString(Constants.ORDER_ID, "");
                    appPreference.setString(Constants.ORDER_SLOT_ID, "");
                    Intent sentintent = new Intent(UserLocationActivity.this, MenuScreen.class);
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    startActivity(sentintent);
                    finish();
                }
            }

        } catch (Exception e) {
            Log.e(Constants.LOG_CAT,e.getMessage());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastReceiver);
//        if(flagService){
//            stopJobService();
//        }
        if(AppUtils.isServiceRunning(UserLocationActivity.this,LocationUpdatesService.class)){
            stopService(new Intent(UserLocationActivity.this, LocationUpdatesService.class));
        }
    }


    public void startTimedialog(final String id) {

        TextView yes = (TextView) starttimeDailog.findViewById(R.id.yes);
        TextView no = (TextView) starttimeDailog.findViewById(R.id.no);

        yes.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                starttimeDailog.dismiss();

                if (AppUtils.isNetworkConnected()) {
                    startTimeapprove(id);
                } else {
                    final ViewGroup viewGroup = (ViewGroup) ((ViewGroup) UserLocationActivity.this.findViewById(android.R.id.content)).getChildAt(0);
                    Constants.showSnackbar(UserLocationActivity.this, viewGroup, getResources().getString(R.string.no_internet), "Retry");
                }


            }
        });
        no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                starttimeDailog.dismiss();
                appPreference.setBoolean(Constants.IS_BUTTON,true);
                headerRightText.setVisibility(View.VISIBLE);
            }
        });

         if(!starttimeDailog.isShowing()){
             starttimeDailog.show();
         }else {
             starttimeDailog.dismiss();
             starttimeDailog.show();
         }

    }

    public void cancelDialog(final ArrayList<RadioBean> arrayList, final String order_id) {
        resionsId = "";
        tittle = "";
        cancelDailog = new Dialog(UserLocationActivity.this);
        cancelDailog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        cancelDailog.setCancelable(false);
        cancelDailog.setCanceledOnTouchOutside(false);
        cancelDailog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        cancelDailog.setContentView(R.layout.cancel_booking);
        listview = (ListView) cancelDailog.findViewById(R.id.listview);
        TextView yes = (TextView) cancelDailog.findViewById(R.id.yes);
        TextView no = (TextView) cancelDailog.findViewById(R.id.no);
        if (arrayList != null && !arrayList.isEmpty()) {
            radioAdapter = new RadioAdapter(UserLocationActivity.this, arrayList);
            listview.setAdapter(radioAdapter);
        }
        listview.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long sid) {
                tittle = arrayList.get(position).title;
                resionsId = arrayList.get(position).id;
                for (int j = 0; j < arrayList.size(); j++) {
                    if (position == j) {
                        arrayList.get(j).status = true;
                    } else {
                        arrayList.get(j).status = false;
                    }
                }
                radioAdapter.notifyDataSetChanged();
            }
        });

        yes.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                cancelDailog.dismiss();
                if (!Constants.isStringNullOrBlank(resionsId)) {
                    if (AppUtils.isNetworkConnected()) {
                        cancelSessiongrapher(order_id, resionsId);
                    } else {
                        final ViewGroup viewGroup = (ViewGroup) ((ViewGroup) UserLocationActivity.this.findViewById(android.R.id.content)).getChildAt(0);
                        Constants.showSnackbar(UserLocationActivity.this, viewGroup, getResources().getString(R.string.no_internet), "Retry");
                    }
                } else {
                    Constants.showToastAlert("Please select cancel reason", UserLocationActivity.this);
                }

            }
        });
        no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelDailog.dismiss();
            }
        });
        cancelDailog.show();
    }

    public void cancelSessiongrapher(String strOrderid, String cancelId) {

        Api api = ApiFactory.getClientWithoutHeader(UserLocationActivity.this).create(Api.class);
        Call<ResponseBody> call;
        String accessToken = appPreference.getString(Constants.ACCESS_TOKEN);
        call = api.cancel_session_by_photographer_api(accessToken, strOrderid, cancelId);


        Constants.showProgressDialog(UserLocationActivity.this, Constants.LOADING);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Constants.hideProgressDialog();
                try {
                    if (response != null && response.isSuccessful() && response.code() == 200) {
                        String output = ErrorUtils.getResponseBody(response);
                        JSONObject object = new JSONObject(output);
                        if (object.optString(Constants.SUCCESS).equalsIgnoreCase(Constants.TRUE)) {
                            Log.e(Constants.LOG_CAT, "onResponse:" + object.toString());
                            JSONObject jsonObject = object.optJSONObject("data");
                            DialogUtils.showOkDialogBox(UserLocationActivity.this, jsonObject.optString(Constants.MESSAGE), new DialogUtils.AlertMessageListener() {
                                @Override
                                public void onClickOk() {
                                    appPreference.setBoolean(Constants.USERLOCATION_ACTIVITY, false);
                                    appPreference.setBoolean(Constants.IS_BUTTON,false);
                                     appPreference.setString(Constants.REQUEST_PROCEED,"");
                                    appPreference.setString(Constants.ORDER_ID, "");
                                    appPreference.setString(Constants.ORDER_SLOT_ID, "");
                                    appPreference.setString("arriving_time","");
                                    Intent intent = new Intent(UserLocationActivity.this, MenuScreen.class);
                                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                                    startActivity(intent);
                                    finish();
                                }
                            });
                        } else if (object.optString(Constants.SUCCESS).equalsIgnoreCase(Constants.FALSE)) {
                            JSONArray jsonArray = object.optJSONArray(Constants.ERROR);
                            if (jsonArray == null) {
                                Constants.showToastAlert(object.getJSONObject(Constants.ERROR).getString(Constants.MESSAGE), UserLocationActivity.this);
                            } else {
                                Constants.showToastAlert(jsonArray.getJSONObject(0).getString(Constants.MESSAGE), UserLocationActivity.this);
                            }
                        }

                    } else if (response != null) {
                        if (response.code() == 400 || response.code() == 500 || response.code() == 403 || response.code() == 404 || response.code() == 401) {
                            if (response.code() == 401) {
                                appPreference.clearsession();
                                DialogUtils.showOkDialogBox(UserLocationActivity.this, getResources().getString(R.string.http_401_error), new DialogUtils.AlertMessageListener() {
                                    @Override
                                    public void onClickOk() {
                                        ActivityUtils.getInstance().sendFlow(UserLocationActivity.this, EnterMobileActivity.class);
                                    }
                                });
                            } else {
                                Constants.showToastAlert(ErrorUtils.getHtttpCodeError(response.code()), UserLocationActivity.this);
                            }
                        } else {
                            String responseStr = ErrorUtils.getResponseBody(response);
                            JSONObject jsonObject = new JSONObject(responseStr);
                            Constants.showToastAlert(ErrorUtils.checkJosnErrorBody(jsonObject), UserLocationActivity.this);
                        }
                    }
                } catch (Exception e) {
                    Log.e(Constants.LOG_CAT,e.getMessage());
                }

            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Constants.hideProgressDialog();
                Constants.showToastAlert(ErrorUtils.getString(R.string.failled), UserLocationActivity.this);
            }
        });
    }

    public void startTimeapprove(String strOrderid) {

        Api api = ApiFactory.getClientWithoutHeader(UserLocationActivity.this).create(Api.class);
        HashMap<String, String> map = new HashMap<>();
        Call<ResponseBody> call;
        String accessToken = appPreference.getString(Constants.ACCESS_TOKEN);
        map.put("order_id", strOrderid);
        call = api.starttimeApproveapi(accessToken, map);
        Log.e(Constants.LOG_CAT, "HEADERS : " + call.request().headers());

        Constants.showProgressDialog(UserLocationActivity.this, Constants.LOADING);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Constants.hideProgressDialog();
                try {
                    if (response != null && response.isSuccessful() && response.code() == 200) {
                        String output = ErrorUtils.getResponseBody(response);
                        JSONObject object = new JSONObject(output);
                        if (object.optString(Constants.SUCCESS).equalsIgnoreCase(Constants.TRUE)) {
                            Log.e(Constants.LOG_CAT, "onResponse:" + object.toString());
                            JSONObject jsonObject = object.optJSONObject("data");
                            appPreference.setString(Constants.ORDER_ID, jsonObject.optString("order_id"));
                            appPreference.setString(Constants.ORDER_SLOT_ID, jsonObject.optString("order_slot_id"));
                            appPreference.setBoolean(Constants.USERLOCATION_ACTIVITY, false);
                            appPreference.setBoolean(Constants.IS_BUTTON,false);
                            ImageUtils.deleteRootDirPath();
                            Intent intent = new Intent(UserLocationActivity.this, PhotoShootStarted.class);
                            intent.putExtra("new_slot_time", slotTime);
                            intent.putExtra(Constants.CUSTOMER_PROFILE_IMAGE, customerProfileimage);
                            appPreference.setString("mtime", slotTime);
                            appPreference.setString(Constants.START_PHOTO, "0");
                            appPreference.setString(Constants.REQUEST_PROCEED,"");
                            appPreference.setString(Constants.REQUEST_END_SESSION, "");
                            appPreference.setString(Constants.REQUEST_START_TIME, "");
                            appPreference.setString("arriving_time","");
                            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                            ImageUtils.deleteRootDirPath();
                            startActivity(intent);
                            finish();

                        } else if (object.optString(Constants.SUCCESS).equalsIgnoreCase(Constants.FALSE)) {
                            JSONArray jsonArray = object.optJSONArray(Constants.ERROR);
                            if (jsonArray == null) {
                                Constants.showToastAlert(object.getJSONObject(Constants.ERROR).getString(Constants.MESSAGE), UserLocationActivity.this);
                            } else {
                                Constants.showToastAlert(jsonArray.getJSONObject(0).getString(Constants.MESSAGE), UserLocationActivity.this);
                            }
                        }

                    } else if (response != null) {
                        if (response.code() == 400 || response.code() == 500 || response.code() == 403 || response.code() == 404 || response.code() == 401) {
                            if (response.code() == 401) {
                                appPreference.showCustomAlert(UserLocationActivity.this, getResources().getString(R.string.http_401_error));
                            } else {
                                Constants.showToastAlert(ErrorUtils.getHtttpCodeError(response.code()), UserLocationActivity.this);
                            }
                        } else {
                            String responseStr = ErrorUtils.getResponseBody(response);
                            JSONObject jsonObject = new JSONObject(responseStr);
                            Constants.showToastAlert(ErrorUtils.checkJosnErrorBody(jsonObject), UserLocationActivity.this);
                        }
                    }
                } catch (Exception e) {
                    Log.e(Constants.LOG_CAT,e.getMessage());
                }

            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Constants.hideProgressDialog();
                Constants.showToastAlert(ErrorUtils.getString(R.string.failled), UserLocationActivity.this);
            }
        });
    }


    public interface LatLngInterpolator {
        LatLng interpolate(float fraction, LatLng a, LatLng b);

        class LinearFixed implements LatLngInterpolator {
            @Override
            public LatLng interpolate(float fraction, LatLng a, LatLng b) {
                double lat = (b.latitude - a.latitude) * fraction + a.latitude;
                double lngDelta = b.longitude - a.longitude;

                // Take the shortest path across the 180th meridian.
                if (Math.abs(lngDelta) > 180) {
                    lngDelta -= Math.signum(lngDelta) * 360;
                }
                double lng = lngDelta * fraction + a.longitude;
                return new LatLng(lat, lng);
            }
        }
    }




    /*private void createJobService() {

        myJob = dispatcher.newJobBuilder()
                .setService(LocationMonitoringService.class)
                .setTag("LocationTracking")
                .setRecurring(true)
                .setTrigger(Trigger.executionWindow(0, 20))
                .setLifetime(Lifetime.UNTIL_NEXT_BOOT)
                .setReplaceCurrent(false)
                .setConstraints(Constraint.ON_ANY_NETWORK)
                .setRetryStrategy(RetryStrategy.DEFAULT_EXPONENTIAL)
                .build();
        flagService = true;
        dispatcher.schedule(myJob);
    }


    private void stopJobService() {
        flagService = false;
        dispatcher.cancel("LocationTracking");

    }
*/

}
