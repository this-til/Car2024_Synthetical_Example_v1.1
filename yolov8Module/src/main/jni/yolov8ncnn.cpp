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

#include <android/asset_manager_jni.h>
#include <android/native_window_jni.h>
#include <android/native_window.h>

#include <android/log.h>

#include <jni.h>

#include <string>
#include <vector>

#include <platform.h>
#include <benchmark.h>

#include "yolo.h"

#include "ndkcamera.h"

#include <opencv2/core/core.hpp>
#include <opencv2/imgproc/imgproc.hpp>

#if __ARM_NEON
#include <arm_neon.h>
#endif // __ARM_NEON


static AAssetManager* mgr;
static bool useGpu;
//static std::unordered_map <std::string, Yolo> map;
struct Yolo* yolo;

static jclass objCls = NULL;
static jmethodID constructortorId;
static jfieldID xId;
static jfieldID yId;
static jfieldID wId;
static jfieldID hId;
static jfieldID labelId;
static jfieldID probId;
// 先定义返回的数据
static ncnn::Mutex lock;

const int target_sizes[] = {320,
                            320,
                            640,
                            640,
                            640,
                            1280,
                            640,
                            640,
                            1280,};
/*const float mean_vals[][3] = {{103.53f, 116.28f, 123.675f},
                              {103.53f, 116.28f, 123.675f},
                              {103.53f, 116.28f, 123.675f},
                              {103.53f, 116.28f, 123.675f},
                              {103.53f, 116.28f, 123.675f},
                              {103.53f, 116.28f, 123.675f},
                              {103.53f, 116.28f, 123.675f},
                              {103.53f, 116.28f, 123.675f},
                              {103.53f, 116.28f, 123.675f},};
const float norm_vals[][3] = {{1 / 255.f, 1 / 255.f, 1 / 255.f},
                              {1 / 255.f, 1 / 255.f, 1 / 255.f},
                              {1 / 255.f, 1 / 255.f, 1 / 255.f},
                              {1 / 255.f, 1 / 255.f, 1 / 255.f},
                              {1 / 255.f, 1 / 255.f, 1 / 255.f},
                              {1 / 255.f, 1 / 255.f, 1 / 255.f},
                              {1 / 255.f, 1 / 255.f, 1 / 255.f},
                              {1 / 255.f, 1 / 255.f, 1 / 255.f},
                              {1 / 255.f, 1 / 255.f, 1 / 255.f},};*/

const float mean_val[] = {103.53f, 116.28f, 123.675f};
const float norm_val[] = {1 / 255.f, 1 / 255.f, 1 / 255.f};

extern "C"

JNIEXPORT jboolean

JNICALL Java_com_yolov8ncnn_Yolov8Ncnn_init(JNIEnv* env, jobject thiz, jobject assetManager, jboolean use_gpu) {
    mgr = AAssetManager_fromJava(env, assetManager);
    yolo = new Yolo;

    useGpu = use_gpu;
    if (useGpu && ncnn::get_gpu_count()) {
        env->ThrowNew(env->FindClass("java/lang/UnsupportedOperationException"), "No GPU support");
        return JNI_FALSE;
    }

    __android_log_print(ANDROID_LOG_DEBUG, "ncnn", "loadModel %p", mgr);

    // 获取 Java 类和构造函数
    jclass localObjCls = env->FindClass("com/yolov8ncnn/Yolov8Ncnn$Obj");
    if (localObjCls == nullptr) {
        __android_log_print(ANDROID_LOG_ERROR, "ncnn", "FindClass failed");
        return JNI_FALSE;
    }

    objCls = reinterpret_cast<jclass>(env->NewGlobalRef(localObjCls));
    constructortorId = env->GetMethodID(objCls, "<init>", "()V");
    xId = env->GetFieldID(objCls, "x", "F");
    yId = env->GetFieldID(objCls, "y", "F");
    wId = env->GetFieldID(objCls, "w", "F");
    hId = env->GetFieldID(objCls, "h", "F");
    labelId = env->GetFieldID(objCls, "label", "I");
    probId = env->GetFieldID(objCls, "prob", "F");

    if (constructortorId == nullptr || xId == nullptr || yId == nullptr || wId == nullptr || hId == nullptr ||
        labelId == nullptr || probId == nullptr) {
        __android_log_print(ANDROID_LOG_ERROR, "ncnn", "GetFieldID or GetMethodID failed");
        return JNI_FALSE;
    }

    return JNI_TRUE;
}

JNIEXPORT void JNICALL
Java_com_yolov8ncnn_Yolov8Ncnn_loadModel(JNIEnv
* env,
jobject thiz, jstring
nodelName) {

const char* nativeString = env->GetStringUTFChars(nodelName, nullptr);

if (!nativeString) {
env->
ThrowNew(env
->FindClass("java/lang/OutOfMemoryError"), "Failed to convert Java string to UTF-8");
return;
}



{
ncnn::MutexLockGuard g(lock);
yolo->
load(
        mgr,
        nativeString,
640,
mean_val,
norm_val,
useGpu
);
}
}

JNIEXPORT jobjectArray

JNICALL Java_yolov8ncnn_Yolov8Ncnn_Detect(JNIEnv * env, jobject
thiz,
jobject bitmap, jboolean
use_gpu,
jint modelid
) {
double start_time = ncnn::get_current_time();
AndroidBitmapInfo info;
AndroidBitmap_getInfo(env, bitmap, &info
);
const int width = info.width;
const int height = info.height;
if (info.format != ANDROID_BITMAP_FORMAT_RGBA_8888)
return
NULL;
std::vector <Object> objects;
if (yolo) {
// 这里调用了yolo.cpp的新函数，将在下面步骤中定义
yolo->
detectPicture(env, bitmap, width, height, objects, modelid
);
} else {
__android_log_print(ANDROID_LOG_DEBUG,
"yolov8ncnn", "g_yolo is NULL!");
}
// 在detectPicture方法中将结果保存在了 objects 中，还需继续对他进行转换
jobjectArray jObjArray = env->NewObjectArray(objects.size(), objCls, NULL);

__android_log_print(ANDROID_LOG_DEBUG,
"yolov8ncnn", "%d  个检测到的物体!", objects.

size()

);

for (
size_t i = 0;
i<objects.

size();

i++) {
jobject jObj = env->NewObject(objCls, constructortorId);

env->
SetFloatField(jObj, xId, objects[i]
.rect.x);
env->
SetFloatField(jObj, yId, objects[i]
.rect.y);
env->
SetFloatField(jObj, wId, objects[i]
.rect.width);
env->
SetFloatField(jObj, hId, objects[i]
.rect.height);
env->
SetIntField(jObj, labelId, objects[i]
.label);
env->
SetFloatField(jObj, probId, objects[i]
.prob);

env->
SetObjectArrayElement(jObjArray, i, jObj
);
}

double elasped = ncnn::get_current_time() - start_time;
__android_log_print(ANDROID_LOG_DEBUG,
"yolov8ncnn", "整个检测过程用时 %.2fms", elasped);

return
jObjArray;
}


static int draw_unsupported(cv::Mat &rgb) {
    const char text[] = "unsupported";

    int baseLine = 0;
    cv::Size label_size = cv::getTextSize(text, cv::FONT_HERSHEY_SIMPLEX, 1.0, 1, &baseLine);

    int y = (rgb.rows - label_size.height) / 2;
    int x = (rgb.cols - label_size.width) / 2;

    cv::rectangle(rgb, cv::Rect(cv::Point(x, y), cv::Size(label_size.width, label_size.height + baseLine)),
                  cv::Scalar(255, 255, 255), -1);

    cv::putText(rgb, text, cv::Point(x, y + label_size.height),
                cv::FONT_HERSHEY_SIMPLEX, 1.0, cv::Scalar(0, 0, 0));

    return 0;
}

static int draw_fps(cv::Mat &rgb) {
    // resolve moving average
    float avg_fps = 0.f;
    {
        static double t0 = 0.f;
        static float fps_history[10] = {0.f};

        double t1 = ncnn::get_current_time();
        if (t0 == 0.f) {
            t0 = t1;
            return 0;
        }

        float fps = 1000.f / (t1 - t0);
        t0 = t1;

        for (int i = 9; i >= 1; i--) {
            fps_history[i] = fps_history[i - 1];
        }
        fps_history[0] = fps;

        if (fps_history[9] == 0.f) {
            return 0;
        }

        for (int i = 0; i < 10; i++) {
            avg_fps += fps_history[i];
        }
        avg_fps /= 10.f;
    }

    char text[32];
    sprintf(text, "FPS=%.2f", avg_fps);

    int baseLine = 0;
    cv::Size label_size = cv::getTextSize(text, cv::FONT_HERSHEY_SIMPLEX, 0.5, 1, &baseLine);

    int y = 0;
    int x = rgb.cols - label_size.width;

    cv::rectangle(rgb, cv::Rect(cv::Point(x, y), cv::Size(label_size.width, label_size.height + baseLine)),
                  cv::Scalar(255, 255, 255), -1);

    cv::putText(rgb, text, cv::Point(x, y + label_size.height),
                cv::FONT_HERSHEY_SIMPLEX, 0.5, cv::Scalar(0, 0, 0));

    return 0;
}

//static Yolo* g_yolo = 0;


class MyNdkCamera : public NdkCameraWindow {
public:
    virtual void on_image_render(cv::Mat &rgb) const;
};

void MyNdkCamera::on_image_render(cv::Mat &rgb) const {
    // nanodet
    {
        ncnn::MutexLockGuard g(lock);

        if (yolo) {
            std::vector <Object> objects;
            yolo->detect(rgb, objects);

            yolo->draw(rgb, objects);
        } else {
            draw_unsupported(rgb);
        }
    }

    draw_fps(rgb);
}

static MyNdkCamera* g_camera = 0;

extern "C" {

JNIEXPORT jint
JNI_OnLoad(JavaVM * vm , void* reserved )
{
__android_log_print(ANDROID_LOG_DEBUG, "ncnn" , "JNI_OnLoad" ) ;

g_camera = new MyNdkCamera;

return JNI_VERSION_1_4;
}

JNIEXPORT void JNI_OnUnload(JavaVM * vm, void * reserved) {
    __android_log_print(ANDROID_LOG_DEBUG, "ncnn", "JNI_OnUnload");

    {
        ncnn::MutexLockGuard g(lock);

        delete yolo;
        yolo = 0;
    }

    delete g_camera;
    g_camera = 0;
}

// public native boolean loadModel(AssetManager mgr, int modelid, int cpugpu);
JNIEXPORT jboolean
Java_yolov8ncnn_Yolov8Ncnn_loadModel(JNIEnv
* env,
jobject thiz, jobject
assetManager,
jint modelid, jint
cpugpu)
{
if (modelid<0 || modelid> 6 || cpugpu<0 || cpugpu> 1)
{
return
JNI_FALSE;
}

AAssetManager* mgr = AAssetManager_fromJava(env, assetManager);

__android_log_print(ANDROID_LOG_DEBUG,
"ncnn", "loadModel %p", mgr);

const char* modeltypes[] =
        {
                "n",
                "s",
                "cx",
        };

const int target_sizes[] =
        {
                320,
                320,
                640,
        };

const float mean_vals[][3] =
        {
                {103.53f, 116.28f, 123.675f},
                {103.53f, 116.28f, 123.675f},
                {103.53f, 116.28f, 123.675f},
        };

const float norm_vals[][3] =
        {
                {1 / 255.f, 1 / 255.f, 1 / 255.f},
                {1 / 255.f, 1 / 255.f, 1 / 255.f},
                {1 / 255.f, 1 / 255.f, 1 / 255.f},
        };

const char* modeltype = modeltypes[(int) modelid];
int target_size = target_sizes[(int) modelid];
bool use_gpu = (int) cpugpu == 1;

// reload
{
ncnn::MutexLockGuard g(lock);

if (

use_gpu &&ncnn::get_gpu_count()

== 0)
{
// no gpu
delete
yolo;
yolo = 0;
}
else
{
if (!yolo)
yolo = new Yolo;
yolo->
load(mgr, modeltype, target_size, mean_vals[(int) modelid], norm_vals[(int) modelid], use_gpu
);
}
}

return
JNI_TRUE;
}

// public native boolean openCamera(int facing);
JNIEXPORT jboolean
Java_yolov8ncnn_Yolov8Ncnn_openCamera(JNIEnv
* env,
jobject thiz, jint
facing)
{
if (facing<0 || facing> 1)
return
JNI_FALSE;

__android_log_print(ANDROID_LOG_DEBUG,
"ncnn", "openCamera %d", facing);

g_camera->open((int)facing);

return
JNI_TRUE;
}

// public native boolean closeCamera();
JNIEXPORT jboolean
Java_yolov8ncnn_Yolov8Ncnn_closeCamera(JNIEnv
* env,
jobject thiz
)
{
__android_log_print(ANDROID_LOG_DEBUG,
"ncnn", "closeCamera");

g_camera->

close();

return
JNI_TRUE;
}

// public native boolean setOutputWindow(Surface surface);
JNIEXPORT jboolean
Java_yolov8ncnn_Yolov8Ncnn_setOutputWindow(JNIEnv
* env,
jobject thiz, jobject
surface)
{
// 从 Surface 对象获取 ANativeWindow 指针
ANativeWindow* win = ANativeWindow_fromSurface(env, surface);

// 打印调试信息
__android_log_print(ANDROID_LOG_DEBUG,
"ncnn", "setOutputWindow %p", win);

// 设置相机的输出窗口
g_camera->
set_window(win);

// 返回 JNI_TRUE 表示成功
return
JNI_TRUE;
}


}
