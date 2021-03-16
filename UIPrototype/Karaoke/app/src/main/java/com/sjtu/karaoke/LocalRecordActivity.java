package com.sjtu.karaoke;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import com.sjtu.karaoke.adapter.RecordListAdapter;
import com.sjtu.karaoke.adapter.SongListAdapter;

import java.util.List;
import java.util.Objects;

public class LocalRecordActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_local_record);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbarLocalRecord);
        // set up back button
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        RecyclerView localRecordList = (RecyclerView) findViewById(R.id.localRecordList);

        List<Data.Record> records = Data.records;
        RecordListAdapter adapter = new RecordListAdapter(records);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        localRecordList.setLayoutManager(layoutManager);
        localRecordList.setAdapter(adapter);
        localRecordList.setNestedScrollingEnabled(false);
    }
}