package com.advanpro.fwtools.module.activity;

import android.animation.Animator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.advanpro.fwtools.R;
import com.advanpro.fwtools.common.util.AnimatorUtils;
import com.advanpro.fwtools.common.util.UiUtils;

/**
 * Created by zengfs on 2016/3/1.
 * 跑步计划Item，包括字item
 */
public class PlanItemView extends FrameLayout {
	private View divider;
	private ImageView ivIndicate;
	private TextView tvTitle;
	private TextView tvContent;
	private View froeground;
	private boolean isFolded;
	private LinearLayout llSubItems;
	private int planType;
	private int itemIndex;//第几周
	private int subItemIndex;//当前item的第一个subItem在整个plan中的索引，即第几天
	private RunPlanView runPlanView;
	private RunPlanParser.Item item;
	private PlanSubItemView[] subItemViews;
	private int dayIndexOfPlan;
	private boolean isCurrentPlan;
	private int subItemHeight = -1;

	public PlanItemView(Context context) {
		this(context, null);
	}

	public PlanItemView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public PlanItemView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init();
	}

	private void init() {
		View rootView = View.inflate(getContext(), R.layout.layout_plan_item, this);
		divider = rootView.findViewById(R.id.divider);
		ivIndicate = (ImageView) rootView.findViewById(R.id.iv_indicate);
		tvTitle = (TextView) rootView.findViewById(R.id.tv_title);
		tvContent = (TextView) rootView.findViewById(R.id.tv_content);
		froeground = rootView.findViewById(R.id.froeground);
		llSubItems = (LinearLayout) rootView.findViewById(R.id.ll_container);
		rootView.findViewById(R.id.fl_item).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				setFold(!isFolded, true);
			}
		});
	}

	private void setFold(boolean fold, boolean enableAnim) {
		isFolded = fold;
		ivIndicate.setBackgroundResource(isFolded ? R.drawable.folded : R.drawable.unfolded);
		//为了加快界面加载速度，在点击主条目时再创建子条目
		if (!isFolded && subItemViews == null) initSubItems();
		fold(enableAnim);		
	}

	public void setData(RunPlanView runPlanView, int type, int itemIndex, int subItemIndex) {
		if (RunPlanParser.getPlanData(type) == null) return;
		this.runPlanView = runPlanView;
		this.planType = type;
		this.itemIndex = itemIndex;
		this.subItemIndex = subItemIndex;
		this.item = RunPlanParser.getPlanData(type).get(itemIndex - 1);
		updateView();
	}

	private void updateConditions() {
		if (runPlanView != null) {
			dayIndexOfPlan = runPlanView.getDayIndexOfPlan();
			isCurrentPlan = runPlanView.isEqualsSavedPlan();
		}
	}
	
	//初始化子条目
	private void initSubItems() {
		subItemViews = new PlanSubItemView[7];
		for (int i = 0; i < subItemViews.length; i++) {
			subItemViews[i] = new PlanSubItemView(getContext());
			RunPlanParser.SubItem subItem = item.subItems.get(i);
			llSubItems.addView(subItemViews[i], -1, subItemHeight == -1 ? UiUtils.dip2px(50) : subItemHeight);
			subItemViews[i].setTitle(subItem.title);
			subItemViews[i].setContent(subItem.content);
			subItemViews[i].setRunPlanView(runPlanView);
		}
		updateConditions();
		updateSubItems();
	}
	
	public void updateView() {
		updateConditions();
		//如果进行到的日期在此条目内，则展开，否则折叠
		setFold(!(isCurrentPlan && dayIndexOfPlan >= subItemIndex && dayIndexOfPlan < subItemIndex + item.subItems.size()), false);
		tvTitle.setText(item.title);
		tvContent.setText(item.content);
		//用蒙层遮住情况：
		//1、未开始任何计划
		//2、正在进行的计划不是此计划
		//3、正在进行的计划是此计划，但未进行到的条目
		froeground.setVisibility(!isCurrentPlan || dayIndexOfPlan == -1 || dayIndexOfPlan < subItemIndex ? VISIBLE : INVISIBLE);
		updateSubItems();
	}

	//更新子条目界面
	private void updateSubItems() {		
		if (subItemViews == null) return;
		for (int i = 0; i < subItemViews.length; i++) {			
			int subIndex = subItemIndex + i;
			subItemViews[i].setCheckable(!((dayIndexOfPlan != -1 && subIndex > dayIndexOfPlan) ||
					dayIndexOfPlan == -1 || !isCurrentPlan));
			//此子条目在整个计划中的序号，即第几天
			subItemViews[i].setIndexOfPlan(subIndex);
			//此子条目是否是正在进行的，即今天
			subItemViews[i].setCurrent(isCurrentPlan && subIndex == dayIndexOfPlan);			
			//如果正在进行的计划不是此计划，复选框都为未选中状态
			subItemViews[i].setChecked(runPlanView != null && runPlanView.isFulfill(subIndex), true);
			//用蒙层遮住情况基本与主条目一致
			subItemViews[i].setFroegroundVisible(!subItemViews[i].isCheckable());
			//如果是最后一个条目，则隐藏路径线
			subItemViews[i].setDividerVisible(subIndex != RunPlanParser.getTotalDayCount(planType));
		}
	}

	
	
	private void fold(boolean enableAnim) {	
		int start = isFolded ? measureHeight(llSubItems) : 0;
		int end = isFolded ? 0 : measureHeight(llSubItems);
		if (enableAnim) {
			AnimatorUtils.addIntAnimatorToView(llSubItems, animatorListener, AnimatorUtils.VERTICAL, 200, start, end);
		} else {
			ViewGroup.LayoutParams params = llSubItems.getLayoutParams();
			params.height = end;
			llSubItems.setLayoutParams(params);
			divider.setVisibility((RunPlanParser.getTotalWeekCount(planType) == itemIndex) && isFolded ? INVISIBLE : VISIBLE);
		}		
	}

	private Animator.AnimatorListener animatorListener = new Animator.AnimatorListener() {
		@Override
		public void onAnimationStart(Animator animation) {
			//主条目展开，则路径线显示
			if (!isFolded) divider.setVisibility(VISIBLE);
		}

		@Override
		public void onAnimationEnd(Animator animation) {
			//主条目折叠，如果是最后一个条目，则隐藏路径线
			if (isFolded) {
				divider.setVisibility(RunPlanParser.getTotalWeekCount(planType) == itemIndex ? INVISIBLE : VISIBLE);
			}
		}

		@Override
		public void onAnimationCancel(Animator animation) {}
		@Override
		public void onAnimationRepeat(Animator animation) {}
	};
	
	//测量高度
	private int measureHeight(View view) {
		view.measure(0, 0);
		return view.getMeasuredHeight();
	}

	public void setSubItemHeight(int height) {
		subItemHeight = height;
	}
}
