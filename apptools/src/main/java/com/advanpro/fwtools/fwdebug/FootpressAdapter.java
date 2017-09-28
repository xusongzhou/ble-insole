package com.advanpro.fwtools.fwdebug;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.advanpro.fwtools.R;

import java.util.List;

/**
 * Created by AdvanPro on 2017/5/2.
 */

public class FootpressAdapter extends IFwdebugListAdapter {
    private FwDebugActivity mActivity;
    private List<String> mDataList;

    FootpressAdapter(FwDebugActivity activity) {
        mActivity = activity;
    }

    @Override
    void updateData(List data) {
        mDataList = data;
    }
    @Override
    public int getCount() {
        if(mDataList == null) {
            return 0;
        }
        return mDataList.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int pos, View convertView, ViewGroup viewGroup) {
        if(convertView == null) {
            convertView = View.inflate(mActivity, R.layout.fwdebug_keyvalue_item, null);
        }
        TextView text = (TextView)convertView.findViewById(R.id.info_key_txt);
        text.setText(mDataList.get(pos));
        return convertView;
    }

}
