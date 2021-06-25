package com.sjtu.karaoke.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.sjtu.karaoke.LocalRecordActivity;
import com.sjtu.karaoke.R;
import com.sjtu.karaoke.SettingActivity;

/*
 * @ClassName: AccountFragment
 * @Author: guozh
 * @Date: 2021/3/28
 * @Version: v1.3
 * @Description: 主界面(MainActivity)中“我的”界面。
 * "我的"页用于查看本地录音，以及跳转到设置页
 */

public class AccountFragment extends Fragment {
    public AccountFragment() { }

    public static AccountFragment newInstance(String param1, String param2) {
        AccountFragment fragment = new AccountFragment();
        Bundle args = new Bundle();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_account, container, false);
        Toolbar toolbar = view.findViewById(R.id.toolbarAccount);

        // 用户点击设置图标时跳转到设置页
        toolbar.setOnMenuItemClickListener(item -> {
            Intent intent = new Intent(getActivity(), SettingActivity.class);
            startActivity(intent);
            return true;
        });

        // 点击"本地录音"按钮时跳转到录音列表
        Button btnLocalRecord = view.findViewById(R.id.btnLocalRecord);
        btnLocalRecord.setOnClickListener(view1 -> {
            Intent intent = new Intent(getContext(), LocalRecordActivity.class);
            startActivity(intent);
        });
        return view;
    }
}