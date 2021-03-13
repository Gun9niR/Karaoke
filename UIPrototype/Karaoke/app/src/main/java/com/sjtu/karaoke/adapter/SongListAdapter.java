package com.sjtu.karaoke.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sjtu.karaoke.R;

public class SongListAdapter extends RecyclerView.Adapter<SongListAdapter.ViewHolder> {
    String[] songNames;
    String[] singers;
    int[] images;

    public SongListAdapter(String[] songNames, String[] singers, int[] images) {
        this.songNames = songNames;
        this.singers = singers;
        this.images = images;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.song_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.songName.setText(songNames[position]);
        holder.singer.setText(singers[position]);
        holder.image.setImageResource(images[position]);
    }

    @Override
    public int getItemCount() {
        return songNames.length;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView songName, singer;
        ImageView image;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            songName = (TextView) itemView.findViewById(R.id.songName);
            singer = (TextView) itemView.findViewById(R.id.singer);
            image = (ImageView) itemView.findViewById(R.id.cover);
        }
    }
}
