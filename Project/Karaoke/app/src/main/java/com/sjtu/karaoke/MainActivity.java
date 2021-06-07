package com.sjtu.karaoke;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.ismaeldivita.chipnavigation.ChipNavigationBar;
import com.sjtu.karaoke.fragment.AccountFragment;
import com.sjtu.karaoke.fragment.ViewSongsFragment;

import static com.sjtu.karaoke.util.MiscUtil.verifyAllPermissions;
import static com.sjtu.karaoke.util.PathUtil.makeDirectories;

/*
 * @ClassName: MainActivity
 * @Author: guozh
 * @Date: 2021/3/28
 * @Version: v1.3
 * @Description: 应用主界面。本类中仅初始化了导航栏，导航栏所指向的界面在其对应的fragment中实现。
 */

public class MainActivity extends AppCompatActivity {
    private Fragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 向用户请求录音、读写权限
        verifyAllPermissions(this);
        // 创建APP的所有目录
        makeDirectories();

        ChipNavigationBar chipNavigationBar = findViewById(R.id.chipNavigation);

        chipNavigationBar.setOnItemSelectedListener(i -> {
            switch (i) {
                case R.id.viewSongs:
                    fragment = new ViewSongsFragment();
                    break;
                case R.id.account:
                    fragment = new AccountFragment();
                    break;
            }
            // 用真正的fragment替换占位的FrameLayout
            getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer, fragment).commit();
        });

        // 默认进入浏览歌曲界面
        chipNavigationBar.setItemSelected(R.id.viewSongs, true);
    }
}