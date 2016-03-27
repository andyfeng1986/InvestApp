package com.investigatorsapp.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
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
import com.investigatorsapp.model.StoreUpload;
import com.investigatorsapp.network.FileUploaderAsyncHttp;
import com.investigatorsapp.utils.UrlWrapper;
import com.investigatorsapp.utils.Util;

import java.util.List;

/**
 * Created by fenglei on 15-12-24.
 */
public class StoreListActivity extends BaseActivity implements View.OnClickListener{

    private class GetDataAsyncTask extends AsyncTask<Void, Void, List<Store>> {

        @Override
        protected List<Store> doInBackground(Void... params) {
            StoreDao dao = DaoSessionInstance.getDaoSession(StoreListActivity.this).getStoreDao();
//            storeList = dao.queryBuilder().where(StoreDao.Properties.Uploaded.eq(isUnCommitList)).list();
            return storeList;
        }

        @Override
        protected void onPostExecute(List<Store> storeList) {
            initListView(storeList);
        }
    }

    private boolean isUnCommitList;
    private Button commitAllBtn;
    private ListView listView;
    private List<Store> storeList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isUnCommitList = getIntent().getBooleanExtra("uncommit", false);
        setContentView(R.layout.activity_store_list);
        commitAllBtn = (Button) findViewById(R.id.commitAllBtn);
        if(isUnCommitList) {
            commitAllBtn.setOnClickListener(this);
            commitAllBtn.setVisibility(View.VISIBLE);
        }else {
            commitAllBtn.setVisibility(View.GONE);
        }
        listView = (ListView)findViewById(R.id.listview);
        getData();
    }

    private void getData() {
//        if(TextUtils.isEmpty(salerno)) {
//            Toast.makeText(this, "没有此店面信息", Toast.LENGTH_LONG).show();
//        }else {
            GetDataAsyncTask asyncTask = new GetDataAsyncTask();
            asyncTask.execute();
//        }
    }

    public void initListView(List<Store> storeList) {
        if(storeList == null || storeList.size() == 0) {
            return;
        }
        MyAdapter myAdapter = new MyAdapter(this, storeList);
        listView.setAdapter(myAdapter);
    }

    @Override
    public void onClick(View v) {
        if(v.equals(commitAllBtn)) {
            showDialog();
        }
    }

    private void showDialog() {
        AlertDialog alertDialog = new AlertDialog.Builder(this).setMessage("确认提交所有数据吗?")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        updateAll();
                    }
                }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).create();
        alertDialog.show();
    }

    private void updateAll() {
        if(storeList != null && storeList.size() > 0) {
            for(int i = 0; i < storeList.size(); i++) {
                update(storeList.get(i));
            }
        }
    }

    private void update(final Store store) {
        Gson gson = new Gson();
        String gsonString = gson.toJson(store);
        GsonRequest<StoreUpload> gsonRequest = new GsonRequest<StoreUpload>(Request.Method.POST,
                UrlWrapper.getPostStoreUrl(), StoreUpload.class,
                UrlWrapper.getPostStoreParams(gsonString), new Response.Listener<StoreUpload>() {
            @Override
            public void onResponse(StoreUpload response) {
                if(response != null && "0000".equals(response.retcode)) {
                    uploadPhoto(store);
                }else {
                    //Toast.makeText(StoreListActivity.this, "上传失败", Toast.LENGTH_LONG).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //Toast.makeText(StoreListActivity.this, "上传失败", Toast.LENGTH_LONG).show();
            }
        });
        VolleySingleton.getInstance().addToRequestQueue(gsonRequest);
    }

    public void updateDbAfterCommitSuccess(Store store) {
        StoreDao dao = DaoSessionInstance.getDaoSession(StoreListActivity.this).getStoreDao();
//        store.setUploaded(true);
        dao.insertOrReplace(store);
    }

    private void uploadPhoto(final Store store) {
        Util.uploadPhotoFile(store.getSalerno(), store.getTime(), new FileUploaderAsyncHttp.UpLoaderCallback() {
            @Override
            public void onSuccess(String response) {
                uploadAudio(store);
            }

            @Override
            public void onFailed(int responseCode, String failReason) {

            }
        });
    }

    private void uploadAudio(final Store store) {
        Util.uploadAudioFile(store.getSalerno(), store.getTime(), new FileUploaderAsyncHttp.UpLoaderCallback() {
            @Override
            public void onSuccess(String response) {
                updateDbAfterCommitSuccess(store);
            }

            @Override
            public void onFailed(int responseCode, String failReason) {

            }
        });
    }

    class ViewHolder {
        public TextView storeNameTV;
        public TextView accessInfoTV;
    }

    public class MyAdapter extends BaseAdapter {

        private Context context;
        private List<Store> storeList;

        public MyAdapter(Context context, List<Store> storeList) {
            this.context = context;
            this.storeList = storeList;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            if(convertView == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.store_list_item, parent, false);
                viewHolder = new ViewHolder();
                viewHolder.storeNameTV = (TextView)convertView.findViewById(R.id.store_name_tv);
                viewHolder.accessInfoTV = (TextView)convertView.findViewById(R.id.access_info_tv);
                convertView.setTag(viewHolder);
            }else {
                viewHolder = (ViewHolder)convertView.getTag();
            }
            viewHolder.storeNameTV.setText(storeList.get(position).getCustname());
            viewHolder.accessInfoTV.setText(storeList.get(position).getCustperson());
            convertView.setClickable(true);
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(StoreListActivity.this, "click pos = " + position + ", isUncooimt = " + isUnCommitList
                        , Toast.LENGTH_LONG).show();
                    if(isUnCommitList) {
                        Intent intent = new Intent(StoreListActivity.this, StoreRecordActivity.class);
                        intent.putExtra("salerno", storeList.get(position).getSalerno());
                        startActivity(intent);
                    }else {
                        Intent intent = new Intent(StoreListActivity.this, StoreDetailActivity.class);
                        intent.putExtra("salerno", storeList.get(position).getSalerno());
                        startActivity(intent);
                    }
                }
            });
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
            return storeList.size();
        }

    }


}
