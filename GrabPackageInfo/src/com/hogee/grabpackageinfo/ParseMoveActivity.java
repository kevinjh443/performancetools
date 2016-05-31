package com.hogee.grabpackageinfo;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class ParseMoveActivity extends Activity {
    
    private File mCurrentFile;
    private String TAG = "kevinjh";
    private Button mGraButton;
    private TextView mTextResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mGraButton = (Button) findViewById(R.id.grab_package_info);
        mGraButton.setOnClickListener(mGrabListener);
        mTextResult = (TextView) findViewById(R.id.greb_package_info_result);
    }
    
    View.OnClickListener mGrabListener = new View.OnClickListener() {
        
        @Override
        public void onClick(View v) {
            
        }
    };

    @Override
    public boolean onTouchEvent(android.view.MotionEvent event) {
        Log.d(TAG, "x = "+event.getRawX() + "  y = "+event.getRawY());
        return true;
    };
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        
        return true;
    }

}
