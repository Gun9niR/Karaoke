package com.sjtu.karaoke.util;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.LabeledIntent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import com.arthenica.mobileffmpeg.FFmpeg;
import com.sjtu.karaoke.component.LoadingDialog;
import com.sjtu.karaoke.component.RateResultDialog;
import com.sjtu.karaoke.data.Score;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import es.dmoral.toasty.Toasty;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static com.sjtu.karaoke.util.Constants.ALBUM_COVER_DIRECTORY;
import static com.sjtu.karaoke.util.Constants.GET_ALBUM_COVER_URL;
import static com.sjtu.karaoke.util.Constants.GET_RECORD_AUDIO;
import static com.sjtu.karaoke.util.Constants.GET_SONG_INFO_URL;
import static com.sjtu.karaoke.util.Constants.PACKAGES_FOR_SHARING;
import static com.sjtu.karaoke.util.Constants.PCM_DIRECTORY;
import static com.sjtu.karaoke.util.Constants.PERMISSIONS_RECORDER;
import static com.sjtu.karaoke.util.Constants.TRIMMED_VOICE_WAV_DIRECTORY;
import static com.sjtu.karaoke.util.FileUtil.saveFileFromResponse;

/*
 * @ClassName: Utils
 * @Author: 郭志东
 * @Date: 2021/3/28
 * @Version: v1.3
 * @Description: 工具方法类。用于减少项目中的重复代码。
 */

public class MiscUtil {
    static {
        Toasty.Config.getInstance()
                .allowQueue(false)
                .apply();
    }

    /**
     * 从要分享的文件生成ChooserIntent，供用户选择分享位置，分享位置包括微信、QQ和TIM
     * @param uri 待分享文件的URI
     * @param context 要进行分享文件的Context（子）类
     * @return 分享Intent
     */
    public static Intent getChooserIntent(Uri uri, Context context) {
        List<LabeledIntent> targetedShareIntents = new ArrayList<>();
        PackageManager pm = context.getPackageManager();

        // 在分享前需要检查APP是否已经安装
        for (String packageName: PACKAGES_FOR_SHARING) {
            if (isPackageInstalled(packageName, context)) {
                targetedShareIntents.addAll(getShareIntents(pm, uri, packageName));
            }
        }

        Intent chooserIntent = Intent.createChooser(targetedShareIntents.remove(0), "分享录音");
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, targetedShareIntents.toArray(new LabeledIntent[ targetedShareIntents.size() ]));
        return chooserIntent;
    }

    /**
     * 根据给定的APP获取可以分享到的位置（例如微信可以分享到好友）
     * @param pm 要分享文件的Context的包管理器
     * @param uri 待分享文件的URI
     * @param packageName APP的包名
     * @return 所有该APP可分享到的位置，以Intent的形式存储
     */
    private static List<LabeledIntent> getShareIntents(PackageManager pm, Uri uri, String packageName) {
        Intent dummy = new Intent(Intent.ACTION_SEND);
        dummy.setType("*/*");
        dummy.setPackage(packageName);

        List<ResolveInfo> info = pm.queryIntentActivities(dummy, 0);
        // 如果使用普通的Intent，标签的名称无法正常显示
        List<LabeledIntent> intents = new ArrayList<>();
        for (ResolveInfo i: info) {
            ActivityInfo activityInfo = i.activityInfo;
            // 音频文件无法分享到微信朋友
            if (activityInfo.packageName.equals("com.tencent.mm") &&
                    activityInfo.name.equals("com.tencent.mm.ui.tools.ShareToTimeLineUI")) {
                continue;
            }
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.putExtra(Intent.EXTRA_STREAM, uri);
            intent.setType("*/*");
            intent.setClassName(activityInfo.packageName, activityInfo.name);
            intents.add(new LabeledIntent(intent, packageName, i.loadLabel(pm), i.icon));
        }

        return intents;
    }

    /**
     * 检查某个APP是否已经安装
     * @param packageName APP的包名
     * @param context 调用该函数的Context
     * @return 当该APP已安装时返回true
     */
    public static boolean isPackageInstalled(String packageName, Context context) {
        try {
            context.getPackageManager().getPackageInfo(packageName, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    /**
     * 请求录音、读取文件的权限
     * @param activity 调用该方法的Activity
     */
    public static void verifyAllPermissions(Activity activity) {
        boolean permission = (ActivityCompat.checkSelfPermission(activity, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED)
                || (ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
                || (ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED);
        if (permission) {
            ActivityCompat.requestPermissions(activity, PERMISSIONS_RECORDER,
                    GET_RECORD_AUDIO);
        }
    }

    /**
     * 显示成功弹窗
     * @param activity 需要显示弹窗的Activity
     * @param message 显示信息
     */
    public static void showSuccessToast(Activity activity, String message) {
        // 改变UI必须在UI线程上进行
        activity.runOnUiThread(() -> {
            Toasty.success(activity, message, Toast.LENGTH_SHORT, true).show();
        });
    }

    /**
     * 显示成功弹窗，并改变弹窗的竖直位置
     * @param activity 需要显示弹窗的Activity
     * @param message 显示信息
     * @param y 需要将弹窗上移的距离
     */
    public static void showSuccessToast(Activity activity, String message, int y) {
        activity.runOnUiThread(() -> {
            Toast toast = Toasty.success(activity, message, Toast.LENGTH_SHORT, true);
            toast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, y);
            toast.show();
        });
    }

    /**
     * 显示警告弹窗
     * @param activity 需要显示弹窗的Activity
     * @param message 显示信息
     */
    public static void showWarningToast(Activity activity, String message) {
        activity.runOnUiThread(() -> {
            Toasty.warning(activity, message, Toast.LENGTH_SHORT, true).show();
        });
    }

    /**
     * 显示加载对话框，不显示进度文字
     * @param activity 显示对话框的Activity
     * @param text 正在显示的对话框对象
     */
    public static LoadingDialog showLoadingDialog(Activity activity, String text) {
        return showLoadingDialog(activity, text, false);
    }

    /**
     * 显示加载对话框
     * @param activity 显示对话框的Activity
     * @param text 正在显示的对话框对象
     * @param showProgress 是否显示进度文字
     */
    public static LoadingDialog showLoadingDialog(Activity activity, String text, boolean showProgress) {
        LoadingDialog loadingDialog = new LoadingDialog(activity, text, showProgress);

        loadingDialog.show();
        return loadingDialog;
    }

    /**
     * 显示演唱打分对话框
     * @param activity 显示对话框的Activity
     * @return 正在显示的对话框对象
     */
    public static RateResultDialog showRateResultDialog(Activity activity, Score score, String instrumentScoreStr) {
        RateResultDialog rateResultDialog = new RateResultDialog(activity, score, instrumentScoreStr);
        rateResultDialog.show();
        return rateResultDialog;
    }

    public static void getSongInfo(Callback callback) {
        getRequest(GET_SONG_INFO_URL, callback);
    }

    /**
     * 发送GET request
     * @param url 请求地址
     * @param callback 收到响应后的回调函数
     */
    public static void getRequest(String url, Callback callback) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).get().build();
        client.newCall(request).enqueue(callback);
    }

    /**
     * 从本地读取图片以加载ImageView
     * @param fullPath 图片文件的绝对路径
     * @param imageView 需要设置的ImageView
     */
    public static void setImageFromFile(String fullPath, ImageView imageView) {
        Bitmap bmp = BitmapFactory.decodeFile(fullPath);
        imageView.setImageBitmap(bmp);
    }

    /**
     * 将歌曲ID格式化为HTTP请求参数
     * @param id 歌曲ID
     */
    public static String getRequestParamFromId(Integer id) {
        return "?id=" + id;
    }

    /**
     * 下载专辑封面，并将其设置为ImageView的图片
     * @param id 歌曲的ID
     * @param songName 歌名
     * @param activity ImageView所在的Activity
     * @param imageView 需要设置图片的ImageView
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
                if (response.isSuccessful()) {
                    saveFileFromResponse(response, destPath);

                    // set image, should run on UI thread
                    activity.runOnUiThread(() -> setImageFromFile(destPath, imageView));
                }
            }
        });
    }

    /**
     * 将打分算法返回的字符串解析成四种分数
     * @param scoreStr 打分算法返回的字符串
     * @return 长度为4的整数数组
     */
    public static Integer[] parseScore(String scoreStr) {
        String[] scores = scoreStr.split(" ");
        return new Integer[] {
                Integer.parseInt(scores[0]),
                Integer.parseInt(scores[1]),
                Integer.parseInt(scores[2]),
                Integer.parseInt(scores[3]),
        };
    }

    /**
     * 将若干单音文件合称为和弦文件
     * @param chordName 和弦名称
     * @param notes 单音名称
     * @return 和弦音频文件的完整绝对路径
     */
    public static String mergeNotesToChord(String chordName, List<String> notes) {
        String destPath = PathUtil.getChordWavFullPath(chordName);
        int noteNum = notes.size();

        // 覆盖原有文件
        StringBuilder command = new StringBuilder("-y ");
        for (String note: notes) {
            command.append("-i ").append(note).append(" ");
        }

        command.append("-filter_complex amix=inputs=").append(notes.size());
        command.append(":duration=longest,volume=").append(noteNum).append(" ");
        command.append(destPath);

        FFmpeg.execute(command.toString());

        return destPath;
    }


    /**
     * 清除录音时产生的pcm和wav文件
     */
    public static void clearTemporaryPcmAndWavFiles() {
        File pcmDir = new File(PCM_DIRECTORY);
        File trimmedWavDir = new File(TRIMMED_VOICE_WAV_DIRECTORY);

        try {
            FileUtils.cleanDirectory(pcmDir);
            FileUtils.cleanDirectory(trimmedWavDir);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
