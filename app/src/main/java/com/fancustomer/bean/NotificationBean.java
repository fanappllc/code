package com.fancustomer.bean;

/**
 * Created by Shubham on 12/21/2017.
 */

public class NotificationBean {


    String id;
    String to_id;
    String from_id;
    String type;
    String message;
    String is_read;
    String created_at;


    public NotificationBean(String id, String to_id, String from_id, String type, String message, String is_read, String created_at) {
        this.id = id;
        this.to_id = to_id;
        this.from_id = from_id;
        this.type = type;

        this.message = message;
        this.is_read = is_read;
        this.created_at = created_at;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTo_id() {
        return to_id;
    }

    public void setTo_id(String to_id) {
        this.to_id = to_id;
    }

    public String getFrom_id() {
        return from_id;
    }

    public void setFrom_id(String from_id) {
        this.from_id = from_id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }


    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getIs_read() {
        return is_read;
    }

    public void setIs_read(String is_read) {
        this.is_read = is_read;
    }

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }


}
