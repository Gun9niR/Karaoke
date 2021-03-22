package com.sjtu.karaoke;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sjtu.karaoke.adapter.RecordListAdapter;

import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.sjtu.karaoke.util.Utils.loadAndPrepareMediaplayer;
import static com.sjtu.karaoke.util.Utils.terminateMediaPlayer;

public class LocalRecordActivity extends AppCompatActivity {

    MediaPlayer recordPlayer;
    private enum State { PAUSE, PLAYING, UNSTARTED};
    private State state = State.UNSTARTED;
    int duration;
    Handler handler = new Handler();
    Runnable runnable;
    SeekBar seekbarRecordProgress;
    boolean playerReleased;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_local_record);

        initToolbar();

        initLocalRecordList();

        initRunnable();

        ImageButton btnPlayRecord = findViewById(R.id.btnPlayRecord);
        btnPlayRecord.setEnabled(false);
        seekbarRecordProgress = findViewById(R.id.seekbarRecordProgress);
        seekbarRecordProgress.setEnabled(false);

        playerReleased = true;
    }

    private void initRunnable() {
        runnable = new Runnable() {
            @Override
            public void run() {
                if (!playerReleased) {
                    seekbarRecordProgress.setProgress(recordPlayer.getCurrentPosition());
                    handler.postDelayed(this, 500);
                }
            }
        };
    }

    private void initLocalRecordList() {
        RecyclerView localRecordList = (RecyclerView) findViewById(R.id.localRecordList);

        List<Data.Record> records = Data.records;
        RecordListAdapter adapter = new RecordListAdapter(this, records);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        localRecordList.setLayoutManager(layoutManager);
        localRecordList.setAdapter(adapter);
        localRecordList.setNestedScrollingEnabled(false);
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbarLocalRecord);

        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
    }

    @Override
    protected void onStop() {
        super.onStop();
        playerReleased = true;
        if (recordPlayer != null) {
            terminateMediaPlayer(recordPlayer);
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
        handler.removeCallbacks(runnable);
        if (this.state == State.UNSTARTED) {
            seekbarRecordProgress.setEnabled(true);
        }

        seekbarRecordProgress.setMax(duration);
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

        handler.postDelayed(runnable, 0);
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
                    handler.postDelayed(runnable, 0);
                    btnPlayRecord.setImageResource(R.drawable.ic_record_pause);
                } else if (state == State.PLAYING) {
                    state = State.PAUSE;
                    recordPlayer.pause();
                    handler.removeCallbacks(runnable);
                    btnPlayRecord.setImageResource(R.drawable.ic_play_record);
                }
            }
        });
    }

    private void initRecordPlayer(String fileName) {
        terminateMediaPlayer(recordPlayer);

        recordPlayer = new MediaPlayer();

        loadAndPrepareMediaplayer(this, recordPlayer, fileName);

        duration = recordPlayer.getDuration();
        playerReleased = false;
    }
}