package com.fancustomer.fragment;


import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.daimajia.swipe.SwipeLayout;
import com.daimajia.swipe.adapters.RecyclerSwipeAdapter;
import com.fancustomer.R;
import com.fancustomer.activity.AddCreditCardActivity;
import com.fancustomer.activity.MenuScreen;

import com.fancustomer.bean.ManageCardBean;
import com.fancustomer.data.constant.Constants;
import com.fancustomer.data.preference.AppPreference;
import com.fancustomer.helper.ErrorUtils;
import com.fancustomer.webservice.Api;
import com.fancustomer.webservice.ApiFactory;
import com.special.ResideMenu.ResideMenu;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class ManageCardFragment extends Fragment implements View.OnClickListener {

    private View view;
    private RecyclerView manageCardRecyclerView;
    private ManageCardAdapter manageCardAdapter;
    private ArrayList<ManageCardBean> manageCardList;
    private TextView noCardTextView;
    private ViewGroup viewGroup;
    MenuScreen menuActivity;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_manage_card, container, false);
        viewGroup = container;
        manageCardList = new ArrayList<>();
        setToolBar();
        intView();
        if (Constants.isInternetOn(getActivity())) {
            getMyCardApi();
        } else {
            menuActivity.showSnackbar(viewGroup, getResources().getString(R.string.no_internet), Constants.RETRY);
        }
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        menuActivity = (MenuScreen) context;

    }

    private void setToolBar() {
        ImageView imageViewEdit = view.findViewById(R.id.imageView_edit);
        RelativeLayout toolBarLeft = view.findViewById(R.id.toolBarLeft);
        ImageView imageViewMenu = view.findViewById(R.id.imageView_menu);
        TextView headerTextView = view.findViewById(R.id.hedertextview);
        ImageView toolbarbackpress = view.findViewById(R.id.toolbarbackpress);
        toolbarbackpress.setVisibility(View.GONE);
        imageViewEdit.setVisibility(View.VISIBLE);
        imageViewMenu.setVisibility(View.VISIBLE);
        headerTextView.setText(getActivity().getResources().getString(R.string.manage_card));
        toolBarLeft.setOnClickListener(this);
        imageViewEdit.setOnClickListener(this);
    }


    private void intView() {
        noCardTextView = view.findViewById(R.id.noCardTextView);
        manageCardRecyclerView = view.findViewById(R.id.manage_card_recycler_view);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        manageCardRecyclerView.setLayoutManager(linearLayoutManager);

    }

    private void setList() {

        if (manageCardAdapter == null) {
            manageCardAdapter = new ManageCardAdapter(getContext(), manageCardList);
            manageCardRecyclerView.setAdapter(manageCardAdapter);
        } else {
            manageCardAdapter.notifyDataSetChanged();
        }


    }


    public void getMyCardApi() {


        Api api = ApiFactory.getClientWithoutHeader(getActivity()).create(Api.class);
        Call<ResponseBody> call;

        call = api.getMyCard(AppPreference.getInstance(getActivity()).getString(Constants.ACCESS_TOKEN));
        Log.e(Constants.LOG_CAT, "FAN MY CARD API------------------->>>>>:" + " " + call.request().url());
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
                            Log.e(Constants.LOG_CAT, "onResponse: MY CARD API LIST=============>>>>>>>>>>" + object.toString());

                            JSONArray jsonArray = object.optJSONArray("data");
                            if (jsonArray != null && jsonArray.length() > 0) {

                                manageCardRecyclerView.setVisibility(View.VISIBLE);
                                noCardTextView.setVisibility(View.GONE);
                                for (int i = 0; i < jsonArray.length(); i++) {

                                    JSONObject jsonObject = jsonArray.optJSONObject(i);
                                    String id = jsonObject.optString("card_id");
                                    String cardId = jsonObject.optString("card_id");
                                    String brand = jsonObject.optString("brand");
                                    String last4Digit = jsonObject.optString("last_4_digit");
                                    ManageCardBean manageCardBean = new ManageCardBean(id, cardId, brand, last4Digit);
                                    manageCardList.add(manageCardBean);


                                }
                            } else {
                                manageCardRecyclerView.setVisibility(View.GONE);
                                noCardTextView.setVisibility(View.VISIBLE);
                            }

                            setList();

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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 202 && resultCode == getActivity().RESULT_OK) {
            manageCardList.clear();
            if (Constants.isInternetOn(getActivity())) {
                getMyCardApi();
            } else {
                menuActivity.showSnackbar(viewGroup, getResources().getString(R.string.no_internet), Constants.RETRY);
            }
        }


    }

    private void showDeleteDialog(final String cardId, final int pos) {
        final Dialog dialog = new Dialog(getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.dialog_log_out);
        TextView textviewHeader = dialog.findViewById(R.id.textview__header);
        TextView textviewMessages = dialog.findViewById(R.id.textview__messages);
        TextView textViewYes = dialog.findViewById(R.id.button_ok);
        TextView textViewNo = dialog.findViewById(R.id.button_cancel);
        textviewHeader.setText(getResources().getString(R.string.alert));
        textviewMessages.setText(getResources().getString(R.string.are_you_sure_you_want));
        textViewYes.setText(getResources().getString(R.string.yes));
        textViewNo.setText(getResources().getString(R.string.no));
        textViewYes.setTextColor(getResources().getColor(R.color.colorPrimary));
        textViewNo.setTextColor(getResources().getColor(R.color.colorPrimary));

        textViewYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                if (Constants.isInternetOn(getActivity())) {
                    getDeleteCardApi(cardId, pos);
                } else {
                    menuActivity.showSnackbar(viewGroup, getResources().getString(R.string.no_internet), Constants.RETRY);
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

    public void getDeleteCardApi(String cardId, final int position) {


        Api api = ApiFactory.getClientWithoutHeader(getActivity()).create(Api.class);
        Call<ResponseBody> call = null;


        call = api.getDeleteCardApi(AppPreference.getInstance(getActivity()).getString(Constants.ACCESS_TOKEN), cardId);
        Log.e(Constants.LOG_CAT, "FAN MY DELETE CARD API------------------->>>>>:" + call.request().url());
        Log.e(Constants.LOG_CAT, "HEADERS : " + call.request().headers());


        Constants.showProgressDialog(getActivity(), "Loading");
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
                            Log.e(Constants.LOG_CAT, "onResponse: DELETE CARD API=============>>>>>>>>>>" + object.toString());
                            manageCardList.remove(position);
                            manageCardAdapter.notifyDataSetChanged();
                            if (manageCardList != null && manageCardList.size() > 0) {

                            } else {
                                noCardTextView.setVisibility(View.VISIBLE);
                                manageCardRecyclerView.setVisibility(View.GONE);
                            }


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

    @Override
    public void onClick(View view) {
        int i = view.getId();
        if (i == R.id.toolBarLeft) {
            MenuScreen.resideMenu.openMenu(ResideMenu.DIRECTION_LEFT);
        } else if (i == R.id.imageView_edit) {
            Intent intent = new Intent(getActivity(), AddCreditCardActivity.class);
            intent.putExtra("COME_ADDCARD", "MANAGE_CARD");
            startActivityForResult(intent, 202);
        }
    }

    //Manage card Adapter//

    public class ManageCardAdapter extends RecyclerSwipeAdapter<ManageCardAdapter.SimpleViewHolder> {

        private Context mContext;
        private ArrayList<ManageCardBean> manageCardList;

        public ManageCardAdapter(Context context, ArrayList<ManageCardBean> manageCardList) {
            this.mContext = context;
            this.manageCardList = manageCardList;
        }

        @Override
        public ManageCardAdapter.SimpleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_manage_card, parent, false);
            return new ManageCardAdapter.SimpleViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ManageCardAdapter.SimpleViewHolder viewHolder, final int position) {


            if (manageCardList.get(position).getBrand().equalsIgnoreCase("" + Constants.Visa)) {
                viewHolder.imgBrand.setImageResource(R.mipmap.visa);
            } else if (manageCardList.get(position).getBrand().equalsIgnoreCase("" + Constants.American_Express) || manageCardList.get(position).getBrand().equalsIgnoreCase("" + Constants.American_E)) {
                viewHolder.imgBrand.setImageResource(R.mipmap.america);
            } else if (manageCardList.get(position).getBrand().equalsIgnoreCase("" + Constants.MasterCard) || manageCardList.get(position).getBrand().equalsIgnoreCase("" + Constants.Master_Card)) {
                viewHolder.imgBrand.setImageResource(R.mipmap.master_card);
            } else if (manageCardList.get(position).getBrand().equalsIgnoreCase("" + Constants.Discover)) {
                viewHolder.imgBrand.setImageResource(R.mipmap.discover);
            } else if (manageCardList.get(position).getBrand().equalsIgnoreCase("" + Constants.JCB)) {
                viewHolder.imgBrand.setImageResource(R.mipmap.jcb);
            } else if (manageCardList.get(position).getBrand().equalsIgnoreCase("" + Constants.Diners_Club)) {
                viewHolder.imgBrand.setImageResource(R.mipmap.dinera_club);
            } else if (manageCardList.get(position).getBrand().equalsIgnoreCase("" + Constants.Unknown)) {
                viewHolder.imgBrand.setImageResource(R.mipmap.default_1);
            }
            viewHolder.swipeLayout.setShowMode(SwipeLayout.ShowMode.PullOut);
            viewHolder.cardNoTextVew.setText("xxxx xxxx xxxx " + manageCardList.get(position).getLast_4_digit());
            viewHolder.cardNameTextView.setText(manageCardList.get(position).getBrand());
            viewHolder.swipeLayout.addDrag(SwipeLayout.DragEdge.Right, viewHolder.swipeLayout.findViewById(R.id.bottom_wrapper));
            viewHolder.swipeLayout.setLeftSwipeEnabled(false);
            viewHolder.swipeLayout.setRightSwipeEnabled(false);

            viewHolder.swipeLayout.addSwipeListener(new SwipeLayout.SwipeListener() {
                @Override
                public void onClose(SwipeLayout layout) {
                    //when the SurfaceView totally cover the BottomView.
                }

                @Override
                public void onUpdate(SwipeLayout layout, int leftOffset, int topOffset) {
                    //you are swiping.
                }

                @Override
                public void onStartOpen(SwipeLayout layout) {

                }

                @Override
                public void onOpen(SwipeLayout layout) {
                    //when the BottomView totally show.
                }

                @Override
                public void onStartClose(SwipeLayout layout) {

                }

                @Override
                public void onHandRelease(SwipeLayout layout, float xvel, float yvel) {
                }
            });

            viewHolder.swipeLayout.getSurfaceView().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //   Toast.makeText(mContext, " onClick : " + notificationList.get(position).getUsername(), Toast.LENGTH_SHORT).show();
                }
            });

            viewHolder.deleteRelative.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    String cardId = manageCardList.get(position).getCard_id();
                    showDeleteDialog(cardId, position);
                    mItemManger.closeAllItems();
                }
            });

            mItemManger.bindView(viewHolder.itemView, position);
        }

        @Override
        public int getItemCount() {
            return manageCardList.size();
        }

        @Override
        public int getSwipeLayoutResourceId(int position) {
            return R.id.swipe;
        }


        public class SimpleViewHolder extends RecyclerView.ViewHolder {
            public SwipeLayout swipeLayout;
            TextView cardNoTextVew;
            TextView cardNameTextView;
            ImageView imgBrand;
            RelativeLayout deleteRelative;

            public SimpleViewHolder(View itemView) {
                super(itemView);
                deleteRelative = itemView.findViewById(R.id.deleteRelative);
                swipeLayout = itemView.findViewById(R.id.swipe);
                cardNoTextVew = itemView.findViewById(R.id.cardNoTextVew);
                cardNameTextView = itemView.findViewById(R.id.cardNameTextView);
                imgBrand = itemView.findViewById(R.id.img_brand);
            }
        }
    }


}
