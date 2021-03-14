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

public class SettingAdapter extends ArrayAdapter<String> {
    Context context;
    String[] settingItems;

    public SettingAdapter(Context c, String[] settingItems) {
        super(c, R.layout.setting_row, R.id.settingName, settingItems);
        this.context = c;
        this.settingItems = settingItems;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
        @SuppressLint("ViewHolder") View row = layoutInflater.inflate(R.layout.setting_row, parent, false);
        TextView settingItem = row.findViewById(R.id.settingName);

        settingItem.setText(settingItems[position]);
        return row;
    }

}