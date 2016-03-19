package com.hogee.grabpackageinfo.data;

import android.graphics.drawable.Drawable;

/**
 * 2016-3-19
 * @author Kevin
 *
 */
public class AppInfo {
    private String mPackageName;
    private String mLauncherClassName;
    private String mAppLableName;
    private Drawable mIconRes;
    private int mAppType;
    
    public int getmAppType() {
		return mAppType;
	}
	public void setmAppType(int mAppType) {
		this.mAppType = mAppType;
	}
	public Drawable getmIconRes() {
        return mIconRes;
    }
    public void setmIconRes(Drawable mIconRes) {
        this.mIconRes = mIconRes;
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
