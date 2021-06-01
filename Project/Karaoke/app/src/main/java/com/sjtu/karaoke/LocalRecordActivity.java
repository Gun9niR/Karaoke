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
import com.sjtu.karaoke.data.Record;

import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.sjtu.karaoke.util.MediaPlayerUtil.loadFileAndPrepareMediaPlayer;
import static com.sjtu.karaoke.util.MediaPlayerUtil.terminateMediaPlayer;
import static com.sjtu.karaoke.util.PathUtil.getAlbumCoverFullPath;
import static com.sjtu.karaoke.util.MiscUtil.setImageFromFile;

/*
 * @ClassName: LocalRecordActivity
 * @Author: guozh
 * @Date: 2021/3/28
 * @Version: v1.2
 * @Description: 本地录音界面。本类中包含了如下功能：
 *                  1. 各个组件的初始化、调用RecordAdapter类来初始化本地录音列表
 *                  2. 播放用户选择的本地伴奏
 *                  3. 将本地录音分享至微信
 */

public class LocalRecordActivity extends AppCompatActivity {

    MediaPlayer recordPlayer;
    CircleImageView circleImageView;
    ImageButton btnPlayRecord;
    TextView recordSongName;
    SeekBar seekbarRecordProgress;
    Animation rotateAnimation;
    Handler handler = new Handler();
    Runnable runnable;

    private int duration;
    private boolean playerReleased;
    private State state = State.UNSTARTED;
    private String currentRecordFullPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_local_record);

        initToolbar();

        initLocalRecordList();

        initRunnable();

        initAnimation();

        initBottomRecordBar();


    }

    private void initBottomRecordBar() {
        recordSongName = findViewById(R.id.recordPlayerName);
        circleImageView = findViewById(R.id.recordPlayerCover);
        btnPlayRecord = findViewById(R.id.btnPlayRecord);
        seekbarRecordProgress = findViewById(R.id.seekbarRecordProgress);

        resetBottomRecordBar();
    }

    private void resetBottomRecordBar() {
        recordSongName.setText("暂无播放");
        circleImageView.setImageBitmap(null);
        circleImageView.clearAnimation();
        btnPlayRecord.setEnabled(false);
        btnPlayRecord.setImageResource(R.drawable.ic_play_record);

        seekbarRecordProgress.setEnabled(false);
        seekbarRecordProgress.setProgress(0);
        playerReleased = true;
    }

    private void initAnimation() {
        rotateAnimation = AnimationUtils.loadAnimation(this, R.anim.rotate_record_cover);
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

        RecordListAdapter adapter = new RecordListAdapter(this);
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
        if (state != State.UNSTARTED) {
            pauseRecordPlayer();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        playerReleased = true;
        if (recordPlayer != null && state != State.UNSTARTED) {
            terminateMediaPlayer(recordPlayer);
        }
    }

    public void playRecord(Record record) {
        initRecordPlayer(record.getRecordFullPath());

        initSeekbar();

        initPlayBtn();

        initRecordTitleAndCover(record);

        this.state = State.PLAYING;
        currentRecordFullPath = record.getRecordFullPath();

        recordPlayer.setOnCompletionListener(mediaPlayer -> {
            this.state = State.UNSTARTED;
            recordPlayer.seekTo(0);
            btnPlayRecord.setImageResource(R.drawable.ic_play_record);
            circleImageView.clearAnimation();
            handler.removeCallbacks(runnable);
            seekbarRecordProgress.setProgress(0);
        });

        startRecordPlayer();
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

        // restart playing
        handler.postDelayed(runnable, 0);
    }

    private void initRecordTitleAndCover(Record record) {
        TextView textView = findViewById(R.id.recordPlayerName);
        textView.setText(record.getSongName());

        setImageFromFile(getAlbumCoverFullPath(record.getSongName()), circleImageView);
    }

    private void initPlayBtn() {
        btnPlayRecord = findViewById(R.id.btnPlayRecord);
        if (state == State.UNSTARTED) {
            btnPlayRecord.setImageResource(R.drawable.ic_pause_record);
        }
        btnPlayRecord.setEnabled(true);

        btnPlayRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (state) {
                    case UNSTARTED:
                    case PAUSE:
                        startRecordPlayer();
                        break;
                    case PLAYING:
                        pauseRecordPlayer();
                        break;
                    default:
                        break;
                }
            }
        });
    }

    private void pauseRecordPlayer() {
        state = State.PAUSE;
        recordPlayer.pause();
        handler.removeCallbacks(runnable);
        btnPlayRecord.setImageResource(R.drawable.ic_play_record);
        circleImageView.clearAnimation();
    }

    private void startRecordPlayer() {
        state = State.PLAYING;
        recordPlayer.start();
        handler.postDelayed(runnable, 0);
        btnPlayRecord.setImageResource(R.drawable.ic_pause_record);
        circleImageView.startAnimation(rotateAnimation);
    }

    private void stopRecordPlayer() {
        state = State.UNSTARTED;
        terminateMediaPlayer(recordPlayer);
        handler.removeCallbacks(runnable);
    }

    private void initRecordPlayer(String fullPath) {
        terminateMediaPlayer(recordPlayer);

        recordPlayer = new MediaPlayer();

        loadFileAndPrepareMediaPlayer(recordPlayer, fullPath);

        duration = recordPlayer.getDuration();
        playerReleased = false;
    }

    /**
     * Called when a local record is removed, reset bottom record bar if the currently playing record
     * is the one being deleted
     */
    public void checkCurrentDeletion(String fullPath) {
        if (fullPath.equals(currentRecordFullPath)) {
            stopRecordPlayer();
            resetBottomRecordBar();
        }
    }

private enum State {PAUSE, PLAYING, UNSTARTED}
}