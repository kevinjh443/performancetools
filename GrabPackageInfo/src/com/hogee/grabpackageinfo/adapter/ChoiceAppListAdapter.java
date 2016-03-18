package com.hogee.grabpackageinfo.adapter;

import java.util.List;

import com.hogee.grabpackageinfo.data.AppInfo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class ChoiceAppListAdapter extends BaseAdapter {
    
    private Context mContext;
    private LayoutInflater mInflater;
    private List<AppInfo> mAppInfoList;

    public ChoiceAppListAdapter(Context context) {
        mContext = context;
    }
    
    public void setAppInfoListItems(List<AppInfo> appInfolist) {
        mAppInfoList = appInfolist;
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return mAppInfoList.size();
    }

    @Override
    public Object getItem(int index) {
        // TODO Auto-generated method stub
        return mAppInfoList.get(index);
    }

    @Override
    public long getItemId(int arg0) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        
        return convertView;
    }

}
