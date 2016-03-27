package com.investigatorsapp.db.greendao;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT. Enable "keep" sections if you want to edit. 
/**
 * Entity mapped to table TRACE.
 */
public class Trace {

    private Long id;
    private String time;
    private String lat;
    private String lng;

    public Trace() {
    }

    public Trace(Long id) {
        this.id = id;
    }

    public Trace(Long id, String time, String lat, String lng) {
        this.id = id;
        this.time = time;
        this.lat = lat;
        this.lng = lng;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getLng() {
        return lng;
    }

    public void setLng(String lng) {
        this.lng = lng;
    }

}
