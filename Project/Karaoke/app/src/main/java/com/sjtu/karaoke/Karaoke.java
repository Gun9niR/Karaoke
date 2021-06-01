package com.sjtu.karaoke;

import android.app.Application;
import android.content.res.Resources;

public class Karaoke extends Application {
    private static Resources res;

    @Override
    public void onCreate() {
        super.onCreate();
        res = getResources();
    }

    public static Resources getRes() {
        return res;
    }
}
