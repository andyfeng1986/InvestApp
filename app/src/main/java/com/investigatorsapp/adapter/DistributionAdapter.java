package com.investigatorsapp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.investigatorsapp.R;

/**
 * Created by fenglei on 16-1-8.
 */

public class DistributionAdapter extends BaseAdapter {

    private Context context;
    private String[] texts;
    private int[] icons;
    private boolean[] selecteds;

    public DistributionAdapter(Context context, String[] texts, int[] icons, boolean[] selecteds) {
        this.context = context;
        this.icons = icons;
        this.texts = texts;
        this.selecteds = selecteds;
    }

    @Override
    public int getCount() {
        if(texts == null) {
            return 0;
        }
        return texts.length;
    }

    @Override
    public Object getItem(int position) {
        return texts[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public static class ViewHolder {
        public ImageView iv;
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
            holder.iv = (ImageView) convertView.findViewById(R.id.item_iv);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.tv.setText(texts[position]);
        holder.iv.setImageResource(icons[position]);
        holder.cb.setChecked(selecteds[position]);
        return convertView;
    }

    public boolean[] getSelecteds() {
        return selecteds;
    }

    public int[] getIcons() {
        return icons;
    }

    public String[] getTexts() {
        return texts;
    }

}
