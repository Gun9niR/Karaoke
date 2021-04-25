package com.sjtu.karaoke.util;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import com.arthenica.mobileffmpeg.FFmpeg;
import com.sjtu.karaoke.waveditor.WavHeader;
import com.sjtu.karaoke.waveditor.WavReader;
import com.sjtu.karaoke.waveditor.WavWriter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static com.sjtu.karaoke.util.Constants.ACCOMPANY_DIRECTORY;
import static com.sjtu.karaoke.util.Constants.BASE_DIRECTORY;
import static com.sjtu.karaoke.util.Constants.GET_RECORD_AUDIO;
import static com.sjtu.karaoke.util.Constants.PCM_DIRECTORY;
import static com.sjtu.karaoke.util.Constants.PERMISSIONS_RECORDER;
import static com.sjtu.karaoke.util.Constants.PERMISSIONS_STORAGE;
import static com.sjtu.karaoke.util.Constants.RECORD_DIRECTORY;
import static com.sjtu.karaoke.util.Constants.REQUEST_EXTERNAL_STORAGE;
import static com.sjtu.karaoke.util.Constants.WAV_DIRECTORY;

/*
 * @ClassName: Utils
 * @Author: guozh
 * @Date: 2021/3/28
 * @Version: v1.2
 * @Description: 工具方法类。用于减少项目中的重复代码。
 */

public class Utils {
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
            mediaPlayer.setDataSource(afd.getFileDescriptor(),afd.getStartOffset(),afd.getLength());
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            mediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void loadFileAndPrepareMediaPlayer(MediaPlayer mediaPlayer, String fileName) {
        if (mediaPlayer == null) {
            return;
        }

        try {
            mediaPlayer.setDataSource(fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            mediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void saveFile(Activity activity, String fileName) {
        verifyStoragePermissions(activity);
        showToast(activity, "文件已保存至" + BASE_DIRECTORY + fileName);
        try {
            File file = new File(BASE_DIRECTORY, fileName);
            file.getParentFile().mkdirs();
            file.createNewFile();
        } catch (Exception e) {
            Log.e("saveToExternalStorage()", e.getMessage());
        }
    }

    public static void makeDirectories() {
        List<File> dirs = new ArrayList<>();
        dirs.add(new File(BASE_DIRECTORY));
        dirs.add(new File(PCM_DIRECTORY));
        dirs.add(new File(WAV_DIRECTORY));
        dirs.add(new File(RECORD_DIRECTORY));
        dirs.add(new File(ACCOMPANY_DIRECTORY));

        for (File dir: dirs) {
            if(!dir.exists()) {
                dir.mkdir();
            }
        }
    }

    public static void verifyStoragePermissions(Activity activity) {
        try {
            int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (permission != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void verifyRecorderPermissions(Activity activity) {
        boolean permission = (ActivityCompat.checkSelfPermission(activity, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED)
                || (ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
                || (ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED);
        if (permission) {
            ActivityCompat.requestPermissions(activity, PERMISSIONS_RECORDER,
                    GET_RECORD_AUDIO);
        }
    }

    public static void showToast(Context context, String message) {
        Toast toast = Toast.makeText(context, message, Toast.LENGTH_LONG);
        ViewGroup group = (ViewGroup) toast.getView();
        TextView tvMessage = (TextView) group.getChildAt(0);
        tvMessage.setText(message);
        tvMessage.setGravity(Gravity.CENTER);
        toast.show();
    }


    /**
     *
     * @param from 原文件名，不包含路径
     * @param to 目标文件名，不包含路径
     * @param start 开始时间 ms
     * @param end 结束时间 ms
     */
    public static void trimWav(String from, String to, int start, int end) {
        System.out.println("========== Trimming" + from + " ==========");
        try {
            //创建原始音频文件流
            WavReader reader = new WavReader(from);
            //读取header
            WavHeader header = reader.getHeader();
            //创建裁剪文件输出文件流
            WavWriter writer = new WavWriter(to);
            writer.writeHeader(header);
            //BYTE_PER_READ 指的是每次读取的字节数，可以自定义
            byte[] buffer = new byte[WavReader.BUFFER_LENGTH];
            int size = -1;
            //移动至裁剪起点
            reader.moveToStart(start);
            //获取裁剪时间段对应的字节大小
            int dataSize = reader.getIntervalSize(end - start);
            int sizeCount = 0;
            while (true) {
                size = reader.readData(buffer, 0, buffer.length);
                //当到达裁剪时间段大小时候结束读取
                if (size < 0 || sizeCount >= dataSize) {
                    //在close时候写入实际音频数据大小
                    writer.closeFile();
                    reader.closeFile();
                    return;
                }
                //写入音频数据到裁剪文件
                writer.writeData(buffer, 0, size);
                //计算读取的字节数，注意，因为BYTE_PER_READ的原因，读取的字节数和实际的音频大小未必相同，
                //不能把它直接当作实际音频数据大小
                sizeCount += size;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("========== Trimming " + from + " finished ==========");
    }

    public static double getWAVDuration(String fullPath) {
        WavReader wavReader = new WavReader(fullPath);
        wavReader.getHeader();
        return wavReader.getDuration();
    }

    public static String getAccompanyFullPath(String fileName) {
        return ACCOMPANY_DIRECTORY + fileName;
    }

    /**
     * Append -trim to the filePath given
     * @param fileName
     * @return
     */
    public static String getTrimmedAccompanyFullPath(String fileName) {
        String newFileName = fileName.substring(0, fileName.lastIndexOf(".")) + "-trim.wav";
        return ACCOMPANY_DIRECTORY + newFileName;
    }

    public static String getVoiceFullPath(String fileName) {
        return WAV_DIRECTORY + fileName;
    }

    public static void mergeWAVs(String fileName, String trimmedAccompanyFullPath, String voiceFullPath,
                                 float accompanyVolume, float voiceVolume) {
        String resultPath = RECORD_DIRECTORY + fileName;

        FFmpeg.execute("-y" +
                " -i " + trimmedAccompanyFullPath +
                " -i " + voiceFullPath +
                " -filter_complex" +
                " \"[0]volume=" + accompanyVolume * 2 + "[a];" +
                "[1]volume=" + voiceVolume * 2 + "[b];" +
                "[a][b]amix=inputs=2:duration=longest:dropout_transition=1\" " + resultPath);


    }

    public static void deleteOneFile(String fullPath) {
        File file = new File(fullPath);

        if (file.exists()) {
            //noinspection ResultOfMethodCallIgnored
            file.delete();
        }
    }

    public static int getScore() {
        Random r = new Random();
        return r.nextInt(101);
    }
}
