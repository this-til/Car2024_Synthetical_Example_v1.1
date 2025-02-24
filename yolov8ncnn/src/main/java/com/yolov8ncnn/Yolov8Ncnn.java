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

package com.yolov8ncnn;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.view.Surface;

public class Yolov8Ncnn {

    static {
        System.loadLibrary("yolov8ncnn");
    }

    public static native void init(AssetManager mgr);

    public static native Obj[] detect(Bitmap bitmap, String modelName);

    /***
     * 加载模型
     * 如果已经加载将不会重复加载
     */
    public static native void loadModel(AssetManager mgr, String modelName, int itemSize, String extractBlobName, boolean useGpu);

    public native void openCamera(int facing, String modelName);

    public native void closeCamera();

    public native void setOutputWindow(Surface surface);

    public static class Obj {
        public float x;
        public float y;
        public float w;
        public float h;
        public int label;
        public float prob;

        public Obj() {
            // 初始化代码
        }
    }

}

/*
public class Yolov8Ncnn {

    // 模型0【n】（弃用）
    // 模型5【形状颜色模型】
    // 模型1【s】（弃用）
    // 模型2【车型】（弃用）
    // 模型3【车型大模型】
    // 模型4【交通标志物】
    // 模型6【交通灯模型】
    // 模型7【人脸】
    // 模型8
    // 模型9
    // 模型0
    */
/**
 * 摄像头角度控制，可发送自定协议
 *
 * @param modelid
 * 模型3【车型大模型】
 * 模型4【交通标志物】
 * 模型5【形状颜色模型】
 * 模型6【交通灯模型】
 * 模型7【人脸】
 * 模型8【车牌】
 * @param cpugpu       CPU/GPU
 *//*

    public static native boolean Init(AssetManager mgr, int modelid, int cpugpu);

    public static String[] labels = {
            "黄色卡车", "黄色摩托车", "自行车", "蓝车", "红色汽车", "红色摩托车", "白色汽车", "白色卡车", "黄车",
    };
    public static String[] bz = {
            "右转",
            "左转",
            "掉头",
            "直行",
            "禁止左转",
            "禁止右转",
            "禁止掉头",
            "禁止直行",
            "禁止通行",
            "限速标志",
    };

    public enum CpaType {
           绿牌,
           浅蓝牌,
           蓝牌,
    }


    public static String[] jtd = {
            "红灯",
            "绿灯",
            "黄灯",
    };
    public static String[] kz = {
            "无口罩",
            "带口罩",
    };
    public static String[] xz = {
            "白色三角形", "白色五角形", "白色圆形", "白色梯形", "白色矩形", "白色菱形",
            "紫色三角形", "紫色五角形", "紫色圆形", "紫色梯形", "紫色矩形", "紫色菱形",
            "红色三角形", "红色五角形", "红色圆形", "红色梯形", "红色矩形", "红色菱形",
            "绿色三角形", "绿色五角形", "绿色圆形", "绿色梯形", "绿色矩形", "绿色菱形",
            "蓝色三角形", "蓝色五角形", "蓝色圆形", "蓝色梯形", "蓝色矩形", "蓝色菱形",
            "青色三角形", "青色五角形", "青色圆形", "青色梯形", "青色矩形", "青色菱形",
            "黄色三角形", "黄色五角形", "黄色圆形", "黄色梯形", "黄色矩形", "黄色菱形",
            "黑色三角形", "黑色五角形", "黑色圆形", "黑色梯形", "黑色矩形", "黑色菱形",
    };

    public static class Obj {
        public float x;
        public float y;
        public float w;
        public float h;
        public int label;
        public float prob;

        public Obj() {
            // 初始化代码
        }
    }
    // 模型0【n】（弃用）
    // 模型1【s】（弃用）
    // 模型2【车型】（弃用）
    // 模型3【车型大模型】
    // 模型4【交通标志物】
    // 模型5【形状颜色模型】
    // 模型6【交通灯模型】
    // 模型7
    // 模型8
    // 模型9
    // 模型0
    public static native Obj[] Detect(Bitmap bitmap, boolean use_gpu,int model);

    public static native boolean loadModel(AssetManager mgr, int modelid, int cpugpu);
    public native boolean openCamera(int facing);
    public native boolean closeCamera();
    public native boolean setOutputWindow(Surface surface);

    static {
        System.loadLibrary("yolov8ncnn");
    }
}
*/

