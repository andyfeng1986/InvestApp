package com.investigatorsapp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;
import com.investigatorsapp.R;
import com.investigatorsapp.common.VolleySingleton;
import com.investigatorsapp.logger.Logger;

import java.util.List;

/**
 * Created by fenglei on 16-1-8.
 */

public class ChoiceAdapter extends BaseAdapter {

    private Context context;
    private List<String> texts;
    private List<String> icons;
    private List<Boolean> selecteds;
    private boolean isMulti;

    public ChoiceAdapter(Context context, List<String> texts, List<String> icons,
                         List<Boolean> selecteds, boolean isMutli) {
        this.context = context;
        this.icons = icons;
        this.texts = texts;
        this.selecteds = selecteds;
        this.isMulti = isMutli;
    }

    @Override
    public int getCount() {
        if(texts == null) {
            return 0;
        }
        return texts.size();
    }

    @Override
    public Object getItem(int position) {
        return texts.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public static class ViewHolder {
        public NetworkImageView iv;
        public TextView tv;
        public CheckBox cb;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.mutli_item, null);
            holder.tv = (TextView) convertView.findViewById(R.id.item_tv);
            holder.cb = (CheckBox) convertView.findViewById(R.id.item_cb);
            holder.iv = (NetworkImageView) convertView.findViewById(R.id.item_iv);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.tv.setText(texts.get(position));
        if(icons == null || icons.size() == 0) {
            holder.iv.setVisibility(View.GONE);
        }else {
            holder.iv.setImageUrl(icons.get(position), VolleySingleton.getInstance().getImageLoader());
//            VolleySingleton.getInstance().getImageLoader().get(icons.get(position),
//                    ImageLoader.getImageListener(holder.iv, 0, 0));
        }
        holder.cb.setChecked(selecteds.get(position));
        return convertView;
    }

    public List<Boolean> getSelecteds() {
        return selecteds;
    }

    public List<String> getIcons() {
        return icons;
    }

    public List<String> getTexts() {
        return texts;
    }

    public boolean isMulti() {
        return isMulti;
    }


}
