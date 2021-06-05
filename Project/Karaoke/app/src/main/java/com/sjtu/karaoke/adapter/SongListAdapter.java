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
import static com.sjtu.karaoke.util.FileUtil.areFilesPresent;
import static com.sjtu.karaoke.util.FileUtil.downloadFiles;
import static com.sjtu.karaoke.util.MiscUtil.downloadAndSetAlbumCover;
import static com.sjtu.karaoke.util.MiscUtil.getRequestParamFromId;
import static com.sjtu.karaoke.util.MiscUtil.showLoadingDialog;
import static com.sjtu.karaoke.util.MiscUtil.showWarningToast;
import static com.sjtu.karaoke.util.PathUtil.getAccompanyFullPath;
import static com.sjtu.karaoke.util.PathUtil.getAccompanyLyricFullPath;
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
 * @Author: 郭志东
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

        // null是为了让图片在下载中时不显示android图标
        holder.image.setImageBitmap(null);
        holder.songName.setText(songName);
        holder.singer.setText(songInfo.getSinger());

        // 为了防止专辑封面有更新，每次都重新下载专辑封面
        downloadAndSetAlbumCover(songInfo.getId(), songInfo.getSongName(), activity, holder.image);

        // 设置"K歌"键的点击事件
        holder.btnSing.setOnClickListener(view -> {
            // 获取当前歌曲
            SongInfo selectedSong = songs.get(position);

            // 产生选择模式对话框
            Dialog chooseModeDialog = new Dialog(activity);
            chooseModeDialog.setContentView(R.layout.dialog_mode);
            chooseModeDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            ImageButton closeButton = chooseModeDialog.findViewById(R.id.btnClose);
            Button btnMvMode = chooseModeDialog.findViewById(R.id.btnMvMode);
            Button btnInsMode = chooseModeDialog.findViewById(R.id.btnInstrumentMode);

            closeButton.setOnClickListener(view12 -> chooseModeDialog.dismiss());

            // 用户选择伴奏演唱模式
            btnMvMode.setOnClickListener(view1 -> {
                chooseModeDialog.dismiss();

                AtomicBoolean isCanceled = new AtomicBoolean(false);
                LoadingDialog loadingDialog = showLoadingDialog(
                        activity,
                        "正在下载文件...",
                        true);

                // 如果用户点击取消键，标记下载过程为已取消，下载函数会中断下载并删除已下载文件
                loadingDialog.setOnKeyListener((dialog, keyCode, event) -> {
                    if (keyCode == KEYCODE_BACK) {
                        isCanceled.getAndSet(true);
                    }
                    return false;
                });

                new Thread(() -> {
                    boolean isSuccess = downloadAccompanySingFiles(selectedSong, loadingDialog, isCanceled);
                    loadingDialog.dismiss();

                    // 下载成功，转到伴奏演唱界面
                    if (isSuccess) {
                        Intent intent = new Intent(activity, AccompanySingActivity.class);
                        intent.putExtra("id", selectedSong.getId());
                        intent.putExtra("songName", selectedSong.getSongName());
                        activity.startActivity(intent);
                    }
                    // 下载失败，且不是因为用户点击取消，提示用户重试
                    else if (!isCanceled.get()) {
                        showWarningToast(activity, "未能成功下载文件，请重试");
                    }
                }).start();
            });

            // 用户选择自弹自唱模式
            btnInsMode.setOnClickListener(view13 -> {
                chooseModeDialog.dismiss();

                AtomicBoolean isCanceled = new AtomicBoolean(false);
                LoadingDialog loadingDialog = showLoadingDialog(
                        activity,
                        "正在下载文件...",
                        true);

                // 如果用户点击取消键，标记下载过程为已取消，下载函数会中断下载并删除已下载文件
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
                    // 下载成功，转到伴奏演唱界面
                    if (isSuccess) {
                        Intent intent = new Intent(activity, InstrumentSingActivity.class);
                        intent.putExtra("id", selectedSong.getId());
                        intent.putExtra("songName", selectedSong.getSongName());
                        activity.startActivity(intent);
                    }
                    // 下载失败，且不是因为用户点击取消，提示用户重试
                    else if (!isCanceled.get()) {
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
        // 刷新列表显示
        notifyDataSetChanged();
    }

    /**
     * 下载伴奏演唱模式相关文件
     * @param songInfo 歌曲信息对象
     * @param loadingDialog 下载时显示的加载对话框
     * @param isCanceled 用户点击取消标记
     * @return 返回true当且仅当所有文件都下载成功
     */
    public boolean downloadAccompanySingFiles(
            SongInfo songInfo,
            LoadingDialog loadingDialog,
            AtomicBoolean isCanceled) {
        Integer id = songInfo.getId();
        String songName = songInfo.getSongName();
        String requestParam = getRequestParamFromId(id);

        // 下载原唱
        // 下载伴奏
        // 下载歌词文件
        // 下载打分文件
        // 下载MV
        String[] destFullPaths = {
                getOriginalFullPath(songName),
                getAccompanyFullPath(songName),
                getAccompanyLyricFullPath(songName),
                getRateFullPath(songName),
                getMVFullPath(songName),
        };

        if (areFilesPresent(destFullPaths)) {
            return true;
        }

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
     * 下载自弹自唱模式相关文件
     * @param songInfo 歌曲信息对象
     * @param loadingDialog 下载时显示的加载对话框
     * @param isCanceled 用户点击取消标记
     * @return 返回true当且仅当所有文件都下载成功
     */
    public boolean downloadInstrumentSingFiles(
            SongInfo songInfo,
            LoadingDialog loadingDialog,
            AtomicBoolean isCanceled) {
        Integer id = songInfo.getId();
        String songName = songInfo.getSongName();
        String requestParam = getRequestParamFromId(id);

        // 伴奏演唱模式的所有伴奏（在演唱结果页才有用）
        // 下载剪切过的.lrc文件
        // 下载打分文件
        // 下载和弦文件
        // 下载鼓点文件
        // 下载贝斯文件
        // 管弦音量
        String[] destFullPaths = {
                getAccompanyFullPath(songName),
                getLyricInstrumentFullPath(songName),
                getRateFullPath(songName),
                getChordTransFullPath(songName),
                getDrumFullPath(songName),
                getBassFullPath(songName),
                getOrchestraFullPath(songName),
        };

        if (areFilesPresent(destFullPaths)) {
            return true;
        }
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
