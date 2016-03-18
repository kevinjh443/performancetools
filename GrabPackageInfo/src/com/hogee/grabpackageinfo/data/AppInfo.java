package com.hogee.grabpackageinfo.data;

import android.R.integer;

public class AppInfo {
    private String mPackageName;
    private String mLauncherClassName;
    private String mAppLableName;
    private int mIconResId;
    
    public int getmIconResId() {
        return mIconResId;
    }
    public void setmIconResId(int mIconResId) {
        this.mIconResId = mIconResId;
    }
    public String getmPackageName() {
        return mPackageName;
    }
    public void setmPackageName(String mPackageName) {
        this.mPackageName = mPackageName;
    }
    public String getmLauncherClassName() {
        return mLauncherClassName;
    }
    public void setmLauncherClassName(String mLauncherClassName) {
        this.mLauncherClassName = mLauncherClassName;
    }
    public String getmAppLableName() {
        return mAppLableName;
    }
    public void setmAppLableName(String mAppLableName) {
        this.mAppLableName = mAppLableName;
    }

}
