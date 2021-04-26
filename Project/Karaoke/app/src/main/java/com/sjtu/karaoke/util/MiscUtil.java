package com.sjtu.karaoke.util;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import com.sjtu.karaoke.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;

import static com.sjtu.karaoke.util.Constants.ACCOMPANY_DIRECTORY;
import static com.sjtu.karaoke.util.Constants.ALBUM_COVER_DIRECTORY;
import static com.sjtu.karaoke.util.Constants.GET_RECORD_AUDIO;
import static com.sjtu.karaoke.util.Constants.GET_SONG_INFO_URL;
import static com.sjtu.karaoke.util.Constants.LYRIC_DIRECTORY;
import static com.sjtu.karaoke.util.Constants.MV_DIRECTORY;
import static com.sjtu.karaoke.util.Constants.ORIGINAL_DIRECTORY;
import static com.sjtu.karaoke.util.Constants.PCM_DIRECTORY;
import static com.sjtu.karaoke.util.Constants.PERMISSIONS_RECORDER;
import static com.sjtu.karaoke.util.Constants.PERMISSIONS_STORAGE;
import static com.sjtu.karaoke.util.Constants.RATE_DIRECTORY;
import static com.sjtu.karaoke.util.Constants.RECORD_DIRECTORY;
import static com.sjtu.karaoke.util.Constants.REQUEST_EXTERNAL_STORAGE;
import static com.sjtu.karaoke.util.Constants.ROOT_DIRECTORY;
import static com.sjtu.karaoke.util.Constants.TRIMMED_VOICE_WAV_DIRECTORY;
import static com.sjtu.karaoke.util.Constants.WAV_DIRECTORY;

/*
 * @ClassName: Utils
 * @Author: guozh
 * @Date: 2021/3/28
 * @Version: v1.2
 * @Description: 工具方法类。用于减少项目中的重复代码。
 */

public class MiscUtil {
    public static Toast toast;

    public static void makeDirectories() {
        List<File> dirs = new ArrayList<>();
        // 保存文件的根目录
        dirs.add(new File(ROOT_DIRECTORY));
        // 录音过程中产生的pcm文件
        dirs.add(new File(PCM_DIRECTORY));
        // 录音结果，由pcm合成
        dirs.add(new File(WAV_DIRECTORY));
        // 录音过程中一句话的pcm转换成wav的结果
        dirs.add(new File(TRIMMED_VOICE_WAV_DIRECTORY));
        // 伴奏和录音合成结果
        dirs.add(new File(RECORD_DIRECTORY));
        // 伴奏和合成最终结果时被截短的伴奏
        dirs.add(new File(ACCOMPANY_DIRECTORY));
        // 原唱
        dirs.add(new File(ORIGINAL_DIRECTORY));
        // 专辑封面
        dirs.add(new File(ALBUM_COVER_DIRECTORY));

        for (File dir : dirs) {
            if (!dir.exists()) {
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
        if (toast != null) {
            toast.cancel();
        }
        toast = Toast.makeText(context, message, Toast.LENGTH_LONG);
        ViewGroup group = (ViewGroup) toast.getView();
        TextView tvMessage = (TextView) group.getChildAt(0);
        tvMessage.setText(message);
        tvMessage.setGravity(Gravity.CENTER);
        toast.show();
    }

    /**
     * Display loading dialog, with provided text as hint
     *
     * @param context
     * @param text
     */
    public static Dialog showLoadingDialog(Context context, String text) {
        Dialog loadingDialog = new Dialog(context);
        loadingDialog.setContentView(R.layout.dialog_loading);
        loadingDialog.setCanceledOnTouchOutside(false);
        if (text != null) {
            TextView textView = (TextView) (loadingDialog.findViewById(R.id.textLoading));
            textView.setText(text);
        }
        loadingDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        loadingDialog.show();
        return loadingDialog;
    }

    public static String getAccompanyFullPath(String songName) {
        return ACCOMPANY_DIRECTORY + songName + "/wav";
    }

    public static int getScore(String wavFullPath) {
        Random r = new Random();
        return r.nextInt(101);
    }

    public static void getSongInfo(Callback callback) {
        getRequest(GET_SONG_INFO_URL, callback);
    }

    /**
     * Append -trim to the filePath given
     *
     * @param songName
     * @return
     */
    public static String getTrimmedAccompanyFullPath(String songName) {
        String newFileName = songName + "-trim.wav";
        return ACCOMPANY_DIRECTORY + newFileName;
    }

    public static String getVoiceFullPath(String songName) {
        return WAV_DIRECTORY + songName + ".wav";
    }

    /**
     * @param url      url to make get request to
     * @param callback
     */
    public static void getRequest(String url, Callback callback) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).get().build();
        client.newCall(request).enqueue(callback);
    }

    /**
     * Load image from given fullPath and set the image of given ImageView object
     *
     * @param fullPath
     * @param imageView
     */
    public static void setImageFromFile(String fullPath, ImageView imageView) {
        Bitmap bmp = BitmapFactory.decodeFile(fullPath);
        imageView.setImageBitmap(bmp);
    }

    public static String getAlbumCoverPath(String fileName) {
        return ALBUM_COVER_DIRECTORY + fileName + ".png";
    }

    public static String getRequestParamFromId(Integer id) {
        return "?id=" + id;
    }

    public static String getMVFullPath(String songName) {
        return MV_DIRECTORY + songName + ".wav";
    }

    public static String getRateFullPath(String songName) {
        return RATE_DIRECTORY + songName + ".wav";
    }

    public static String getLyricFullPath(String songName) {
        return LYRIC_DIRECTORY + songName + ".wav";
    }

    public static String getOriginalFullPath(String songName) {
        return ORIGINAL_DIRECTORY + songName + ".wav";
    }
}
