package com.sjtu.karaoke;

import android.app.SearchManager;
import android.content.Context;
import android.os.Bundle;
import android.view.Menu;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sjtu.karaoke.adapter.SongListAdapter;
import com.sjtu.karaoke.data.SongInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/*
 * @ClassName: SearchActivity
 * @Author: guozh
 * @Date: 2021/3/28
 * @Version: v1.2
 * @Description: 歌曲搜素界面。本类中包含了如下功能：
 *                  1. 各个组件的初始化、调用Adapter类来初始化本地录音列表
 *                  2. 播放用户选择的本地伴奏
 *                  3. 将本地录音分享至微信
 */

public class SearchActivity extends AppCompatActivity {
    private RecyclerView songRecyclerView;
    private SongListAdapter adapter;
    private List<SongInfo> songList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbarSearch);
        // set up back button
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        // set up list
        initRecyclerView();
    }

    private void initRecyclerView() {
        songRecyclerView = findViewById(R.id.songSearchList);

        songList = getIntent().getParcelableArrayListExtra("songList");
        adapter = new SongListAdapter(songList);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        songRecyclerView.setLayoutManager(layoutManager);
        songRecyclerView.setAdapter(adapter);
        songRecyclerView.setNestedScrollingEnabled(false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search_menu, menu);
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.actionSearch).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setMaxWidth(Integer.MAX_VALUE);
        searchView.setQueryHint("搜索歌曲...");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                List<SongInfo> searchedSongs = new ArrayList<>();
                for (SongInfo song: songList) {
                    if (song.getSongName().toLowerCase().contains(newText.toLowerCase())) {
                        searchedSongs.add(song);
                    }
                }
                adapter.handleSearchResult(searchedSongs);
                return false;
            }
        });

        menu.findItem(R.id.actionSearch).expandActionView();
        return true;
    }
}