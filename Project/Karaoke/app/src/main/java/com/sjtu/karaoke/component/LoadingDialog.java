package com.sjtu.karaoke.component;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.widget.TextView;

import com.mikhaellopez.circularprogressbar.CircularProgressBar;
import com.sjtu.karaoke.R;

import java.util.concurrent.Semaphore;

public class LoadingDialog {
    public final static int MAX_PROGRESS = 100;

    Activity activity;
    Dialog dialog;
    CircularProgressBar progressBar;
    TextView progressText;

    Semaphore semaphore;

    public LoadingDialog(Activity activity, String text) {
        this.activity = activity;

        semaphore = new Semaphore(1);

        dialog = new Dialog(activity);
        dialog.setContentView(R.layout.dialog_loading);
        dialog.setCanceledOnTouchOutside(false);
        if (text != null && !text.equals("")) {
            TextView textView = (dialog.findViewById(R.id.textLoading));
            textView.setText(text);
        }
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        progressBar = dialog.findViewById(R.id.circularProgressBar);

        progressText = dialog.findViewById(R.id.progressText);
        progressText.setText(formatProgress(0));
        progressBar.setIndeterminateMode(true);
    }

    public LoadingDialog(Activity activity, String text, boolean showProgress) {
        this.activity = activity;

        semaphore = new Semaphore(1);

        dialog = new Dialog(activity);
        dialog.setContentView(R.layout.dialog_loading);
        dialog.setCanceledOnTouchOutside(false);
        if (text != null && !text.equals("")) {
            TextView textView = (dialog.findViewById(R.id.textLoading));
            textView.setText(text);
        }
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        progressBar = dialog.findViewById(R.id.circularProgressBar);

        progressText = dialog.findViewById(R.id.progressText);
        if (!showProgress) {
            progressText.setVisibility(View.INVISIBLE);
        }
        progressBar.setIndeterminateMode(true);
    }

    public void show() {
        dialog.show();
    }

    public void dismiss() {
        dialog.dismiss();
    }

    public void setProgress(int progress) {
        activity.runOnUiThread(() -> {
            progressBar.setProgress(progress);
            progressText.setText(formatProgress(progress));
        });
    }

    public void incrementProgress(int incr) {
        semaphore.acquireUninterruptibly();
        setProgress((int) progressBar.getProgress() + incr);
        semaphore.release();
    }

    private String formatProgress(int progress) {
        return progress + "%";
    }
}
