LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

OPENCV_ROOT:=/home/gunman/Downloads/software/OpenCV-android-sdk/
OPENCV_CAMERA_MODULES:=on
OPENCV_INSTALL_MODULES:=on
OPENCV_LIB_TYPE:=SHARED
include ${OPENCV_ROOT}/sdk/native/jni/OpenCV.mk

LOCAL_MODULE    := opendraw

LOCAL_SRC_FILES := group1_poseestimationmsa_NativeDraw.cpp
LOCAL_LDLIBS += -llog -landroid -lEGL -lGLESv2
LOCAL_LDLIBS    += -lOpenMAXAL -lmediandk
LOCAL_LDLIBS    += -landroid
LOCAL_CFLAGS    += -UNDEBUG


include $(BUILD_SHARED_LIBRARY)