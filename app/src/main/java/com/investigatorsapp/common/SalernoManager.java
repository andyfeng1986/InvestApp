package com.investigatorsapp.common;

import android.content.Context;

import com.investigatorsapp.db.greendao.Store;
import com.investigatorsapp.db.greendao.StoreDao;
import com.investigatorsapp.model.BlocksResult;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by fenglei on 16-1-4.
 */
public class SalernoManager {

    private static final SalernoManager instance = new SalernoManager();

    private Map<String, Integer> salernoHashMap;

    private SalernoManager() {}

    public static SalernoManager getInstance() {
        return instance;
    }

    public Map<String, Integer> getSalernoHashMap() {
        return salernoHashMap;
    }

    public synchronized void initSalernoManager(Context context, BlocksResult blocksResult) {
        if(salernoHashMap == null) {
            salernoHashMap = new ConcurrentHashMap<String, Integer>();
        }else {
            salernoHashMap.clear();
        }
        if(blocksResult != null && blocksResult.polygons != null) {
            for(int i = 0; i < blocksResult.polygons.size(); i++) {
                BlocksResult.Block block = blocksResult.polygons.get(i);
                if(block != null) {
                    int count = 0;
                    try {
                        count = Integer.parseInt(block.polycount);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    salernoHashMap.put(block.polygonno, count);
                }
            }
            StoreDao dao = DaoSessionInstance.getDaoSession(context).getStoreDao();
            List<Store> storeList = dao.loadAll();
            if(storeList != null) {
                for(int i = 0; i < storeList.size(); i++) {
                    Store store = storeList.get(i);
                    try {
                        String salerno = store.getSalerno();
                        int salernum = Integer.parseInt(salerno.substring(salerno.length() - 5));
                        Integer count = salernoHashMap.get(store.getPolygonid());
                        if(count != null) {
                            if(salernum >= count) {
                                count = salernum + 1;
                                salernoHashMap.put(store.getPolygonid(), count);
                            }
                        }
                    } catch (Exception e ) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public synchronized void updatePolycountHashMap(String polyno, String salerno) {
        try {
            int salernum = Integer.parseInt(salerno.substring(salerno.length() - 5));
            Integer polycount = salernoHashMap.get(polyno);
            if(polycount != null && salernum >= polycount) {
                salernoHashMap.put(polyno, polycount + 1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
