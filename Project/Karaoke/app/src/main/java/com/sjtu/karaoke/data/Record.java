package com.sjtu.karaoke.data;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Objects;

import static com.sjtu.karaoke.data.Rank.strToRank;
import static com.sjtu.karaoke.util.PathUtil.getRecordCoverFullPath;
import static com.sjtu.karaoke.util.PathUtil.getRecordFileFullPath;
import static com.sjtu.karaoke.util.PathUtil.getRecordMetadataFullPath;

/*
 * @ClassName: Record
 * @Author: 郭志东
 * @Date: 2021/6/5
 * @Description: 录音类，存储录音歌名、时间、等级、封面、路径
 * 每个录音都保存在一个目录中，目录名为一个独特的字符串。
 * 目录中的内容包括：
 *  |
 *  |---- cover.png         (专辑封面)
 *  |---- <song name>.wav   (录音文件)
 *  |---- metadata.txt      (录音时间、等级)
 */
public class Record {
    // 歌名
    private final String songName;
    // 录音时间
    private final String recordTime;
    // 录音等级
    private final Rank rank;

    // uuid目录名
    private final String dirFullPath;
    // 录音的.wav文件绝对路径，存储这个路径是为了在点击播放录音时，不需要每次都重新算一次路径
    private final String recordFullPath;
    // 为了使专辑封面在没网的时候也可以正常显示，保存了专辑封面的snapshot
    private final String albumCoverFullPath;

    /**
     * 通过传入录音所在的绝对路径来初始化录音
     * @param dirFullPath 绝对！路径
     * @throws ParseException 当元数据中的日期格式错误时抛出
     */
    public Record(String dirFullPath) throws ParseException, IOException {
        BufferedReader reader;
        this.dirFullPath = dirFullPath;

        // 设置专精封面路径
        albumCoverFullPath = getRecordCoverFullPath(dirFullPath);

        // 解析元数据
        reader = new BufferedReader(
                new FileReader(getRecordMetadataFullPath(dirFullPath)));

        // 歌曲名
        songName = reader.readLine();

        // 日期
        String dateStr = reader.readLine();
        SimpleDateFormat format = new SimpleDateFormat(
                "yyyy-MM-dd-HH-mm",
                Locale.CHINA);
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(Objects.requireNonNull(format.parse(dateStr)));
        recordTime = String.format(Locale.CHINA, "%d-%02d-%02d %02d:%02d",
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH) + 1,
                calendar.get(Calendar.DAY_OF_MONTH),
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE));

        // 等级
        String rankStr = reader.readLine();
        rank = strToRank.get(rankStr);

        // 录音文件路径
        recordFullPath = getRecordFileFullPath(dirFullPath, songName);
    }

    public String getDirFullPath() {
        return dirFullPath;
    }

    public String getRecordFullPath() { return recordFullPath; }

    public String getSongName() {
        return songName;
    }

    public String getRecordTime() {
        return recordTime;
    }

    public Rank getRank() {
        return rank;
    }

    public String getAlbumCoverFullPath() {
        return albumCoverFullPath;
    }
}
