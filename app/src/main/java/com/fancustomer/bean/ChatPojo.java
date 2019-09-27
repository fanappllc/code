package com.fancustomer.bean;

/**
 * Created by Shubham on 12/14/2017.
 */

public class ChatPojo {
    public boolean left;
    public int id;
    public String sender;
    public String reciever;
    public String message;
    public String time;
    public String case_id;
    public String type = "";
    public String mediaURL = "";
    public String sender_name = "";
    public String sender_img_url = "";


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public ChatPojo(boolean left, String case_id, String sender, String reciever, String message, String time, String type, String mediaURL) {
        super();

        this.left = left;
        this.case_id = case_id;
        this.sender = sender;
        this.reciever = reciever;
        this.message = message;
        this.time = time;
        this.type = type;
        this.mediaURL = mediaURL;
//        this.sender_name=sender_name;
//        this.sender_img_url=sender_img_url
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMediaURL() {
        return mediaURL;
    }

    public void setMediaURL(String mediaURL) {
        this.mediaURL = mediaURL;
    }

    public String getCase_id() {
        return case_id;
    }

    public void setCase_id(String case_id) {
        this.case_id = case_id;
    }

    public boolean isLeft() {
        return left;
    }

    public void setLeft(boolean left) {
        this.left = left;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReciever() {
        return reciever;
    }

    public void setReciever(String reciever) {
        this.reciever = reciever;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

}
