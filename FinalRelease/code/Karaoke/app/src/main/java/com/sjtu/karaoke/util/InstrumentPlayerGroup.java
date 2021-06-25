package com.sjtu.karaoke.util;

import android.app.Activity;
import android.os.Handler;

import com.google.android.exoplayer2.SimpleExoPlayer;

import java.util.ArrayList;
import java.util.List;

import static com.sjtu.karaoke.util.MediaPlayerUtil.loadAudioFileAndPrepareExoPlayer;
import static com.sjtu.karaoke.util.MediaPlayerUtil.terminateExoPlayer;
import static com.sjtu.karaoke.util.PathUtil.getBassFullPath;
import static com.sjtu.karaoke.util.PathUtil.getDrumFullPath;
import static com.sjtu.karaoke.util.PathUtil.getOrchestraFullPath;
import static com.sjtu.karaoke.util.PathUtil.getUserPlayFullPath;
import static com.sjtu.karaoke.util.PathUtil.getVoiceFullPath;
import static com.sjtu.karaoke.util.WavUtil.getWAVDuration;
import static com.sjtu.karaoke.util.WavUtil.mergeWAVs;

/*
 * @ClassName: InstrumentPlayerGroup
 * @Author: 郭志东
 * @Date: 2021/6/5
 * @Description: 自弹自唱模式的播放器组，播放录音、用户弹奏的钢琴、鼓点、贝斯、管弦
 */
public class InstrumentPlayerGroup implements ExoPlayerGroup {
    private final Activity activity;

    private final String songName;
    private int duration;
    private int voiceOffset;
    private int actualOffset;

    private SimpleExoPlayer voicePlayer;
    private SimpleExoPlayer pianoPlayer;
    private SimpleExoPlayer drumPlayer;
    private SimpleExoPlayer bassPlayer;
    private SimpleExoPlayer orchestraPlayer;

    public InstrumentPlayerGroup(Activity activity, String songName, int voiceOffset) {
        this.activity = activity;
        this.voiceOffset = voiceOffset;
        this.songName = songName;
        initPlayers(songName);
    }

    @Override
    public void initPlayers(String songName) {
        initVoicePlayer(songName);
        initPianoPlayer(songName);
        initDrumPlayer(songName);
        initBassPlayer(songName);
        initOrchestraPlayer(songName);
    }

    private void initVoicePlayer(String songName) {
        String voiceFullPath = getVoiceFullPath(songName);
        voicePlayer = new SimpleExoPlayer.Builder(activity).build();
        loadAudioFileAndPrepareExoPlayer(activity, voicePlayer, voiceFullPath);
        duration = (int) getWAVDuration(voiceFullPath);
        voicePlayer.seekTo(voiceOffset);
    }

    private void initPianoPlayer(String songName) {
        pianoPlayer = new SimpleExoPlayer.Builder(activity).build();
        loadAudioFileAndPrepareExoPlayer(activity, pianoPlayer, getUserPlayFullPath(songName));
    }

    private void initDrumPlayer(String songName) {
        drumPlayer = new SimpleExoPlayer.Builder(activity).build();
        loadAudioFileAndPrepareExoPlayer(activity, drumPlayer, getDrumFullPath(songName));
        drumPlayer.setVolume(0);
    }

    private void initBassPlayer(String songName) {
        bassPlayer = new SimpleExoPlayer.Builder(activity).build();
        loadAudioFileAndPrepareExoPlayer(activity, bassPlayer, getBassFullPath(songName));
        bassPlayer.setVolume(0);
    }

    private void initOrchestraPlayer(String songName) {
        orchestraPlayer = new SimpleExoPlayer.Builder(activity).build();
        loadAudioFileAndPrepareExoPlayer(activity, orchestraPlayer, getOrchestraFullPath(songName));
        orchestraPlayer.setVolume(0);
    }

    public SimpleExoPlayer getVoicePlayer() {
        return voicePlayer;
    }

    public SimpleExoPlayer getPianoPlayer() {
        return pianoPlayer;
    }

    public SimpleExoPlayer getDrumPlayer() {
        return drumPlayer;
    }

    public SimpleExoPlayer getBassPlayer() {
        return bassPlayer;
    }

    public SimpleExoPlayer getOrchestraPlayer() {
        return orchestraPlayer;
    }

    /**
     * 设置人声提前量
     * @param voiceOffset 单位为毫秒
     */
    @Override
    public void setVoiceOffset(int voiceOffset) {
        this.voiceOffset = voiceOffset;

        int accompanyPosition = getAccompanyPosition();
        int newPosition = accompanyPosition + voiceOffset;
        voicePlayer.seekTo(Math.min(newPosition, duration));
    }

    /**
     * 在用户通过进度条对齐后0.5秒再检测一次进度差，因为进度条的值和实际的进度差不完全相等
     */
    @Override
    public void setActualOffset() {
        Handler handler = new Handler();
        handler.postDelayed(() -> {
            if (voicePlayer.isPlaying()) {
                actualOffset = (int) voicePlayer.getCurrentPosition() - getAccompanyPosition();
            } else {
                actualOffset = voiceOffset;
            }
        }, 500);
    }

    @Override
    public void startAllPlayers() {
        voicePlayer.play();
        pianoPlayer.play();
        drumPlayer.play();
        bassPlayer.play();
        orchestraPlayer.play();
    }

    @Override
    public void pauseAllPlayers() {
        voicePlayer.pause();
        pianoPlayer.pause();
        drumPlayer.pause();
        bassPlayer.pause();
        orchestraPlayer.pause();
    }

    @Override
    public void terminateAllPlayers() {
        terminateExoPlayer(activity, voicePlayer);
        terminateExoPlayer(activity, pianoPlayer);
        terminateExoPlayer(activity, drumPlayer);
        terminateExoPlayer(activity, bassPlayer);
        terminateExoPlayer(activity, orchestraPlayer);
    }

    private int getAccompanyPosition() {
        return (int) pianoPlayer.getCurrentPosition();
    }
    @Override
    public int getCurrentPosition() {
        return getAccompanyPosition();
    }

    @Override
    public int getDuration() {
        return duration;
    }


    @Override
    public void seekTo(int position) {
        pianoPlayer.seekTo(position);
        drumPlayer.seekTo(position);
        bassPlayer.seekTo(position);
        orchestraPlayer.seekTo(position);
        voicePlayer.seekTo(Math.min(position + voiceOffset, duration));
    }

    @Override
    public boolean isPlaying() {
        return voicePlayer.isPlaying();
    }

    @Override
    public void mergeWav(String destPath) {
        List<String> accompanyPaths = new ArrayList<>();
        List<Float> accompanyVolumes = new ArrayList<>();

        accompanyPaths.add(getUserPlayFullPath(songName));
        accompanyPaths.add(getDrumFullPath(songName));
        accompanyPaths.add(getBassFullPath(songName));
        accompanyPaths.add(getOrchestraFullPath(songName));

        accompanyVolumes.add(pianoPlayer.getVolume());
        accompanyVolumes.add(drumPlayer.getVolume());
        accompanyVolumes.add(bassPlayer.getVolume());
        accompanyVolumes.add(orchestraPlayer.getVolume());

        mergeWAVs(destPath, getVoiceFullPath(songName), voicePlayer.getVolume(), accompanyPaths, accompanyVolumes, actualOffset);
    }
}
