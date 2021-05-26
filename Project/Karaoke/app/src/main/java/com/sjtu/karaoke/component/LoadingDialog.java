package com.sjtu.karaoke.component;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.widget.TextView;

import com.mikhaellopez.circularprogressbar.CircularProgressBar;
import com.sjtu.karaoke.R;

public class LoadingDialog {
    boolean showProgressText;

    Activity activity;
    Dialog dialog;
    CircularProgressBar progressBar;
    TextView progressText;

    public LoadingDialog(Activity activity, String text) {
        this.activity = activity;

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
    }

    public LoadingDialog(Activity activity, String text, boolean showProgressText) {
        this.activity = activity;

        dialog = new Dialog(activity);
        dialog.setContentView(R.layout.dialog_loading);
        dialog.setCanceledOnTouchOutside(false);
        if (text != null && !text.equals("")) {
            TextView textView = (dialog.findViewById(R.id.textLoading));
            textView.setText(text);
        }
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        progressBar = dialog.findViewById(R.id.circularProgressBar);

        progressText = activity.findViewById(R.id.progressText);
        if (!showProgressText) {
            progressText.setVisibility(View.INVISIBLE);
        }
    }

    public void show() {
        dialog.show();
    }

    public void dismiss() {
        dialog.dismiss();
    }

    public void setProgress(int progress) {
        setProgress(progress, false);
    }

    public void setProgress(int progress, boolean animated) {
        activity.runOnUiThread(() -> {
            if (animated) {
                progressBar.setProgressWithAnimation(progress);
            } else {
                progressBar.setProgress(progress);
            }
            progressText.setText(formatProgress(progress));
        });
    }

    private String formatProgress(int progress) {
        return progress + "%";
    }
}
