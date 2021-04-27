package com.sjtu.karaoke.entity;

import android.annotation.SuppressLint;

import org.apache.commons.io.FilenameUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class Record {
    private final Integer id;
    private final String fullPath;
    private final String songName;
    private final String recordTime;

    @SuppressLint("DefaultLocale")
    public Record(String fullPath) throws ParseException {
        this.fullPath = fullPath;

        String fileName = FilenameUtils.getBaseName(fullPath);

        id = Integer.parseInt(fileName.substring(0, fileName.indexOf('-')));

        String fileNameWithoutId = fileName.substring(fileName.indexOf('-' + 1));
        songName = fileNameWithoutId.substring(0, fileNameWithoutId.indexOf('-'));
        String dateString = fileNameWithoutId.substring(fileName.indexOf('-') + 1);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd-HH-mm");
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(format.parse(dateString));
        this.recordTime = String.format("%d.%d.%d %d:%d",
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH) + 1,
                calendar.get(Calendar.DAY_OF_MONTH),
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE));
    }

    public Integer getId() {
        return id;
    }

    public String getFullPath() { return fullPath; }

    public String getSongName() {
        return songName;
    }

    public String getRecordTime() {
        return recordTime;
    }
}
