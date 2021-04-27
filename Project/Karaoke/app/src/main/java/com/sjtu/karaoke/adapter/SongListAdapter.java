package com.sjtu.karaoke.adapter;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
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
import com.sjtu.karaoke.entity.SongInfo;

import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static com.sjtu.karaoke.util.Constants.ALBUM_COVER_DIRECTORY;
import static com.sjtu.karaoke.util.Constants.GET_ACCOMPANY_URL;
import static com.sjtu.karaoke.util.Constants.GET_ALBUM_COVER_URL;
import static com.sjtu.karaoke.util.Constants.GET_LYRIC_URL;
import static com.sjtu.karaoke.util.Constants.GET_MV_URL;
import static com.sjtu.karaoke.util.Constants.GET_ORIGINAL_URL;
import static com.sjtu.karaoke.util.Constants.GET_RATE_URL;
import static com.sjtu.karaoke.util.FileUtil.downloadFiles;
import static com.sjtu.karaoke.util.FileUtil.isFilePresent;
import static com.sjtu.karaoke.util.FileUtil.saveFileFromResponse;
import static com.sjtu.karaoke.util.MiscUtil.getAccompanyFullPath;
import static com.sjtu.karaoke.util.MiscUtil.getAlbumCoverPath;
import static com.sjtu.karaoke.util.MiscUtil.getLyricFullPath;
import static com.sjtu.karaoke.util.MiscUtil.getMVFullPath;
import static com.sjtu.karaoke.util.MiscUtil.getOriginalFullPath;
import static com.sjtu.karaoke.util.MiscUtil.getRateFullPath;
import static com.sjtu.karaoke.util.MiscUtil.getRequest;
import static com.sjtu.karaoke.util.MiscUtil.getRequestParamFromId;
import static com.sjtu.karaoke.util.MiscUtil.setImageFromFile;
import static com.sjtu.karaoke.util.MiscUtil.showLoadingDialog;

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
            songName = (TextView) itemView.findViewById(R.id.songName);
            singer = (TextView) itemView.findViewById(R.id.singer);
            image = (ImageView) itemView.findViewById(R.id.cover);
            btnSing = (Button) itemView.findViewById(R.id.btnSing);
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
        String albumCoverFullPath = getAlbumCoverPath(songName);
        holder.songName.setText(songName);
        holder.singer.setText(songInfo.getSinger());

        if (isFilePresent(albumCoverFullPath)) {
            // if file already exists, just set the image
            setImageFromFile(albumCoverFullPath, holder.image);
        } else {
            downloadAndSetAlbumCover(songInfo, holder.image);
        }

        // set button onClick listener
        holder.btnSing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // get the song to sing
                SongInfo selectedSong = songs.get(position);

                // declare dialog
                Dialog chooseModeDialog = new Dialog(activity);
                chooseModeDialog.setContentView(R.layout.dialog_mode);
                chooseModeDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                ImageButton closeButton = chooseModeDialog.findViewById(R.id.btnClose);
                Button btnMvMode = chooseModeDialog.findViewById(R.id.btnMvMode);
                Button btnInsMode = chooseModeDialog.findViewById(R.id.btnInstrumentMode);

                closeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        chooseModeDialog.dismiss();
                    }
                });

                btnMvMode.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // 伴奏演唱模式

                        Dialog loadingDialog = showLoadingDialog(activity, "正在下载文件...");

                        downloadAccompanySingFiles(selectedSong);

                        loadingDialog.dismiss();
                        chooseModeDialog.dismiss();

                        Intent intent = new Intent(activity, AccompanySingActivity.class);
                        intent.putExtra("songName", selectedSong.getSongName());
                        activity.startActivity(intent);

                    }
                });

                btnInsMode.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(activity, InstrumentSingActivity.class);
                        activity.startActivity(intent);
                        chooseModeDialog.dismiss();
                    }
                });

                chooseModeDialog.show();
            }
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
     * Download album cover from the server.
     * @param songInfo SongInfo object of the song whose album cover is to be downloaded
     * @param imageView The ImageView object to set the image
     */
    public void downloadAndSetAlbumCover(SongInfo songInfo, ImageView imageView) {
        getRequest(GET_ALBUM_COVER_URL + "?id=" + songInfo.getId(), new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("Error when downloading file", "Failed to download album cover for " + songInfo.getSongName());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                // receive and save the file
                String destPath = ALBUM_COVER_DIRECTORY + songInfo.getSongName() + ".png";
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

    public void downloadAccompanySingFiles(SongInfo songInfo) {
        Integer id = songInfo.getId();
        String songName = songInfo.getSongName();
        String requestParam = getRequestParamFromId(id);
        // download original
        // download accompany
        // download lyric
        // download rating txt
        // download mv

        String[] urls = {
                GET_ORIGINAL_URL + requestParam,
                GET_ACCOMPANY_URL + requestParam,
                GET_LYRIC_URL + requestParam,
                GET_RATE_URL + requestParam,
                GET_MV_URL + requestParam,
        };

        String[] destFullPaths = {
                getOriginalFullPath(songName),
                getAccompanyFullPath(songName),
                getLyricFullPath(songName),
                getRateFullPath(songName),
                getMVFullPath(songName),
        };

        downloadFiles(urls, destFullPaths);
    }
}
