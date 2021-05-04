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
import android.os.Build;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;

import com.dreamfish.record.FileUtil;
import com.sjtu.karaoke.R;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static com.sjtu.karaoke.util.Constants.ACCOMPANY_DIRECTORY;
import static com.sjtu.karaoke.util.Constants.ALBUM_COVER_DIRECTORY;
import static com.sjtu.karaoke.util.Constants.GET_ALBUM_COVER_URL;
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
import static com.sjtu.karaoke.util.FileUtil.saveFileFromResponse;

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
        // 录音结果，由pcm合成
        dirs.add(new File(WAV_DIRECTORY));
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
        // 歌词文件
        dirs.add(new File(LYRIC_DIRECTORY));
        // MV
        dirs.add(new File(MV_DIRECTORY));

        for (File dir : dirs) {
            if (!dir.exists()) {
                dir.mkdir();
            }
        }

        FileUtil.setBaseDirectories(ROOT_DIRECTORY);
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
        return ACCOMPANY_DIRECTORY + songName + ".wav";
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

    public static String getAlbumCoverFullPath(String songName) {
        return ALBUM_COVER_DIRECTORY + songName + ".png";
    }

    public static String getRequestParamFromId(Integer id) {
        return "?id=" + id;
    }

    public static String getMVFullPath(String songName) {
        return MV_DIRECTORY + songName + ".mp4";
    }

    public static String getRateFullPath(String songName) {
        return RATE_DIRECTORY + songName + ".f0a";
    }

    public static String getLyricFullPath(String songName) {
        return LYRIC_DIRECTORY + songName + ".lrc";
    }

    public static String getOriginalFullPath(String songName) {
        return ORIGINAL_DIRECTORY + songName + ".wav";
    }

    /**
     * Returns the trimmed wav path which is converted from a pcm file
     * @param fileName File name without extension
     * @return Full path with .wav extension
     */
    public static String getTrimmedVoiceFullPath(String fileName) {
        return TRIMMED_VOICE_WAV_DIRECTORY + fileName + ".wav";
    }
    /**
     * Get name of the record file from song name
     * Naming strategy is: <songName>-<year>-<month>-<date>-<hour>-<minute>
     * Time is generated after merging pcm files to wav file
     * @param id
     * @param songName
     * @return Resulting record file name
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    public static String getRecordName(Integer id, String songName) {
        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat formatter = new SimpleDateFormat("-yyyy-MM-dd-HH-mm");
        String dateString = formatter.format(date);
        return id + "-" + songName + dateString + ".wav";
    }

    /**
     * Download album cover from the server.
     * @param id
     * @param songName
     * @param activity The activity that contains the ImageVIew
     * @param imageView The ImageView object to set the image
     */
    public static void downloadAndSetAlbumCover(Integer id, String songName, Activity activity, ImageView imageView) {
        getRequest(GET_ALBUM_COVER_URL + "?id=" + id, new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("Error when downloading file", "Failed to download album cover for " + songName);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                // receive and save the file
                String destPath = ALBUM_COVER_DIRECTORY + songName + ".png";
                saveFileFromResponse(response, destPath);

                // set image, should run on UI thread
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setImageFromFile(destPath, imageView);
                    }
                });
            }
        });
    }
}
