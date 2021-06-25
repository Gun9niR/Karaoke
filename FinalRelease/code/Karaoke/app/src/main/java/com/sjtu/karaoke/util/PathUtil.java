package com.sjtu.karaoke.util;

import android.content.Context;

import com.dreamfish.record.FileUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static com.sjtu.karaoke.util.Constants.ACCOMPANY_DIRECTORY;
import static com.sjtu.karaoke.util.Constants.ACCOMPANY_INSTRUMENT_DIRECTORY;
import static com.sjtu.karaoke.util.Constants.ALBUM_COVER_DIRECTORY;
import static com.sjtu.karaoke.util.Constants.ASSET_DIRECTORY;
import static com.sjtu.karaoke.util.Constants.BASS_DIRECTORY;
import static com.sjtu.karaoke.util.Constants.CHORD_TRANS_DIRECTORY;
import static com.sjtu.karaoke.util.Constants.CHORD_WAV_DIRECTORY;
import static com.sjtu.karaoke.util.Constants.DRUM_DIRECTORY;
import static com.sjtu.karaoke.util.Constants.LYRIC_DIRECTORY;
import static com.sjtu.karaoke.util.Constants.LYRIC_INSTRUMENT_DIRECTORY;
import static com.sjtu.karaoke.util.Constants.MV_DIRECTORY;
import static com.sjtu.karaoke.util.Constants.ORCHESTRA_DIRECTORY;
import static com.sjtu.karaoke.util.Constants.ORIGINAL_DIRECTORY;
import static com.sjtu.karaoke.util.Constants.PCM_DIRECTORY;
import static com.sjtu.karaoke.util.Constants.RATE_DIRECTORY;
import static com.sjtu.karaoke.util.Constants.RECORD_DIRECTORY;
import static com.sjtu.karaoke.util.Constants.ROOT_DIRECTORY;
import static com.sjtu.karaoke.util.Constants.TEMPORARY_DIRECTORY;
import static com.sjtu.karaoke.util.Constants.TRIMMED_VOICE_WAV_DIRECTORY;
import static com.sjtu.karaoke.util.Constants.USER_PLAY_DIRECTORY;
import static com.sjtu.karaoke.util.Constants.VOICE_DIRECTORY;
import static com.sjtu.karaoke.util.FileUtil.isFilePresent;

public class PathUtil {

    /**
     * 在MainActivity中调用，创建所有目录
     */
    public static void makeDirectories() {
        List<File> dirs = new ArrayList<>();
        // 保存文件的根目录
        dirs.add(new File(ROOT_DIRECTORY));
        // 录音结果，由pcm合成
        dirs.add(new File(VOICE_DIRECTORY));
        // 录音过程中产生的pcm文件
        dirs.add(new File(PCM_DIRECTORY));
        // 伴奏和合成最终结果时被截短的伴奏
        dirs.add(new File(ACCOMPANY_DIRECTORY));
        // 伴奏和录音合成结果
        dirs.add(new File(RECORD_DIRECTORY));
        // 原唱
        dirs.add(new File(ORIGINAL_DIRECTORY));
        // 打分文件
        dirs.add(new File(RATE_DIRECTORY));
        // 录音过程中一句话的pcm转换成wav的结果
        dirs.add(new File(TRIMMED_VOICE_WAV_DIRECTORY));
        // 专辑封面
        dirs.add(new File(ALBUM_COVER_DIRECTORY));
        // 伴奏演唱模式歌词文件
        dirs.add(new File(LYRIC_DIRECTORY));
        // 自弹自唱模式歌词文件
        dirs.add(new File(LYRIC_INSTRUMENT_DIRECTORY));
        // MV
        dirs.add(new File(MV_DIRECTORY));
        // 和弦文件
        dirs.add(new File(CHORD_TRANS_DIRECTORY));
        // 临时文件目录
        dirs.add(new File(TEMPORARY_DIRECTORY));
        // asset文件
        dirs.add(new File(ASSET_DIRECTORY));
        // 和弦合成文件
        dirs.add(new File(CHORD_WAV_DIRECTORY));
        // 用户弹奏文件
        dirs.add(new File(USER_PLAY_DIRECTORY));
        // 自弹自唱伴奏
        dirs.add(new File(ACCOMPANY_INSTRUMENT_DIRECTORY));
        // drum
        dirs.add(new File(DRUM_DIRECTORY));
        // bass
        dirs.add(new File(BASS_DIRECTORY));
        // orchestra
        dirs.add(new File(ORCHESTRA_DIRECTORY));

        for (File dir : dirs) {
            if (!dir.exists()) {
                dir.mkdirs();
            }
        }

        FileUtil.setBaseDirectories(ROOT_DIRECTORY);
    }

    /*
     * 根据歌曲名获得用户录音的绝对路径
     */
    public static String getVoiceFullPath(String songName) {
        return VOICE_DIRECTORY + songName + ".wav";
    }

    /*
     * 根据歌曲名获得伴奏的绝对路径
     */
    public static String getAccompanyFullPath(String songName) {
        return ACCOMPANY_DIRECTORY + songName + ".wav";
    }

    /*
     * 根据歌曲名获得鼓点伴奏的绝对路径
     */
    public static String getDrumFullPath(String songName) {
        return DRUM_DIRECTORY + songName + ".wav";
    }

    /*
     * 根据歌曲名获得贝斯伴奏的绝对路径
     */
    public static String getBassFullPath(String songName) {
        return BASS_DIRECTORY + songName + ".wav";
    }

    /*
     * 根据歌曲名获得管弦伴奏的绝对路径
     */
    public static String getOrchestraFullPath(String songName) {
        return ORCHESTRA_DIRECTORY + songName + ".wav";
    }

    /*
     * 根据歌曲名获得原唱路径
     */
    public static String getOriginalFullPath(String songName) {
        return ORIGINAL_DIRECTORY + songName + ".wav";
    }

    /*
     * 根据歌曲名获得打分文件的路径
     */
    public static String getRateFullPath(String songName) {
        return RATE_DIRECTORY + songName + ".f0a";
    }

    /*
     * 根据歌曲名获得专辑封面路径
     */
    public static String getAlbumCoverFullPath(String songName) {
        return ALBUM_COVER_DIRECTORY + songName + ".png";
    }

    /*
     * 根据歌曲名获得伴奏演唱模式的歌词文件
     */
    public static String getAccompanyLyricFullPath(String songName) {
        return LYRIC_DIRECTORY + songName + ".lrc";
    }

    /*
     * 根据歌曲名获得自弹自唱模式的歌词文件
     */
    public static String getLyricInstrumentFullPath(String songName) {
        return LYRIC_INSTRUMENT_DIRECTORY + songName + ".lrc";
    }

    /*
     * 根据歌曲名获得mv路径
     */
    public static String getMVFullPath(String songName) {
        return MV_DIRECTORY + songName + ".mp4";
    }

    /*
     * 根据歌曲名获得和弦文件的路径
     */
    public static String getChordTransFullPath(String songName) {
        return CHORD_TRANS_DIRECTORY + songName + ".chordtrans";
    }

    /*
     * 根据录音目录获得该录音的专辑封面路径
     */
    public static String getRecordCoverFullPath(String dirFullPath) {
        return dirFullPath + "/cover.png";
    }

    /*
     * 根据录音目录获得该录音的元数据路径
     */
    public static String getRecordMetadataFullPath(String dirFullPath) {
        return dirFullPath + "/metadata.txt";
    }

    /*
     * 根据录音目录获得该录音的音频路径
     */
    public static String getRecordFileFullPath(String dirFullPath, String songName) {
        return dirFullPath + "/" + songName + ".wav";
    }

    /*
     * 将asset文件复制到手机内部存储中
     */
    public static String getAssetFullPath(Context context, String fileName) {
        String destPath = ASSET_DIRECTORY + fileName;

        if (isFilePresent(destPath)) {
            return destPath;
        }

        File fileToWrite = new File(destPath);

        try {
            try (InputStream inputStream = context.getAssets().open(fileName)) {
                try (FileOutputStream outputStream = new FileOutputStream(fileToWrite)) {
                    byte[] buf = new byte[1024];
                    int len;
                    while ((len = inputStream.read(buf)) > 0) {
                        outputStream.write(buf, 0, len);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return destPath;
    }

    /*
     * 根据和弦名获得和弦音频文件的路径
     */
    public static String getChordWavFullPath(String chordName) {
        return CHORD_WAV_DIRECTORY + chordName + ".wav";
    }

    /*
     * 根据歌曲名获得用户弹奏音频路径
     */
    public static String getUserPlayFullPath(String songName) {
        return USER_PLAY_DIRECTORY + songName + ".wav";
    }

    /**
     * 根据歌曲名获得剪切后的伴奏路径（因为要与人声保持一致）
     */
    public static String getTrimmedAccompanyFullPath(String songName) {
        String newFileName = songName + "-trim.wav";
        return ACCOMPANY_DIRECTORY + newFileName;
    }
}
