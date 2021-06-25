package com.sjtu.karaoke.component;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.Outline;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.widget.Button;

import androidx.annotation.NonNull;

import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.PlayerView;
import com.sjtu.karaoke.Karaoke;
import com.sjtu.karaoke.R;

import static com.sjtu.karaoke.util.MediaPlayerUtil.terminateExoPlayer;

public class TutorialDialog extends Dialog {
    private final Activity activity;
    private final SimpleExoPlayer player;

    public TutorialDialog(@NonNull Activity context) {
        super(context);
        this.activity = context;


        setContentView(R.layout.dialog_tutorial);
        setCanceledOnTouchOutside(false);
        int width = (int)(Karaoke.getRes().getDisplayMetrics().widthPixels*0.80);
        int height = (int)(Karaoke.getRes().getDisplayMetrics().heightPixels*0.90);
        getWindow().setLayout(width, height);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        player = new SimpleExoPlayer.Builder(activity).build();
        MediaItem mediaItem = MediaItem.fromUri(Uri.parse("asset:///tutorial.mp4"));
        player.setMediaItem(mediaItem);
        player.prepare();

        PlayerView playerView = findViewById(R.id.tutorialView);
        playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FILL);
        playerView.setShowNextButton(false);
        playerView.setShowPreviousButton(false);
        playerView.setShowFastForwardButton(false);
        playerView.setShowRewindButton(false);
        playerView.setControllerAutoShow(false);
        playerView.setPlayer(player);
        playerView.setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), 30);
            }
        });
        playerView.setClipToOutline(true);
        player.play();

        // 点击"我知道了"按钮，结束播放
        Button confirmBtn = this.findViewById(R.id.btnTutorialFinish);
        confirmBtn.setOnClickListener((v) -> {
            this.dismiss();
        });
    }

    @Override
    public void dismiss() {
        terminateExoPlayer(activity, player);
        super.dismiss();
    }

    public void pause() {
        player.pause();
    }

    public void play() {
        player.play();
    }
}
