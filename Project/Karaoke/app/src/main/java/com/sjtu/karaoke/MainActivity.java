package com.sjtu.karaoke;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.ismaeldivita.chipnavigation.ChipNavigationBar;
import com.sjtu.karaoke.fragment.AccountFragment;
import com.sjtu.karaoke.fragment.ViewSongsFragment;

import static com.sjtu.karaoke.util.PathUtil.makeDirectories;
import static com.sjtu.karaoke.util.MiscUtil.verifyAllPermissions;

/*
 * @ClassName: MainActivity
 * @Author: guozh
 * @Date: 2021/3/28
 * @Version: v1.2
 * @Description: 应用主界面。本类中仅初始化了导航栏，导航栏所指向的界面在其对应的fragment中实现。
 */

public class MainActivity extends AppCompatActivity {
    private Fragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        verifyAllPermissions(this);
        makeDirectories();

        ChipNavigationBar chipNavigationBar = findViewById(R.id.chipNavigation);

        chipNavigationBar.setOnItemSelectedListener(new ChipNavigationBar.OnItemSelectedListener() {
            @Override
            public void onItemSelected(int i) {
                switch (i) {
                    case R.id.viewSongs:
                        fragment = new ViewSongsFragment();
                        break;
                    case R.id.account:
                        fragment = new AccountFragment();
                        break;
                }
                getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer, fragment).commit();
            }
        });

        chipNavigationBar.setItemSelected(R.id.viewSongs, true);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        return false;
    }
}