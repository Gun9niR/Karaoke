#include "setting.h"

class Beat {
public:
    Beat(string _chordName = ""): chordName(_chordName) {
        memset(delay, -1, sizeof(delay));
    }
    int id;
    string chordName;
    int delay[SPLIT_NUMBER + 1];
    void insert(double time);
    double getScore();
    double calcScoreWithMath(double delay);
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