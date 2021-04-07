package com.sjtu.karaoke;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;

import static com.sjtu.karaoke.util.Utils.loadAndPrepareMediaplayer;
import static com.sjtu.karaoke.util.Utils.saveFile;
import static com.sjtu.karaoke.util.Utils.terminateMediaPlayer;

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

    int duration;

    Toolbar toolbar;
    BottomNavigationView bottomNavbarResult;
    TextView titleText, playerPosition, playerDuration;
    SeekBar seekBarResultProgress,  seekbarTuneVoice, seekbarTuneAccompany;
    ImageView btnPlay, btnPause;
    MediaPlayer accompanyPlayer, voicePlayer;
    Handler handler = new Handler();
    Runnable runnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sing_result);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        accompanyPlayer = new MediaPlayer();
        voicePlayer = new MediaPlayer();
        loadAndPrepareMediaplayer(this, accompanyPlayer, "Accompany.mp3");
        loadAndPrepareMediaplayer(this, voicePlayer, "voice.m4a");

        initRunnable();

        initToolBar();

        initTitle();

        initBottomNavbar();

        initPlaySeekbar();

        initButtonControl();

        initTuneSeekbar();

        startAllPlayers();

        initFab();
    }

    private void initFab() {
        FloatingActionButton fabSave = findViewById(R.id.fabSave);
        fabSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveFile(SingResultActivity.this, "1.txt");
            }
        });
    }

    private void initRunnable() {

        runnable = new Runnable() {
            @Override
            public void run() {
                seekBarResultProgress.setProgress(accompanyPlayer.getCurrentPosition());
                handler.postDelayed(this, 500);
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

        Spannable songName = new SpannableString("Attention");
        songName.setSpan(new ForegroundColorSpan(Color.WHITE), 0, songName.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        titleText.setText(songName);

        Spannable rating = new SpannableString("  SS");
        rating.setSpan(new ForegroundColorSpan(Color.YELLOW), 0, rating.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        titleText.append(rating);

    }

    private void initBottomNavbar() {
        bottomNavbarResult = findViewById(R.id.bottomNavigationViewResult);
        bottomNavbarResult.setBackground(null);
        bottomNavbarResult.getMenu().getItem(1).setEnabled(false);

        bottomNavbarResult.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                switch(id) {
                    case R.id.resultRetry:
                        onBackPressed();
                        break;
                    case R.id.resultShare:
                        break;
                }
                return false;
            }
        });
    }


    private void initPlaySeekbar() {
        duration =  accompanyPlayer.getDuration();
        // seekbar text

        playerPosition = findViewById(R.id.playerPosition);
        playerDuration = findViewById(R.id.playerDuration);
        String sDuration = convertFormat(duration);
        playerDuration.setText(sDuration);

        // seekbar

        seekBarResultProgress = findViewById(R.id.seekbarResultProgress);
        seekBarResultProgress.setMax(duration);
        seekBarResultProgress.setProgress(0);

        seekBarResultProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    accompanyPlayer.seekTo(progress);
                    voicePlayer.seekTo(progress);
                    System.out.println(convertFormat(progress));
                }

                playerPosition.setText(convertFormat((accompanyPlayer.getCurrentPosition())));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private void initButtonControl() {

        btnPlay = findViewById(R.id.resultPlay);
        btnPause = findViewById(R.id.resultPause);

        accompanyPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                btnPause.setVisibility(View.GONE);
                btnPlay.setVisibility(View.VISIBLE);
                accompanyPlayer.seekTo(0);
                voicePlayer.seekTo(0);
            }
        });

        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnPlay.setVisibility(View.GONE);
                btnPause.setVisibility(View.VISIBLE);
                startAllPlayers();
            }
        });

        btnPause.setOnClickListener((new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnPause.setVisibility(View.GONE);
                btnPlay.setVisibility(View.VISIBLE);
                pauseAllPlayers();
            }
        }));
    }

    private void pauseAllPlayers() {
        syncedCommand(voicePlayer, accompanyPlayer, MP_COMMAND.PAUSE);
        handler.removeCallbacks(runnable);
    }

    private void initTuneSeekbar() {

        seekbarTuneVoice = findViewById(R.id.seekbarTuneVoice);
        seekbarTuneVoice.setMax(100);
        seekbarTuneVoice.setProgress(80);

        seekbarTuneAccompany = findViewById(R.id.seekbarTuneAccompany);
        seekbarTuneAccompany.setMax(100);
        seekbarTuneAccompany.setProgress(70);

        seekbarTuneVoice.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float volume = (float)progress / 100;
                if (fromUser)
                    voicePlayer.setVolume(volume, volume);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        seekbarTuneAccompany.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float volume = (float)progress / 100;
                if (fromUser)
                    accompanyPlayer.setVolume(volume, volume);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private void startAllPlayers() {
        handler.postDelayed(runnable, 0);

        voicePlayer.start();
        accompanyPlayer.start();
    }

    @Override
    protected void onStop() {
        super.onStop();
        btnPause.callOnClick();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(runnable);

        terminateMediaPlayer(voicePlayer);
        terminateMediaPlayer(accompanyPlayer);
    }

    @SuppressLint("DefaultLocale")
    private String convertFormat(int duration) {
        return String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(duration),
                TimeUnit.MILLISECONDS.toSeconds(duration)
                        - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration)));
    }

    public enum MP_COMMAND {
        START,
        STOP,
        PAUSE
    }

    /**
     * Uses threads to execute synced commands for the current video media player and
     * background music player in tandem.
     */
    public void syncedCommand(MediaPlayer player1, MediaPlayer player2, MP_COMMAND command) {
        final CyclicBarrier commandBarrier = new CyclicBarrier(2);
        new Thread(new SyncedCommandService(commandBarrier, player1, command)).start();
        new Thread(new SyncedCommandService(commandBarrier, player2, command)).start();
    }

    /**
     * Inner class that starts a given media player synchronously
     * with other threads utilizing SyncedStartService
     */
    static private class SyncedCommandService implements Runnable {
        private final CyclicBarrier              mCommandBarrier;
        private final SingResultActivity.MP_COMMAND mCommand;
        private final MediaPlayer                mMediaPlayer;

        public SyncedCommandService(CyclicBarrier barrier, MediaPlayer player, SingResultActivity.MP_COMMAND command) {
            mCommandBarrier = barrier;
            mMediaPlayer = player;
            mCommand = command;
        }

        @Override public void run() {
            try {
                mCommandBarrier.await();
            } catch (InterruptedException | BrokenBarrierException e) {
                e.printStackTrace();
            }

            switch (mCommand) {
                case START:
                    mMediaPlayer.start();
                    break;

                case STOP:
                    mMediaPlayer.stop();
                    break;

                case PAUSE:
                    mMediaPlayer.pause();
                    break;

                default:
                    break;
            }
        }
    }
}