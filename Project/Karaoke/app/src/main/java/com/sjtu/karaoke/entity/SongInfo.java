package com.sjtu.karaoke.entity;

import android.os.Parcel;
import android.os.Parcelable;

public class SongInfo implements Parcelable {
    private Integer id;
    private String songName;
    private String singer;

    public SongInfo(Integer id, String songName, String singer) {
        this.id = id;
        this.songName = songName;
        this.singer = singer;
    }

    public SongInfo(Parcel in) {
        setId(in.readInt());
        setSongName(in.readString());
        setSinger(in.readString());
    }

    public static final Creator<SongInfo> CREATOR = new Creator<SongInfo>() {
        @Override
        public SongInfo createFromParcel(Parcel in) {
            return new SongInfo(in);
        }

        @Override
        public SongInfo[] newArray(int size) {
            return new SongInfo[size];
        }
    };

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getSongName() {
        return songName;
    }

    public void setSongName(String songName) {
        this.songName = songName;
    }

    public String getSinger() {
        return singer;
    }

    public void setSinger(String singer) {
        this.singer = singer;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(songName);
        dest.writeString(singer);
    }
}
