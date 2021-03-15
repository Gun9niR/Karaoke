package com.sjtu.karaoke.adapter;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.ColorSpace;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

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

        // set button onClick listener
        holder.btnSing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // get the song to sing
                Data.Song selectedSong = songs.get(position);

                // declare dialog
                Dialog chooseModeDialog = new Dialog(context);
                chooseModeDialog.setContentView(R.layout.mode_dialogue);

                ImageButton closeButton = chooseModeDialog.findViewById(R.id.btnClose);
                closeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
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

    public void handleSearchResult(List<Data.Song> newSongs) {
        this.songs = newSongs;
        notifyDataSetChanged();
    }

}
