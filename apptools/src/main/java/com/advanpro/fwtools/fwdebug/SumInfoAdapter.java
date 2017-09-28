package com.advanpro.fwtools.fwdebug;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.advanpro.fwtools.R;

import java.util.List;

/**
 * Created by AdvanPro on 2016/6/17.
 */
public class SumInfoAdapter extends IFwdebugListAdapter {
    private FwDebugActivity mActivity;
    private List<SumInfoParser.SumInfo> mDataList;

    private class ViewHolder {
        TextView mDataText;
        TextView mFootText;
        TextView mSportText;
        TextView mStandText;
    }

    SumInfoAdapter(FwDebugActivity activity) {
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
        ViewHolder holder = null;
        if(convertView == null) {
            convertView = View.inflate(mActivity, R.layout.fwdebug_suminfo_item, null);
            holder = new ViewHolder();
            holder.mDataText = (TextView) convertView.findViewById(R.id.info_date_txt);
            holder.mFootText = (TextView) convertView.findViewById(R.id.info_foot_txt);
            holder.mSportText = (TextView) convertView.findViewById(R.id.info_sport_txt);
            holder.mStandText = (TextView) convertView.findViewById(R.id.info_stand_txt);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder)convertView.getTag();
        }
        SumInfoParser.SumInfo  info = mDataList.get(pos);
        holder.mDataText.setText(info.date);
        StringBuilder builder = new StringBuilder("前脚：" + info.pressFront);
        builder.append(" 全脚：" + info.pressFull);
        builder.append(" 脚跟：" + info.pressEnd);
        builder.append(" 内翻：" + info.pressInward);
        builder.append(" 外翻：" + info.pressOutward);
        holder.mFootText.setText(builder);


        builder = new StringBuilder("跑步：" + info.runningSteps);
        builder.append(" 跑步时间：" + info.runningTime);
        builder.append(" 走路：" + info.walkingSteps);
        builder.append(" 走路时间：" + info.walkingTime);
        holder.mSportText.setText(builder);

        builder = new StringBuilder("坐：" + info.standTime);
        builder.append(" 站：" + info.sitTime);
        holder.mStandText.setText(builder);
        return convertView;
    }
}
