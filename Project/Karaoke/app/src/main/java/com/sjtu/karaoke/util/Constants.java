package com.sjtu.karaoke.util;

import android.Manifest;
import android.annotation.SuppressLint;

public class Constants {
    public static final String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    public static final int REQUEST_EXTERNAL_STORAGE = 1;

    public static final int GET_RECORD_AUDIO = 1;

    public static final String[] PERMISSIONS_RECORDER = {
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
    };

    public static final int PROGRESS_UPDATE_INTERVAL = 100;

    public static final double RECORD_DELAY_LB = 0.2;

    public static final double RECORD_DELAY_UB = 0.9;
    @SuppressLint("SdCardPath")
    public static final String ROOT_DIRECTORY = "/data/data/com.sjtu.karaoke/";
    // public static final String ROOT_DIRECTORY = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) + FILE_SAVE_DIR;

    public static final String WAV_DIRECTORY = ROOT_DIRECTORY + "wav/";

    public static final String PCM_DIRECTORY = ROOT_DIRECTORY + "pcm/";

    public static final String ACCOMPANY_DIRECTORY = ROOT_DIRECTORY + "accompany/";

    public static final String RECORD_DIRECTORY = ROOT_DIRECTORY + "record/";

    public static final String ORIGINAL_DIRECTORY = ROOT_DIRECTORY + "original/";

    public static final String RATE_DIRECTORY = ROOT_DIRECTORY + "rating/";

    public static final String TRIMMED_VOICE_WAV_DIRECTORY = WAV_DIRECTORY + "trimmed_voice/";

    public static final String ALBUM_COVER_DIRECTORY = ROOT_DIRECTORY + "album_cover/";

    public static final String LYRIC_DIRECTORY = ROOT_DIRECTORY + "lyric/";

    public static final String MV_DIRECTORY = ROOT_DIRECTORY + "mv/";

    public static final String PACKAGE_NAME = "com.sjtu.karaoke";

    public static final String AUTHORITY = PACKAGE_NAME + ".fileprovider";

    // request URL
    public static final String SERVER_IP = "http://172.20.10.13:5000";

    public static final String GET_FILE_ROOT_URL = SERVER_IP + "/getFile";

    public static final String GET_SONG_INFO_URL = SERVER_IP + "/getSongInfo";

    public static final String GET_ALBUM_COVER_URL = GET_FILE_ROOT_URL + "/album";

    public static final String GET_ORIGINAL_URL = GET_FILE_ROOT_URL + "/original";

    public static final String GET_ACCOMPANY_URL = GET_FILE_ROOT_URL + "/accompany_accompany";

    public static final String GET_LYRIC_URL = GET_FILE_ROOT_URL + "/lyric";

    public static final String GET_RATE_URL = GET_FILE_ROOT_URL + "/rate";

    public static final String GET_MV_URL = GET_FILE_ROOT_URL + "/mv";

    public static final String[] PACKAGES_FOR_SHARING = {
            "com.tencent.mobileqq",
            "com.tencent.mm",
            "com.tencent.tim"
    };
}