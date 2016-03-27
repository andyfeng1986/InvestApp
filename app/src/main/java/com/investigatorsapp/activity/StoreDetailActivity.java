package com.investigatorsapp.activity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.TextView;
import android.widget.Toast;

import com.investigatorsapp.R;
import com.investigatorsapp.common.DaoSessionInstance;
import com.investigatorsapp.db.greendao.Store;
import com.investigatorsapp.db.greendao.StoreDao;

import de.greenrobot.dao.query.QueryBuilder;

/**
 * Created by fenglei on 15-12-24.
 */
public class StoreDetailActivity extends BaseActivity{

    private static final String TAG = StoreDetailActivity.class.getSimpleName();

    private class GetDataAsyncTask extends AsyncTask<String, Void, Store> {
        @Override
        protected Store doInBackground(String... params) {
            String salerno = params[0];
            StoreDao dao = DaoSessionInstance.getDaoSession(StoreDetailActivity.this).getStoreDao();
            QueryBuilder<Store> queryBuilder = dao.queryBuilder();
            Store store = queryBuilder.where(StoreDao.Properties.Salerno.eq(salerno)).build().unique();
            return store;
        }

        @Override
        protected void onPostExecute(Store store) {
            if(store != null) {
                updateUI(store);
            }
        }
    }

    private TextView accessInfoTV;
    private TextView customNameTV;
    private TextView contactTV;
    private TextView telphoneTV;
    private TextView emailTV;
    private TextView faxnumTV;
    private TextView fixPhoneTV;
    private TextView youbianTV;
    private TextView addressTV;
    private TextView customTypeTV;
    private TextView channelTypeTV;
    private TextView stationNumTV;
    private TextView repairNumTV;
    private TextView distributionNumTV;
    private TextView avgoilTV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store_detail);
        initUI();
        getData();
    }

    private void getData() {
        Intent intent = getIntent();
        final String salerno = intent.getStringExtra("salerno");
        if(TextUtils.isEmpty(salerno)) {
            Toast.makeText(StoreDetailActivity.this, "获取店面编号出错", Toast.LENGTH_LONG).show();
            finish();
        }else {
            getDataBySalerNo(salerno);
        }
    }

    private void getDataBySalerNo(String salerNo) {
        GetDataAsyncTask asyncTask = new GetDataAsyncTask();
        asyncTask.execute(salerNo);
    }

    private void initUI() {
        accessInfoTV = (TextView)findViewById(R.id.access_info_tv);
        customNameTV = (TextView)findViewById(R.id.customname_tv);
        contactTV = (TextView)findViewById(R.id.contact_tv);
        telphoneTV = (TextView)findViewById(R.id.telphone_tv);
        emailTV = (TextView)findViewById(R.id.email_tv);
        faxnumTV = (TextView)findViewById(R.id.faxnum_tv);
        fixPhoneTV = (TextView)findViewById(R.id.fixphone_tv);
        youbianTV = (TextView)findViewById(R.id.youbian_tv);
        addressTV = (TextView)findViewById(R.id.address_tv);
        customTypeTV = (TextView)findViewById(R.id.customtype_tv);
        channelTypeTV = (TextView)findViewById(R.id.channeltype_tv);
        stationNumTV = (TextView)findViewById(R.id.station_num_tv);
        repairNumTV = (TextView)findViewById(R.id.repair_num_tv);
        distributionNumTV = (TextView)findViewById(R.id.distribution_tv);
    }

    private void updateUI(Store store) {
        if(store != null) {
            accessInfoTV.setText("");
            customNameTV.setText(store.getCustname());
            contactTV.setText(store.getCustperson());
            telphoneTV.setText(store.getTelephone());
            emailTV.setText(store.getEmail());
            faxnumTV.setText(store.getFax());
            fixPhoneTV.setText(store.getFphone());
            youbianTV.setText(store.getZipcode());
            addressTV.setText(store.getAddress());
//            customTypeTV.setText(store.getChanneltype());
            channelTypeTV.setText(store.getChanneltype());
            stationNumTV.setText(store.getStationnum());
            repairNumTV.setText(store.getWorkernum());
//            distributionNumTV.setText(store.getCustname());
        }
    }

}
