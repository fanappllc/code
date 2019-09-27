package com.fanphotographer.bean;

/**
 * Created by Ghanshyam on 2/22/2017.
 */
public class PhotoBean {
    private int type;
    private String dateText;
    private int imageID;

    public String getDateText() {
        return dateText;
    }

    public void setDateText(String dateText) {
        this.dateText = dateText;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getImageID() {
        return imageID;
    }

    public void setImageID(int imageID) {
        this.imageID = imageID;
    }
}
