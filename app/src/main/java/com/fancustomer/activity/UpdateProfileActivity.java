package com.fancustomer.activity;


import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;

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
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;


import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.bumptech.glide.request.target.Target;
import com.fancustomer.R;

import com.fancustomer.data.constant.Constants;
import com.fancustomer.data.preference.AppPreference;

import com.fancustomer.helper.ErrorUtils;
import com.fancustomer.helper.ExceptionHandler;

import com.fancustomer.webservice.Api;
import com.fancustomer.webservice.ApiFactory;
import com.theartofdev.edmodo.cropper.CropImage;


import org.json.JSONException;
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


public class UpdateProfileActivity extends BaseActivity implements View.OnClickListener {

    private String photoFileName = "";
    private ProgressBar progressBar;
    private File mFile;
    private String isUserAccountAdded = "";
    private String deviceModel;
    private TextView txtSubmitDetail;
    private EditText firstNameedittext;
    private EditText lastNameEditText;
    private EditText addressEditText;
    private EditText emailIdEditText;
    private EditText zipCodeEditText;
    private String firstName = "";
    private String lastName = "";
    private String emailId = "";
    private String profileImage = "";
    private String address;
    private String zipCode = "";
    private String mobile = "";
    private CircleImageView userImageView;
    private RelativeLayout rlUserImage;
    private String accessToken = "";
    private String comeFrom = "";

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_profile);
        accessToken = AppPreference.getInstance(UpdateProfileActivity.this).getString(Constants.ACCESS_TOKEN);
        getParam();
        setToolBar();
        initView();
        setClicks();


        txtSubmitDetail.setOnClickListener(listener);
    }

    private void getParam() {
        comeFrom = getIntent().getStringExtra("COME_FROM");
        mobile = getIntent().getStringExtra("mobile");
        if (comeFrom.equals("profileFragment")) {
            firstName = getIntent().getStringExtra("first_name");
            lastName = getIntent().getStringExtra("last_name");
            emailId = getIntent().getStringExtra("email");
            profileImage = getIntent().getStringExtra("profile_image");
            zipCode = getIntent().getStringExtra("zip_code");
            address = getIntent().getStringExtra("address");
        }


    }

    private void initView() {

        deviceModel = android.os.Build.MODEL;
        RelativeLayout toolBarLeft = (RelativeLayout) findViewById(R.id.toolBarLeft);
        toolBarLeft.setOnClickListener(listener);
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        TextView mobileTextView = (TextView) findViewById(R.id.mobileTextView);
        txtSubmitDetail = (TextView) findViewById(R.id.txtSubmitDetail);
        firstNameedittext = (EditText) findViewById(R.id.first_nameEditText);
        lastNameEditText = (EditText) findViewById(R.id.last_nameEditText);
        emailIdEditText = (EditText) findViewById(R.id.email_idEditText);
        addressEditText = (EditText) findViewById(R.id.addressEditText);
        zipCodeEditText = (EditText) findViewById(R.id.zip_codeEditText);
        userImageView = (CircleImageView) findViewById(R.id.userImageView);
        rlUserImage = (RelativeLayout) findViewById(R.id.rl_user_image);


        firstNameedittext.setText(firstName);
        lastNameEditText.setText(lastName);
        emailIdEditText.setText(emailId);
        addressEditText.setText(address);
        zipCodeEditText.setText(zipCode);
        mobileTextView.setText(mobile);
        if (!profileImage.equals("")) {
            Glide.with(UpdateProfileActivity.this).load(profileImage)
                    .thumbnail(0.5f)
                    .placeholder(R.mipmap.defult_user).dontAnimate()
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .listener(new RequestListener<String, GlideDrawable>() {
                        @Override
                        public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                            progressBar.setVisibility(View.GONE);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                            progressBar.setVisibility(View.GONE);
                            return false;
                        }
                    }).into(userImageView);
        }


    }

    private void setClicks() {
        rlUserImage.setOnClickListener(this);
    }


    private void setToolBar() {
        RelativeLayout toolBarLeft = (RelativeLayout) findViewById(R.id.toolBarLeft);
        TextView headerTextView = (TextView) findViewById(R.id.hedertextview);
        headerTextView.setText(UpdateProfileActivity.this.getResources().getString(R.string.profile_detail));
        toolBarLeft.setOnClickListener(listener);
    }

    View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            int i = view.getId();
            if (i == R.id.toolBarLeft) {
                finish();
            } else if (i == R.id.txtSubmitDetail) {
                checkAllFields();
            }
        }
    };

    private void checkAllFields() {

        firstName = firstNameedittext.getText().toString().trim();
        lastName = lastNameEditText.getText().toString().trim();
        emailId = emailIdEditText.getText().toString().trim();
        address = addressEditText.getText().toString().trim();
        zipCode = zipCodeEditText.getText().toString().trim();
        if (!firstName.equalsIgnoreCase("")) {
            if (!lastName.equalsIgnoreCase("")) {
                if (!emailId.equalsIgnoreCase("")) {
                    if (Constants.isValidEmail(emailId)) {
                        if (!address.equalsIgnoreCase("")) {
                            if (!zipCode.equalsIgnoreCase("")) {
                                if (Constants.isInternetOn(UpdateProfileActivity.this)) {
                                    updateProfileApi();

                                } else {
                                    final ViewGroup viewGroup = (ViewGroup) ((ViewGroup) UpdateProfileActivity.this.findViewById(android.R.id.content)).getChildAt(0);
                                    showSnackbar(viewGroup, getResources().getString(R.string.no_internet), Constants.RETRY);
                                }


                            } else {
                                Constants.showToastAlert(getResources().getString(R.string.please_enter_zip_code), UpdateProfileActivity.this);
                            }

                        } else {
                            Constants.showToastAlert(getResources().getString(R.string.please_enter_address), UpdateProfileActivity.this);
                        }

                    } else {
                        Constants.showToastAlert(getResources().getString(R.string.enter_valid_email_id), UpdateProfileActivity.this);
                    }

                } else {
                    Constants.showToastAlert(getResources().getString(R.string.please_enter_email_address), UpdateProfileActivity.this);
                }

            } else {
                Constants.showToastAlert(getResources().getString(R.string.please_enter_last_name), UpdateProfileActivity.this);
            }

        } else {
            Constants.showToastAlert(getResources().getString(R.string.please_enter_first_name), UpdateProfileActivity.this);
        }

    }


    public void updateProfileApi() {

        Map<String, RequestBody> map = new HashMap<>();
        if (getFile() != null) {
            Uri selectedUri = Uri.fromFile(getFile());
            System.out.println("====selectedUri==" + selectedUri);
            String fileExtension = MimeTypeMap.getFileExtensionFromUrl(selectedUri.toString());
            String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension);
            RequestBody reqBodyImageOfUser = RequestBody.create(MediaType.parse(mimeType), getFile());
            map.put("profile_image\"; filename=\"" + getFile().getName() + "\"", reqBodyImageOfUser);
            Log.e(Constants.LOG_CAT, "FanEditProfile: image============>>>>>>>>>>>>" + map.toString());
        }
        RequestBody firstNameStr = RequestBody.create(MediaType.parse("multipart/form-data"), firstName);
        RequestBody lastNameStr = RequestBody.create(MediaType.parse("multipart/form-data"), lastName);
        RequestBody email = RequestBody.create(MediaType.parse("multipart/form-data"), emailId);
        RequestBody mobileModal = RequestBody.create(MediaType.parse("multipart/form-data"), deviceModel);
        RequestBody isTermsConditionAccepted = RequestBody.create(MediaType.parse("multipart/form-data"), "1");
        RequestBody zipCodeStr = RequestBody.create(MediaType.parse("multipart/form-data"), zipCode);
        RequestBody addressStr = RequestBody.create(MediaType.parse("multipart/form-data"), address);
        Api api = ApiFactory.getClientWithoutHeader(UpdateProfileActivity.this).create(Api.class);
        Call<ResponseBody> call;
        Log.e(Constants.LOG_CAT, "updateProfileApi:DeviceModel================ " + deviceModel);
        map.put("first_name", firstNameStr);
        map.put("last_name", lastNameStr);
        map.put("email", email);
        map.put("mobile_model", mobileModal);
        map.put("is_terms_condition_accepted", isTermsConditionAccepted);
        map.put("zip_code", zipCodeStr);
        map.put("address", addressStr);

        accessToken = AppPreference.getInstance(UpdateProfileActivity.this).getString(Constants.ACCESS_TOKEN);
        call = api.updateProfile(accessToken, map);
        Log.e(Constants.LOG_CAT, "API FAN Profile------------------->>>>>:" + map + " " + call.request().url());
        Log.e(Constants.LOG_CAT, "HEADERS : " + call.request().headers());


        Constants.showProgressDialog(UpdateProfileActivity.this, Constants.LOADING);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Constants.hideProgressDialog();
                try {
                    if (response.isSuccessful()) {
                        String output = ErrorUtils.getResponseBody(response);
                        JSONObject object = new JSONObject(output);
                        Log.d("tag", object.toString(1));
                        if (object.optString("success").equalsIgnoreCase("true")) {
                            Log.e(Constants.LOG_CAT, "FANCUSTOMER UPDATE Profile==============>>>>>" + object.toString());
                            JSONObject jsonObject = object.optJSONObject("data");
                            String customerId = jsonObject.optString("customer_id");
                            isUserAccountAdded = jsonObject.optString("is_user_account_added");
                            AppPreference.getInstance(UpdateProfileActivity.this).setString(Constants.IS_PROFILE_UPDATE, jsonObject.optString("is_profile_updated"));
                            AppPreference.getInstance(UpdateProfileActivity.this).setString(Constants.CUSTOMER_ID, customerId);
                            AppPreference.getInstance(UpdateProfileActivity.this).setString(Constants.FIRST_NAME, jsonObject.optString("first_name"));
                            AppPreference.getInstance(UpdateProfileActivity.this).setString(Constants.LAST_NAME, jsonObject.optString("last_name"));
                            AppPreference.getInstance(UpdateProfileActivity.this).setString(Constants.MOBILE, jsonObject.optString("mobile"));
                            AppPreference.getInstance(UpdateProfileActivity.this).setString(Constants.EMAIL, jsonObject.optString("email"));
                            AppPreference.getInstance(UpdateProfileActivity.this).setString(Constants.PROFILE_PIC, jsonObject.optString("profile_image"));
                            editSucccesDialog();

                        } else if (object.optString(Constants.SUCCESS).equalsIgnoreCase(Constants.FALSE)) {
                            ErrorUtils.showFalseMessage(object, UpdateProfileActivity.this);


                        }
                    } else if (response.code() == 400 || response.code() == 500 || response.code() == 403 || response.code() == 404 || response.code() == 401) {
                        if (response.code() == 401) {
                            Constants.showSessionExpireAlert(UpdateProfileActivity.this);
                        } else {
                            Constants.showToastAlert(ErrorUtils.getHtttpCodeError(response.code()), UpdateProfileActivity.this);
                        }

                    } else {
                        String responseStr = ErrorUtils.getResponseBody(response);
                        JSONObject jsonObject = new JSONObject(responseStr);
                        Constants.showToastAlert(ErrorUtils.checkJosnErrorBody(jsonObject), UpdateProfileActivity.this);
                    }
                } catch (Exception e) {
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
    public void onBackPressed() {
        super.onBackPressed();
        finish();

    }

    @Override
    protected void networkConnnectivityChange() {
        super.networkConnnectivityChange();
        if (!Constants.isInternetOn(UpdateProfileActivity.this)) {
            final ViewGroup viewGroup = (ViewGroup) ((ViewGroup) UpdateProfileActivity.this.findViewById(android.R.id.content)).getChildAt(0);
            showSnackbar(viewGroup, getResources().getString(R.string.no_internet), Constants.RETRY);
        } else {
            hideSnackbar();
        }
    }

    private void editSucccesDialog() {
        final Dialog dialog = new Dialog(UpdateProfileActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.dialog_one_button);
        TextView textviewHeader = (TextView) dialog.findViewById(R.id.tv_header);
        TextView textviewMessages = (TextView) dialog.findViewById(R.id.tv_messages);
        TextView buttonOk = (TextView) dialog.findViewById(R.id.button_ok);
        textviewHeader.setText("SUCCESS");
        textviewMessages.setText("Your profile has been updated successfully!");
        buttonOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                if (comeFrom.equals("profileFragment")) {
                    Intent intent = new Intent();
                    setResult(RESULT_OK, intent);
                    finish();
                } else {
                    if (isUserAccountAdded.equals("0")) {
                        Intent intent = new Intent(UpdateProfileActivity.this, AddCreditCardActivity.class);
                        intent.putExtra("COME_ADDCARD", "OTHER");
                        startActivity(intent);
                    }
                }
            }
        });
        dialog.show();

    }


    public void pickImageDialog() {
        final Dialog slidDialog = new Dialog(UpdateProfileActivity.this);
        slidDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        slidDialog.setCancelable(false);
        slidDialog.setCanceledOnTouchOutside(false);
        slidDialog.setContentView(R.layout.dialog_edit_pic);
        slidDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        slidDialog.getWindow().getAttributes().windowAnimations = R.style.SmileWindow;
        RelativeLayout takePhotoRelative = slidDialog.findViewById(R.id.takePhotoRelative);
        RelativeLayout photoRelativeLayout = slidDialog.findViewById(R.id.photoRelativeLayout);
        TextView btnCancel = slidDialog.findViewById(R.id.btn_cancel);
        takePhotoRelative.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickImageFromCamera();
                slidDialog.dismiss();
            }
        });
        photoRelativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickImageFromGallery();
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

    @Override
    public void invokedWhenNoOrAllreadyPermissionGranted() {
        super.invokedWhenNoOrAllreadyPermissionGranted();
        pickImageDialog();
    }

    @Override
    public void invokedWhenPermissionGranted() {
        super.invokedWhenPermissionGranted();
        pickImageDialog();
    }


    public void setfile(File file) {
        this.mFile = file;
    }

    public File getFile() {
        return mFile;
    }


    public void pickImageFromGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, Constants.PICK_IMAGE_REQUEST);

    }

    public void pickImageFromCamera() {

        try {
            photoFileName = "";
            photoFileName = photoFileName + System.currentTimeMillis() + ".jpg";
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, getPhotoFileUri(photoFileName));

            List<ResolveInfo> resInfoList = getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
            for (ResolveInfo resolveInfo : resInfoList) {
                String packageName = resolveInfo.activityInfo.packageName;
                grantUriPermission(packageName, getPhotoFileUri(photoFileName), Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }

            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(intent, Constants.CAMERA_REQUEST);
            }
        } catch (Exception e) {
            ExceptionHandler.printStackTrace(e);
        }


    }

    public Uri getPhotoFileUri(String fileName) {
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
            Uri takenPhotoUri = getPhotoFileUri(photoFileName);
            CropImage.activity(takenPhotoUri).setFixAspectRatio(false).setAspectRatio(5, 5).start(this);

        } else if (requestCode == Constants.PICK_IMAGE_REQUEST && resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            CropImage.activity(selectedImage).setFixAspectRatio(false).setAspectRatio(5, 5).start(this);
        }
        switch (requestCode) {
            case CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE:
                CropImage.ActivityResult result = CropImage.getActivityResult(data);
                if (resultCode == RESULT_OK) {
                    Uri resultUri = result.getUri();
                    File file = new File(resultUri.getPath());
                    setfile(file);
                    String photoPath = resultUri.getPath();
                    Glide.with(UpdateProfileActivity.this).load(photoPath).asBitmap().centerCrop().into(new BitmapImageViewTarget(userImageView) {
                        @Override
                        protected void setResource(Bitmap resource) {
                            RoundedBitmapDrawable circularBitmapDrawable =
                                    RoundedBitmapDrawableFactory.create(getResources(), resource);
                            circularBitmapDrawable.setCircular(true);
                            userImageView.setImageDrawable(circularBitmapDrawable);
                        }
                    });
                } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                    Log.e(Constants.LOG_CAT, "resultCode: ");
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onClick(View view) {
        int i = view.getId();
        if (i == R.id.rl_user_image) {
            checkRequiredPermission(Constants.MEDIA_PERMISSION);
        } else if (i == R.id.toolBarLeft) {
            onBackPressed();
        }

    }
}
