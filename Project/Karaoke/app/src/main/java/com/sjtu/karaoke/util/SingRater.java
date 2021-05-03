package com.sjtu.karaoke.util;

public class SingRater {
    //对filePath这个wav文件进行基频分析。以伴奏时钟为参考基准，其起始时间为jstartTimeInMicroMS毫秒，
    //返回算法消耗的时间。可以并行。注意，不可以对同一时刻的音频多次调用该函数。
    public native String f0analysis(String filePath, int jstartTimeInMicroMS);

    //以伴奏时钟为参考基准，获取startTimeInMicroMS毫秒至endTimeInMicroMS毫秒的单句演唱打分，
    //返回以空格隔开且不超过100的四个非负整数，依次代表单句总分，单句音准与节奏得分，单句感情得分与单句气息得分。
    //注意，调用该函数之前，必须先对[startTimeInMicroMS + delayLowerBound, endTimeInMicroMS + delayUpperBound]
    //区间内的全部音频调用过f0analysis，
    //且该区间内全部的f0analysis调用已经正常返回。
    public native String getScore(int startTimeInMicroMS, int endTimeInMicroMS);

    //对歌唱打分系统进行初始化，filePath为原唱人声的基频文件(拓展名为f0a)，
    //splitTimeInMicroMS代表以毫秒为单位相邻两次调用f0analysis的间隔时间，
    //delayLowerThreshold和delayUpperThreshold分别代表用户录音时钟相对伴奏时钟延迟下界与上界。
    //若初始化正常运行，则返回字符串为"Done"，不含引号。
    //注意，在初始化结束之前，不可以调用任何歌唱打分系统的函数。
    public native String init(String filePath, int splitTimeInMicroMS, double delayLowerBound, double delayUpperBound);

    //加载实时歌唱打分系统所需动态链接库。注意，该系统只能在AccompanySingActivity中使用。
    static {
        System.loadLibrary("WORLD");
    }
}
