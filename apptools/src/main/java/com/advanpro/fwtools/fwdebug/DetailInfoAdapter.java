package com.advanpro.fwtools.fwdebug;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.advanpro.fwtools.R;

import java.util.List;

/**
 * Created by AdvanPro on 2016/6/17.
 */
public class DetailInfoAdapter extends IFwdebugListAdapter
        implements View.OnClickListener, FwDataAsyncHandler.CompleteListener {

    private FwDebugActivity mActivity;
    private List<DetailInfoParser.SectorInfo> mDataList;

    @Override
    public void onClick(View v) {
        int pos = (Integer)v.getTag();
        switch (v.getId()) {
        case R.id.fd_detail_txt:
            DetailInfoParser.SectorInfo info = mDataList.get(pos);
            mActivity.loadFootPress(info.footDataAddr, info.footDataLen);
            break;
        }
    }

    @Override
    public void onLoadComplete(int type, int errorNo, Object data) {

    }

    private class ViewHolder {
        TextView mSportNo;
        TextView mWalks;
        TextView mRuns;

        TextView mFdNo;
        TextView mFdAddr;
        TextView mFdLen;
        TextView mFdDetail;
    }
    DetailInfoAdapter(FwDebugActivity activity) {
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

    private String convertSectorNo2Time(int sec) {
        int hours = sec/4;
        int min = (sec%4)*15;
        return "/"+Integer.toString(hours) + ":" +Integer.toString(min);
    }

    @Override
    public View getView(int pos, View convertView, ViewGroup viewGroup) {
        ViewHolder holder = null;
        if (convertView == null) {
            convertView = View.inflate(mActivity, R.layout.fwdebug_detail_sports, null);
            holder = new ViewHolder();
            holder.mSportNo = (TextView) convertView.findViewById(R.id.sport_no_txt);
            holder.mWalks = (TextView) convertView.findViewById(R.id.walks_txt);
            holder.mRuns = (TextView) convertView.findViewById(R.id.runs_txt);
            holder.mFdNo = (TextView) convertView.findViewById(R.id.fd_no_txt);
            holder.mFdAddr = (TextView) convertView.findViewById(R.id.fd_addr_txt);
            holder.mFdLen = (TextView) convertView.findViewById(R.id.fd_len_txt);
            holder.mFdDetail = (TextView) convertView.findViewById(R.id.fd_detail_txt);
            holder.mFdDetail.setOnClickListener(this);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder)convertView.getTag();
        }
        DetailInfoParser.SectorInfo info = mDataList.get(pos);
        holder.mSportNo.setText(Integer.toString(info.sectorNo) +"/index:"+ Integer.toString(pos));
        holder.mWalks.setText(" 走路数目：" + info.walkingSteps);
        holder.mRuns.setText(" 跑步：" + info.runningSteps);
        holder.mFdNo.setText(Integer.toString(info.footNo) + convertSectorNo2Time(info.footNo));
        holder.mFdAddr.setText(" 步态地址：0x" + Integer.toHexString(info.footDataAddr));
        holder.mFdLen.setText(" 长度：" + info.footDataLen);

        holder.mFdDetail.setTag(pos);

        return convertView;
    }
}
