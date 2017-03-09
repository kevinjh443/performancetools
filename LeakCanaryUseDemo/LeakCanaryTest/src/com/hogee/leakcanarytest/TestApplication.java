package com.hogee.leakcanarytest;

import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.watcher.RefWatcher;

import android.app.Application;
import android.content.Context;

public class TestApplication extends Application {

	public static RefWatcher getRefWatcher(Context context) {
		TestApplication application = (TestApplication) context.getApplicationContext();
		return application.refWatcher;
	}

	private RefWatcher refWatcher;

	@Override
	public void onCreate() {
		super.onCreate();
		refWatcher = LeakCanary.install(this);
	}
}
