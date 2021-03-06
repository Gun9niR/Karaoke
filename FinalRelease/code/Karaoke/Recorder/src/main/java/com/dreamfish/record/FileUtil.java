package com.dreamfish.record;

import android.os.Environment;
import android.text.TextUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 管理录音文件的类
 *
 * @author chenmy0709
 * @version V001R001C01B001
 */
public class FileUtil {

    static String ROOT_DIRECTORY;
    //原始文件(不能播放)
    static String PCM_DIRECTORY;
    //可播放的高质量音频文件
    static String WAV_DIRECTORY;
    //截取得到到wav文件
    static String TRIMMED_VOICE_WAV_DIRECTORY;


    public static void setBaseDirectories(String baseDirectory) {
        FileUtil.ROOT_DIRECTORY = baseDirectory;
        PCM_DIRECTORY = ROOT_DIRECTORY + "pcm/";
        WAV_DIRECTORY = ROOT_DIRECTORY + "wav/";
        TRIMMED_VOICE_WAV_DIRECTORY = WAV_DIRECTORY + "trimmed_voice/";
    }

    public static String getPcmFileAbsolutePath(String fileName) {
        if (TextUtils.isEmpty(fileName)) {
            throw new NullPointerException("fileName isEmpty");
        }
        if (!isSdcardExit()) {
            throw new IllegalStateException("sd card no found");
        }
        String mAudioRawPath = "";
        if (isSdcardExit()) {
            if (!fileName.endsWith(".pcm")) {
                fileName = fileName + ".pcm";
            }
            String fileBasePath = PCM_DIRECTORY;
            File file = new File(fileBasePath);
            //创建目录
            if (!file.exists()) {
                file.getParentFile().mkdirs();
            }
            mAudioRawPath = fileBasePath + fileName;
        }

        return mAudioRawPath;
    }

    public static String getWavFileAbsolutePath(String fileName) {
        if (fileName == null) {
            throw new NullPointerException("fileName can't be null");
        }
        if (!isSdcardExit()) {
            throw new IllegalStateException("sd card no found");
        }

        String mAudioWavPath = "";
        if (isSdcardExit()) {
            if (!fileName.endsWith(".wav")) {
                fileName = fileName + ".wav";
            }
            String fileBasePath = WAV_DIRECTORY;
            File file = new File(fileBasePath);
            //创建目录
            if (!file.exists()) {
                file.getParentFile().mkdirs();
            }
            mAudioWavPath = fileBasePath + fileName;
        }
        return mAudioWavPath;
    }

    /**
     * 判断是否有外部存储设备sdcard
     *
     * @return true | false
     */
    public static boolean isSdcardExit() {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
            return true;
        else
            return false;
    }

    /**
     * 获取全部pcm文件列表
     *
     * @return
     */
    public static List<File> getPcmFiles() {
        List<File> list = new ArrayList<>();
        String fileBasePath = PCM_DIRECTORY;

        File rootFile = new File(fileBasePath);
        if (!rootFile.exists()) {
        } else {

            File[] files = rootFile.listFiles();
            for (File file : files) {
                list.add(file);
            }

        }
        return list;

    }

    /**
     * 获取全部wav文件列表
     *
     * @return
     */
    public static List<File> getWavFiles() {
        List<File> list = new ArrayList<>();
        String fileBasePath = WAV_DIRECTORY;

        File rootFile = new File(fileBasePath);
        if (!rootFile.exists()) {
        } else {
            File[] files = rootFile.listFiles();
            for (File file : files) {
                list.add(file);
            }

        }
        return list;
    }

    /**
     * Delete file at given full path.
     * It handles deletion of nonexistent files
     * @param fullPath
     */
    public static void deleteOneFile(String fullPath) {
        File file = new File(fullPath);

        if (file.exists()) {
            //noinspection ResultOfMethodCallIgnored
            file.delete();
        }
    }

    /**
     *
     * @param pcmFileName pcm filename with out extention
     * @return Full path to the pcm file
     */
    public static String getPcmFullPath(String pcmFileName) {
        return PCM_DIRECTORY + pcmFileName + ".pcm";
    }

    public static String getTrimmedWavFullPath(String pcmFileName) {
        return TRIMMED_VOICE_WAV_DIRECTORY + pcmFileName + ".wav";
    }
}
