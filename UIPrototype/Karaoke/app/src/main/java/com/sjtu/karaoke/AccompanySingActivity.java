package com.sjtu.karaoke;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import android.content.res.AssetFileDescriptor;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.sang.lrcview.LrcView;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

import static android.widget.Toast.*;

public class AccompanySingActivity extends AppCompatActivity {

    VideoView videoView;
    LrcView lrcView;
    ProgressBar mProgressBar, mScoreBar;
    TextView scoreRecorder;
    FloatingActionButton  fab;
    Integer duration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accompany_sing);

        // toolbar

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbarSing);
        // TextView mTitle = (TextView) toolbar.findViewById(R.id.toolbar_sing_title);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        // bottom navbar

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setBackground(null);
        bottomNavigationView.getMenu().getItem(1).setEnabled(false);


        // video

        videoView = findViewById(R.id.video_view);
        String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.attention;
        Uri uri = Uri.parse(videoPath);
        videoView.setVideoURI(uri);
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {

            public void onPrepared(MediaPlayer mp) {
                duration = videoView.getDuration();
                Toast.makeText(getApplicationContext(), duration.toString(), Toast.LENGTH_SHORT).show();
                mp.setVolume(0, 0);
            }
        });

        MediaController mediaController = new MediaController(this);
        videoView.setMediaController(mediaController);
        mediaController.setAnchorView(videoView);

        // progress bar

        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mProgressBar.setProgress(0);
        mProgressBar.setMax(100);

        // score bar

        mScoreBar = (ProgressBar) findViewById(R.id.scoreBar);
        mScoreBar.setMax(100);
        mScoreBar.setProgress(80);

        Drawable draw = getDrawable(R.drawable.custom_scorebar);
        mScoreBar.setProgressDrawable(draw);


//        scoreRecorder = (TextView) findViewById(R.id.singingScore);
//        int padding = scoreRecorder.getCompoundPaddingRight();
//        mScoreBar.offsetLeftAndRight(padding);

        // LrcView
        lrcView = findViewById(R.id.lrcRoller);
        fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new MyAsync().execute();
            }
        });
    }
    

    private class MyAsync extends AsyncTask<Void, Integer, Void> {

        int current = 0;

        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        protected Void doInBackground(Void... params) {

            lrcView.setHighLineColor(ContextCompat.getColor(getApplicationContext(), R.color.purple_500));

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
            MediaPlayer player = new MediaPlayer();
            try {
                player.setDataSource(afd.getFileDescriptor(),afd.getStartOffset(),afd.getLength());
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                player.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }
            lrcView.setPlayer(player);
            lrcView.init();

            player.start();
            videoView.start();


            do {
                current = videoView.getCurrentPosition();
//                System.out.println("duration - " + duration + " current- "
//                        + current);
                try {
                    publishProgress((int) (current * 100 / duration));
                    if(mProgressBar.getProgress() >= 100){
                        break;
                    }
                } catch (Exception e) {
                }
            } while (mProgressBar.getProgress() <= 100);

            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
//            System.out.println(values[0]);
            mProgressBar.setProgress(values[0]);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.sing_menu, menu);
        return true;
    }

//    public void singingModeClick(View v)
//    {
//        MenuItem singingMode = findViewById(R.id.singingMode);
//        singingMode.setTitle("原唱");
//    }
}

