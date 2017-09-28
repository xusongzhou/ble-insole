package com.advanpro.fwtools.module.activity;

import android.util.SparseArray;
import com.advanpro.fwtools.Constant;
import com.advanpro.fwtools.MyApplication;
import com.advanpro.fwtools.R;
import com.advanpro.fwtools.common.util.DateUtils;
import com.advanpro.fwtools.common.util.UiUtils;
import com.advanpro.fwtools.db.Dao;
import com.advanpro.fwtools.db.RunPlan;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by zengfs on 2016/2/25.
 * 跑步计划解析
 */
public class RunPlanParser {
	private static final int[] totalDayCount = {56, 56, 56, 56, 56};
	private static final int[] totalWeekCount = {8, 8, 8, 8, 8};
	private static SparseArray<List<Item>> plans;	
	
	static {
		InputStream is = MyApplication.getInstance().getResources().openRawResource(R.raw.run_plan);
		byte[] buffer = new byte[1024];	
		int length;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		String json;
		try {
			while ((length = is.read(buffer)) != -1) {
				baos.write(buffer, 0, length);				
			}
			json = baos.toString();
			baos.close();
			is.close();
			
			//加载到内存
			plans = new SparseArray<>();
			JSONArray array = new JSONArray(json);
			for (int i = 0; i < array.length(); i++) {
				JSONObject planObj = array.getJSONObject(i);
				JSONArray weekArray = planObj.getJSONArray("items");
				List<Item> items = new ArrayList<>();
				for (int j = 0; j < weekArray.length(); j++) {
					JSONObject weekObj = weekArray.getJSONObject(j);
					Item item = new Item();
					item.title = weekObj.getString("title");
					item.content = weekObj.getString("content");
					item.subItems = new ArrayList<>();
					JSONArray dayArray = weekObj.getJSONArray("items");
					for (int k = 0; k < dayArray.length(); k++) {
						JSONObject dayObj = dayArray.getJSONObject(k);
						SubItem subItem = new SubItem();
						subItem.title = dayObj.getString("title");
						subItem.content = dayObj.getString("content");
						item.subItems.add(subItem);
					}
					items.add(item);
				}	
				plans.put(i, items);
			}
		} catch (Exception e) {
			e.printStackTrace();			
		}		
	}
	
	/**
	 * 根据计划类型获取计划名
	 */
	public static String getPlanString(int type) {
		switch(type) {
		    case Constant.PLAN_DAILY_FITNESS:		
				return UiUtils.getString(R.string.daily_fitness_plan);
			case Constant.PLAN_LOSE_WEIGHT_EXERCISE:
				return UiUtils.getString(R.string.lose_weight_exercise_plan);
			case Constant.PLAN_MARATHON_TRAINING_5KM:
				return UiUtils.getString(R.string.marathon_training_plan_5km);
			case Constant.PLAN_MARATHON_TRAINING_10KM:
				return UiUtils.getString(R.string.marathon_training_plan_10km);
			case Constant.PLAN_MARATHON_TRAINING_FULL:
				return UiUtils.getString(R.string.marathon_training_plan_full);
		    default:				
				return null;
		}
	}

	/**
	 * 根据计划类型获取计划总天数
	 */
	public static int getTotalDayCount(int type) {
		return type > 0 && type <= 5 ? totalDayCount[type - 1] : -1;
	}

	/**
	 * 根据计划类型获取计划总周数
	 */
	public static int getTotalWeekCount(int type) {
		return type > 0 && type <= 5 ? totalWeekCount[type - 1] : -1;
	}

    /**
     * 
     */
    
	/**
	 * 获取进行到计划的第几天
	 */
	public static int getDayIndexOfPlan() {
		RunPlan plan = Dao.INSTANCE.queryRunPlan();
		if (plan != null) {
			return DateUtils.daysBetween(plan.getStartDate(), new Date()) + 1;
		}
		return -1;
	}

	/**
	 * 生成一个默认的训练执行情况字符串
	 */
	public static String createInitFulfillState(int type) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < getTotalDayCount(type); i++) {
			sb.append(0);			
		}
		return sb.toString();
	}

	/**
	 * 获取进行过训练的天数
	 */
	public static int getTrainedDays(String fulfillState) {
		if (fulfillState != null) {
			char[] chars = fulfillState.toCharArray();
			int count = 0;
			for (char aChar : chars) {
				if (aChar == '1') count++;
			}
			return count;
		}
		return 0;
	}
	
	/**
	 * 获取计划文本集合
	 */
	public static List<Item> getPlanData(int type) {
		return plans.get(type - 1);
	}
	
	public static class Item {
		public String title;
		public String content;
		public List<SubItem> subItems;
	}

	public static class SubItem {
		public String title;
		public String content;
	}
}
