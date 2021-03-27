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
import com.sjtu.karaoke.util.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SearchActivity extends AppCompatActivity {
    private List<Data.Song> songs;
    private SongListAdapter adapter;
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
        RecyclerView songSearchList = (RecyclerView) findViewById(R.id.songSearchList);

        songs = Data.songs;
        adapter = new SongListAdapter(songs);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        songSearchList.setLayoutManager(layoutManager);
        songSearchList.setAdapter(adapter);
        songSearchList.setNestedScrollingEnabled(false);
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
                List<Data.Song> searchedSongs = new ArrayList<>();
                for (Data.Song song: songs) {
                    if (song.songName.contains(newText)) {
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