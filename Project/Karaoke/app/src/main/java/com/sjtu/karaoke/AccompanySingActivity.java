package com.sjtu.karaoke;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
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
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.dreamfish.record.AudioRecorder;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.sang.lrcview.LrcView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

import static com.sjtu.karaoke.util.Utils.loadAndPrepareMediaplayer;
import static com.sjtu.karaoke.util.Utils.terminateMediaPlayer;
import static com.sjtu.karaoke.util.Utils.verifyRecorderPermissions;

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

    private static final int UPDATE_INTERVAL = 100;
    VideoView videoView;
    LrcView lrcView;
    MediaPlayer accompanyPlayer;
    ProgressBar mProgressBar, mScoreBar;
    TextView scoreRecorder;
    FloatingActionButton fab;
    Integer duration;
    State state;
    SingMode singMode;
    Handler handler = new Handler();
    Runnable progressBarUpdater;
    AudioRecorder voiceRecorder;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        System.out.println("OnCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accompany_sing);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        accompanyPlayer = new MediaPlayer();
        loadAndPrepareMediaplayer(this, accompanyPlayer, "Attention.mp3");
        // todo: voicePlayer

        verifyRecorderPermissions(this);

        initToolbar();

        initVoiceRecorder();
        // todo: read by file name
        initVideoView();

        initLrcView("Attention.lrc");

        initBottomNavbar();

        initProgressBar();

        initScoreBar();

        initFab();

        initState();

        initProgressBarUpdater();
        /**
         * todo: store song name, accompany file name and mv file name (if any)
         * when choosing a song from ViewSongsFragment, above data will be present (in the Intent)
         * whereas returning from sing result, they will be absent, so setup the previous song
         */
    }

    // todo: change to file name
    private void initVoiceRecorder() {
        voiceRecorder = AudioRecorder.getInstance();

        voiceRecorder.createDefaultAudio("Attention");
    }

    private void initProgressBarUpdater() {
        progressBarUpdater = new Runnable() {
            @Override
            public void run() {
                mProgressBar.setProgress(accompanyPlayer.getCurrentPosition());
                handler.postDelayed(this, 500);
            }
        };
    }

    private void initState() {
        state = State.UNSTARTED;
        singMode = SingMode.WITHOUT;
    }

    private void initFab() {
        fab = findViewById(R.id.fabPlayPause);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (state == State.PLAYING) {
                    pauseAllPlayers();
                    pauseRecording();
                    // todo: pause recording
                } else if (state == State.PAUSE) {
                    startAllPlayers();
                    startRecording();
                    // todo: start recording
                } else {
                    startAllPlayers();
                    startRecording();
                    // todo: do some async task, setting progress bar and score bar
                }
            }
        });
    }

    private void pauseRecording() {
        voiceRecorder.pauseRecord();
    }

    private void startRecording() {
        voiceRecorder.startRecord(null);
    }

    private void startUpdateProgressBar() {
        handler.postDelayed(progressBarUpdater, 0);
    }

    private void stopUpdateProgressBar() {
        handler.removeCallbacks(progressBarUpdater);
    }

    private void startAllPlayers() {
        accompanyPlayer.start();
        videoView.start();
        fab.setImageResource(R.drawable.ic_pause);
        state = State.PLAYING;

        accompanyPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

            @Override
            public void onCompletion(MediaPlayer accompanyPlayer) {
                Intent intent = new Intent(getApplicationContext(), SingResultActivity.class);
                startActivity(intent);
            }

        });

        startUpdateProgressBar();
    }

    private void pauseAllPlayers() {
        accompanyPlayer.pause();
        videoView.pause();
        fab.setImageResource(R.drawable.ic_fab_play);
        state = State.PAUSE;
        handler.removeCallbacks(progressBarUpdater);
    }

    private void initScoreBar() {
        mScoreBar = (ProgressBar) findViewById(R.id.scoreBar);
        mScoreBar.setMax(100);
        mScoreBar.setProgress(80);
        Drawable draw = getDrawable(R.drawable.custom_scorebar);
        mScoreBar.setProgressDrawable(draw);
    }

    private void initProgressBar() {
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mProgressBar.setMax(accompanyPlayer.getDuration());
        mProgressBar.setProgress(0);
    }

    private void initBottomNavbar() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setBackground(null);
        bottomNavigationView.getMenu().getItem(1).setEnabled(false);
        bottomNavigationView.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.singingMode:
                                if (singMode == SingMode.WITH) {
                                    item.setTitle("伴唱");
                                    singMode = SingMode.WITHOUT;
                                } else {
                                    item.setTitle("原唱");
                                    singMode = SingMode.WITH;
                                }
                                break;
                            case R.id.singingFinish:
                                voiceRecorder.stopRecord();
                                state = State.UNSTARTED;
                                Intent intent = new Intent(getApplicationContext(), SingResultActivity.class);
                                startActivity(intent);
                                break;
                        }
                        return false;
                    }
                }
        );
    }

    private void restartAllPlayers() {
        accompanyPlayer.seekTo(0);
        accompanyPlayer.pause();
        videoView.pause();
        videoView.seekTo(0);
        fab.setImageResource(R.drawable.ic_fab_play);
        stopUpdateProgressBar();
        state = State.UNSTARTED;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void initLrcView(String fileName) {
        lrcView = findViewById(R.id.lrcRoller);
        lrcView.setHighLineColor(ContextCompat.getColor(getApplicationContext(), R.color.purple_500));

        String lrc = null;
        try {
            InputStream stream = getAssets().open(fileName);

            lrc = new BufferedReader(new InputStreamReader(stream))
                    .lines().collect(Collectors.joining("\n"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        lrcView.setLrc(lrc);
        lrcView.setPlayer(accompanyPlayer);
        lrcView.init();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.retry) {
            if (state != State.UNSTARTED) {
                // todo: clear record, reset score, progress bar, terminate async
                mProgressBar.setProgress(0, true);
                mScoreBar.setProgress(0, true);

                lrcView.init();

                restartAllPlayers();
            }
        }
        else {
            onBackPressed();
        }

        return true;
    }

    private void initVideoView() {
        videoView = findViewById(R.id.video_view);
        String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.attention;
        Uri uri = Uri.parse(videoPath);
        videoView.setVideoURI(uri);
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            public void onPrepared(MediaPlayer mp) {
                duration = videoView.getDuration();
                mp.setVolume(0, 0);
            }
        });
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbarAccompanySing);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
    }

    @Override
    protected void onStop() {
        super.onStop();
        System.out.println("OnStop");
        if (this.state == State.PLAYING) {
            pauseAllPlayers();
            pauseRecording();
        } else if (this.state == State.UNSTARTED) {
            lrcView.alertPlayerReleased();
            videoView.stopPlayback();
            voiceRecorder.release();
            terminateMediaPlayer(accompanyPlayer);
            handler.removeCallbacks(progressBarUpdater);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        System.out.println("OnDestroy");
        lrcView.alertPlayerReleased();
        videoView.stopPlayback();
        voiceRecorder.release();
        terminateMediaPlayer(accompanyPlayer);
        handler.removeCallbacks(progressBarUpdater);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.sing_menu, menu);
        return true;
    }

    private enum State {
        PAUSE, PLAYING, UNSTARTED
    }

    private enum SingMode {
        WITH, WITHOUT
    }
}
