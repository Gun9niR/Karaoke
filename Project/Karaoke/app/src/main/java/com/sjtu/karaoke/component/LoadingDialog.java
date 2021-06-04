package com.sjtu.karaoke.component;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.widget.TextView;

import com.mikhaellopez.circularprogressbar.CircularProgressBar;
import com.sjtu.karaoke.R;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;

/*
 * @ClassName: LoadingDialog
 * @Author: 郭志东
 * @Date: 2021/6/4
 * @Version: v1.2
 * @Description: 加载对话框类，可以显示当前进度、环形加载动画及自定义说明文字
 */
public class LoadingDialog extends Dialog {
    public final static int MAX_PROGRESS = 100;

    Activity activity;
    CircularProgressBar progressBar;
    TextView progressText;

    // 用互斥锁确保所有进度设置是线程安全的
    Semaphore semaphore;

    /**
     * 在构造函数中设置好提示文字和进度文字
     * @param activity 对话框所在的activity
     * @param text 提示文字
     * @param showProgress 是否显示进度（加载动画始终显示）
     */
    public LoadingDialog(Activity activity, String text, boolean showProgress) {
        super(activity);
        setContentView(R.layout.dialog_loading);
        setCanceledOnTouchOutside(false);

        this.activity = activity;
        semaphore = new Semaphore(1);

        if (text != null && !text.equals("")) {
            TextView textView = (findViewById(R.id.textLoading));
            textView.setText(text);
        }
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        progressBar = findViewById(R.id.circularProgressBar);
        progressBar.setIndeterminateMode(true);

        progressText = findViewById(R.id.progressText);
        if (!showProgress) {
            progressText.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * 设置进度（线程安全）
     * @param progress 目标进度
     */
    public void setProgress(int progress) {
        final int actualProgress = Math.min(progress, MAX_PROGRESS);
        final CountDownLatch cdl = new CountDownLatch(1);
        activity.runOnUiThread(() -> {
            progressBar.setProgress(actualProgress);
            progressText.setText(formatProgress(actualProgress));
            cdl.countDown();
        });
        try {
            cdl.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 增加进度（线程安全）
     * @param incr 进度增加量
     */
    public void incrementProgress(int incr) {
        semaphore.acquireUninterruptibly();
        setProgress((int) progressBar.getProgress() + incr);
        semaphore.release();
    }

    public int getProgress() {
        return (int) progressBar.getProgress();
    }

    private String formatProgress(int progress) {
        return progress + "%";
    }
}
