//
// Created by 66328 on 2021/4/29.
//

#include "getscore.h"
#include "setting.h"
#include "f0analysis.h"
#include "utils.h"

double baseFreq[200] = {16.352, 17.324, 18.354, 19.446, 20.602, 21.827,
                        23.125, 24.500, 25.957, 27.501, 29.136, 30.868};
char originF0Path[70];
double splittime;
double delayLowerBound;
double delayUpperBound;
vector<F0data> originf0;


vector<F0data> getUserF0(double startTime, double endTime, double delay) {
    vector<string> files;
    int cnt = utils::scanDir(dataDir, files);
    vector<F0data> userf0;
    for (int i = 0; i < cnt; i++) {
        stringstream filenamestream(files[i]);
        int sttimeMs;
        filenamestream >> sttimeMs;
        double sttime = sttimeMs / 1000.0 + delay;
        if (sttime > endTime || sttime + splittime < startTime)   continue;
        string str;
        ifstream file(dataDir + files[i]);
        while (getline(file, str)) {
            stringstream ss(str);
            double a1, a2;
            ss >> a1 >> a2;
            userf0.push_back(F0data(a1 + sttime, a2));
        }
        file.close();
    }
    sort(userf0.begin(), userf0.end());
    return userf0;
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_sjtu_karaoke_singrater_RatingUtil_getScore(JNIEnv *env, jobject thiz, jint jstartTimeInMicroMS, jint jendTimeInMicroMS) {
    int startTimeInMicroMS = jstartTimeInMicroMS;
    int endTimeInMicroMS = jendTimeInMicroMS;
    return env->NewStringUTF(getScoreWithDelay(startTimeInMicroMS / 1000.0, endTimeInMicroMS / 1000.0).c_str());
}

int getFirstScore(string s) {
    stringstream ss(s);
    int x;
    ss >> x;
    return x;
}

string getScoreWithDelay(double startTime, double endTime) {
    string res, tmp;
    for (double delay = delayLowerBound; delay <= delayUpperBound; delay += f0Shift) {
        tmp = getScore(startTime, endTime, delay);
        if (res == "" || getFirstScore(res) < getFirstScore(tmp)) res = tmp;
    }
    return res;
}

string getScore(double startTime, double endTime, double delay) {
    string res;
    int CorrectnessScore = getCorrectnessScore(startTime, endTime, delay);
    //getEmotionScore(startTime, endTime, delay);
    if (CorrectnessScore > correctnessUpperThreshold) {
        res += to_string(CorrectnessScore);
        res += " " + to_string(CorrectnessScore);
        res += " " + to_string(CorrectnessScore);
        res += " " + to_string(CorrectnessScore);
    }
    else {
        if (CorrectnessScore > correctnessLowerThreshold)
            CorrectnessScore = CorrectnessScore + (correctnessUpperThreshold - CorrectnessScore) / 3;
        res += to_string(CorrectnessScore);
        res += " " + to_string(CorrectnessScore);
        res += " " + to_string(CorrectnessScore);
        res += " " + to_string(CorrectnessScore);
    }
    return res;
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_sjtu_karaoke_singrater_RatingUtil_init(JNIEnv *env, jobject thiz, jstring filePath, jint splitTimeInMicroMS,
                                                jdouble _delayLowerBound, jdouble _delayUpperBound) {
    jboolean icCopy = 0;
    string cppfilePath = env->GetStringUTFChars(filePath, &icCopy);
    delayLowerBound = _delayLowerBound;
    delayUpperBound = _delayUpperBound;
    init(cppfilePath, splitTimeInMicroMS);
    return env->NewStringUTF("Done");
}

void init(string cppfilePath, int splitTimeInMicroMS) {
    memset(originF0Path, 0 ,sizeof (originF0Path));
    originf0.clear();
    strcpy(originF0Path, cppfilePath.c_str());
    splittime = splitTimeInMicroMS / 1000.0;
    vector<string> files;
    system(("rm -r " + string(dataDir)).c_str());
    system(("mkdir " + string(dataDir)).c_str());
    initbaseFreq();
    string str;
    ifstream file(originF0Path);
    while (getline(file, str)) {
        stringstream ss(str);
        double a1, a2;
        ss >> a1 >> a2;
        originf0.push_back(F0data(a1, a2));
    }
    file.close();
}

void initbaseFreq() {
    for (int i = 1; i <= 9; i++)
        for (int j = 0; j < 12; j++) {
            int id = j + 12 * i;
            baseFreq[id] = baseFreq[id - 12] * 2;
        }
}


int getCorrectnessScore(double startTime, double endTime, double delay) {
    vector<F0data> userf0 = getUserF0(startTime, endTime, delay);

    int userNowLowerID = 0, userNowUpperID = 0;
    double rightScore = 0, wrongPenalty = 0;
    for (int i = 0; i < originf0.size(); i++) {
        double sttime = originf0[i].time;
        if (sttime > endTime || sttime + splittime < startTime)   continue;
        while (userNowLowerID + 1 < userf0.size() && userf0[userNowLowerID].time + correctnessInterval < sttime) userNowLowerID++;
        while (userNowUpperID + 1 < userf0.size() && userf0[userNowUpperID].time - correctnessInterval < sttime) userNowUpperID++;
        int maxPitch = INT_MIN, minPitch = INT_MAX;
        for (int j = userNowLowerID; j < userf0.size() && j <= userNowUpperID; j++) {
            minPitch = min(minPitch, userf0[j].pitchID);
            maxPitch = max(maxPitch, userf0[j].pitchID);
        }
        if (originf0[i].pitchID >= minPitch && originf0[i].pitchID <= maxPitch) rightScore += OnceRightScore;
        else {
            if ((originf0[i].pitchID + 1 >= minPitch && originf0[i].pitchID + 1 <= maxPitch) ||
                (originf0[i].pitchID - 1 >= minPitch && originf0[i].pitchID - 1 <= maxPitch))   wrongPenalty += OnceNotCorrectScore;
            else   wrongPenalty += OnceWrongScore;
        }
    }

    int res = rightScore / (rightScore + wrongPenalty) * 100 + 0.5;
    return res;
}

int getEmotionScore(double startTime, double endTime, double delay) {
    vector<F0data> userf0 = getUserF0(startTime, endTime, delay);
    int last = 0;
    double delta, minf0(INT_MAX), maxf0(INT_MIN);
    for (int i = 0; i < userf0.size(); i++) {
        if (userf0[last].pitchID != userf0[i].pitchID) {
            delta = (maxf0 - minf0) / (baseFreq[userf0[last].pitchID + 1] - baseFreq[userf0[last].pitchID]);
            __android_log_print(ANDROID_LOG_INFO, "Rater",
                                "pitchID = %d EmotionScore = %d\n", userf0[last].pitchID, delta);
            last = i;
            minf0 = INT_MAX;
            maxf0 = INT_MIN;
        }
        minf0 = min(minf0, userf0[i].f0);
        maxf0 = max(maxf0, userf0[i].f0);
    }
    __android_log_print(ANDROID_LOG_INFO, "Rater",
                        "pitchID = %d EmotionScore = %d\n", userf0[last].pitchID, delta);
    return 0;
}
