//
// Created by 66328 on 2021/4/29.
//

#ifndef HELLO_LIBS_GETSCORE_H
#define HELLO_LIBS_GETSCORE_H
#include "setting.h"


const int pitchTot = 12 * 10;

class F0data {
public:
    double time;
    double f0;
    int pitchID;
    bool operator< (const F0data &t)const {
        return time < t.time;
    }
    F0data(double _time = 0, double _f0 = 0): time(_time), f0(_f0) {
        extern double baseFreq[200];
        pitchID = lower_bound(baseFreq, baseFreq + pitchTot, f0) - baseFreq;
        if (pitchID + 1 <= pitchTot && fabs(baseFreq[pitchID + 1] - f0) < fabs(baseFreq[pitchID] - f0))  pitchID++;
    }
};

void initbaseFreq();

void init(string cppfilePath, int splitTimeInMicroMS);

string getScore(double startTime, double endTime, double delay);

string getScoreWithDelay(double startTime, double endTime);

int getCorrectnessScore(double startTime, double endTime, double delay);

int getEmotionScore(double startTime, double endTime, double delay);

extern "C" JNIEXPORT jstring JNICALL
Java_com_sjtu_karaoke_singrater_RatingUtil_init(JNIEnv *env, jobject thiz, jstring filePath, jint splitTimeInMicroMS,
                                                jdouble _delayLowerBound, jdouble _delayUpperBound);

extern "C" JNIEXPORT jstring JNICALL
Java_com_sjtu_karaoke_singrater_RatingUtil_getScore(JNIEnv *env, jobject thiz, jint jstartTimeInMicroMS, jint jendTimeInMicroMS);

vector<F0data> getUserF0(double startTime, double endTime, double delay);

#endif //HELLO_LIBS_GETSCORE_H
