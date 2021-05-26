package com.sjtu.karaoke.util;

import android.content.Context;
import android.os.Build;

import androidx.annotation.RequiresApi;

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
import static com.sjtu.karaoke.util.Constants.ORCHESTRA_DIRECORY;
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
import static com.sjtu.karaoke.util.MiscUtil.getRecordName;

public class PathUtil {

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
        dirs.add(new File(ORCHESTRA_DIRECORY));

        for (File dir : dirs) {
            if (!dir.exists()) {
                dir.mkdirs();
            }
        }

        FileUtil.setBaseDirectories(ROOT_DIRECTORY);
    }

    public static String getVoiceFullPath(String songName) {
        return VOICE_DIRECTORY + songName + ".wav";
    }

    public static String getAccompanyFullPath(String songName) {
        return ACCOMPANY_DIRECTORY + songName + ".wav";
    }

    public static String getDrumFullPath(String songName) {
        return DRUM_DIRECTORY + songName + ".wav";
    }

    public static String getBassFullPath(String songName) {
        return BASS_DIRECTORY + songName + ".wav";
    }

    public static String getOrchestraFullPath(String songName) {
        return ORCHESTRA_DIRECORY + songName + ".wav";
    }

    public static String getOriginalFullPath(String songName) {
        return ORIGINAL_DIRECTORY + songName + ".wav";
    }

    public static String getRateFullPath(String songName) {
        return RATE_DIRECTORY + songName + ".f0a";
    }

    public static String getAlbumCoverFullPath(String songName) {
        return ALBUM_COVER_DIRECTORY + songName + ".png";
    }

    public static String getAccompanyLyricFullPath(String songName) {
        return LYRIC_DIRECTORY + songName + ".lrc";
    }

    public static String getLyricInstrumentFullPath(String songName) {
        return LYRIC_INSTRUMENT_DIRECTORY + songName + ".lrc";
    }

    public static String getMVFullPath(String songName) {
        return MV_DIRECTORY + songName + ".mp4";
    }

    public static String getChordTransFullPath(String songName) {
        return CHORD_TRANS_DIRECTORY + songName + ".chordtrans";
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static String getRecordFullPath(int id, String songName) {
        return RECORD_DIRECTORY + getRecordName(id, songName);

    }
    /*
     * Given asset fileName(with extension), extract it into a temporary folder
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

    public static String getChordWavFullPath(String chordName) {
        return CHORD_WAV_DIRECTORY + chordName + ".wav";
    }

    public static String getUserPlayFullPath(String songName) {
        return USER_PLAY_DIRECTORY + songName + ".wav";
    }

    /**
     * Append -trim to the filePath given
     */
    public static String getTrimmedAccompanyFullPath(String songName) {
        String newFileName = songName + "-trim.wav";
        return ACCOMPANY_DIRECTORY + newFileName;
    }
}
