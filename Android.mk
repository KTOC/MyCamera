LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := eng

LOCAL_SRC_FILES := $(call all-java-files-under, src)

LOCAL_JAVA_LIBRARIES := services

LOCAL_PACKAGE_NAME := testRecorder
LOCAL_STATIC_JAVA_LIBRARIES := lydetect libaw360 mv4
LOCAL_CERTIFICATE := platform
LOCAL_PROGUARD_ENABLED := disabled
include $(BUILD_PACKAGE)

include $(CLEAR_VARS)

LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := lydetect:libs/lydetect.jar \
				libaw360:libs/libaw360.jar \
				mv4:libs/mv4.jar

include $(BUILD_MULTI_PREBUILT)