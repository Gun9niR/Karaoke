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

public class LoadingDialog extends Dialog {
    public final static int MAX_PROGRESS = 100;

    Activity activity;
    CircularProgressBar progressBar;
    TextView progressText;

    Semaphore semaphore;

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

        progressText = findViewById(R.id.progressText);
        if (!showProgress) {
            progressText.setVisibility(View.INVISIBLE);
        }
        progressBar.setIndeterminateMode(true);
    }

    /**
     * Synchronously set the progress of progress bar
     * @param progress The progress to set
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
     * Thread safe method to increment progress by a certain amount
     * @param incr The amount of progress to increment
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
