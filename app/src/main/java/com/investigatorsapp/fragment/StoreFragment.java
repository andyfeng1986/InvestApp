package com.investigatorsapp.fragment;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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
import com.google.gson.Gson;
import com.investigatorsapp.R;
import com.investigatorsapp.activity.StoreRecordActivity;
import com.investigatorsapp.common.DaoSessionInstance;
import com.investigatorsapp.common.LocationReport;
import com.investigatorsapp.common.VolleySingleton;
import com.investigatorsapp.db.greendao.Store;
import com.investigatorsapp.db.greendao.StoreDao;
import com.investigatorsapp.logger.Logger;
import com.investigatorsapp.model.StoreUpload;
import com.investigatorsapp.network.FileUploaderAsyncHttp;
import com.investigatorsapp.utils.Constant;
import com.investigatorsapp.utils.UrlWrapper;
import com.investigatorsapp.utils.Util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by fenglei on 15-12-24.
 */
public class StoreFragment extends Fragment implements View.OnClickListener{

    private static final String TAG = "StoreFragment";

    private Button commitAllBtn;
    private ListView listView;
    private MyAdapter myAdapter;

    private List<Store> initStoreDB() {
        List<Store> storeList = new ArrayList<>();
        StoreDao dao = DaoSessionInstance.getDaoSession(getActivity()).getStoreDao();
        for(int i = 0; i < 20; i++) {
            Store store = new Store();
            store.setSalerno(String.valueOf("000" + i));
            store.setCustname("商店" + i);
            store.setStatus(String.valueOf(i % 3));
            storeList.add(store);
            dao.insertOrReplace(store);
        }
        return storeList;
    }

    private class GetDataAsyncTask extends AsyncTask<Void, Void, List<Store>> {

        @Override
        protected List<Store> doInBackground(Void... params) {
            StoreDao dao = DaoSessionInstance.getDaoSession(getActivity()).getStoreDao();
            return dao.queryBuilder().list();
        }

        @Override
        protected void onPostExecute(List<Store> storeList) {
            initListView(storeList);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Logger.d(TAG, "onCreateView");
        View view = inflater.inflate(R.layout.fragment_store, container, false);
        commitAllBtn = (Button) view.findViewById(R.id.commitAllBtn);
        listView = (ListView) view.findViewById(R.id.listview);
        commitAllBtn.setOnClickListener(this);
        View emptyView = view.findViewById(R.id.empty);
        listView.setEmptyView(emptyView);
        return view;
    }

    @Override
    public void onResume() {
        Logger.d(TAG, "onResume");
        super.onResume();
        //initStoreDB();
        getData();
    }

    private void getData() {
        GetDataAsyncTask asyncTask = new GetDataAsyncTask();
        asyncTask.execute();
    }

    public void initListView(final List<Store> storeList) {
//        if(storeList == null || storeList.size() == 0) {
//            return;
//        }
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
        final Store store = myAdapter.getStoreList().get(pos);
        AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).setTitle(store.getCustname())
                .setPositiveButton("修改", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        Intent intent = new Intent(getActivity(), StoreRecordActivity.class);
                        intent.putExtra("salerno", store.getSalerno());
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
        Store store = myAdapter.getStoreList().get(pos);
        AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).setTitle(store.getCustname())
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
        Store store = myAdapter.getStoreList().get(pos);
        StoreDao dao = DaoSessionInstance.getDaoSession(getActivity()).getStoreDao();
        dao.delete(store);
        myAdapter.getStoreList().remove(pos);
        myAdapter.notifyDataSetChanged();
    }

    private void deleteItemList(List<Integer> posList) {
        if(myAdapter == null) {
            return;
        }
        List<Store> storeList = new ArrayList<>();
        for(int i = 0; i < posList.size(); i++) {
            int pos = posList.get(i);
            Store store = myAdapter.getStoreList().get(pos);
            StoreDao dao = DaoSessionInstance.getDaoSession(getActivity()).getStoreDao();
            dao.delete(store);
//            myAdapter.getStoreList().remove(store);
            storeList.add(store);
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
        final Store store = myAdapter.getStoreList().get(pos);
        Gson gson = new Gson();
        String gsonString = gson.toJson(store);
        String resultGsonString = new StringBuilder().append("{\"type\":\"postQst\",")
                .append(gsonString.substring(1)).toString();
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
                        commitSuccess = true;
//                        if(commitOne) {
//                            Toast.makeText(getActivity(), "此门店信息已提交, 长按可删除此门店", Toast.LENGTH_LONG).show();
//                        }
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
        Store store = myAdapter.getStoreList().get(pos);
        final String salerno = store.getSalerno();
        final File file = new File(Util.getPhotoFilePath(salerno, store.getTime()));
        if(file.exists()) {
            Util.uploadPhotoFile(salerno, store.getTime(), new FileUploaderAsyncHttp.UpLoaderCallback() {
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
        Store store = myAdapter.getStoreList().get(pos);
        final String salerno = store.getSalerno();
        final File file = new File(Util.getAudioFilePath(salerno, store.getTime()));
        if(file.exists()) {
            Util.uploadAudioFile(salerno, store.getTime(), new FileUploaderAsyncHttp.UpLoaderCallback() {
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
        public TextView accessInfoTV;
    }

    public class MyAdapter extends BaseAdapter {

        private Context context;
        private List<Store> storeList;

        public MyAdapter(Context context, List<Store> storeList) {
            this.context = context;
            this.storeList = storeList;
        }

        public void setStoreList(List<Store> storeList) {
            this.storeList = storeList;
        }

        public List<Store> getStoreList() {
            return storeList;
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
            viewHolder.accessInfoTV.setText(statusToString(storeList.get(position).getStatus()));
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
            if(storeList == null) {
                return 0;
            }
            return storeList.size();
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
