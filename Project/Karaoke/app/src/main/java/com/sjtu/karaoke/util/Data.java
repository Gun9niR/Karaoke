package com.sjtu.karaoke.util;

import com.sjtu.karaoke.R;

import java.util.ArrayList;
import java.util.List;

public class Data {
    public static class Song {
        public String songName;
        public String singer;
        public int image;

        public Song(String songName, String singer, int image) {
            this.songName = songName;
            this.singer = singer;
            this.image = image;
        }
    }

    public static List<Song> songs = new ArrayList<Song>() {{
        add(new Song("Attention", "Charlie Puth", R.drawable.voice_notes));
        add(new Song("Dangerously", "Charlie Puth", R.drawable.nine_track_mind));
        add(new Song("Back to December", "Taylor Swift", R.drawable.speak_now));
        add(new Song("Dangerously", "Charlie Puth", R.drawable.nine_track_mind));
        add(new Song("Dangerously", "Charlie Puth", R.drawable.nine_track_mind));
    }};
}
