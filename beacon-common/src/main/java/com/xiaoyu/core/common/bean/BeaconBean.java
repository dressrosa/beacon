package com.xiaoyu.core.common.bean;

public class BeaconBean {

    private String id;
    private String name;
    private String from;

    public String getId() {
        return id;
    }

    public BeaconBean setId(String id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public BeaconBean setName(String name) {
        this.name = name;
        return this;
    }

    public String getFrom() {
        return from;
    }

    public BeaconBean setFrom(String from) {
        this.from = from;
        return this;
    }

}
