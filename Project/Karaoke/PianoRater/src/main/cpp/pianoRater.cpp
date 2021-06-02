#include "pianoRater.h"

Beat *beat;
int chordCount, errorCount, beatCount;
string chordTransPath, *chordName;
double *chord;
double *originChord;
int beatN, beatM;
double startTime, endTime, timePerBeat;

extern "C"
JNIEXPORT jstring JNICALL
Java_com_sjtu_pianorater_PianoRater_getScore(JNIEnv *env, jclass clazz, jstring _chordTransPath,
                                             jint _chordCount, jdoubleArray _chord, jobjectArray _chordName) {
    chordCount = _chordCount;
    errorCount = 0;
    chordTransPath = env->GetStringUTFChars(_chordTransPath,NULL);
    chordName = new string[chordCount];
    chord = new double[chordCount];
    originChord = new double[chordCount];
    for (int i = 0; i < chordCount; i++) {
        jstring str = static_cast<jstring>(env->GetObjectArrayElement(_chordName, i));
        const char* s = env->GetStringUTFChars(str,NULL);
        chordName[i] = s;
        originChord[i] = env->GetDoubleArrayElements(_chord, NULL)[i];
        __android_log_print(ANDROID_LOG_INFO, "Rater",
                            "Java_com_sjtu_pianorater_PianoRater_getScore: "
                            "chordName = %s originChord = %.2lf\n",
                            chordName[i].c_str(), originChord[i]);
    }
    return env->NewStringUTF(runRater().c_str());
}

string runRater() {
    readFile();
    string res = "0";
    for (int i = 0; i <= RATE_PRECISION; i++) {
        for (int j = 0; j < chordCount; j++) {
            if (i <= RATE_PRECISION * 0.5)
                chord[j] = originChord[j] + MIK_DELAY * (i / (RATE_PRECISION * 0.5));
            else
                chord[j] = originChord[j] - MIK_DELAY * ((i - RATE_PRECISION * 0.5) /
                        (RATE_PRECISION * 0.5));
        }
        classifyBeat();
        string tmp = getScore();
        if (stoi(tmp) > stoi(res))  res = tmp;
    }
    recycleSpace();
    return res;
}

void readFile() {
    ifstream file(chordTransPath);
    string str;
    int curBeat = 0;
    //读入文件头
    file >> timePerBeat >> beatN >> beatM >> startTime >> endTime;
    getline(file, str);getline(file, str);
    beatCount = (endTime - startTime) / timePerBeat + 0.5;
    beat = new Beat[beatCount + 5];
    //读入和弦字典
    while (getline(file, str)) {
        if (str == "" || str == "\r")  break;
    }
    //读入和弦
    while (getline(file, str)) {
        if (str == "" || str == "\r")  break;
        stringstream ss(str);
        string arg1;
        int arg2, lastBeat;
        ss >> arg1 >> arg2;
        lastBeat = arg2 / timePerBeat + 0.5;
        for (int i = 0; i < lastBeat; i++) {
            beat[curBeat].chordName = arg1;
            beat[curBeat].id = i;
            __android_log_print(ANDROID_LOG_INFO, "Rater",
                                "readFile: beat[%d].chordName=%s\n",
                                curBeat, beat[curBeat].chordName.c_str());
            curBeat++;
        }

    }
}

void recycleSpace() {
    delete[] chordName;
    delete[] chord;
    delete[] originChord;
    delete[] beat;
}

void Beat::insert(double time) {
    double minval = INT_MAX;
    int id = 0;
    for (int i = 0; i < SPLIT_NUMBER; i++) {
        double timeNow = timePerBeat * i / SPLIT_NUMBER;
        if (fabs(timeNow - time) < minval) {
            minval = fabs(timeNow - time);
            id = i;
        }
    }
    if (delay[id] != -1)    errorCount++;
    else    delay[id] = minval;
}

bool validID(int id) {
    return id >= 0 && id < beatCount;
}

void classifyBeat() {
    errorCount = 0;
    for (int i = 0; i < chordCount; i++) {
        int id = chord[i] / timePerBeat;
        __android_log_print(ANDROID_LOG_INFO, "Rater",
                            "classifyBeat: chordName = %s beat[id].chordName = %s\n",
                            chordName[i].c_str(), beat[id].chordName.c_str());
        if (validID(id) && chordName[i] == beat[id].chordName)
            beat[id].insert(chord[i] - id * timePerBeat);
        else
            if (validID(id + 1) && chord[i] > (id + 0.5) * timePerBeat && chordName[i] == beat[id + 1].chordName)
                beat[id + 1].insert(chord[i] - (id + 1) * timePerBeat);
        else
            if (validID(id - 1) && chord[i] < (id + 0.5) * timePerBeat && chordName[i] == beat[id - 1].chordName)
                beat[id - 1].insert(chord[i] - (id - 1) * timePerBeat);
        else    errorCount++;
    }
}

double Beat::calcScoreWithMath(double delay) {
    double y = -40 * delay * delay + 10;
    return y >= 0 ? y : 0;
}

double Beat::getScore() {
    int cnt = 0;
    double sum = 0;
    for (int i = 0; i < SPLIT_NUMBER; i++) {
        if (delay[i] != -1) {
            sum += calcScoreWithMath(delay[i] / timePerBeat);
            cnt++;
        }
    }
    return cnt == 0 ? 0 : sum / cnt;
}

string getScore() {
    double score = 0;
    for (int i = 0; i < beatCount; i++) {
        score += beat[i].getScore();
    }
    score /= beatCount;
    score -= errorCount * PER_ERR_PUNISH;
    if (chordCount <= beatCount - 3)    score = min(score, 3.0);
    if (chordCount <= beatCount * 1.5 - 3)    score = min(score, 6.0);
    score = max(score, 1.0);
    return to_string(int(score + 0.5));
}












