package com.investigatorsapp.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.GsonRequest;
import com.google.gson.Gson;
import com.investigatorsapp.R;
import com.investigatorsapp.adapter.DistributionAdapter;
import com.investigatorsapp.common.DaoSessionInstance;
import com.investigatorsapp.common.LocationReport;
import com.investigatorsapp.common.SalernoManager;
import com.investigatorsapp.common.UserSingleton;
import com.investigatorsapp.common.VolleySingleton;
import com.investigatorsapp.db.greendao.Store;
import com.investigatorsapp.db.greendao.StoreDao;
import com.investigatorsapp.logger.Logger;
import com.investigatorsapp.model.Gps;
import com.investigatorsapp.model.StoreUpload;
import com.investigatorsapp.network.FileUploaderAsyncHttp;
import com.investigatorsapp.utils.Constant;
import com.investigatorsapp.utils.UrlWrapper;
import com.investigatorsapp.utils.Util;
import com.investigatorsapp.widget.AddressLayout;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Date;

import de.greenrobot.dao.query.QueryBuilder;

/**
 * Created by fenglei on 15-12-24.
 */
public class StoreRecordActivity extends BaseActivity implements View.OnClickListener{

    private static final String TAG = StoreRecordActivity.class.getSimpleName();

    private static class MediaRecorderHandler extends Handler {

        private WeakReference<StoreRecordActivity> weakReference;

        public MediaRecorderHandler(StoreRecordActivity activity) {
            weakReference = new WeakReference<StoreRecordActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            StoreRecordActivity activity = weakReference.get();
            if(activity != null) {
                activity.stopMediaRecorder();
            }
        }
    }

    private class GetDataAsyncTask extends AsyncTask<String, Void, Store> {

        @Override
        protected Store doInBackground(String... params) {
            String salerno = params[0];
            StoreDao dao = DaoSessionInstance.getDaoSession(StoreRecordActivity.this).getStoreDao();
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
                mPolygonid = mStore.getPolygonid();
                mLat1 = mStore.getLat1();
                mLng1 = mStore.getLng1();
                mPolyname = mStore.getPolygonname();
                mEnterTime = mStore.getTime();
                setUI(store);
            }else {
                mEnterTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
                polynameTV.setText("区块: " + mPolyname);
                latTV.setText("纬度: " + mLat1);
                lngTV.setText("经度: " + mLng1);
                startMediaRecorder();
                mediaRecorderHandler = new MediaRecorderHandler(StoreRecordActivity.this);
                mediaRecorderHandler.sendEmptyMessageDelayed(0, 20 * 60 * 1000);
            }
            initAddressLayout(store);
        }
    }


    private TextView hintTV;
    private ViewGroup contentVG;

    private Spinner accessInfoSpinner;

    private Spinner customTypeSpinner;

    private Spinner channelTypeSpinner;
    private int channelTypePos = 0;

    private AddressLayout addressLayout;

    private TextView polynameTV;
    private TextView latTV;
    private TextView lngTV;
    private ViewGroup customNameLL;
    private ViewGroup contactLL;
    private ViewGroup telphoneLL;
    private ViewGroup emailLL;
    private ViewGroup faxnumLL;
    private ViewGroup fixPhoneLL;
    private ViewGroup youbianLL;
    private ViewGroup addressLL;
    private ViewGroup stationNumLL;
    private ViewGroup repairNumLL;
    private ViewGroup distributionLL;
    private Button distributionBtn;
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

//    boolean[] distributionSelected = new boolean[]{false,false,false};

    private Button photoBtn;
    private Button audioBtn;
    private Button commitBtn;
    private Button saveBtn;
    private Store mStore;
    private String mSalerNo;
    private String mPolygonid;
    private String mLat1;
    private String mLng1;
    private String mPolyname;
    private String mEnterTime;

    private MediaRecorderHandler mediaRecorderHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Logger.d(TAG, "onCreate, savedInstanceState = " + savedInstanceState);
        setContentView(R.layout.activity_store_record);
        initUI();
        getData();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopMediaRecorder();
        if(!TextUtils.isEmpty(addressLayout.getPronvice())) {
            Util.savePronvice(this, addressLayout.getPronvice());
        }
        if(!TextUtils.isEmpty(addressLayout.getCity())) {
            Util.saveCtiy(this, addressLayout.getCity());
        }
        if(!TextUtils.isEmpty(addressLayout.getArea())) {
            Util.saveArea(this, addressLayout.getArea());
        }
    }

    private void getData() {
        Intent intent = getIntent();
        mSalerNo = intent.getStringExtra("salerno");
        mPolygonid = intent.getStringExtra("polygonid");
        mLat1 = intent.getStringExtra("lat");
        mLng1 = intent.getStringExtra("lng");
        mPolyname = intent.getStringExtra("polyname");
        Logger.d(TAG, "mSalerNo = " + mSalerNo + ", polygonid = " + mPolygonid
            + ", mPolyname = " + mPolyname);
        if(TextUtils.isEmpty(mSalerNo)) {
            Toast.makeText(StoreRecordActivity.this, "无法获取店面编号", Toast.LENGTH_LONG).show();
            finish();
//            String lat = intent.getStringExtra("lat");
//            String lng = intent.getStringExtra("lng");
//            GsonRequest<SalerNo> gsonRequest = new GsonRequest<SalerNo>(Request.Method.POST,
//                    UrlWrapper.getSalerNoUrl(), SalerNo.class,
//                    UrlWrapper.getSalerNoParams(mPolygonid, lat, lng), new Response.Listener<SalerNo>() {
//                @Override
//                public void onResponse(SalerNo response) {
//                    if(response != null && !TextUtils.isEmpty(response.salerno)) {
//                        mSalerNo = response.salerno;
//                        getDataBySalerNo(mSalerNo);
//                    }else {
//                        Toast.makeText(StoreRecordActivity.this, "获取店面编号出错", Toast.LENGTH_LONG).show();
//                        finish();
//                    }
//                }
//            }, new Response.ErrorListener() {
//                @Override
//                public void onErrorResponse(VolleyError error) {
//                    Toast.makeText(StoreRecordActivity.this, "无法获取店面编号", Toast.LENGTH_LONG).show();
//                    finish();
//                }
//            });
//            VolleySingleton.getInstance().addToRequestQueue(gsonRequest);
        }else {
            getDataBySalerNo(mSalerNo);
        }
    }

    private void getDataBySalerNo(String salerNo) {
        GetDataAsyncTask asyncTask = new GetDataAsyncTask();
        asyncTask.execute(salerNo);
    }

    private void initUI() {
        polynameTV = (TextView) findViewById(R.id.polyname);
        latTV = (TextView) findViewById(R.id.lat);
        lngTV = (TextView) findViewById(R.id.lng);
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

        accessInfoSpinner = (Spinner) findViewById(R.id.access_info_spinner);

        customNameLL = (ViewGroup)findViewById(R.id.include_customname);
        customNameET = (EditText) customNameLL.findViewById(R.id.et);
        customNameET.setFilters(new InputFilter[]{new InputFilter.LengthFilter(128)});
        ((TextView)(customNameLL.findViewById(R.id.tv))).setText("客户名称*");

        contactLL = (ViewGroup) findViewById(R.id.include_contact);
        contactET = (EditText) contactLL.findViewById(R.id.et);
        contactET.setFilters(new InputFilter[]{new InputFilter.LengthFilter(128)});
        ((TextView)(contactLL.findViewById(R.id.tv))).setText("联系人*");

        telphoneLL = (ViewGroup) findViewById(R.id.include_telphone);
        telphoneET = (EditText) telphoneLL.findViewById(R.id.et);
        telphoneET.setInputType(InputType.TYPE_CLASS_PHONE);
        telphoneET.setFilters(new InputFilter[]{new InputFilter.LengthFilter(40)});
        ((TextView)(telphoneLL.findViewById(R.id.tv))).setText("电话");

        emailLL = (ViewGroup)findViewById(R.id.include_email);
        emailET = (EditText) emailLL.findViewById(R.id.et);
        emailET.setFilters(new InputFilter[]{new InputFilter.LengthFilter(40)});
        ((TextView)(emailLL.findViewById(R.id.tv))).setText("电子邮件");

        faxnumLL = (ViewGroup) findViewById(R.id.include_faxnum);
        faxnumET = (EditText) faxnumLL.findViewById(R.id.et);
        faxnumET.setInputType(InputType.TYPE_CLASS_NUMBER);
        faxnumET.setFilters(new InputFilter[]{new InputFilter.LengthFilter(40)});
        ((TextView)(faxnumLL.findViewById(R.id.tv))).setText("传真号");

        fixPhoneLL = (ViewGroup)findViewById(R.id.fixphone);
        fixPhoneET = (EditText) fixPhoneLL.findViewById(R.id.et);
        fixPhoneET.setInputType(InputType.TYPE_CLASS_NUMBER);
        fixPhoneET.setFilters(new InputFilter[]{new InputFilter.LengthFilter(40)});
        ((TextView)(fixPhoneLL.findViewById(R.id.tv))).setText("电话");

        youbianLL = (ViewGroup)findViewById(R.id.include_youbian);
        youbianET = (EditText) youbianLL.findViewById(R.id.et);
        youbianET.setInputType(InputType.TYPE_CLASS_NUMBER);
        youbianET.setFilters(new InputFilter[]{new InputFilter.LengthFilter(11)});
        ((TextView)(youbianLL.findViewById(R.id.tv))).setText("邮政编码");

        addressLL = (ViewGroup)findViewById(R.id.include_address);
        addressET = (EditText) addressLL.findViewById(R.id.et);
        addressET.setFilters(new InputFilter[]{new InputFilter.LengthFilter(128)});
        ((TextView)(addressLL.findViewById(R.id.tv))).setText("详细地址*");

        stationNumLL = (ViewGroup) findViewById(R.id.include_station_num);
        stationNumET = (EditText) stationNumLL.findViewById(R.id.et);
        stationNumET.setInputType(InputType.TYPE_CLASS_NUMBER);
        stationNumET.setFilters(new InputFilter[]{new InputFilter.LengthFilter(11)});
        ((TextView)(stationNumLL.findViewById(R.id.tv))).setText("工位数*");

        repairNumLL = (ViewGroup)findViewById(R.id.include_repair_num);
        repairNumET = (EditText) repairNumLL.findViewById(R.id.et);
        repairNumET.setInputType(InputType.TYPE_CLASS_NUMBER);
        repairNumET.setFilters(new InputFilter[]{new InputFilter.LengthFilter(11)});
        ((TextView)(repairNumLL.findViewById(R.id.tv))).setText("机修工人数*");

        customTypeSpinner = (Spinner) findViewById(R.id.custom_type_spinner);
        customTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long l) {
                ArrayAdapter<String> simpleAdapter;
                if(pos == 0) {
                    simpleAdapter=new ArrayAdapter<String>(StoreRecordActivity.this,
                            android.R.layout.simple_spinner_item,
                            getResources().getStringArray(R.array.gas));
                }else {
                    simpleAdapter=new ArrayAdapter<String>(StoreRecordActivity.this,
                            android.R.layout.simple_spinner_item,
                            getResources().getStringArray(R.array.diesel));
                }
                simpleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                channelTypeSpinner.setAdapter(simpleAdapter);
                if(channelTypePos != 0) {
                    channelTypeSpinner.setSelection(channelTypePos);
                    channelTypePos = 0;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        channelTypeSpinner = (Spinner) findViewById(R.id.channel_type_spinner);

        distributionLL = (ViewGroup)findViewById(R.id.distribution_ll);
        distributionBtn = (Button) distributionLL.findViewById(R.id.distribution_btn);
        distributionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                AlertDialog.Builder builder = new AlertDialog.Builder(StoreRecordActivity.this);
//                builder.setMultiChoiceItems(R.array.distribution, distributionSelected,
//                        new DialogInterface.OnMultiChoiceClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialogInterface, int i, boolean b) {
//                                distributionSelected[i] = b;
//                            }
//                        }).setPositiveButton("确定", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialogInterface, int i) {
//                        dialogInterface.dismiss();
//                    }
//                });
//                builder.create().show();
                showDitributionView();
            }
        });

        avgoilSpinner = (Spinner) findViewById(R.id.avg_oil_spinner);

        addressLayout = (AddressLayout) findViewById(R.id.address_layout);
    }

    private final String[] distributionTexts = new String[] {"壳牌", "嘉实多", "美孚"};
    private final int[] distributionIcons = new int[] {R.drawable.shell,
            R.drawable.castol, R.drawable.mobil};
    private final boolean[] distributionSelects = new boolean[] {false, false, false};

    private void showDitributionView(){
        AlertDialog alertDialog;
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.distribution_select_dialog, null);
        ListView lv = (ListView) layout.findViewById(R.id.mylistview);
        final DistributionAdapter adapter = new DistributionAdapter(this,
                distributionTexts, distributionIcons, distributionSelects);
        lv.setAdapter(adapter);
        alertDialog = new AlertDialog.Builder(this).setView(layout).create();
        alertDialog.show();
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                    long arg3) {
                DistributionAdapter.ViewHolder holder =
                        (DistributionAdapter.ViewHolder) arg1.getTag();
                holder.cb.toggle();
                boolean[] selecteds = adapter.getSelecteds();
                selecteds[arg2] = holder.cb.isChecked();
            }
        });
    }

    private void setUI(Store store) {
        if(store == null) {
            return;
        }
        polynameTV.setText("区块: " + store.getPolygonname());
        latTV.setText("纬度: " + store.getLat1());
        lngTV.setText("经度: " + store.getLng1());
        accessInfoSpinner.setSelection(Integer.parseInt(store.getStatus()));
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

        setSpinnerDefaultValue(customTypeSpinner, R.array.custom_info, store.getCusttype());
        if(customTypeSpinner.getSelectedItemPosition() == 0) {
            channelTypePos = setSpinnerDefaultValue(channelTypeSpinner, R.array.gas, store.getChanneltype());
        }else {
            channelTypePos = setSpinnerDefaultValue(channelTypeSpinner, R.array.diesel, store.getChanneltype());
        }
        if("1".equals(store.getIsshell())) {
            distributionSelects[0] = true;
        }
        if("1".equals(store.getIscastrol())) {
            distributionSelects[1] = true;
        }
        if("1".equals(store.getIsmobil())) {
            distributionSelects[2] = true;
        }
        if(!TextUtils.isEmpty(store.getMonthoil())) {
            try {
                avgoilSpinner.setSelection(Integer.parseInt(store.getMonthoil()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
//        addressLayout.setPronvice(store.getProvince());
//        addressLayout.setCity(store.getCity());
//        addressLayout.setArea(store.getDistrict());
    }

    private void initAddressLayout(Store store) {
        String province = "";
        String city = "";
        String area = "";
        if(store != null) {
            province = store.getProvince();
            city = store.getCity();
            area = store.getDistrict();
        }
        if(TextUtils.isEmpty(province)) {
            province = Util.getPronvice(this);
        }
        if(TextUtils.isEmpty(city)) {
            city = Util.getCity(this);
        }
        if(TextUtils.isEmpty(area)) {
            area = Util.getArea(this);
        }
        addressLayout.setPronvice(province);
        addressLayout.setCity(city);
        addressLayout.setArea(area);
    }

    private int setSpinnerDefaultValue(Spinner spinner, int id, String value) {
        String[] strings = getResources().getStringArray(id);
        if(strings != null) {
            for(int i = 0; i < strings.length; i++) {
                if(strings[i].equals(value)) {
                    spinner.setSelection(i);
                    return i;
                }
            }
        }
        return 0;
    }

    private boolean checkInput() {
        File file = new File(Util.getPhotoFilePath(mSalerNo, mEnterTime));
        if(!file.exists()) {
            Toast.makeText(this, "未拍照，请拍照后再保存或提交", Toast.LENGTH_SHORT).show();
            return false;
        }
        if(TextUtils.isEmpty(customNameET.getText().toString())) {
            Toast.makeText(this, "客户名称不能为空", Toast.LENGTH_SHORT).show();
            return false;
        }
        if(TextUtils.isEmpty(contactET.getText().toString())) {
            Toast.makeText(this, "联系人不能为空", Toast.LENGTH_SHORT).show();
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
        if(TextUtils.isEmpty(repairNumET.getText().toString())) {
            Toast.makeText(this, "机修工人数不能为空", Toast.LENGTH_SHORT).show();
            return false;
        }
        if(channelTypeSpinner.getSelectedItemPosition() == 0) {
            Toast.makeText(this, "渠道类型不能为空", Toast.LENGTH_SHORT).show();
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
                stopMediaRecorder();
                updateStoreFromUI(mStore);
                StoreDao dao = DaoSessionInstance.getDaoSession(StoreRecordActivity.this).getStoreDao();
                Store store = dao.queryBuilder().where(StoreDao.Properties.Salerno.eq(mSalerNo)).build().unique();
                if(store != null) {
                    dao.insertOrReplace(mStore);
                }else {
                    dao.insertOrReplace(mStore);
                    SalernoManager.getInstance().updatePolycountHashMap(mPolygonid, mSalerNo);
//                    MyApp.updatePolycountHashMap(mPolygonid);
                }
                Toast.makeText(this, "问卷已保存, 请后续在店面页面提交", Toast.LENGTH_LONG).show();
            }
        }else if(v.equals(commitBtn)) {
            stopMediaRecorder();
            commit();
            LocationReport.reportLocation(this);
        }else if(v.equals(photoBtn)) {
            clickPhotoBtn();
        }else if(v.equals(audioBtn)) {
            clickAudioBtn();
        }
    }

    private void commit() {
        if(checkInput()) {
            createProgress("提交", "正在提交，请稍候");
            if(mStore == null) {
                mStore = new Store();
            }
            updateStoreFromUI(mStore);
            Gson gson = new Gson();
            String gsonString = gson.toJson(mStore);
            String resultGsonString = new StringBuilder().append("{\"type\":\"postQst\",")
                    .append(gsonString.substring(1)).toString();
            GsonRequest<StoreUpload> gsonRequest = new GsonRequest<StoreUpload>(Request.Method.POST,
                    UrlWrapper.getPostStoreUrl(), StoreUpload.class,
                    UrlWrapper.getPostStoreParams(resultGsonString), new Response.Listener<StoreUpload>() {
                @Override
                public void onResponse(StoreUpload response) {
                    if(response != null) {
                        if(Constant.RET_SUCCESS_CODE.equals(response.retcode)) {
                            uploadPhoto();
                        }else if(Constant.RET_DUP_COMMIT_CODE.equals(response.retcode)){
                            uploadPhoto();
                        }else {
                            commitFail(response.retmessage);
                        }
                    }else {
                        commitFail("");
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    commitFail("");
                }
            });
            VolleySingleton.getInstance().addToRequestQueue(gsonRequest);
        }
    }


    private void uploadPhoto() {
        final File file = new File(Util.getPhotoFilePath(mSalerNo, mEnterTime));
        if(file.exists()) {
            Util.uploadPhotoFile(mSalerNo, mEnterTime, new FileUploaderAsyncHttp.UpLoaderCallback() {
                @Override
                public void onSuccess(String response) {
//                    if (file != null && file.exists()) {
//                        file.delete();
//                    }
                    uploadAudio();
                }

                @Override
                public void onFailed(int responseCode, String failReason) {
                    commitFail(failReason);
                }
            });
        }else {
            uploadAudio();
        }
    }

    private void uploadAudio() {
        final File file = new File(Util.getAudioFilePath(mSalerNo, mEnterTime));
        if(file.exists()) {
            Util.uploadAudioFile(mSalerNo, mEnterTime, new FileUploaderAsyncHttp.UpLoaderCallback() {
                @Override
                public void onSuccess(String response) {
                    if (file != null && file.exists()) {
                        file.delete();
                    }
                    commitSuccess();
                }

                @Override
                public void onFailed(int responseCode, String failReason) {
                    commitFail(failReason);
                }
            });
        }else {
            commitSuccess();
        }
    }

    private void commitSuccess() {
        StoreDao dao = DaoSessionInstance.getDaoSession(StoreRecordActivity.this).getStoreDao();
        Store store = dao.queryBuilder().where(StoreDao.Properties.Salerno.eq(mSalerNo)).build().unique();
        if(store != null) {
            dao.delete(mStore);
        }else {
            SalernoManager.getInstance().updatePolycountHashMap(mPolygonid, mSalerNo);
//            MyApp.updatePolycountHashMap(mPolygonid);
        }
        Toast.makeText(StoreRecordActivity.this, "上传成功", Toast.LENGTH_LONG).show();
        dissmissProgress();
        finish();
    }

    private void commitFail(String message) {
        if(TextUtils.isEmpty(message)) {
            Toast.makeText(StoreRecordActivity.this, "上传失败", Toast.LENGTH_LONG).show();
        }else {
            Toast.makeText(StoreRecordActivity.this, message, Toast.LENGTH_LONG).show();
        }
        dissmissProgress();
    }

    private void clickPhotoBtn() {
//my photo
        File file = new File(Util.getPhotoFilePath(mSalerNo, mEnterTime));
        Logger.d(TAG, "file path = " + file.getAbsolutePath());
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
        startActivityForResult(intent, 1);

//        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//        startActivityForResult(intent, 1);

//        Intent intent = new Intent(this, TakePhotoActivity.class);
//        intent.putExtra("saler_no", mSalerNo);
//        intent.putExtra("enter_time", mEnterTime);
//        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
//my photo
                File file = new File(Util.getPhotoFilePath(mSalerNo, mEnterTime));
                Bitmap bitmap = getSmallBitmap(file.getAbsolutePath());
                saveImage(bitmap, file.getAbsolutePath());

//                Bitmap markPre = BitmapFactory.decodeResource(this.getResources(), R.drawable.logo);
//                int distWidth = bm.getWidth() / 5;
//                int distHeight = bm.getHeight() / 5;
//                float scalex = (float)distWidth / markPre.getWidth();
//                float scaley = (float)distHeight / markPre.getHeight();
//                Matrix matrix = new Matrix();
//                matrix.postScale(scalex, scaley);
//                Bitmap mark = Bitmap.createBitmap(markPre, 0, 0,
//                        markPre.getWidth(), markPre.getHeight(), matrix, true);
//                markPre.recycle();
//                Bitmap photoMark = Bitmap.createBitmap(bm.getWidth(),
//                        bm.getHeight(), Bitmap.Config.ARGB_8888);
//                Canvas canvas = new Canvas(photoMark);
//                canvas.drawBitmap(bm, 0, 0, null);
//                canvas.drawBitmap(mark, bm.getWidth() - bm.getWidth() / 5,
//                        bm.getHeight() - bm.getHeight() / 5, null);
//                canvas.save(Canvas.ALL_SAVE_FLAG);
//                canvas.restore();

//                try {
//                    File file = new File(Util.getPhotoFilePath(mSalerNo, mEnterTime));
//                    Bitmap bm = (Bitmap) data.getExtras().get("data");
//                    BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
//                    bm.compress(Bitmap.CompressFormat.JPEG, 100, bos);
//                    bos.flush();
//                    bos.close();
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
            }
        }
    }

    public static int calculateInSampleSize(BitmapFactory.Options options,int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int heightRatio = Math.round((float) height/ (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        return inSampleSize;
    }

    public static Bitmap getSmallBitmap(String filePath) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, 240, 320);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;

        return BitmapFactory.decodeFile(filePath, options);
    }

    public static void saveImage(Bitmap photo, String spath) {
        try {
            BufferedOutputStream bos = new BufferedOutputStream(
                    new FileOutputStream(spath, false));
            photo.compress(Bitmap.CompressFormat.JPEG, 100, bos);
            bos.flush();
            bos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private MediaRecorder mMediaRecorder;

    private void startMediaRecorder() {
        try {
//            boolean sdCardExist = Environment.getExternalStorageState().
//                    equals(Environment.MEDIA_MOUNTED);
//            if (!sdCardExist) {
//                Toast.makeText(this, "sd卡不存在", Toast.LENGTH_SHORT).show();
//                return;
//            }
            String fileName = Util.getAudioFilePath(mSalerNo, mEnterTime);
            File file = new File(fileName);
            if (file.exists()) {
                file.delete();
            }
            if (mMediaRecorder == null) {
                mMediaRecorder = new MediaRecorder();
                mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                mMediaRecorder.setOutputFile(fileName);
                mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            }
            mMediaRecorder.prepare();
            mMediaRecorder.start();
        } catch (Exception e) {
            Logger.e(TAG, "mMediaRecorder start failed");
        }
    }

    private void stopMediaRecorder() {
        try {
            if(mMediaRecorder != null) {
                mMediaRecorder.stop();
                mMediaRecorder.release();
                mMediaRecorder = null;
            }
        } catch (Exception e) {
            Logger.e(TAG, "mMediaRecorder stop failed");
            e.printStackTrace();
        }
    }

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
                String fileName = Util.getAudioFilePath(mSalerNo, mEnterTime);
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

    private void updateStoreFromUI(Store store) {
//        store.setUserid(UserSingleton.getInstance().getUser().userid);
//        store.setJobid(UserSingleton.getInstance().getUser().jobid);
//        store.setToken(UserSingleton.getInstance().getUser().token);
        if(TextUtils.isEmpty(store.getUserid())) {
            store.setUserid(UserSingleton.getInstance().getUser().userid);
        }
        if(TextUtils.isEmpty(store.getJobid())) {
            store.setJobid(UserSingleton.getInstance().getUser().jobid);
        }
        if(TextUtils.isEmpty(store.getToken())) {
            store.setToken(UserSingleton.getInstance().getUser().token);
        }
        store.setSalerno(mSalerNo);
        store.setPolygonid(mPolygonid);
        store.setPolygonname(mPolyname);
        store.setTime(mEnterTime);
        store.setStatus(String.valueOf(accessInfoSpinner.getSelectedItemPosition()));
        store.setCustname(customNameET.getText().toString());
        store.setCustperson(contactET.getText().toString());
        store.setTelephone(telphoneET.getText().toString());
        store.setEmail(emailET.getText().toString());
        store.setFax(faxnumET.getText().toString());
        store.setFphone(fixPhoneET.getText().toString());
        store.setCountry("中国");
        store.setProvince(addressLayout.getPronvice());
        store.setCity(addressLayout.getCity());
        store.setDistrict(addressLayout.getArea());
        store.setZipcode(youbianET.getText().toString());
        store.setAddress(addressET.getText().toString());
        store.setStationnum(stationNumET.getText().toString());
        store.setWorkernum(repairNumET.getText().toString());
        store.setCusttype(customTypeSpinner.getSelectedItem().toString());
        store.setChanneltype(channelTypeSpinner.getSelectedItem().toString());
        if(customTypeSpinner.getSelectedItemPosition() == 0) {//汽机油
            if(channelTypeSpinner.getSelectedItemPosition() == 1) {
                store.setProducttype("B2B");
            }else {
                store.setProducttype("B2C");
            }
        }else {
            if(channelTypeSpinner.getSelectedItemPosition() == 3
                    || channelTypeSpinner.getSelectedItemPosition() == 5) {
                store.setProducttype("B2B");
            }else {
                store.setProducttype("B2C");
            }
        }
        store.setLat1(mLat1);
        store.setLng1(mLng1);
        try {
            Gps gps = Util.bd09_To_Gps84(Double.parseDouble(mLat1), Double.parseDouble(mLng1));
            if(gps != null) {
                store.setLat2(String.valueOf(gps.getWgLat()));
                store.setLng2(String.valueOf(gps.getWgLon()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(distributionSelects[0]) {
            store.setIsshell("1");
        }
        if(distributionSelects[1]) {
            store.setIscastrol("1");
        }
        if(distributionSelects[2]) {
            store.setIsmobil("1");
        }
        store.setMonthoil(String.valueOf(avgoilSpinner.getSelectedItemPosition()));
        File photoFile = new File(Util.getPhotoFilePath(mSalerNo, mEnterTime));
        if(photoFile.exists()) {
            store.setPhotoname(photoFile.getName());
        }
        File audioFile = new File(Util.getAudioFilePath(mSalerNo, mEnterTime));
        if(audioFile.exists()) {
            store.setAudioname(audioFile.getName());
        }
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK) {
            showExitDialog();
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    private void showExitDialog() {
        new AlertDialog.Builder(this).setTitle("退出前请确认数据已保存，否则会丢失已填数据，确认退出吗？")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 点击“确认”后的操作
                        StoreRecordActivity.this.finish();
                    }
                })
                .setNegativeButton("返回", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 点击“返回”后的操作,这里不设置没有任何操作
                    }
                }).show();
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
