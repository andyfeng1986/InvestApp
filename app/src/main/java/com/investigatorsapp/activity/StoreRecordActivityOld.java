package com.investigatorsapp.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.GsonRequest;
import com.google.gson.Gson;
import com.investigatorsapp.R;
import com.investigatorsapp.common.DaoSessionInstance;
import com.investigatorsapp.common.VolleySingleton;
import com.investigatorsapp.db.greendao.Store;
import com.investigatorsapp.db.greendao.StoreDao;
import com.investigatorsapp.logger.Logger;
import com.investigatorsapp.model.SalerNo;
import com.investigatorsapp.model.StoreUpload;
import com.investigatorsapp.network.FileUploaderAsyncHttp;
import com.investigatorsapp.utils.UrlWrapper;
import com.investigatorsapp.utils.Util;
import com.investigatorsapp.widget.AddressLayout;

import java.io.File;

import de.greenrobot.dao.query.QueryBuilder;

/**
 * Created by fenglei on 15-12-24.
 */
public class StoreRecordActivityOld extends BaseActivity implements View.OnClickListener{

    private static final String TAG = StoreRecordActivityOld.class.getSimpleName();

    private class GetDataAsyncTask extends AsyncTask<String, Void, Store> {

        @Override
        protected Store doInBackground(String... params) {
            String salerno = params[0];
            StoreDao dao = DaoSessionInstance.getDaoSession(StoreRecordActivityOld.this).getStoreDao();
            QueryBuilder<Store> queryBuilder = dao.queryBuilder();
            Store store = queryBuilder.where(StoreDao.Properties.Salerno.eq(salerno)).build().unique();
            return store;
        }

        @Override
        protected void onPostExecute(Store store) {
            hintTV.setVisibility(View.GONE);
            contentVG.setVisibility(View.VISIBLE);
            if(store != null) {
                mStore = store;
                setData(store);
            }
        }
    }


    private TextView hintTV;
    private ViewGroup contentVG;

    private LinearLayout accessInfoLL;
    private Spinner accessInfoSpinner;

    private LinearLayout customTypeLL;
    private Spinner customTypeSpinner;

    private LinearLayout channelTypeLL;
    private Spinner channelTypeSpinner;

    //private LinearLayout addressChoiceLL;
//    private Button addressChoiceBtn;
    private AddressLayout addressLayout;

    private LinearLayout customNameLL;
    private LinearLayout contactLL;
    private LinearLayout telphoneLL;
    private LinearLayout emailLL;
    private LinearLayout faxnumLL;
    private LinearLayout fixPhoneLL;
    private LinearLayout youbianLL;
    private LinearLayout addressLL;
    private LinearLayout stationNumLL;
    private LinearLayout repairNumLL;
    private LinearLayout distributionLL;
    private Button distributionBtn;
    private LinearLayout avgoilLL;
    private Spinner avgoilSpinner;

    private EditText customNameET;
    private EditText contactET;
    private EditText telphoneET;
    private EditText emailET;
    private EditText faxnumET;
    private EditText fixPhoneET;
    private EditText youbianET;
    private EditText addressET;
    private EditText stationNumET;
    private EditText repairNumET;

    private Button photoBtn;
    private Button audioBtn;
    private Button commitBtn;
    private Button saveBtn;
    private Store mStore;
    private String mSalerNo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store_record);
        initUI();
        getData();
    }

    private void getData() {
        Intent intent = getIntent();
        final String salerno = intent.getStringExtra("salerno");
//        final String salerno = "55555";
        if(TextUtils.isEmpty(salerno)) {
            String polygonid = intent.getStringExtra("polygonid");
            String lat = intent.getStringExtra("lat");
            String lng = intent.getStringExtra("lng");
            GsonRequest<SalerNo> gsonRequest = new GsonRequest<SalerNo>(Request.Method.POST,
                    UrlWrapper.getSalerNoUrl(), SalerNo.class,
                    UrlWrapper.getSalerNoParams(polygonid, lat, lng), new Response.Listener<SalerNo>() {
                @Override
                public void onResponse(SalerNo response) {
                    if(response != null && !TextUtils.isEmpty(response.salerno)) {
                        mSalerNo = response.salerno;
                        getDataBySalerNo(salerno);
                    }else {
                        Toast.makeText(StoreRecordActivityOld.this, "获取店面编号出错", Toast.LENGTH_LONG).show();
                        finish();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(StoreRecordActivityOld.this, "无法获取店面编号", Toast.LENGTH_LONG).show();
                    finish();
                }
            });
            VolleySingleton.getInstance().addToRequestQueue(gsonRequest);
        }else {
            mSalerNo = salerno;
            getDataBySalerNo(salerno);
        }
    }

    private void getDataBySalerNo(String salerNo) {
        GetDataAsyncTask asyncTask = new GetDataAsyncTask();
        asyncTask.execute(salerNo);
    }

    private void initUI() {
        hintTV = (TextView) findViewById(R.id.hint_tv);
        contentVG = (ViewGroup) findViewById(R.id.contentLL);
        hintTV.setVisibility(View.VISIBLE);
        contentVG.setVisibility(View.GONE);
        commitBtn = (Button) findViewById(R.id.commitBtn);
        saveBtn = (Button) findViewById(R.id.saveBtn);
        photoBtn = (Button) findViewById(R.id.photoBtn);
        audioBtn = (Button) findViewById(R.id.audioBtn);
        photoBtn.setOnClickListener(this);
        audioBtn.setOnClickListener(this);
        commitBtn.setOnClickListener(this);
        saveBtn.setOnClickListener(this);

        accessInfoLL = (LinearLayout)findViewById(R.id.access_info_ll);
        accessInfoSpinner = (Spinner) findViewById(R.id.access_info_spinner);

        customTypeLL = (LinearLayout)findViewById(R.id.custom_type_ll);
        customTypeSpinner = (Spinner) findViewById(R.id.custom_type_spinner);
        customTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long l) {
                ArrayAdapter<String> simpleAdapter;
                if(pos == 0) {
                    simpleAdapter=new ArrayAdapter<String>(StoreRecordActivityOld.this,
                            android.R.layout.simple_spinner_item,
                            getResources().getStringArray(R.array.gas));
                }else {
                    simpleAdapter=new ArrayAdapter<String>(StoreRecordActivityOld.this,
                            android.R.layout.simple_spinner_item,
                            getResources().getStringArray(R.array.diesel));
                }
                simpleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                channelTypeSpinner.setAdapter(simpleAdapter);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        channelTypeLL = (LinearLayout)findViewById(R.id.channel_type_ll);
        channelTypeSpinner = (Spinner) findViewById(R.id.channel_type_spinner);

        customNameLL = (LinearLayout)findViewById(R.id.include_customname);
        customNameET = (EditText) customNameLL.findViewById(R.id.et);
        ((TextView)(customNameLL.findViewById(R.id.tv))).setText("客户名称(必填)");

        contactLL = (LinearLayout) findViewById(R.id.include_contact);
        contactET = (EditText) contactLL.findViewById(R.id.et);
        ((TextView)(contactLL.findViewById(R.id.tv))).setText("联系人");

        telphoneLL = (LinearLayout) findViewById(R.id.include_telphone);
        telphoneET = (EditText) telphoneLL.findViewById(R.id.et);
        telphoneET.setInputType(InputType.TYPE_CLASS_PHONE);
        ((TextView)(telphoneLL.findViewById(R.id.tv))).setText("手机");

        emailLL = (LinearLayout)findViewById(R.id.include_email);
        emailET = (EditText) emailLL.findViewById(R.id.et);
        ((TextView)(emailLL.findViewById(R.id.tv))).setText("电子邮件");

        faxnumLL = (LinearLayout) findViewById(R.id.include_faxnum);
        faxnumET = (EditText) faxnumLL.findViewById(R.id.et);
        faxnumET.setInputType(InputType.TYPE_CLASS_NUMBER);
        ((TextView)(faxnumLL.findViewById(R.id.tv))).setText("传真号");

        fixPhoneLL = (LinearLayout)findViewById(R.id.fixphone);
        fixPhoneET = (EditText) fixPhoneLL.findViewById(R.id.et);
        fixPhoneET.setInputType(InputType.TYPE_CLASS_NUMBER);
        ((TextView)(fixPhoneLL.findViewById(R.id.tv))).setText("电话");

        youbianLL = (LinearLayout)findViewById(R.id.include_youbian);
        youbianET = (EditText) youbianLL.findViewById(R.id.et);
        youbianET.setInputType(InputType.TYPE_CLASS_NUMBER);
        ((TextView)(youbianLL.findViewById(R.id.tv))).setText("邮政编码");

        //addressChoiceLL = (LinearLayout) findViewById(R.id.address_choice_ll);
        addressLayout = (AddressLayout) findViewById(R.id.address_layout);

//        addressChoiceBtn = (Button) findViewById(R.id.address_choice_btn);
//        addressChoiceBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                showPopupWindow(view);
//            }
//        });
        addressLL = (LinearLayout)findViewById(R.id.include_address);
        addressET = (EditText) addressLL.findViewById(R.id.et);
        ((TextView)(addressLL.findViewById(R.id.tv))).setText("地址名称(必填)");

        stationNumLL = (LinearLayout) findViewById(R.id.include_station_num);
        stationNumET = (EditText) stationNumLL.findViewById(R.id.et);
        stationNumET.setInputType(InputType.TYPE_CLASS_NUMBER);
        ((TextView)(stationNumLL.findViewById(R.id.tv))).setText("工位数(必填)");

        repairNumLL = (LinearLayout)findViewById(R.id.include_repair_num);
        repairNumET = (EditText) repairNumLL.findViewById(R.id.et);
        repairNumET.setInputType(InputType.TYPE_CLASS_NUMBER);
        ((TextView)(repairNumLL.findViewById(R.id.tv))).setText("机修工人数");

        distributionLL = (LinearLayout)findViewById(R.id.distribution_ll);
        distributionBtn = (Button) distributionLL.findViewById(R.id.distribution_btn);
        distributionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(StoreRecordActivityOld.this);
                builder.setMultiChoiceItems(R.array.distribution,
                        new boolean[]{false, false, false},
                        new DialogInterface.OnMultiChoiceClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i, boolean b) {

                            }
                        });
                builder.create().show();
            }
        });
        avgoilLL = (LinearLayout)findViewById(R.id.avgoil_ll);
        avgoilSpinner = (Spinner) findViewById(R.id.avg_oil_spinner);
    }

    private void setData(Store store) {
        if(store == null) {
            return;
        }
        customNameET.setText(store.getCustname());
        contactET.setText(store.getCustperson());
        telphoneET.setText(store.getTelephone());
        emailET.setText(store.getEmail());
        faxnumET.setText(store.getFax());
        fixPhoneET.setText(store.getFphone());
        youbianET.setText(store.getZipcode());
        addressET.setText(store.getAddress());
        stationNumET.setText(store.getStationnum());
        repairNumET.setText(store.getWorkernum());
    }

    private boolean checkInput() {
        if(TextUtils.isEmpty(customNameET.getText().toString())) {
            Toast.makeText(this, "客户名称不能为空", Toast.LENGTH_SHORT).show();
            return false;
        }
        if(TextUtils.isEmpty(addressET.getText().toString())) {
            Toast.makeText(this, "地址名称不能为空", Toast.LENGTH_SHORT).show();
            return false;
        }
        if(TextUtils.isEmpty(stationNumET.getText().toString())) {
            Toast.makeText(this, "工位数不能为空", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    @Override
    public void onClick(View v) {
        if(v.equals(saveBtn)) {
            if(checkInput()) {
                if(mStore == null) {
                    mStore = new Store();
                }
                updateStore(mStore);
                StoreDao dao = DaoSessionInstance.getDaoSession(StoreRecordActivityOld.this).getStoreDao();
                dao.insertOrReplace(mStore);
            }
        }else if(v.equals(commitBtn)) {
            if(checkInput()) {
                if(mStore == null) {
                    mStore = new Store();
                }
                updateStore(mStore);
                Gson gson = new Gson();
                String gsonString = gson.toJson(mStore);
                GsonRequest<StoreUpload> gsonRequest = new GsonRequest<StoreUpload>(Request.Method.POST,
                        UrlWrapper.getPostStoreUrl(), StoreUpload.class,
                        UrlWrapper.getPostStoreParams(gsonString), new Response.Listener<StoreUpload>() {
                    @Override
                    public void onResponse(StoreUpload response) {
                        if(response != null && "0000".equals(response.retcode)) {
                            uploadPhoto();
                        }else {
                            Toast.makeText(StoreRecordActivityOld.this, "上传失败", Toast.LENGTH_LONG).show();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(StoreRecordActivityOld.this, "上传失败", Toast.LENGTH_LONG).show();
                    }
                });
                VolleySingleton.getInstance().addToRequestQueue(gsonRequest);
            }
        }else if(v.equals(photoBtn)) {
            clickPhotoBtn();
        }else if(v.equals(audioBtn)) {
            clickAudioBtn();
        }
    }

    public void updateDbAfterCommitSuccess() {
        StoreDao dao = DaoSessionInstance.getDaoSession(StoreRecordActivityOld.this).getStoreDao();
//        mStore.setUploaded(true);
        dao.insertOrReplace(mStore);
    }

    private void uploadPhoto() {
//        Util.uploadPhotoFile(mSalerNo, mStore.getTime(), new FileUploaderAsyncHttp.UpLoaderCallback() {
//            @Override
//            public void onSuccess(String response) {
//                uploadAudio();
//            }
//
//            @Override
//            public void onFailed(int responseCode, String failReason) {
//
//            }
//        });
    }

    private void uploadAudio() {
        Util.uploadAudioFile(mSalerNo, mStore.getTime(), new FileUploaderAsyncHttp.UpLoaderCallback() {
            @Override
            public void onSuccess(String response) {
                updateDbAfterCommitSuccess();
            }

            @Override
            public void onFailed(int responseCode, String failReason) {

            }
        });
    }

    private void clickPhotoBtn() {
        File file = new File(Util.getAudioFilePath(mSalerNo, mStore.getTime()));
        Logger.d(TAG, "file path = " + file.getAbsolutePath());
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
        startActivityForResult(intent, 1);
    }

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        if (requestCode == 1) {
//            if (resultCode == RESULT_OK) {
//                Bitmap bm = (Bitmap) data.getExtras().get("data");
//                File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
//                        mSalerNo + ".jpg");
//                try {
//                    BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
//                    bm.compress(Bitmap.CompressFormat.JPEG, 80, bos);
//                    bos.flush();
//                    bos.close();
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//    }

    private void clickAudioBtn() {
        if(audioBtn.getText().equals("开始录音")
                || audioBtn.getText().equals("重新录音")) {
            try {
                boolean sdCardExist = Environment.getExternalStorageState().
                        equals(Environment.MEDIA_MOUNTED);
                if(!sdCardExist) {
                    Toast.makeText(this, "sd卡不存在", Toast.LENGTH_SHORT).show();
                    return;
                }
                String fileName = Util.getAudioFilePath(mSalerNo, mStore.getTime());
                File file = new File(fileName);
                if(file.exists()) {
                    file.delete();
                }
                if(mMediaRecorder == null) {
                    mMediaRecorder = new MediaRecorder();
                    mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                    mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                    mMediaRecorder.setOutputFile(fileName);
                    mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
                }
                mMediaRecorder.prepare();
                mMediaRecorder.start();
                audioBtn.setText("停止录音");
            } catch (Exception e) {
                Logger.e(TAG, "mMediaRecorder start failed");
            }
        }else if(audioBtn.getText().equals("停止录音")) {
            mMediaRecorder.stop();
            mMediaRecorder.release();
            mMediaRecorder = null;
            audioBtn.setText("重新录音");
        }
    }

    private MediaRecorder mMediaRecorder;

    private void updateStore(Store store) {
        store.setCustname(customNameET.getText().toString());
        store.setCustperson(contactET.getText().toString());
        store.setTelephone(telphoneET.getText().toString());
        store.setEmail(emailET.getText().toString());
        store.setFax(faxnumET.getText().toString());
        store.setFphone(fixPhoneET.getText().toString());
        store.setZipcode(youbianET.getText().toString());
        store.setAddress(addressET.getText().toString());
        store.setStationnum(stationNumET.getText().toString());
        store.setWorkernum(repairNumET.getText().toString());
    }

//    private void showPopupWindow(View view) {
//        addressLayout = (AddressLayout) LayoutInflater.from(this).inflate(
//                R.layout.wheel_address, null);
//
//        final PopupWindow popupWindow = new PopupWindow(addressLayout,
//                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, true);
//
//        popupWindow.setTouchable(true);
//        popupWindow.setTouchInterceptor(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View view, MotionEvent motionEvent) {
//                return false;
//            }
//        });
//        popupWindow.setBackgroundDrawable(getResources().getDrawable(R.drawable.popup));
//        popupWindow.showAsDropDown(view);
//
//    }


}
