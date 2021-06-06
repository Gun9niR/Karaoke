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

    // 自弹自唱模式的临时文件
    public static final String TEMPORARY_DIRECTORY = ROOT_DIRECTORY + "temporary/";

    // 将asset中的单音文件提取到该目录中
    public static final String ASSET_DIRECTORY = TEMPORARY_DIRECTORY + "assets/";

    // 将由单音文件合成而成的和弦文件保存到该目录
    public static final String CHORD_WAV_DIRECTORY = TEMPORARY_DIRECTORY + "chords/";

    // 储存用户弹奏产生的音频文件
    public static final String USER_PLAY_DIRECTORY = TEMPORARY_DIRECTORY + "user_play/";

    // 应用包名
    public static final String PACKAGE_NAME = "com.sjtu.karaoke";

    // 分享权限
    public static final String AUTHORITY = PACKAGE_NAME + ".fileprovider";

    // 发送请求的地址
    public static final String SERVER_IP = "https://jyzkaraoke.cn1.utools.club";

    // 下载文件的根URL
    public static final String GET_FILE_ROOT_URL = SERVER_IP + "/getFile";

    // 获取歌曲信息
    public static final String GET_SONG_INFO_URL = SERVER_IP + "/getSongInfo";

    // 下载专辑封面
    public static final String GET_ALBUM_COVER_URL = GET_FILE_ROOT_URL + "/album";

    // 下载原唱
    public static final String GET_ORIGINAL_URL = GET_FILE_ROOT_URL + "/original";

    // 下载伴奏
    public static final String GET_ACCOMPANY_URL = GET_FILE_ROOT_URL + "/accompany";

    // 下载鼓点伴奏
    public static final String GET_DRUM_URL = GET_FILE_ROOT_URL + "/drum";

    // 下载贝斯伴奏
    public static final String GET_BASS_URL = GET_FILE_ROOT_URL + "/bass";

    // 下载管弦伴奏
    public static final String GET_ORCHESTRA_URL = GET_FILE_ROOT_URL + "/orchestra";

    // 下载伴奏演唱模式下的歌词
    public static final String GET_LYRIC_URL = GET_FILE_ROOT_URL + "/lyric_accompany";

    // 下载自弹自唱模式下的歌词
    public static final String GET_LYRIC_INSTRUMENT_URL = GET_FILE_ROOT_URL + "/lyric_instrument";

    // 下载伴奏演唱模式的打分文件
    public static final String GET_RATE_URL = GET_FILE_ROOT_URL + "/rate";

    // 下载mv
    public static final String GET_MV_URL = GET_FILE_ROOT_URL + "/mv";

    // 下载和弦文件
    public static final String GET_CHORD_URL = GET_FILE_ROOT_URL + "/chord";

    // 支持分享录音文件到的app：qq、微信、TIM
    public static final String[] PACKAGES_FOR_SHARING = {
            "com.tencent.mobileqq",
            "com.tencent.mm",
            "com.tencent.tim"
    };
}