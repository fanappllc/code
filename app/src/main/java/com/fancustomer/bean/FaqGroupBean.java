package com.fancustomer.bean;

import java.util.ArrayList;

/**
 * Created by Ghanshyam on 2/22/2017.
 */
public class FaqGroupBean
{
    String ques;
    ArrayList<FaqChildBean> childList;


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
