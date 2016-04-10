package com.investigatorsapp.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.investigatorsapp.R;
import com.investigatorsapp.logger.Logger;
import com.investigatorsapp.utils.Util;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by fenglei on 16/4/10.
 */
public class DynamicPhotoActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = "DynamicPhotoActivity";

    private String mSalerNo;
    private String mEnterTime;
    ArrayList<String> photoTextList;
    ArrayList<String> photoNoList;
    ArrayList<ViewHolder> viewHolderList;
    private int mCurIndex;

    class ViewHolder {
        public ImageView iv;
        public Button btn;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dynamic_photo);
        mSalerNo = getIntent().getStringExtra("salerno");
        mEnterTime = getIntent().getStringExtra("enter_time");
        photoTextList = getIntent().getStringArrayListExtra("text");
        photoNoList = getIntent().getStringArrayListExtra("no");
        viewHolderList = new ArrayList<>();
        initUI();
        setUI();
    }

    private void initUI() {
        int size = 0;
        if(photoTextList != null) {
            size = photoTextList.size();
        }
        initViewHolder(R.id.include_1, 0, size);
        initViewHolder(R.id.include_2, 1, size);
        initViewHolder(R.id.include_3, 2, size);
        initViewHolder(R.id.include_4, 3, size);
        initViewHolder(R.id.include_5, 4, size);
    }

    private void initViewHolder(int id, int index, int size) {
        View view = findViewById(id);
        if(index < size) {
            ViewHolder viewHolder = new ViewHolder();
            viewHolder.btn = (Button)view.findViewById(R.id.photo_btn);
            viewHolder.btn.setOnClickListener(this);
            viewHolder.btn.setTag(index);
            viewHolder.iv = (ImageView) view.findViewById(R.id.photo_iv);
            viewHolder.iv.setTag(index);
            viewHolder.iv.setOnClickListener(this);
            viewHolderList.add(viewHolder);
        } else {
            view.setVisibility(View.GONE);
        }
    }

    private void setUI() {
        for(int i = 0; i < viewHolderList.size(); i++) {
            viewHolderList.get(i).btn.setText(photoTextList.get(i));
            File file = new File(Util.getPhotoFilePath(mSalerNo + "_" +
                    photoNoList.get(i), mEnterTime));
            if(file.exists()) {
                Bitmap bitmap = Util.getSmallBitmap(file.getAbsolutePath());
                viewHolderList.get(i).iv.setImageBitmap(bitmap);
            }
        }
    }

    @Override
    public void onClick(View v) {
        int index = (Integer)v.getTag();
        if(v instanceof Button) {
            mCurIndex = index;
            clickPhotoBtn(index);
        } else if(v instanceof ImageView) {
            clickImageView(index);
        }
    }

    private void clickPhotoBtn(int index) {
        File file = new File(Util.getPhotoFilePath(mSalerNo + "_" + photoNoList.get(index), mEnterTime));
        Logger.d(TAG, "file path = " + file.getAbsolutePath());
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
        startActivityForResult(intent, 1);
    }

    private void clickImageView(int index) {
        String path = Util.getPhotoFilePath(mSalerNo + "_" + photoNoList.get(index), mEnterTime);
        Intent intent = new Intent(this, DisplayPhotoActivity.class);
        intent.putExtra("path", path);
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                File file = new File(Util.getPhotoFilePath(mSalerNo + "_" +
                        photoNoList.get(mCurIndex), mEnterTime));
                Bitmap bitmap = Util.getSmallBitmap(file.getAbsolutePath());
                viewHolderList.get(mCurIndex).iv.setImageBitmap(bitmap);
                Util.saveImage(bitmap, file.getAbsolutePath());
            }
        }
    }

}
