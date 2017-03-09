package com.hogee.leakcanarytest;

import com.squareup.leakcanary.watcher.RefWatcher;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

public class MainActivity extends Activity {
	
	private static String TAG = "LeakCanaryTestItem";
	private static LeakDemo mLeakDemo = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        RefWatcher refWatcher = TestApplication.getRefWatcher(this);
	    refWatcher.watch(this);
        
        if (mLeakDemo == null) {
			mLeakDemo = new LeakDemo();
			Log.d(TAG, "leakdemo class create");
		}
        mLeakDemo.doSomething();
    }
    
    class LeakDemo{
    	private void doSomething() {
			Log.d(TAG, "leakdemo class doSomething method run!");
		}
    }
}
