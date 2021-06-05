package com.sjtu.karaoke.data;

/*
 * @ClassName: Chord
 * @Author: 郭志东
 * @Date: 2021/6/5
 * @Description: 和弦类，包括和弦名，对应的和弦文件路径，和它从SoundPool获得的ID
 */
public class Chord {
    // 和弦名称
    private String name;
    // 和弦文件路径
    private String filePath;
    // SoundPool的ID
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
