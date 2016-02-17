package com.hogee.storageexam;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Random;

import com.hogee.storageexam.R;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.StatFs;
import android.R.color;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.SeekBar.OnSeekBarChangeListener;

/**
 * crazy create 1KB - 2MB files to /sdcard, aim to create many storage fragmentation. simulate the user use phone.
 * @author jianhua.he
 *
 */
public class EasyTestActivity extends Activity {
    
    private static final String TAG = "fragmentation_test";
    private static boolean DEBUG = Log.isLoggable(TAG, Log.DEBUG);
    
    private Button mCreateFragmBtn;
    private Button mStopBtn;
    private Button mDeleteFragmBtn;
    private EditText mLoopCountEdt;
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
    private int MAX_FILE_FOLDER_DEPTH = 10;
    private int MIN_FILE_FOLDER_DEPTH = 3;
    private int MAX_SUB_FILE_FOLDER = 5;
    private int mBigLoopCount = 1;
    private int mBigLoopCounter = 1;
    /** the random bigest size */
    private int mRandomSize = 1024*2048;
    
    private static final int CASE_CHECK_AVAIL = 0;
    private static final int CASE_CONTINUE_WRITE = 1;
    private static final int CASE_BREAK_WRITE = 2;
    private static final int CASE_CLEARN_DATA = 3;
    private static final int CASE_DELETE_FILES = 4;
    private static final int CASE_DELETE_FILES_DONE = 5;
    private static final int CASE_RANDOM_FRAG_FILES = 6;
    private static final int CASE_AUDO_DELETE = 7;
    private static final int CASE_AUDO_AGAIN = 8;
    private static final int CASE_CANCEL_ALL = 9;
    
    private static final String CREATE_FILE_SUFFIX = ".rom_exam";
    private static final String CREATE_FILEFOLDER_PREFIX = "rom_folder_test_";
    private byte[] mBuf; 
    
    private static volatile boolean isThreadLockOn = false;
    private static volatile boolean isThreadStopClick = false;
    private volatile Object mObjectLockFlag = new Object();
    /** to recode the create files need continue create */
    private volatile boolean isLoopNeeded = true;
    private volatile boolean isCreateFileThreadRunning = false;
    private CreateFragmThread mCreateFragmThread = null;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_easy_test);
        
        mDetailTextView = (TextView) findViewById(R.id.result_show_contants);
        mResultTextView = (TextView) findViewById(R.id.result_show_contants_2);
        mFillScaleTextView = (TextView) findViewById(R.id.fill_scale_textview);
        
        mLoopCountEdt = (EditText) findViewById(R.id.loop_count_edit);
        mStopBtn = (Button) findViewById(R.id.stop_test_btn);
        mStopBtn.setOnClickListener(mStopTestListener);
        mDeleteFragmBtn = (Button) findViewById(R.id.delete_fragmentation_all_test_btn);
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
        
        mBuf = new byte[mRandomSize];
        
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
        mDetailTextView.setText(R.string.exam_intr_easy);
        mAvailCountTemp = 0;
        mFileCounter = 0;
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
    
    OnClickListener mDeleteFragmHalfListener = new View.OnClickListener() {
        
        @Override
        public void onClick(View v) {
            if (!isCreateFileThreadRunning) {
                DeleteFragmThread deleteFragmThread = new DeleteFragmThread();
                deleteFragmThread.initDeleteFragmThread(DeleteFragmThread.TYPE_DELETE_HALF);
                deleteFragmThread.execute(100);
            }
        }
    };
    
    OnClickListener mStopTestListener = new View.OnClickListener() {
        
        @Override
        public void onClick(View v) {
            if (!isCreateFileThreadRunning) {
                isLoopNeeded = false;
            }
            isThreadStopClick = true;
            if (mCreateFragmThread != null && mCreateFragmThread.getStatus() != AsyncTask.Status.FINISHED) {
                mCreateFragmThread.cancel(true);
            }
        }
    };
    
    OnClickListener mDeleteFragmRandomListener = new View.OnClickListener() {
        
        @Override
        public void onClick(View v) {
            if (!isCreateFileThreadRunning) {
                DeleteFragmThread deleteFragmThread = new DeleteFragmThread();
                deleteFragmThread.initDeleteFragmThread(DeleteFragmThread.TYPE_RANDOM_FRAG);
                deleteFragmThread.execute(100);
            }
        }
    };
    
    private void workDeleteJob() {
        while (isCreateFileThreadRunning) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (!isCreateFileThreadRunning) {
            DeleteFragmThread deleteFragmThread = new DeleteFragmThread();
            deleteFragmThread.initDeleteFragmThread(DeleteFragmThread.TYPE_RANDOM_FRAG);
            deleteFragmThread.execute(100);
        }
        
    }
    
    private void workStartAgainJob() {
        if (!isCreateFileThreadRunning) {
            initIntr();
            if (mCreateFragmThread == null) {
                mCreateFragmThread = new CreateFragmThread();
                mCreateFragmThread.execute(100);
            }
        } else {
            
        }
    }
    
    OnClickListener mDeleteFragmListener = new View.OnClickListener() {
        
        @Override
        public void onClick(View v) {
            Log.d(TAG, "isCreateFileThreadRunning = "+isCreateFileThreadRunning);
            isThreadStopClick = false;
            if (!isCreateFileThreadRunning) {
                DeleteFragmThread deleteFragmThread = new DeleteFragmThread();
                deleteFragmThread.initDeleteFragmThread(DeleteFragmThread.TYPE_DELETE_ALL);
                deleteFragmThread.execute(100);
            }
        }
    };
    
    OnClickListener mCreateFragmListener = new View.OnClickListener() {
        
        @SuppressLint("NewApi")
        @Override
        public void onClick(View v) {
            String bigLoopCountTemp = mLoopCountEdt.getText().toString();
            
            if (bigLoopCountTemp == "" || bigLoopCountTemp.isEmpty()) {
                Toast.makeText(getApplicationContext(), "请输入产生碎片操作循环次数！",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            try {
                mBigLoopCount = Integer.valueOf(bigLoopCountTemp).intValue();
            } catch (Exception e) {
                
            }
            
            if (mFillScale == 0) {
                Toast.makeText(getApplicationContext(), "请选择每次填充的比例...",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            
            if (!isCreateFileThreadRunning) {
                initBufData();
            }
            
            workStartAgainJob();
        }
    };
                        
    private void initBufData() {
        int length = mBuf.length;
        for (int i = 0; i < length; i++) {
            mBuf[i] = (byte) (Math.random() * 120);
        }
    }
    
    /**
     * according to type, decide to delete what kind of files
     * @author jianhua.he
     *
     */
    class DeleteFragmThread extends AsyncTask<Integer, Integer, String> {
        
        private static final int TYPE_RANDOM_FRAG = 3;
        private static final int TYPE_DELETE_HALF = 2;
        private static final int TYPE_DELETE_RANDOM = 1;
        private static final int TYPE_DELETE_ALL = 0;
        private int mDeleteType = TYPE_DELETE_ALL;
        
        public void initDeleteFragmThread(int deleteType) {
            mDeleteType = deleteType;
        }

        @Override
        protected String doInBackground(Integer... arg0) {
            switch (mDeleteType) {
            case TYPE_DELETE_ALL:
                //deleteAllFile();
                deleteAllFiles();
                break;
            case TYPE_DELETE_RANDOM:
                deleteRandomFile();
                break;
            case TYPE_DELETE_HALF:
                deleteHalfFile();
                break;
            case TYPE_RANDOM_FRAG:
                mFileCounter = 0;
                doRandomFrag();
                break;

            default:
                break;
            }
            return null;
        }
        
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            mHandler.sendEmptyMessageDelayed(CASE_AUDO_AGAIN, 200);
        }
        
    }
    
    private void lockSubThread() {
        synchronized (mObjectLockFlag) {
            try {
                Log.d(TAG, "lock here");
                isThreadLockOn = true;
                mObjectLockFlag.wait();
            } catch (InterruptedException e) {
                Log.d(TAG, "lock object have an issue", e);
            }
        }
    }
    
    private void unlockSubThread() {
        try {
            while (!isThreadLockOn) {
                Log.d(TAG, "wait object wait");
                Thread.sleep(50);
            }
            Thread.sleep(20);
        } catch (Exception e) {
            Log.e(TAG, "wait object wait have an issue", e);
        }
        synchronized (mObjectLockFlag) {
            Log.d(TAG, "notify");
            mObjectLockFlag.notify();
            isThreadLockOn = false;
        }
    }
    
    /**
     * create some fake data files to sdcard
     * @author jianhua.he
     *
     */
    class CreateFragmThread extends AsyncTask<Integer, Integer, String> {

        @Override
        protected String doInBackground(Integer... arg0) {
            try {
                isLoopNeeded = true;
                int iCount = 0;
                while (isLoopNeeded) {
                    Log.d(TAG, "do the create while count = "+iCount);
                    if (!isCreateFileThreadRunning) {
                        isCreateFileThreadRunning = true;
                    }
                    createRootFileFolders(iCount);
                    iCount++;
                    if (isCancelled()) {
                        mHandler.sendEmptyMessage(CASE_CANCEL_ALL);
                        break;// if stop btn, stop this asynctask
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "insertFileRandomData have an issue", e);
            } finally {
                isCreateFileThreadRunning = false;
                Log.d(TAG, "set isCreateFileThreadRunning = flase!");
            }
            return null;
        }
        
        /**
         * create file folder in /sdcard/
         * @param iCount
         */
        private void createRootFileFolders(int iCount) {
            Log.d(TAG, "createRootFileFolders  do");
            if (isLoopNeeded) {
                String fileFolderName = mSDcardDir.getAbsoluteFile() +"/"+ CREATE_FILEFOLDER_PREFIX + iCount;
                File rootFile = new File(fileFolderName);
                if (!rootFile.exists()) {
                    rootFile.mkdirs();
                    createSubFileFolders(rootFile);
                }
            }
        }
        
        /**
         * create sub file folder under /sdcard/xxxx/
         * @param rootFile
         */
        private void createSubFileFolders(File rootFile) {
            Random rand = new Random();
            int fileLevelDepth = rand.nextInt(MAX_FILE_FOLDER_DEPTH - MIN_FILE_FOLDER_DEPTH) + MIN_FILE_FOLDER_DEPTH;
            Log.d(TAG, " fileLevelDepth = "+fileLevelDepth);
            createSubFileFolders(rootFile, fileLevelDepth);
        }
        
        
        /**
         * create sub file folder and files (random)，  recursive function
         * @param rootFile
         * @param currentDepth
         */
        private void createSubFileFolders(File rootFile, int currentDepth) {
            if (isCancelled()) {
                return;// if stop btn, stop this asynctask
            }
            Log.d(TAG, " currentDepth = "+currentDepth);
            Random rand = new Random();
            if (currentDepth == 0) {
                fillRandomFiles(rootFile, rand.nextInt(10));
                return;
            }
            
            for (int i = 0; i < currentDepth; i++) {
                int fileCount = rand.nextInt(MAX_SUB_FILE_FOLDER);
                Log.d(TAG, " this fileCount = "+fileCount);
                for (int j = 0; j < fileCount; j++) {
                    if (isCancelled()) {
                        return;// if stop btn, stop this asynctask
                    }
                    File file = new File(rootFile.getAbsoluteFile()+"/"+CREATE_FILEFOLDER_PREFIX+j);
                    if (!file.exists()) {
                        file.mkdirs();
                        //create files
                        fillRandomFiles(file, rand.nextInt(10));
                        if (!isLoopNeeded) {// if size is ok, return
                            return;
                        }
                        createSubFileFolders(file, currentDepth - 1);
                    }
                }
            }
        }
        
        /**
         * create random size to fill files
         * @param rootPath
         * @param fileCount
         */
        private void fillRandomFiles(File rootPath, int fileCount) {
            try {
                for (int i = 0; i < fileCount; i++) {
                    File file = new File(rootPath.getAbsoluteFile() +"/"+ createFileName());
                    if (!file.exists()) {
                        file.createNewFile();
                        mFileCounter++;
                    }
                    
                    int fileSize = (int)(Math.random()*mRandomSize);//create random filesize 1KB - 2MB random
                    Log.d(TAG, "random file size = "+(fileSize/1024) + "KB");
                    insertFileRandomData(file, fileSize);
                    Log.d(TAG, "random file real size = "+(file.length()/1024) + "KB");
                    mAvailCountTemp += fileSize;
                    
                    if (mFileCounter % 5 == 0 && isLoopNeeded) {
                        mHandler.sendEmptyMessage(CASE_CHECK_AVAIL);
                        lockSubThread();
                    }
                    if (!isLoopNeeded) {
                        return;
                    }
                }
            } catch (IOException e) {
                Log.e(TAG, "fill random files have an issue", e);
                e.printStackTrace();
                return;
            }
        }
        
        
        /**
         * real insert, create the fake data to file and insert
         * @param file
         * @param FileSize
         */
        @SuppressLint("NewApi")
        private void insertFileRandomData(File file, int FileSize) {//filesize 1KB 2MB random
            FileOutputStream out = null;
            try {
                out = new FileOutputStream(file);
                //byte[] buf = new byte[FileSize];
                //for (int i = 0; i < buf.length; i++) {// fill real data, aim to flash the cell about EMMC, but it is slow
                //    buf[i] = (byte) (Math.random() * 120);// byte -> value: 0 - 127
                //}
                if (FileSize >= 2097100) {
                    FileSize = 1024;
                }
                out.write(Arrays.copyOfRange(mBuf, 0, FileSize));
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
                    Log.e(TAG, "insert file data close have an issue!", e);
                    e.printStackTrace();
                }
            }
        }
        
        /**
         * according the file name, create real file, and count the files size or some thing else
         * @param fileName
         * @throws IOException
         */
        /*private void insertFileRandomData(String fileName) throws IOException {
            File file = new File(mSDcardDir.getAbsoluteFile() +"/"+ fileName);
            if (!file.exists()) {
                file.createNewFile();
                mFileCounter++;
            }
            
            int fileSize = 1024*4;//1024 * (1 + (int)(Math.random()*2048));//create random filesize 1KB - 2MB random
            Log.d(TAG, "random file size = "+(fileSize/1024) + "KB");
            insertFileRandomData(file, fileSize);
            Log.d(TAG, "random file real size = "+(file.length()/1024) + "KB");
            mAvailCountTemp += fileSize;
            
            if (mFileCounter % 5 == 0) {
                mHandler.sendEmptyMessage(CASE_CHECK_AVAIL);
                lockSubThread();
            }
        }*/
    }
    
    /**
     * create the different files name about what you want fill 
     * @return fileNameString
     */
    private String createFileName() {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss.SSS");
        String fileNameString = df.format(new Date());
        fileNameString = "fragmentation_test_"+fileNameString+CREATE_FILE_SUFFIX;
        Log.d(TAG, "create file name = "+fileNameString);
        return fileNameString;
    }
    
    
    /**
     * the file name filter, filter like : xxxx.CREATE_FILE_SUFFIX
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
    
    class ExamFileFolderNameFilter implements FilenameFilter {
        
        private String fileNameType;  
        public ExamFileFolderNameFilter(String type){  
            this.fileNameType = type;  
        }

        @Override
        public boolean accept(File dir, String name) {
            return name.startsWith(fileNameType);
        }
        
    }
    
    /**
     * random operation for those files, maybe delete, maybe copy, maybe do nothing. that is create fragment
     */
    private void doRandomFrag() {
        File[] files = mSDcardDir.listFiles(new ExamFileFolderNameFilter(CREATE_FILEFOLDER_PREFIX));
        mTotalFileCount = files.length - 1;
        for (int i = 0; i < files.length; i++) {
            Log.d(TAG, ""+files[i].getName());
            doRandomFilesFrag(files[i]);
            if (isThreadStopClick) {
                break;
            }
        }
        
        mHandler.sendEmptyMessage(CASE_DELETE_FILES_DONE);
    }
    
    private void doRandomFilesFrag(File parentPath) {
        if (isThreadStopClick) {
            return;
        }
        File[] fileFolder = parentPath.listFiles(new ExamFileFolderNameFilter(CREATE_FILEFOLDER_PREFIX));
        File[] files = parentPath.listFiles(new ExamFileNameFilter(CREATE_FILE_SUFFIX));
        for (int i = 0; i < files.length; i++) {
            doRealRandomFiles(files[i]);
            if (isThreadStopClick) {
                break;
            }
        }
        mHandler.sendEmptyMessage(CASE_RANDOM_FRAG_FILES);
        
        for (int i = 0; i < fileFolder.length; i++) {
            doRandomFilesFrag(fileFolder[i]);
            if (isThreadStopClick) {
                break;
            }
        }
    }
    
    /**
     * random operation for those files, maybe delete, maybe copy, maybe do nothing. that is create fragment
     * @param file
     */
    private void doRealRandomFiles(File file) {
        Random random = new Random();
        int choice = random.nextInt(4);
        switch (choice) {
        case 0:
            Log.d(TAG, "do nothing about this file !");
            break;
        case 1:
            Log.d(TAG, "copy file, and delete sorce file !");
            try {
                File targetFile = new File(file.getParent()+"/"+createFileName());
                if (!targetFile.exists()) {
                    targetFile.createNewFile();
                }
                fileChannelCopy(file, targetFile);
                file.delete();
            } catch (IOException e) {
                Log.d(TAG, "copy file have an issue", e);
            }
            
            break;
        case 2:
            Log.d(TAG, "delete file directly !");
            if (file != null && file.exists()) {
                file.delete();
                mFileCounter++;
            }
            break;
        case 3:
            Log.d(TAG, "just create file directly !");
            try {
                File targetFile = new File(file.getParent()+"/"+createFileName());
                if (!targetFile.exists()) {
                    targetFile.createNewFile();
                }
            } catch (IOException e) {
                Log.d(TAG, "create file have an issue", e);
            }
            break;

        default:
            break;
        }
    }
    
    /**
     * file copy 
     * @param s source file
     * @param t target file
     */
    public void fileChannelCopy(File s, File t) {
        FileInputStream fi = null;
        FileOutputStream fo = null;
        FileChannel in = null;
        FileChannel out = null;
        try {
            fi = new FileInputStream(s);
            fo = new FileOutputStream(t);
            in = fi.getChannel();//得到对应的文件通道
            out = fo.getChannel();//得到对应的文件通道
            in.transferTo(0, in.size(), out);//连接两个通道，并且从in通道读取，然后写入out通道
        } catch (IOException e) {
            Log.e(TAG, "copy file have an issue 2", e);
            e.printStackTrace();
        } finally {
            try {
                fi.close();
                in.close();
                fo.close();
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    /**
     * delete half of files 
     */
    private void deleteHalfFile() {
        File[] files = mSDcardDir.listFiles(new ExamFileNameFilter(CREATE_FILE_SUFFIX));
        mTotalFileCount = files.length - 1;
        for (int i = 0; i < files.length; i += 2) {
            Log.d(TAG, "delete file = "+files[i]);
            files[i].delete();
            mFileCounter = i;
            if (i % 10 == 0) {
                mHandler.sendEmptyMessage(CASE_DELETE_FILES);
            }
            if (isThreadStopClick) {
                break;
            }
        }
        
        mHandler.sendEmptyMessage(CASE_DELETE_FILES_DONE);
    }
    
    /**
     * delete about {deleteRate}% files
     */
    private void deleteRandomFile() {
        File[] files = mSDcardDir.listFiles(new ExamFileNameFilter(CREATE_FILE_SUFFIX));
        mTotalFileCount = files.length - 1;
        
        int deleteRate = 70;// can be add a choice item
        deleteRate = (files.length - 1) * deleteRate / 100;
        Random random = new Random();
        
        int i = 0;
        while (i <= deleteRate) {
            i++;
            try {
                files[random.nextInt(files.length - 1)].delete();
                mFileCounter = i;
                if (i % 5 == 0) {
                    mHandler.sendEmptyMessage(CASE_DELETE_FILES);
                }
            } catch (Exception e) {
                Log.e(TAG, "random delete file have an issue!", e);
            }
            if (isThreadStopClick) {
                break;
            }
        }
        
        mHandler.sendEmptyMessage(CASE_DELETE_FILES_DONE);
    }
    
    /**
     * delete all the xxx.CREATE_FILE_SUFFIX files in /sdcard
     */
    private void deleteAllFile() {
        File[] files = mSDcardDir.listFiles(new ExamFileNameFilter(CREATE_FILE_SUFFIX));
        mTotalFileCount = files.length - 1;
        for (int i = 0; i < files.length; i++) {
            Log.d(TAG, "delete file = "+files[i]);
            files[i].delete();
            mFileCounter = i;
            if (i % 5 == 0) {
                mHandler.sendEmptyMessage(CASE_DELETE_FILES);
            }
            if (isThreadStopClick) {
                break;
            }
        }
        
        mHandler.sendEmptyMessage(CASE_DELETE_FILES_DONE);
    }
    
    /**
     * delete all CREATE_FILEFOLDER_PREFIX fileFolder s 
     */
    private void deleteAllFiles() {
        File[] files = mSDcardDir.listFiles(new ExamFileFolderNameFilter(CREATE_FILEFOLDER_PREFIX));
        mTotalFileCount = files.length - 1;
        Log.d(TAG, "mTotalFileCount = "+mTotalFileCount);
        for (int i = 0; i < files.length; i++) {
            Log.d(TAG, "delete fileFloder = "+files[i]);
            //files[i].delete();
            recursionDeleteFile(files[i]);
            mFileCounter = i;

            mHandler.sendEmptyMessage(CASE_DELETE_FILES);
            if (isThreadStopClick) {
                mHandler.sendEmptyMessage(CASE_CANCEL_ALL);
                break;
            }
        }
        if (!isThreadStopClick) {
            mHandler.sendEmptyMessage(CASE_DELETE_FILES_DONE);
        }
    }
    
    private void recursionDeleteFile(File file){
        if (isThreadStopClick) {
            return;
        }
        if(file.isFile()){
            file.delete();
            return;
        }
        if(file.isDirectory()){
            File[] childFile = file.listFiles();
            if(childFile == null || childFile.length == 0){
                file.delete();
                return;
            }
            for(File f : childFile){
                recursionDeleteFile(f);
            }
            file.delete();
        }
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
            case CASE_CANCEL_ALL:
                mResultTextView.setText("cancel all the opreation!!!");
                mCreateFragmThread = null;
                break;
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
                mResultTextView.setText("the loop "+mBigLoopCounter+": create fill:"+String.format("%.2f", proportion)+" % \n");
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
                    unlockSubThread();
                } else {
                    isLoopNeeded = false;
                    mResultTextView.append("\n\n DONE!!!!!!");
                    unlockSubThread();
                    Log.d(TAG, "isThreadStopClick = "+isThreadStopClick);
                    if (!isThreadStopClick) {
                        mHandler.sendEmptyMessageDelayed(CASE_AUDO_DELETE, 200);
                    }
                }
                break;
            case CASE_AUDO_DELETE:
                workDeleteJob();
                break;
            case CASE_AUDO_AGAIN:
                mBigLoopCounter++;
                if (isThreadStopClick) {
                    return;
                }
                if (mBigLoopCounter > mBigLoopCount) {
                    initIntr();
                    mResultTextView.setText("total loop "+mBigLoopCount+": ALL DONE!!! \n\n already do the crazy ROM fragment, you can test the ROM score now ");
                    mBigLoopCounter = 1;
                    mCreateFragmThread = null;
                } else {
                    workStartAgainJob();
                }
                break;
            case CASE_CONTINUE_WRITE:
                isLoopNeeded = true;
                break;
            case CASE_BREAK_WRITE:
                isLoopNeeded = false;
                break;
                
            case CASE_CLEARN_DATA:
                mDetailTextView.setText("");
                break;
                
            case CASE_DELETE_FILES:
                int prop = (int) (100 * mFileCounter / (mTotalFileCount + 1));
                mProgressView.setProgress(100 - prop);
                mResultTextView.setText("delete file: "+mFileCounter);
                break;
                
            case CASE_RANDOM_FRAG_FILES:
                mProgressView.setProgress(1);
                mResultTextView.setText("the loop "+mBigLoopCounter+": delete file: "+mFileCounter);
                break;
                
            case CASE_DELETE_FILES_DONE:
                initIntr();
                mProgressView.setProgress(0);
                mResultTextView.setText("the loop "+mBigLoopCounter+": delete file: "+mFileCounter);
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
