package com.fancustomer.activity;

import android.Manifest;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
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
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.fancustomer.AppController;
import com.fancustomer.R;
import com.fancustomer.data.constant.Constants;
import com.fancustomer.data.preference.AppPreference;
import com.fancustomer.fcm.MyFirebaseMessagingService;
import com.fancustomer.helper.ErrorUtils;
import com.fancustomer.helper.ExceptionHandler;
import com.fancustomer.webservice.Api;
import com.fancustomer.webservice.ApiFactory;
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
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;


public class TrackPhotographer extends BaseActivity implements View.OnClickListener {


    private String orderID = "";
    private GoogleMap googleMap;
    private BroadcastReceiver broadcastReceiver;
    private String orderCancelCharge = "";
    private FloatingActionButton chatFabButton;
    private double photographerLatitude;
    private double photographerLongitude;
    private double customerLatitude;
    private double customerLongitude;
    private Marker photographerMarker;
    private MarkerOptions photoMarkerOption;
    public static Activity trackPhotographer;
    private String arrivingTime = "";
    private TextView textviewMint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        trackPhotographer = this;
        setContentView(R.layout.activity_track_photographer);
        orderID = AppPreference.getInstance(TrackPhotographer.this).getString("orderID");
        orderCancelCharge = AppPreference.getInstance(TrackPhotographer.this).getString("order_cancel_charge");
        arrivingTime = AppPreference.getInstance(TrackPhotographer.this).getString("arriving_time");
        String customerLatitud = AppPreference.getInstance(TrackPhotographer.this).getString("customer_latitude");
        String customerLongitud = AppPreference.getInstance(TrackPhotographer.this).getString("customer_longitude");
        String photographerLatitud = AppPreference.getInstance(TrackPhotographer.this).getString("photographer_latitude");
        String photographerLongitud = AppPreference.getInstance(TrackPhotographer.this).getString("photographer_longitude");
        if (!Constants.isStringNullOrBlank(customerLatitud)) {
            customerLatitude = Double.parseDouble(customerLatitud);
            customerLongitude = Double.parseDouble(customerLongitud);
            photographerLatitude = Double.parseDouble(photographerLatitud);
            photographerLongitude = Double.parseDouble(photographerLongitud);
        }
        setupMap();
        startTracking();
        intView();
        setToolBar();
    }


    private void startTracking() {

        AppController.getInstance().publicSocket(new AppController.TrackLocationListener() {
            @Override
            public void onLocationGet(final double latitude, final double longitude) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        photographerLatitude = latitude;
                        photographerLongitude = longitude;
                        rotateMarker(photographerMarker, new LatLng(photographerLatitude, photographerLongitude), 1);
                        animateMarker(photographerMarker, new LatLng(photographerLatitude, photographerLongitude), false);
                    }
                });


            }

            @Override
            public void onConnected() {
                Log.e(Constants.LOG_CAT, "onConnected: ");

            }
        });
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
                        ExceptionHandler.printStackTrace(e);
                    }
                }
            });
            valueAnimator.start();
        }
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

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @SuppressLint("SetTextI18n")
    private void intView() {
        textviewMint = (TextView) findViewById(R.id.textView_mint);
        chatFabButton = (FloatingActionButton) findViewById(R.id.chat_fab_button);
        TextView buttonCancel = (TextView) findViewById(R.id.button_cancel);
        buttonCancel.setOnClickListener(this);
        chatFabButton.setOnClickListener(this);
        textviewMint.setText(arrivingTime + " to reach");
    }


    @Override
    protected void networkConnnectivityChange() {
        super.networkConnnectivityChange();
        if (!Constants.isInternetOn(TrackPhotographer.this)) {
            final ViewGroup viewGroup = (ViewGroup) ((ViewGroup) TrackPhotographer.this.findViewById(android.R.id.content)).getChildAt(0);
            showSnackbar(viewGroup, getResources().getString(R.string.no_internet), Constants.RETRY);
        } else {
            hideSnackbar();
        }
    }

    private void setToolBar() {
        TextView headerRightText = (TextView) findViewById(R.id.trackHeaderRightText);
        ImageView imageViewMenu = (ImageView) findViewById(R.id.imageView_menu);
        TextView headerTextView = (TextView) findViewById(R.id.hedertextview);
        ImageView toolbarbackpress = (ImageView) findViewById(R.id.toolbarbackpress);
        toolbarbackpress.setVisibility(View.GONE);
        imageViewMenu.setVisibility(View.GONE);
        headerRightText.setVisibility(View.VISIBLE);
        headerRightText.setText(getResources().getString(R.string.start));
        headerTextView.setText(getResources().getString(R.string.track_photographer));
        toolbarbackpress.setOnClickListener(this);
        headerRightText.setOnClickListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            unregisterReceiver(broadcastReceiver);
        } catch (Exception e) {
            ExceptionHandler.printStackTrace(e);
        }
    }


    private double distance(double lat1, double lon1, double lat2, double lon2) {
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1))
                * Math.sin(deg2rad(lat2))
                + Math.cos(deg2rad(lat1))
                * Math.cos(deg2rad(lat2))
                * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        return (dist);
    }

    private double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    private double rad2deg(double rad) {
        return (rad * 180.0 / Math.PI);
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter("ACTION_REFRESH_USER.intent.MAIN");
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String broadCastActionType = intent.getStringExtra(Constants.BROADCAST_ACTION);
                String cancelSessionByPhotographer = intent.getStringExtra("CANCEL_SESSION_BY_PHOTOGRAPHER");
                String notificationId = intent.getStringExtra("notification_id");
                if (broadCastActionType.equals(Constants.ACTION_REFRESH_USER)) {
                    if (!Constants.isStringNullOrBlank(cancelSessionByPhotographer)) {
                        if (cancelSessionByPhotographer.equals(Constants.CANCEL_SESSION_BY_PHOTOGRAPHER)) {
                            AppPreference.getInstance(TrackPhotographer.this).setString(Constants.TRACK_START, "0");
                            Intent requestIntent = new Intent(TrackPhotographer.this, MenuScreen.class);
                            startActivity(requestIntent);
                            finish();
                            if (!Constants.isStringNullOrBlank(notificationId)) {
                                MyFirebaseMessagingService.clearNotification(TrackPhotographer.this, Integer.valueOf(notificationId));
                            }

                        }
                    }

                }


            }
        };
        registerReceiver(broadcastReceiver, intentFilter);
    }

    @SuppressLint("SetTextI18n")
    private void showCancelDialog() {
        final Dialog dialog = new Dialog(TrackPhotographer.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);

        dialog.setCanceledOnTouchOutside(false);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.dialog_payment);
        TextView buttonConfirm = dialog.findViewById(R.id.button_confirm);
        TextView textViewNo = dialog.findViewById(R.id.button_cancel);
        TextView textviewNewMessage = dialog.findViewById(R.id.textview__new_message);
        TextView textviewMessages = dialog.findViewById(R.id.textview__messages);
        TextView textviewHeader = dialog.findViewById(R.id.textview__header);
        textviewHeader.setText(getResources().getString(R.string.cancel_photoshoot));
        buttonConfirm.setText(getResources().getString(R.string.yes));
        textViewNo.setText(getResources().getString(R.string.no));
        textviewNewMessage.setText(getResources().getString(R.string.you_wil_be_charged_a_cancellation));
        textviewMessages.setText(" $ " + new DecimalFormat("#,##0.00").format(Double.parseDouble(orderCancelCharge)));
        buttonConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();

                if (Constants.isInternetOn(TrackPhotographer.this)) {
                    cancelApi(orderID);
                } else {
                    final ViewGroup viewGroup = (ViewGroup) ((ViewGroup) TrackPhotographer.this.findViewById(android.R.id.content)).getChildAt(0);
                    showSnackbar(viewGroup, getResources().getString(R.string.no_internet), Constants.RETRY);
                }

            }
        });
        textViewNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dialog.dismiss();
            }
        });
        dialog.show();

    }

    private void setupMap() {

        SupportMapFragment mSupportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.fragmentMap1);


        if (mSupportMapFragment != null) {
            mSupportMapFragment.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap map) {
                    googleMap = map;
                    googleMap.setTrafficEnabled(false);
                    googleMap.setIndoorEnabled(true);
                    googleMap.setBuildingsEnabled(false);
                    if (ActivityCompat.checkSelfPermission(TrackPhotographer.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(TrackPhotographer.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }
                    googleMap.setMyLocationEnabled(false);
                    googleMap.getUiSettings().setZoomControlsEnabled(true);
                    photoMarkerOption = new MarkerOptions();
                    if (googleMap != null) {
                        double pickLat = customerLatitude;
                        double pickLong = customerLongitude;
                        distance(customerLatitude, customerLongitude, photographerLatitude, photographerLongitude);
                        photographerMarker = googleMap.addMarker(photoMarkerOption.position(new LatLng(photographerLatitude, photographerLongitude))
                                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.current_location)));
                        addDestinationMarker(pickLat, pickLong);
                        String url = makeURL(
                                pickLat,
                                pickLong,
                                photographerLatitude,
                                photographerLongitude);
                        getJsonFromUrl(url);
                        CameraPosition cameraPosition = new CameraPosition.Builder().target(new LatLng(pickLat, pickLong)).zoom(13.0f).build();
                        CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition);
                        googleMap.moveCamera(cameraUpdate);


                    }
                }
            });
        }
    }


    public void getJsonFromUrl(String url) {
        url = url.replace(" ", "%20").trim();
        new AsyncTask<String, Void, String>() {

            @Override
            protected String doInBackground(String... url) {
                String data = "";
                try {
                    data = downloadUrl(url[0]);
                    Log.d("Background Task data", data.toString());
                } catch (Exception e) {
                    Log.d("Background Task", e.toString());
                }
                return data;
            }

            @Override
            protected void onPostExecute(String result) {
                super.onPostExecute(result);
                drawPath(result);
                /*showCurrentLocation(22.737578, 75.891855);*/
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
            StringBuffer sb = new StringBuffer();
            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            data = sb.toString();

            Log.e("url", "downloadUrl: " + data);
            br.close();

        } catch (Exception e) {
            ExceptionHandler.printStackTrace(e);
        } finally {
            iStream.close();
            urlConnection.disconnect();
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

            ExceptionHandler.printStackTrace(e);
        }
    }

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
        Log.e(Constants.LOG_CAT, "onBackPressed: ");
    }

    private void addDestinationMarker(double lat, double lon) {

        try {
            final LatLng latLng = new LatLng(lat, lon);
            Drawable drawableOrange = getResources().getDrawable(R.mipmap.pointer);
            BitmapDescriptor markerIcon = getMarkerIconFromDrawable(drawableOrange);
            Marker perth = googleMap.addMarker(new MarkerOptions()
                    .position(latLng)
                    .icon(markerIcon)
                    .draggable(true));
        } catch (Exception e) {
            ExceptionHandler.printStackTrace(e);
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
            ExceptionHandler.printStackTrace(e);
        }
        return null;
    }


    public void cancelApi(final String orderID) {

        Api api = ApiFactory.getClientWithoutHeader(TrackPhotographer.this).create(Api.class);
        Call<ResponseBody> call;
        call = api.cancelPhoto(AppPreference.getInstance(TrackPhotographer.this).getString(Constants.ACCESS_TOKEN), orderID);
        Log.e(Constants.LOG_CAT, "API  CancelApi------------------->>>>>:" + call.request().url());
        Log.e(Constants.LOG_CAT, "HEADERS : " + call.request().headers());
        Constants.showProgressDialog(TrackPhotographer.this, Constants.LOADING);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Constants.hideProgressDialog();
                try {
                    if (response.isSuccessful()) {
                        String output = ErrorUtils.getResponseBody(response);
                        JSONObject object = new JSONObject(output);
                        Log.d("tag", object.toString(1));

                        if (object.optString(Constants.SUCCESS).equalsIgnoreCase(Constants.TRUE)) {
                            Log.e(Constants.LOG_CAT, "onResponse:API  CancelApi>>>>>>>>>>>>>>>>>>>>>>>" + object.toString());
                            AppPreference.getInstance(TrackPhotographer.this).setString(Constants.TRACK_START, "0");
                            Intent intent = new Intent(TrackPhotographer.this, MenuScreen.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK
                                    | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                            finish();
                        } else if (object.optString(Constants.SUCCESS).equalsIgnoreCase(Constants.FALSE)) {
                            ErrorUtils.showFalseMessage(object, TrackPhotographer.this);
                        }
                    } else if (response.code() == 400 || response.code() == 500 || response.code() == 403 || response.code() == 404 || response.code() == 401) {
                        if (response.code() == 401) {
                            Constants.showSessionExpireAlert(TrackPhotographer.this);
                        } else {
                            Constants.showToastAlert(ErrorUtils.getHtttpCodeError(response.code()), TrackPhotographer.this);
                        }

                    } else {
                        String responseStr = ErrorUtils.getResponseBody(response);
                        JSONObject jsonObject = new JSONObject(responseStr);
                        Constants.showToastAlert(ErrorUtils.checkJosnErrorBody(jsonObject), TrackPhotographer.this);
                    }
                } catch (JSONException e) {
                    Constants.hideProgressDialog();
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Constants.hideProgressDialog();
            }
        });


    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.toolbarbackpress:
                onBackPressed();
                break;

            case R.id.trackHeaderRightText:
                Intent intent1 = new Intent(TrackPhotographer.this, StartPhotosShootActivity.class);
                startActivity(intent1);
                break;

            case R.id.button_cancel:
                showCancelDialog();
                break;

            case R.id.chat_fab_button:
                Intent intent = new Intent(TrackPhotographer.this, ChatActivity.class);
                startActivity(intent);
                break;
            default:
                break;


        }
    }
}
