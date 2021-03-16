package com.sjtu.karaoke.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.ColorSpace;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sjtu.karaoke.AccompanySingActivity;
import com.sjtu.karaoke.Data;
import com.sjtu.karaoke.R;

import java.util.ArrayList;
import java.util.List;

public class SongListAdapter extends RecyclerView.Adapter<SongListAdapter.ViewHolder> {
    List<Data.Song> songs;
    Context context;
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
            // get the button also ?
        }
    }

    public SongListAdapter(List<Data.Song> songs) {
        this.songs = songs;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.song_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.songName.setText(songs.get(position).songName);
        holder.singer.setText(songs.get(position).singer);
        holder.image.setImageResource(songs.get(position).image);
        holder.btnSing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, AccompanySingActivity.class);
                context.startActivity(intent);
            }
        });
        // set button onClick listener ?
    }

    @Override
    public int getItemCount() {
        return songs.size();
    }

    public void handleSearchResult(List<Data.Song> newSongs) {
        this.songs = newSongs;
        notifyDataSetChanged();
    }

}
