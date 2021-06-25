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

import org.apache.commons.lang3.StringUtils;
import org.sang.lrcview.LrcView;
import org.sang.lrcview.bean.LrcBean;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.Semaphore;
import java.util.stream.Collectors;

import me.grantland.widget.AutofitTextView;

import static com.dreamfish.record.AudioRecorder.PCM_SPLIT_INTERVAL;
import static com.sjtu.karaoke.singrater.RatingUtil.getScore;
import static com.sjtu.karaoke.singrater.RatingUtil.init;
import static com.sjtu.karaoke.util.Constants.PROGRESS_UPDATE_INTERVAL;
import static com.sjtu.karaoke.util.Constants.RECORD_DELAY_LB;
import static com.sjtu.karaoke.util.Constants.RECORD_DELAY_UB;
import static com.sjtu.karaoke.util.MediaPlayerUtil.loadAudioFileAndPrepareExoPlayer;
import static com.sjtu.karaoke.util.MediaPlayerUtil.terminateExoPlayer;
import static com.sjtu.karaoke.util.MiscUtil.clearTemporaryPcmAndWavFiles;
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
 * @Author: 郭志东
 * @Date: 2021/3/28
 * @Version: v1.3
 * @Description: 伴奏演唱界面。本类中包含了如下功能：
 *                  1. 各个组件的初始化、设置点击事件
 *                  2. 根据传入的歌曲信息，初始化MV播放器、伴奏播放器、原唱播放器、歌词滚动器
 *                  3. 在播放时监控进度、更新进度条、得分条、实时打分
 *                  4. 录音
 */

public class AccompanySingActivity extends AppCompatActivity {
    // 得分提示的显示持续时间
    private static final int ANIMATION_DURATION = 350;
    // 得分提示向上飞的距离
    private static final int MOVE_UP_LENGTH = 70;
    // 得分提示的渐入效果
    private static final AlphaAnimation fadeInAnimation;
    // 得分提示的上移动画
    private static final TranslateAnimation moveUpAnimation;

    // mv视频播放器
    SimpleExoPlayer mvPlayer;
    // 歌词滚动视图
    LrcView lrcView;
    // 伴奏播放器
    SimpleExoPlayer accompanyPlayer;
    // 原唱播放器
    SimpleExoPlayer originalPlayer;
    // 进度条
    ProgressBar progressBar;
    // 得分条
    ProgressBar scoreBar;
    // 播放/暂停按钮
    FloatingActionButton fab;
    // 录音器
    AudioRecorder voiceRecorder;
    // 底部导航栏
    BottomNavigationView bottomNavigationView;
    // 得分显示文本框
    AutofitTextView scoreLabel;

    // 保护分数显示的互斥锁，防止用户在点击重试/完成后分数还在显示
    Semaphore mutex = new Semaphore(1);

    // 使用handler而非自线程来进行各种监听，因为需要在主线程中获取播放器的进度
    Runnable progressMonitor;
    Runnable recordMonitor;
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
    // 有/无原唱
    SingMode singMode;
    // 每句话的歌词
    List<LrcBean> lrcs;
    // 打分线程
    List<Thread> ratingThread;
    // 当前歌词迭代器
    ListIterator<LrcBean> lrcIterator;
    // 当前歌词
    LrcBean currentLrc;
    // 得分
    Score score;
    // 当前播放进度
    int currentPosition;

    // 初始化分数动画
    static {
        fadeInAnimation = new AlphaAnimation(0.5f, 0.8f);
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

        // 进行UI相关，与播放无关的初始化
        initSongName();
        initToolbar();
        initState();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onStart() {
        super.onStart();

        // 在重唱或从其他界面跳转过来时需要重新进行所有和播放相关的初始化，如果是用户退出APP再重新进入什么都不需要做
        if (state == State.UNSTARTED) {
            LoadingDialog loadingDialog = showLoadingDialog(
                    this,
                    getString(R.string.initialize_hint),
                    true
            );
            loadingDialog.setCancelable(false);

            currentPosition = 0;
            new Thread(() -> {

                // 初始化伴奏和原唱播放器
                accompanyPlayer = new SimpleExoPlayer.Builder(this).build();
                loadAudioFileAndPrepareExoPlayer(this, accompanyPlayer, getAccompanyFullPath(songName));
                originalPlayer = new SimpleExoPlayer.Builder(this).build();
                loadAudioFileAndPrepareExoPlayer(this, originalPlayer, getOriginalFullPath(songName));
                loadingDialog.setProgress(20);

                // 初始化进度监听、录音监听和完成监听
                nextPcmSplitTime = PCM_SPLIT_INTERVAL;
                initProgressMonitor(originalPlayer);
                initRecordMonitor();
                initOnCompleteListener(originalPlayer);
                loadingDialog.setProgress(40);

                // 初始化打分系统
                initRatingSystem();
                loadingDialog.setProgress(60);

                // 初始化mv播放器和歌词滚动空间
                initMVPlayer();
                initLrcView();
                loadingDialog.setProgress(80);

                // 如果用户点击重试，需要清除上一次产生的临时文件，初始化录音、进度条、分数条和标签
                clearTemporaryPcmAndWavFiles();
                initVoiceRecorder();
                initProgressBar();
                initScore();
                initScoreBarAndLabel();
                loadingDialog.setProgress(90);

                // 初始化播放、暂停按钮、播放状态、底部导航栏、伴奏/原唱切换键，并默认切换到伴奏模式
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
                stopActivity(false);
                onStart();
            }
        } else {
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
        // 在播放时如果用户退出app，暂停播放和录音
        if (this.state == State.PLAYING) {
            pause();
        }
    }

    /*
     * onDestroy被调用的时机不确定，但是LrcView和ExoPlayer都可以确保在被反复释放时不报错，VoiceRecorder增加了
     * 状态检查
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (voiceRecorder.getStatus() == AudioRecorder.Status.STATUS_PAUSE ||
            voiceRecorder.getStatus() == AudioRecorder.Status.STATUS_START) {
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
        ratingThread = new ArrayList<>();
        init(getRateFullPath(songName), PCM_SPLIT_INTERVAL, RECORD_DELAY_LB, RECORD_DELAY_UB);
    }

    private void initMVPlayer() {
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

    private void initScoreBarAndLabel() {
        int numOfLinesToRate = 0;
        scoreBar = findViewById(R.id.scoreBar);

        for (LrcBean lrc : lrcs) {
            if (lrc.shouldRate()) {
                ++numOfLinesToRate;
            }
        }
        scoreBar.setMax(numOfLinesToRate * 100);
        scoreBar.setProgress(0);
        Drawable draw = ContextCompat.getDrawable(this, R.drawable.custom_scorebar);
        scoreBar.setProgressDrawable(draw);

        scoreLabel = findViewById(R.id.singingScore);
        this.runOnUiThread(() -> {
            scoreLabel.setMaxLines(1);
            scoreLabel.setText(formatScore(0));
        });
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
     * 静音原唱，打开伴奏音量
     */
    private void muteOriginal() {
        this.runOnUiThread(() -> {
            accompanyPlayer.setVolume(1);
            originalPlayer.setVolume(0);
        });

    }

    /**
     * 静音伴奏，打开原唱音量
     */
    private void unmuteOriginal() {
        originalPlayer.setVolume(1);
        accompanyPlayer.setVolume(0);
    }

    private void initProgressMonitor(SimpleExoPlayer player) {
        progressMonitor = new Runnable() {
            @Override
            public void run() {
                currentPosition = (int) player.getContentPosition();
                progressBar.setProgress(currentPosition);

                handler.postDelayed(this, PROGRESS_UPDATE_INTERVAL);
            }
        };
    }

    private void initRecordMonitor() {
        // 由监听器来"提醒"录音器需要切分新的pcm，因为录音器的写文件在自子线程中进行，无法直接获取播放器的进度
        recordMonitor = new Runnable() {
            @Override
            public void run() {
                if (currentPosition >= nextPcmSplitTime) {
                    voiceRecorder.setLastPcmStartTime(nextPcmSplitTime - PCM_SPLIT_INTERVAL);
                    if (lrcIterator.hasNext() && currentPosition > currentLrc.getEnd()) {
                        if (currentLrc.shouldRate()) {
                            rate((int) currentLrc.getStart(), (int) currentLrc.getEnd());
                        }
                        currentLrc = lrcIterator.next();
                    }
                    voiceRecorder.setShouldStartNewPcm(true);

                    nextPcmSplitTime += PCM_SPLIT_INTERVAL;
                }
                // 每50ms检查一次是否需要切分新的pcm
                handler.postDelayed(this, 50);
            }
        };
    }

    private void  initBottomNavbar() {
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        this.runOnUiThread(() -> {
            // 如果不设置，底部导航栏会有奇怪的阴影
            bottomNavigationView.setBackground(null);
            // 默认伴唱模式
            bottomNavigationView.getMenu().getItem(0).setTitle("原唱");
            // 禁用中间的占位item
            bottomNavigationView.getMenu().getItem(1).setEnabled(false);
            // 禁用完成按钮，因为录音还没有开始
            disableFinishButton();
            bottomNavigationView.setOnNavigationItemSelectedListener(
                    item -> {
                        switch (item.getItemId()) {
                            case R.id.singingMode:
                                if (singMode == SingMode.WITH_ORIGINAL) {
                                    item.setTitle("原唱");
                                    withOriginalMode();
                                } else {
                                    item.setTitle("伴唱");
                                    withoutOriginalMode();
                                }
                                break;
                            case R.id.singingFinish:
                                // 防止用户狂点完成按钮
                                disableFinishButton();
                                LoadingDialog loadingDialog = showLoadingDialog(
                                        AccompanySingActivity.this,
                                        getString(R.string.process_record_hint));
                                loadingDialog.setCancelable(false);

                                score.computeFinalScore();

                                new Thread(() -> {
                                    stopActivity(true);
                                    Intent intent = new Intent(getApplicationContext(),
                                            SingResultActivity.class);
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

    /**
     * 将整个activity设为播放状态，包括设置暂停图标、设置状态变量、开始监听、开始播放音频、开始录音
     */
    private void start() {
        fab.setImageResource(R.drawable.ic_pause);
        state = State.PLAYING;

        handler.postDelayed(progressMonitor, 0);
        handler.postDelayed(recordMonitor, 0);

        startRecording();
        startAllPlayers();
    }

    /**
     * 所有播放器都开始播放
     */
    private void startAllPlayers() {
        originalPlayer.play();
        accompanyPlayer.play();
        mvPlayer.play();
    }

    /**
     * 将整个activity设为暂停状态，包括设置暂停图标、设置状态变量、暂停监听、暂停播放音频、暂停录音
     */
    private void pause() {
        fab.setImageResource(R.drawable.ic_fab_play);
        state = State.PAUSE;
        handler.removeCallbacks(progressMonitor);
        handler.removeCallbacks(recordMonitor);

        pauseAllPlayers();
        voiceRecorder.pauseRecord();
    }

    /**
     * 所有播放器都暂停
     */
    private void pauseAllPlayers() {
        accompanyPlayer.pause();
        originalPlayer.pause();
        mvPlayer.pause();
    }

    /**
     * 进行各种资源的释放，包括设置状态为未开始、清除打分提示、停止监听、释放播放器、释放歌词滚动空间、释放录音器
     * @param shouldMergePcm 是否需要合成用户的录音
     */
    private void stopActivity(boolean shouldMergePcm) {
        this.state = State.UNSTARTED;

        // 删除所有分数提示标签
        this.runOnUiThread(() -> {
            mutex.acquireUninterruptibly();
            RelativeLayout track = findViewById(R.id.scoreTrack);
            track.removeAllViews();
            mutex.release();
        });

        // 删除监听器
        handler.removeCallbacks(progressMonitor);
        handler.removeCallbacks(recordMonitor);

        // 释放所有播放器
        terminateExoPlayer(this, mvPlayer);
        terminateExoPlayer(this, accompanyPlayer);
        terminateExoPlayer(this, originalPlayer);

        // 停止录音
        voiceRecorder.stopRecord(shouldMergePcm);

        // 释放歌词滚动控件
        lrcView.alertPlayerReleased();

        // 等待所有打分结束
        for (Thread thread: ratingThread) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 默认禁用完成按钮，否则没有文件提供给SingResultActivity
     */
    private void disableFinishButton() {
        bottomNavigationView.getMenu().getItem(2).setEnabled(false);
    }

    /**
     * 对指定时间段内的用户录音进行打分
     * @param startTime 开始时间（毫秒）
     * @param endTime   结束时间（毫秒）
     */
    private void rate(int startTime, int endTime) {
        Thread thread = new Thread(() -> {
            // 等待区间内的所有小片段基频分析完成，为了避免spin需要在loop中sleep，但是ScheduledExecutorService
            // 是更好的实现方法，但由于开发进度问题只能在该版本中使用sleep
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

            AccompanySingActivity.this.runOnUiThread(() -> {
                        scoreLabel.setText(formatScore(score.getTotalScore()));
                        scoreBar.setProgress(score.getTotalScore(), true);
                    }
            );

            // 用互斥锁确保用户点击重唱后不会还有分数继续显示出来
            mutex.acquireUninterruptibly();
            if (state != State.UNSTARTED) {
                displayScore(scores[0]);
            }
            mutex.release();
        });

        ratingThread.add(thread);
        thread.start();
    }

    /**
     * 在用户唱完一句后显示该句的得分，并增加总分
     * @param score 该句得分
     */
    private void displayScore(Integer score) {
        this.runOnUiThread(() -> {
            RelativeLayout track = findViewById(R.id.scoreTrack);

            // 设置分数标签的内容
            TextView textView = new TextView(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            textView.setText("+" + score);
            textView.setTextColor(ResourcesCompat.getColor(Karaoke.getRes(), R.color.score_text, null));
            textView.setGravity(Gravity.CENTER);
            textView.setLayoutParams(params);
            track.addView(textView);

            // 设置分数标签的渐入、上升和爆炸动画
            AnimationSet animationSet = new AnimationSet(true);
            animationSet.setDuration(ANIMATION_DURATION);
            animationSet.addAnimation(fadeInAnimation);
            animationSet.addAnimation(moveUpAnimation);
            animationSet.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    ExplosionField explosionField = new ExplosionField(AccompanySingActivity.this, new FlyawayFactory());
                    RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) textView.getLayoutParams();
                    layoutParams.bottomMargin = MOVE_UP_LENGTH;
                    textView.clearAnimation();
                    textView.setLayoutParams(layoutParams);

                    // 在上升到顶部后700ms爆炸
                    handler.postDelayed(() -> {
                        if (track.getParent() != null) {
                            explosionField.explode(textView);
                        }
                    }, 700);

                    // 在上升到顶部2s后删除TextView，防止界面中堆积太多控件
                    handler.postDelayed(() -> {
                        if (track.getParent() != null) {
                            track.removeView(textView);
                        }
                    }, 2000);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });

            textView.startAnimation(animationSet);
        });
    }

    private String formatScore(Integer score) {
        return StringUtils.leftPad(score.toString(), 4) + "分";
    }

    private enum State {
        PAUSE, PLAYING, UNSTARTED
    }

    private enum SingMode {
        WITH_ORIGINAL, WITHOUT_ORIGINAL
    }
}
