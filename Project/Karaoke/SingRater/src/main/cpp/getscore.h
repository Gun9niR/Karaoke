//
// Created by 66328 on 2021/4/29.
//

#ifndef HELLO_LIBS_GETSCORE_H
#define HELLO_LIBS_GETSCORE_H
#include "setting.h"


const int pitchTot = 12 * 10;//音高总数目

//基频分析数据
class F0data {
public:
    double time;//时刻
    double f0;//基频
    int pitchID;//音高ID
    bool operator< (const F0data &t)const {
        return time < t.time;
    }
    F0data(double _time = 0, double _f0 = 0): time(_time), f0(_f0) {
        extern double baseFreq[200];
        pitchID = lower_bound(baseFreq, baseFreq + pitchTot, f0) - baseFreq;//二分查找确定音高ID
        if (pitchID + 1 <= pitchTot && fabs(baseFreq[pitchID + 1] - f0) < fabs(baseFreq[pitchID] - f0))  pitchID++;
    }
};

//根据十二平均律原理计算每个音高对应的频率
void initbaseFreq();

//对歌唱打分系统进行初始化
void init(string cppfilePath, int splitTimeInMicroMS);

//获取音准得分
string getAccuracyScore(double startTime, double endTime, double delay);

//对带延迟演唱进行综合评分
string getScoreWithDelay(double startTime, double endTime);

//获取音准打分
int getCorrectnessScore(double startTime, double endTime, double delay);

//获取情感打分
int getEmotionScore(double startTime, double endTime, double delay, int accuracyScore);

//获取气息得分
int getBreathScore(double startTime, double endTime, double delay, int accuracyScore);

//获取字符串s中的第一个整数
int getFirstInterger(string s);

//标准化得分
void constraintScore(int accuracyScore, int &scoreToConstraint);

//JNI接口
extern "C" JNIEXPORT jstring JNICALL
Java_com_sjtu_karaoke_singrater_RatingUtil_init(JNIEnv *env, jobject thiz, jstring filePath, jint splitTimeInMicroMS,
                                                jdouble _delayLowerBound, jdouble _delayUpperBound);

//JNI接口
extern "C" JNIEXPORT jstring JNICALL
Java_com_sjtu_karaoke_singrater_RatingUtil_getScore(JNIEnv *env, jobject thiz, jint jstartTimeInMicroMS, jint jendTimeInMicroMS);

//从文件读取用户的基频分析数据
vector<F0data> getUserF0(double startTime, double endTime, double delay);

//获取原唱的基频分析数据
vector<F0data> getOriginF0(double startTime, double endTime);

//输出某个基频分析文件
void printUserF0(const vector<F0data> &userF0);

//获取一段基频分析文件中的ContinuousBlankCnt
int getContinuousBlankCnt(const vector<F0data> &F0);

//获取一段基频分析文件中的EndBlankCnt
int getEndBlankCnt(const vector<F0data> &F0);

//获取一段基频分析文件中的EmotionDelta
double getEmotionDelta(const vector<F0data> &F0);

#endif //HELLO_LIBS_GETSCORE_H
