package com.sjtu.karaoke;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
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

import org.apache.commons.io.FilenameUtils;
import org.sang.lrcview.LrcView;
import org.sang.lrcview.bean.LrcBean;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.ListIterator;
import java.util.stream.Collectors;

import static com.dreamfish.record.PcmToWav.makePCMFileToWAVFile;
import static com.sjtu.karaoke.util.Constants.PROGRESS_UPDATE_INTERVAL;
import static com.sjtu.karaoke.util.Constants.TRIMMED_VOICE_WAV_DIRECTORY;
import static com.sjtu.karaoke.util.MediaPlayerUtil.loadFileAndPrepareMediaPlayer;
import static com.sjtu.karaoke.util.MediaPlayerUtil.terminateMediaPlayer;
import static com.sjtu.karaoke.util.MiscUtil.getAccompanyFullPath;
import static com.sjtu.karaoke.util.MiscUtil.getLyricFullPath;
import static com.sjtu.karaoke.util.MiscUtil.getMVFullPath;
import static com.sjtu.karaoke.util.MiscUtil.getOriginalFullPath;
import static com.sjtu.karaoke.util.MiscUtil.getScore;
import static com.sjtu.karaoke.util.MiscUtil.verifyRecorderPermissions;

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
    VideoView videoView;
    LrcView lrcView;
    MediaPlayer accompanyPlayer;
    MediaPlayer originalPlayer;
    ProgressBar progressBar, scoreBar;
    FloatingActionButton fab;
    Runnable progressBarUpdater;
    AudioRecorder voiceRecorder;
    BottomNavigationView bottomNavigationView;
    Handler handler = new Handler();

    // 状态量
    // 当前歌的id
    Integer id;
    // 当前歌名
    String songName;
    // 歌曲的持续时间，mv和所有伴奏的声音时长一样
    Integer duration;
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

    private void initSongName() {
        Intent intent = getIntent();
        id = intent.getIntExtra("id", 0);
        songName = intent.getStringExtra("songName");
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onStart() {
        super.onStart();
        // return from sing result activity or from main activity, initialize all players
        if (state == State.UNSTARTED) {
            // Player-related initialization
            accompanyPlayer = new MediaPlayer();
            loadFileAndPrepareMediaPlayer(accompanyPlayer, getAccompanyFullPath(songName));
            originalPlayer = new MediaPlayer();
            loadFileAndPrepareMediaPlayer(originalPlayer, getOriginalFullPath(songName));
            muteOriginal();

            initVideoView();
            initLrcView();
            initVoiceRecorder();
            initProgressBar();
            initScoreBar();
            initFab();
            initState();
            initProgressBarUpdater();
            initBottomNavbar();
        }
    }

    private void muteOriginal() {
        originalPlayer.setVolume(0, 0);
    }

    private void unmuteOriginal() {
        originalPlayer.setVolume(1, 1);
    }

    private void initVoiceRecorder() {
        voiceRecorder = AudioRecorder.getInstance();

        voiceRecorder.createDefaultAudio(songName);
    }

    private void initProgressBarUpdater() {
        progressBarUpdater = new Runnable() {
            @Override
            public void run() {
                progressBar.setProgress(accompanyPlayer.getCurrentPosition());
                handler.postDelayed(this, PROGRESS_UPDATE_INTERVAL);
            }
        };
    }

    private void initState() {
        state = State.UNSTARTED;
        singMode = SingMode.WITHOUT;
    }

    private void initFab() {
        fab = findViewById(R.id.fabPlayPause);
        fab.setImageResource(R.drawable.ic_fab_play);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (state == State.PLAYING) {
                    pause();
                } else if (state == State.PAUSE) {
                    startAllPlayers();
                    startRecording();
                } else {
                    startAllPlayers();
                    startRecording();
                    bottomNavigationView.getMenu().getItem(2).setEnabled(true);
                }
            }
        });
    }

    private void enableFinishButton() {
        MenuItem finishButton = bottomNavigationView.getMenu().getItem(2);
        finishButton.setEnabled(true);
    }

    private void pauseRecording() {
        voiceRecorder.pauseRecord();
    }

    private void startRecording() {
        voiceRecorder.startRecord(null, accompanyPlayer);
    }

    private void startUpdateProgressBar() {
        handler.postDelayed(progressBarUpdater, 0);
    }

    private void stopUpdateProgressBar() {
        handler.removeCallbacks(progressBarUpdater);
    }

    private void startAllPlayers() {
        originalPlayer.start();
        accompanyPlayer.start();
        videoView.start();
        fab.setImageResource(R.drawable.ic_pause);
        state = State.PLAYING;

        accompanyPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

            @Override
            public void onCompletion(MediaPlayer accompanyPlayer) {
                // todo: also pass score
                Intent intent = new Intent(getApplicationContext(), SingResultActivity.class);
                intent.putExtra("id", id);
                intent.putExtra("songName", songName);
                startActivity(intent);
            }
        });

        startSplittingLines();

        startUpdateProgressBar();
    }

    private void startSplittingLines() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (state == State.PLAYING && lrcIterator.hasNext()) {
                    if (accompanyPlayer.getCurrentPosition() > currentLrc.getEnd()) {
                        voiceRecorder.setShouldStartNewLine(true);
                        // wait for the recorder to finish writing current file
                        while (voiceRecorder.shouldStartNewLine()) { }

                        if (currentLrc.shouldRate()) {
                            rate(voiceRecorder.getLastFinishedPCMFilePath());
                        }
                        currentLrc = lrcIterator.next();
                    }
                }
            }
        }).start();
    }

    private void pauseAllPlayers() {
        accompanyPlayer.pause();
        originalPlayer.pause();
        videoView.pause();
        fab.setImageResource(R.drawable.ic_fab_play);
        state = State.PAUSE;
        handler.removeCallbacks(progressBarUpdater);
    }

    private void initScoreBar() {
        scoreBar = (ProgressBar) findViewById(R.id.scoreBar);
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

    private void initProgressBar() {
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setMax(accompanyPlayer.getDuration());
        progressBar.setProgress(0);
    }

    private void initBottomNavbar() {
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setBackground(null);
        // 禁用中间的占位item
        bottomNavigationView.getMenu().getItem(1).setEnabled(false);
        // 禁用完成，因为录音还没有开始
        disableFinishButton();

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
                                voiceRecorder.stopRecord();
                                Intent intent = new Intent(getApplicationContext(), SingResultActivity.class);
                                intent.putExtra("id", id);
                                intent.putExtra("songName", songName);
                                startActivity(intent);
                                break;
                        }
                        return false;
                    }
                }
        );
    }

    private void disableFinishButton() {
        bottomNavigationView.getMenu().getItem(2).setEnabled(false);
    }

    private void restartAllPlayers() {
        accompanyPlayer.seekTo(0);
        accompanyPlayer.pause();
        originalPlayer.seekTo(0);
        originalPlayer.pause();
        videoView.pause();
        videoView.seekTo(0);
        fab.setImageResource(R.drawable.ic_fab_play);
        stopUpdateProgressBar();
        state = State.UNSTARTED;
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

            lrcView.setPlayer(accompanyPlayer);
            lrcView.init();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.retry) {
            if (state != State.UNSTARTED) {
                // todo: clear record, reset score, progress bar, terminate async
                progressBar.setProgress(0, true);
                scoreBar.setProgress(0, true);

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
        String mvFullPath = getMVFullPath(songName);
        videoView.setVideoPath(mvFullPath);
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            public void onPrepared(MediaPlayer mp) {
                duration = videoView.getDuration();
                mp.setVolume(0, 0);
            }
        });
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbarAccompanySing);
        TextView title = (TextView) findViewById(R.id.toolbarAccompanySingTitle);
        title.setText(songName);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
    }

    @Override
    protected void onStop() {
        super.onStop();
        System.out.println("AccompanySing OnStop");
        // 在播放时退出app，暂停播放和录音
        if (this.state == State.PLAYING) {
            // 在播放时退出app
            pause();
        } else if (this.state == State.UNSTARTED) {
            // 完成键只有在开始录音后，state变为非UNSTARTE才可以点，所以能进入这里必然是录音结束
            lrcView.alertPlayerReleased();
            handler.removeCallbacks(progressBarUpdater);
            videoView.stopPlayback();
            terminateMediaPlayer(accompanyPlayer);
            terminateMediaPlayer(originalPlayer);
        }
    }

    private void pause() {
        // change of state is done in pauseAllPlayers
        pauseAllPlayers();
        pauseRecording();
    }

    /*
     * onDestroy() is not called when hitting finish or close the app
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        System.out.println("AccompanySing OnDestroy");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.sing_menu, menu);
        return true;
    }

    /**
     *
     * @param pcmFullPath Full path to the pcm file of the recording
     */
    public void rate(String pcmFullPath) {
        String trimmedVoiceFullPath = getTrimmedVoicePath(pcmFullPath);
        makePCMFileToWAVFile(pcmFullPath, trimmedVoiceFullPath, false);
        int score = getScore(trimmedVoiceFullPath);
        scoreBar.setProgress(scoreBar.getProgress() + score, true);
    }

    private String getTrimmedVoicePath(String pcmFullPath) {
        // pcm path: .../Karaoke/pcm/<fileName>.pcm
        String fileName = FilenameUtils.getBaseName(pcmFullPath);
        return TRIMMED_VOICE_WAV_DIRECTORY + fileName + ".wav";
    }

    private enum State {
        PAUSE, PLAYING, UNSTARTED
    }

    private enum SingMode {
        WITH, WITHOUT
    }
}
