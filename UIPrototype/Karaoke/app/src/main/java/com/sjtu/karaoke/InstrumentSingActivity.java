package com.sjtu.karaoke;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.sang.lrcview.LrcView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class InstrumentSingActivity extends AppCompatActivity {

    Toolbar toolbar;
    List<ProgressBar> instrumentBtns;
    MediaPlayer accompanyPlayer;
    LrcView lrcView;
    private Menu menu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instrument_sing);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        initToolbar();

        initInstrumentBtns();

        initLrcView();

        initBtns();

//        int n = instrumentBtns.size();
//        for (int i = n-1; i >= 0; --i) {
//            ProgressBar btn = instrumentBtns.get(i);
//            setBtnClickEvent(btn);
//        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onStart() {
        super.onStart();
        setUpLrcView();
        accompanyPlayer.start();

        accompanyPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

            @Override
            public void onCompletion(MediaPlayer accompanyPlayer) {
                Intent intent = new Intent(getApplicationContext(), SingResultActivity.class);
                startActivity(intent);
            }

        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        accompanyPlayer.stop();
    }

    private void initToolbar() {
        toolbar = findViewById(R.id.toolbarInstrumentSing);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
    }

    private void initInstrumentBtns() {

        instrumentBtns = new ArrayList<>();

        instrumentBtns.add(findViewById(R.id.instrumentButton0));
        instrumentBtns.add(findViewById(R.id.instrumentButton1));
        instrumentBtns.add(findViewById(R.id.instrumentButton2));
        instrumentBtns.add(findViewById(R.id.instrumentButton3));
        instrumentBtns.add(findViewById(R.id.instrumentButton4));

        @SuppressLint("UseCompatLoadingForDrawables")
        Drawable draw = getDrawable(R.drawable.custom_instrument_button);

        Iterator<ProgressBar> btnIt = instrumentBtns.iterator();
        ProgressBar instrumentBtn;

        while (btnIt.hasNext()) {
            instrumentBtn = btnIt.next();
            instrumentBtn.setMax(100);
            instrumentBtn.setProgress(0);
            instrumentBtn.setProgressDrawable(draw);
        }

    }

    private void initLrcView() {
        lrcView = findViewById(R.id.lrcRoller);
        lrcView.setHighLineColor(ContextCompat.getColor(getApplicationContext(), R.color.purple_500));
    }

    private void initBtns() {

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
        }
        try {
            accompanyPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }

        accompanyPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

            @Override
            public void onCompletion(MediaPlayer accompanyPlayer) {
                Intent intent = new Intent(getApplicationContext(), SingResultActivity.class);
                startActivity(intent);
            }

        });

        lrcView.setPlayer(accompanyPlayer);
        lrcView.init();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.instrument_sing_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        Intent intent;

        if (item.getItemId() == R.id.instrumentSingRetry) {
            retry();
            return true;
        }
        else if (item.getItemId() == R.id.finish)
            intent = new Intent(this, SingResultActivity.class);
        else
            intent = new Intent(this, MainActivity.class);

        startActivity(intent);

        return true;
    }

    private void retry() {
        accompanyPlayer.seekTo(0);
    }

//
//    private void setBtnClickEvent(ProgressBar instrumentBtn) {
//            instrumentBtn.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    int progress = instrumentBtn.getProgress();
//                    instrumentBtn.setProgress(progress+10);
//                }
//            });
//    }

//        while (btnIt.hasNext()) {
//            final ProgressBar instrumentBtn = btnIt.next();
//            int progress = instrumentBtn.getProgress();
//            instrumentBtn.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    instrumentBtn.setProgress(progress+10);
//                }
//            });
//        }



//
//    private void setBtnClickEvent(ProgressBar instrumentBtn) {
//            instrumentBtn.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    int progress = instrumentBtn.getProgress();
//                    instrumentBtn.setProgress(progress+10);
//                }
//            });
//    }

//        while (btnIt.hasNext()) {
//            final ProgressBar instrumentBtn = btnIt.next();
//            int progress = instrumentBtn.getProgress();
//            instrumentBtn.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    instrumentBtn.setProgress(progress+10);
//                }
//            });
//        }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }
}