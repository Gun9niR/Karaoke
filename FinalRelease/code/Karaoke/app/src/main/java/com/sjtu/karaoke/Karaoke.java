package com.sjtu.karaoke;

import android.app.Application;
import android.content.res.Resources;

/*
 * @ClassName: Karaoke
 * @Author: 郭志东
 * @Date: 2021/6/5
 * @Description: App的资源提供类，使的各个工具方法不需要获得context也可以直接获取各种资源
 */
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
