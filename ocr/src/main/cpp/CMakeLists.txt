#project(OcrLite)
#
#cmake_minimum_required(VERSION 3.22.1)
#
#option(OCR_LITE_VULKAN "OcrLite Enable Vulkan" ON)
##set(OCR_LITE_VULKAN ON)
#if (OCR_LITE_VULKAN)
#    set(ncnn_DIR "${CMAKE_SOURCE_DIR}/../ncnn-vulkan/${ANDROID_ABI}/lib/cmake/ncnn")
#else ()
#    set(ncnn_DIR "${CMAKE_SOURCE_DIR}/../ncnn/${ANDROID_ABI}/lib/cmake/ncnn")
#endif ()
#find_package(ncnn REQUIRED)
#
### opencv 库
#set(OpenCV_DIR "${CMAKE_SOURCE_DIR}/../../sdk/native/jni")
#find_package(OpenCV REQUIRED)
#if (OpenCV_FOUND)
#    message(STATUS "OpenCV_LIBS: ${OpenCV_LIBS}")
#    message(STATUS "OpenCV_INCLUDE_DIRS: ${OpenCV_INCLUDE_DIRS}")
#else ()
#    message(FATAL_ERROR "opencv Not Found!")
#endif (OpenCV_FOUND)
#
#include_directories(include)
#file(GLOB OCR_SRC src/*.cpp)
#set(OCR_COMPILE_CODE ${OCR_SRC})
#
#add_library(OcrLite SHARED ${OCR_COMPILE_CODE})
#
#target_link_libraries(
#        OcrLite
#        ncnn
#        ${OpenCV_LIBS}
#        jnigraphics)
#
#if (OCR_LITE_VULKAN)
#    target_compile_definitions(OcrLite PRIVATE __VULKAN__)
#endif ()


project(OcrLite)
cmake_minimum_required(VERSION 3.22.1)

# [1. 强制查找静态OpenMP库]
set(CMAKE_FIND_LIBRARY_SUFFIXES .a ${CMAKE_FIND_LIBRARY_SUFFIXES})  # 优先查找.a静态库
find_package(OpenMP REQUIRED)

# [2. 配置ncnn]
option(OCR_LITE_VULKAN "OcrLite Enable Vulkan" ON)
if (OCR_LITE_VULKAN)
    set(ncnn_DIR "${CMAKE_SOURCE_DIR}/../ncnn-vulkan/${ANDROID_ABI}/lib/cmake/ncnn")
else ()
    set(ncnn_DIR "${CMAKE_SOURCE_DIR}/../ncnn/${ANDROID_ABI}/lib/cmake/ncnn")
endif ()
find_package(ncnn REQUIRED)

# [3. 配置OpenCV]
set(OpenCV_DIR "${CMAKE_SOURCE_DIR}/../../sdk/native/jni")
find_package(OpenCV REQUIRED)
if (NOT OpenCV_FOUND)
    message(FATAL_ERROR "OpenCV Not Found!")
endif ()

# [4. 添加OpenMP编译选项]
add_compile_options(-fopenmp)

# [5. 构建目标]
include_directories(include ${OpenCV_INCLUDE_DIRS})
file(GLOB OCR_SRC src/*.cpp)
add_library(OcrLite SHARED ${OCR_SRC})

# [6. 链接静态OpenMP]
target_link_libraries(OcrLite 
  PRIVATE 
  ncnn
  ${OpenCV_LIBS}
  jnigraphics
  OpenMP::OpenMP_CXX  # 静态链接OpenMP
)

# [7. 可选：手动指定静态库路径（如果自动查找失败）]
# target_link_libraries(OcrLite PRIVATE
#   ${ANDROID_NDK}/toolchains/llvm/prebuilt/linux-x86_64/lib64/clang/14.0.6/lib/linux/aarch64/libomp.a
# )

# [8. Vulkan配置]
if (OCR_LITE_VULKAN)
  target_compile_definitions(OcrLite PRIVATE __VULKAN__)
endif ()
