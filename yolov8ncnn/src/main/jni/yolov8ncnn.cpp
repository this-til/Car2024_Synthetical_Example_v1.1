// Tencent is pleased to support the open source community by making ncnn available.
//
// Copyright (C) 2021 THL A29 Limited, a Tencent company. All rights reserved.
//
// Licensed under the BSD 3-Clause License (the "License"); you may not use this file except
// in compliance with the License. You may obtain a copy of the License at
//
// https://opensource.org/licenses/BSD-3-Clause
//
// Unless required by applicable law or agreed to in writing, software distributed
// under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
// CONDITIONS OF ANY KIND, either express or implied. See the License for the
// specific language governing permissions and limitations under the License.


#include "yolov8ncnn.h" 

#if __ARM_NEON
#include <arm_neon.h>
#endif // __ARM_NEON


static AAssetManager* mgr;
static std::unordered_map<std::string, Yolo*> map;
//struct Yolo* yolo;

static NdkCameraWindow* g_camera = 0;

static jclass objCls = NULL;
static jmethodID constructortorId;
static jfieldID xId;
static jfieldID yId;
static jfieldID wId;
static jfieldID hId;
static jfieldID labelId;
static jfieldID probId;
// 先定义返回的数据
//ncnn::Mutex lock;

const float mean_val[] = {103.53f, 116.28f, 123.675f};
const float norm_val[] = {1 / 255.f, 1 / 255.f, 1 / 255.f};

jint OnLoad(JavaVM* vm, void* reserved) {
    __android_log_print(ANDROID_LOG_DEBUG, "ncnn", "JNI_OnLoad");
    g_camera = new NdkCameraWindow;
    return JNI_VERSION_1_4;
}

void OnUnload(JavaVM* vm, void* reserved) {
    __android_log_print(ANDROID_LOG_DEBUG, "ncnn", "JNI_OnUnload");
    /*{
        ncnn::MutexLockGuard g(lock);
        for (const auto &[key, value]: map) {
            delete value;
        }
        map.clear();
    }*/

    for (const auto &[key, value]: map) {
        delete value;
    }
    map.clear();
    
    delete g_camera;
    g_camera = 0;
}

void init(JNIEnv* env, jobject thiz, jobject assetManager) {
    mgr = AAssetManager_fromJava(env, assetManager);
    
    __android_log_print(ANDROID_LOG_DEBUG, "ncnn", "loadModel %p", mgr);

    // 获取 Java 类和构造函数
    jclass localObjCls = env->FindClass("com/yolov8ncnn/Yolov8Ncnn$Obj");
    if (localObjCls == nullptr) {
        env->ThrowNew(env->FindClass("java/lang/UnsupportedOperationException"),
                      "No com.yolov8ncnn.Yolov8Ncnn.Obj Class");
        return;
    }

    objCls = reinterpret_cast<jclass>(env->NewGlobalRef(localObjCls));
    constructortorId = env->GetMethodID(objCls, "<init>", "()V");
    xId = env->GetFieldID(objCls, "x", "F");
    yId = env->GetFieldID(objCls, "y", "F");
    wId = env->GetFieldID(objCls, "w", "F");
    hId = env->GetFieldID(objCls, "h", "F");
    labelId = env->GetFieldID(objCls, "label", "I");
    probId = env->GetFieldID(objCls, "prob", "F");

    if (
            constructortorId == nullptr
            || xId == nullptr
            || yId == nullptr
            || wId == nullptr
            || hId == nullptr
            || labelId == nullptr
            || probId == nullptr
            ) {
        env->ThrowNew(env->FindClass("java/lang/UnsupportedOperationException"),
                      "No field in com.yolov8ncnn.Yolov8Ncnn.Obj Class");
        return;
    }

}

void loadModel(JNIEnv* env, jobject thiz, jstring modelName, jint itemSize, jstring extractBlobName, jint targetSizes, jboolean useGpu) {
    const char* nativeModelName;
    const char* nativeExtractBlobName;
    std::string nativeModelNameStr;
    Yolo* yolo;
    char* copyStr;
    size_t strLen;
      
    if (useGpu && ncnn::get_gpu_count() == 0) {
        env->ThrowNew(env->FindClass("java/lang/UnsupportedOperationException"), "No GPU support");
        goto end;
    }
 
    nativeModelName = env->GetStringUTFChars(modelName, nullptr);
    nativeExtractBlobName = env->GetStringUTFChars(extractBlobName, nullptr);

    if (!nativeModelName || !nativeExtractBlobName) {
        env->ThrowNew(env->FindClass("java/lang/OutOfMemoryError"), "Failed to convert Java string to UTF-8");
        goto end;
    }

    nativeModelNameStr = nativeModelName;

    if (map.find(nativeModelNameStr) != map.end()) {
       goto end;
    }
 
    yolo = new Yolo();

    strLen = strlen(nativeExtractBlobName) + 1;
    copyStr = new char[strLen];
    strcpy(copyStr, nativeExtractBlobName);
    yolo->extractBlobName = copyStr;

    yolo->itemSize = itemSize;
    yolo->load(
            mgr,
            nativeModelName,
            targetSizes,
            mean_val,
            norm_val,
            useGpu
    );
    map.insert({nativeModelNameStr, yolo});

    end:

    if (nativeModelName) {
        env->ReleaseStringUTFChars(modelName, nativeModelName);
    }
    if (nativeExtractBlobName) {
        env->ReleaseStringUTFChars(extractBlobName, nativeExtractBlobName);
    }
    /*if (nativeModelNameStr) {
        delete nativeModelNameStr
    }*/

}

jobjectArray detect(JNIEnv* env, jobject thiz, jobject bitmap, jstring modelName) {

    jobjectArray jObjArray = NULL;
    
    const char* nativeModelName;
    std::string nativeModelNameStr;
    
    Yolo *yolo;
    std::vector <Object> objects;
    
    AndroidBitmapInfo info;
    AndroidBitmap_getInfo(env, bitmap, &info);
    const int width = info.width;
    const int height = info.height;
    if (info.format != ANDROID_BITMAP_FORMAT_RGBA_8888) {
        env->ThrowNew(env->FindClass("java/lang/UnsupportedOperationException"), "bitmap is not RGBA_8888");
        goto end;
    }

    nativeModelName = env->GetStringUTFChars(modelName, nullptr);

    if (!nativeModelName) {
        env->ThrowNew(env->FindClass("java/lang/OutOfMemoryError"), "Failed to convert Java string to UTF-8");
        goto end;
    }

    nativeModelNameStr = nativeModelName;

    if (map.find(nativeModelNameStr) == map.end()) {
        env->ThrowNew(env->FindClass("java/lang/UnsupportedOperationException"), "model not loaded");
        goto end;
    }

   
    yolo = map[nativeModelName];
    yolo->detectPicture(env, bitmap, width, height, objects);

    jObjArray = env->NewObjectArray(objects.size(), objCls, NULL);

    for (size_t i = 0; i < objects.size(); i++) {
        jobject jObj = env->NewObject(objCls, constructortorId);
        env->SetFloatField(jObj, xId, objects[i].rect.x);
        env->SetFloatField(jObj, yId, objects[i].rect.y);
        env->SetFloatField(jObj, wId, objects[i].rect.width);
        env->SetFloatField(jObj, hId, objects[i].rect.height);
        env->SetIntField(jObj, labelId, objects[i].label);
        env->SetFloatField(jObj, probId, objects[i].prob);
        env->SetObjectArrayElement(jObjArray, i, jObj);
    }

    end:
    if (nativeModelName) {
        env->ReleaseStringUTFChars(modelName, nativeModelName);
    }
    /*if (nativeModelNameStr) {
        delete nativeModelNameStr;
    }*/

    return jObjArray;
}

void openCamera(JNIEnv* env, jobject thiz, jint facing, jstring modelName) {
    
     const char* nativeModelName;
        std::string nativeModelNameStr;

    if (facing < 0 || facing > 1) {
        env->ThrowNew(env->FindClass("java/lang/UnsupportedOperationException"), "open camera it must be a front or rear camera");
        goto end;
    }


    if (!nativeModelName) {
        env->ThrowNew(env->FindClass("java/lang/OutOfMemoryError"), "Failed to convert Java string to UTF-8");
        goto end;
    }
    
    nativeModelNameStr = nativeModelName;
    
    if (map.find(nativeModelNameStr) == map.end()) {
        env->ThrowNew(env->FindClass("java/lang/UnsupportedOperationException"),  " model not loaded");
        goto end;
    }
    
    g_camera->open((int) facing);
    g_camera->yolo = map[nativeModelName];
    
   __android_log_print(ANDROID_LOG_DEBUG, "ncnn", "openCamera %d", facing);
    
    end:
    if (nativeModelName) { 
        env->ReleaseStringUTFChars(modelName, nativeModelName);
    }
    /*if(nativeModelNameStr) {
        delete nativeModelName;
    }*/
}

void closeCamera(JNIEnv* env, jobject thiz) {
    __android_log_print(ANDROID_LOG_DEBUG, "ncnn", "closeCamera");
    g_camera->close();
}

void setOutputWindow(JNIEnv* env, jobject thiz, jobject surface) {
    ANativeWindow* win = ANativeWindow_fromSurface(env, surface);
    __android_log_print(ANDROID_LOG_DEBUG, "ncnn", "setOutputWindow %p", win);
    g_camera->set_window(win);
}

extern "C"  {

JNIEXPORT jint JNI_OnLoad(JavaVM * vm , void* reserved) {
    return OnLoad(vm, reserved);
}

JNIEXPORT void JNI_OnUnload(JavaVM * vm, void * reserved) {
     OnUnload(vm, reserved);
}

JNIEXPORT void JNICALL Java_com_yolov8ncnn_Yolov8Ncnn_init(JNIEnv* env, jobject thiz, jobject assetManager, jboolean use_gpu) {
    init(env, thiz, assetManager);
}

JNIEXPORT void JNICALL Java_com_yolov8ncnn_Yolov8Ncnn_loadModel(JNIEnv* env, jobject thiz, jstring modelName, jint itemSize, jstring extractBlobName,jint targetSizes, jboolean useGpu)  {
    loadModel(env, thiz, modelName, itemSize , extractBlobName, targetSizes, useGpu);
}

JNIEXPORT jobjectArray JNICALL Java_com_yolov8ncnn_Yolov8Ncnn_detect(JNIEnv* env, jobject thiz, jobject bitmap, jstring modelName) {
    return detect(env, thiz, bitmap, modelName);
}

JNIEXPORT void JNICALL Java_com_yolov8ncnn_Yolov8Ncnn_openCamera(JNIEnv* env, jobject thiz, jint facing, jstring modelName) {
    openCamera(env, thiz, facing, modelName);
}

JNIEXPORT void Java_com_yolov8ncnn_Yolov8Ncnn_closeCamera(JNIEnv* env, jobject thiz) {
    closeCamera(env, thiz);
}

JNIEXPORT void Java_com_yolov8ncnn_Yolov8Ncnn_setOutputWindow(JNIEnv* env, jobject thiz, jobject surface) {
    setOutputWindow(env, thiz , surface);
}

}
