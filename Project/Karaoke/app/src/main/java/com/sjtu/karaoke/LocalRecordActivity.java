package com.sjtu.karaoke;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
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
import static com.sjtu.karaoke.util.MiscUtil.setImageFromFile;
import static com.sjtu.karaoke.util.PathUtil.getAlbumCoverFullPath;

/*
 * @ClassName: LocalRecordActivity
 * @Author: 郭志东
 * @Date: 2021/3/28
 * @Version: v1.3
 * @Description: 本地录音界面。本类中包含了如下功能：
 *                  1. 各个组件的初始化、调用RecordAdapter类来初始化本地录音列表
 *                  2. 播放用户选择的本地伴奏
 *                  3. 将本地录音分享至QQ、微信、TIM
 *                  4. 删除本地录音
 */

public class LocalRecordActivity extends AppCompatActivity {
    // 录音播放器
    MediaPlayer recordPlayer;
    // 底部播放器的专辑封面
    CircleImageView circleImageView;
    // 播放录音按钮
    ImageButton btnPlayRecord;
    // 录音的歌名
    TextView recordSongName;
    // 录音播放进度
    SeekBar seekbarRecordProgress;
    // 专辑封面的旋转动画
    Animation rotateAnimation;

    // 监听进度
    Handler handler = new Handler();
    Runnable progressMonitor;

    // 录音持续时间
    private int duration;
    // 播放停止flag
    private boolean playerReleased;
    // 当前播放状态
    private State state = State.UNSTARTED;
    // 当前正在播放的录音的绝对路径
    private String currentRecordFullPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_local_record);

        // 初始化顶部工具栏
        initToolbar();
        //初始化本地录音列表
        initLocalRecordList();
        // 初始化进度监听
        initRunnable();
        // 初始化专辑封面的转动动画
        initAnimation();
        // 初始化底部的录音播放栏
        initBottomRecordBar();
    }

    private void initBottomRecordBar() {
        recordSongName = findViewById(R.id.recordPlayerName);
        circleImageView = findViewById(R.id.recordPlayerCover);
        btnPlayRecord = findViewById(R.id.btnPlayRecord);
        seekbarRecordProgress = findViewById(R.id.seekbarRecordProgress);

        resetBottomRecordBar();
    }

    /**
     * 将底部的录音播放栏设置为暂无播放状态，包括设置提示文字、清除图片、禁用播放按钮、重置进度条
     */
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
        progressMonitor = new Runnable() {
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

    /**
     * 在用户退出APP时暂停播放
     */
    @Override
    protected void onStop() {
        super.onStop();
        if (state != State.UNSTARTED) {
            pauseRecordPlayer();
        }
    }

    /**
     * 释放播放器的资源
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        playerReleased = true;
        if (recordPlayer != null && state != State.UNSTARTED) {
            terminateMediaPlayer(recordPlayer);
        }
    }

    /**
     * 捕获用户点击返回主页按钮的事件，调用onBackPressed()，否则返回时会返回到歌曲浏览界面
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return false;
    }

    /**
     * 播放用户选择的录音
     * @param record 要播放的录音
     */
    public void playRecord(Record record) {
        // 设置播放器的数据源
        initRecordPlayer(record.getRecordFullPath());
        // 初始化进度条
        initSeekbar();
        // 初始化播放按钮
        initPlayBtn();
        // 初始化专辑封面
        initRecordTitleAndCover(record);


        this.state = State.PLAYING;
        currentRecordFullPath = record.getRecordFullPath();

        // 在播放完成后不重复播放
        recordPlayer.setOnCompletionListener(mediaPlayer -> {
            this.state = State.UNSTARTED;
            recordPlayer.seekTo(0);
            btnPlayRecord.setImageResource(R.drawable.ic_play_record);
            circleImageView.clearAnimation();
            handler.removeCallbacks(progressMonitor);
            seekbarRecordProgress.setProgress(0);
        });

        startRecordPlayer();
    }

    /**
     * 初始化进度条
     */
    private void initSeekbar() {
        handler.removeCallbacks(progressMonitor);
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

        handler.postDelayed(progressMonitor, 0);
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

        btnPlayRecord.setOnClickListener(view -> {
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
        });
    }

    private void pauseRecordPlayer() {
        state = State.PAUSE;
        recordPlayer.pause();
        handler.removeCallbacks(progressMonitor);
        btnPlayRecord.setImageResource(R.drawable.ic_play_record);
        circleImageView.clearAnimation();
    }

    private void startRecordPlayer() {
        state = State.PLAYING;
        recordPlayer.start();
        handler.postDelayed(progressMonitor, 0);
        btnPlayRecord.setImageResource(R.drawable.ic_pause_record);
        circleImageView.startAnimation(rotateAnimation);
    }

    private void stopRecordPlayer() {
        state = State.UNSTARTED;
        terminateMediaPlayer(recordPlayer);
        handler.removeCallbacks(progressMonitor);
    }

    private void initRecordPlayer(String fullPath) {
        terminateMediaPlayer(recordPlayer);

        recordPlayer = new MediaPlayer();

        loadFileAndPrepareMediaPlayer(recordPlayer, fullPath);

        duration = recordPlayer.getDuration();
        playerReleased = false;
    }

    /**
     * 如果被删除的录音就是当前正在播放的录音，那么将底部的录音播放栏重设为暂无播放状态
     */
    public void checkCurrentDeletion(String fullPath) {
        if (fullPath.equals(currentRecordFullPath)) {
            stopRecordPlayer();
            resetBottomRecordBar();
        }
    }

private enum State {PAUSE, PLAYING, UNSTARTED}
}