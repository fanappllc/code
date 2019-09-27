package com.fancustomer.bean;

import android.widget.ImageView;

import java.util.ArrayList;

/**
 * Created by Ghanshyam on 2/22/2017.
 */
public class PhotoBean {
    int type;
    String dateText;
    String order_id;
    String photographer_name;
    String  imagePath;
    boolean isSelected;

    int imageID;

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

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public String getOrder_id() {
        return order_id;
    }

    public void setOrder_id(String order_id) {
        this.order_id = order_id;
    }

    public String getPhotographer_name() {
        return photographer_name;
    }

    public void setPhotographer_name(String photographer_name) {
        this.photographer_name = photographer_name;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }
}
