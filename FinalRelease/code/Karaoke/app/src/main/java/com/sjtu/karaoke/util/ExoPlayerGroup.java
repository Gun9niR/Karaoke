package com.sjtu.karaoke.util;

import com.google.android.exoplayer2.SimpleExoPlayer;

/*
 * @ClassName: ExoPlayerGroup
 * @Author: 郭志东
 * @Date: 2021/6/5
 * @Description: 播放器组接口。由于在演唱结果页的播放器数量不定，而且有多个播放器，播放进度不同步（需要对齐人声），
 * 所以用一个接口统一管理
 */
public interface ExoPlayerGroup {
    void initPlayers(String songName);

    // 无论是哪个模式，都有录音播放器
    SimpleExoPlayer getVoicePlayer();

    // 设置人声对齐
    void setVoiceOffset(int voiceOffset);

    // 由于播放器播放时还会产生一个误差，所以真实的人声提前量和通过拖动进度条产生的voiceOffset不同
    void setActualOffset();

    void startAllPlayers();

    void pauseAllPlayers();

    void terminateAllPlayers();

    int getCurrentPosition();

    int getDuration();

    void seekTo(int position);

    boolean isPlaying();

    // 将所有播放器播放的音频合成为最终的作品
    void mergeWav(String destPath);
}
