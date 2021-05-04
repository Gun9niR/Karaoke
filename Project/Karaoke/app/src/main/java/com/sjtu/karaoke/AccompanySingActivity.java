package com.sjtu.karaoke;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.dreamfish.record.AudioRecorder;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.sang.lrcview.LrcView;
import org.sang.lrcview.bean.LrcBean;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.ListIterator;
import java.util.stream.Collectors;

import static com.dreamfish.record.AudioRecorder.PCM_SPLIT_INTERVAL;
import static com.sjtu.karaoke.singrater.RatingUtil.getScore;
import static com.sjtu.karaoke.singrater.RatingUtil.init;
import static com.sjtu.karaoke.util.Constants.PROGRESS_UPDATE_INTERVAL;
import static com.sjtu.karaoke.util.Constants.RECORD_DELAY_LB;
import static com.sjtu.karaoke.util.Constants.RECORD_DELAY_UB;
import static com.sjtu.karaoke.util.MediaPlayerUtil.loadAudioFileAndPrepareExoPlayer;
import static com.sjtu.karaoke.util.MediaPlayerUtil.terminateExoPlayer;
import static com.sjtu.karaoke.util.MiscUtil.getAccompanyFullPath;
import static com.sjtu.karaoke.util.MiscUtil.getLyricFullPath;
import static com.sjtu.karaoke.util.MiscUtil.getMVFullPath;
import static com.sjtu.karaoke.util.MiscUtil.getOriginalFullPath;
import static com.sjtu.karaoke.util.MiscUtil.getRateFullPath;
import static com.sjtu.karaoke.util.MiscUtil.showLoadingDialog;
import static com.sjtu.karaoke.util.MiscUtil.showToast;
import static com.sjtu.karaoke.util.MiscUtil.verifyRecorderPermissions;
import static com.sjtu.karaoke.util.WavUtil.getWAVDuration;
/*
 * @ClassName: AccompanySingActivity
 * @Author: guozh
 * @Date: 2021/3/28
 * @Version: v1.2
 * @Description: 伴奏演唱界面。本类中包含了如下功能：
 *                  1. 各个组件的初始化、设置点击事件
 *                  2. 根据传入的歌曲信息，初始化MV播放器、伴奏播放器、原唱播放器、歌词滚动器
 *                  3. 在播放时监控进度、更新进度条、得分条
 *                  4. 录音
 */

public class AccompanySingActivity extends AppCompatActivity {
    SimpleExoPlayer mvPlayer;
    LrcView lrcView;
    SimpleExoPlayer accompanyPlayer;
    SimpleExoPlayer originalPlayer;
    ProgressBar progressBar;
    ProgressBar scoreBar;
    FloatingActionButton fab;
    Runnable progressMonitor;
    AudioRecorder voiceRecorder;
    BottomNavigationView bottomNavigationView;
    Handler handler = new Handler();

    // 状态量
    // 当前歌的id
    Integer id;
    // 当前歌名
    String songName;
    // 歌曲的持续时间，mv和所有伴奏的声音时长一样
    Integer nextPcmSplitTime;
    // 当前的播放状态，未开始、正在播放、暂停
    State state;
    // 有/无原唱
    SingMode singMode;
    // 每句话的歌词
    List<LrcBean> lrcs;
    // 当前歌词迭代器
    ListIterator<LrcBean> lrcIterator;
    // 当前歌词
    LrcBean currentLrc;
    // 得分
    Integer totalScore;
    Integer accuracyScore;
    Integer emotionScore;
    Integer breathScore;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accompany_sing);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        verifyRecorderPermissions(this);

        // UI-related initialization
        initSongName();
        initToolbar();
        initState();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onStart() {
        super.onStart();
        // return from sing refsult activity or from main activity, initialize all players
        if (state == State.UNSTARTED) {
            // Player-related initialization
            accompanyPlayer = new SimpleExoPlayer.Builder(this).build();
            loadAudioFileAndPrepareExoPlayer(accompanyPlayer, getAccompanyFullPath(songName));
            originalPlayer = new SimpleExoPlayer.Builder(this).build();
            loadAudioFileAndPrepareExoPlayer(originalPlayer, getOriginalFullPath(songName));

            nextPcmSplitTime = PCM_SPLIT_INTERVAL;

            initOnCompleteListener(originalPlayer);
            initRatingSystem();
            initMVPlayer();
            initLrcView();
            initVoiceRecorder();
            initProgressBar();
            initScore();
            initScoreBar();
            initFab();
            initState();
            initBottomNavbar();

            muteOriginal();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.sing_menu, menu);
        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.retry) {
            if (state != State.UNSTARTED) {

                // todo: clear record, reset score, progress bar, terminate async
                stopActivity(false);
                onStart();
            }
        }
        else {
            if (this.state != State.UNSTARTED) {
                stopActivity(false);
            }
            onBackPressed();
        }

        return true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        // 在播放时退出app，暂停播放和录音
        if (this.state == State.PLAYING) {
            // 在播放时退出app
            pause();
        }
    }

    /*
     * onDestroy() is not called when hitting finish or close the app
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        System.out.println("AccompanySing OnDestroy");
    }

    private void initSongName() {
        Intent intent = getIntent();
        id = intent.getIntExtra("id", 0);
        songName = intent.getStringExtra("songName");
    }

    private void initToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbarAccompanySing);
        TextView title = findViewById(R.id.toolbarAccompanySingTitle);
        title.setText(songName);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
    }

    private void initState() {
        state = State.UNSTARTED;
        singMode = SingMode.WITHOUT;
    }

    private void initOnCompleteListener(SimpleExoPlayer player) {
        player.addListener(new Player.EventListener() {
            @Override
            public void onPlaybackStateChanged(int state) {
                if (state == Player.STATE_ENDED) {
                    Intent intent = new Intent(getApplicationContext(), SingResultActivity.class);
                    intent.putExtra("id", id);
                    intent.putExtra("songName", songName);
                    startActivity(intent);
                }
            }
        });
    }

    private void initRatingSystem() {
        Dialog loadingDialog = showLoadingDialog(this, "正在初始化");
        if (init(getRateFullPath(songName), PCM_SPLIT_INTERVAL, RECORD_DELAY_LB, RECORD_DELAY_UB).equals("Done")) {
            System.out.println("========== Rating init success ==========");
        }
        loadingDialog.dismiss();
    }

    private void initMVPlayer() {
        // set up mv player and the view it should attach to
        mvPlayer = new SimpleExoPlayer.Builder(this).build();
        PlayerView mvPlayerView = findViewById(R.id.mvView);
        mvPlayerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FILL);
        mvPlayerView.setPlayer(mvPlayer);
        mvPlayerView.setUseController(false);

        File mvFile = new File(getMVFullPath(songName));
        MediaItem mv = MediaItem.fromUri(Uri.fromFile(mvFile));
        mvPlayer.setMediaItem(mv);
        mvPlayer.prepare();
        mvPlayer.setVolume(0);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void initLrcView() {
        lrcView = findViewById(R.id.lrcRoller);
        lrcView.setHighLineColor(ContextCompat.getColor(getApplicationContext(), R.color.purple_500));
        try {
            InputStream is = new FileInputStream(getLyricFullPath(songName));

            String lrc = new BufferedReader(new InputStreamReader(is))
                    .lines().collect(Collectors.joining("\n"));

            lrcs = lrcView.setLrc(lrc);
            lrcIterator = lrcs.listIterator();
            currentLrc = lrcIterator.next();

            lrcView.setPlayer(originalPlayer);
            lrcView.init();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initVoiceRecorder() {
        voiceRecorder = AudioRecorder.getInstance();
        voiceRecorder.createDefaultAudio(songName);
        voiceRecorder.setCurrentLrc(currentLrc);
    }

    private void initProgressBar() {
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setMax((int) getWAVDuration(getOriginalFullPath(songName)));
        progressBar.setProgress(0);
    }

    private void initScore() {
        totalScore = 0;
        accuracyScore = 0;
        emotionScore = 0;
        breathScore = 0;
    }

    /**
     * 初始化所有分数变量和分数条
     */
    private void initScoreBar() {
        scoreBar = findViewById(R.id.scoreBar);
        int numOfLinesToRate = 0;

        for (LrcBean lrc: lrcs) {
            if (lrc.shouldRate()) {
                ++numOfLinesToRate;
            }
        }
        scoreBar.setMax(numOfLinesToRate * 100);
        scoreBar.setProgress(0);
        Drawable draw = getDrawable(R.drawable.custom_scorebar);
        scoreBar.setProgressDrawable(draw);
    }

    private void initFab() {
        fab = findViewById(R.id.fabPlayPause);
        fab.setImageResource(R.drawable.ic_fab_play);
        fab.setOnClickListener(view -> {
            if (state == State.PLAYING) {
                pause();
            } else if (state == State.PAUSE) {
                start();
            } else {
                start();
                bottomNavigationView.getMenu().getItem(2).setEnabled(true);
            }
        });
    }

    private void muteOriginal() {
        originalPlayer.setVolume(0);
    }

    private void unmuteOriginal() {
        originalPlayer.setVolume(1);
    }

    private void setProgressMonitor(SimpleExoPlayer player) {
        progressMonitor = new Runnable() {
            @Override
            public void run() {
                // update progress bar
                int currentPosition = (int) player.getContentPosition();
                progressBar.setProgress(currentPosition);

                if (currentPosition >= nextPcmSplitTime) {
                    voiceRecorder.setCurrentPcmStartTime(nextPcmSplitTime - PCM_SPLIT_INTERVAL);
                    if (lrcIterator.hasNext() && currentPosition > currentLrc.getEnd()) {
                        if (currentLrc.shouldRate()) {
                            rate((int) currentLrc.getStart(), (int) currentLrc.getEnd());
                        }
                        currentLrc = lrcIterator.next();
                        voiceRecorder.setCurrentLrc(currentLrc);
                    }
                    voiceRecorder.setShouldStartNewPcm(true);

                    nextPcmSplitTime += PCM_SPLIT_INTERVAL;
                }

                handler.postDelayed(this, PROGRESS_UPDATE_INTERVAL);
            }
        };
    }

    private void initBottomNavbar() {
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setBackground(null);
        // 禁用中间的占位item
        bottomNavigationView.getMenu().getItem(1).setEnabled(false);
        // 禁用完成，因为录音还没有开始
        disableFinishButton();

        bottomNavigationView.setOnNavigationItemSelectedListener(
                item -> {
                    switch (item.getItemId()) {
                        case R.id.singingMode:
                            if (singMode == SingMode.WITH) {
                                item.setTitle("伴唱");
                                singMode = SingMode.WITHOUT;
                                muteOriginal();
                            } else {
                                item.setTitle("原唱");
                                singMode = SingMode.WITH;
                                unmuteOriginal();
                            }
                            break;
                        case R.id.singingFinish:
                            state = State.UNSTARTED;
                            // it has to be placed here, to wait for the merging to complete
                            stopActivity(true);
                            Intent intent = new Intent(getApplicationContext(), SingResultActivity.class);
                            intent.putExtra("id", id);
                            intent.putExtra("songName", songName);
                            startActivity(intent);
                            break;
                    }
                    return false;
                }
        );
    }

    private void pauseRecording() {
        voiceRecorder.pauseRecord();
    }

    private void startRecording() {
        voiceRecorder.startRecord(null);
    }

    private void stopUpdateProgressBar() {
        handler.removeCallbacks(progressMonitor);
    }

    private void start() {
        startRecording();
        startAllPlayers();
    }

    private void startAllPlayers() {
        originalPlayer.play();
        accompanyPlayer.play();
        mvPlayer.play();
        fab.setImageResource(R.drawable.ic_pause);
        state = State.PLAYING;

        setProgressMonitor(originalPlayer);
        monitorProgress();
    }

    private void pause() {
        // change of state is done in pauseAllPlayers
        pauseAllPlayers();
        pauseRecording();
    }

    private void pauseAllPlayers() {
        accompanyPlayer.pause();
        originalPlayer.pause();
        mvPlayer.pause();
        fab.setImageResource(R.drawable.ic_fab_play);
        state = State.PAUSE;
        handler.removeCallbacks(progressMonitor);
    }

    private void restartAllPlayers() {
        accompanyPlayer.seekTo(0);
        accompanyPlayer.pause();
        originalPlayer.seekTo(0);
        originalPlayer.pause();
        mvPlayer.pause();
        mvPlayer.seekTo(0);
        fab.setImageResource(R.drawable.ic_fab_play);
        stopUpdateProgressBar();
        state = State.UNSTARTED;
    }

    private void monitorProgress() {
        handler.postDelayed(progressMonitor, 0);
    }

    /**
     *
     * @param shouldMergePcm
     */
    private void stopActivity(boolean shouldMergePcm) {
        this.state = State.UNSTARTED;
        voiceRecorder.stopRecord(shouldMergePcm);
        lrcView.alertPlayerReleased();
        handler.removeCallbacks(progressMonitor);
        terminateExoPlayer(mvPlayer);
        terminateExoPlayer(accompanyPlayer);
        terminateExoPlayer(originalPlayer);
    }

    private void disableFinishButton() {
        bottomNavigationView.getMenu().getItem(2).setEnabled(false);
    }

    /**
     * Rate the record in a given interval
     * @param startTime Starting time of the line (ms)
     * @param endTime End time of the line (ms)
     */
    public void rate(int startTime, int endTime) {
        new Thread(() -> {
            while (!voiceRecorder.isf0AnalysisComplete(startTime, endTime)) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            String scoreStr = getScore(startTime, endTime);
            String[] scores = scoreStr.split(" ");
            int total = Integer.parseInt(scores[0]);
            int accuracy = Integer.parseInt(scores[1]);
            int emotion = Integer.parseInt(scores[2]);
            int breath = Integer.parseInt(scores[3]);

            totalScore += total;
            accuracyScore += accuracy;
            emotionScore += emotion;
            breathScore += breath;

            AccompanySingActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    scoreBar.setProgress(totalScore, true);
                }
            });

            Looper.prepare();
            showToast(AccompanySingActivity.this, Integer.toString(total));
            Looper.loop();
        }).start();

    }

    private enum State {
        PAUSE, PLAYING, UNSTARTED
    }

    private enum SingMode {
        WITH, WITHOUT
    }
}
