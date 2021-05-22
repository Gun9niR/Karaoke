package com.sjtu.karaoke.entity;

public class Chord {
    private String name;
    private String filePath;
    private Integer soundId;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public Integer getSoundId() {
        return soundId;
    }

    public void setSoundId(Integer soundId) {
        this.soundId = soundId;
    }

    public Chord(String name, String filePath) {
        this.name = name;
        this.filePath = filePath;
    }
}
