package com.sjtu.karaoke.util;

import android.Manifest;
import android.annotation.SuppressLint;

public class Constants {
    // 权限
    public static final int GET_RECORD_AUDIO = 1;
    public static final String[] PERMISSIONS_RECORDER = {
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
    };

    // 播放进度更新间隔
    public static final int PROGRESS_UPDATE_INTERVAL = 10;

    // 录音延迟下界
    public static final double RECORD_DELAY_LB = 0.2;

    // 录音延迟上界
    public static final double RECORD_DELAY_UB = 0.9;

    // 项目数据的根目录
    @SuppressLint("SdCardPath")
    public static final String ROOT_DIRECTORY = "/data/data/com.sjtu.karaoke/";

    // 临时合成的录音目录
    public static final String VOICE_DIRECTORY = ROOT_DIRECTORY + "wav/";

    // 录音时写入的pcm文件目录
    public static final String PCM_DIRECTORY = ROOT_DIRECTORY + "pcm/";

    // 伴奏文件目录
    public static final String ACCOMPANY_DIRECTORY = ROOT_DIRECTORY + "accompany/";

    // 自弹自唱模式的伴奏，本目录下不存储文件，只有以下3个目录
    public static final String ACCOMPANY_INSTRUMENT_DIRECTORY = ROOT_DIRECTORY + "i_accompany/";

    // 鼓点文件目录
    public static final String DRUM_DIRECTORY = ACCOMPANY_INSTRUMENT_DIRECTORY + "drum/";

    // 贝斯文件目录
    public static final String BASS_DIRECTORY = ACCOMPANY_INSTRUMENT_DIRECTORY + "bass/";

    // 管弦文件目录
    public static final String ORCHESTRA_DIRECTORY = ACCOMPANY_INSTRUMENT_DIRECTORY + "orchestra/";

    // 最后录音保存到的目录
    public static final String RECORD_DIRECTORY = ROOT_DIRECTORY + "record/";

    // 原唱（无伴奏）文件目录
    public static final String ORIGINAL_DIRECTORY = ROOT_DIRECTORY + "original/";

    // 打分文件目录
    public static final String RATE_DIRECTORY = ROOT_DIRECTORY + "rating/";

    // 在一个唱段需要打分时，将pcm文件转化为wav文件，保存在此目录下
    public static final String TRIMMED_VOICE_WAV_DIRECTORY = VOICE_DIRECTORY + "trimmed_voice/";

    // 专辑封面目录
    public static final String ALBUM_COVER_DIRECTORY = ROOT_DIRECTORY + "album_cover/";

    // 伴奏演唱歌词文件目录
    public static final String LYRIC_DIRECTORY = ROOT_DIRECTORY + "lyric/";

    // 自弹自唱歌词文件目录
    public static final String LYRIC_INSTRUMENT_DIRECTORY = ROOT_DIRECTORY + "i_lyric/";

    // MV目录
    public static final String MV_DIRECTORY = ROOT_DIRECTORY + "mv/";

    // 打分系统临时文件目录
    public static final String RATERDATA_DIRECTORY = ROOT_DIRECTORY + "/raterdata";

    // 和弦文件目录
    public static final String CHORD_TRANS_DIRECTORY = ROOT_DIRECTORY + "chord/";

    //
    public static final String TEMPORARY_DIRECTORY = ROOT_DIRECTORY + "temporary/";

    public static final String ASSET_DIRECTORY = TEMPORARY_DIRECTORY + "assets/";

    public static final String CHORD_WAV_DIRECTORY = TEMPORARY_DIRECTORY + "chords/";

    public static final String USER_PLAY_DIRECTORY = TEMPORARY_DIRECTORY + "user_play/";

    // package data
    public static final String PACKAGE_NAME = "com.sjtu.karaoke";

    public static final String AUTHORITY = PACKAGE_NAME + ".fileprovider";

    // request URL
    public static final String SERVER_IP = "http://192.168.1.92:5000";

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