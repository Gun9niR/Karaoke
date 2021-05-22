package com.sjtu.karaoke.entity;

public class PlayChordRecord {
    Chord chord;
    Integer time;
    Integer duration;

    public Chord getChord() {
        return chord;
    }

    public void setChord(Chord chord) {
        this.chord = chord;
    }

    public Integer getTime() {
        return time;
    }

    public void setTime(Integer time) {
        this.time = time;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public PlayChordRecord(Chord chord, Integer time) {
        this.chord = chord;
        this.time = time;
        this.duration = null;
    }

    public PlayChordRecord(Chord chord, Integer time, Integer duration) {
        this.chord = chord;
        this.time = time;
        this.duration = duration;
    }

    public void decrementTime(int time) {
        this.time -= time;
    }
}
