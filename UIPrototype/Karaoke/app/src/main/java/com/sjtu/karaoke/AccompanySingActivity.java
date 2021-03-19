package com.sjtu.karaoke;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import android.content.Intent;

import android.content.res.AssetFileDescriptor;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.TextView;

import android.widget.Toast;
import android.widget.VideoView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.sang.lrcview.LrcView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

public class AccompanySingActivity extends AppCompatActivity {

    private static final int UPDATE_INTERVAL = 100;

    private enum State {
        PAUSE, PLAYING, UNSTARTED
    }

    private enum SingMode {
        WITH, WITHOUT
    }

    VideoView videoView;
    LrcView lrcView;
    MediaPlayer accompanyPlayer;
    ProgressBar mProgressBar, mScoreBar;
    TextView scoreRecorder;
    FloatingActionButton fab;
    Integer duration;
    State state;
    SingMode singMode;
    Thread progressBarUpdater;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accompany_sing);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        initToolbar();

        initVideoView();

        initLrcView();

        initBottomNavbar();

        initProgressBar();

        initScoreBar();

        initFab();

        initState();
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
                } else if (state == State.PAUSE) {
                    startAllPlayers();
                } else {
                    startAllPlayers();
                    // todo: do some async task, setting progress bar and score bar
                    startUpdateProgressBar();
                }
            }
        });
    }

    private void startUpdateProgressBar() {
        progressBarUpdater = new Thread(new Runnable() {
            @Override
            public void run() {
                int current;
                int progress;

                do {
                    current = videoView.getCurrentPosition();
                    progress = (int) (current * 100 / duration);

                    if (progress >= 100) {
                        break;
                    }

                    mProgressBar.setProgress(progress);
                    try {
                        Thread.sleep(UPDATE_INTERVAL);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } while (progress <= 100 && !Thread.currentThread().isInterrupted());
            }
        });

        progressBarUpdater.start();
    }

    private void stopUpdateProgressBar() {
        progressBarUpdater.interrupt();
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
    }

    private void pauseAllPlayers() {
        accompanyPlayer.pause();
        videoView.pause();
        fab.setImageResource(R.drawable.ic_fab_play);
        state = State.PAUSE;
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
        mProgressBar.setMax(100);
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
                            case R.id.singingRetry:
                                if (state != State.UNSTARTED) {
                                    // todo: clear record, reset score, progress bar, terminate async
                                    mProgressBar.setProgress(0, true);

                                    lrcView.init();

                                    restartAllPlayers();
                                    stopUpdateProgressBar();
                                }
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
        state = State.UNSTARTED;
    }

    private void initLrcView() {
        lrcView = findViewById(R.id.lrcRoller);
        lrcView.setHighLineColor(ContextCompat.getColor(getApplicationContext(), R.color.purple_500));
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
//        Toast.makeText(getApplicationContext(), item.getTitle(), Toast.LENGTH_SHORT).show();
        Intent intent;

        if (item.getItemId() == R.id.finish)
            intent = new Intent(this, SingResultActivity.class);
        else
            intent = new Intent(this, MainActivity.class);

        startActivity(intent);

        return true;
    }

    private void initVideoView() {
        videoView = findViewById(R.id.video_view);
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbarAccompanySing);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
    }

    /**
     * Reset file path for videoView and accompanyPlayer
     * accompanyPlaer status: prepare
     * videoView status: stop
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onStart() {
        super.onStart();
        /**
         * todo: store song name, accompany file name and mv file name (if any)
         * when choosing a song from ViewSongsFragment, above data will be present (in the Intent)
         * whereas returning from sing result, they will be absent, so setup the previous song
         */

        initState();

        setUpVideoView();

        setUpLrcView();

        // Progress bar
        mProgressBar.setProgress(0);

        // Score bar
    }

    private void setUpVideoView() {
        String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.attention;
        Uri uri = Uri.parse(videoPath);
        videoView.setVideoURI(uri);
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            public void onPrepared(MediaPlayer mp) {
                duration = videoView.getDuration();
//                mp.setVolume(0, 0);
            }
        });

        // todo: setup onCompletetionListener
    }

    @Override
    protected void onStop() {
        super.onStop();
        videoView.stopPlayback();
        accompanyPlayer.stop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.sing_menu, menu);
        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void setUpLrcView() {
        String lrc = null;
        try {
            InputStream stream = getAssets().open("Attention.lrc");

            lrc = new BufferedReader(new InputStreamReader(stream))
                    .lines().collect(Collectors.joining("\n"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        lrcView.setLrc(lrc);

        AssetFileDescriptor afd = null;
        try {
            afd = getAssets().openFd("Attention.mp3");
        } catch (IOException e) {
            e.printStackTrace();
        }
        accompanyPlayer = new MediaPlayer();
        try {
            accompanyPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
        } catch (IOException e) {
            e.printStackTrace();accompanyPlayer.seekTo(0);
                                    accompanyPlayer.pause();
                                    videoView.pause();
                                    videoView.seekTo(0);
                                    fab.setImageResource(R.drawable.ic_fab_play);
                                    state = State.UNSTARTED;
        }
        try {
            accompanyPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        lrcView.setPlayer(accompanyPlayer);
        lrcView.init();
    }
}
