package com.advanpro.fwtools.module.activity;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;

import com.advanpro.fwtools.Constant;
import com.advanpro.fwtools.R;
import com.advanpro.fwtools.ble.BleDevice;
import com.advanpro.fwtools.ble.DeviceMgr;
import com.advanpro.fwtools.common.base.BasePager;

/**
 * Created by zengfs on 2016/1/18.
 * 步态页面
 */
public class GaitPager extends BasePager {
	private static final int[][] leftResIds = {{R.drawable.l_a_2_1, R.drawable.l_a_2_2, R.drawable.l_a_2_3,
			R.drawable.l_a_2_4, R.drawable.l_a_2_5,R.drawable.l_a_2_6, R.drawable.l_a_2_7, R.drawable.l_a_2_8},
			{R.drawable.l_c_2_1, R.drawable.l_c_2_2, R.drawable.l_c_2_3,R.drawable.l_c_2_4, R.drawable.l_c_2_5, 
			R.drawable.l_c_2_6, R.drawable.l_c_2_7, R.drawable.l_c_2_8}};
	private static final int[][] rightResIds = {{R.drawable.r_a_2_1, R.drawable.r_a_2_2, R.drawable.r_a_2_3, R.drawable.r_a_2_4, R.drawable.r_a_2_5,
			R.drawable.r_a_2_6, R.drawable.r_a_2_7, R.drawable.r_a_2_8},
			{R.drawable.r_c_2_1, R.drawable.r_c_2_2, R.drawable.r_c_2_3,R.drawable.r_c_2_4, R.drawable.r_c_2_5, 
			R.drawable.r_c_2_6, R.drawable.r_c_2_7, R.drawable.r_c_2_8}};
	private static final int[] impactRankColors = {R.drawable.rank_1, R.drawable.rank_2, R.drawable.rank_3, 
			R.drawable.rank_4, R.drawable.rank_5, R.drawable.rank_6, R.drawable.rank_7, R.drawable.rank_8};
	private ActivityFragment fragment;
	private ImageView[] leftViews;
	private ImageView[] rightViews;
	private ImageView[] leftViews2;
	private ImageView[] rightViews2;

	public GaitPager(Context context, ActivityFragment fragment) {
		super(context);
		this.fragment = fragment;
	}

	@Override
	protected void assignViews() {
		leftViews = new ImageView[4];
		leftViews[0] = (ImageView) rootView.findViewById(R.id.left_a);
		leftViews[1] = (ImageView) rootView.findViewById(R.id.left_b);
		leftViews[2] = (ImageView) rootView.findViewById(R.id.left_c);
		leftViews[3] = (ImageView) rootView.findViewById(R.id.left_d);
		rightViews = new ImageView[4];
		rightViews[0] = (ImageView) rootView.findViewById(R.id.right_a);
		rightViews[1] = (ImageView) rootView.findViewById(R.id.right_b);
		rightViews[2] = (ImageView) rootView.findViewById(R.id.right_c);
		rightViews[3] = (ImageView) rootView.findViewById(R.id.right_d);
		leftViews2 = new ImageView[2];
		leftViews2[0] = (ImageView) rootView.findViewById(R.id.l_heel);
		leftViews2[1] = (ImageView) rootView.findViewById(R.id.l_forefoot);
		rightViews2 = new ImageView[2];
		rightViews2[0] = (ImageView) rootView.findViewById(R.id.r_heel);
		rightViews2[1] = (ImageView) rootView.findViewById(R.id.r_forefoot);
	}

	@Override
	protected void initViews() {
		 
	}
	
	/**
	 * 更新双脚图片状态
	 * @param device 设备
	 * @param data 各传感器原始值
	 * @param impactRank 冲击力等级
	 */
	public void updateView(BleDevice device, int data, int impactRank) {		
		//分版本判断步态类型
		switch(DeviceMgr.getBoundDeviceType()) {
		    case Constant.DEVICE_TYPE_BASIC:	
				ImageView[] vs = device.isLeft ? leftViews2 : rightViews2;
				int[][] resIds = device.isLeft ? leftResIds : rightResIds;
				for (int i = 0; i < vs.length; i++) {
					vs[i].setVisibility((data & (0x08 >> (i * 2))) == 0 ? View.INVISIBLE : View.VISIBLE);
					vs[i].setBackgroundResource(resIds[i][impactRank]);
				}
				break;
		    case Constant.DEVICE_TYPE_POPULARITY:
				ImageView[] views = device.isLeft ? leftViews : rightViews;
				for (int i = 0; i < views.length; i++) {
					views[i].setVisibility((data & (0x08 >> i)) == 0 ? View.INVISIBLE : View.VISIBLE);
					views[i].setImageResource(impactRankColors[impactRank]);
				}
				break;
            default:
                ImageView[] v1 = device.isLeft ? leftViews2 : rightViews2;
                ImageView[] v2 = device.isLeft ? leftViews : rightViews;
                for (ImageView v : v1) {
                    v.setVisibility(View.INVISIBLE);
                }
                for (ImageView v : v2) {
                    v.setVisibility(View.INVISIBLE);
                }
                break;
		}
	}
	
	@Override
	protected View getRootView() {
		return View.inflate(context, R.layout.pager_gait, null);
	}
}
