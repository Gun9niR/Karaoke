package com.sjtu.karaoke.util;

import com.google.android.exoplayer2.SimpleExoPlayer;

public interface ExoPlayerGroup {
    void initPlayers(String songName);

    SimpleExoPlayer getVoicePlayer();

    void setVoiceOffset(int voiceOffset);

    void setActualOffset();

    void startAllPlayers();

    void pauseAllPlayers();

    void terminateAllPlayers();

    int getCurrentPosition();

    int getDuration();

    void seekTo(int position);

    boolean isPlaying();

    void mergeWav(String destPath);
}
