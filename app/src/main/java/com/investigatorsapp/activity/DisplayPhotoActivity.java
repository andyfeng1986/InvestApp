package com.investigatorsapp.activity;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.investigatorsapp.R;
import com.investigatorsapp.utils.Util;

import java.io.File;

/**
 * Created by fenglei on 16/4/10.
 */
public class DisplayPhotoActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_display);
        String path = getIntent().getStringExtra("path");
        ImageView iv = (ImageView)findViewById(R.id.iv);
        File file = new File(path);
        Bitmap bitmap = Util.getSmallBitmap(file.getAbsolutePath());
        iv.setImageBitmap(bitmap);
        iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

}
