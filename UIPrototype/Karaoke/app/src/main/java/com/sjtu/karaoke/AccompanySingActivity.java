package com.sjtu.karaoke;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.VideoView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.w3c.dom.Text;

public class AccompanySingActivity extends AppCompatActivity {

    VideoView videoView;
    ProgressBar mProgressBar, mScoreBar;
    TextView scoreRecorder;

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

        MediaController mediaController = new MediaController(this);
        videoView.setMediaController(mediaController);
        mediaController.setAnchorView(videoView);

        // progress bar

        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mProgressBar.setProgress(0);
        mProgressBar.setMax(100);

        new MyAsync().execute();

        // score bar

        mScoreBar = (ProgressBar) findViewById(R.id.scoreBar);
        mScoreBar.setMax(100);
        mScoreBar.setProgress(80);

        Drawable draw = getDrawable(R.drawable.custom_scorebar);
        mScoreBar.setProgressDrawable(draw);


//        scoreRecorder = (TextView) findViewById(R.id.singingScore);
//        int padding = scoreRecorder.getCompoundPaddingRight();
//        mScoreBar.offsetLeftAndRight(padding);

    }
    

    private class MyAsync extends AsyncTask<Void, Integer, Void> {

        int duration = 0;
        int current = 0;

        @Override
        protected Void doInBackground(Void... params) {

            videoView.start();
            videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {

                public void onPrepared(MediaPlayer mp) {
                    duration = videoView.getDuration();
                }
            });

            do {
                current = videoView.getCurrentPosition();
                System.out.println("duration - " + duration + " current- "
                        + current);
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
            System.out.println(values[0]);
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

