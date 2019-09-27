package com.fanphotographer.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.text.Spannable;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.fanphotographer.R;
import com.fanphotographer.data.constant.Constants;
import com.fanphotographer.utility.AppUtils;
import com.fanphotographer.utility.CustomTypefaceSpan;
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
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;


public class IdProofActivity extends BaseActivity
{

    private TextView headerTextView;
    private TextView tvheadder;
    private ImageView imageView;
    private Context mContext = this;
    private String photoFileName = "";
    private File file;
    private Uri resultUri;
    private File mFile;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_id_prrof);
        setToolBar();
        initView();
    }


    private void initView() {
        TextView tvSave = (TextView) findViewById(R.id.txtSubmitDetail);
        imageView = (ImageView) findViewById(R.id.license_img);
        tvheadder = (TextView) findViewById(R.id.txt_headder);
        TextView headerTextView = (TextView) findViewById(R.id.hedertextview);
        headerTextView.setText(IdProofActivity.this.getResources().getString(R.string.id_proof));
        tvSave.setOnClickListener(listener);
        imageView.setOnClickListener(listener);
        setTermscondition();
    }

    public void setTermscondition() {

        Typeface font = Typeface.createFromAsset(getAssets(), "fonts/Quicksand-Medium.ttf");
        String text1 = IdProofActivity.this.getResources().getString(R.string.driving_license);
        int startIndex1 = tvheadder.getText().toString().indexOf(text1);
        int endIndex1 = startIndex1 + text1.length();
        tvheadder.setMovementMethod(LinkMovementMethod.getInstance());
        tvheadder.setText(tvheadder.getText(), TextView.BufferType.SPANNABLE);
        Spannable mySpannable1 = (Spannable) tvheadder.getText();
        mySpannable1.setSpan(new ForegroundColorSpan(IdProofActivity.this.getResources().getColor(R.color.black)), startIndex1, endIndex1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        mySpannable1.setSpan (new CustomTypefaceSpan("", font), startIndex1, endIndex1, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);

    }

    private void setToolBar() {

        RelativeLayout toolBarLeft = (RelativeLayout) findViewById(R.id.toolBarLeft);
        tvheadder = (TextView) findViewById(R.id.hedertextview);
        tvheadder.setText(IdProofActivity.this.getResources().getString(R.string.id_proof));
        toolBarLeft.setOnClickListener(listener);

    }

    View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            int i = view.getId();
            if (i == R.id.toolBarLeft) {
                finish();

            } else if (i == R.id.txtSubmitDetail) {
                setValidation();

            } else if (i == R.id.license_img) {
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

    private void setValidation() {
        if (file != null && file.exists()){
            KeyboardUtils.hideKeyboard(IdProofActivity.this);
            if (AppUtils.isNetworkConnected()) {
                updateDrivinglicenceApi();
            } else {
                final ViewGroup viewGroup = (ViewGroup) ((ViewGroup) IdProofActivity.this.findViewById(android.R.id.content)).getChildAt(0);
                Constants.showSnackbar(mContext, viewGroup, getResources().getString(R.string.no_internet), "Retry");
            }
        }else {
            Constants.showToastAlert(getResources().getString(R.string.pic_to), mContext);
        }

    }



    public void pickImagedialog() {

        final Dialog slidDialog = new Dialog(IdProofActivity.this);

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
                    IdProofActivity.this,
                    IdProofActivity.this.getApplicationContext()
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.CAMERA_REQUEST && resultCode == RESULT_OK) {
            Uri takenPhotoUri = getPhotofileuri(photoFileName);
            CropImage.activity(takenPhotoUri).setFixAspectRatio(false).start(this);

        } else if (requestCode == Constants.PICK_IMAGE_REQUEST && resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            if (selectedImage != null) {
                CropImage.activity(selectedImage).setFixAspectRatio(false).start(this);
            }
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                resultUri = result.getUri();
                file = new File(resultUri.getPath());
                setfile(file);
                String photoPath = resultUri.getPath();
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

                Glide.with(IdProofActivity.this).load(photoPath)
                        .thumbnail(0.5f)
                        .apply(new RequestOptions().placeholder(R.mipmap.defult_user).dontAnimate())
                        .apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL))
                        .into(imageView);

//                Glide.with(IdProofActivity.this)
//                        .load(photoPath)
//                        .placeholder(R.mipmap.upload)
//                        .error(R.mipmap.upload)
//                        .diskCacheStrategy(DiskCacheStrategy.SOURCE)
//                        .into(imageView);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Log.e(Constants.LOG_CAT,""+result.getError());
            }

        }
    }


    public void updateDrivinglicenceApi() {

        Api api = ApiFactory.getClientWithoutHeader(IdProofActivity.this).create(Api.class);
        Map<String, RequestBody> map = new HashMap<>();
        Call<ResponseBody> call;
        if (getFile() != null) {

            Uri selectedUri = Uri.fromFile(getFile());
            String fileExtension = MimeTypeMap.getFileExtensionFromUrl(selectedUri.toString());
            String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension);
            RequestBody reqBodyImageOfUser = RequestBody.create(MediaType.parse(mimeType), getFile());
            map.put("driving_licence_image\"; filename=\"" + getFile().getName() + "\"", reqBodyImageOfUser);

        }

        String  accessToken = appPreference.getString(Constants.ACCESS_TOKEN);
        call = api.updatedrivinglicence(accessToken, map );
        Constants.showProgressDialog(IdProofActivity.this, Constants.LOADING);

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
                                appPreference.setString(Constants.DRIVING_LICENCE_IMAGE, "Done");
                                Intent intent = new Intent(IdProofActivity.this, AccountDetailActivity.class);
                                startActivity(intent);
//                                finish();

                            }

                        } else if (object.optString(Constants.SUCCESS).equalsIgnoreCase(Constants.FALSE)) {
                            appPreference.setString(Constants.DRIVING_LICENCE_IMAGE, "");
                            JSONArray jsonArray = object.optJSONArray(Constants.ERROR);
                            if (jsonArray == null) {
                                Constants.showToastAlert(object.getJSONObject(Constants.ERROR).getString(Constants.MESSAGE),mContext);
                            }else {
                                Constants.showToastAlert(jsonArray.getJSONObject(0).getString(Constants.MESSAGE), mContext);
                            }
                        }

                    } else if (response != null) {
                        if (response.code() == 400 || response.code() == 500 || response.code() == 403 || response.code() == 404 || response.code() == 401) {
                            appPreference.setString(Constants.DRIVING_LICENCE_IMAGE, "");
                            if(response.code()==401){
                                appPreference.showCustomAlert(IdProofActivity.this,getResources().getString(R.string.http_401_error));
                            }else {
                                Constants.showToastAlert(ErrorUtils.getHtttpCodeError(response.code()), mContext);
                            }

                        } else {
                            appPreference.setString(Constants.DRIVING_LICENCE_IMAGE, "");
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

}

