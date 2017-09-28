package com.advanpro.fwtools.module.stat;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.advanpro.fwtools.Constant;
import com.advanpro.fwtools.R;
import com.advanpro.fwtools.common.base.BaseHolder;
import com.advanpro.fwtools.common.base.BaseListAdapter;
import com.advanpro.fwtools.common.base.BasePager;
import com.advanpro.fwtools.common.util.DateUtils;
import com.advanpro.fwtools.common.util.StringUtils;
import com.advanpro.fwtools.db.Activity;
import com.advanpro.fwtools.db.Dao;
import com.advanpro.fwtools.db.RunRecord;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by DEV002 on 2016/3/21.
 */
public class ActivityAdapter extends BasePager {

    public ActivityAdapter(Context context) {
        super(context);
    }

    @Override
    protected View getRootView() {
        return View.inflate(context, R.layout.activity_activity_viewpager, null);
    }

    @Override
    protected void assignViews() {
        ListView l = (ListView) rootView.findViewById(R.id.activity_listview);
        ListAdapter adapter = new ListAdapter(getListData());
        View v = View.inflate(context, R.layout.list_null_view, null);
        v.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        ((ViewGroup) l.getParent()).addView(v);
        l.setEmptyView(v);
        l.setAdapter(adapter);
    }

    private List<Activity> getListData() {
        List<Activity> list = new ArrayList<>();
        for (Activity a : Dao.INSTANCE.queryAllActivities(true)) {
            if (a.runningDistance > 0 || a.runningCalorie > 0 || a.runningTime > 0 || a.runningSteps > 0 ||
                    a.runRate > 0 || a.walkCalorie > 0 || a.walkDistance > 0 || a.walkTime > 0 || a.walkSteps > 0) {
                list.add(a);
            }
        }
        return list;
    }

    class ListAdapter extends BaseListAdapter<Activity> {

        public ListAdapter(List<Activity> data) {
            super(data);
        }

        @Override
        protected BaseHolder<Activity> getHolder() {
            return new BaseHolder<Activity>() {
                private TextView time;
                private TextView num;
                private TextView countTime;
                private TextView countPosition;
                private TextView countPoistion1;
                private TextView countKill;
                private TextView countKill1;
                private TextView num1;
                private TextView num2;
                private TextView countTime1;
                private ImageButton positionBtn;
                
                @Override
                protected void setData(final Activity value, int position) {                    
                    time.setText(DateUtils.formatDate(value.getDate(), "yyyy-MM-dd"));
                    num.setText(value.walkSteps + context.getString(R.string.unit_step));
                    countTime.setText(String.format("%02d:%02d:%02d", value.walkTime / 3600,
                            value.walkTime % 3600 / 60, value.walkTime % 60));
                    countPosition.setText((value.walkDistance > 0 ? StringUtils.formatDecimal(value.walkDistance, 2) : 0) +
                            context.getString(R.string.km));
                    countKill.setText((value.walkCalorie > 0 ? StringUtils.formatDecimal(value.walkCalorie, 1) : 0) +
                            context.getString(R.string.kcal));
                    num1.setText((value.runningDistance > 0 ? StringUtils.formatDecimal(value.runningDistance, 2) : 0) +
                            context.getString(R.string.km));
                    num2.setText(value.runningSteps + context.getString(R.string.unit_step));
                    countTime1.setText(String.format("%02d:%02d:%02d", value.runningTime / 3600,
                            value.runningTime % 3600 / 60, value.runningTime % 60));
                    countPoistion1.setText((value.runRate == 0 ? "0" : String.format("%.1f", value.runRate)));
                    countKill1.setText((value.runningCalorie > 0 ? StringUtils.formatDecimal(value.runningCalorie, 1) : 0) +
                            context.getString(R.string.kcal));                    

					List<RunRecord> records = Dao.INSTANCE.queryRunRecords(value.getDate());
					boolean result = false;
					for (RunRecord rec : records) {
						if (Dao.INSTANCE.queryRunLatlngs(rec.getId()).size() > 0) {
							result = true;
							break;
						}
					}
					positionBtn.setVisibility(result ? View.VISIBLE : View.INVISIBLE);
                    positionBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent i = new Intent(context, PathRecordActivity.class);
                            i.putExtra(Constant.EXTRA_TIME, value.getDate().getTime());
                            context.startActivity(i);
                        }
                    });
                }

                @Override
                protected View createConvertView() {
                    View convertView = View.inflate(context, R.layout.activity_activity_listview_item, null);
                    time = (TextView) convertView.findViewById(R.id.time);
                    countTime = (TextView) convertView.findViewById(R.id.countTime);
                    countPosition = (TextView) convertView.findViewById(R.id.countPoistion);
                    countKill = (TextView) convertView.findViewById(R.id.countKill);
                    num1 = (TextView) convertView.findViewById(R.id.num1);
                    num2 = (TextView) convertView.findViewById(R.id.num2);
                    countTime1 = (TextView) convertView.findViewById(R.id.countTime1);
                    countPoistion1 = (TextView) convertView.findViewById(R.id.countPoistion1);
                    countKill1 = (TextView) convertView.findViewById(R.id.countKill1);
                    positionBtn = (ImageButton) convertView.findViewById(R.id.positionImage);
                    num = (TextView) convertView.findViewById(R.id.num);
                    return convertView;
                }
            };
        }
    }
}
