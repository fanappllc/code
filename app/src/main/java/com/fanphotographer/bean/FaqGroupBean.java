package com.fanphotographer.bean;

import java.util.ArrayList;

public class FaqGroupBean
{
    private String ques;
    private ArrayList<FaqChildBean> childList;


    public String getQues() {
        return ques;
    }

    public void setQues(String ques) {
        this.ques = ques;
    }

    public ArrayList<FaqChildBean> getChildList()
    {
        return childList;
    }

    public void setChildList(ArrayList<FaqChildBean> childList)
    {
        this.childList = childList;
    }
}
