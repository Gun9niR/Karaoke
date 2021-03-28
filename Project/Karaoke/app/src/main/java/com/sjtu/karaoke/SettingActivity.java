package com.sjtu.karaoke;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.sjtu.karaoke.adapter.SettingAdapter;

import static com.sjtu.karaoke.util.Utils.showToast;

/*
 * @ClassName: SettingActivity
 * @Author: guozh
 * @Date: 2021/3/28
 * @Version: v1.2
 * @Description: 设置界面。本类中保存了设置项名称，并调用SettingAdapter来设置设置列表中显示的内容及点击事件。
 */

public class SettingActivity extends AppCompatActivity {
    private String[] settingItems = {"清空本地缓存", "关于"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        // add back button
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbarSetting);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        ListView listView = findViewById(R.id.listSetting);
        SettingAdapter listAdapter = new SettingAdapter(getApplicationContext(), settingItems);
        listView.setAdapter(listAdapter);
        listView.setFooterDividersEnabled(true);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                switch(i) {
                    case 0:
                        showToast(getApplicationContext(), "clear");
                        break;
                    case 1:
                        showToast(getApplicationContext(), "about");
                        break;
                }
            }
        });
    }
}