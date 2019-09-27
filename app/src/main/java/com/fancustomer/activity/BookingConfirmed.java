package com.fancustomer.activity;


import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;

import android.widget.TextView;


import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.fancustomer.R;
import com.fancustomer.data.constant.Constants;
import com.fancustomer.data.preference.AppPreference;
import com.fancustomer.helper.ErrorUtils;
import com.fancustomer.helper.ExceptionHandler;
import com.fancustomer.helper.NumberProgressBar;
import com.fancustomer.webservice.Api;
import com.fancustomer.webservice.ApiFactory;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
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
import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

import static com.fancustomer.fragment.HomeFragment.mCountDownTimerHome;

public class BookingConfirmed extends BaseActivity implements View.OnClickListener {


    private GoogleMap googleMap;
    private CircleImageView userImageView;
    private String photographerId;
    private String orderId;
    private String ratingAvg;
    private TextView userNameTextView;
    private TextView arrivingTimeTextView;
    private TextView dateTextView;
    private TextView photoShootTime;
    private TextView mobileModalTextView;
    private RatingBar userRatingBar;
    private String profileImage = "";
    private String price = "";
    private String slotTime = "";
    private NumberProgressBar bnp;
    private String orderCancelCharge;
    private CountDownTimer mCountDownTimer;
    private double photographerLatitude;
    private double photographerLongitude;
    private double customerLatitude;
    private double customerLongitude;
    private String firstName = "";
    private String lastName = "";
    private String arrivingTime = "";


    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_confirmed);

        getParam();
        mCountDownTimerHome.cancel();
        setToolBar();
        startSearching();
        intView();

        if (!photographerId.equals("") || !orderId.equals("")) {
            if (Constants.isInternetOn(BookingConfirmed.this)) {
                getPhotographerProfile(photographerId, orderId);
            } else {
                final ViewGroup viewGroup = (ViewGroup) ((ViewGroup) BookingConfirmed.this.findViewById(android.R.id.content)).getChildAt(0);
                showSnackbar(viewGroup, getResources().getString(R.string.no_internet), Constants.RETRY);
            }


        }
    }

    private void getParam() {
        photographerId = getIntent().getStringExtra("photographer_id");
        orderId = getIntent().getStringExtra("order_id");
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void networkConnnectivityChange() {
        super.networkConnnectivityChange();
        if (!Constants.isInternetOn(BookingConfirmed.this)) {
            final ViewGroup viewGroup = (ViewGroup) ((ViewGroup) BookingConfirmed.this.findViewById(android.R.id.content)).getChildAt(0);
            showSnackbar(viewGroup, getResources().getString(R.string.no_internet), "Retry");
        } else {
            hideSnackbar();
        }
    }


    private int total = 0;

    public void startSearching() {
        total = 0;
        bnp = (NumberProgressBar) findViewById(R.id.number_progress_bar);
        bnp.setProgress(total);
        mCountDownTimer = new CountDownTimer(60000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                Log.v("Log_tag", "Booking timer" + total + millisUntilFinished);
                total++;
                bnp.setProgress((int) total);
                bnp.incrementProgressBy(1);

            }

            @Override
            public void onFinish() {
                try {
                    ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
                    ComponentName cn = am.getRunningTasks(1).get(0).topActivity;
                    System.out.println("=======" + cn.getClassName());
                    if (cn.getClassName().equals("com.fancustomer.activity.BookingConfirmed")) {

                        if (Constants.isInternetOn(BookingConfirmed.this)) {
                            proceedAndCancelApi(orderId, "cancel");
                        } else {
                            final ViewGroup viewGroup = (ViewGroup) ((ViewGroup) BookingConfirmed.this.findViewById(android.R.id.content)).getChildAt(0);
                            showSnackbar(viewGroup, getResources().getString(R.string.no_internet), "Retry");
                        }


                    } else {
                        Log.e(Constants.LOG_CAT, "onFinish: ");
                    }
                } catch (Exception e) {
                    ExceptionHandler.printStackTrace(e);
                }

            }
        };
        mCountDownTimer.start();

    }

    private void intView() {
        userRatingBar = (RatingBar) findViewById(R.id.userRatingBar);
        userImageView = (CircleImageView) findViewById(R.id.userImageView);
        userNameTextView = (TextView) findViewById(R.id.userNameTextView);
        arrivingTimeTextView = (TextView) findViewById(R.id.arriving_time_textView);
        dateTextView = (TextView) findViewById(R.id.dateTextView);
        photoShootTime = (TextView) findViewById(R.id.photoShootTime);
        mobileModalTextView = (TextView) findViewById(R.id.mobileModalTextView);
        userNameTextView = (TextView) findViewById(R.id.userNameTextView);
        userNameTextView = (TextView) findViewById(R.id.userNameTextView);


        TextView buttonCancel = (TextView) findViewById(R.id.button_cancel);
        TextView buttonProceed = (TextView) findViewById(R.id.button_proceed);
        buttonProceed.setOnClickListener(this);
        buttonCancel.setOnClickListener(this);
    }

    private void setToolBar() {


        TextView headerTextView = (TextView) findViewById(R.id.hedertextview);
        ImageView toolbarbackpress = (ImageView) findViewById(R.id.toolbarbackpress);
        toolbarbackpress.setVisibility(View.VISIBLE);
        toolbarbackpress.setVisibility(View.GONE);
        headerTextView.setText(BookingConfirmed.this.getResources().getString(R.string.booking_confirmed));
        toolbarbackpress.setOnClickListener(this);
    }

    @Override
    public void onBackPressed() {
    }

    private void setupMap() {

        SupportMapFragment mSupportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.fragmentMap1);


        if (mSupportMapFragment != null) {
            mSupportMapFragment.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap map) {
                    googleMap = map;
                    if (googleMap != null) {
                        double pickLat = customerLatitude;
                        double pickLong = customerLongitude;
                        double pickLat2 = photographerLatitude;
                        double pickLong2 = photographerLongitude;
                        addDestinationMarker(pickLat, pickLong);
                        addtwoDestinationMarker(pickLat2, pickLong2);
                        String url = makeURL(
                                pickLat,
                                pickLong,
                                pickLat2,
                                pickLong2);
                        getJsonFromUrl(url);

                        CameraPosition cameraPosition = new CameraPosition.Builder().target(new LatLng(pickLat, pickLong)).zoom(14.0f).build();
                        CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition);
                        googleMap.moveCamera(cameraUpdate);


                    }
                }
            });
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

    private void addtwoDestinationMarker(double lat, double lon) {
        try {
            final LatLng latLng = new LatLng(lat, lon);
            Drawable drawableOrange = getResources().getDrawable(R.mipmap.current_location);
            BitmapDescriptor markerIcon = getMarkerIconFromDrawable(drawableOrange);
            Marker perth = googleMap.addMarker(new MarkerOptions()
                    .position(latLng)
                    .icon(markerIcon)
                    .draggable(true));
        } catch (Exception e) {

            ExceptionHandler.printStackTrace(e);

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

            LatLng p = new LatLng((((double) lat / 1E5)), (((double) lng / 1E5)));
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
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.toolbarbackpress:
                onBackPressed();
                break;
            case R.id.button_proceed:

                if (Constants.isInternetOn(BookingConfirmed.this)) {
                    proceedAndCancelApi(orderId, "proceed");
                } else {
                    final ViewGroup viewGroup = (ViewGroup) ((ViewGroup) BookingConfirmed.this.findViewById(android.R.id.content)).getChildAt(0);
                    showSnackbar(viewGroup, getResources().getString(R.string.no_internet), Constants.RETRY);
                }

                break;

            case R.id.button_cancel:
                if (Constants.isInternetOn(BookingConfirmed.this)) {
                    proceedAndCancelApi(orderId, "cancel");
                } else {
                    final ViewGroup viewGroup = (ViewGroup) ((ViewGroup) BookingConfirmed.this.findViewById(android.R.id.content)).getChildAt(0);
                    showSnackbar(viewGroup, getResources().getString(R.string.no_internet), Constants.RETRY);
                }
                break;
            default:
                break;


        }
    }

    public void getPhotographerProfile(String photographerId, String orderID) {
        Api api = ApiFactory.getClientWithoutHeader(BookingConfirmed.this).create(Api.class);
        Call<ResponseBody> call;
        call = api.getPhotographerProfile(AppPreference.getInstance(BookingConfirmed.this).getString(Constants.ACCESS_TOKEN), photographerId, orderID);
        Log.e(Constants.LOG_CAT, "HEADERS : " + call.request().headers());
        Constants.showProgressDialog(BookingConfirmed.this, Constants.LOADING);
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
                            Log.e(Constants.LOG_CAT, "onResponse:API Photographer Profile=>?????????????" + object.toString());
                            JSONObject jsonObject = object.optJSONObject("data");
                            firstName = jsonObject.optString("first_name");
                            lastName = jsonObject.optString("last_name");
                            profileImage = jsonObject.optString("profile_image");
                            photographerLatitude = Double.parseDouble(jsonObject.optString("photographer_latitude"));
                            photographerLongitude = Double.parseDouble(jsonObject.optString("photographer_longitude"));
                            customerLatitude = Double.parseDouble(jsonObject.optString("customer_latitude"));
                            customerLongitude = Double.parseDouble(jsonObject.optString("customer_longitude"));
                            String createdAt = jsonObject.optString("created_at");
                            dateTextView.setText(Constants.getDateInFormat("yyyy-MM-dd hh:mm:ss", "MMM dd,yyyy hh:mm aa", createdAt));
                            ratingAvg = jsonObject.optString("rating_avg");
                            slotTime = jsonObject.optString("slot_time");
                            price = jsonObject.optString("price");
                            arrivingTime = jsonObject.optString("arriving_time");
                            String mobileModel = jsonObject.optString("mobile_model");
                            orderCancelCharge = jsonObject.optString("order_cancel_charge");
                            userNameTextView.setText(firstName + " " + lastName);
                            if (!Constants.isStringNullOrBlank(ratingAvg)) {
                                userRatingBar.setRating(Float.parseFloat(ratingAvg));
                            }
                            arrivingTimeTextView.setText(arrivingTime);
                            photoShootTime.setText(slotTime);
                            mobileModalTextView.setText(mobileModel);

                            if (!profileImage.equals("")) {
                                Glide.with(BookingConfirmed.this).load(profileImage)
                                        .thumbnail(0.5f)
                                        .placeholder(R.mipmap.defult_user).dontAnimate()
                                        .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                                        .listener(new RequestListener<String, GlideDrawable>() {
                                            @Override
                                            public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                                                return false;
                                            }

                                            @Override
                                            public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {

                                                return false;
                                            }
                                        }).into(userImageView);

                            }

                            setupMap();
                        } else if (object.optString(Constants.SUCCESS).equalsIgnoreCase(Constants.FALSE)) {
                            ErrorUtils.showFalseMessage(object, BookingConfirmed.this);
                        }
                    } else if (response.code() == 400 || response.code() == 500 || response.code() == 403 || response.code() == 404 || response.code() == 401) {
                        if (response.code() == 401) {
                            Constants.showSessionExpireAlert(BookingConfirmed.this);
                        } else {
                            Constants.showToastAlert(ErrorUtils.getHtttpCodeError(response.code()), BookingConfirmed.this);
                        }

                    } else {
                        String responseStr = ErrorUtils.getResponseBody(response);
                        JSONObject jsonObject = new JSONObject(responseStr);
                        Constants.showToastAlert(ErrorUtils.checkJosnErrorBody(jsonObject), BookingConfirmed.this);
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


    public void proceedAndCancelApi(final String orderID, final String flag) {
        Api api = ApiFactory.getClientWithoutHeader(BookingConfirmed.this).create(Api.class);
        Call<ResponseBody> call;
        call = api.getProceedCancelApi(AppPreference.getInstance(BookingConfirmed.this).getString(Constants.ACCESS_TOKEN), orderID, flag);
        Log.e(Constants.LOG_CAT, "API proceedAndCancelApi------------------->>>>>:" + " " + call.request().url());
        Log.e(Constants.LOG_CAT, "HEADERS : " + call.request().headers());


        Constants.showProgressDialog(BookingConfirmed.this, Constants.LOADING);
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
                            mCountDownTimer.cancel();
                            Log.e(Constants.LOG_CAT, "onResponse:API proceedAndCancelApi>>>>>>>>>>>>>>>>>>>>>>>" + object.toString());
                            if (flag.equals("proceed")) {
                                AppPreference.getInstance(BookingConfirmed.this).setString(Constants.TRACK_START, "1");
                                Intent intent = new Intent(BookingConfirmed.this, TrackPhotographer.class);
                                AppPreference.getInstance(BookingConfirmed.this).setString("orderID", orderID);
                                AppPreference.getInstance(BookingConfirmed.this).setString("profile_image_new", profileImage);
                                AppPreference.getInstance(BookingConfirmed.this).setString("photographer_name", firstName + " " + lastName);
                                AppPreference.getInstance(BookingConfirmed.this).setString("price", price);
                                AppPreference.getInstance(BookingConfirmed.this).setString("slotTime", slotTime);
                                AppPreference.getInstance(BookingConfirmed.this).setString("order_cancel_charge", orderCancelCharge);
                                AppPreference.getInstance(BookingConfirmed.this).setString("photographer_id", photographerId);
                                AppPreference.getInstance(BookingConfirmed.this).setString("customer_latitude", "" + customerLatitude);
                                AppPreference.getInstance(BookingConfirmed.this).setString("customer_longitude", "" + customerLongitude);
                                AppPreference.getInstance(BookingConfirmed.this).setString("photographer_latitude", "" + photographerLatitude);
                                AppPreference.getInstance(BookingConfirmed.this).setString("photographer_longitude", "" + photographerLongitude);
                                AppPreference.getInstance(BookingConfirmed.this).setString("arriving_time", "" + arrivingTime);
                                startActivity(intent);
                                finish();
                            } else {
                                Intent intent = new Intent(BookingConfirmed.this, MenuScreen.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK
                                        | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                                finish();
                            }
                        } else if (object.optString(Constants.SUCCESS).equalsIgnoreCase(Constants.FALSE)) {
                            ErrorUtils.showFalseMessage(object, BookingConfirmed.this);
                        }
                    } else if (response.code() == 400 || response.code() == 500 || response.code() == 403 || response.code() == 404 || response.code() == 401) {
                        if (response.code() == 401) {
                            Constants.showSessionExpireAlert(BookingConfirmed.this);
                        } else {
                            Constants.showToastAlert(ErrorUtils.getHtttpCodeError(response.code()), BookingConfirmed.this);
                        }

                    } else {
                        String responseStr = ErrorUtils.getResponseBody(response);
                        JSONObject jsonObject = new JSONObject(responseStr);
                        Constants.showToastAlert(ErrorUtils.checkJosnErrorBody(jsonObject), BookingConfirmed.this);
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


}
