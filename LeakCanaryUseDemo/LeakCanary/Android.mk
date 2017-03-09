LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)


LOCAL_MODULE_TAGS := optional


LOCAL_SRC_FILES := $(call all-java-files-under, src)

LOCAL_MODULE := LeakCanary

LOCAL_SDK_VERSION := current

#LOCAL_MODULE_PATH := $(TARGET_OUT)/bin
LOCAL_RESOURCE_DIR := \
$(LOCAL_PATH)/res

#LOCAL_SHARED_LIBRARIES := LeakCanary


#include $(BUILD_PACKAGE)
LOCAL_MODULE_CLASS := JAVA_LIBRARIES

include $(BUILD_STATIC_JAVA_LIBRARY)

# Use the following include to make our test apk.
#include $(call all-makefiles-under,$(LOCAL_PATH))
