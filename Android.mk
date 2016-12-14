LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := eng

LOCAL_SRC_FILES := $(call all-java-files-under, src)

LOCAL_JAVA_LIBRARIES := services

LOCAL_PACKAGE_NAME := testRecorder
LOCAL_STATIC_JAVA_LIBRARIES := lydetect
LOCAL_CERTIFICATE := platform

include $(BUILD_PACKAGE)

include $(CLEAR_VARS)

LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := lydetect:libs/lydetect.jar

include $(BUILD_MULTI_PREBUILT)