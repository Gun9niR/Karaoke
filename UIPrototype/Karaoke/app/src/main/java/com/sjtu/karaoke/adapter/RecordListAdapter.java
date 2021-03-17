package com.sjtu.karaoke.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sjtu.karaoke.Data;
import com.sjtu.karaoke.R;

import java.util.List;

import static com.sjtu.karaoke.Data.Record.getRecordTimeStr;

public class RecordListAdapter extends RecyclerView.Adapter<RecordListAdapter.ViewHolder> {
    List<Data.Record> records;
    Context context;

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView recordName, recordTime;
        ImageView recordCover;
        ImageButton btnPlay;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            recordName = (TextView) itemView.findViewById(R.id.recordName);
            recordTime = (TextView) itemView.findViewById(R.id.recordTime);
            recordCover = (ImageView) itemView.findViewById(R.id.recordCover);
            btnPlay = (ImageButton) itemView.findViewById(R.id.btnPlay);
        }
    }

    public RecordListAdapter(List<Data.Record> records) {
        this.records = records;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.row_record, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.recordName.setText(records.get(position).recordName);
        holder.recordTime.setText(getRecordTimeStr(records.get(position).recordTime));
        holder.recordCover.setImageResource(records.get(position).recordCover);

        // set button onClick listener
    }

    @Override
    public int getItemCount() {
        return records.size();
    }
}
