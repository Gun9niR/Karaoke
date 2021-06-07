//
// Created by 66328 on 2021/4/29.
//

#ifndef HELLO_LIBS_F0ANALYSIS_H
#define HELLO_LIBS_F0ANALYSIS_H
#include "setting.h"

//JNI接口
extern "C" JNIEXPORT jstring JNICALL
Java_com_sjtu_karaoke_singrater_RatingUtil_f0analysis(JNIEnv *env, jobject thiz, jstring filePath, jint jstartTimeInMicroMS);

#endif //HELLO_LIBS_F0ANALYSIS_H
