package com.hogee.grabpackageinfo.adapter;

import java.util.List;

import com.hogee.grabpackageinfo.R;
import com.hogee.grabpackageinfo.data.AppInfo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ChoiceAppListAdapter extends BaseAdapter {
    
    private Context mContext;
    private LayoutInflater mInflater;
    private List<AppInfo> mAppInfoList;

    public ChoiceAppListAdapter(Context context) {
        mContext = context;
        mInflater = (LayoutInflater) context
        		.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        Holder holder;
        if (convertView == null) {
			convertView = mInflater.inflate(R.layout.adapter_app_info, null);
			holder = new Holder();
			holder.mIcon = (ImageView) convertView.findViewById(R.id.app_icon);
			holder.mLable = (TextView) convertView.findViewById(R.id.app_lable);
			convertView.setTag(holder);
		} else {
			holder = (Holder) convertView.getTag();
		}
        
        holder.mLable.setText(mAppInfoList.get(position).getmPackageName());
        holder.mIcon.setImageResource(mAppInfoList.get(position).getmIconResId());
        
        return convertView;
    }

    class Holder {  
        private ImageView mIcon;  
        private TextView mLable;
        
		public ImageView getIcon() {
			return mIcon;
		}
		public void setIcon(ImageView mIcon) {
			this.mIcon = mIcon;
		}
		public TextView getLable() {
			return mLable;
		}
		public void setLable(TextView mLable) {
			this.mLable = mLable;
		}    
    }  
    
}
