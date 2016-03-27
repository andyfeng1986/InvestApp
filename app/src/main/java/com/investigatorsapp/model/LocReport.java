package com.investigatorsapp.model;

import com.investigatorsapp.db.greendao.Trace;

import java.util.ArrayList;

/**
 * Created by fenglei on 16/1/13.
 */
public class LocReport {

    public String type;
    public String userid;
    public String jobid;
    public String token;
    public String date;
    public String pointnum;
    public ArrayList<Trace> points;

}
