package com.sjtu.karaoke;

import android.app.SearchManager;
import android.content.Context;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
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
 * @Author: 郭志东
 * @Date: 2021/3/28
 * @Version: v1.3
 * @Description: 歌曲搜素界面。用户可以在顶部的搜索栏根据歌名和歌手对歌曲进行搜索
 */

public class SearchActivity extends AppCompatActivity {
    // 歌曲列表控件
    private RecyclerView songRecyclerView;
    // 歌曲列表适配器
    private SongListAdapter adapter;
    // 歌曲信息
    private List<SongInfo> songList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbarSearch);
        // 初始化返回按钮
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        // 初始化歌曲列表控件
        initRecyclerView();
    }

    /**
     * 歌曲信息从主页传入，所以用户在搜索栏中输入关键词时不会向后端发送请求，而是直接从已经获取的歌曲信息中筛选
     */
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
                    // 按照歌曲名和歌手名进行搜索
                    if (song.getSongName().toLowerCase().contains(newText.toLowerCase()) ||
                        song.getSinger().toLowerCase().contains(newText.toLowerCase())) {
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

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return false;
    }
}