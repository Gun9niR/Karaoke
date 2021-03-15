package com.sjtu.karaoke;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.os.Bundle;
import android.view.MenuItem;

import com.ismaeldivita.chipnavigation.ChipNavigationBar;
import com.sjtu.karaoke.fragment.AccountFragment;
import com.sjtu.karaoke.fragment.ViewSongsFragment;

public class MainActivity extends AppCompatActivity {
    private Fragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ChipNavigationBar chipNavigationBar = findViewById(R.id.chipNavigation);
        // default fragment: plaza fragment
        // set item selection listener
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