package com.investigatorsapp.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.GsonRequest;
import com.investigatorsapp.R;
import com.investigatorsapp.adapter.ChoiceAdapter;
import com.investigatorsapp.common.LocationReport;
import com.investigatorsapp.common.SalernoManager;
import com.investigatorsapp.common.UserSingleton;
import com.investigatorsapp.common.VolleySingleton;
import com.investigatorsapp.logger.Logger;
import com.investigatorsapp.model.Gps;
import com.investigatorsapp.model.StoreUpload;
import com.investigatorsapp.model.Survey;
import com.investigatorsapp.network.FileUploaderAsyncHttp;
import com.investigatorsapp.utils.Constant;
import com.investigatorsapp.utils.UrlWrapper;
import com.investigatorsapp.utils.Util;
import com.investigatorsapp.widget.AddressLayout;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by fenglei on 16/3/28.
 */

public class DynamicStoreActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = DynamicStoreActivity.class.getSimpleName();

    private String mSalerNo;
    private String mPolygonid;
    private String mLatb;
    private String mLngb;
    private String mPolyname;

    private Survey mSurvey;
    private String mEnterTime;

    private TextView hintTV;
    private ViewGroup allVG;

    private Button saveBtn;
    private Button commitBtn;

    private LinearLayout mContentLL;

    private ViewGroup customNameLL;
    private ViewGroup telphoneLL;
    private ViewGroup addressLL;
    private EditText customNameET;
    private EditText telephoneET;
    private EditText addressET;
    private AddressLayout addressLayout;

    private Map<Survey.Question, View> viewMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Logger.d(TAG, "onCreate, savedInstanceState = " + savedInstanceState);
        setContentView(R.layout.activity_dynamic_store_record);

        Intent intent = getIntent();
        mSalerNo = intent.getStringExtra("salerno");
        mPolygonid = intent.getStringExtra("polygonid");
        mLatb = intent.getStringExtra("lat");
        mLngb = intent.getStringExtra("lng");
        mPolyname = intent.getStringExtra("polyname");
        Logger.d(TAG, "mSalerNo = " + mSalerNo + ", polygonid = " + mPolygonid
                + ", mPolyname = " + mPolyname);

        mEnterTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

        hintTV = (TextView) findViewById(R.id.hint_tv);
        allVG = (ViewGroup) findViewById(R.id.allLL);
        hintTV.setVisibility(View.VISIBLE);
        allVG.setVisibility(View.GONE);

        mContentLL = (LinearLayout)findViewById(R.id.content_ll);

        initFixUI();
        getData();
    }

    @Override
    protected void onDestroy() {
        stopAudio();
        super.onDestroy();
    }

    private void getData() {
        getSurvey();
    }

    private void getSurvey() {
        GsonRequest<Survey> gsonRequest = new GsonRequest<Survey>(Request.Method.POST,
                UrlWrapper.getQstUrl(), Survey.class,
                UrlWrapper.getQstParam(), new Response.Listener<Survey>() {
            @Override
            public void onResponse(Survey response) {
                mSurvey = response;
                initDynamicUI(response);
                hintTV.setVisibility(View.GONE);
                allVG.setVisibility(View.VISIBLE);
            }
        }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(DynamicStoreActivity.this, "无法获取店面编号", Toast.LENGTH_LONG).show();
                    finish();
                }
            });
        VolleySingleton.getInstance().addToRequestQueue(gsonRequest);
    }

    private void initFixUI() {
        commitBtn = (Button) findViewById(R.id.commitBtn);
        saveBtn = (Button) findViewById(R.id.saveBtn);
        customNameLL = (ViewGroup)findViewById(R.id.include_customname);
        customNameET = (EditText) customNameLL.findViewById(R.id.et);
        customNameET.setFilters(new InputFilter[]{new InputFilter.LengthFilter(128)});
        ((TextView)(customNameLL.findViewById(R.id.tv))).setText("客户名称*");

        telphoneLL = (ViewGroup) findViewById(R.id.include_telphone);
        telephoneET = (EditText) telphoneLL.findViewById(R.id.et);
        telephoneET.setInputType(InputType.TYPE_CLASS_PHONE);
        telephoneET.setFilters(new InputFilter[]{new InputFilter.LengthFilter(40)});
        ((TextView)(telphoneLL.findViewById(R.id.tv))).setText("手机");

        addressLL = (ViewGroup)findViewById(R.id.include_address);
        addressET = (EditText) addressLL.findViewById(R.id.et);
        addressET.setFilters(new InputFilter[]{new InputFilter.LengthFilter(128)});
        ((TextView)(addressLL.findViewById(R.id.tv))).setText("详细地址*");

        addressLayout = (AddressLayout) findViewById(R.id.address_layout);
    }

    private void initDynamicUI(Survey survey) {
        if(survey != null) {
            startAudio(survey);
            if(survey.questions != null) {
                for(int i = 0; i < survey.questions.size(); i++) {
                    Survey.Question question = survey.questions.get(i);
                    if(question != null) {
                        if("text".equals(question.type)) {
                            setTextQuestionUI(question);
                        } else if("number".equals(question.type)) {
                            setTextQuestionUI(question);
                        } else if("single".equals(question.type)) {
                            setChoiceQuestionUI(question);
                        } else if("multi".equals(question.type)) {
                            setChoiceQuestionUI(question);
                        }
                    }
                }
            }
        }
    }

    private void setChoiceQuestionUI(final Survey.Question question) {
        if(question != null && question.type != null) {
            View view = LayoutInflater.from(this).inflate(R.layout.multi_store_record_item, mContentLL, false);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    (int) getResources().getDimension(R.dimen.y100));
            layoutParams.topMargin = (int) getResources().getDimension(R.dimen.y10);
            view.setLayoutParams(layoutParams);
            mContentLL.addView(view);
            TextView tv = (TextView) view.findViewById(R.id.tv);
            final Button btn = (Button) view.findViewById(R.id.btn);
            if(question.must == 1) {
                tv.setText(question.text + "*");
            } else {
                tv.setText(question.text);
            }
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setChoiceDialogView(question, btn);
                }
            });
            viewMap.put(question, view);
        }
    }

    private void setChoiceDialogView(Survey.Question question, final Button btn){
        List<String> textList = new ArrayList<>();
        List<String> iconList = new ArrayList<>();
        List<Boolean> selecteds = new ArrayList<>();
        if(question != null && question.option != null) {
            for(int i = 0; i < question.option.size(); i++) {
                Survey.Option option = question.option.get(i);
                if(option != null) {
                    textList.add(option.text);
                    if(question.hasphoto == 1) {
                        iconList.add(option.photolink);
                    }
                    selecteds.add(false);
                }
            }
        }
        if(textList.size() == 0) {
            return;
        }
        boolean isMulti = false;
        if("multi".equals(question.type)) {
            isMulti = true;
        }
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.distribution_select_dialog, null);
        ListView lv = (ListView) layout.findViewById(R.id.mylistview);
        final ChoiceAdapter adapter = new ChoiceAdapter(this,
                textList, iconList, selecteds, isMulti);
        lv.setAdapter(adapter);
        AlertDialog alertDialog = new AlertDialog.Builder(this).setView(layout).create();
        alertDialog.show();
        alertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                StringBuilder stringBuilder = new StringBuilder();
                for (int i = 0; i < adapter.getCount(); i++) {
                    if (adapter.getSelecteds().get(i)) {
                        stringBuilder.append(adapter.getTexts().get(i)).append(",");
                    }
                }
                String text = "请选择";
                if (stringBuilder.length() > 0) {
                    text = stringBuilder.subSequence(0, stringBuilder.length() - 1).toString();
                }
                btn.setText(text);
            }
        });
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                    long arg3) {
                ChoiceAdapter.ViewHolder holder =
                        (ChoiceAdapter.ViewHolder) arg1.getTag();
                holder.cb.toggle();
                List<Boolean> selecteds = adapter.getSelecteds();
                selecteds.set(arg2, holder.cb.isChecked());
                if(!adapter.isMulti()) {
                    if(selecteds.get(arg2)) {
                        for(int i = 0; i < adapter.getCount(); i++) {
                            if(i != arg2) {
                                adapter.getSelecteds().set(i, false);
                            }
                        }
                        adapter.notifyDataSetChanged();
                    }
                }
            }
        });
    }

    private void setTextQuestionUI(Survey.Question question) {
        if(question != null && question.type != null) {
            View view = LayoutInflater.from(this).inflate(R.layout.text_store_record_item, mContentLL, false);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    (int) getResources().getDimension(R.dimen.y100));
            layoutParams.topMargin = (int) getResources().getDimension(R.dimen.y10);
            view.setLayoutParams(layoutParams);
            mContentLL.addView(view);
            TextView tv = (TextView) view.findViewById(R.id.tv);
            EditText et = (EditText) view.findViewById(R.id.et);
            if(question.must == 1) {
                tv.setText(question.text + "*");
            } else {
                tv.setText(question.text);
            }
            if("number".equals(question.type)) {
                et.setInputType(InputType.TYPE_CLASS_NUMBER);
            }
            if(question.option != null && question.option.size() > 0) {
                Survey.Option option = question.option.get(0);
                if(option != null) {
                    if(!TextUtils.isEmpty(option.maxlen)) {
                        try {
                            int maxLen = Integer.parseInt(option.maxlen);
                            et.setFilters(new InputFilter[]{new InputFilter.LengthFilter(maxLen)});
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    if(!TextUtils.isEmpty(option.text)) {
                        et.setHint(option.text);
                    }
                }
            }
            viewMap.put(question, view);
        }
    }

    private void startAudio(Survey survey) {
        if(survey != null && "1".equals(survey.audio)) {
            startMediaRecorder();
        }
    }

    private void stopAudio() {
        if(mSurvey != null && "1".equals(mSurvey.audio)) {
            stopMediaRecorder();
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

    @Override
    public void onClick(View v) {
        if(v.equals(saveBtn)) {
            save();
        } else if(v.equals(commitBtn)) {
            commit();
            LocationReport.reportLocation(this);
        }
    }

    private void save() {
        if(checkInput()) {
            stopAudio();
            File destDir = new File(getFilesDir(), UserSingleton.getInstance().getUser().userid);
            if (!destDir.exists()) {
                destDir.mkdirs();
            }
            File file = new File(destDir, mSalerNo);
            try {
                FileOutputStream outputStream = new FileOutputStream(file);
                outputStream.write(genCommitString().getBytes());
                outputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            SalernoManager.getInstance().updatePolycountHashMap(mPolygonid);
            Toast.makeText(this, "问卷已保存, 请后续在店面页面提交", Toast.LENGTH_LONG).show();
        }
    }

    private void commit() {
        if(checkInput()) {
            stopAudio();
            String string = genCommitString();
            String resultGsonString = new StringBuilder().append("{\"type\":\"postQst2\",")
                    .append(string).append("}").toString();
            GsonRequest<StoreUpload> gsonRequest = new GsonRequest<StoreUpload>(Request.Method.POST,
                    UrlWrapper.getPostStoreUrl(), StoreUpload.class,
                    UrlWrapper.getPostStoreParams(resultGsonString), new Response.Listener<StoreUpload>() {
                @Override
                public void onResponse(StoreUpload response) {
                    if(response != null) {
                        if(Constant.RET_SUCCESS_CODE.equals(response.retcode)) {
                            uploadPhoto();
                        }else if(Constant.RET_DUP_COMMIT_CODE.equals(response.retcode)){
                            Toast.makeText(DynamicStoreActivity.this, "此门店信息已提交", Toast.LENGTH_LONG).show();
                            finish();
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
        SalernoManager.getInstance().updatePolycountHashMap(mPolygonid);
        Toast.makeText(this, "上传成功", Toast.LENGTH_LONG).show();
        dissmissProgress();
        finish();
    }

    private void commitFail(String message) {
        if(TextUtils.isEmpty(message)) {
            Toast.makeText(this, "上传失败", Toast.LENGTH_LONG).show();
        }else {
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        }
        dissmissProgress();
    }

    private String genCommitString() {
        StringBuilder sb = new StringBuilder();
        sb.append(appendString("userid", UserSingleton.getInstance().getUser().userid));
        sb.append(",").append(appendString("jobid", UserSingleton.getInstance().getUser().jobid));
        sb.append(",").append(appendString("token", UserSingleton.getInstance().getUser().token));
        sb.append(",").append(appendString("polygonid", mPolygonid));
        sb.append(",").append(appendString("salerno", mSalerNo));
        sb.append(",").append(appendString("create_time", mEnterTime));
        sb.append(",").append(appendString("custname", customNameET.getText().toString()));
        sb.append(",").append(appendString("phone", telephoneET.getText().toString()));
        sb.append(",").append(appendString("address", addressET.getText().toString()));
        sb.append(",").append(appendString("province", addressLayout.getPronvice()));
        sb.append(",").append(appendString("city", addressLayout.getCity()));
        sb.append(",").append(appendString("area", addressLayout.getArea()));
        sb.append(",").append(appendString("lat_b", mLatb));
        sb.append(",").append(appendString("lng_b", mLngb));
        try {
            Gps gps = Util.bd09_To_Gps84(Double.parseDouble(mLatb), Double.parseDouble(mLngb));
            if(gps != null) {
                sb.append(",").append(appendString("lat_g", String.valueOf(gps.getWgLat())));
                sb.append(",").append(appendString("lng_g", String.valueOf(gps.getWgLon())));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        for(Map.Entry<Survey.Question, View> entry : viewMap.entrySet()) {
            if (entry != null) {
                Survey.Question question = entry.getKey();
                View view = entry.getValue();
                if("text".equals(question.type) || "number".equals(question.type)) {
                    EditText et = (EditText) view.findViewById(R.id.et);
                    sb.append(",").append(appendString(question.name, et.getText().toString()));
                } else if("single".equals(question.type)) {
                    Button btn = (Button) view.findViewById(R.id.btn);
                    sb.append(",").append(appendString(question.name, btn.getText().toString()));
                } else if("multi".equals(question.type)) {
                    Button btn = (Button) view.findViewById(R.id.btn);
                    if(TextUtils.isEmpty(btn.getText().toString())) {
                        continue;
                    }
                    String[] strings = btn.getText().toString().split(",");
                    if(question.option != null) {
                        for(int i = 0; i < question.option.size(); i++) {
                            Survey.Option option = question.option.get(i);
                            boolean found = false;
                            if(option != null) {
                                for(int j = 0; j < strings.length; j++) {
                                    if(option.text.equals(strings[j])) {
                                        found = true;
                                        break;
                                    }
                                }
                            }
                            if(found) {
                                sb.append(",").append(appendString(option.name, "1"));
                            }else {
                                sb.append(",").append(appendString(option.name, "0"));
                            }
                        }
                    }
                }
            }
        }
        return sb.toString();
    }

    private String appendString(String key, String value) {
        return "\"" + key + "\":" + "\"" + value + "\"";
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
        if(TextUtils.isEmpty(addressET.getText().toString())) {
            Toast.makeText(this, "地址名称不能为空", Toast.LENGTH_SHORT).show();
            return false;
        }
        for(Map.Entry<Survey.Question, View> entry : viewMap.entrySet()) {
            if(entry != null) {
                Survey.Question question = entry.getKey();
                View view = entry.getValue();
                if(question.must == 1) {
                    if("text".equals(question.type) || "number".equals(question.type)) {
                        EditText et = (EditText) view.findViewById(R.id.et);
                        if(TextUtils.isEmpty(et.getText().toString())) {
                            Toast.makeText(this, question.text + "不能为空", Toast.LENGTH_LONG).show();
                            return false;
                        }
                    } else if("single".equals(question.type) || "multi".equals(question.type)) {
                        Button btn = (Button) view.findViewById(R.id.btn);
                        if(TextUtils.isEmpty(btn.getText())) {
                            Toast.makeText(this, question.text + "不能为空", Toast.LENGTH_LONG).show();
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }


}

