package com.sjtu.karaoke;

import android.Manifest;

public class Constants {
    public static String FILE_SAVE_DIR = "/Karaoke";
    public static final String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    public static final int REQUEST_EXTERNAL_STORAGE = 1;
}
