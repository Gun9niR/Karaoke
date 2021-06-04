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
 * @Author: guozh
 * @Date: 2021/3/28
 * @Version: v1.2
 * @Description: 演唱结果界面。本类中包括了如下功能：
 *                  1. 各个组件的初始化、设置点击事件
 *                  2. 根据传入的歌曲信息、用户录音，初始化伴奏播放器、录音播放器
 *                  3. 在播放时监控进度、更新进度条
 *                  4. 在用户拖动音量条时修改录音或伴奏的音量
 */

public class SingResultActivity extends AppCompatActivity {
    final int INITIAL_OFFSET = 300;

    // mode dependent
    From callingActivity;

    SeekBar seekBarResultProgress;

    ExoPlayerGroup playerGroup;

    Toolbar toolbar;
    BottomNavigationView bottomNavbarResult;
    TextView titleText, playerPosition, playerDuration;
    ImageView btnPlay;
    ImageView btnPause;
    ImageView albumCover;
    RateResultDialog rateResultDialog;

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

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sing_result);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // initialize song info and mode info
        Intent intent = getIntent();
        id = intent.getIntExtra("id", 0);
        songName = intent.getStringExtra("songName");

        callingActivity = getCallingActivity().getShortClassName().equals(".AccompanySingActivity")
                ? From.ACCOMPANY : From.INSTRUMENT;

        initPlayerGroup();

        this.state = State.UNSTARTED;

        // init UI part that is mode-irrelevant
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

    private void initPlayerGroup() {
        if (isFromAccompanySingActivity()) {
            playerGroup = new AccompanyPlayerGroup(this, songName, INITIAL_OFFSET);
        } else {
            playerGroup = new InstrumentPlayerGroup(this, songName, INITIAL_OFFSET);
        }
    }

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

    private void initAlbumCover() {
        albumCover = findViewById(R.id.albumCover);
        setImageFromFile(getAlbumCoverFullPath(songName), albumCover);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void initAlignSeekbar() {
        SeekBar seekbarAlignVoice = findViewById(R.id.seekbarAlignVoice);
        // Tune forward or backward by 1s
        seekbarAlignVoice.setMin(0);
        seekbarAlignVoice.setMax(1000);
        seekbarAlignVoice.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            private boolean isPreviouslyPlaying;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                playerGroup.setVoiceOffset(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                isPreviouslyPlaying = playerGroup.isPlaying();
                pause();
            }

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

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void initFab() {
        FloatingActionButton fabSave = findViewById(R.id.fabSave);
        fabSave.setOnClickListener(view -> {
            if (isFileSaved) {
                showSuccessToast(SingResultActivity.this, "文件已经保存", 350);
            } else {
                LoadingDialog loadingDialog = showLoadingDialog(SingResultActivity.this, "正在生成作品...");
                new Thread(() -> {
                    saveRecord(songName, playerGroup, rateResultDialog.getRankingText());
                    loadingDialog.dismiss();
                    showSuccessToast(SingResultActivity.this, "录音已成功保存", 350);
                    isFileSaved = true;
                }).start();
            }
        });
    }

    /**
     * Save record
     * @param songName Name of the song
     * @param playerGroup Player group that handles merging of multiple wav files
     * @param rankingText T
     * @return Path to merged record .wav file
     */
    private String saveRecord(String songName, ExoPlayerGroup playerGroup, String rankingText) {
        String dirFullPath = RECORD_DIRECTORY + UUID.randomUUID().toString().replaceAll("-", "") + "/";
        String wavFullPath = dirFullPath + songName + ".wav";

        File dir = new File(dirFullPath);
        dir.mkdirs();

        // copy and rename cover
        try {
            FileUtils.copyFile(new File(getAlbumCoverFullPath(songName)),
                    new File(getRecordCoverFullPath(dirFullPath)));
        } catch (IOException e) {
            e.printStackTrace();
        }

        // write metadata
        try {
             BufferedWriter writer = new BufferedWriter(
                     new FileWriter(getRecordMetadataFullPath(dirFullPath)));
            // write song name
            writer.write(songName);
            writer.newLine();

            // write record time
            Date date = new Date(System.currentTimeMillis());
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HH-mm",
                    Locale.CHINA);
            String dateString = formatter.format(date);
            writer.write(dateString);
            writer.newLine();

            // write rank
            writer.write(rankingText);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // merge wav
        playerGroup.mergeWav(getRecordFileFullPath(dirFullPath, songName));

        return wavFullPath;
    }

    private void initProgressUpdater() {
        progressUpdater = new Runnable() {
            @Override
            public void run() {
                seekBarResultProgress.setProgress(playerGroup.getCurrentPosition());
                handler.postDelayed(this, PROGRESS_UPDATE_INTERVAL);
            }
        };
    }

    private void initToolBar() {
        toolbar = findViewById(R.id.toolbarResult);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
    }

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

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void initBottomNavbar() {
        bottomNavbarResult = findViewById(R.id.bottomNavigationViewResult);
        bottomNavbarResult.setBackground(null);
        bottomNavbarResult.getMenu().getItem(1).setEnabled(false);

        bottomNavbarResult.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            String recordFullPath;

            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                switch (id) {
                    case R.id.resultRetry:
                        onBackPressed();
                        break;
                    case R.id.resultShare:
                        if (!isFileSaved) {
                            LoadingDialog loadingDialog = showLoadingDialog(
                                    SingResultActivity.this,
                                    "正在生成作品...");
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


    private void initPlaySeekbar() {
        // seekbar text
        playerPosition = findViewById(R.id.playerPosition);
        playerDuration = findViewById(R.id.playerDuration);
        String sDuration = convertFormat(playerGroup.getDuration());
        playerDuration.setText(sDuration);

        // seekbar
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

    private void initButtonControl() {

        btnPlay = findViewById(R.id.resultPlay);
        btnPause = findViewById(R.id.resultPause);

        btnPlay.setOnClickListener(v -> start());
        btnPause.setOnClickListener((v -> pause()));

        btnPlay.setVisibility(View.VISIBLE);
        btnPause.setVisibility(View.GONE);
    }

    private void pause() {
        this.state = State.PAUSE;
        btnPause.setVisibility(View.GONE);
        btnPlay.setVisibility(View.VISIBLE);

        playerGroup.pauseAllPlayers();
        handler.removeCallbacks(progressUpdater);
    }

    private void initTuneSeekbar() {
        // voice tuner shared by both modes
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
        if (isFromAccompanySingActivity()) {
            System.out.println("from accompany");
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
        } else {
            tuneWrapper.removeView(findViewById(R.id.wrapperTuneAccompany));

            BottomSheetBehavior<View> bottomSheetBehavior = BottomSheetBehavior.from(findViewById(R.id.trackBottomSheet));
            LinearLayout trackSeekBarWrapper = findViewById(R.id.trackSeekBarWrapper);
            trackSeekBarWrapper.setVisibility(View.GONE);
            bottomSheetBehavior.setHideable(false);
            bottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
                @Override
                public void onStateChanged(@NonNull View bottomSheet, int newState) {
                    if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                        trackSeekBarWrapper.setVisibility(View.GONE);
                    } else {
                        trackSeekBarWrapper.setVisibility(View.VISIBLE);
                    }
                }

                @Override
                public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                    findViewById(R.id.singResultBackgroundDummy).setAlpha(slideOffset / 2);
                }
            });

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

            SeekBar seekBarTuneDrum = findViewById(R.id.seekbarTuneDrum);
            seekBarTuneDrum.setMax(100);
            seekBarTuneDrum.setProgress(0);
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

            SeekBar seekBarTuneBass = findViewById(R.id.seekbarTuneBass);
            seekBarTuneBass.setMax(100);
            seekBarTuneBass.setProgress(0);
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

            SeekBar seekBarTuneOrchestra = findViewById(R.id.seekbarTuneOrchestra);
            seekBarTuneOrchestra.setMax(100);
            seekBarTuneOrchestra.setProgress(0);
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

    private void start() {
        this.state = State.PLAYING;
        btnPlay.setVisibility(View.GONE);
        btnPause.setVisibility(View.VISIBLE);

        handler.postDelayed(progressUpdater, 0);
        playerGroup.startAllPlayers();
    }

    private void stop() {
        this.state = State.UNSTARTED;
        btnPause.setVisibility(View.GONE);
        btnPlay.setVisibility(View.VISIBLE);

        playerGroup.pauseAllPlayers();
        playerGroup.seekTo(0);
        seekBarResultProgress.setProgress(0);
        handler.removeCallbacks(progressUpdater);
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (this.state == State.PLAYING) {
            stop();
        }

    }

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

    @SuppressLint("DefaultLocale")
    private String convertFormat(int duration) {
        return String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(duration),
                TimeUnit.MILLISECONDS.toSeconds(duration)
                        - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration)));
    }

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