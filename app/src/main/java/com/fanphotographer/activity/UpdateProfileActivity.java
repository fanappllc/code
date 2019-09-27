package com.fanphotographer.activity;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.fanphotographer.R;
import com.fanphotographer.data.constant.Constants;
import com.fanphotographer.utility.AppUtils;
import com.fanphotographer.utility.ErrorUtils;
import com.fanphotographer.utility.KeyboardUtils;
import com.fanphotographer.webservice.Api;
import com.fanphotographer.webservice.ApiFactory;
import com.theartofdev.edmodo.cropper.CropImage;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;


public class UpdateProfileActivity extends BaseActivity {

    private TextView headerTextView;
    private EditText firstNameEditText;
    private EditText lastNameEditText;
    private EditText emailEditText;
    private EditText addressEditText;
    private EditText zipCodeEditText;
    private EditText mobileModalEditText;
    private String firstNameStr = "";
    private String lastNameStr = "";
    private String emailStr = "";
    private String addressStr = "";
    private String zipCodeStr = "";
    private CircleImageView userImageView;
    private Context mContext = this;
    private String photoFileName = "";
    private File file;
    private Uri resultUri;
    private File mFile;
    private BroadcastReceiver broadcastReceiver;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_profile);
        setToolbar();
        initView();
    }

    private void initView() {

        firstNameEditText = (EditText) findViewById(R.id.firstNameEditText);
        lastNameEditText = (EditText) findViewById(R.id.lastNameEditText);
        emailEditText = (EditText) findViewById(R.id.emailEditText);
        addressEditText = (EditText) findViewById(R.id.addressEditText);
        zipCodeEditText = (EditText) findViewById(R.id.zipCodeEditText);
        mobileModalEditText = (EditText) findViewById(R.id.mobileModalEditText);
        TextView txtSubmitDetail = (TextView) findViewById(R.id.txtSubmitDetail);
        TextView verifyMobile = (TextView) findViewById(R.id.verify_mobile);
        RelativeLayout rlUserimage = (RelativeLayout) findViewById(R.id.rl_user_image);
        userImageView = (CircleImageView) findViewById(R.id.userImageView);
        txtSubmitDetail.setOnClickListener(listener);
        rlUserimage.setOnClickListener(listener);

        String model = Build.BRAND + " " + Build.MODEL;



        verifyMobile.setText(appPreference.getString(Constants.COUNTRY_CODE)+" "+appPreference.getString(Constants.MOBILE));
        if(!model.equalsIgnoreCase("")){
            mobileModalEditText.setText(model);
        }else {
            mobileModalEditText.setText(appPreference.getString(Constants.MOBILE_MODEL));
        }

        if(appPreference.getString(Constants.IS_USER_REGISTERED).equalsIgnoreCase("0")){
            headerTextView.setText(UpdateProfileActivity.this.getResources().getString(R.string.profile_detail));
            txtSubmitDetail.setText(UpdateProfileActivity.this.getResources().getString(R.string.submit));
        }else {
            firstNameEditText.setText(appPreference.getString(Constants.FIRST_NAME));
            lastNameEditText.setText(appPreference.getString(Constants.LAST_NAME));
            emailEditText.setText(appPreference.getString(Constants.EMAIL));
            addressEditText.setText(appPreference.getString(Constants.ADDRESS));
            zipCodeEditText.setText(appPreference.getString(Constants.ZIP_CODE));
            showProfile(appPreference.getString(Constants.PROFILE_IMAGE));
            headerTextView.setText(UpdateProfileActivity.this.getResources().getString(R.string.profile_edit));
            txtSubmitDetail.setText(UpdateProfileActivity.this.getResources().getString(R.string.save));
        }
    }

    private void setToolbar() {
        RelativeLayout toolBarLeft = (RelativeLayout) findViewById(R.id.toolBarLeft);
        headerTextView = (TextView) findViewById(R.id.hedertextview);
        toolBarLeft.setOnClickListener(listener);
    }

    private void setValidation() {
        KeyboardUtils.hideKeyboard(UpdateProfileActivity.this);
        firstNameStr = firstNameEditText.getText().toString().trim();
        lastNameStr = lastNameEditText.getText().toString().trim();
        emailStr = emailEditText.getText().toString().trim();
        addressStr = addressEditText.getText().toString().trim();
        zipCodeStr = zipCodeEditText.getText().toString().trim();
        String mobileStr = mobileModalEditText.getText().toString().trim();

        if (file == null ){
            Constants.showToastAlert(getResources().getString(R.string.user_pic_to), mContext);
        }else if (!file.exists()) {
            Constants.showToastAlert(getResources().getString(R.string.user_pic_to), mContext);
        } else if (firstNameStr.equals("")) {
            Constants.showToastAlert(getResources().getString(R.string.Please_enter_first_name), mContext);
        } else if (lastNameStr.equals("")) {
            Constants.showToastAlert(getResources().getString(R.string.Please_enter_last_name), mContext);
        } else if (emailStr.equals("")) {
            Constants.showToastAlert(getResources().getString(R.string.Please_enter_email_address), mContext);
        } else if (!Constants.isValidEmail(emailStr)) {
            Constants.showToastAlert(getResources().getString(R.string.enter_valid_email_id), mContext);
        } else if (addressStr.equals("")) {
            Constants.showToastAlert(getResources().getString(R.string.Please_enter_address), mContext);
        } else if (zipCodeStr.equals("")) {
            Constants.showToastAlert(getResources().getString(R.string.Please_enter_zip_code), mContext);
        } else if (mobileStr.equals("")) {
            Constants.showToastAlert(getResources().getString(R.string.Please_enter_mobile_modal), mContext);
        } else {

            if (AppUtils.isNetworkConnected()) {
                updateProfileapi();
            } else {
                final ViewGroup viewGroup = (ViewGroup) ((ViewGroup) UpdateProfileActivity.this.findViewById(android.R.id.content)).getChildAt(0);
                Constants.showSnackbar(mContext, viewGroup, getResources().getString(R.string.no_internet), "Retry");
            }
        }

    }




    View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            int i = view.getId();
            if (i == R.id.toolBarLeft) {
                finish();

            } else if (i == R.id.txtSubmitDetail) {
                setValidation();

            } else if (i == R.id.rl_user_image) {
                checkRequiredPermission(Constants.MEDIA_PERMISSION);

            }
        }
    };


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
    protected void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter("ACTION_REFRESH_USER.intent.MAIN");
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent mintent) {
                String broadCastActionType = mintent.getStringExtra(Constants.BROADCAST_ACTION);
                String request = mintent.getStringExtra(Constants.REQUEST);

                if (broadCastActionType.equals(Constants.ACTION_REFRESH_USER)) {
                    if (request.equals(Constants.SEND_REQUEST)) {
                       if(!Constants.isStringNullOrBlank(appPreference.getString(Constants.ID))){
                           Intent intent = new Intent(mContext, MenuScreen.class);
                           intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                           overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                           startActivity(intent);
                       }


                    }

                }


            }
        };
        registerReceiver(broadcastReceiver, intentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(broadcastReceiver);
    }
    public void updateProfileapi() {

        Api api = ApiFactory.getClientWithoutHeader(UpdateProfileActivity.this).create(Api.class);
        Map<String, RequestBody> map = new HashMap<String, RequestBody>();
        Call<ResponseBody> call = null;

        RequestBody firstName = RequestBody.create(MediaType.parse(Constants.MULTIPART), firstNameStr);
        RequestBody lastName = RequestBody.create(MediaType.parse(Constants.MULTIPART), lastNameStr);
        RequestBody email = RequestBody.create(MediaType.parse(Constants.MULTIPART), emailStr);
        RequestBody mobileModal = RequestBody.create(MediaType.parse(Constants.MULTIPART), Build.BRAND + Build.MODEL );
        RequestBody isTermsconditionaccepted = RequestBody.create(MediaType.parse(Constants.MULTIPART), "1");
        RequestBody zipCode = RequestBody.create(MediaType.parse(Constants.MULTIPART), zipCodeStr);
        RequestBody addressst = RequestBody.create(MediaType.parse(Constants.MULTIPART), addressStr);

        if (getFile() != null) {

            Uri selectedUri = Uri.fromFile(getFile());
            String fileExtension = MimeTypeMap.getFileExtensionFromUrl(selectedUri.toString());
            String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension);
            RequestBody reqBodyImageOfUser = RequestBody.create(MediaType.parse(mimeType), getFile());
            map.put("profile_image\"; filename=\"" + getFile().getName() + "\"", reqBodyImageOfUser);

        }

        String accessToken = appPreference.getString(Constants.ACCESS_TOKEN);
        call = api.updateProfile(accessToken, map, firstName, lastName, email, mobileModal, isTermsconditionaccepted, zipCode, addressst);
        Constants.showProgressDialog(mContext, Constants.LOADING);

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
                            String isUserregistered = jsonObject.optString("is_user_registered");
                            if (isUserregistered.equals("0")) {
                                appPreference.setString(Constants.IS_PROFILE_UPDATED, "1");
                                Intent intent = new Intent(UpdateProfileActivity.this, SsnActivity.class);
                                startActivity(intent);
//                                finish();
                            }

                        } else if (object.optString(Constants.SUCCESS).equalsIgnoreCase(Constants.FALSE)) {
                            appPreference.setString(Constants.IS_PROFILE_UPDATED, "0");
                            JSONArray jsonArray = object.optJSONArray(Constants.ERROR);
                            if (jsonArray == null) {
                                Constants.showToastAlert(object.getJSONObject(Constants.ERROR).getString(Constants.MESSAGE),mContext);
                            }else {
                                Constants.showToastAlert(jsonArray.getJSONObject(0).getString(Constants.MESSAGE), mContext);
                            }
                        }

                    }  else if (response != null) {
                        if (response.code() == 400 || response.code() == 500 || response.code() == 403 || response.code() == 404 || response.code() == 401) {
                            appPreference.setString(Constants.IS_PROFILE_UPDATED, "0");
                            if(response.code()==401){
                                appPreference.showCustomAlert(UpdateProfileActivity.this,getResources().getString(R.string.http_401_error));
                            }else {
                                Constants.showToastAlert(ErrorUtils.getHtttpCodeError(response.code()), mContext);
                            }

                        } else {
                            appPreference.setString(Constants.IS_PROFILE_UPDATED, "0");
                            String responseStr = ErrorUtils.getResponseBody(response);
                            JSONObject jsonObject = new JSONObject(responseStr);
                            Constants.showToastAlert(ErrorUtils.checkJosnErrorBody(jsonObject), mContext);
                        }
                    }
                }catch (Exception e){
                    Log.e(Constants.LOG_CAT,e.getMessage());
                }

            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Constants.hideProgressDialog();
                Constants.showToastAlert(ErrorUtils.getString(R.string.failled), mContext);
            }
        });


    }



    public void pickImagedialog() {

        final Dialog slidDialog = new Dialog(UpdateProfileActivity.this);

        slidDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        slidDialog.setCancelable(false);
        slidDialog.setCanceledOnTouchOutside(false);
        slidDialog.setContentView(R.layout.dialog_pick_image);
        slidDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        slidDialog.getWindow().getAttributes().windowAnimations = R.style.SmileWindow;

        TextView tvTakephoto = (TextView) slidDialog.findViewById(R.id.tv_take_photo);
        TextView tvPhotolibrary = (TextView) slidDialog.findViewById(R.id.tv_photo_library);
        TextView btnCancel = (TextView) slidDialog.findViewById(R.id.btn_cancel);

        tvTakephoto.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                slidDialog.dismiss();
                pickImagefromcamera();
            }
        });

        tvPhotolibrary.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                slidDialog.dismiss();
                pickImagefromgallery();
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

    public void pickImagefromcamera() {

        try {
            photoFileName = "";
            photoFileName = photoFileName + System.currentTimeMillis() + ".jpg";
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, getPhotofileuri(photoFileName));

            List<ResolveInfo> resInfoList = getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
            for (ResolveInfo resolveInfo : resInfoList) {
                String packageName = resolveInfo.activityInfo.packageName;
                grantUriPermission(packageName, getPhotofileuri(photoFileName), Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }

            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(intent, Constants.CAMERA_REQUEST);
            }
        } catch (Exception e) {
            Log.e(Constants.LOG_CAT,e.getMessage());
        }

    }

    public void setfile(File file) {
        this.mFile = file;
    }

    public File getFile() {
        return mFile;
    }


    public void pickImagefromgallery() {
            Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(galleryIntent, Constants.PICK_IMAGE_REQUEST);
    }


    public Uri getPhotofileuri(String fileName) {
        if (isExternalStorageAvailable()) {
            File mediaStorageDir = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), Constants.LOG_CAT);
            File outputFile = new File(mediaStorageDir.getPath() + File.separator + fileName);
            Uri outputFileUri = FileProvider.getUriForFile(
                    UpdateProfileActivity.this,
                    UpdateProfileActivity.this.getApplicationContext()
                            .getPackageName() + ".provider", outputFile);
            if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()) {
                Log.e(Constants.LOG_CAT, "failed to create directory");
            }
            return outputFileUri;
        }
        return null;
    }


    private boolean isExternalStorageAvailable() {
        String state = Environment.getExternalStorageState();
        return state.equals(Environment.MEDIA_MOUNTED);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.CAMERA_REQUEST && resultCode == RESULT_OK) {
            Uri takenPhotoUri = getPhotofileuri(photoFileName);
            CropImage.activity(takenPhotoUri).setFixAspectRatio(false).setAspectRatio(5, 5).start(this);

        } else if (requestCode == Constants.PICK_IMAGE_REQUEST && resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            CropImage.activity(selectedImage).setFixAspectRatio(false).setAspectRatio(5, 5).start(this);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                resultUri = result.getUri();
                file = new File(resultUri.getPath());
                setfile(file);
                String photoPath = resultUri.getPath();
                showProfile(photoPath);

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Log.e(Constants.LOG_CAT, "" + result.getError());
            }

        }
    }

    public void showProfile(String path){
        if(!Constants.isStringNullOrBlank(path)){

            Glide.with(mContext).load(path)
                    .thumbnail(0.5f)
                    .apply(new RequestOptions().placeholder(R.mipmap.defult_user).dontAnimate())
                    .apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL))
                    .into(userImageView);

//            Glide.with(UpdateProfileActivity.this).load(path).asBitmap().centerCrop().into(new BitmapImageViewTarget(userImageView) {
//                @Override protected void setResource(Bitmap resource) {
//                    RoundedBitmapDrawable circularBitmapDrawable = RoundedBitmapDrawableFactory.create(getResources(), resource);
//                    circularBitmapDrawable.setCircular(true);
//                    userImageView.setImageDrawable(circularBitmapDrawable);
//                }
//            });
        }

    }

}
