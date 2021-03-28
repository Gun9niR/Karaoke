package com.sjtu.karaoke.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
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
 * @Version: v1.2
 * @Description: 主界面(MainActivity)中“我的”界面。本类用于初始化界面中的各个组件，设置其点击事件。
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

        // set up toolbar
        Toolbar toolbar = view.findViewById(R.id.toolbarAccount);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                // go to setting activity
                Intent intent = new Intent(getActivity(), SettingActivity.class);
                startActivity(intent);
                return true;
            }
        });

        Button btnLocalRecord = view.findViewById(R.id.btnLocalRecord);
        btnLocalRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), LocalRecordActivity.class);
                startActivity(intent);
            }
        });
        return view;
    }
}