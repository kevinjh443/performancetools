package com.hogee.performancealarmrebatch;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.R.bool;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity {
    
    private final static String TAG = "MainActivity_main";
    
    private Button btn_action;
    private Button btn_release;
    private Button btn_alarm_later;
    private TextView txv_result;
    private Context context;
    private final int amCount = 500;
    private List<AlarmManager> alarms = new ArrayList<AlarmManager>();
    private List<String> mAlarmName = new ArrayList<String>();
    private List<PendingIntent> mPendingIntent = new ArrayList<PendingIntent>();
    private int[] mDelay = new int[amCount];
    private long mExeBeginTime = 0L;
    private long mExeEndTime = 0L;
    private String mShowReString = "";
    private boolean isTestAlarmLaterForDoze = false;
    private List<PendingIntent> mPendingIntentAlarmLater = new ArrayList<PendingIntent>();
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        context = this;
        btn_action = (Button) findViewById(R.id.button1);
        btn_release = (Button) findViewById(R.id.button2);
        btn_alarm_later = (Button) findViewById(R.id.button3);
        txv_result = (TextView) findViewById(R.id.textView2);
        
        btn_action.setOnClickListener(actionListener);
        btn_release.setOnClickListener(releaseListener);
        btn_alarm_later.setOnClickListener(mAlarmLaterListener);
        
        for (int i = 0; i < amCount; i++) {
            mDelay[i] = 2000 + (500*(i+1));
        }
        
        final IntentFilter filter = new IntentFilter();    
        filter.addAction(Intent.ACTION_SCREEN_OFF);    
        filter.addAction(Intent.ACTION_SCREEN_ON);   
        //filter.addAction(Intent.ACTION_USER_PRESENT); 
        
        registerReceiver(mBatInfoReceiver, filter);
    }
    
    View.OnClickListener mAlarmLaterListener = new View.OnClickListener() {
        
        @Override
        public void onClick(View v) {
            synchronized (this) {
                if (isTestAlarmLaterForDoze) {
                    isTestAlarmLaterForDoze = false;
                    btn_alarm_later.setText(R.string.button_alrarm_later);
                    if (mPendingIntentAlarmLater.size() > 0) {
                        mPendingIntentAlarmLater.clear();
                    }
                    Log.i(TAG, "cancel alarm later test");
                } else {
                    isTestAlarmLaterForDoze = true;
                    btn_alarm_later.setText(R.string.button_alrarm_later_on);
                    
                    AlarmManager alarm = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
                    
                    PendingIntent sender = PendingIntent.getBroadcast(
                            context, 0, new Intent("TestAlarmLaterForDoze"), 2);
                    
                    mPendingIntentAlarmLater.add(sender);
                    
                    Date t = new Date();
                    t.setTime(java.lang.System.currentTimeMillis() + 2*60*1000);//2 min later
                    
                    alarm.set(AlarmManager.RTC_WAKEUP, t.getTime(), sender);
                    Log.i(TAG, "set alarm 2 min later");
                }
            }
            
        }
    };
    
    View.OnClickListener actionListener = new View.OnClickListener() {
        
        @Override
        public void onClick(View v) {
            for (int i = 0; i < amCount; i++) {
                mAlarmName.add("RTC_ALARM_"+i);
                if (i%50 == 0 || i== (amCount-1)) {
                    Log.i(TAG, "i = "+i+"  alarm = "+mAlarmName.get(i));
                }
                
                
                AlarmManager alarm = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
                
                PendingIntent sender = PendingIntent.getBroadcast(
                        context, 0, new Intent(mAlarmName.get(i)), 1);
                
                mPendingIntent.add(sender);
                
                Date t = new Date();
                t.setTime(java.lang.System.currentTimeMillis() + mDelay[i]);
                
                alarm.set(AlarmManager.RTC_WAKEUP, t.getTime(), sender);
                alarms.add(alarm);
            }
            Log.i(TAG, "------------wait the alarm do---------------------------");
        }
    };
    
    View.OnClickListener releaseListener = new View.OnClickListener() {
        
        @Override
        public void onClick(View v) {
            for (int i = 0; i < amCount; i++) {
                alarms.get(i).cancel(mPendingIntent.get(i));
            }
            Log.i(TAG, "------------release done---------------------------");
        }
    };
    
    final BroadcastReceiver mBatInfoReceiver = new BroadcastReceiver() {    
        @Override    
        public void onReceive(final Context context, final Intent intent) {  
              
            Log.d(TAG, " on receive do"); 
            String action = intent.getAction();
 
           if(Intent.ACTION_SCREEN_OFF.equals(action)){    
                Log.d(TAG, "screen is off...test isTestAlarmLaterForDoze = "+isTestAlarmLaterForDoze);
                if (isTestAlarmLaterForDoze) {
                    return;
                }
                mExeBeginTime = java.lang.System.currentTimeMillis();
                // do alarm  to test the alarm for screen on performance
                AlarmManager alarm = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
                
                PendingIntent sender = PendingIntent.getBroadcast(
                        context, 0, new Intent("RTC_ALARM_"+(amCount-5)), 1);
                
                Date t = new Date();
                t.setTime(java.lang.System.currentTimeMillis() + 2000);
                
                alarm.set(AlarmManager.RTC_WAKEUP, t.getTime(), sender);
                
                alarm.set(AlarmManager.RTC_WAKEUP, t.getTime()+100, sender);
                
                mExeEndTime = java.lang.System.currentTimeMillis();
                mShowReString += (mExeEndTime - mExeBeginTime)+" ms";
                Log.i(TAG,"---------exe time off = "+mShowReString);
           }  else if (Intent.ACTION_SCREEN_ON.equals(action)) {
               Log.d(TAG, "screen is on...");
               mExeEndTime = java.lang.System.currentTimeMillis();
               mShowReString += (mExeEndTime - mExeBeginTime)+" ms  \n\n";
               Log.i(TAG,"---------exe time on = "+mShowReString);
               txv_result.setText(mShowReString);
           }
           
        }    
    };  

}
