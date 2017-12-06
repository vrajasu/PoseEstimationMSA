LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

OPENCV_ROOT:=/Users/vrajdelhivala/Downloads/OpenCV-android-sdk-3.2.0
OPENCV_CAMERA_MODULES:=on
OPENCV_INSTALL_MODULES:=on
OPENCV_LIB_TYPE:=SHARED
include ${OPENCV_ROOT}/sdk/native/jni/OpenCV.mk

LOCAL_MODULE    := asuforia

LOCAL_SRC_FILES := group1_asuforia_NativeCallMethods.cpp
LOCAL_LDLIBS += -llog -landroid -lEGL -lGLESv2
LOCAL_LDLIBS    += -lOpenMAXAL -lmediandk
LOCAL_LDLIBS    += -landroid
LOCAL_CFLAGS    += -UNDEBUG


include $(BUILD_SHARED_LIBRARY)