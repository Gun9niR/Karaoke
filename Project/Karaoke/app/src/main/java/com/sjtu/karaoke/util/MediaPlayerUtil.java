package com.sjtu.karaoke.util;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;

import java.io.IOException;

public class MediaPlayerUtil {
    public static void terminateMediaPlayer(MediaPlayer mediaPlayer) {
        if (mediaPlayer != null) {
            mediaPlayer.reset();
            mediaPlayer.release();
        }
    }

    public static void loadAndPrepareMediaplayer(Context context, MediaPlayer mediaPlayer, String fileName) {
        // todo: change to load from local storage, discard this method
        if (mediaPlayer == null) {
            return;
        }

        AssetFileDescriptor afd = null;
        try {
            afd = context.getAssets().openFd(fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            mediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
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
     * Set the data source of a media player and put it to prepared state
     *
     * @param mediaPlayer
     * @param fullPath
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
}
