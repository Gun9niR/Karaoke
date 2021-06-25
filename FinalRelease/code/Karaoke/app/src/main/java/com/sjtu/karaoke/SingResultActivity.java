package com.sjtu.karaoke;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.FileProvider;

import com.google.android.exoplayer2.Player;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.sjtu.karaoke.component.LoadingDialog;
import com.sjtu.karaoke.component.RateResultDialog;
import com.sjtu.karaoke.data.Score;
import com.sjtu.karaoke.util.AccompanyPlayerGroup;
import com.sjtu.karaoke.util.ExoPlayerGroup;
import com.sjtu.karaoke.util.InstrumentPlayerGroup;

import org.apache.commons.io.FileUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.sjtu.karaoke.util.Constants.AUTHORITY;
import static com.sjtu.karaoke.util.Constants.PROGRESS_UPDATE_INTERVAL;
import static com.sjtu.karaoke.util.Constants.RECORD_DIRECTORY;
import static com.sjtu.karaoke.util.FileUtil.deleteOneFile;
import static com.sjtu.karaoke.util.MiscUtil.getChooserIntent;
import static com.sjtu.karaoke.util.MiscUtil.setImageFromFile;
import static com.sjtu.karaoke.util.MiscUtil.showLoadingDialog;
import static com.sjtu.karaoke.util.MiscUtil.showRateResultDialog;
import static com.sjtu.karaoke.util.MiscUtil.showSuccessToast;
import static com.sjtu.karaoke.util.PathUtil.getAlbumCoverFullPath;
import static com.sjtu.karaoke.util.PathUtil.getRecordCoverFullPath;
import static com.sjtu.karaoke.util.PathUtil.getRecordMetadataFullPath;
import static com.sjtu.karaoke.util.PathUtil.getRecordFileFullPath;
import static com.sjtu.karaoke.util.PathUtil.getTrimmedAccompanyFullPath;
import static com.sjtu.karaoke.util.PathUtil.getVoiceFullPath;

/*
 * @ClassName: SingResultActivity
 * @Author: 郭志东
 * @Date: 2021/3/28
 * @Version: v1.3
 * @Description: 演唱结果界面。本类中包括了如下功能：
 *                  1. 各个组件的初始化、设置点击事件
 *                  2. 根据传入的歌曲信息、用户录音，初始化伴奏播放器、录音播放器
 *                  3. 在播放时监控进度、更新进度条
 *                  4. 在用户拖动音量条时修改录音或伴奏的音量
 */

public class SingResultActivity extends AppCompatActivity {
    // 初始的录音提前量
    final int INITIAL_OFFSET = 300;

    // 演唱模式
    From callingActivity;
    // 演唱结果播放进度条
    SeekBar seekBarResultProgress;
    // 播放器组
    ExoPlayerGroup playerGroup;

    // 顶部工具栏
    Toolbar toolbar;
    // 底部导航栏
    BottomNavigationView bottomNavbarResult;
    // 歌曲标题、当前播放时间、音频总时长
    TextView titleText, playerPosition, playerDuration;
    // 播放按钮
    ImageView btnPlay;
    // 暂停按钮
    ImageView btnPause;
    // 专辑封面
    ImageView albumCover;
    // 打分结果对话框
    RateResultDialog rateResultDialog;

    // 播放进度监听
    Handler handler = new Handler();
    Runnable progressUpdater;

    // 录音文件是否已经生成并保存
    boolean isFileSaved = false;
    // 当前播放状态
    State state;
    // 歌曲id
    Integer id;
    // 歌曲名称
    String songName;
    // 录音路径
    String recordFullPath;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sing_result);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // 初始化歌曲信息，并根据演唱模式初始化播放器组
        Intent intent = getIntent();
        id = intent.getIntExtra("id", 0);
        songName = intent.getStringExtra("songName");

        callingActivity = getCallingActivity().getShortClassName().equals(".AccompanySingActivity")
                ? From.ACCOMPANY : From.INSTRUMENT;

        initPlayerGroup();

        this.state = State.UNSTARTED;

        // 进行UI相关，与播放无关的初始化
        showScore();
        initOnCompleteListener();
        initAlbumCover();
        initProgressUpdater();
        initToolBar();
        initTitle();
        initBottomNavbar();
        initPlaySeekbar();

        initButtonControl();
        initTuneSeekbar();
        initAlignSeekbar();
        initFab();
    }

    /**
     * 显示得分对话框
     */
    private void showScore() {
        Score score;
        String pianoScore;

        Intent intent = getIntent();
        score = intent.getParcelableExtra("score");
        pianoScore = intent.getStringExtra("pianoScore");
        if (pianoScore == null) {
            pianoScore = "";
        }

        rateResultDialog = showRateResultDialog(this, score, pianoScore);
    }

    /**
     * 初始化播放器组，不同的模式播放器有所不同
     */
    private void initPlayerGroup() {
        if (isFromAccompanySingActivity()) {
            playerGroup = new AccompanyPlayerGroup(this, songName, INITIAL_OFFSET);
        } else {
            playerGroup = new InstrumentPlayerGroup(this, songName, INITIAL_OFFSET);
        }
    }

    /**
     * 在播放完成后不重新播放
     */
    private void initOnCompleteListener() {
        playerGroup.getVoicePlayer().addListener(new Player.EventListener() {
            @Override
            public void onPlaybackStateChanged(int state) {
                if (state == Player.STATE_ENDED) {
                    stop();
                }
            }
        });

    }

    /**
     * 初始化专辑封面
     */
    private void initAlbumCover() {
        albumCover = findViewById(R.id.albumCover);
        setImageFromFile(getAlbumCoverFullPath(songName), albumCover);
    }

    /**
     * 初始化人声对齐拖动条
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void initAlignSeekbar() {
        SeekBar seekbarAlignVoice = findViewById(R.id.seekbarAlignVoice);
        // 录音最多提前1秒
        seekbarAlignVoice.setMin(0);
        seekbarAlignVoice.setMax(1000);
        seekbarAlignVoice.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            private boolean isPreviouslyPlaying;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                playerGroup.setVoiceOffset(progress);
            }

            // 在用户拖动时暂停播放
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                isPreviouslyPlaying = playerGroup.isPlaying();
                pause();
            }

            // 在用户释放拖动条时，如果恢复之前的播放状态
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (isPreviouslyPlaying) {
                    start();
                    playerGroup.setActualOffset();
                }
            }
        });
        seekbarAlignVoice.setProgress(INITIAL_OFFSET);
    }

    /**
     * 初始化保存录音按钮
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void initFab() {
        FloatingActionButton fabSave = findViewById(R.id.fabSave);
        fabSave.setOnClickListener(view -> {
            // 不重复保存录音
            if (isFileSaved) {
                showSuccessToast(
                        SingResultActivity.this,
                        getString(R.string.save_success_hint),
                        350
                );
            } else {
                LoadingDialog loadingDialog = showLoadingDialog(SingResultActivity.this,
                        getString(R.string.save_record_hint));
                loadingDialog.setCancelable(false);
                new Thread(() -> {
                    recordFullPath = saveRecord(songName, playerGroup, rateResultDialog.getRankingText());
                    loadingDialog.dismiss();
                    showSuccessToast(
                            SingResultActivity.this,
                            getString(R.string.save_success_hint),
                            350
                    );
                    isFileSaved = true;
                }).start();
            }
        });
    }

    /**
     * 保存录音
     * @param songName 歌名，写入元数据
     * @param playerGroup 负责合成各个音频文件的播放器组
     * @param rankingText 打分等级文本，写入元数据
     * @return 合成文件的绝对路径
     */
    private String saveRecord(String songName, ExoPlayerGroup playerGroup, String rankingText) {
        // 生成录音目录的UUID
        String dirFullPath = RECORD_DIRECTORY + UUID.randomUUID().
                toString().
                replaceAll("-", "") + "/";
        // 为了在分享时不产生困惑，录音的文件名和歌名相同
        String wavFullPath = dirFullPath + songName + ".wav";

        File dir = new File(dirFullPath);
        dir.mkdirs();

        // 复制当前的专辑封面
        try {
            FileUtils.copyFile(new File(getAlbumCoverFullPath(songName)),
                    new File(getRecordCoverFullPath(dirFullPath)));
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 写入元数据
        try {
             BufferedWriter writer = new BufferedWriter(
                     new FileWriter(getRecordMetadataFullPath(dirFullPath)));
            // 写入歌曲名
            writer.write(songName);
            writer.newLine();

            // 写入录音时间
            Date date = new Date(System.currentTimeMillis());
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HH-mm",
                    Locale.CHINA);
            String dateString = formatter.format(date);
            writer.write(dateString);
            writer.newLine();

            // 写入等级
            writer.write(rankingText);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 合成录音
        playerGroup.mergeWav(getRecordFileFullPath(dirFullPath, songName));

        return wavFullPath;
    }

    /**
     * 初始化进度监听
     */
    private void initProgressUpdater() {
        progressUpdater = new Runnable() {
            @Override
            public void run() {
                seekBarResultProgress.setProgress(playerGroup.getCurrentPosition());
                handler.postDelayed(this, PROGRESS_UPDATE_INTERVAL);
            }
        };
    }

    /**
     * 初始化顶部工具栏
     */
    private void initToolBar() {
        toolbar = findViewById(R.id.toolbarResult);

        setSupportActionBar(toolbar);
        // 不显示默认标题
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
    }

    /**
     * 初始化标题为歌曲名+打分等级，点击标题会弹出得分对话框
     */
    private void initTitle() {
        titleText = findViewById(R.id.toolbarResultTitle);

        Spannable songName = new SpannableString(this.songName);
        songName.setSpan(new ForegroundColorSpan(Color.WHITE), 0, songName.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        titleText.setText(songName);

        Spannable rank = new SpannableString("  " + rateResultDialog.getRankingText());
        rank.setSpan(new ForegroundColorSpan(rateResultDialog.getRankingColor()), 0,
                rank.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        titleText.append(rank);
        titleText.setOnClickListener(v -> {
            rateResultDialog.show();
        });
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    /**
     * 初始化底部导航栏
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void initBottomNavbar() {
        bottomNavbarResult = findViewById(R.id.bottomNavigationViewResult);
        // 清除导航栏的奇怪阴影
        bottomNavbarResult.setBackground(null);
        // 禁用中间的占位item
        bottomNavbarResult.getMenu().getItem(1).setEnabled(false);

        bottomNavbarResult.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                switch (id) {
                    // 重试
                    case R.id.resultRetry:
                        SingResultActivity.super.onBackPressed();
                        break;
                    // 用户点击分享时，先保存录音，再弹出分享界面
                    case R.id.resultShare:
                        if (!isFileSaved) {
                            // 如果录音没有保存，就先保存录音
                            LoadingDialog loadingDialog = showLoadingDialog(
                                    SingResultActivity.this,
                                    getString(R.string.save_record_hint));
                            new Thread(() -> {
                                recordFullPath = saveRecord(
                                        songName,
                                        playerGroup,
                                        rateResultDialog.getRankingText());
                                loadingDialog.dismiss();
                                isFileSaved = true;

                                File recordFile = new File(recordFullPath);
                                Uri uri = FileProvider.getUriForFile(
                                        SingResultActivity.this,
                                        AUTHORITY,
                                        recordFile);

                                Intent chooserIntent = getChooserIntent(
                                        uri,
                                        SingResultActivity.this);
                                startActivity(chooserIntent);
                            }).start();
                        } else {
                            // 如果录音已经保存，直接分享
                            File recordFile = new File(recordFullPath);
                            Uri uri = FileProvider.getUriForFile(
                                    SingResultActivity.this,
                                    AUTHORITY,
                                    recordFile);

                            Intent chooserIntent = getChooserIntent(
                                    uri,
                                    SingResultActivity.this);
                            startActivity(chooserIntent);
                        }
                        break;
                }
                return false;
            }
        });
    }

    /**
     * 初始化播放进度条
     */
    private void initPlaySeekbar() {
        // 设置持续时间文字
        playerPosition = findViewById(R.id.playerPosition);
        playerDuration = findViewById(R.id.playerDuration);
        String sDuration = convertFormat(playerGroup.getDuration());
        playerDuration.setText(sDuration);

        // 设置进度条的拖动事件
        seekBarResultProgress = findViewById(R.id.seekbarResultProgress);
        seekBarResultProgress.setMax(playerGroup.getDuration());
        seekBarResultProgress.setProgress(0);

        seekBarResultProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    playerGroup.seekTo(progress);
                }

                playerPosition.setText(convertFormat(playerGroup.getCurrentPosition()));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });
    }

    /*
     * 初始化暂停和播放按钮，点击播放按钮时隐藏播放按钮，显示暂停按钮，反之亦然
     */
    private void initButtonControl() {
        btnPlay = findViewById(R.id.resultPlay);
        btnPause = findViewById(R.id.resultPause);

        btnPlay.setOnClickListener(v -> start());
        btnPause.setOnClickListener((v -> pause()));

        btnPlay.setVisibility(View.VISIBLE);
        btnPause.setVisibility(View.GONE);
    }

    /**
     * 初始化音量调节拖动条
     */
    private void initTuneSeekbar() {
        // 两种模式都可以调节人声音量
        SeekBar seekbarTuneVoice = findViewById(R.id.seekbarTuneVoice);
        seekbarTuneVoice.setMax(100);
        seekbarTuneVoice.setProgress(100);
        seekbarTuneVoice.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    playerGroup.getVoicePlayer().setVolume((float) progress / 100);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });

        LinearLayout tuneWrapper = findViewById(R.id.tuneWrapper);
        // 如果是伴奏演唱模式，删除钢琴伴奏音量调节，且不显示DIY伴奏
        if (isFromAccompanySingActivity()) {
            ConstraintLayout wrapper = findViewById(R.id.singResultWrapper);

            tuneWrapper.removeView(findViewById(R.id.wrapperTunePiano));
            wrapper.removeView(findViewById(R.id.bottomSheetWrapper));

            SeekBar seekbarTuneAccompany = findViewById(R.id.seekbarTuneAccompany);
            seekbarTuneAccompany.setMax(100);
            seekbarTuneAccompany.setProgress(100);
            seekbarTuneAccompany.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (fromUser) {
                        ((AccompanyPlayerGroup) playerGroup).getAccompanyPlayer().setVolume((float) progress / 100);
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });
        }
        // 如果是自弹自唱模式，删除伴奏音量调节（不播放伴奏）
        else {
            tuneWrapper.removeView(findViewById(R.id.wrapperTuneAccompany));

            // BottomSheet可以向上拖动
            BottomSheetBehavior<View> bottomSheetBehavior = BottomSheetBehavior.from(findViewById(R.id.trackBottomSheet));
            LinearLayout trackSeekBarWrapper = findViewById(R.id.trackSeekBarWrapper);
            trackSeekBarWrapper.setVisibility(View.GONE);
            bottomSheetBehavior.setHideable(false);
            // 在BottomSheet缩回时隐藏拖动条，否则底部会露出一部分进度条，非常丑
            bottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
                @Override
                public void onStateChanged(@NonNull View bottomSheet, int newState) {
                    if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                        trackSeekBarWrapper.setVisibility(View.GONE);
                    } else {
                        trackSeekBarWrapper.setVisibility(View.VISIBLE);
                    }
                }

                // 在用户拉起BottomSheet时暗化背景
                @Override
                public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                    findViewById(R.id.singResultBackgroundDummy).setAlpha(slideOffset / 2);
                }
            });

            // 初始化钢琴伴奏调音
            SeekBar seekBarTunePiano = findViewById(R.id.seekbarTunePiano);
            seekBarTunePiano.setMax(100);
            seekBarTunePiano.setProgress(100);
            seekBarTunePiano.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (fromUser) {
                        ((InstrumentPlayerGroup) playerGroup).getPianoPlayer().setVolume((float) progress / 100);
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });

            // 初始化鼓点伴奏调音
            SeekBar seekBarTuneDrum = findViewById(R.id.seekbarTuneDrum);
            seekBarTuneDrum.setMax(100);
            seekBarTuneDrum.setProgress(70);
            ((InstrumentPlayerGroup) playerGroup).getDrumPlayer().setVolume((float) 0.7);
            seekBarTuneDrum.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (fromUser) {
                        ((InstrumentPlayerGroup) playerGroup).getDrumPlayer().setVolume((float) progress / 100);
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });

            // 初始化贝斯伴奏调音
            SeekBar seekBarTuneBass = findViewById(R.id.seekbarTuneBass);
            seekBarTuneBass.setMax(100);
            seekBarTuneBass.setProgress(70);
            ((InstrumentPlayerGroup) playerGroup).getBassPlayer().setVolume((float) 0.7);
            seekBarTuneBass.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (fromUser) {
                        ((InstrumentPlayerGroup) playerGroup).getBassPlayer().setVolume((float) progress / 100);
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });

            // 初始化管弦伴奏调音
            SeekBar seekBarTuneOrchestra = findViewById(R.id.seekbarTuneOrchestra);
            seekBarTuneOrchestra.setMax(100);
            seekBarTuneOrchestra.setProgress(70);
            ((InstrumentPlayerGroup) playerGroup).getOrchestraPlayer().setVolume((float) 0.7);
            seekBarTuneOrchestra.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (fromUser) {
                        ((InstrumentPlayerGroup) playerGroup).getOrchestraPlayer().setVolume((float) progress / 100);
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });
        }
    }

    /**
     * 将activity设为播放中状态
     */
    private void start() {
        this.state = State.PLAYING;
        btnPlay.setVisibility(View.GONE);
        btnPause.setVisibility(View.VISIBLE);

        handler.postDelayed(progressUpdater, 0);
        playerGroup.startAllPlayers();
    }

    /**
     * 将activity设为暂停状态
     */
    private void pause() {
        this.state = State.PAUSE;
        btnPause.setVisibility(View.GONE);
        btnPlay.setVisibility(View.VISIBLE);

        playerGroup.pauseAllPlayers();
        handler.removeCallbacks(progressUpdater);
    }

    /**
     * 将activity设为停止状态，但其实是将播放器都暂停，并将进度调回0
     */
    private void stop() {
        this.state = State.UNSTARTED;
        btnPause.setVisibility(View.GONE);
        btnPlay.setVisibility(View.VISIBLE);

        playerGroup.pauseAllPlayers();
        playerGroup.seekTo(0);
        seekBarResultProgress.setProgress(0);
        handler.removeCallbacks(progressUpdater);
    }

    /**
     * 如果用户在播放录音时退出APP，则暂停播放
     */
    @Override
    protected void onStop() {
        super.onStop();

        if (this.state == State.PLAYING) {
            stop();
        }

    }

    /**
     * 在退出演唱结果页时释放播放器资源、并删除临时文件
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        deleteOneFile(getVoiceFullPath(songName));
        if (isFromAccompanySingActivity()) {
            deleteOneFile(getTrimmedAccompanyFullPath(songName));
        }
        handler.removeCallbacks(progressUpdater);
        playerGroup.terminateAllPlayers();
    }

    /**
     * 将持续时间从毫秒转化为mm-ss格式
     * @param duration 以毫秒为单位的持续时间
     * @return 格式化后的字符串
     */
    @SuppressLint("DefaultLocale")
    private String convertFormat(int duration) {
        return String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(duration),
                TimeUnit.MILLISECONDS.toSeconds(duration)
                        - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration)));
    }

    /**
     * 帮助函数，用于判断
     * @return 如果是伴奏演唱模式跳转过来，返回true
     */
    private boolean isFromAccompanySingActivity() {
        return callingActivity.equals(From.ACCOMPANY);
    }

    private enum From {
        ACCOMPANY, INSTRUMENT
    }

    private enum State {
        PLAYING, PAUSE, UNSTARTED
    }
}