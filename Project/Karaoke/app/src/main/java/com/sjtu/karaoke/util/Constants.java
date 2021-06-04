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

    public static final int PROGRESS_UPDATE_INTERVAL = 10;

    public static final double RECORD_DELAY_LB = 0.2;

    public static final double RECORD_DELAY_UB = 0.9;
    @SuppressLint("SdCardPath")
    public static final String ROOT_DIRECTORY = "/data/data/com.sjtu.karaoke/";

    public static final String VOICE_DIRECTORY = ROOT_DIRECTORY + "wav/";

    public static final String PCM_DIRECTORY = ROOT_DIRECTORY + "pcm/";

    public static final String ACCOMPANY_DIRECTORY = ROOT_DIRECTORY + "accompany/";

    public static final String ACCOMPANY_INSTRUMENT_DIRECTORY = ROOT_DIRECTORY + "i_accompany/";

    public static final String DRUM_DIRECTORY = ACCOMPANY_INSTRUMENT_DIRECTORY + "drum/";

    public static final String BASS_DIRECTORY = ACCOMPANY_INSTRUMENT_DIRECTORY + "bass/";

    public static final String ORCHESTRA_DIRECTORY = ACCOMPANY_INSTRUMENT_DIRECTORY + "orchestra/";

    public static final String RECORD_DIRECTORY = ROOT_DIRECTORY + "record/";

    public static final String ORIGINAL_DIRECTORY = ROOT_DIRECTORY + "original/";

    public static final String RATE_DIRECTORY = ROOT_DIRECTORY + "rating/";

    public static final String TRIMMED_VOICE_WAV_DIRECTORY = VOICE_DIRECTORY + "trimmed_voice/";

    public static final String ALBUM_COVER_DIRECTORY = ROOT_DIRECTORY + "album_cover/";

    public static final String LYRIC_DIRECTORY = ROOT_DIRECTORY + "lyric/";

    public static final String LYRIC_INSTRUMENT_DIRECTORY = ROOT_DIRECTORY + "i_lyric/";

    public static final String MV_DIRECTORY = ROOT_DIRECTORY + "mv/";

    public static final String RATERDATA_DIRECTORY = ROOT_DIRECTORY + "/raterdata";

    public static final String CHORD_TRANS_DIRECTORY = ROOT_DIRECTORY + "chord/";

    public static final String TEMPORARY_DIRECTORY = ROOT_DIRECTORY + "temporary/";

    public static final String ASSET_DIRECTORY = TEMPORARY_DIRECTORY + "assets/";

    public static final String CHORD_WAV_DIRECTORY = TEMPORARY_DIRECTORY + "chords/";

    public static final String USER_PLAY_DIRECTORY = TEMPORARY_DIRECTORY + "user_play/";

    // package data
    public static final String PACKAGE_NAME = "com.sjtu.karaoke";

    public static final String AUTHORITY = PACKAGE_NAME + ".fileprovider";

    // request URL
    public static final String SERVER_IP = "http://10.163.80.67:5000";

    public static final String GET_FILE_ROOT_URL = SERVER_IP + "/getFile";

    public static final String GET_SONG_INFO_URL = SERVER_IP + "/getSongInfo";

    public static final String GET_ALBUM_COVER_URL = GET_FILE_ROOT_URL + "/album";

    public static final String GET_ORIGINAL_URL = GET_FILE_ROOT_URL + "/original";

    public static final String GET_ACCOMPANY_URL = GET_FILE_ROOT_URL + "/accompany";

    public static final String GET_DRUM_URL = GET_FILE_ROOT_URL + "/drum";

    public static final String GET_BASS_URL = GET_FILE_ROOT_URL + "/bass";

    public static final String GET_ORCHESTRA_URL = GET_FILE_ROOT_URL + "/orchestra";

    public static final String GET_LYRIC_URL = GET_FILE_ROOT_URL + "/lyric_accompany";

    public static final String GET_LYRIC_INSTRUMENT_URL = GET_FILE_ROOT_URL + "/lyric_instrument";

    public static final String GET_RATE_URL = GET_FILE_ROOT_URL + "/rate";

    public static final String GET_MV_URL = GET_FILE_ROOT_URL + "/mv";

    public static final String GET_CHORD_URL = GET_FILE_ROOT_URL + "/chord";

    public static final String[] PACKAGES_FOR_SHARING = {
            "com.tencent.mobileqq",
            "com.tencent.mm",
            "com.tencent.tim"
    };
}