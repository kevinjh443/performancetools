LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)


LOCAL_MODULE_TAGS := optional

LOCAL_SRC_FILES := $(call all-java-files-under, src)

LOCAL_PACKAGE_NAME := LeakCanaryTest

#LOCAL_MODULE := LeakCanaryTest

LOCAL_STATIC_JAVA_LIBRARIES := LeakCanary

LOCAL_SDK_VERSION := current


LOCAL_MODULE_PATH := $(TARGET_OUT)/bin
LOCAL_RESOURCE_DIR := \
	packages/apps/LeakCanary/res \
	$(LOCAL_PATH)/res
	

LOCAL_AAPT_FLAGS := --auto-add-overlay --extra-packages com.squareup.leakcanary


#include packages/apps/LeakCanary/Android.mk

include $(BUILD_PACKAGE)
##################################################
#include $(CLEAR_VARS)

############################################################### 
#include $(CLEAR_VARS) 
#LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := LeakCanary
#include $(BUILD_MULTI_PREBUILT) 
################################################################ 


#include $(BUILD_MULTI_PREBUILT)

# Use the following include to make our test apk.
#include $(call all-makefiles-under,$(LOCAL_PATH))
