package com.sjtu.karaoke.util;

import com.sjtu.karaoke.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
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
        add(new Song("Back to December", "Taylor Swift", R.drawable.speak_now));
    }};

    public static class Record {
        public String recordName;
        public Calendar recordTime;
        public int recordCover;

        public Record(String recordName, Calendar recordTime, int recordCover) {
            this.recordName = recordName;
            this.recordTime = recordTime;
            this.recordCover = recordCover;
        }

        public static String getRecordTimeStr(Calendar recordTime) {
            return recordTime.get(Calendar.YEAR) + "." + recordTime.get(Calendar.MONTH)+ "." + recordTime.get(Calendar.DATE);
        }

    }

    public static List<Record> records = new ArrayList<Record>() {{
       add(new Record("Attention", new GregorianCalendar(2021, Calendar.FEBRUARY, 15), R.drawable.voice_notes));
    }};
}
