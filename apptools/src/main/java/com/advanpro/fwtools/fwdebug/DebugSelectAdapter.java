package com.advanpro.fwtools.fwdebug;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.advanpro.fwtools.R;

import java.util.List;

/**
 * Created by AdvanPro on 2016/6/15.
 */
public class DebugSelectAdapter extends IFwdebugListAdapter {
    private Context mContext;
    private String[] mSelcetItem;
    public DebugSelectAdapter(Context context, String[] data) {
        mContext = context;
        mSelcetItem = data;
    }

    @Override
    public int getCount() {
        return mSelcetItem.length;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null) {
            convertView = View.inflate(mContext, R.layout.fwdebug_select_item, null);
        }
        TextView text = (TextView)convertView.findViewById(R.id.select_txt);
        text.setText(mSelcetItem[position]);
        return convertView;
    }

    @Override
    void updateData(List data) {

    }
}
