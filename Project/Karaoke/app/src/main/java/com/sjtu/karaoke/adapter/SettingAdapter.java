package com.sjtu.karaoke.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.sjtu.karaoke.R;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

/*
 * @ClassName: SettingAdapter
 * @Author: 郭志东
 * @Date: 2021/3/28
 * @Version: v1.2
 * @Description: 设置界面的选项列表生成类。根据构造时传入的设置项名称数组设置界面中显示的设置项。
 */

public class SettingAdapter extends ArrayAdapter<String> {
    Context context;
    String[] settingItems;

    public SettingAdapter(Context c, String[] settingItems) {
        super(c, R.layout.row_setting, R.id.settingName, settingItems);
        this.context = c;
        this.settingItems = settingItems;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
        @SuppressLint("ViewHolder") View row = layoutInflater.inflate(R.layout.row_setting, parent, false);

        TextView settingItem = row.findViewById(R.id.settingName);

        settingItem.setText(settingItems[position]);
        return row;
    }

}