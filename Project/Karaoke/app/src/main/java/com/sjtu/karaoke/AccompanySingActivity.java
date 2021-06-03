package com.sjtu.karaoke;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import com.dreamfish.record.AudioRecorder;
import com.example.administrator.myapplication.ExplosionField;
import com.example.administrator.myapplication.factory.FlyawayFactory;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.sjtu.karaoke.component.LoadingDialog;
import com.sjtu.karaoke.data.Score;

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
import static com.sjtu.karaoke.util.MiscUtil.parseScore;
import static com.sjtu.karaoke.util.MiscUtil.showLoadingDialog;
import static com.sjtu.karaoke.util.PathUtil.getAccompanyFullPath;
import static com.sjtu.karaoke.util.PathUtil.getAccompanyLyricFullPath;
import static com.sjtu.karaoke.util.PathUtil.getMVFullPath;
import static com.sjtu.karaoke.util.PathUtil.getOriginalFullPath;
import static com.sjtu.karaoke.util.PathUtil.getRateFullPath;
import static com.sjtu.karaoke.util.WavUtil.getWAVDuration;
/*
 * @ClassName: AccompanySingActivity
 * @Author: guozh
 * @Date: 2021/3/28
 * @Version: v1.2
 * @Description: 伴奏演唱界面。本类中包含了如下功能：
 *                  1. 各个组件的初始化、设置点击事件
 *                  2. 根据传入的歌曲信息，初始化MV播放器、伴奏播放器、原唱播放器、歌词滚动器
 *                  3. 在播放时监控进度、更4新进度条、得分条
 *                  4. 录音
 */

public class AccompanySingActivity extends AppCompatActivity {
    private static final int ANIMATION_DURATION = 1000;
    private static final int MOVE_UP_LENGTH = 100;
    private static final AlphaAnimation fadeInAnimation;
    private static final TranslateAnimation moveUpAnimation;

    SimpleExoPlayer mvPlayer;
    LrcView lrcView;
    SimpleExoPlayer accompanyPlayer;
    SimpleExoPlayer originalPlayer;
    ProgressBar progressBar;
    ProgressBar scoreBar;
    FloatingActionButton fab;
    AudioRecorder voiceRecorder;
    BottomNavigationView bottomNavigationView;
    Handler handler = new Handler();

    Runnable progressMonitor;
    Runnable recordMonitor;

    // 状态量
    // 当前歌的id
    Integer id;
    // 当前歌名
    String songName;
    // 下一次切分pcm的时间
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
    Score score;
    // 当前播放进度
    int currentPosition;

    static {
        fadeInAnimation = new AlphaAnimation(0, 1);
        fadeInAnimation.setDuration(ANIMATION_DURATION);

        moveUpAnimation = new TranslateAnimation(0, 0, 0, -MOVE_UP_LENGTH);
        moveUpAnimation.setDuration(ANIMATION_DURATION);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accompany_sing);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

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
            LoadingDialog loadingDialog = showLoadingDialog(this, "正在初始化", true);

            new Thread(() -> {
                // Player-related initialization
                accompanyPlayer = new SimpleExoPlayer.Builder(this).build();
                loadAudioFileAndPrepareExoPlayer(this, accompanyPlayer, getAccompanyFullPath(songName));
                originalPlayer = new SimpleExoPlayer.Builder(this).build();
                loadAudioFileAndPrepareExoPlayer(this, originalPlayer, getOriginalFullPath(songName));

                loadingDialog.setProgress(20);

                nextPcmSplitTime = PCM_SPLIT_INTERVAL;

                initProgressMonitor(originalPlayer);
                initRecordMonitor();

                loadingDialog.setProgress(40);


                initOnCompleteListener(originalPlayer);
                initRatingSystem();

                loadingDialog.setProgress(60);

                initMVPlayer();
                initLrcView();

                loadingDialog.setProgress(80);

                initVoiceRecorder();
                initProgressBar();
                initScore();
                initScoreBar();

                loadingDialog.setProgress(90);

                initFab();
                initState();
                initBottomNavbar();

                muteOriginal();

                loadingDialog.dismiss();
            }).start();
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
//                item.setEnabled(false);
                stopActivity(false);
                onStart();
            }
        }
        else {
            item.setEnabled(false);
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
        if (voiceRecorder.getStatus() == AudioRecorder.Status.STATUS_PAUSE || voiceRecorder.getStatus() == AudioRecorder.Status.STATUS_START) {
            voiceRecorder.stopRecord(false);
        }
        lrcView.alertPlayerReleased();
        terminateExoPlayer(this, mvPlayer);
        terminateExoPlayer(this, accompanyPlayer);
        terminateExoPlayer(this, originalPlayer);
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
        singMode = SingMode.WITHOUT_ORIGINAL;
    }

    private void initOnCompleteListener(SimpleExoPlayer player) {
        player.addListener(new Player.EventListener() {
            @Override
            public void onPlaybackStateChanged(int state) {
                if (state == Player.STATE_ENDED) {
                    bottomNavigationView.setSelectedItemId(R.id.singingFinish);
                }
            }
        });
    }

    private void initRatingSystem() {
        init(getRateFullPath(songName), PCM_SPLIT_INTERVAL, RECORD_DELAY_LB, RECORD_DELAY_UB);
    }

    private void initMVPlayer() {
        // set up mv player and the view it should attach to
        this.runOnUiThread(() -> {
            mvPlayer = new SimpleExoPlayer.Builder(AccompanySingActivity.this).build();
            File mvFile = new File(getMVFullPath(songName));
            MediaItem mv = MediaItem.fromUri(Uri.fromFile(mvFile));
            mvPlayer.setMediaItem(mv);
            mvPlayer.prepare();
            mvPlayer.setVolume(0);

            PlayerView mvPlayerView = findViewById(R.id.mvView);
            mvPlayerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FILL);
            mvPlayerView.setPlayer(mvPlayer);
            mvPlayerView.setUseController(false);
        });

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void initLrcView() {
        lrcView = findViewById(R.id.lrcRoller);
        lrcView.setHighLineColor(ContextCompat.getColor(getApplicationContext(), R.color.purple_500));
        try {
            InputStream is = new FileInputStream(getAccompanyLyricFullPath(songName));

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
        voiceRecorder.createDefaultAudio(songName, 0);
    }

    private void initProgressBar() {
        progressBar = findViewById(R.id.progressBar);
        progressBar.setMax((int) getWAVDuration(getOriginalFullPath(songName)));
        progressBar.setProgress(0);
    }

    private void initScore() {
        score = new Score();
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
        Drawable draw = ContextCompat.getDrawable(this, R.drawable.custom_scorebar);
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

    /*
     * Mute original, unmute accompany
     */
    private void muteOriginal() {
        this.runOnUiThread(() -> {
            accompanyPlayer.setVolume(1);
            originalPlayer.setVolume(0);
        });

    }

    /**
     * Mute accompany, unmute original
     */
    private void unmuteOriginal() {
        originalPlayer.setVolume(1);
        accompanyPlayer.setVolume(0);
    }

    private void initProgressMonitor(SimpleExoPlayer player) {
        progressMonitor = new Runnable() {
            @Override
            public void run() {
                // update progress bar
                currentPosition = (int) player.getContentPosition();
                progressBar.setProgress(currentPosition);

                handler.postDelayed(this, PROGRESS_UPDATE_INTERVAL);
            }
        };
    }

    private void initRecordMonitor() {
        recordMonitor = new Runnable() {
            @Override
            public void run() {
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
                handler.postDelayed(this, 50);
            }
        };
    }

    private void initBottomNavbar() {
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        this.runOnUiThread(() -> {
            bottomNavigationView.setBackground(null);
            // 禁用中间的占位item
            bottomNavigationView.getMenu().getItem(0).setTitle("伴唱");
            bottomNavigationView.getMenu().getItem(1).setEnabled(false);
            // 禁用完成，因为录音还没有开始
            disableFinishButton();

            bottomNavigationView.setOnNavigationItemSelectedListener(
                    item -> {
                        switch (item.getItemId()) {
                            case R.id.singingMode:
                                if (singMode == SingMode.WITH_ORIGINAL) {
                                    item.setTitle("伴唱");
                                    withOriginalMode();
                                } else {
                                    item.setTitle("原唱");
                                    withoutOriginalMode();
                                }
                                break;
                            case R.id.singingFinish:
                                // it has to be placed here, to wait for the merging to complete
                                disableFinishButton();
                                LoadingDialog loadingDialog = showLoadingDialog(AccompanySingActivity.this, "正在处理录音");

                                score.computeFinalScore();
                                new Thread(() -> {
                                    stopActivity(true);
                                    Intent intent = new Intent(getApplicationContext(), SingResultActivity.class);
                                    intent.putExtra("id", id);
                                    intent.putExtra("score", score);
                                    intent.putExtra("songName", songName);
                                    startActivityForResult(intent, 0);
                                    loadingDialog.dismiss();
                                }).start();

                                break;
                        }
                        return false;
                    }
            );
        });

    }

    private void withOriginalMode() {
        singMode = SingMode.WITHOUT_ORIGINAL;
        muteOriginal();
    }

    private void withoutOriginalMode() {
        singMode = SingMode.WITH_ORIGINAL;
        unmuteOriginal();
    }

    private void startRecording() {
        voiceRecorder.startRecord(null);
    }

    private void start() {
        fab.setImageResource(R.drawable.ic_pause);
        state = State.PLAYING;

        handler.postDelayed(progressMonitor, 0);
        handler.postDelayed(recordMonitor, 0);

        startRecording();
        startAllPlayers();
    }

    private void startAllPlayers() {
        originalPlayer.play();
        accompanyPlayer.play();
        mvPlayer.play();
    }

    private void pause() {
        // change of state is done in pauseAllPlayers
        fab.setImageResource(R.drawable.ic_fab_play);
        state = State.PAUSE;
        handler.removeCallbacks(progressMonitor);
        handler.removeCallbacks(recordMonitor);

        pauseAllPlayers();
        voiceRecorder.pauseRecord();
    }

    private void pauseAllPlayers() {
        accompanyPlayer.pause();
        originalPlayer.pause();
        mvPlayer.pause();
    }

    /**
     *
     * @param shouldMergePcm
     */
    private void stopActivity(boolean shouldMergePcm) {
        this.state = State.UNSTARTED;

        handler.removeCallbacks(progressMonitor);
        handler.removeCallbacks(recordMonitor);

        terminateExoPlayer(this, mvPlayer);
        terminateExoPlayer(this, accompanyPlayer);
        terminateExoPlayer(this, originalPlayer);

        voiceRecorder.stopRecord(shouldMergePcm);
        lrcView.alertPlayerReleased();
    }

    private void disableFinishButton() {
        bottomNavigationView.getMenu().getItem(2).setEnabled(false);
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

            AccompanySingActivity.this.runOnUiThread(() -> scoreBar.setProgress(score.getTotalScore(), true));

            displayScore(scores[0]);
        }).start();
    }

    private void displayScore(Integer score) {
        this.runOnUiThread(() -> {
            RelativeLayout track = findViewById(R.id.scoreTrack);

            TextView textView = new TextView(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);

            textView.setText("+" + score);
            textView.setTextColor(ResourcesCompat.getColor(Karaoke.getRes(), R.color.score_text, null));
            textView.setGravity(Gravity.CENTER);
            textView.setLayoutParams(params);

            // display textview
            track.addView(textView);

            AnimationSet animationSet = new AnimationSet(true);
            animationSet.setDuration(ANIMATION_DURATION);
            animationSet.addAnimation(fadeInAnimation);
            animationSet.addAnimation(moveUpAnimation);

            animationSet.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) { }

                @Override
                public void onAnimationEnd(Animation animation) {
                    ExplosionField explosionField = new ExplosionField(AccompanySingActivity.this, new FlyawayFactory());
                    RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) textView.getLayoutParams();
                    layoutParams.bottomMargin = MOVE_UP_LENGTH;
                    textView.setLayoutParams(layoutParams);

                    // explodes after 1.5s
                    handler.postDelayed(() -> {
                        explosionField.explode(textView);
                    }, 1500);

                    // removes textview after 2s
                    handler.postDelayed(() -> {
                        track.removeView(textView);
                    }, 2000);
                }

                @Override
                public void onAnimationRepeat(Animation animation) { }
            });

            textView.startAnimation(animationSet);
        });
    }

    private enum State {
        PAUSE, PLAYING, UNSTARTED
    }

    private enum SingMode {
        WITH_ORIGINAL, WITHOUT_ORIGINAL
    }
}
