package com.sjtu.karaoke.adapter;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.SpannableString;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.view.menu.MenuPopupHelper;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.content.FileProvider;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.sjtu.karaoke.Karaoke;
import com.sjtu.karaoke.LocalRecordActivity;
import com.sjtu.karaoke.R;
import com.sjtu.karaoke.data.Record;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.sjtu.karaoke.util.Constants.AUTHORITY;
import static com.sjtu.karaoke.util.Constants.RECORD_DIRECTORY;
import static com.sjtu.karaoke.util.FileUtil.getFullPathsInDirectory;
import static com.sjtu.karaoke.util.MiscUtil.getChooserIntent;
import static com.sjtu.karaoke.util.MiscUtil.setImageFromFile;

/*
 * @ClassName: RecordListAdapter
 * @Author: guozh
 * @Date: 2021/3/28
 * @Version: v1.2
 * @Description: 本地录音界面的录音列表生成类。根据构造时传入的录音列表参数设置本地录音列表中每行的内容和点击事件。
 * 每个录音都保存在一个目录中，目录名为一个独特的字符串。
 * 目录中的内容包括：
 *  |
 *  |---- cover.png         (专辑封面)
 *  |---- <song name>.wav   (录音文件)
 *  |---- metadata.txt      (录音时间、等级)
 */
public class RecordListAdapter extends RecyclerView.Adapter<RecordListAdapter.ViewHolder> {
    List<Record> records;
    LocalRecordActivity activity;

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView recordName, recordTime, recordRank;
        ImageView recordCover;
        ImageButton btnRecordOperation;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            recordName = itemView.findViewById(R.id.recordName);
            recordTime = itemView.findViewById(R.id.recordTime);
            recordRank = itemView.findViewById(R.id.recordRank);
            recordCover = itemView.findViewById(R.id.recordCover);
            btnRecordOperation = itemView.findViewById(R.id.btnRecordOperation);
        }
    }

    public RecordListAdapter(LocalRecordActivity activity) {
        this.activity = activity;
        List<String> recordDirFullPaths = getFullPathsInDirectory(RECORD_DIRECTORY);

        records = new ArrayList<>();
        for (String recordDirFullPath: recordDirFullPaths) {
            try {
                records.add(new Record(recordDirFullPath));
            } catch (ParseException | IOException e) {
                Log.e("Initialize records", "Incorrect metadata format!");
                e.printStackTrace();
            }
        }

        System.out.println(records.size());
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.row_record, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Record record = records.get(position);
        String songName = record.getSongName();
        // set song name
        holder.recordName.setText(songName);
        holder.recordName.append(new SpannableString("  "));

        holder.recordRank.setText(record.getRank().getRankingText());
        Drawable recordRankBackground = ResourcesCompat.getDrawable(Karaoke.getRes(), R.drawable.bg_rank, null);
        Objects.requireNonNull(recordRankBackground).setTint(record.getRank().getRankingColor());
        holder.recordRank.setBackground(recordRankBackground);

        // set record time
        holder.recordTime.setText(record.getRecordTime());

        // set album cover
        activity.runOnUiThread(() -> setImageFromFile(
                record.getAlbumCoverFullPath(),
                holder.recordCover
                )
        );

        holder.btnRecordOperation.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(activity, v);
            popup.inflate(R.menu.record_operation_menu);
            popup.setOnMenuItemClickListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.menuItemPlayRecord) {
                    activity.playRecord(record);
                } else if (itemId == R.id.menuItemShareRecord) {
                    File recordFile = new File(record.getRecordFullPath());
                    Uri uri = FileProvider.getUriForFile(activity, AUTHORITY, recordFile);

                    Intent chooserIntent = getChooserIntent(uri, activity);
                    activity.startActivity(chooserIntent);
                } else {
                    String recordFullPath = record.getRecordFullPath();
                    activity.checkCurrentDeletion(recordFullPath);
                    try {
                        FileUtils.deleteDirectory(new File(record.getDirFullPath()));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    records.remove(position);
                    notifyDataSetChanged();
                }

                popup.dismiss();
                return true;
            });

            @SuppressLint("RestrictedApi") MenuPopupHelper menuPopupHelper = new MenuPopupHelper(activity, (MenuBuilder) popup.getMenu(), v);
            menuPopupHelper.setForceShowIcon(true);
            menuPopupHelper.show();
        });
    }

    @Override
    public int getItemCount() {
        return records.size();
    }
}
