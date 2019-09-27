package com.fancustomer.bean;

/**
 * Created by Shubham on 12/21/2017.
 */

public class SlotBean {


    String id;
    String slot_minutes;
    String price;
    String status;
    String created_at;
    boolean isSelected;


    public SlotBean(String id, String slot_minutes, String price, String status, String created_at, boolean isSelected) {
        this.id = id;
        this.slot_minutes = slot_minutes;
        this.price = price;
        this.status = status;
        this.created_at = created_at;
        this.isSelected = isSelected;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSlot_minutes() {
        return slot_minutes;
    }

    public void setSlot_minutes(String slot_minutes) {
        this.slot_minutes = slot_minutes;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }
}
