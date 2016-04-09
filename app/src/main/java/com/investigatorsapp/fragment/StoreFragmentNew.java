package com.investigatorsapp.fragment;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.GsonRequest;
import com.investigatorsapp.R;
import com.investigatorsapp.activity.DynamicStoreActivity;
import com.investigatorsapp.common.LocationReport;
import com.investigatorsapp.common.VolleySingleton;
import com.investigatorsapp.logger.Logger;
import com.investigatorsapp.model.StoreUpload;
import com.investigatorsapp.network.FileUploaderAsyncHttp;
import com.investigatorsapp.utils.Constant;
import com.investigatorsapp.utils.UrlWrapper;
import com.investigatorsapp.utils.Util;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by fenglei on 15-12-24.
 */
public class StoreFragmentNew extends Fragment implements View.OnClickListener{

    private static final String TAG = "StoreFragment";

    private Button commitAllBtn;
    private ListView listView;
    private MyAdapter myAdapter;
//    private Survey mSurvey;
    private Map<String, String> salerno2ContentMap;
    private Map<String ,String> name2SalernoMap;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Logger.d(TAG, "onCreateView");
        View view = inflater.inflate(R.layout.fragment_store, container, false);
        commitAllBtn = (Button) view.findViewById(R.id.commitAllBtn);
        listView = (ListView) view.findViewById(R.id.listview);
        commitAllBtn.setOnClickListener(this);
        View emptyView = view.findViewById(R.id.empty);
        listView.setEmptyView(emptyView);
        salerno2ContentMap = new HashMap<>();
        name2SalernoMap = new HashMap<>();
        return view;
    }

    @Override
    public void onResume() {
        Logger.d(TAG, "onResume");
        super.onResume();
        salerno2ContentMap.clear();
        name2SalernoMap.clear();
        getData();
    }

    private void getData() {
        //getSurvey();
        GetDataAsyncTask asyncTask = new GetDataAsyncTask();
        asyncTask.execute();
    }

//    private void getSurvey() {
//        GsonRequest<Survey> gsonRequest = new GsonRequest<Survey>(Request.Method.POST,
//                UrlWrapper.getQstUrl(), Survey.class,
//                UrlWrapper.getQstParam(), new Response.Listener<Survey>() {
//            @Override
//            public void onResponse(Survey response) {
//                mSurvey = response;
//                GetDataAsyncTask asyncTask = new GetDataAsyncTask();
//                asyncTask.execute();
//            }
//        }, new Response.ErrorListener() {
//            @Override
//            public void onErrorResponse(VolleyError error) {
//                Toast.makeText(getActivity(), "无法获取问卷信息", Toast.LENGTH_LONG).show();
//            }
//        });
//        VolleySingleton.getInstance().addToRequestQueue(gsonRequest);
//    }

    private class GetDataAsyncTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            initSaveData();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            initUI();
        }
    }

    private void initUI() {
        List<String> storeNameList = new ArrayList<>();
        for(Map.Entry<String, String> entry : salerno2ContentMap.entrySet()) {
            String salerno = entry.getKey();
            String value = entry.getValue();
            int start = value.indexOf("custname") + 8 + 3;
            int end = value.indexOf("\"", start);
            String custName = value.substring(start, end);
            storeNameList.add(custName);
            name2SalernoMap.put(custName, salerno);
        }
        initListView(storeNameList);
    }

    private void initSaveData() {
        File destDir = new File(Util.getDataDirPath(getActivity()));
        if(!destDir.exists()) {
            return;
        }
        File[] fileList = destDir.listFiles();
        if(fileList != null && fileList.length > 0) {
            for(File file : fileList) {
                String result = Util.readFileToString(file);
                if(TextUtils.isEmpty(result)) {
                    file.delete();
                }else {
                    salerno2ContentMap.put(file.getName(), result);
                }
            }
        }
    }


    public void initListView(final List<String> storeList) {
        if(myAdapter == null) {
            myAdapter = new MyAdapter(getActivity(), storeList);
            listView.setAdapter(myAdapter);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int pos, long l) {
                    clickItem(pos);
                }
            });
            listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                    showDeleteDialog(i);
                    return true;
                }
            });
        }else {
            myAdapter.setStoreList(storeList);
            myAdapter.notifyDataSetChanged();
        }
    }

    private void clickItem(final int pos) {
        if(myAdapter == null) {
            return;
        }
        final String name = myAdapter.getStoreList().get(pos);
        AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).setTitle(name)
                .setPositiveButton("修改", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        Intent intent = new Intent(getActivity(), DynamicStoreActivity.class);
                        intent.putExtra("salerno", name2SalernoMap.get(name));
                        startActivity(intent);
                    }
                }).setNeutralButton("提交", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        createProgress("提交", "正在提交，请稍候");
                        commitItem(pos, true);
                    }
                }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                }).create();
        alertDialog.show();
    }

    private void showDeleteDialog(final int pos) {
        if(myAdapter == null) {
            return;
        }
        String storeName = myAdapter.getStoreList().get(pos);
        AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).setTitle(storeName)
                .setPositiveButton("删除", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        deleteItem(pos);
                    }
                }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).create();
        alertDialog.show();
    }

    private void deleteItem(int pos) {
        if(myAdapter == null) {
            return;
        }
        String storeName = myAdapter.getStoreList().get(pos);
        String salerno = name2SalernoMap.get(storeName);
        deleteItemFile(salerno);
        myAdapter.getStoreList().remove(pos);
        myAdapter.notifyDataSetChanged();
    }

    private void deleteItemFile(String fileName) {
        File destDir = new File(Util.getDataDirPath(getActivity()));
        if(!destDir.exists()) {
            return;
        }
        for(File file : destDir.listFiles()) {
            if(file.getName().equals(fileName)) {
                file.delete();
                break;
            }
        }
    }

    private void deleteItemList(List<Integer> posList) {
        if(myAdapter == null) {
            return;
        }
        List<String> storeList = new ArrayList<>();
        for(int i = 0; i < posList.size(); i++) {
            int pos = posList.get(i);
            String storeName = myAdapter.getStoreList().get(pos);
            String salerno = name2SalernoMap.get(storeName);
            deleteItemFile(salerno);
            storeList.add(storeName);
        }
        for(int i = 0; i < storeList.size(); i++) {
            myAdapter.getStoreList().remove(storeList.get(i));
        }
        myAdapter.notifyDataSetChanged();
    }

    private void commitItem(final int pos, final boolean commitOne) {
        if(myAdapter == null) {
            return;
        }
        final String store = myAdapter.getStoreList().get(pos);
        String content =  salerno2ContentMap.get(salerno2ContentMap.get(store));

        String resultGsonString = new StringBuilder().append("{\"type\":\"postQst2\",")
                .append(content).append("}").toString();

        GsonRequest<StoreUpload> gsonRequest = new GsonRequest<StoreUpload>(Request.Method.POST,
                UrlWrapper.getPostStoreUrl(), StoreUpload.class,
                UrlWrapper.getPostStoreParams(resultGsonString), new Response.Listener<StoreUpload>() {
            @Override
            public void onResponse(StoreUpload response) {
                boolean commitSuccess = false;
                if(response != null) {
                    if(Constant.RET_SUCCESS_CODE.equals(response.retcode)) {
                        commitSuccess = true;
                        //uploadPhoto(pos, commitOne);
                    }else if(Constant.RET_DUP_COMMIT_CODE.equals(response.retcode)){
                        //注释，否则会导致全部提交后的后续提交出错
//                        deleteItem(pos);
                        if(commitOne) {
                            Toast.makeText(getActivity(), "此门店信息已提交, 长按可删除此门店", Toast.LENGTH_LONG).show();
                        }
                    }else {
                        if(commitOne) {
                            Toast.makeText(getActivity(), response.retmessage, Toast.LENGTH_LONG).show();
                        }
                    }
                }else {
                    if(commitOne) {
                        Toast.makeText(getActivity(), "上传失败", Toast.LENGTH_LONG).show();
//                        dissmissProgress();
                    }
//                    else {
//                        commitNext(pos, false);
//                    }
                }
                if(commitSuccess) {
                    uploadPhoto(pos, commitOne);
                }else {
                    if(commitOne) {
                        dissmissProgress();
                    }else {
                        commitNext(pos, false);
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if(commitOne) {
                    Toast.makeText(getActivity(), "上传失败", Toast.LENGTH_LONG).show();
                    dissmissProgress();
                }else {
                    commitNext(pos, false);
                }
            }
        });
        VolleySingleton.getInstance().addToRequestQueue(gsonRequest);
        LocationReport.reportLocation(getActivity());
    }

    private void uploadPhoto(final int pos, final boolean commitOne) {
        String storeName = myAdapter.getStoreList().get(pos);
        final String salerno = name2SalernoMap.get(storeName);
        String content = salerno2ContentMap.get(salerno);
        int start = content.indexOf("create_time") + 8 + 3;
        int end = content.indexOf("\"", start);
        String createTime = content.substring(start, end);

        final File file = new File(Util.getPhotoFilePath(salerno, createTime));
        if(file.exists()) {
            Util.uploadPhotoFile(salerno, createTime, new FileUploaderAsyncHttp.UpLoaderCallback() {
                @Override
                public void onSuccess(String response) {
                    if (file != null && file.exists()) {
                        file.delete();
                    }
                    uploadAudio(pos, commitOne);
                }

                @Override
                public void onFailed(int responseCode, String failReason) {
                    if (commitOne) {
                        Toast.makeText(getActivity(), "上传失败 : " + failReason, Toast.LENGTH_LONG).show();
                        dissmissProgress();
                    } else {
                        commitNext(pos, false);
                    }
                }
            });
        }else {
            uploadAudio(pos, commitOne);
        }
    }

    private void uploadAudio(final int pos, final boolean commitOne) {
        String storeName = myAdapter.getStoreList().get(pos);
        final String salerno = name2SalernoMap.get(storeName);
        String content = salerno2ContentMap.get(salerno);
        int start = content.indexOf("create_time") + 8 + 3;
        int end = content.indexOf("\"", start);
        String createTime = content.substring(start, end);

        final File file = new File(Util.getAudioFilePath(salerno, createTime));
        if(file.exists()) {
            Util.uploadAudioFile(salerno, createTime, new FileUploaderAsyncHttp.UpLoaderCallback() {
                @Override
                public void onSuccess(String response) {
                    if (file != null && file.exists()) {
                        file.delete();
                    }
                    if (commitOne) {
                        deleteItem(pos);
                        Toast.makeText(getActivity(), "上传成功", Toast.LENGTH_LONG).show();
                        dissmissProgress();
                    } else {
                        commitNext(pos, true);
                    }
                }

                @Override
                public void onFailed(int responseCode, String failReason) {
                    if (commitOne) {
                        Toast.makeText(getActivity(), "上传失败 : " + failReason, Toast.LENGTH_LONG).show();
                        dissmissProgress();
                    }else {
                        commitNext(pos, false);
                    }
                }
            });
        }else {
            if(commitOne) {
                deleteItem(pos);
                Toast.makeText(getActivity(), "上传成功", Toast.LENGTH_LONG).show();
                dissmissProgress();
            }else {
                commitNext(pos, true);
            }
        }
    }

    private void commitNext(int currentPos, boolean currentResult) {
        if(currentResult) {
            commitSuccessList.add(currentPos);
        }
        if((currentPos + 1) == myAdapter.getStoreList().size()) {
            if(commitSuccessList.size() > 0) {
                deleteItemList(commitSuccessList);
                commitSuccessList.clear();
            }
            dissmissProgress();
        }else {
            commitItem(currentPos + 1, false);
        }
    }

    class ViewHolder {
        public TextView storeNameTV;
    }

    public class MyAdapter extends BaseAdapter {

        private Context context;
        private List<String> storeNameList;

        public MyAdapter(Context context, List<String> storeNameList) {
            this.context = context;
            this.storeNameList = storeNameList;
        }

        public void setStoreList(List<String> storeNameList) {
            this.storeNameList = storeNameList;
        }

        public List<String> getStoreList() {
            return storeNameList;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            if(convertView == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.dynamic_store_list_item, parent, false);
                viewHolder = new ViewHolder();
                viewHolder.storeNameTV = (TextView)convertView.findViewById(R.id.store_name_tv);
                convertView.setTag(viewHolder);
            }else {
                viewHolder = (ViewHolder)convertView.getTag();
            }
            viewHolder.storeNameTV.setText(storeNameList.get(position));
            return convertView;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public int getCount() {
            if(storeNameList == null) {
                return 0;
            }
            return storeNameList.size();
        }

    }

    private String statusToString(String status) {
        String result = "";
        try {
            String [] accessInfos = getResources().getStringArray(R.array.access_info);
            result = accessInfos[Integer.parseInt(status)];
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public void onClick(View v) {
        if(v.equals(commitAllBtn)) {
            showDialog();
        }
    }

    private void showDialog() {
        AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).setMessage("确认提交所有数据吗?")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        commitAll();
                    }
                }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).create();
        alertDialog.show();
    }

    private List<Integer> commitSuccessList = new ArrayList<>();

    private void commitAll() {
        if(myAdapter != null && myAdapter.getStoreList().size() > 0) {
            createProgress("提交", "正在提交，请稍候");
            commitItem(0, false);
        }
    }

    protected ProgressDialog progressDialog;

    public void dissmissProgress() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
            progressDialog = null;
        }
    }

    public void createProgress(String title, String message) {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(getActivity(), AlertDialog.THEME_HOLO_LIGHT);
            progressDialog.setCancelable(false);
            progressDialog.setIndeterminate(true);
        }
        if (progressDialog != null && progressDialog.isShowing())
            return;
        if (!progressDialog.isShowing()) {
            progressDialog.setTitle(title);
            progressDialog.setMessage(message);
            progressDialog.show();
        }
    }

}
