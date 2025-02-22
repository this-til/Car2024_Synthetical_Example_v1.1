package com.til.car_service.data;

public class LibraryLoader {
    private static volatile boolean isLibraryLoaded = false;
    private static final Object lock = new Object();

    public static void initLibraries() {
        if (!isLibraryLoaded) {
            synchronized (lock) {
                if (!isLibraryLoaded) {
                    try {
                        System.loadLibrary("yolov8ncnn");
                        isLibraryLoaded = true;
                    } catch (UnsatisfiedLinkError e) {
                        e.printStackTrace();
                        throw new RuntimeException("无法加载 yolov8ncnn 库: " + e.getMessage());
                    }
                }
            }
        }
    }
}