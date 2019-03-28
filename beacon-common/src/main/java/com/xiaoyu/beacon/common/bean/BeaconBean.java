/**
 * 唯有读书,不慵不扰
 * 
 */
package com.xiaoyu.beacon.common.bean;

/**
 * @author hongyu
 * @date 2018-05
 * @description 由BeaconExporter,BeaconProtocol,BeaconReference,BeaconRegistry继承
 */
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
