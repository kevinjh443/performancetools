package com.hogee.storageexam;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.hogee.storageexam.R;

import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.StatFs;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.SeekBar.OnSeekBarChangeListener;

/**
 * crazy create 1KB - 2MB files to /sdcard, aim to create many storage fragmentation. simulate the user use phone.
 * @author jianhua.he
 *
 */
public class MainActivity extends Activity {
    
    private static final String TAG = "fragmentation_test";
    private static boolean DEBUG = Log.isLoggable(TAG, Log.DEBUG);
    
    private Button mCreateFragmBtn;
    private Button mDeleteFragmBtn;
    private ScrollView mDetailScrollView;
    private TextView mDetailTextView;
    private TextView mResultTextView;
    private TextView mFillScaleTextView;
    /** the circle progress about how much filled of delete */
    private CircleProgress mProgressView;
    /** adjust the proportion of need fill data */
    private SeekBar mSeekBar;
    
    private File mSDcardDir;
    private StatFs mSf;
    /** the counter of create files & delete files */
    private long mFileCounter = 0;
    /** the total files need delete */
    private long mTotalFileCount = 0;
    /** the available storage of sdcard: Byte */
    private long  mAvailCount = 0;
    private volatile long  mAvailCountTemp = 0;
    /** the proportion of need fill data (for available storage) */
    private int mFillScale = 0;
    /** recode the start time  */
    private long mStartTime = 0;
    
    private static final int CASE_CHECK_AVAIL = 0;
    private static final int CASE_CONTINUE_WRITE = 1;
    private static final int CASE_BREAK_WRITE = 2;
    private static final int CASE_CLEARN_DATA = 3;
    private static final int CASE_DELETE_FILES = 4;
    private static final int CASE_DELETE_FILES_DONE = 5;
    
    private volatile Object mObjectLockFlag = new Object();
    /** to recode the create files need continue create */
    private volatile boolean isLoopNeeded = true;
    private volatile boolean isCreateFileThreadRunning = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        mDetailScrollView = (ScrollView) findViewById(R.id.result_show_scroll);
        mDetailTextView = (TextView) findViewById(R.id.result_show_contants);
        mResultTextView = (TextView) findViewById(R.id.result_show_contants_2);
        mFillScaleTextView = (TextView) findViewById(R.id.fill_scale_textview);
        
        mDeleteFragmBtn = (Button) findViewById(R.id.delete_fragmentation_test_btn);
        mDeleteFragmBtn.setOnClickListener(mDeleteFragmListener);
        
        mCreateFragmBtn = (Button) findViewById(R.id.create_fragmentation_test_btn);
        mCreateFragmBtn.setOnClickListener(mCreateFragmListener);
        
        String state = Environment.getExternalStorageState(); 
        if(Environment.MEDIA_MOUNTED.equals(state)) {
            mSDcardDir = Environment.getExternalStorageDirectory(); 
        }
        
        mSeekBar = (SeekBar) findViewById(R.id.seek_bar);
        mSeekBar.setOnSeekBarChangeListener(mSeekBarChangeListener);
        
        mProgressView = (CircleProgress) findViewById(R.id.circle_progress);
        
        initIntr();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
    }
    
    /**
     * initialization about this tool title, mean Introduction
     */
    private void initIntr() {
        mDetailTextView.setText(R.string.exam_intr);
        mAvailCountTemp = 0;
        mStartTime = System.currentTimeMillis();
        initSDCardState();
    }
    
    OnSeekBarChangeListener mSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        
        @Override
        public void onStopTrackingTouch(SeekBar arg0) {
            
        }
        
        @Override
        public void onStartTrackingTouch(SeekBar arg0) {
            
        }
        
        @Override
        public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
            if (i <= 95) {
                mFillScale = i;
            } else {
                mFillScale = 95;
            }
            mFillScaleTextView.setText(mFillScale+"%");
        }
    };
    
    OnClickListener mDeleteFragmListener = new View.OnClickListener() {
        
        @Override
        public void onClick(View v) {
            if (!isCreateFileThreadRunning) {
                new DeleteFragmThread().start();
            }
        }
    };
    
    OnClickListener mCreateFragmListener = new View.OnClickListener() {
        
        @Override
        public void onClick(View v) {
            if (!isCreateFileThreadRunning) {
                initIntr();
                new CreateFragmThread().start(); 
            } else {
                
            }
        }
    };
    
    /**
     * delete all the files created
     * @author jianhua.he
     *
     */
    class DeleteFragmThread extends Thread {

        @Override
        public void run() {
            deleteFile();
        }
        
    }
    
    /**
     * create some fake data files to sdcard
     * @author jianhua.he
     *
     */
    class CreateFragmThread extends Thread {

        @Override
        public void run() {
            try {
                isLoopNeeded = true;
                while (isLoopNeeded) {
                    if (!isCreateFileThreadRunning) {
                        isCreateFileThreadRunning = true;
                    }
                    insertFileRandomData(createFileName());
                }
            } catch (Exception e) {
                Log.d(TAG, "insertFileRandomData have an issue");
                e.printStackTrace();
            } finally {
                isCreateFileThreadRunning = false;
            }
        }
        
    }
    
    /**
     * create the different files name about what you want fill 
     * @return fileNameString
     */
    private String createFileName() {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss.SSS");
        String fileNameString = df.format(new Date());
        fileNameString = "fragmentation_test_"+fileNameString+".rom_exam";
        Log.d(TAG, "create file name = "+fileNameString);
        return fileNameString;
    }
    
    /**
     * according the file name, create real file, and count the files size or some thing else
     * @param fileName
     * @throws IOException
     */
    private void insertFileRandomData(String fileName) throws IOException {
        File file = new File(mSDcardDir.getAbsoluteFile() +"/"+ fileName);
        if (!file.exists()) {
            file.createNewFile();
            mFileCounter++;
        }
        
        int fileSize = 1024 * (1 + (int)(Math.random()*2048));//create random filesize 1KB - 2MB random
        Log.d(TAG, "random file size = "+(fileSize/1024) + "KB");
        insertFileRandomData(file, fileSize);
        Log.d(TAG, "random file real size = "+(file.length()/1024) + "KB");
        mAvailCountTemp += fileSize;
        
        if (mFileCounter % 5 == 0) {
            mHandler.sendEmptyMessage(CASE_CHECK_AVAIL);
        }
        
        //TODO: may be have a bug here, when do the UI operation, need lock here.
//        try {
//            mObjectLockFlag.wait();
//        } catch (InterruptedException e) {
//            Log.d(TAG, "wait lock have an issue");
//            isLoopNeeded = false;
//            e.printStackTrace();
//        }
    }
    
    /**
     * real insert, create the fake data to file and insert
     * @param file
     * @param FileSize
     */
    private void insertFileRandomData(File file, int FileSize) {//filesize 1KB 2MB random
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(file);
            byte[] buf = new byte[FileSize];
            out.write(buf);
            out.flush();
        } catch (IOException e) {
            Log.d(TAG, "insert file data have an issue!");
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                Log.d(TAG, "insert file data close have an issue!");
                e.printStackTrace();
            }
        }
    }
    
    /**
     * the file name filter, filter like : xxxx.rom_exam
     * @author jianhua.he
     *
     */
    class ExamFileNameFilter implements FilenameFilter {
        
        private String fileNameType;  
        public ExamFileNameFilter(String type){  
            this.fileNameType = type;  
        }

        @Override
        public boolean accept(File dir, String name) {
            return name.endsWith(fileNameType);
        }
        
    }
    
    /**
     * delete all the xxx.rom_exam files in /sdcard
     */
    private void deleteFile() {
        File[] files = mSDcardDir.listFiles(new ExamFileNameFilter(".rom_exam"));
        mTotalFileCount = files.length - 1;
        for (int i = 0; i < files.length; i++) {
            Log.d(TAG, "delete file = "+files[i]);
            files[i].delete();
            mFileCounter = i;
            if (i % 5 == 0) {
                mHandler.sendEmptyMessage(CASE_DELETE_FILES);
            }
        }
        
        mHandler.sendEmptyMessage(CASE_DELETE_FILES_DONE);
    }
    
    /**
     * get the sdcard size, available size, block size etc. information.
     */
    @SuppressLint("NewApi")
    void initSDCardState() { 
        mSf = new StatFs(mSDcardDir.getPath()); 
        long blockSize = mSf.getBlockSizeLong(); 
        long blockCount = mSf.getBlockCountLong(); 
        long availCount = mSf.getAvailableBlocksLong(); 
        mAvailCount = availCount * blockSize;
        mDetailTextView.append("Sdcard狀態: block大小:"+ blockSize+"； block数目:"+ blockCount+"； 总大小:"+blockSize*blockCount/1048576+"MB \n");
        mDetailTextView.append("可用的block数目:"+ availCount+";  剩余空间:"+ mAvailCount/1048576+"MB");//1024*1024=1048576
        Log.d(TAG, "block大小:"+ blockSize+", block数目:"+ blockCount+", 总大小:"+blockSize*blockCount/1024+"KB"); 
        Log.d(TAG, "可用的block数目：:"+ availCount+", 剩余空间:"+ availCount*blockSize/1024+"KB"); 
    }
    
    /**
     * update UI hander
     */
    Handler mHandler = new Handler() {

        @SuppressLint("NewApi")
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case CASE_CHECK_AVAIL:
                //1.calculate the proportion about fill files
                Log.d(TAG, "mAvailCountTemp = "+mAvailCountTemp +", mAvailCount = "+mAvailCount);
                double proportion = (double)mAvailCountTemp / (double)mAvailCount;
                proportion = proportion * 100;
                Log.d(TAG, "proportion = "+Double.toString(proportion));
                long useTimeTemp = System.currentTimeMillis() - mStartTime;//ms
                useTimeTemp = useTimeTemp / 1000;//sec
                long createSize = mAvailCountTemp / 1048576; //MB
                
                //2.update the UI show information
                mResultTextView.setText("fill:"+String.format("%.2f", proportion)+" % \n");
                mResultTextView.append("產生文件：  "+mFileCounter+"  個 \n");
                mResultTextView.append("產生大小：  "+ createSize +"  MB \n");
                mResultTextView.append("所用时间：  "+ useTimeTemp +"  sec \n");
                if (0 == useTimeTemp) {
                    mResultTextView.append("当前写入速度约：  ---  MB/sec \n");
                } else {
                    mResultTextView.append("平均写入速度约：  "+ (createSize/useTimeTemp) +"  MB/sec \n");
                }
                mProgressView.setProgress((int)proportion);
                //3. operation of fill files
                if (proportion < (double)mFillScale) {
                    //mHandler.sendEmptyMessageDelayed(CASE_CONTINUE_WRITE,200);
                    isLoopNeeded = true;
                } else {
                    isLoopNeeded = false;
                    mResultTextView.append("\n\n DONE!!!!!!");
                    initIntr();
                    //mHandler.sendEmptyMessageDelayed(CASE_BREAK_WRITE, 200);
                }
                break;
            case CASE_CONTINUE_WRITE:
                isLoopNeeded = true;
//                mObjectLockFlag.notify();
                break;
            case CASE_BREAK_WRITE:
                isLoopNeeded = false;
//                mObjectLockFlag.notify();
                break;
                
            case CASE_CLEARN_DATA:
                mDetailTextView.setText("");
                break;
                
            case CASE_DELETE_FILES:
                int prop = (int) (100 * mFileCounter / (mTotalFileCount + 1));
                mProgressView.setProgress(100 - prop);
                mResultTextView.setText("delete file: "+mFileCounter);
                break;
                
            case CASE_DELETE_FILES_DONE:
                initIntr();
                mProgressView.setProgress(0);
                mResultTextView.setText("delete file: "+mFileCounter);
                mResultTextView.append("\n\n DONE!!!!");
                long avail = mSf.getAvailableBlocksLong() * mSf.getBlockSizeLong();
                mResultTextView.append("\n 剩餘： "+ (avail/1048576) +" MB");
                break;

            default:
                break;
            }
        }
        
    };

}
