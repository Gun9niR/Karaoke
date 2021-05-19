package com.sjtu.karaoke;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.drawable.Drawable;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.dreamfish.record.AudioRecorder;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.sjtu.karaoke.entity.Score;

import org.sang.lrcview.LrcView;
import org.sang.lrcview.bean.LrcBean;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
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
import static com.sjtu.karaoke.util.MiscUtil.getLyricInsrumentFullPath;
import static com.sjtu.karaoke.util.MiscUtil.getRateFullPath;
import static com.sjtu.karaoke.util.MiscUtil.parseScore;
import static com.sjtu.karaoke.util.MiscUtil.showLoadingDialog;


/*
 * @ClassName: InstrumentSingActivity
 * @Author: guozh
 * @Date: 2021/3/28
 * @Version: v1.2
 * @Description: 自弹自唱界面。本类中包含了如下功能：
 *                  1. 各个组件的初始化、设置点击事件
 *                  2. 根据传入的歌曲信息，初始化伴奏播放器、歌词滚动器、音效池
 *                  3. 在播放时监控进度、更新得分（不以得分条的形式显示）
 *                  4. 录音
 */

public class InstrumentSingActivity extends AppCompatActivity {

    Toolbar toolbar;
    List<ProgressBar> instrumentBtns;
    SimpleExoPlayer accompanyPlayer;
    LrcView lrcView;
    SoundPool chordPlayer;
    // todo: change to hashmap
    List<Integer> chords;
    AudioRecorder voiceRecorder;


    Runnable progressMonitor;
    Handler handler = new Handler();

    // 状态量
    // 当前歌的id
    Integer id;
    // 当前歌名
    String songName;
    // 下一次切分pcm的时间
    Integer nextPcmSplitTime;
    // 当前的播放状态，未开始、正在播放、暂停
    State state;
    // 每句话的歌词
    List<LrcBean> lrcs;
    // 当前歌词迭代器
    ListIterator<LrcBean> lrcIterator;
    // 当前歌词
    LrcBean currentLrc;
    // 得分
    Score score;
    // 演奏片段开始时间
    Integer startTime;
    // 演奏片段结束时间
    Integer finishTime;
    // 和弦/按键个数
    Integer chordNum;

    // todo: parse .chordTrans file
    // todo: generate buttons in a linear layout
    // todo: merge each chord .wav file and store as temporary file
    // todo: load soundpool with temporary wav files
    // todo: figure out how to update progress bar asyncly
    // todo: record the time when user pressed the button

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instrument_sing);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        initSongName();
        initToolbar();
        initState();
        initSoundPool();
        initBtns();
    }

    protected void onStart() {
        super.onStart();

        if (state == State.UNSTARTED) {
            // todo: parse .chordTrans file

            initAccompanyPlayer();

            nextPcmSplitTime = PCM_SPLIT_INTERVAL;

            initOnCompleteListener(accompanyPlayer);
            initRatingSystem();
            initLrcView();
            initVoiceRecorder();
            initScore();

            //            initInstrumentBtns();

            start();
        } else if (state == State.PAUSE) {
            start();
        }
    }

    private void initAccompanyPlayer() {
        accompanyPlayer = new SimpleExoPlayer.Builder(this).build();
        loadAudioFileAndPrepareExoPlayer(accompanyPlayer, getAccompanyFullPath(songName));
        accompanyPlayer.setVolume(0);

    }

    private void initRatingSystem() {
        Dialog loadingDialog = showLoadingDialog(this, "正在初始化");
        init(getRateFullPath(songName), PCM_SPLIT_INTERVAL, RECORD_DELAY_LB, RECORD_DELAY_UB);
        loadingDialog.dismiss();
    }

    private void initVoiceRecorder() {
        voiceRecorder = AudioRecorder.getInstance();
        voiceRecorder.createDefaultAudio(songName);
    }

    private void initScore() {
        score = new Score();
    }

    private void initOnCompleteListener(SimpleExoPlayer player) {
        player.addListener(new Player.EventListener() {
            @Override
            public void onPlaybackStateChanged(int state) {
                if (state == Player.STATE_ENDED) {
                    toolbar.getMenu().findItem(R.id.finish).setChecked(true);
                }
            }
        });
    }

    private void initSongName() {
        Intent intent = getIntent();
        id = intent.getIntExtra("id", 0);
        songName = intent.getStringExtra("songName");
    }

    private void initState() {
        state = State.UNSTARTED;
    }

    private void initSoundPool() {
        chordPlayer = new SoundPool.Builder()
                .setMaxStreams(chordNum)
                .build();
        chords = new ArrayList<>();

        loadChord("1.wav");
        loadChord("3.wav");
        loadChord("5.wav");
    }

    private void loadChord(String fileName) {
        AssetFileDescriptor afd = null;
        try {
            afd = getAssets().openFd(fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
        chords.add(chordPlayer.load(afd,1));
    }

    private void startAllPlayers() {
        accompanyPlayer.play();
        state = State.PLAYING;

        setProgressMonitor(accompanyPlayer);
        monitorProgress();
    }

    private void setProgressMonitor(SimpleExoPlayer player) {
        progressMonitor = new Runnable() {
            @Override
            public void run() {
                // update progress bar
                int currentPosition = (int) player.getContentPosition();

                if (currentPosition >= nextPcmSplitTime) {
                    voiceRecorder.setCurrentPcmStartTime(nextPcmSplitTime - PCM_SPLIT_INTERVAL);
                    if (lrcIterator.hasNext() && currentPosition > currentLrc.getEnd()) {
                        if (currentLrc.shouldRate()) {
                            rate((int) currentLrc.getStart(), (int) currentLrc.getEnd());
                        }
                        currentLrc = lrcIterator.next();
                    }
                    voiceRecorder.setShouldStartNewPcm(true);

                    nextPcmSplitTime += PCM_SPLIT_INTERVAL;
                }

                handler.postDelayed(this, PROGRESS_UPDATE_INTERVAL);
            }
        };
    }

    private void monitorProgress() {
        handler.postDelayed(progressMonitor, 0);
    }

    private void start() {
        voiceRecorder.startRecord(null);
        startAllPlayers();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        start();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (this.state == State.PLAYING) {
            pause();
        }
    }

    private void pause() {
        pauseAllPlayers();
        voiceRecorder.pauseRecord();
    }

    private void pauseAllPlayers() {
        accompanyPlayer.pause();
        state = State.PAUSE;
        handler.removeCallbacks(progressMonitor);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (voiceRecorder.getStatus() == AudioRecorder.Status.STATUS_PAUSE || voiceRecorder.getStatus() == AudioRecorder.Status.STATUS_START) {
            voiceRecorder.stopRecord(false);
        }
        lrcView.alertPlayerReleased();
        terminateExoPlayer(accompanyPlayer);
        chordPlayer.release();
    }

    private void initToolbar() {
        toolbar = findViewById(R.id.toolbarInstrumentSing);
        TextView title = findViewById(R.id.toolbarInstrumentSingTitle);
        title.setText(songName);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
    }

    private void initInstrumentBtns() {

        instrumentBtns = new ArrayList<>();

        instrumentBtns.add(findViewById(R.id.instrumentButton0));
        instrumentBtns.add(findViewById(R.id.instrumentButton1));
        instrumentBtns.add(findViewById(R.id.instrumentButton2));
        instrumentBtns.add(findViewById(R.id.instrumentButton3));
        instrumentBtns.add(findViewById(R.id.instrumentButton4));

        @SuppressLint("UseCompatLoadingForDrawables")
        Drawable draw = getDrawable(R.drawable.custom_instrument_button);

        Iterator<ProgressBar> btnIt = instrumentBtns.iterator();
        ProgressBar instrumentBtn;

        while (btnIt.hasNext()) {
            instrumentBtn = btnIt.next();
            instrumentBtn.setMax(100);
            instrumentBtn.setProgress(0);
            instrumentBtn.setProgressDrawable(draw);
        }

        instrumentBtns.get(0).setOnClickListener(view -> {
            for (Integer i: chords) {
                chordPlayer.play(i, 1, 1, 0, 0, 1);
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void initLrcView() {
        lrcView = findViewById(R.id.lrcRoller);
        lrcView.setHighLineColor(ContextCompat.getColor(getApplicationContext(), R.color.purple_500));
        try {
            InputStream is = new FileInputStream(getLyricInsrumentFullPath(songName));

            String lrc = new BufferedReader(new InputStreamReader(is))
                    .lines().collect(Collectors.joining("\n"));

            lrcs = lrcView.setLrc(lrc);
            lrcIterator = lrcs.listIterator();
            currentLrc = lrcIterator.next();

            lrcView.setPlayer(accompanyPlayer);
            lrcView.init();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initBtns() {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.instrument_sing_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.instrumentSingRetry) {
            retry();
            return true;
        } else if (item.getItemId() == R.id.finish) {
            state = State.UNSTARTED;
            Dialog loadingDialog = showLoadingDialog(this, "正在处理录音");
            stopActivity(true);
            loadingDialog.dismiss();
            Intent intent = new Intent(this, SingResultActivity.class);
            intent.putExtra("id", id);
            intent.putExtra("songName", songName);
            startActivity(intent);
        } else {
            if (this.state != State.UNSTARTED) {
                stopActivity(false);
            }
            onBackPressed();
        }

        return false;
    }

    private void retry() {
        stopActivity(false);
        onStart();
    }

    private void stopActivity(boolean shouldMergePcm) {
        this.state = State.UNSTARTED;
        voiceRecorder.stopRecord(shouldMergePcm);
        // todo: merge chord as well
        lrcView.alertPlayerReleased();
        handler.removeCallbacks(progressMonitor);
        terminateExoPlayer(accompanyPlayer);
    }

//
//    private void setBtnClickEvent(ProgressBar instrumentBtn) {
//            instrumentBtn.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    int progress = instrumentBtn.getProgress();
//                    instrumentBtn.setProgress(progress+10);
//                }
//            });
//    }

//        while (btnIt.hasNext()) {
//            final ProgressBar instrumentBtn = btnIt.next();
//            int progress = instrumentBtn.getProgress();
//            instrumentBtn.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    instrumentBtn.setProgress(progress+10);
//                }
//            });
//        }



//
//    private void setBtnClickEvent(ProgressBar instrumentBtn) {
//            instrumentBtn.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    int progress = instrumentBtn.getProgress();
//                    instrumentBtn.setProgress(progress+10);
//                }
//            });
//    }

//        while (btnIt.hasNext()) {
//            final ProgressBar instrumentBtn = btnIt.next();
//            int progress = instrumentBtn.getProgress();
//            instrumentBtn.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    instrumentBtn.setProgress(progress+10);
//                }
//            });
//        }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }

    /**
     * Rate the record in a given interval
     * @param startTime Starting time of the line (ms)
     * @param endTime End time of the line (ms)
     */
    private void rate(int startTime, int endTime) {
        new Thread(() -> {
            while (!voiceRecorder.isf0AnalysisComplete(startTime, endTime)) {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            String scoreStr = getScore(startTime, endTime);
            Integer[] scores = parseScore(scoreStr);
            score.update(scores);
        }).start();
    }

    private enum State {
        PAUSE, PLAYING, UNSTARTED
    }
}