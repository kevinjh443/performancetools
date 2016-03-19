package com.hogee.grabpackageinfo;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import com.hogee.grabpackageinfo.adapter.ChoiceAppListAdapter;
import com.hogee.grabpackageinfo.data.AppInfo;

import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ChoiceAppActivity extends Activity {
    
    private File mCurrentFile;
    private String TAG = "kevinjh";
    private GridView mAppGridView;
    
    private ArrayList<AppInfo> mAppInfoList = new ArrayList<AppInfo>();
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAppGridView = (GridView) findViewById(R.id.choice_app_gview);
        
        initData();
        
        ChoiceAppListAdapter adapter = new ChoiceAppListAdapter(this);
        adapter.setAppInfoListItems(mAppInfoList);
        
        mAppGridView.setAdapter(adapter);
        //mAppGridView.setOnItemLongClickListener(mGridItemLongClickListener);
    }
    
    OnItemLongClickListener mGridItemLongClickListener = new OnItemLongClickListener() {

		@Override
		public boolean onItemLongClick(AdapterView<?> parent, View view,
				int position, long id) {
			Log.d(TAG, "position long clicked = "+position);
			return false;
		}
	};
    
    private void initData() {
    	mAppInfoList.clear();
    	
    	Intent localIntent = new Intent("android.intent.action.MAIN", null);
    	localIntent.addCategory("android.intent.category.LAUNCHER");
        ArrayList<ResolveInfo> localList = (ArrayList<ResolveInfo>) 
        		ChoiceAppActivity.this.getApplicationContext().getPackageManager()
        		.queryIntentActivities(localIntent, 0);
        
        for (ResolveInfo info : localList) {
        	AppInfo appInfo = new AppInfo();
        	appInfo.setmPackageName(info.activityInfo.packageName);
        	appInfo.setmLauncherClassName(info.activityInfo.name);
        	appInfo.setmIconResId(info.getIconResource());
        	mAppInfoList.add(appInfo);
        }
	}
    
    View.OnClickListener mGrabListener = new View.OnClickListener() {
        
        @Override
        public void onClick(View v) {
            grabPackagesInfo();
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        
        return true;
    }
    
    private void grabPackagesInfo() {
        iniFile();
        //mCurrentFile = new File("/sdcard/Apps.txt");
        try {
            if(!mCurrentFile.exists()){
                try {
                    mCurrentFile.createNewFile();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
               }
            FileWriter fileWritter = new FileWriter(mCurrentFile);
            BufferedWriter bufferWritter = new BufferedWriter(fileWritter);
            
            Log.d(TAG, "grabPackagesInfo START!");
            Intent localIntent = new Intent("android.intent.action.MAIN", null);
            localIntent.addCategory("android.intent.category.LAUNCHER");
            ArrayList<ResolveInfo> localList = (ArrayList<ResolveInfo>) ChoiceAppActivity.this.getApplicationContext().getPackageManager().queryIntentActivities(localIntent, 0);
            
            for (ResolveInfo info : localList) {
                bufferWritter.write("jianhua.he@tcl.com : "+info.activityInfo.packageName+"/"+info.activityInfo.name+" : 2\n");
                Log.d("John", info.activityInfo.packageName+"/"+info.activityInfo.name);
            }
            bufferWritter.flush();
            bufferWritter.close();
            Log.d(TAG, "grabPackagesInfo DONE!");
        } catch (Exception e) {
            Log.e(TAG, "have an issue! ", e);
            // TODO: handle exception
        }
    }
    
    /**
     * init dumpsysalarm.txt file, if exist, delete first, can be optimize
     */
    private void iniFile() {
        if (Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)) {
            mCurrentFile = Environment.getExternalStorageDirectory();
            mCurrentFile = new File(mCurrentFile.getAbsoluteFile()+"/Apps.txt");
            if (mCurrentFile.exists() && mCurrentFile.isFile()) {
                mCurrentFile.delete();
            }
            try {
                mCurrentFile.createNewFile();
            } catch (IOException e) {
                Log.e(TAG, "create file have an issue", e);
                e.printStackTrace();
            }
        }
    }

}
