package com.hogee.grabpackageinfo;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import com.hogee.grabpackageinfo.adapter.ChoiceAppListAdapter;
import com.hogee.grabpackageinfo.data.AppInfo;

import android.Manifest;
import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 2016-3-19
 * @author Kevin
 *
 */
public class ChoiceAppActivity extends Activity {
    
	private Context mContext;
    private File mCurrentFile;
    private String TAG = "kevinjh";
    private GridView mAppGridView;
    private TextView mInfoShow;
    private Button mParseButton;
    private PackageManager mPm;
    
    private ChoiceAppListAdapter mAdapter;
    private AlertDialog.Builder mChoiceAlertDialog = null;
    private int mChoicedId;
    private int mHighCount = 0, mMidCount = 0, mLowCount = 0, mNouseCount = 0;
    
    private ArrayList<AppInfo> mAppInfoList = new ArrayList<AppInfo>();
    private ArrayList<AppInfo> mAppInfoChoiced = new ArrayList<AppInfo>();
    
    // Storage Permissions //sdk23+ need this
    /*private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choice_app);
        
        mParseButton = (Button) findViewById(R.id.parse_btn);
        
        mInfoShow = (TextView) findViewById(R.id.info_show);
        mAppGridView = (GridView) findViewById(R.id.choice_app_gview);
        mInfoShow.setMovementMethod(ScrollingMovementMethod.getInstance());
        mInfoShow.setVisibility(View.INVISIBLE);
        mAppGridView.setVisibility(View.INVISIBLE);
        
        mParseButton.setOnClickListener(mParseStartListener);
        
        mPm = this.getPackageManager();
        mContext = this;
    }
    
    View.OnClickListener mParseStartListener = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			mParseButton.setVisibility(View.GONE);
			mInfoShow.setVisibility(View.VISIBLE);
			mAppGridView.setVisibility(View.VISIBLE);
			
			initData();
	        mAdapter = new ChoiceAppListAdapter(mContext);
	        mAdapter.setAppInfoListItems(mAppInfoList);
	        mAppGridView.setAdapter(mAdapter);
	        mAppGridView.setOnItemLongClickListener(mGridItemLongClickListener);
	        
	        mInfoShow.setText("your device total App : "+mAppInfoList.size()+"\n" +
	        		"please long press to choice diff app!");
		}
	};
    
    OnItemLongClickListener mGridItemLongClickListener = new OnItemLongClickListener() {

		@Override
		public boolean onItemLongClick(AdapterView<?> parent, View view,
				int position, long id) {
			Log.d(TAG, "position long clicked = "+position);
			mChoicedId = position;
			choiceAppInDiffType();
			return true;
		}
	};
	
	private void choiceAppInDiffType() {
		if (mChoiceAlertDialog == null) {
			mChoiceAlertDialog = new AlertDialog.Builder(mContext);
			mChoiceAlertDialog.setTitle("choice type:");
			mChoiceAlertDialog.setItems(R.array.choice_diff_type_item, mChoiceDiffTypeListener);
			mChoiceAlertDialog.create();
		}
		mChoiceAlertDialog.show();
	}
	
	DialogInterface.OnClickListener mChoiceDiffTypeListener = 
			new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					Log.d(TAG, "you choice : "+which);
					switch (which) {
					case 0:
						mHighCount++;
						setAppInfoType(3);
						break;
					case 1:
						mMidCount++;
						setAppInfoType(2);
						break;
					case 2:
						mLowCount++;
						setAppInfoType(1);
						break;
					case 3:
						mNouseCount++;
						setAppInfoType(0);
						break;

					default:
						printToSdcard();
						break;
					}
				}
			};
    
	private void setAppInfoType(int which) {
		AppInfo appInfo = mAppInfoList.get(mChoicedId);
		mAppInfoList.remove(mChoicedId);
		appInfo.setmAppType(which);
		mAppInfoChoiced.add(appInfo);
		
		mAdapter.notifyDataSetChanged();
		mInfoShow.setText("[High use]:"+mHighCount+"  [Mid use]:"+mMidCount+
				"  [Low use]:"+mLowCount+"  [No use]:"+mNouseCount+
				"\n now have "+mAppInfoList.size()+" to choice!");
	}
	
	private void printToSdcard() {
		if (mAppInfoChoiced.size() == 0) {
			Toast.makeText(getApplicationContext(), "please long press to choice app!",
				     Toast.LENGTH_SHORT).show();
		} else {
			wirteDataToSdcard();
			mInfoShow.append("\n DONE!!!\n Result in /sdcard/Apps.txt \n" +
	        		"adb shell pull /sdcard/Apps.txt ./ \n" +
	        		"or copy it frome windows MTP. \n" +
	        		"or. adb logcat | grep/findstr John  when PRINT. \n" +
	        		"anything can contacts author: jianhua.he@tcl.com - Ext.66051. Thanks");
		}
	}
    private void initData() {
    	mAppInfoList.clear();
    	mAppInfoChoiced.clear();
    	
    	Intent localIntent = new Intent("android.intent.action.MAIN", null);
    	localIntent.addCategory("android.intent.category.LAUNCHER");
        ArrayList<ResolveInfo> localList = (ArrayList<ResolveInfo>) 
        		ChoiceAppActivity.this.getApplicationContext().getPackageManager()
        		.queryIntentActivities(localIntent, 0);
        
        for (ResolveInfo info : localList) {
        	AppInfo appInfo = new AppInfo();
        	appInfo.setmPackageName(info.activityInfo.packageName);
        	appInfo.setmLauncherClassName(info.activityInfo.name);
        	appInfo.setmIconRes(info.loadIcon(mPm));
        	appInfo.setmAppLableName(info.activityInfo.loadLabel(mPm).toString());
        	mAppInfoList.add(appInfo);
        }
	}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        
        return true;
    }
    
    /**
     * sdk23+ need this
     * Checks if the app has permission to write to device storage
     * If the app does not has permission then the user will be prompted to grant permissions
     * @param activity
     */
    /*public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }*/

    private void wirteDataToSdcard() {
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
            
            Log.d(TAG, "write data START!");
            for (int i = 0; i < mAppInfoChoiced.size(); i++) {
            	AppInfo appInfo = mAppInfoChoiced.get(i);
				if (appInfo.getmAppType() != 0) {
					bufferWritter.write("jianhua.he@tcl.com : "+appInfo.getmPackageName()+"/"+appInfo.getmLauncherClassName()
							+" : "+appInfo.getmAppType()+"\n");
					Log.d("John", appInfo.getmPackageName()+"/"+appInfo.getmLauncherClassName());
				}
			}
            bufferWritter.flush();
            bufferWritter.close();
            Log.d(TAG, "write data DONE!");
        } catch (Exception e) {
            Log.e(TAG, "have an issue! ", e);
        }
    }
    
    /**
     * init Apps.txt file, if exist, delete first, can be optimize
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
