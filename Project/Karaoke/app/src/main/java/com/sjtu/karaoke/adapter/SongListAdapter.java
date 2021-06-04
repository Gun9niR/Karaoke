package com.sjtu.karaoke.adapter;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sjtu.karaoke.AccompanySingActivity;
import com.sjtu.karaoke.InstrumentSingActivity;
import com.sjtu.karaoke.R;
import com.sjtu.karaoke.component.LoadingDialog;
import com.sjtu.karaoke.data.SongInfo;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static android.view.KeyEvent.KEYCODE_BACK;
import static com.sjtu.karaoke.util.Constants.GET_ACCOMPANY_URL;
import static com.sjtu.karaoke.util.Constants.GET_BASS_URL;
import static com.sjtu.karaoke.util.Constants.GET_CHORD_URL;
import static com.sjtu.karaoke.util.Constants.GET_DRUM_URL;
import static com.sjtu.karaoke.util.Constants.GET_LYRIC_INSTRUMENT_URL;
import static com.sjtu.karaoke.util.Constants.GET_LYRIC_URL;
import static com.sjtu.karaoke.util.Constants.GET_MV_URL;
import static com.sjtu.karaoke.util.Constants.GET_ORCHESTRA_URL;
import static com.sjtu.karaoke.util.Constants.GET_ORIGINAL_URL;
import static com.sjtu.karaoke.util.Constants.GET_RATE_URL;
import static com.sjtu.karaoke.util.FileUtil.downloadFiles;
import static com.sjtu.karaoke.util.FileUtil.isFilePresent;
import static com.sjtu.karaoke.util.MiscUtil.downloadAndSetAlbumCover;
import static com.sjtu.karaoke.util.MiscUtil.getRequestParamFromId;
import static com.sjtu.karaoke.util.MiscUtil.setImageFromFile;
import static com.sjtu.karaoke.util.MiscUtil.showLoadingDialog;
import static com.sjtu.karaoke.util.MiscUtil.showWarningToast;
import static com.sjtu.karaoke.util.PathUtil.getAccompanyFullPath;
import static com.sjtu.karaoke.util.PathUtil.getAccompanyLyricFullPath;
import static com.sjtu.karaoke.util.PathUtil.getAlbumCoverFullPath;
import static com.sjtu.karaoke.util.PathUtil.getBassFullPath;
import static com.sjtu.karaoke.util.PathUtil.getChordTransFullPath;
import static com.sjtu.karaoke.util.PathUtil.getDrumFullPath;
import static com.sjtu.karaoke.util.PathUtil.getLyricInstrumentFullPath;
import static com.sjtu.karaoke.util.PathUtil.getMVFullPath;
import static com.sjtu.karaoke.util.PathUtil.getOrchestraFullPath;
import static com.sjtu.karaoke.util.PathUtil.getOriginalFullPath;
import static com.sjtu.karaoke.util.PathUtil.getRateFullPath;

/*
 * @ClassName: SongListAdapter
 * @Author: guozh
 * @Date: 2021/3/28
 * @Version: v1.2
 * @Description: 点歌界面和歌曲搜素界面的歌曲列表生成类。根据构造时传入的歌曲列表设置歌曲列表中每行的内容和点击事件。
 */

public class SongListAdapter extends RecyclerView.Adapter<SongListAdapter.ViewHolder> {
    List<SongInfo> songs;
    Activity activity;

    public void setSongs(List<SongInfo> songs) {
        this.songs = songs;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView songName, singer;
        ImageView image;
        Button btnSing;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            songName = itemView.findViewById(R.id.songName);
            singer = itemView.findViewById(R.id.singer);
            image = itemView.findViewById(R.id.cover);
            btnSing = itemView.findViewById(R.id.btnSing);
        }
    }

    public SongListAdapter(List<SongInfo> songs) {
        this.songs = songs;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        activity = (Activity) parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.row_song, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SongInfo songInfo = songs.get(position);
        String songName = songInfo.getSongName();
        String albumCoverFullPath = getAlbumCoverFullPath(songName);
        holder.songName.setText(songName);
        holder.singer.setText(songInfo.getSinger());

        if (isFilePresent(albumCoverFullPath)) {
            // if file already exists, just set the image
            setImageFromFile(albumCoverFullPath, holder.image);
        } else {
            downloadAndSetAlbumCover(songInfo.getId(), songInfo.getSongName(), activity, holder.image);
        }

        // set button onClick listener
        holder.btnSing.setOnClickListener(view -> {
            // get the song to sing
            SongInfo selectedSong = songs.get(position);

            // declare dialog
            Dialog chooseModeDialog = new Dialog(activity);
            chooseModeDialog.setContentView(R.layout.dialog_mode);
            chooseModeDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            ImageButton closeButton = chooseModeDialog.findViewById(R.id.btnClose);
            Button btnMvMode = chooseModeDialog.findViewById(R.id.btnMvMode);
            Button btnInsMode = chooseModeDialog.findViewById(R.id.btnInstrumentMode);

            closeButton.setOnClickListener(view12 -> chooseModeDialog.dismiss());

            btnMvMode.setOnClickListener(view1 -> {
                // 伴奏演唱模式
                chooseModeDialog.dismiss();

                AtomicBoolean isCanceled = new AtomicBoolean(false);
                LoadingDialog loadingDialog = showLoadingDialog(
                        activity,
                        "正在下载文件...",
                        true);

                loadingDialog.setOnKeyListener((dialog, keyCode, event) -> {
                    if (keyCode == KEYCODE_BACK) {
                        isCanceled.getAndSet(true);
                    }
                    return false;
                });

                new Thread(() -> {
                    boolean isSuccess = downloadAccompanySingFiles(selectedSong, loadingDialog, isCanceled);
                    loadingDialog.dismiss();
                    if (isSuccess) {
                        Intent intent = new Intent(activity, AccompanySingActivity.class);
                        intent.putExtra("id", selectedSong.getId());
                        intent.putExtra("songName", selectedSong.getSongName());
                        activity.startActivity(intent);
                    } else if (!isCanceled.get()) {
                        showWarningToast(activity, "未能成功下载文件，请重试");
                    }
                }).start();
            });

            btnInsMode.setOnClickListener(view13 -> {
                chooseModeDialog.dismiss();

                AtomicBoolean isCanceled = new AtomicBoolean(false);
                LoadingDialog loadingDialog = showLoadingDialog(
                        activity,
                        "正在下载文件...",
                        true);

                loadingDialog.setOnKeyListener((dialog, keyCode, event) -> {
                    if (keyCode == KEYCODE_BACK) {
                        isCanceled.getAndSet(true);
                    }
                    return false;
                });

                new Thread(() -> {
                    boolean isSuccess = downloadInstrumentSingFiles(
                            selectedSong,
                            loadingDialog,
                            isCanceled);
                    loadingDialog.dismiss();
                    if (isSuccess) {
                        Intent intent = new Intent(activity, InstrumentSingActivity.class);
                        intent.putExtra("id", selectedSong.getId());
                        intent.putExtra("songName", selectedSong.getSongName());
                        activity.startActivity(intent);
                    } else if (!isCanceled.get()) {
                        showWarningToast(activity, "未能成功下载文件，请重试");
                    }
                }).start();
            });

            chooseModeDialog.show();
        });
    }

    @Override
    public int getItemCount() {
        return songs.size();
    }

    public void handleSearchResult(List<SongInfo> newSongs) {
        this.songs = newSongs;
        notifyDataSetChanged();
    }

    /**
     * This method runs on the thread that calls it
     * @param songInfo
     * @return true if success, false if some files are not downloaded successfully
     */
    public boolean downloadAccompanySingFiles(
            SongInfo songInfo,
            LoadingDialog loadingDialog,
            AtomicBoolean isCanceled) {
        Integer id = songInfo.getId();
        String songName = songInfo.getSongName();
        String requestParam = getRequestParamFromId(id);
        // download original
        // download accompany
        // download lyric
        // download rating txt
        // download mv

        String[] destFullPaths = {
                getOriginalFullPath(songName),
                getAccompanyFullPath(songName),
                getAccompanyLyricFullPath(songName),
                getRateFullPath(songName),
                getMVFullPath(songName),
        };


        String[] urls = {
                GET_ORIGINAL_URL + requestParam,
                GET_ACCOMPANY_URL + requestParam,
                GET_LYRIC_URL + requestParam,
                GET_RATE_URL + requestParam,
                GET_MV_URL + requestParam,
        };

        return downloadFiles(urls, destFullPaths, loadingDialog, isCanceled);
    }

    /**
     * This method runs on the thread that calls it
     * @param songInfo
     * @return true if success, false if some files are not downloaded successfully
     */
    public boolean downloadInstrumentSingFiles(
            SongInfo songInfo,
            LoadingDialog loadingDialog,
            AtomicBoolean isCanceled) {
        Integer id = songInfo.getId();
        String songName = songInfo.getSongName();
        String requestParam = getRequestParamFromId(id);
        // download instrument_accompany
        // download instrument_lyric
        // download rating txt
        // download chords
        // download drum
        // download bass
        // download orchestra

        String[] destFullPaths = {
                getAccompanyFullPath(songName),
                getLyricInstrumentFullPath(songName),
                getRateFullPath(songName),
                getChordTransFullPath(songName),
                getDrumFullPath(songName),
                getBassFullPath(songName),
                getOrchestraFullPath(songName),
        };


        String[] urls = {
                GET_ACCOMPANY_URL + requestParam,
                GET_LYRIC_INSTRUMENT_URL + requestParam,
                GET_RATE_URL + requestParam,
                GET_CHORD_URL + requestParam,
                GET_DRUM_URL + requestParam,
                GET_BASS_URL + requestParam,
                GET_ORCHESTRA_URL + requestParam
        };

        return downloadFiles(urls, destFullPaths, loadingDialog, isCanceled);
    }
}
