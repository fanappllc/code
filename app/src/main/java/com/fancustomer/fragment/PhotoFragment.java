package com.fancustomer.fragment;

import android.app.Dialog;

import android.content.Context;

import android.graphics.Bitmap;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import android.util.Log;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;

import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;


import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.fancustomer.R;
import com.fancustomer.activity.MenuScreen;
import com.fancustomer.activity.TrackPhotographer;
import com.fancustomer.adapter.CustomPhotoAdapter;

import com.fancustomer.bean.PhotoBean;
import com.fancustomer.data.constant.Constants;
import com.fancustomer.data.preference.AppPreference;
import com.fancustomer.helper.ErrorUtils;
import com.fancustomer.helper.ExceptionHandler;
import com.fancustomer.webservice.Api;
import com.fancustomer.webservice.ApiFactory;
import com.special.ResideMenu.ResideMenu;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class PhotoFragment extends Fragment implements View.OnClickListener {
    private View view;
    private LinearLayout layout_animation;
    private TextView header_TextView;
    private RelativeLayout toolBarLeft;
    private ImageView imageView_menu, toolbarbackpress;
    public LinearLayout popup;
    private RecyclerView vertical_recycler_view;
    private CustomPhotoAdapter customAdapter;
    private Animation slidein;
    private Animation slideUp;
    private TextView noSavedTextView;
    private Button btn_close;
    private Bitmap bitmap;
    private TextView saveTextView;
    private TextView unSavedTextView;
    private Button show;
    private boolean isMultiple;
    private boolean isSaved;
    private ArrayList<HashMap<String, Object>> parentList = new ArrayList<>();
    private AlbumAdapter albumAdapter;
    ViewGroup viewGroup;
    private String customerNameStr = "";
    private TextView headerRightText;
    MenuScreen menuActivity;
    private TextView headerTextCancel;
    private TextView headerRightSave;
    private RecyclerView recyclerView;
    ArrayList<PhotoBean> tempList = new ArrayList<>();
    private TextView headerTextView;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_photo, container, false);
        viewGroup = container;
        setToolBar();
        customerNameStr = AppPreference.getInstance(getActivity()).getString(Constants.FIRST_NAME);
        intView();
        if (Constants.isInternetOn(getActivity())) {
            getPhoto();
        } else {
            menuActivity.showSnackbar(viewGroup, getResources().getString(R.string.no_internet), Constants.RETRY);
        }
        return view;
    }


    private void intView() {

        // set LayoutManager to RecyclerView
        headerTextView = view.findViewById(R.id.headerTextView);
        headerTextView.setVisibility(View.VISIBLE);
        noSavedTextView = view.findViewById(R.id.noSaved_TextView);
        unSavedTextView = view.findViewById(R.id.unSavedTextView);
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        saveTextView = view.findViewById(R.id.saveTextView);
        saveTextView.setOnClickListener(this);
        unSavedTextView.setOnClickListener(this);
        headerRightText.setOnClickListener(this);
        btn_close = view.findViewById(R.id.show1);
        layout_animation = view.findViewById(R.id.layout_animation);
        show = view.findViewById(R.id.show);
        slideUp = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_for_in);
        slidein = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_for_out);
        popup = view.findViewById(R.id.sliding1);
        vertical_recycler_view = view.findViewById(R.id.vertical_recycler_view);
        popup.setVisibility(View.GONE);
        albumAdapter = new AlbumAdapter();
        LinearLayoutManager horizontalLayoutManagaer = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        vertical_recycler_view.setLayoutManager(horizontalLayoutManagaer);
        vertical_recycler_view.setAdapter(albumAdapter);
        show.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popup.setVisibility(View.VISIBLE);
                btn_close.setVisibility(View.VISIBLE);
                show.setVisibility(View.GONE);
                layout_animation.startAnimation(slideUp);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        show.setVisibility(View.VISIBLE);
                    }
                }, 300);

            }
        });


        btn_close.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        popup.setVisibility(View.GONE);
                        btn_close.setVisibility(View.GONE);
                    }
                }, 500);
                layout_animation.startAnimation(slidein);

            }
        });


    }

    public void getPhoto() {
        Api api = ApiFactory.getClientWithoutHeader(getActivity()).create(Api.class);
        Call<ResponseBody> call;

        call = api.getPhoto(AppPreference.getInstance(getActivity()).getString(Constants.ACCESS_TOKEN));
        Log.e(Constants.LOG_CAT, "API getPhoto------------------->>>>>:" + " " + call.request().url());
        Log.e(Constants.LOG_CAT, "HEADERS : " + call.request().headers());


        Constants.showProgressDialog(getActivity(), Constants.LOADING);
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
                            Log.e(Constants.LOG_CAT, "onResponse:API getPhoto>>>>>>>>>>>>>>>>>>>>>>>" + object.toString());
                            JSONArray jsonArray = object.optJSONArray("data");

                            if (parentList != null && !parentList.isEmpty()) {
                                parentList.clear();
                            } else {
                                parentList = new ArrayList<>();
                            }
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONArray jsonArray1 = jsonArray.optJSONArray(i);
                                HashMap<String, Object> map = new HashMap<>();
                                ArrayList<PhotoBean> verticalList = new ArrayList<PhotoBean>();
                                for (int j = 0; j < jsonArray1.length(); j++) {
                                    JSONObject jsonObject = jsonArray1.optJSONObject(j);
                                    PhotoBean photoBean = new PhotoBean();
                                    String orderId = jsonObject.optString("order_id");
                                    String photographerName = jsonObject.optString("photographer_name");
                                    String created = jsonObject.optString("created");
                                    photoBean.setDateText(created);
                                    photoBean.setOrder_id(orderId);
                                    photoBean.setPhotographer_name(photographerName);
                                    photoBean.setType(0);
                                    verticalList.add(photoBean);

                                    JSONArray photoArray = jsonObject.getJSONArray("photos");
                                    for (int k = 0; k < photoArray.length(); k++) {
                                        String picture = photoArray.getJSONObject(k).optString("picture");
                                        PhotoBean photoBean2 = new PhotoBean();
                                        photoBean2.setType(1);
                                        photoBean2.setDateText(created);
                                        photoBean2.setOrder_id(orderId);
                                        photoBean2.setPhotographer_name(photographerName);
                                        photoBean2.setImagePath(picture);
                                        verticalList.add(photoBean2);
                                    }
                                    map.put("name", photographerName);
                                }
                                map.put("vertical_list", verticalList);
                                parentList.add(map);
                            }
                            albumAdapter.notifyDataSetChanged();
                            setData("");

                        } else if (object.optString(Constants.SUCCESS).equalsIgnoreCase(Constants.FALSE)) {
                            ErrorUtils.showFalseMessage(object, getActivity());
                        }
                    } else if (response.code() == 400 || response.code() == 500 || response.code() == 403 || response.code() == 404 || response.code() == 401) {
                        if (response.code() == 401) {
                            Constants.showSessionExpireAlert(getActivity());
                        } else {
                            Constants.showToastAlert(ErrorUtils.getHtttpCodeError(response.code()), getActivity());
                        }

                    } else {
                        String responseStr = ErrorUtils.getResponseBody(response);
                        JSONObject jsonObject = new JSONObject(responseStr);
                        Constants.showToastAlert(ErrorUtils.checkJosnErrorBody(jsonObject), getActivity());
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

    private void getAllImageSaved() {
        try {
            String ExternalStorageDirectoryPath = Environment
                    .getExternalStorageDirectory()
                    .getAbsolutePath();

            String targetPath = ExternalStorageDirectoryPath + "/FAN" + " " + customerNameStr + "/";

            File targetDirector = new File(targetPath);

            File[] files = targetDirector.listFiles();
            if (parentList != null && !parentList.isEmpty()) {
                parentList.clear();
            } else {
                parentList = new ArrayList<>();
            }
            if (files != null) {
                show.setVisibility(View.VISIBLE);
                ArrayList<File> fileArrayList = new ArrayList<>();
                for (File f : files) {
                    fileArrayList.add(f);
                }

                Collections.sort(fileArrayList, new Comparator<File>() {
                    @Override
                    public int compare(File o1, File o2) {
                        return o1.compareTo(o2);
                    }
                });

                for (int i = 0; i < fileArrayList.size(); i++) {
                    HashMap<String, Object> map = new HashMap<>();
                    ArrayList<PhotoBean> verticalList = new ArrayList<>();
                    File file = fileArrayList.get(i);
                    if (file.isDirectory()) {
                        PhotoBean photoBean1 = new PhotoBean();
                        photoBean1.setType(0);
                        photoBean1.setDateText("");
                        photoBean1.setOrder_id("");
                        photoBean1.setPhotographer_name(file.getName());

                        photoBean1.setImagePath("");
                        verticalList.add(photoBean1);
                        File[] fileList = file.listFiles();
                        for (File filePic : fileList) {
                            if (filePic.isFile()) {
                                PhotoBean photoBean2 = new PhotoBean();
                                photoBean2.setType(1);
                                photoBean2.setDateText("");
                                photoBean2.setOrder_id("");
                                photoBean2.setPhotographer_name(file.getName());
                                photoBean2.setImagePath(filePic.getAbsolutePath());
                                verticalList.add(photoBean2);
                            }
                        }
                        map.put("name", file.getName());
                    }
                    map.put("vertical_list", verticalList);
                    parentList.add(map);
                }

            } else {
                show.setVisibility(View.GONE);
            }
            albumAdapter.notifyDataSetChanged();
            setData("save");

        } catch (Exception e) {
            ExceptionHandler.printStackTrace(e);
        }

    }

    private ProgressBar progress_bar;

    public void openImage(final Context context, final String imageUrl, final String photographerName, final String type) {
        final Dialog dialogPopUp = new Dialog(context);
        dialogPopUp.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialogPopUp.setCancelable(true);
        dialogPopUp.setContentView(R.layout.popup_open_image);
        TextView txtSubmitImage = (TextView) dialogPopUp.findViewById(R.id.txtSubmitImage);
        progress_bar = (ProgressBar) dialogPopUp.findViewById(R.id.progress_bar);
        final ImageView imgEnlargedImage = (ImageView) dialogPopUp.findViewById(R.id.img_enlarged_image);

        final RelativeLayout cancleDialog = (RelativeLayout) dialogPopUp.findViewById(R.id.img_cancel_btn);
        dialogPopUp.getWindow().setBackgroundDrawable(new ColorDrawable(Color
                .parseColor("#50000000")));
        dialogPopUp.getWindow().setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        dialogPopUp.show();

        if (isSaved) {
            txtSubmitImage.setVisibility(View.GONE);
        } else {
            txtSubmitImage.setVisibility(View.VISIBLE);
        }
        Glide.with(getActivity()).load(imageUrl)
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
                        progress_bar.setVisibility(View.GONE);
                        return false;
                    }
                }).into(imgEnlargedImage);

        txtSubmitImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Constants.showProgressDialog(getActivity(), "Saving...");
                getBitmap(imageUrl, photographerName, 0, "single");
                dialogPopUp.dismiss();
            }

        });
        cancleDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialogPopUp.dismiss();
            }
        });

    }

    private void getBitmap(final String url, final String photographerName, final int size, final String single) {
        /*new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                Looper.prepare();
                try {
                    bitmap = Glide.
                            with(getActivity()).
                            load(url).
                            asBitmap().
                            into(-1, -1).
                            get();
                } catch (final ExecutionException e) {
                    Log.e(Constants.LOG_CAT, e.getMessage());
                } catch (final InterruptedException e) {
                    Log.e(Constants.LOG_CAT, e.getMessage());
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void dummy) {
                if (null != bitmap) {
                    // The full bitmap should be available here
                    //  image.setImageBitmap(theBitmap);
                    Log.e(Constants.LOG_CAT, "savedImage=============bitmap" + bitmap.toString());
                    //storeImage(bitmap, "shubham");
                    SaveImage(bitmap, photographerName, url);
                }
                ;
            }
        }.execute();*/
        new AsyncTask<Void, Void, Bitmap>() {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected Bitmap doInBackground(Void... voids) {
                Bitmap bitmap2 = null;
                try {
                    bitmap2 = Glide.
                            with(getActivity()).
                            load(url).
                            asBitmap().
                            into(-1, -1).
                            get();
                } catch (final ExecutionException e) {
                    Log.e(Constants.LOG_CAT, e.getMessage());
                } catch (final InterruptedException e) {
                    Log.e(Constants.LOG_CAT, e.getMessage());

                } catch (Exception e) {
                    e.printStackTrace();
                }
                return bitmap2;
            }

            @Override
            protected void onPostExecute(Bitmap bitmap2) {
                super.onPostExecute(bitmap2);
                if (null != bitmap2) {
                    // The full bitmap should be available here
                    //  image.setImageBitmap(theBitmap);
                    Log.e(Constants.LOG_CAT, "savedImage=============bitmap" + bitmap2.toString());
                    //storeImage(bitmap, "shubham");
                    SaveImage(bitmap2, photographerName, url, size, single);

                }

            }
        }.execute();

    }

    private void setData(final String type) {
        popup.setVisibility(View.GONE);
        btn_close.setVisibility(View.GONE);
        if (parentList != null && !parentList.isEmpty()) {
            recyclerView.setVisibility(View.VISIBLE);
            show.setVisibility(View.VISIBLE);

            if (type.equals("")) {
                headerRightText.setVisibility(View.VISIBLE);
            } else {
                headerRightText.setVisibility(View.GONE);
            }


            noSavedTextView.setVisibility(View.GONE);
            final ArrayList<PhotoBean> arrayList = (ArrayList<PhotoBean>) parentList.get(0).get("vertical_list");
            GridLayoutManager layoutManager = new GridLayoutManager(getActivity(), 3);
            layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {

                    if (arrayList != null && arrayList.size() > 0) {
                        for (int i = 0; i < arrayList.size(); i++) {
                            if (0 == arrayList.get(position).getType()) {
                                return 3;
                            } else {
                                return 1;
                            }
                        }
                        return 3;
                    } else {
                        noSavedTextView.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                        show.setVisibility(View.GONE);

                    }

                    return 0;
                }
            });
            recyclerView.setLayoutManager(layoutManager);
            if (type.equals("")) {
                customAdapter = new CustomPhotoAdapter(getActivity(), arrayList, true);
            } else {
                customAdapter = new CustomPhotoAdapter(getActivity(), arrayList, false);
            }

            recyclerView.setAdapter(customAdapter); // set the Adapter to RecyclerView
            customAdapter.onItemClickMethod(new CustomPhotoAdapter.ItemInterFace() {
                @Override
                public void onItemClick(View view, int position) {
                    switch (view.getId()) {
                        case R.id.image:

                            if (isMultiple == true) {
                                PhotoBean photoBean = arrayList.get(position);
                                if (arrayList.get(position).isSelected()) {
                                    photoBean.setSelected(false);

                                } else {
                                    photoBean.setSelected(true);
                                }
                                tempList.add(photoBean);


                                customAdapter.notifyDataSetChanged();
                            } else {
                                String url = arrayList.get(position).getImagePath();
                                String photographerName = arrayList.get(position).getPhotographer_name();
                                openImage(getActivity(), url, photographerName, type);
                            }
                            break;
                    }


                }

            });
        } else {
            recyclerView.setVisibility(View.GONE);
            noSavedTextView.setVisibility(View.VISIBLE);
            show.setVisibility(View.GONE);
            headerRightText.setVisibility(View.GONE);
        }
    }

    private void SaveImage(Bitmap finalBitmap, String photographerName, String url, int size, String single) {

        File folder = new File(Environment.getExternalStorageDirectory() + "/FAN" + " " + customerNameStr + "/" + photographerName);
        if (!folder.exists()) {
            String root = Environment.getExternalStorageDirectory().toString();
            File myDir = new File(root + "/FAN" + " " + customerNameStr + "/" + photographerName);
            myDir.mkdirs();
            //   Random generator = new Random();
            // int n = 10000;
            // n = generator.nextInt(n);
            String photo[] = url.split("/");
            String newNmae = photo[photo.length - 1];
            Log.e(Constants.LOG_CAT, "savedImage=====Name" + newNmae);
            String fname = newNmae + "-" + ".jpg";
            File file = new File(myDir, fname);
            MediaScannerConnection.scanFile(getActivity(),
                    new String[]{file.toString()}, null,
                    new MediaScannerConnection.OnScanCompletedListener() {
                        public void onScanCompleted(String path, Uri uri) {
                            Log.i("ExternalStorage", "Scanned " + path + ":");
                            Log.i("ExternalStorage", "-> uri=" + uri);
                        }
                    });
            if (file.exists()) file.delete();
            try {
                FileOutputStream out = new FileOutputStream(file);
                finalBitmap.compress(Bitmap.CompressFormat.JPEG, 70, out);
                Log.e(Constants.LOG_CAT, "savedImage=============out" + out);
                out.flush();
                out.close();

            } catch (Exception e) {
                Constants.hideProgressDialog();
                e.printStackTrace();
            }
            Log.e(Constants.LOG_CAT, "SaveImage success: " + folder);
        } else {
            //Random generator = new Random();
//            int n = 10000;
//            n = generator.nextInt(n);
            String photo[] = url.split("/");
            String newNmae = photo[photo.length - 1];
            Log.e(Constants.LOG_CAT, "savedImage=====Name" + newNmae);
            String fname = newNmae + "-" + ".jpg";
            File file = new File(folder, fname);
            MediaScannerConnection.scanFile(getActivity(),
                    new String[]{file.toString()}, null,
                    new MediaScannerConnection.OnScanCompletedListener() {
                        public void onScanCompleted(String path, Uri uri) {
                            Log.i("ExternalStorage", "Scanned " + path + ":");
                            Log.i("ExternalStorage", "-> uri=" + uri);
                        }
                    });

            if (file.exists()) file.delete();
            try {
                FileOutputStream out = new FileOutputStream(file);
                finalBitmap.compress(Bitmap.CompressFormat.JPEG, 70, out);
                Log.e(Constants.LOG_CAT, "savedImage=============out" + out);
                out.flush();
                out.close();

//                Bitmap photo = (Bitmap) "your Bitmap image";
//                photo = Bitmap.createScaledBitmap(photo, 100, 100, false);
//                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
//                photo.compress(Bitmap.CompressFormat.JPEG, 40, bytes);
//
//                File f = new File(Environment.getExternalStorageDirectory()
//                        + File.separator + "Imagename.jpg");
//                f.createNewFile();
//                FileOutputStream fo = new FileOutputStream(f);
//                fo.write(bytes.toByteArray());
//                fo.close();

            } catch (Exception e) {
                e.printStackTrace();
                Constants.hideProgressDialog();
            }
            Log.e(Constants.LOG_CAT, "savedImage=============folder" + folder);

        }


        if (single.equals("single")) {
            Constants.hideProgressDialog();
            tempList.clear();
            isMultiple = false;
            headerRightText.setVisibility(View.VISIBLE);
            headerRightSave.setVisibility(View.GONE);
            headerTextCancel.setVisibility(View.GONE);
            imageView_menu.setVisibility(View.VISIBLE);
            customAdapter.isRadio = false;
            for (int i = 0; i < customAdapter.getList().size(); i++) {
                PhotoBean photoBean = customAdapter.getList().get(i);
                if (customAdapter.getList().get(i).isSelected()) {
                    photoBean.setSelected(false);

                }
            }
            Constants.showToastAlert("Image saved successfully!!", getActivity());
        }


        if (tempList != null && !tempList.isEmpty() && size >= (tempList.size() - 1)) {
            Constants.hideProgressDialog();
            tempList.clear();
            isMultiple = false;
            headerRightText.setVisibility(View.VISIBLE);
            headerRightSave.setVisibility(View.GONE);
            headerTextCancel.setVisibility(View.GONE);
            imageView_menu.setVisibility(View.VISIBLE);
            customAdapter.isRadio = false;
            for (int i = 0; i < customAdapter.getList().size(); i++) {
                PhotoBean photoBean = customAdapter.getList().get(i);
                if (customAdapter.getList().get(i).isSelected()) {
                    photoBean.setSelected(false);

                }
            }
            Constants.showToastAlert("Images saved successfully!!", getActivity());
        }
        customAdapter.notifyDataSetChanged();
//        String root = Environment.getExternalStorageDirectory().toString();
//        File myDir = new File(root + "/FAN");
//        myDir.mkdirs();

    }


    private void setToolBar() {
        headerRightSave = (TextView) view.findViewById(R.id.headerRightSave);
        headerTextCancel = (TextView) view.findViewById(R.id.headerText_cancel);
        headerRightText = (TextView) view.findViewById(R.id.headerRightText);
        toolBarLeft = (RelativeLayout) view.findViewById(R.id.toolBarLeft);
        imageView_menu = (ImageView) view.findViewById(R.id.imageView_menu);
        header_TextView = (TextView) view.findViewById(R.id.hedertextview);
        toolbarbackpress = (ImageView) view.findViewById(R.id.toolbarbackpress);
        toolbarbackpress.setVisibility(View.GONE);
        imageView_menu.setVisibility(View.VISIBLE);
        headerRightText.setVisibility(View.VISIBLE);
        header_TextView.setText(getActivity().getResources().getString(R.string.my_photos));
        headerRightText.setText("Select");
        imageView_menu.setOnClickListener(this);
        headerTextCancel.setOnClickListener(this);
        headerRightSave.setOnClickListener(this);

    }


    class AlbumAdapter extends RecyclerView.Adapter<AlbumAdapter.MyViewHolder>

    {

        public class MyViewHolder extends RecyclerView.ViewHolder {
            public TextView folder_name;


            public MyViewHolder(View view) {
                super(view);
                folder_name = (TextView) view.findViewById(R.id.folder_name);

            }
        }


        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_album, parent, false);

            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final MyViewHolder holder, final int position) {
            String photgrapherName = String.valueOf(parentList.get(position).get("name"));
            holder.folder_name.setText(Constants.wordFirstCap(photgrapherName));
            holder.folder_name.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {

                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    popup.setVisibility(View.GONE);
                                    btn_close.setVisibility(View.GONE);
                                    if (isSaved == true) {
                                        isMultiple = false;
                                        headerRightText.setVisibility(View.GONE);
                                        headerRightSave.setVisibility(View.GONE);
                                        headerTextCancel.setVisibility(View.GONE);
                                        imageView_menu.setVisibility(View.VISIBLE);
                                    } else {
                                        isMultiple = false;
                                        headerRightText.setVisibility(View.VISIBLE);
                                        headerRightSave.setVisibility(View.GONE);
                                        headerTextCancel.setVisibility(View.GONE);
                                        imageView_menu.setVisibility(View.VISIBLE);
                                    }

                                    tempList.clear();
                                    for (int i = 0; i < customAdapter.getList().size(); i++) {
                                        PhotoBean photoBean = customAdapter.getList().get(i);
                                        if (customAdapter.getList().get(i).isSelected()) {
                                            photoBean.setSelected(false);

                                        }
                                    }
                                    final ArrayList<PhotoBean> arrayList = (ArrayList<PhotoBean>) parentList.get(position).get("vertical_list");
                                    GridLayoutManager layoutManager = new GridLayoutManager(getActivity(), 3);
                                    layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                                        @Override
                                        public int getSpanSize(int position) {
                                            for (int i = 0; i < arrayList.size(); i++) {
                                                if (0 == arrayList.get(position).getType()) {
                                                    return 3;
                                                } else {
                                                    return 1;
                                                }
                                            }
                                            return 3;
                                        }
                                    });
                                    recyclerView.setLayoutManager(layoutManager);

                                    customAdapter = new CustomPhotoAdapter(getActivity(), arrayList, true);
                                    recyclerView.setAdapter(customAdapter); // set the Adapter to RecyclerView
                                    customAdapter.onItemClickMethod(new CustomPhotoAdapter.ItemInterFace() {
                                        @Override
                                        public void onItemClick(View view, int position) {
                                            switch (view.getId()) {
                                                case R.id.image:
                                                    if (isMultiple == true) {
                                                        PhotoBean photoBean = arrayList.get(position);
                                                        if (arrayList.get(position).isSelected()) {
                                                            photoBean.setSelected(false);

                                                        } else {
                                                            photoBean.setSelected(true);
                                                        }
                                                        tempList.add(photoBean);


                                                        customAdapter.notifyDataSetChanged();
                                                    } else {
                                                        String url = arrayList.get(position).getImagePath();
                                                        String photographerName = arrayList.get(position).getPhotographer_name();
                                                        openImage(getActivity(), url, photographerName, "");
                                                    }
                                                    break;
                                            }
                                        }

                                    });

                                }
                            });

                            //  layout_animation.setBackgroundDrawable(getActivity().getResources().getDrawable(R.mipmap.side_option_open));
                        }
                    }, 500);
                    layout_animation.startAnimation(slidein);
                }
            });
        }

        @Override
        public int getItemCount() {
            return parentList.size();
        }

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        menuActivity = (MenuScreen) context;

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.imageView_menu:
                MenuScreen.resideMenu.openMenu(ResideMenu.DIRECTION_LEFT);
                break;
            case R.id.saveTextView:
                headerTextView.setVisibility(View.GONE);
                isMultiple = false;
                isSaved = true;
                headerRightText.setVisibility(View.GONE);
                headerRightSave.setVisibility(View.GONE);
                headerTextCancel.setVisibility(View.GONE);
                imageView_menu.setVisibility(View.VISIBLE);
                saveTextView.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                unSavedTextView.setBackgroundColor(getResources().getColor(R.color.white));
                saveTextView.setTextColor(getResources().getColor(R.color.white));
                unSavedTextView.setTextColor(getResources().getColor(R.color.black));
                // show.setVisibility(View.GONE);
                getAllImageSaved();
                break;
            case R.id.unSavedTextView:
                headerTextView.setVisibility(View.VISIBLE);
                isSaved = false;
                isMultiple = false;
                headerRightText.setVisibility(View.VISIBLE);
                saveTextView.setBackgroundColor(getResources().getColor(R.color.white));
                saveTextView.setTextColor(getResources().getColor(R.color.black));
                unSavedTextView.setTextColor(getResources().getColor(R.color.white));
                unSavedTextView.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                show.setVisibility(View.VISIBLE);
                if (Constants.isInternetOn(getActivity())) {
                    getPhoto();
                } else {
                    menuActivity.showSnackbar(viewGroup, getResources().getString(R.string.no_internet), "Retry");
                }
                break;

            case R.id.headerRightText:
                isMultiple = true;
                headerRightText.setVisibility(View.GONE);
                headerRightSave.setVisibility(View.VISIBLE);
                headerTextCancel.setVisibility(View.VISIBLE);
                imageView_menu.setVisibility(View.GONE);

                customAdapter.isRadio = true;
                customAdapter.notifyDataSetChanged();


                break;
            case R.id.headerText_cancel:
                isMultiple = false;
                headerRightText.setVisibility(View.VISIBLE);
                headerRightSave.setVisibility(View.GONE);
                headerTextCancel.setVisibility(View.GONE);
                imageView_menu.setVisibility(View.VISIBLE);
                tempList.clear();
                customAdapter.isRadio = false;
                for (int i = 0; i < customAdapter.getList().size(); i++) {
                    PhotoBean photoBean = customAdapter.getList().get(i);
                    if (customAdapter.getList().get(i).isSelected()) {
                        photoBean.setSelected(false);

                    }
                }
                customAdapter.notifyDataSetChanged();
                break;

            case R.id.headerRightSave:
                if (tempList.size() > 0) {
                    Constants.showProgressDialog(getActivity(), "Saving...");
                    for (int i = 0; i < tempList.size(); i++) {
                        String url = tempList.get(i).getImagePath();
                        String photographerName = tempList.get(i).getPhotographer_name();
                        getBitmap(url, photographerName, i, "");
                    }
                } else {
                    Constants.showToastAlert("Please select photos", getActivity());
                }
                Log.e(Constants.LOG_CAT, "SAVE list size=======>>>>>>>>>" + tempList.size());
                break;
        }
    }
}

