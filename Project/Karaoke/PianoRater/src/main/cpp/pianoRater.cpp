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
        originChord[i] = *env->GetDoubleArrayElements(_chord, NULL);
        __android_log_print(ANDROID_LOG_INFO, "Rater",
                            "获取java的参数:%s\n", s);
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
                chord[j] = originChord[j] - MIK_DELAY * ((i - RATE_PRECISION * 0.5) / (RATE_PRECISION * 0.5));
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
    beat = new Beat[beatCount];
    //读入和弦字典
    while (getline(file, str)) {
        if (str == "")  break;
    }
    //读入和弦
    while (getline(file, str)) {
        if (str == "")  break;
        stringstream ss(str);
        string arg1;
        int arg2, lastBeat;
        ss >> arg1 >> arg2;
        lastBeat = arg2 / timePerBeat + 0.5;
        for (int i = 0; i < lastBeat; i++)
            beat[++curBeat].chordName = arg1;
    }
}

void recycleSpace() {
    delete[] chordName;
    delete[] chord;
    delete[] originChord;
    delete[] beat;
}

void classifyBeat() {
    errorCount = 0;
    for (int i = 0; i < chordCount; i++) {

    }
}













