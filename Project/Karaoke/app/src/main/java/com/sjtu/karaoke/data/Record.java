package com.sjtu.karaoke.data;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

import static com.sjtu.karaoke.data.Rank.strToRank;
import static com.sjtu.karaoke.util.PathUtil.getRecordCoverFullPath;
import static com.sjtu.karaoke.util.PathUtil.getRecordFileFullPath;
import static com.sjtu.karaoke.util.PathUtil.getRecordMetadataFullPath;

public class Record {
    private final String songName;
    private final String recordTime;
    private final Rank rank;

    private final String dirFullPath;
    private final String recordFullPath;
    private final String albumCoverFullPath;

    /**
     * Given full path to the directory of a record, initialize song name, date and rating and file
     * @param dirFullPath
     * @throws ParseException
     */
    public Record(String dirFullPath) throws ParseException, IOException {
        BufferedReader reader;

        this.dirFullPath = dirFullPath;
        // set album cover full path
        albumCoverFullPath = getRecordCoverFullPath(dirFullPath);

        // read metadata
        reader = new BufferedReader(
                new FileReader(getRecordMetadataFullPath(dirFullPath)));
        songName = reader.readLine();

        String dateStr = reader.readLine();
        SimpleDateFormat format = new SimpleDateFormat(
                "yyyy-MM-dd-HH-mm",
                Locale.CHINA);
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(format.parse(dateStr));
        recordTime = String.format(Locale.CHINA, "%d-%02d-%02d %02d:%02d",
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH) + 1,
                calendar.get(Calendar.DAY_OF_MONTH),
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE));

        String rankStr = reader.readLine();
        rank = strToRank.get(rankStr);

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
