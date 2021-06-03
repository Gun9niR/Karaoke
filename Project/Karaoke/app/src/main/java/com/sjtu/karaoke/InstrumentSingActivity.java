package com.sjtu.karaoke;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Space;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.core.content.ContextCompat;

import com.arthenica.mobileffmpeg.FFmpeg;
import com.dreamfish.record.AudioRecorder;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.sjtu.karaoke.component.LoadingDialog;
import com.sjtu.karaoke.data.Chord;
import com.sjtu.karaoke.data.PlayChordRecord;
import com.sjtu.karaoke.data.Score;
import com.sjtu.pianorater.PianoRater;
import com.sunty.droidparticle.ParticleSystem;
import com.sunty.droidparticle.ParticleSystemView;
import com.sunty.droidparticle.ParticleSystemConfig;

import org.apache.commons.io.FileUtils;
import org.sang.lrcview.LrcView;
import org.sang.lrcview.bean.LrcBean;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Scanner;
import java.util.stream.Collectors;

import me.grantland.widget.AutofitTextView;

import static com.dreamfish.record.AudioRecorder.PCM_SPLIT_INTERVAL;
import static com.sjtu.karaoke.singrater.RatingUtil.getScore;
import static com.sjtu.karaoke.singrater.RatingUtil.init;
import static com.sjtu.karaoke.util.Constants.ASSET_DIRECTORY;
import static com.sjtu.karaoke.util.Constants.PROGRESS_UPDATE_INTERVAL;
import static com.sjtu.karaoke.util.Constants.RECORD_DELAY_LB;
import static com.sjtu.karaoke.util.Constants.RECORD_DELAY_UB;
import static com.sjtu.karaoke.util.FileUtil.deleteOneFile;
import static com.sjtu.karaoke.util.MediaPlayerUtil.loadAudioFileAndPrepareExoPlayer;
import static com.sjtu.karaoke.util.MediaPlayerUtil.terminateExoPlayer;
import static com.sjtu.karaoke.util.MiscUtil.mergeNotesToChord;
import static com.sjtu.karaoke.util.MiscUtil.parseScore;
import static com.sjtu.karaoke.util.MiscUtil.showLoadingDialog;
import static com.sjtu.karaoke.util.MiscUtil.showSuccessToast;
import static com.sjtu.karaoke.util.PathUtil.getAccompanyFullPath;
import static com.sjtu.karaoke.util.PathUtil.getAssetFullPath;
import static com.sjtu.karaoke.util.PathUtil.getChordTransFullPath;
import static com.sjtu.karaoke.util.PathUtil.getLyricInstrumentFullPath;
import static com.sjtu.karaoke.util.PathUtil.getRateFullPath;
import static com.sjtu.karaoke.util.PathUtil.getUserPlayFullPath;


/*
 * @ClassName: InstrumentSingActivity
 * @Author: guozh
 * @Date: 2021/3/28
 * @Version: v1.2
 * @Description: 自弹自唱界面。本类中包含了如下功能：
 *                  1. 各个组件的初始化、设置点击事件
 *                  2. 根据传入的歌曲信息，初始化伴奏播放器、歌词滚动器、音效池
 *                  3. 在播放时监控进度、更新得分（不以得分条的形式显示）
 *                  4. 录音
 */

public class InstrumentSingActivity extends AppCompatActivity {
    private static final float chordVolume = (float) 1.0;
    private static Integer HINT_DURATION = 3000;

    SimpleExoPlayer accompanyPlayer;
    LrcView lrcView;
    SoundPool chordPlayer;
    AudioRecorder voiceRecorder;
    AppCompatImageButton finishButton;
    AppCompatImageButton retryButton;
    AppCompatImageButton backButton;

    Handler handler = new Handler();

    // 监听进度
    Runnable progressMonitor;
    // 监听结束
    Runnable completionListener;
    // 监听录音切分
    Runnable recordMonitor;
    // 监听按钮提示
    Runnable hintMonitor;

    // 当前亮的进度条
    ProgressBar currentHint;
    // 当前保持在顶部的粒子特效
    ParticleSystem currentPtc;

    // 状态量
    // 当前歌的id
    int id;
    // 当前歌名
    String songName;
    // 当前播放进度
    int currentPosition;
    // 下一次切分pcm的时间
    int nextPcmSplitTime;
    // 当前的播放状态，未开始、正在播放、暂停
    State state;
    // 每句话的歌词
    List<LrcBean> lrcs;
    // 当前歌词迭代器
    ListIterator<LrcBean> lrcIterator;
    // 当前歌词
    LrcBean currentLrc;
    // 得分
    Score score;
    // 演奏片段开始时间（文件中）
    int startTime;
    // 程序中的伴奏开始时间
    int playbackStartTime;
    // 演奏片段结束时间
    int finishTime;
    // 和弦/按键个数
    int chordNum;
    // 和弦对象
    List<Chord> chords;
    // 标准的每个和弦的时间
    List<PlayChordRecord> standardSequence;
    // 用户弹的每个和弦的时间
    List<PlayChordRecord> userSequence;
    // 打分线程
    List<Thread> ratingThread;
    // 将和弦名映射到和弦对象
    HashMap<String, Chord> nameToChord;
    // 将和弦映射到按钮
    HashMap<Chord, ProgressBar> chordToBtn;
    HashMap<Chord, ParticleSystemView> chordToPSV;
    // 下一次提示时间和对应和弦
    int nextHintTime;
    PlayChordRecord nextHintChord;


    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instrument_sing);
        initFullScreen();


        initSongName();
        initState();
        initProgressMonitor();
        initRecordMonitor();
        initHintMonitor();
        initTopRightButtons();
    }

    private void initFullScreen() {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onStart() {
        super.onStart();

        if (state == State.UNSTARTED) {
            LoadingDialog loadingDialog = showLoadingDialog(this, "正在初始化", true);

            initFullScreen();

            new Thread(() -> {
                parseChordFile();
                loadingDialog.setProgress(15);

                initSoundPool();
                loadingDialog.setProgress(25);

                initInstrumentButtons();
                loadingDialog.setProgress(40);

                nextPcmSplitTime = PCM_SPLIT_INTERVAL + playbackStartTime;
                initAccompanyPlayer();
                loadingDialog.setProgress(50);

                initRatingSystem();
                loadingDialog.setProgress(65);

                initOnCompleteListener();
                initLrcView();
                loadingDialog.setProgress(80);
                initVoiceRecorder();
                initScore();
                loadingDialog.setProgress(90);

                nextHintChord = standardSequence.remove(0);
                nextHintTime = getHintTime(nextHintChord.getTime());

                loadingDialog.dismiss();

                InstrumentSingActivity.this.runOnUiThread(this::start);

                this.runOnUiThread(() -> {
                    retryButton.setEnabled(true);
                    finishButton.setEnabled(true);
                });
            }).start();
        } else if (state == State.PAUSE) {
            start();
        }
    }


    @Override
    protected void onStop() {
        super.onStop();
        if (this.state == State.PLAYING) {
            pause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (voiceRecorder.getStatus() == AudioRecorder.Status.STATUS_PAUSE || voiceRecorder.getStatus() == AudioRecorder.Status.STATUS_START) {
            voiceRecorder.stopRecord(false);
        }
        lrcView.alertPlayerReleased();
        terminateExoPlayer(this, accompanyPlayer);
        chordPlayer.release();
    }

    private void initSongName() {
        Intent intent = getIntent();
        id = intent.getIntExtra("id", 0);
        songName = intent.getStringExtra("songName");
    }

    private void initState() {
        state = State.UNSTARTED;
    }

    private void initProgressMonitor() {
        progressMonitor = new Runnable() {
            @Override
            public void run() {
                currentPosition = (int) accompanyPlayer.getCurrentPosition();
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
                    nextPcmSplitTime += PCM_SPLIT_INTERVAL;

                    if (lrcIterator.hasNext() && currentPosition > currentLrc.getEnd()) {
                        if (currentLrc.shouldRate()) {
                            rate((int) currentLrc.getStart(), (int) currentLrc.getEnd());
                        }
                        currentLrc = lrcIterator.next();
                    }
                    voiceRecorder.setShouldStartNewPcm(true);
                }
                handler.postDelayed(this, 50);
            }
        };
    }

    private void initHintMonitor() {
        hintMonitor = new Runnable() {
            @Override
            public void run() {
                if (currentPosition > nextHintTime) {
                    displayHint(nextHintTime, nextHintChord);

                    if (!standardSequence.isEmpty()) {
                        nextHintChord = standardSequence.remove(0);
                        nextHintTime = getHintTime(nextHintChord.getTime());
                    } else {
                        handler.removeCallbacks(this);
                        return;
                    }
                }
                handler.postDelayed(this, 10);
            }
        };
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void initTopRightButtons() {
        finishButton = findViewById(R.id.instrumentFinishBtn);
        backButton = findViewById(R.id.instrumentBackBtn);
        retryButton = findViewById(R.id.instrumentRetryBtn);

        finishButton.setOnClickListener(v -> {
            finishButton.setEnabled(false);
            LoadingDialog loadingDialog = showLoadingDialog(this, "正在处理录音");
            int len = userSequence.size();

            new Thread(() -> {
                stopActivity(true);

                double[] userTimeSequence = new double[len];
                String[] userChordNameSequence = new String[len];
                for (int i = 0; i < len; ++i) {
                    PlayChordRecord r = userSequence.get(i);
                    userTimeSequence[i] = (r.getTime() - HINT_DURATION);
                    userChordNameSequence[i] = r.getChord().getName();
                }

                for (Thread thread: ratingThread) {
                    try {
                        thread.join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                score.computeFinalScore();
                Intent intent = new Intent(InstrumentSingActivity.this,
                        SingResultActivity.class);
                intent.putExtra("id", id);
                intent.putExtra("songName", songName);
                intent.putExtra("score", score);
                intent.putExtra("pianoScore",
                        PianoRater.getScore(
                                getChordTransFullPath(songName),
                                len,
                                userTimeSequence,
                                userChordNameSequence
                        )
                );
                startActivityForResult(intent, 0);
                loadingDialog.dismiss();
            }).start();
        });

        backButton.setOnClickListener(v -> {
            backButton.setEnabled(false);
            if (this.state != State.UNSTARTED) {
                stopActivity(false);
            }
            onBackPressed();
        });

        retryButton.setOnClickListener(v -> {
            retryButton.setEnabled(false);
            retry();
            // enable retry button in onStart(), after recording starts
        });

        finishButton.setEnabled(false);
        retryButton.setEnabled(false);
    }

    private void parseChordFile() {
        chords = new ArrayList<>();
        standardSequence = new ArrayList<>();
        userSequence = new ArrayList<>();
        nameToChord = new HashMap<>();
        chordNum = 0;

        File chordFile = new File(getChordTransFullPath(songName));
        Scanner scanner;
        String line;

        try {
            scanner = new Scanner(chordFile);

            // skip useless data
            scanner.nextDouble();
            scanner.nextInt();
            scanner.nextInt();

            // read start time and finish time
            startTime = scanner.nextInt();
            playbackStartTime = startTime - HINT_DURATION;
            finishTime = scanner.nextInt();

            // skip empty line
            do {
                line = scanner.nextLine();
            } while (line.equals(""));

            // read chord info
            for (; !line.equals(""); line = scanner.nextLine()) {
                // get chord name
                String[] params = line.split(" ");
                String chordName = params[0];
                List<String> notes = new ArrayList<>();

                // increment number of chords
                ++chordNum;

                // extract get path to note files
                for (int i = 1; i < params.length; ++i) {
                    notes.add(getAssetFullPath(this, params[i] + ".wav"));
                }

                // // merge notes to a chord file and create chord object
                Chord chord = new Chord(chordName, mergeNotesToChord(chordName, notes));
                chords.add(chord);
                nameToChord.put(chordName, chord);
            }

            // clear assets temporary file
            FileUtils.cleanDirectory(new File(ASSET_DIRECTORY));

            // generate sequence in which buttons display animation
            Integer time = startTime;
            HashMap<Chord, Integer> lastChordTime = new HashMap<>();

            while (scanner.hasNext()) {
                line = scanner.nextLine();
                String[] params = line.split(" ");

                Chord chord  = nameToChord.get(params[0]);
                int duration = Integer.parseInt(params[1]);

                if (lastChordTime.containsKey(chord)) {
                    if (time - lastChordTime.get(chord) < HINT_DURATION) {
                        HINT_DURATION = time - lastChordTime.get(chord);
                    }
                }
                lastChordTime.put(chord, time);

                standardSequence.add(new PlayChordRecord(chord, time, duration));
                time += duration;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initSoundPool() {
        chordPlayer = new SoundPool.Builder()
                .setMaxStreams(chordNum)
                .build();

        for (Chord chord: chords) {
            chord.setSoundId(chordPlayer.load(chord.getFilePath(), 1));
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void initInstrumentButtons() {
        this.runOnUiThread(() -> {
            LinearLayout btnContainer = findViewById(R.id.instrumentButtonsContainer);

            btnContainer.removeAllViews();

            ListIterator<Chord> chordIt = chords.listIterator();
            chordToBtn = new HashMap<>();
            chordToPSV = new HashMap<>();

            while (chordIt.hasNext()) {
                Chord chord = chordIt.next();
                // add relative layout
                RelativeLayout relativeLayout = new RelativeLayout(InstrumentSingActivity.this);
                LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        (float) 1.0);
                relativeLayout.setGravity(Gravity.CENTER);
                relativeLayout.setLayoutParams(params1);

                // add progress bar
                ProgressBar instrumentBtn = new ProgressBar(InstrumentSingActivity.this, null, android.R.attr.progressBarStyleHorizontal);
                RelativeLayout.LayoutParams params2 = new RelativeLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                );
                params2.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
                instrumentBtn.setProgressBackgroundTintList(ColorStateList.valueOf(getColor(R.color.instrument_btn_background)));
                instrumentBtn.setIndeterminateDrawable(ContextCompat.getDrawable(InstrumentSingActivity.this, R.drawable.custom_instrument_button));
                instrumentBtn.setProgressDrawable(ContextCompat.getDrawable(InstrumentSingActivity.this, R.drawable.custom_instrument_button));
                instrumentBtn.setLayoutParams(params2);
                instrumentBtn.setMax(100);
                instrumentBtn.setProgress(0);
                // fixme: probably set in progress monitor?
                instrumentBtn.setOnClickListener(v -> {
                            chordPlayer.play(chord.getSoundId(), chordVolume, chordVolume, 1, 0, 1);
                            userSequence.add(new PlayChordRecord(chord, currentPosition));
                        }
                );
                chordToBtn.put(chord, instrumentBtn);
                relativeLayout.addView(instrumentBtn);

                // add particle system view
                ParticleSystemView particleSystemView = new ParticleSystemView(this);
                particleSystemView.setLayoutParams(params2);
                chordToPSV.put(chord, particleSystemView);
                relativeLayout.addView(particleSystemView);

                // add text view
                AutofitTextView chordLabel = new AutofitTextView(InstrumentSingActivity.this);
                RelativeLayout.LayoutParams params3 = new RelativeLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
                params3.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
                chordLabel.setText(chord.getName());
                chordLabel.setTextColor(getColor(R.color.instrument_chord_label));
                chordLabel.setTypeface(Typeface.DEFAULT_BOLD);
                chordLabel.setGravity(Gravity.CENTER);
                chordLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
                chordLabel.setMaxLines(1);
                chordLabel.setSizeToFit();

                chordLabel.setLayoutParams(params3);
                relativeLayout.addView(chordLabel);

                btnContainer.addView(relativeLayout);

                // add space
                if (chordIt.hasNext()) {
                    Space space = new Space(InstrumentSingActivity.this);
                    LinearLayout.LayoutParams params4 = new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            1.0f
                    );
                    space.setLayoutParams(params4);
                    btnContainer.addView(space);
                }
            }
        });

    }

    private void initAccompanyPlayer() {
        accompanyPlayer = new SimpleExoPlayer.Builder(this).build();
        loadAudioFileAndPrepareExoPlayer(this, accompanyPlayer, getAccompanyFullPath(songName));

        runOnUiThread(() -> accompanyPlayer.seekTo(playbackStartTime));
    }

    private Integer getHintTime(Integer time) {
        return time - HINT_DURATION;
    }

    private void initRatingSystem() {
        ratingThread = new ArrayList<>();
        init(getRateFullPath(songName), PCM_SPLIT_INTERVAL, RECORD_DELAY_LB, RECORD_DELAY_UB);
    }

    private void initOnCompleteListener() {
        completionListener = new Runnable() {
            @Override
            public void run() {
                if (currentPosition > finishTime) {
                    finishButton.callOnClick();
                    return;
                }
                handler.postDelayed(this, 200);
            }
        };
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void initLrcView() {
        lrcView = findViewById(R.id.lrcRoller);
        lrcView.setHighLineColor(ContextCompat.getColor(getApplicationContext(), R.color.instrument_lyric));
        try {
            InputStream is = new FileInputStream(getLyricInstrumentFullPath(songName));

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

    private void initVoiceRecorder() {
        voiceRecorder = AudioRecorder.getInstance();
        voiceRecorder.createDefaultAudio(songName, playbackStartTime % PCM_SPLIT_INTERVAL);
    }

    private void initScore() {
        score = new Score();
    }

    private void displayHint(int startTime, PlayChordRecord hintChord) {

        new Thread(() -> {
            ProgressBar progressBar = chordToBtn.get(hintChord.getChord());
            ParticleSystemView particleSystemView = chordToPSV.get(hintChord.getChord());
            ParticleSystem particleSystem = particleSystemView.createParticleSystem();

            final int height = progressBar.getMeasuredHeight();
            final int width = progressBar.getMeasuredWidth();

            initParticleSystem(particleSystem, width / 2, 0);

            int hintFinishTime = hintChord.getTime();
            particleSystem.start();
            while (currentPosition < hintFinishTime && !(state == State.UNSTARTED)) {
                int percentage = (currentPosition - startTime) / (HINT_DURATION / 100);

                progressBar.setProgress(percentage);
                particleSystem.setPtcPosition(width / 2, height * percentage / 100);
            }

            if (currentHint != null) {
                currentHint.setProgress(0);
                currentPtc.stop();
            }

            progressBar.setProgress(100);

            currentHint = progressBar;
            currentPtc = particleSystem;
        }).start();
    }

    private void initParticleSystem(ParticleSystem ptcSys, int x, int y) {
        BitmapDrawable drawable = (BitmapDrawable) getResources().getDrawable(R.drawable.ptc16);
        Bitmap img = drawable.getBitmap();

        ptcSys.setPtcBlend(1);
        ptcSys.setFps(40);
        ptcSys.setPps(30);
        ptcSys.setPtcImage(img);
        ptcSys.setPtcPosition(x, y);
        ParticleSystemConfig config = new ParticleSystemConfig();
        config.duration.set(1000, 0);
        config.theta.set(270, 15);
        config.startVelocity.set(400, 0);
        config.endVelocity.set(400, 0);
        config.startAngularRate.set(0, 0);
        config.endAngularRate.set(0, 0);
        config.startSpinRate.set(360, 0);
        config.endSpinRate.set(360, 0);
        config.startScale.set(1, 0);
        config.endScale.set(1.5f, 0);
        config.startAlpha.set(1, 0);
        config.endAlpha.set(0.75f, 0);
        config.startRed.set(1, 0);
        config.endRed.set(1, 0);
        config.startGreen.set(0, 0);
        config.endGreen.set(1, 0);
        config.startBlue.set(0, 0);
        config.endBlue.set(0, 0);
        ptcSys.setConfig(config);
    }

    private void startAllPlayers() {
        accompanyPlayer.play();
        chordPlayer.autoResume();
    }

    private void start() {
        state = State.PLAYING;

        handler.postDelayed(progressMonitor, 0);
        handler.postDelayed(completionListener, 0);
        handler.postDelayed(recordMonitor, 0);
        handler.postDelayed(hintMonitor, 0);

        voiceRecorder.startRecord(null);
        startAllPlayers();
    }

    private void pauseAllPlayers() {
        accompanyPlayer.pause();
        chordPlayer.autoPause();
    }

    private void pause() {
        state = State.PAUSE;

        handler.removeCallbacks(progressMonitor);
        handler.removeCallbacks(completionListener);
        handler.removeCallbacks(recordMonitor);
        handler.removeCallbacks(hintMonitor);

        voiceRecorder.pauseRecord();
        pauseAllPlayers();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void retry() {
        stopActivity(false);
        onStart();
    }

    private void stopActivity(boolean shouldMergePcm) {
        this.state = State.UNSTARTED;

        handler.removeCallbacks(completionListener);
        handler.removeCallbacks(progressMonitor);
        handler.removeCallbacks(recordMonitor);
        handler.removeCallbacks(hintMonitor);

        terminateExoPlayer(this, accompanyPlayer);

        voiceRecorder.stopRecord(shouldMergePcm);
        if (shouldMergePcm) {
            mergeUserChords(userSequence);
        }
        lrcView.alertPlayerReleased();
        resetProgressBar();
    }

    private void resetProgressBar() {
        for (ProgressBar progressBar: chordToBtn.values()) {
            progressBar.setProgress(0);
        }
    }

    private void mergeUserChords(List<PlayChordRecord> userSequence) {
        int duration = finishTime - playbackStartTime;
        String destPath = getUserPlayFullPath(songName);
        String silencePath = getUserPlayFullPath("silence");
        int labelId = 1;
        int volumeMultiply = userSequence.size() + 1;
        // generate silent audio
        StringBuilder command = new StringBuilder("-f lavfi -i anullsrc -t ");

        command.append(duration).append("ms ").append(silencePath);
        FFmpeg.execute(command.toString());

        command = new StringBuilder("-y ");

        command.append("-i ").append(silencePath).append(" ");

        System.out.println("===== user chord =====");
        for (PlayChordRecord userRecord: userSequence) {
            userRecord.decrementTime(playbackStartTime);
            System.out.println(userRecord.getTime());
            command.append("-i ").append(userRecord.getChord().getFilePath()).append(" ");
        }

        command.append("-filter_complex \"");
        command.append(String.format(Locale.CHINA, "[0]volume=%d[00];", volumeMultiply));
        for (PlayChordRecord userRecord: userSequence) {
            int time = userRecord.getTime();
            command.append(String.format(Locale.CHINA, "[%d]volume=%d,adelay=%d|%d[0%d];",
                    labelId, volumeMultiply, time, time, labelId));
            ++labelId;
        }
        for (int i = 0; i < labelId; ++i) {
            command.append(String.format(Locale.CHINA, "[0%d]", i));
        }
        command.append("amix=inputs=").append(userSequence.size() + 1);
        command.append(":duration=longest:dropout_transition=1000000\" ");
        command.append(destPath);

        FFmpeg.execute(command.toString());

        deleteOneFile(silencePath);
    }

    /**
     * Rate the record in a given interval
     * @param startTime Starting time of the line (ms)
     * @param endTime End time of the line (ms)
     */
    private void rate(int startTime, int endTime) {
        Thread thread = new Thread(() -> {
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

            // fixme: change
            showSuccessToast(InstrumentSingActivity.this, Integer.toString(scores[0]));
        });

        ratingThread.add(thread);
        thread.start();
    }

    private enum State {
        PAUSE, PLAYING, UNSTARTED
    }
}