#include <string.h>
#include <jni.h>


extern "C"
JNIEXPORT jstring JNICALL
Java_com_sjtu_pianorater_PianoRater_getScore(JNIEnv *env, jclass clazz, jstring chordTransPath,
                                             jint chordCount, jdoubleArray chord) {
    // TODO: implement getScore()
}