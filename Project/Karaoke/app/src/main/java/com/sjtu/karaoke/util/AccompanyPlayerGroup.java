package com.sjtu.karaoke.util;

import android.app.Activity;
import android.os.Handler;

import com.google.android.exoplayer2.SimpleExoPlayer;

import java.util.ArrayList;
import java.util.List;

import static com.sjtu.karaoke.util.MediaPlayerUtil.loadAudioFileAndPrepareExoPlayer;
import static com.sjtu.karaoke.util.MediaPlayerUtil.terminateExoPlayer;
import static com.sjtu.karaoke.util.PathUtil.getAccompanyFullPath;
import static com.sjtu.karaoke.util.PathUtil.getTrimmedAccompanyFullPath;
import static com.sjtu.karaoke.util.PathUtil.getVoiceFullPath;
import static com.sjtu.karaoke.util.WavUtil.getWAVDuration;
import static com.sjtu.karaoke.util.WavUtil.mergeWAVs;
import static com.sjtu.karaoke.util.WavUtil.trimWav;

public class AccompanyPlayerGroup implements ExoPlayerGroup {
    private final Activity activity;

    private final String songName;
    private int duration;
    private int voiceOffset;
    private int actualOffset;

    private SimpleExoPlayer voicePlayer;
    private SimpleExoPlayer accompanyPlayer;

    public AccompanyPlayerGroup(Activity activity, String songName, int voiceOffset) {
        this.activity = activity;
        this.voiceOffset = voiceOffset;
        this.songName = songName;
        initPlayers(songName);
    }

    @Override
    public void initPlayers(String songName) {
        initVoicePlayer(songName);
        initAccompanyPlayer(songName);
    }

    private void initVoicePlayer(String songName) {
        String voiceFullPath = getVoiceFullPath(songName);
        voicePlayer = new SimpleExoPlayer.Builder(activity).build();
        loadAudioFileAndPrepareExoPlayer(activity, voicePlayer, getVoiceFullPath(songName));
        duration = (int) getWAVDuration(voiceFullPath);
        voicePlayer.seekTo(voiceOffset);
    }

    private void initAccompanyPlayer(String songName) {
        String trimmedAccompanyFullPath = getTrimmedAccompanyFullPath(songName);
        trimWav(getAccompanyFullPath(songName), trimmedAccompanyFullPath, 0, duration);
        accompanyPlayer = new SimpleExoPlayer.Builder(activity).build();
        loadAudioFileAndPrepareExoPlayer(activity, accompanyPlayer, trimmedAccompanyFullPath);
    }

    public SimpleExoPlayer getVoicePlayer() {
        return voicePlayer;
    }

    public SimpleExoPlayer getAccompanyPlayer() {
        return accompanyPlayer;
    }

    @Override
    public void setVoiceOffset(int voiceOffset) {
        this.voiceOffset = voiceOffset;

        long accompanyPosition = accompanyPlayer.getCurrentPosition();
        long newPosition = accompanyPosition + voiceOffset;
        voicePlayer.seekTo(newPosition < duration ? newPosition : duration);
    }

    @Override
    public void setActualOffset() {
        Handler handler = new Handler();
        handler.postDelayed(() -> {
            if (voicePlayer.isPlaying()) {
                actualOffset = (int) voicePlayer.getCurrentPosition() - (int) accompanyPlayer.getCurrentPosition();
            } else {
                actualOffset = voiceOffset;
            }
        }, 500);
    }

    @Override
    public void startAllPlayers() {
        voicePlayer.play();
        accompanyPlayer.play();
    }

    @Override
    public void pauseAllPlayers() {
        voicePlayer.pause();
        accompanyPlayer.pause();
    }

    @Override
    public void terminateAllPlayers() {
        terminateExoPlayer(activity, voicePlayer);
        terminateExoPlayer(activity, accompanyPlayer);
    }

    @Override
    public int getCurrentPosition() {
        return (int) accompanyPlayer.getCurrentPosition();
    }

    @Override
    public int getDuration() {
        return duration;
    }

    @Override
    public void seekTo(int position) {
        accompanyPlayer.seekTo(position);
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

        accompanyPaths.add(getTrimmedAccompanyFullPath(songName));
        accompanyVolumes.add(accompanyPlayer.getVolume());

        mergeWAVs(destPath, getVoiceFullPath(songName), voicePlayer.getVolume(), accompanyPaths, accompanyVolumes, actualOffset);
    }
}
