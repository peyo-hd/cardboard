LOCAL_PATH:= $(call my-dir)

include $(CLEAR_VARS)

LOCAL_SRC_FILES := RiftManager.cpp

LOCAL_SHARED_LIBRARIES := libopenhmd

LOCAL_C_INCLUDES  := \
    frameworks/base/core/jni \
    external/openhmd/include

LOCAL_MODULE    := libriftmanager_jni
LOCAL_MODULE_TAGS := optional

include $(BUILD_SHARED_LIBRARY)
