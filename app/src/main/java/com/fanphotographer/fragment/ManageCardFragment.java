package com.fanphotographer.fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.fanphotographer.R;
import com.fanphotographer.activity.AccountDetailActivity;
import com.fanphotographer.activity.MenuScreen;
import com.fanphotographer.adapter.ManageCardAdapter;
import com.fanphotographer.utility.ActivityUtils;
import com.special.ResideMenu.ResideMenu;

import java.util.ArrayList;


public class ManageCardFragment extends Fragment implements View.OnClickListener {

    private View view;
    private RecyclerView manageCardrecyclerview;
    private ArrayList<String> manageCardList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_manage_card, container, false);
        manageCardList = new ArrayList<>();
        setToolBar();
        intView();
        return view;
    }

    private void setToolBar() {
        ImageView imageViewEdit = (ImageView) view.findViewById(R.id.imageView_edit);
        RelativeLayout toolBarLeft = (RelativeLayout) view.findViewById(R.id.toolBarLeft);
        ImageView imageViewMenu = (ImageView) view.findViewById(R.id.imageView_menu);
        TextView headerTextView = (TextView) view.findViewById(R.id.hedertextview);
        ImageView toolbarbackpress = (ImageView) view.findViewById(R.id.toolbarbackpress);
        toolbarbackpress.setVisibility(View.GONE);
        imageViewEdit.setVisibility(View.VISIBLE);
        imageViewMenu.setVisibility(View.VISIBLE);
        headerTextView.setText(getActivity().getResources().getString(R.string.manage_card));
        toolBarLeft.setOnClickListener(this);
        imageViewEdit.setOnClickListener(this);
    }


    private void intView() {
        manageCardrecyclerview = (RecyclerView) view.findViewById(R.id.manage_card_recycler_view);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        manageCardrecyclerview.setLayoutManager(linearLayoutManager);
        setList();
    }

    private void setList() {
        manageCardList.add("");
        manageCardList.add("");
        manageCardList.add("");
        manageCardList.add("");
        manageCardList.add("");
        manageCardList.add("");
        manageCardList.add("");
        manageCardList.add("");
        manageCardList.add("");
        manageCardList.add("");
        manageCardList.add("");
        manageCardList.add("");

        ManageCardAdapter manageCardAdapter = new ManageCardAdapter(getContext(), manageCardList);
        manageCardrecyclerview.setAdapter(manageCardAdapter);


    }

    @Override
    public void onClick(View view) {
        int i = view.getId();
        if (i == R.id.toolBarLeft) {
            MenuScreen.resideMenu.openMenu(ResideMenu.DIRECTION_LEFT);

        } else if (i == R.id.imageView_edit) {
            ActivityUtils.getInstance().invokeActivity(getActivity(), AccountDetailActivity.class, false);

        }
    }


}
