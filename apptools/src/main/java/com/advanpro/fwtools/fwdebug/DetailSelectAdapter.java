package com.advanpro.fwtools.fwdebug;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.advanpro.fwtools.R;

import java.util.List;

/**
 * Created by AdvanPro on 2016/6/24.
 */
public class DetailSelectAdapter extends IFwdebugListAdapter
        implements View.OnClickListener, FwDataAsyncHandler.CompleteListener{

    private FwDebugActivity mActivity;
    private List<DetailInfoParser.DetailInfo> mDataList;

    @Override
    public void onClick(View v) {
        int part = (Integer)(v.getTag());
        switch (v.getId()) {
            case R.id.info_sport_txt:
                mActivity.loadDayDetail(part|0x10);
                mActivity.setTitle(mDataList.get(part).date + " 第" + (part+1) + "天");
                break;
//            case R.id.info_psas_txt:
//                part = part|0x30;
//                mActivity.loadDayDetail(part, this);
//                break;
            default:
                return;
        }
    }

    @Override
    public void onLoadComplete(int type, int errorNo, Object data) {

    }

    private class ViewHolder {
        int pos;
        TextView mDateText;
        TextView mCheckSportDetail;
    }

    DetailSelectAdapter(FwDebugActivity activity) {
        mActivity = activity;
    }

    @Override
    void updateData(List data) {
        mDataList = data;
    }
    @Override
    public int getCount() {
        if (mDataList == null) {
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
        if (convertView == null) {
            convertView = View.inflate(mActivity, R.layout.fwdebug_detailinfo_item, null);
            holder = new ViewHolder();
            holder.mDateText = (TextView) convertView.findViewById(R.id.info_date_txt);
            holder.mCheckSportDetail = (TextView) convertView.findViewById(R.id.info_sport_txt);
            holder.mCheckSportDetail.setOnClickListener(this);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.pos = pos;
        holder.mCheckSportDetail.setTag(pos);
        DetailInfoParser.DetailInfo info = mDataList.get(pos);
        holder.mDateText.setText(info.date);

        return convertView;
    }
}
