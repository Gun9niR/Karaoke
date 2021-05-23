#include "setting.h"

class Beat {
public:
    Beat(string _chordName = ""): chordName(_chordName) {
        delay[1] = -1;
        delay[2] = -1;
        delay[3] = -1;
        delay[4] = -1;
    }
    string chordName;
    double delay[5];
    double getScore();
};

extern "C"
JNIEXPORT jstring JNICALL
Java_com_sjtu_pianorater_PianoRater_getScore(JNIEnv *env, jclass clazz, jstring _chordTransPath,
                                             jint _chordCount, jdoubleArray _chord, jobjectArray _chordName);

string runRater();

void readFile();

void classifyBeat();

string getScore();

void recycleSpace();