package com.investigatorsapp.model;

import java.util.ArrayList;

/**
 * Created by fenglei on 15-12-23.
 */
public class BlocksResult {

    public String retcode;
    public String retmessage;
    public String userid;
    public String jobid;
    public ArrayList<Block> polygons;

    public static class Block {
        public String polygonno;
        public String polygonname;
        public String polycount;
        public ArrayList<GeoInfo> polygon;
    }

    public static class GeoInfo {
        public String lat;
        public String lng;
    }

}
