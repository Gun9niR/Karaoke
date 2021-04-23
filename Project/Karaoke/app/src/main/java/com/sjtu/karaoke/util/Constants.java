package com.sjtu.karaoke.util;

import android.Manifest;
import android.os.Environment;

public class Constants {
    public static String FILE_SAVE_DIR = "/Karaoke/";
    public static final String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    public static final int REQUEST_EXTERNAL_STORAGE = 1;

    public static final int GET_RECORD_AUDIO = 1;

    public static final String[] PERMISSIONS_RECORDER = {
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
    };

    public static final String BASE_DIRECTORY = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) + FILE_SAVE_DIR;

    public static final String WAV_DIRECTORY = BASE_DIRECTORY + "wav/";

    public static final String ACCOMPANY_DIRECTORY = BASE_DIRECTORY + "accompany/";
}