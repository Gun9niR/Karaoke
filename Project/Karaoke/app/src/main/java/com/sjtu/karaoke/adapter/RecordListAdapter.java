package com.sjtu.karaoke.adapter;

import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.sjtu.karaoke.LocalRecordActivity;
import com.sjtu.karaoke.R;
import com.sjtu.karaoke.entity.Record;

import java.io.File;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import static com.sjtu.karaoke.util.Constants.AUTHORITY;
import static com.sjtu.karaoke.util.Constants.RECORD_DIRECTORY;
import static com.sjtu.karaoke.util.FileUtil.deleteOneFile;
import static com.sjtu.karaoke.util.FileUtil.getFullPathsInDirectory;
import static com.sjtu.karaoke.util.FileUtil.isFilePresent;
import static com.sjtu.karaoke.util.MiscUtil.downloadAndSetAlbumCover;
import static com.sjtu.karaoke.util.PathUtil.getAlbumCoverFullPath;
import static com.sjtu.karaoke.util.MiscUtil.getChooserIntent;
import static com.sjtu.karaoke.util.MiscUtil.setImageFromFile;

/*
 * @ClassName: RecordListAdapter
 * @Author: guozh
 * @Date: 2021/3/28
 * @Version: v1.2
 * @Description: 本地录音界面的录音列表生成类。根据构造时传入的录音列表参数设置本地录音列表中每行的内容和点击事件。
 */
public class RecordListAdapter extends RecyclerView.Adapter<RecordListAdapter.ViewHolder> {
    List<Record> records;
    LocalRecordActivity activity;

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView recordName, recordTime;
        ImageView recordCover;
        ImageButton btnPlay;
        ImageButton btnShare;
        ImageButton btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            recordName = (TextView) itemView.findViewById(R.id.recordName);
            recordTime = (TextView) itemView.findViewById(R.id.recordTime);
            recordCover = (ImageView) itemView.findViewById(R.id.recordCover);
            btnPlay = (ImageButton) itemView.findViewById(R.id.btnPlay);
            btnShare = (ImageButton) itemView.findViewById(R.id.btnShare);
            btnDelete = (ImageButton) itemView.findViewById(R.id.btnDelete);
        }
    }

    public RecordListAdapter(LocalRecordActivity activity) {
        this.activity = activity;
        List<String> recordFullPaths = getFullPathsInDirectory(RECORD_DIRECTORY);
        records = new ArrayList<>();
        for (String recordFullPath: recordFullPaths) {
            try {
                records.add(new Record(recordFullPath));
            } catch (ParseException e) {
                Log.e("Initialize records", "Incorrect record file name format!");
                e.printStackTrace();
            }
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.row_record, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Record record = records.get(position);
        String songName = record.getSongName();
        holder.recordName.setText(songName);
        holder.recordTime.setText(record.getRecordTime());
        // download it if it does not exist
        if (isFilePresent(getAlbumCoverFullPath(songName))) {
            setImageFromFile(getAlbumCoverFullPath(songName), holder.recordCover);
        } else {
            downloadAndSetAlbumCover(record.getId(), songName, activity, holder.recordCover);
        }
        holder.btnShare.setOnClickListener(view -> {
            File recordFile = new File(record.getFullPath());
            Uri uri = FileProvider.getUriForFile(activity, AUTHORITY, recordFile);

            Intent chooserIntent = getChooserIntent(uri, activity);
            activity.startActivity(chooserIntent);
        });

        holder.btnPlay.setOnClickListener(view -> activity.playRecord(record));

        holder.btnDelete.setOnClickListener(v -> {
            String recordFullPath = record.getFullPath();
            deleteOneFile(recordFullPath);
            records.remove(position);
            notifyDataSetChanged();

            activity.checkCurrentDeletion(recordFullPath);
        });
    }

    @Override
    public int getItemCount() {
        return records.size();
    }
}
