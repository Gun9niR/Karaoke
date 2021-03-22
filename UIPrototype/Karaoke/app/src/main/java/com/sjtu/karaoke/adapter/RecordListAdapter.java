package com.sjtu.karaoke.adapter;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sjtu.karaoke.Data;
import com.sjtu.karaoke.LocalRecordActivity;
import com.sjtu.karaoke.R;

import java.io.IOException;
import java.util.List;

import static com.sjtu.karaoke.Data.Record.getRecordTimeStr;

public class RecordListAdapter extends RecyclerView.Adapter<RecordListAdapter.ViewHolder> {
    List<Data.Record> records;
    LocalRecordActivity context;

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView recordName, recordTime;
        ImageView recordCover;
        ImageButton btnPlay;
        ImageButton btnShare;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            recordName = (TextView) itemView.findViewById(R.id.recordName);
            recordTime = (TextView) itemView.findViewById(R.id.recordTime);
            recordCover = (ImageView) itemView.findViewById(R.id.recordCover);
            btnPlay = (ImageButton) itemView.findViewById(R.id.btnPlay);
            btnShare = (ImageButton) itemView.findViewById(R.id.btnShare);
        }
    }

    public RecordListAdapter(LocalRecordActivity context, List<Data.Record> records) {
        this.context = context;
        this.records = records;
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
        holder.recordName.setText(records.get(position).recordName);
        holder.recordTime.setText(getRecordTimeStr(records.get(position).recordTime));
        holder.recordCover.setImageResource(records.get(position).recordCover);
        holder.btnShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message = records.get(position).recordName + "已经成功分享到微信";
                Toast toast = Toast.makeText(context, message, Toast.LENGTH_LONG);
                ViewGroup group = (ViewGroup) toast.getView();
                TextView tvMessage = (TextView) group.getChildAt(0);
                tvMessage.setText(message);
                tvMessage.setGravity(Gravity.CENTER);
                toast.show();
            }
        });
        // set button onClick listener
        holder.btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                context.playRecord(records.get(position));
            }
        });
    }

    @Override
    public int getItemCount() {
        return records.size();
    }
}
