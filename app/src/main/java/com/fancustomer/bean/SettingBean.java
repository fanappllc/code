package com.fancustomer.bean;

/**
 * Created by Shubham on 1/2/2018.
 */

public class SettingBean {

    String name;
    String value;

    public SettingBean(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
