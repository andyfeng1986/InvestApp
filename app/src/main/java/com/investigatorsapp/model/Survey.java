package com.investigatorsapp.model;

import java.util.List;

/**
 * Created by fenglei on 16/3/30.
 */
public class Survey {

    public String retcode;
    public String retmessage;
    public String userid;
    public String jobid;
    public String audio;
    public List<Photo> photo;
    public List<Question> questions;

    public static class Photo {
        public String id;
        public String text;
    }

    public static class Question {
        public String type;
        public String name;
        public String text;
        public int must;
        public int sort;
        public int hasphoto;
        public List<Option> option;
    }

    public static class Option {
        public String name;
        public String text;
        public String photolink;
        public String maxlen;
    }

}
