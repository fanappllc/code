package com.fanphotographer.activity;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.FileProvider;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.fanphotographer.AppController;
import com.fanphotographer.R;
import com.fanphotographer.data.constant.Constants;
import com.fanphotographer.data.preference.AppPreference;
import com.fanphotographer.fcm.MyFirebaseMessagingService;
import com.fanphotographer.helper.RingProgressBar;
import com.fanphotographer.helper.TimerService;
import com.fanphotographer.utility.AppUtils;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;
import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;


public class PhotoShootStarted extends BaseActivity implements View.OnClickListener {

    private static final int CAMERA_REQUEST = 2;
    private Timer timer;
    private TextView imageViewLoading;
    private TextView showText;
    private FloatingActionButton cemaraFloatButton;
    private BroadcastReceiver broadcastReceiver;
    private File mFile;
    private String profilePic;
    private String obj;
    private String notificationId;
    private String mTime;
    private String showTime;
    private long time = 0;
    private long status = 0;
    private String accessToken;
    private String orderId;
    private String orderSlotid;
    private Uri mImageUri;
    private String imageFilePath;


    private long killTimerTime;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_shoot_started);
        mTime = getIntent().getStringExtra(Constants.NEW_SLOT_TIME);
        showTime = appPreference.getString(Constants.MTIME);
        profilePic = getIntent().getStringExtra(Constants.CUSTOMER_PROFILE_IMAGE);

        if (getIntent().hasExtra(Constants.NOTIFICATION_ID)) {
            notificationId = getIntent().getStringExtra(Constants.NOTIFICATION_ID);
        }
        setToolbar();
        intView();
        setClicks();
        register();


        obj = appPreference.getString(Constants.REQUEST_END_SESSION);
        appPreference.setString("customer_json", "");



        if (!Constants.isStringNullOrBlank(obj)) {
            reomvenotification(notificationId);
            broadcastAction();
        }



    }


    @Override
    protected void onResume() {
        super.onResume();

        if (AppUtils.isServiceRunning(PhotoShootStarted.this, TimerService.class)) {
            stopService(new Intent(PhotoShootStarted.this, TimerService.class));
        }
        cancelTimer();
        TimerService.canceltimer();

        String comeFrom = appPreference.getString(Constants.START_PHOTO);
        if (getIntent().hasExtra(Constants.SLOT_TIME)) {
            String slotTime = getIntent().getStringExtra("slot_time");
            String notification_id = getIntent().getStringExtra(Constants.NOTIFICATION_ID);
            long extandTime = appPreference.getLongValue(Constants.TIMER_KEY);
            stopService(new Intent(PhotoShootStarted.this, TimerService.class));
            cancelTimer();
            TimerService.canceltimer();
            ringProgress(Long.parseLong(slotTime) * 60 + extandTime, "");
            updateView(slotTime);
            if (!Constants.isStringNullOrBlank(notification_id)) {
                MyFirebaseMessagingService.clearNotification(PhotoShootStarted.this, Integer.valueOf(notification_id));
            }
        } else {
            if (comeFrom.equals("1")) {
                try {
                    String currentTime = AppUtils.getCurrentTime();
                    String previousTime = appPreference.getString("currentTime");
                    if(!previousTime.equals("")){
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        Date date_current = simpleDateFormat.parse(currentTime);
                        Date date_previous = simpleDateFormat.parse(previousTime);

                        long newTime = appPreference.getLongValue("KILLTIMENEW");

                        long finalTime =  AppUtils.printDifference(date_current, date_previous,newTime);
                        long maintime = finalTime/1000;


                        ringProgress(maintime, "renew");
                    }


                }catch (Exception e){
                    e.printStackTrace();
                }
            } else {
                if (!Constants.isStringNullOrBlank(mTime)) {
                    appPreference.setString(Constants.START_PHOTO, "1");
                    long newTimeSecond = Long.parseLong(mTime) * 60;
                    ringProgress(newTimeSecond, "");
                }
            }
        }

    }


    @Override
    protected void onPause() {
        super.onPause();
        if(timer!=null) {
            appPreference.setString(Constants.START_PHOTO, "1");
            String currenttime = AppUtils.getCurrentTime();
            Log.e("time.....", "" + currenttime);
            killTimerTime = AppPreference.getInstance(getApplicationContext()).getLongValue("timer_key");
            AppPreference.getInstance(getApplicationContext()).setLongValue("KILLTIMENEW", killTimerTime);
            AppPreference.getInstance(getApplicationContext()).setString("currentTime", currenttime);
        }
    }

    private void intView() {
        cemaraFloatButton = (FloatingActionButton) findViewById(R.id.cemara_floatButton);
        showText = (TextView) findViewById(R.id.show_text);
        showText.setText("Session will end after " + showTime + " minutes, or user can end it anytime");
        CircleImageView profileImage = (CircleImageView) findViewById(R.id.profile_image);
        if (!Constants.isStringNullOrBlank(profilePic)) {


            Glide.with(PhotoShootStarted.this).load(profilePic)
                    .thumbnail(0.5f)
                    .apply(new RequestOptions().placeholder(R.mipmap.defult_user).dontAnimate())
                    .apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL))
                    .into(profileImage);

//            Glide.with(PhotoShootStarted.this).load(profilePic)
//                    .thumbnail(0.5f)
//                    .placeholder(R.mipmap.defult_user).dontAnimate()
//                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
//                    .listener(new RequestListener<String, GlideDrawable>() {
//                        @Override
//                        public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
//                            return false;
//                        }
//
//                        @Override
//                        public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
//                            return false;
//                        }
//                    }).into(profileImage);
        }
    }

    private void setClicks() {
        cemaraFloatButton.setOnClickListener(this);
    }

    private void setToolbar() {

        ImageView imageViewenu = (ImageView) findViewById(R.id.imageView_menu);
        TextView headerTextView = (TextView) findViewById(R.id.hedertextview);
        ImageView toolbarbackpress = (ImageView) findViewById(R.id.toolbarbackpress);
        toolbarbackpress.setVisibility(View.GONE);
        imageViewenu.setVisibility(View.GONE);
        headerTextView.setText(getResources().getString(R.string.photo_shoot));
        toolbarbackpress.setOnClickListener(this);

    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    public void register() {
        IntentFilter intentFilter = new IntentFilter("ACTION_REFRESH_USER.intent.MAIN");
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String REQUEST = intent.getStringExtra(Constants.REQUEST);
                if (!Constants.isStringNullOrBlank(REQUEST)) {

                    if (REQUEST.equals(Constants.REQUEST_END_SESSION)) {
                        obj = intent.getStringExtra("jsonObject");
                        notificationId = intent.getStringExtra("notification_id");
                        reomvenotification(notificationId);
                        broadcastAction();
                    } else if (REQUEST.equals(Constants.RENEW_SESSION_TIME)) {
                        String slot_time = intent.getStringExtra(Constants.SLOT_TIME);
                        String notification_id = intent.getStringExtra(Constants.NOTIFICATION_ID);
                        long extandTime = time / 1000;
                        stopService(new Intent(PhotoShootStarted.this, TimerService.class));
                        cancelTimer();
                        TimerService.canceltimer();
                        ringProgress(Long.parseLong(slot_time) * 60 + extandTime, "");
                        updateView(slot_time);
                        reomvenotification(notification_id);
                    }
                }
            }
        };
        registerReceiver(broadcastReceiver, intentFilter);
    }

    public void broadcastAction() {
        if (!Constants.isStringNullOrBlank(obj)) {
            Intent sentintent = new Intent(PhotoShootStarted.this, BillDetailActivity.class);
            sentintent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            sentintent.putExtra("jsonObject", obj);
            TimerService.canceltimer();
            stopService(new Intent(PhotoShootStarted.this, TimerService.class));
            appPreference.setString(Constants.START_PHOTO, "0");
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            startActivity(sentintent);
        }
    }

    public void updateView(String time) {
        if (!Constants.isStringNullOrBlank(time)) {
            if (!Constants.isStringNullOrBlank(showTime)) {
                int preSl = Integer.parseInt(showTime);
                int sl = Integer.parseInt(time);
                int finSl = preSl + sl;
                showText.setText("Session will end after " + finSl + " minutes, or user can end it anytime");
                appPreference.setString("mtime", "" + finSl);
            }
        }
    }

    public void reomvenotification(String id) {
        if (!Constants.isStringNullOrBlank(id)) {
            MyFirebaseMessagingService.clearNotification(PhotoShootStarted.this, Integer.valueOf(id));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastReceiver);
    }

    private void ringProgress(long wtime, String renew) {
        if (renew.equals("")) {
            appPreference.setLongValue("statusKilled", wtime);
        }

        Intent serviceintent = new Intent(PhotoShootStarted.this, TimerService.class);
        startService(serviceintent);
        appPreference.setLongValue("timer_key", wtime);
        final RingProgressBar mRingProgressBar = (RingProgressBar) findViewById(R.id.progress_bar_1);
        imageViewLoading = (TextView) findViewById(R.id.imageView_loading);

        mRingProgressBar.setMax((int) (appPreference.getLongValue("statusKilled")));
        status = (wtime);
        time = status * 1000;
        try {
            if (renew.equals("")) {
                mRingProgressBar.setProgress((int) status);
            } else {
                mRingProgressBar.setProgress((int) status);
            }

        }catch (Exception e){
            e.printStackTrace();
        }
        timer = new Timer();
        timer.schedule(new TimerTask() {

            @Override
            public void run() {

                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {

                        try {

                            if(timer!=null) {

                                if (time == 0 || time < 0) {
                                    cancelTimer();
                                    TimerService.canceltimer();
                                    stopService(new Intent(PhotoShootStarted.this, TimerService.class));

                                }else {
                                    status = status - 1;
                                    mRingProgressBar.setProgress((int) status);
                                    time = time - 1000;
                                    String text = String.format("%02d:%02d:%02d",
                                            TimeUnit.MILLISECONDS.toHours(time) - TimeUnit.HOURS.toHours(TimeUnit.MILLISECONDS.toDays(time)),
                                            TimeUnit.MILLISECONDS.toMinutes(time) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(time)),
                                            TimeUnit.MILLISECONDS.toSeconds(time) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(time)));
                                    imageViewLoading.setText(text);

                                }
                            }

                        } catch (Exception e) {
                            cancelTimer();
                        }
                    }
                });
            }

        }, 0, 1000);
    }

    public void cancelTimer() {
        try {
            if (timer != null) {
                timer.cancel();
                timer = null;
            }
        } catch (Exception e) {
            Log.e(Constants.LOG_CAT,e.getMessage());
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.toolbarbackpress:
                onBackPressed();
                break;

            case R.id.cemara_floatButton:
                checkRequiredPermission(Constants.MEDIA_PERMISSION);
                break;
        }
    }

    @Override
    public void invokedWhenNoOrAllreadyPermissionGranted() {
        super.invokedWhenNoOrAllreadyPermissionGranted();
        pickImagedialog();
    }

    @Override
    public void invokedWhenPermissionGranted() {
        super.invokedWhenPermissionGranted();
        pickImagedialog();
    }




    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CAMERA_REQUEST) {
            if (resultCode == RESULT_OK) {

                File temp = null;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    temp = new File(imageFilePath);
                } else {
                    temp = new File(mImageUri.getPath());
            }

                File _mFile = null;
                try {
                    _mFile = new Compressor(this).compressToFile(temp);

                } catch (Exception e) {
                    Log.e(Constants.LOG_CAT,e.getMessage());
                }
                setfile(_mFile);
                pickImagefromcamera();
                accessToken = appPreference.getString(Constants.ACCESS_TOKEN);
                orderId = appPreference.getString(Constants.ORDER_ID);
                orderSlotid = appPreference.getString(Constants.ORDER_SLOT_ID);
                uploadPhoto();

            }
        }
    }



    public void setfile(File file) {
        this.mFile = file;
    }

    public File getFile() {
        return mFile;
    }

    @Override
    public void onBackPressed() {
    }

    public void pickImagedialog() {

        final Dialog slidDialog = new Dialog(PhotoShootStarted.this);
        slidDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        slidDialog.setCancelable(false);
        slidDialog.setCanceledOnTouchOutside(false);
        slidDialog.setContentView(R.layout.dialog_edit_pic);
        slidDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        slidDialog.getWindow().getAttributes().windowAnimations = R.style.SmileWindow;

        RelativeLayout takePhotoRelative = (RelativeLayout) slidDialog.findViewById(R.id.takePhotoRelative);
        RelativeLayout photoRelativeLayout = (RelativeLayout) slidDialog.findViewById(R.id.photoRelativeLayout);
        TextView btnCancel = (TextView) slidDialog.findViewById(R.id.btn_cancel);

        takePhotoRelative.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                slidDialog.dismiss();
                if (AppUtils.isNetworkConnected()) {

                    pickImagefromcamera();
                } else {
                    final ViewGroup viewGroup = (ViewGroup) ((ViewGroup) PhotoShootStarted.this.findViewById(android.R.id.content)).getChildAt(0);
                    Constants.showSnackbar(PhotoShootStarted.this, viewGroup, getResources().getString(R.string.no_internet), "Retry");
                }

            }
        });


        photoRelativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                slidDialog.dismiss();
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                slidDialog.dismiss();

            }
        });

        slidDialog.show();
    }

    private void pickImagefromcamera() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Intent pictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (pictureIntent.resolveActivity(getPackageManager()) != null) {
                File photoFile = null;
                try {
                    photoFile = createImagefile();
                } catch (Exception ex) {
                    // Error occurred while creating the File
                }
                if (photoFile != null) {
                    mImageUri = FileProvider.getUriForFile(this, "com.fanphotographer.provider", photoFile);
                    pictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri);
                    startActivityForResult(pictureIntent, CAMERA_REQUEST);
                }
            }
        } else {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            mImageUri = getOutputmediafileuri(MEDIA_TYPE_IMAGE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri);
            startActivityForResult(intent, CAMERA_REQUEST);
        }
    }

    public void uploadPhoto() {
        if (mFile != null) {
            AppController.getInstance().upload(accessToken, orderId, orderSlotid, getFile());
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("file_uri", mImageUri);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mImageUri = savedInstanceState.getParcelable("file_uri");
    }

    private File createImagefile() throws IOException {

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), Constants.IMAGE_DIRECTORY_NAME);
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d(Constants.IMAGE_DIRECTORY_NAME, "Oops! Failed create " + Constants.IMAGE_DIRECTORY_NAME + " directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        File mediaFile;
        mediaFile = new File(mediaStorageDir.getPath() + File.separator + "IMG_" + timeStamp + ".jpg");
        imageFilePath=mediaFile.getAbsolutePath();
        return mediaFile;


    }

    public Uri getOutputmediafileuri(int type) {
        return Uri.fromFile(getOutputmediafile(type));
    }


    private static File getOutputmediafile(int type) {
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), Constants.IMAGE_DIRECTORY_NAME);
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d(Constants.IMAGE_DIRECTORY_NAME, "Oops! Failed create " + Constants.IMAGE_DIRECTORY_NAME + " directory");
                return null;
            }
        }
        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator + "IMG_" + timeStamp + ".jpg");
        } else {
            return null;
        }

        return mediaFile;
    }
}