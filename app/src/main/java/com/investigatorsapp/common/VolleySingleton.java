package com.investigatorsapp.common;

import android.content.Context;
import android.graphics.Bitmap;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;

/**
 * Created by fenglei on 15/12/21.
 */
public class VolleySingleton {

    private static final VolleySingleton instance = new VolleySingleton();

    private RequestQueue requestQueue;
    private ImageLoader imageLoader;

    private VolleySingleton() {

    }

    public static VolleySingleton getInstance() {
        return instance;
    }

    public void init(Context context) {
        requestQueue = Volley.newRequestQueue(context);
        imageLoader = new ImageLoader(requestQueue, new ImageLoader.ImageCache() {
            @Override
            public Bitmap getBitmap(String url) {
                return null;
            }

            @Override
            public void putBitmap(String url, Bitmap bitmap) {

            }
        });
    }

    public <T> void addToRequestQueue(Request<T> request) {
        requestQueue.add(request);
    }

    public ImageLoader getImageLoader() {
        return imageLoader;
    }

}
