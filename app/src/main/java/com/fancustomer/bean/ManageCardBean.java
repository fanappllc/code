package com.fancustomer.bean;

/**
 * Created by Shubham on 12/21/2017.
 */

public class ManageCardBean {


    String id;
    String user_id;
    String card_id;
    String brand;
    String last_4_digit;
    String created_at;

  /*  String id = jsonObject.optString("card_id");
    String card_id = jsonObject.optString("card_id");
    String brand = jsonObject.optString("brand");
    String last_4_digit = jsonObject.optString("last_4_digit");*/


    public ManageCardBean(String id, String card_id, String brand, String last_4_digit) {
        this.id = id;
        this.card_id = card_id;
        this.brand = brand;
        this.last_4_digit = last_4_digit;

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getCard_id() {
        return card_id;
    }

    public void setCard_id(String card_id) {
        this.card_id = card_id;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getLast_4_digit() {
        return last_4_digit;
    }

    public void setLast_4_digit(String last_4_digit) {
        this.last_4_digit = last_4_digit;
    }

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }
}
