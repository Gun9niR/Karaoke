package com.sjtu.karaoke.data;

/*
 * @ClassName: PlayChordRecord
 * @Author: 郭志东
 * @Date: 2021/6/5
 * @Description: 和弦弹奏记录类，既可以用于存储标准的弹奏顺序，也可以存储用户的弹奏顺序
 */
public class PlayChordRecord {
    // 本次弹奏弹的和弦
    Chord chord;
    // 弹奏开始时间
    Integer time;
    // 本次弹奏的持续时间（目前只用于决定下一次弹奏开始的时间）
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
