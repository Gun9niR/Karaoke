package com.sjtu.karaoke;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.sjtu.karaoke.adapter.RecordListAdapter;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class LocalRecordActivity extends AppCompatActivity {

    MediaPlayer recordPlayer;
    private enum State { PAUSE, PLAYING, UNSTARTED};
    private State state = State.UNSTARTED;
    Thread recordProgressUpdater;

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
        RecordListAdapter adapter = new RecordListAdapter(this, records);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        localRecordList.setLayoutManager(layoutManager);
        localRecordList.setAdapter(adapter);
        localRecordList.setNestedScrollingEnabled(false);

        ImageButton btnPlayRecord = findViewById(R.id.btnPlayRecord);
        btnPlayRecord.setEnabled(false);
        SeekBar seekbarRecordProgress = findViewById(R.id.seekbarRecordProgress);
        seekbarRecordProgress.setEnabled(false);
    }

    @Override
    protected void onStop() {
        super.onStop();
        recordProgressUpdater.interrupt();
        if (recordPlayer != null) {
            recordPlayer.stop();
            // recordPlayer.release();
        }
    }

    public void playRecord(Data.Record record) {
        initRecordPlayer("Attention.mp3");

        initSeekbar();

        initPlayBtn();

        initRecordTitleAndCover(record);

        this.state = State.PLAYING;

        recordPlayer.start();
    }

    private void initSeekbar() {
        if (recordProgressUpdater != null) {
            recordProgressUpdater.interrupt();
        }

        SeekBar seekbarRecordProgress = findViewById(R.id.seekbarRecordProgress);
        if (this.state == State.UNSTARTED) {
            seekbarRecordProgress.setEnabled(true);
        }

        seekbarRecordProgress.setMax(recordPlayer.getDuration());
        seekbarRecordProgress.setProgress(0);

        seekbarRecordProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    recordPlayer.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        recordProgressUpdater = new Thread(new Runnable() {
            @Override
            public void run() {
                int progress;
                do {
                    progress = recordPlayer.getCurrentPosition();

                    seekbarRecordProgress.setProgress(progress);
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } while (!Thread.currentThread().isInterrupted() && progress <= recordPlayer.getDuration());

            }
        });

        recordProgressUpdater.start();
    }

    private void initRecordTitleAndCover(Data.Record record) {
        TextView textView = findViewById(R.id.recordPlayerName);
        textView.setText(record.recordName);

        Animation rotateAnimation = AnimationUtils.loadAnimation(this, R.anim.rotate_record_cover);
        CircleImageView circleImageView = findViewById(R.id.recordPlayerCover);
        circleImageView.setImageResource(record.recordCover);
        circleImageView.startAnimation(rotateAnimation);
    }

    private void initPlayBtn() {
        ImageButton btnPlayRecord = findViewById(R.id.btnPlayRecord);
        if (state == State.UNSTARTED) {
            btnPlayRecord.setImageResource(R.drawable.ic_record_pause);
        }
        btnPlayRecord.setEnabled(true);

        btnPlayRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (state == State.PAUSE) {
                    state = State.PLAYING;
                    recordPlayer.start();
                    btnPlayRecord.setImageResource(R.drawable.ic_record_pause);
                } else if (state == State.PLAYING) {
                    state = State.PAUSE;
                    recordPlayer.pause();
                    btnPlayRecord.setImageResource(R.drawable.ic_play_record);
                }
            }
        });
    }

    private void initRecordPlayer(String fileName) {
        if (recordPlayer != null) {
            if (recordProgressUpdater != null) {
                recordProgressUpdater.interrupt();
            }
            recordPlayer.release();
        }
        recordPlayer = new MediaPlayer();
        AssetFileDescriptor afd = null;
        try {
            afd = getAssets().openFd(fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            recordPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            recordPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}