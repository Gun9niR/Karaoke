package com.sjtu.karaoke.util;

import android.app.Activity;
import android.media.MediaPlayer;
import android.net.Uri;

import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.SimpleExoPlayer;

import java.io.File;
import java.io.IOException;

public class MediaPlayerUtil {
    /**
     * 封装MediaPlayer的reset和release方法，减
     * @param mediaPlayer 需要释放的MediaPlayer
     */
    public static void terminateMediaPlayer(MediaPlayer mediaPlayer) {
        if (mediaPlayer != null) {
            mediaPlayer.reset();
            mediaPlayer.release();
        }
    }

    /**
     * 为MediaPlayer设置数据源并将其状态设为准备
     * @param mediaPlayer 需要设置的MediaPlayer
     * @param fullPath 音频的绝对路径
     */
    public static void loadFileAndPrepareMediaPlayer(MediaPlayer mediaPlayer, String fullPath) {
        if (mediaPlayer == null) {
            return;
        }

        try {
            mediaPlayer.setDataSource(fullPath);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            mediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 为SimpleExoPlayer设置数据源并将其状态设为准备
     * @param activity 调用SimpleExoPlayer的Acitivity
     * @param exoPlayer 需要设置的播放器
     * @param fullPath 音频绝对路径
     */
    public static void loadAudioFileAndPrepareExoPlayer(Activity activity, SimpleExoPlayer exoPlayer, String fullPath) {
        File audioFile = new File(fullPath);
        MediaItem mediaItem = MediaItem.fromUri(Uri.fromFile(audioFile));
        activity.runOnUiThread(() -> {
            exoPlayer.setMediaItem(mediaItem);
            exoPlayer.prepare();
        });
    }

    /**
     * 释放SimpleExoPlayer
     * @param activity 调用SimpleExoPlayer的Activity
     * @param player 要释放的播放器
     */
    public static void terminateExoPlayer(Activity activity, SimpleExoPlayer player) {
        if (player != null) {
            activity.runOnUiThread(player::release);
        }
    }
}
