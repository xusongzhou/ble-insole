package com.advanpro.fwtools.common.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
/**
 * 可循环滚动的选择器
 */
public class CycleWheelView extends ListView {

    public static final String TAG = CycleWheelView.class.getSimpleName();
    private static final int COLOR_DIVIDER_DEFALUT = Color.parseColor("#747474");
    private static final int HEIGHT_DIVIDER_DEFAULT = 2;
    private static final int COLOR_SOLID_DEFAULT = Color.parseColor("#3e4043");
    private static final int COLOR_SOLID_SELET_DEFAULT = Color.parseColor("#323335");
    private static final int WHEEL_SIZE_DEFAULT = 3;

    private Handler mHandler;

    private CycleWheelViewAdapter mAdapter;

    /**
     * Labels
     */
    private List<String> mLabels;

    /**
     * Color Of Selected Label
     */
    private int mLabelSelectColor = Color.WHITE;

    /**
     * Color Of Unselected Label
     */
    private int mLabelColor = Color.GRAY;

    /**
     * Gradual Alph
     */
    private float mAlphaGradual = 0.7f;

    /**
     * Color Of Divider
     */
    private int dividerColor = COLOR_DIVIDER_DEFALUT;

    /**
     * Height Of Divider
     */
    private int dividerHeight = HEIGHT_DIVIDER_DEFAULT;

    /**
     * Color of Selected Solid
     */
    private int seletedSolidColor = COLOR_SOLID_SELET_DEFAULT;

    /**
     * Color of Unselected Solid
     */
    private int solidColor = COLOR_SOLID_DEFAULT;

    /**
     * Size Of Wheel , it should be odd number like 3 or greater
     */
    private int mWheelSize = WHEEL_SIZE_DEFAULT;

    /**
     * res Id of Wheel Item Layout
     */
    private int mItemLayoutId = -1;

    /**
     * res Id of Label TextView
     */
    private int mItemLabelTvId = android.R.id.text1;

    /**
     * Height of Wheel Item
     */
    private int mItemHeight;
    private boolean cylceEnable;
    private int mCurrentPositon;
	private float itemSpace = 5;
	private float textSize = 16;

    private WheelItemSelectedListener mItemSelectedListener;

    public CycleWheelView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public CycleWheelView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CycleWheelView(Context context) {
        super(context);
    }

    private void init() {
        mHandler = new Handler();
        mAdapter = new CycleWheelViewAdapter();
        setVerticalScrollBarEnabled(false);
        setScrollingCacheEnabled(false);
        setCacheColorHint(Color.TRANSPARENT);
        setFadingEdgeLength(0);
        setOverScrollMode(OVER_SCROLL_NEVER);
        setDividerHeight(0);
        setAdapter(mAdapter);
        setOnScrollListener(new OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (scrollState == SCROLL_STATE_IDLE) {
                    View itemView = getChildAt(0);
                    if (itemView != null) {
                        float deltaY = itemView.getY();
                        if (deltaY == 0) {
                            return;
                        }
                        if (Math.abs(deltaY) < mItemHeight / 2) {
                            smoothScrollBy(getDistance(deltaY), 50);
                        } else {
                            smoothScrollBy(getDistance(mItemHeight + deltaY), 50);
                        }
                    }
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
                                 int totalItemCount) {
                refreshItems();
            }
        });
    }

    private int getDistance(float scrollDistance) {
        if (Math.abs(scrollDistance) <= 2) {
            return (int) scrollDistance;
        } else if (Math.abs(scrollDistance) < 12) {
            return scrollDistance > 0 ? 2 : -2;
        } else {
            return (int) (scrollDistance / 6);
        }
    }

    private void refreshItems() {
        int offset = mWheelSize / 2;
        int firstPosition = getFirstVisiblePosition();
        int position;
        if (getChildAt(0) == null) {
            return;
        }
        if (Math.abs(getChildAt(0).getY()) <= mItemHeight / 2) {
            position = firstPosition + offset;
        } else {
            position = firstPosition + offset + 1;
        }
        if (position == mCurrentPositon) {
            return;
        }
        mCurrentPositon = position;
        if (mItemSelectedListener != null) {
            mItemSelectedListener.onItemSelected(getSelection(), getSelectLabel());
        }
        resetItems(firstPosition, position, offset);
    }

    private void resetItems(int firstPosition, int position, int offset){
        for (int i = position - offset - 1; i < position + offset + 1; i++) {
            View itemView = getChildAt(i - firstPosition);
            if (itemView == null) {
                continue;
            }
            TextView labelTv = (TextView) itemView.findViewById(mItemLabelTvId);
            if (position == i) {
                labelTv.setTextColor(mLabelSelectColor);
                itemView.setAlpha(1f);
            } else {
                labelTv.setTextColor(mLabelColor);
                int delta = Math.abs(i - position);
                double alpha = Math.pow(mAlphaGradual, delta);
                itemView.setAlpha((float) alpha);
            }
        }
    }

    /**
     * 设置滚轮的刻度列表
     */
    public void setLabels(List<String> labels) {
        mLabels = labels;
        mAdapter.setData(mLabels);
        mAdapter.notifyDataSetChanged();
        initView();
    }

	/**
	 * 设置条目间距
	 * @param space 间距，单位dp
	 */
	public void setItemSpace(float space) {
		itemSpace = space;
		initView();
	}

	/**
	 * 设置文本字体大小
	 * @param size 字体大小，单位sp
	 */
	public void setTextSize(float size) {
		textSize = size;
		initView();
	}
	
    /**
     * 设置滚轮滚动监听
     */
    public void setOnWheelItemSelectedListener(WheelItemSelectedListener mItemSelectedListener) {
        this.mItemSelectedListener = mItemSelectedListener;
    }

    /**
     * 获取滚轮的刻度列表
     */
    public List<String> getLabels() {
        return mLabels;
    }

    /**
     * 设置滚轮是否为循环滚动
     * @param enable true-循环 false-单程
     */
    public void setCycleEnable(boolean enable) {
        if (cylceEnable != enable) {
            cylceEnable = enable;
            mAdapter.notifyDataSetChanged();
            setSelection(getSelection());
        }
    }

    /*
     * 滚动到指定位置
     */
    @Override
    public void setSelection(final int position) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                CycleWheelView.super.setSelection(getPosition(position));
            }
        });
    }
	
    private int getPosition(int positon) {
        if (mLabels == null || mLabels.size() == 0) {
            return 0;
        }
        if (cylceEnable) {
            int d = Integer.MAX_VALUE / 2 / mLabels.size();
            return positon + d * mLabels.size();
        }
        return positon;
    }

    /**
     * 获取当前滚轮位置
     */
    public int getSelection() {
        if (mCurrentPositon == 0) {
            mCurrentPositon = mWheelSize / 2;
        }		
        return mLabels.size() == 0 ? 0 : (mCurrentPositon - mWheelSize / 2) % mLabels.size();
    }

    /**
     * 获取当前滚轮位置的刻度
     */
    public String getSelectLabel() {
        int position = getSelection();
        position = position < 0 ? 0 : position;
        try {
            return mLabels.get(position);
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * 如果需要自定义滚轮每个Item，调用此方法设置自定义Item布局，自定义布局中需要一个TextView来显示滚轮刻度
     * @param itemResId 布局文件Id
     * @param labelTvId 刻度TextView的资源Id
     */
    public void setWheelItemLayout(int itemResId, int labelTvId) {
        mItemLayoutId = itemResId;
        mItemLabelTvId = labelTvId;
        mAdapter = new CycleWheelViewAdapter();
        mAdapter.setData(mLabels);
        setAdapter(mAdapter);
        initView();
    }

    /**
     * 设置未选中刻度文字颜色
     */
    public void setLabelColor(int labelColor) {
        this.mLabelColor = labelColor;
        resetItems(getFirstVisiblePosition(), mCurrentPositon, mWheelSize/2);
    }

    /**
     * 设置选中刻度文字颜色
     */
    public void setLabelSelectColor(int labelSelectColor) {
        this.mLabelSelectColor = labelSelectColor;
        resetItems(getFirstVisiblePosition(), mCurrentPositon, mWheelSize/2);
    }

    /**
     * 设置滚轮刻度透明渐变值
     */
    public void setAlphaGradual(float alphaGradual) {
        this.mAlphaGradual = alphaGradual;
        resetItems(getFirstVisiblePosition(), mCurrentPositon, mWheelSize/2);
    }

    /**
     * 设置滚轮可显示的刻度数量，必须为奇数，且大于等于3
     * @throws CycleWheelViewException 滚轮数量错误
     */
    public void setWheelSize(int wheelSize) throws CycleWheelViewException  {
        if (wheelSize < 3 || wheelSize % 2 != 1) {
            throw new CycleWheelViewException("Wheel Size Error , Must Be 3,5,7,9...");
        } else {
            mWheelSize = wheelSize;
            initView();
        }
    }

    /**
     * 设置块的颜色
     * @param unselectedSolidColor 未选中的块的颜色
     * @param selectedSolidColor 选中的块的颜色
     */
    public void setSolid(int unselectedSolidColor, int selectedSolidColor){
        this.solidColor = unselectedSolidColor;
        this.seletedSolidColor = selectedSolidColor;
        initView();
    }

    /**
     * 设置分割线样式
     * @param dividerColor  分割线颜色
     * @param dividerHeight 分割线高度(px)
     */
    public void setDivider(int dividerColor, int dividerHeight){
        this.dividerColor = dividerColor;
        this.dividerHeight = dividerHeight;
    }

    @SuppressWarnings("deprecation")
    private void initView() {
        mItemHeight = measureHeight();
        ViewGroup.LayoutParams lp = getLayoutParams();
        lp.height = mItemHeight * mWheelSize;
        mAdapter.setData(mLabels);
        mAdapter.notifyDataSetChanged();
        Drawable backgroud = new Drawable() {
            @Override
            public void draw(Canvas canvas) {
                int viewWidth = getWidth();
                Paint dividerPaint = new Paint();
                dividerPaint.setColor(dividerColor);
                dividerPaint.setStrokeWidth(dividerHeight);
                Paint seletedSolidPaint = new Paint();
                seletedSolidPaint.setColor(seletedSolidColor);
                Paint solidPaint = new Paint();
                solidPaint.setColor(solidColor);
                canvas.drawRect(0, 0, viewWidth, mItemHeight * (mWheelSize / 2), solidPaint);
                canvas.drawRect(0, mItemHeight * (mWheelSize / 2 + 1), viewWidth, mItemHeight
                        * (mWheelSize), solidPaint);
                canvas.drawRect(0, mItemHeight * (mWheelSize / 2), viewWidth, mItemHeight
                        * (mWheelSize / 2 + 1), seletedSolidPaint);
                canvas.drawLine(0, mItemHeight * (mWheelSize / 2), viewWidth, mItemHeight
                        * (mWheelSize / 2), dividerPaint);
                canvas.drawLine(0, mItemHeight * (mWheelSize / 2 + 1), viewWidth, mItemHeight
                        * (mWheelSize / 2 + 1), dividerPaint);
            }

            @Override
            public void setAlpha(int alpha) {
            }

            @Override
            public void setColorFilter(ColorFilter cf) {
            }

            @Override
            public int getOpacity() {
                return 0;
            }
        };
        setBackgroundDrawable(backgroud);
    }

    private int measureHeight() {
        View itemView = getConvertView(null);
        int w = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        int h = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        itemView.measure(w, h);
		// int width = view.getMeasuredWidth();
        return itemView.getMeasuredHeight();
    }

    public interface WheelItemSelectedListener {
        void onItemSelected(int position, String label);
    }

    public class CycleWheelViewException extends Exception {
        private static final long serialVersionUID = 1L;

        public CycleWheelViewException(String detailMessage) {
            super(detailMessage);
        }
    }

    public class CycleWheelViewAdapter extends BaseAdapter {

        private List<String> mData = new ArrayList<String>();

        public void setData(List<String> mWheelLabels) {
            mData.clear();
            mData.addAll(mWheelLabels);
        }

        @Override
        public int getCount() {
            if (cylceEnable) {
                return Integer.MAX_VALUE;
            }
            return mData.size() + mWheelSize - 1;
        }

        @Override
        public Object getItem(int position) {
            return "";
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public boolean isEnabled(int position) {
            return false;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;
            if (convertView == null) convertView = getConvertView(holder = new ViewHolder());
			else holder = (ViewHolder) convertView.getTag();			 
            
            if (position < mWheelSize / 2
                    || (!cylceEnable && position >= mData.size() + mWheelSize / 2)) {
                holder.tv.setText("");
                convertView.setVisibility(View.INVISIBLE);
            } else {
				holder.tv.setText(mData.get((position - mWheelSize / 2) % mData.size()));
                convertView.setVisibility(View.VISIBLE);
            }
            return convertView;
        }
    }
	
	private static class ViewHolder {
		TextView tv;
	}
	
	private View getConvertView(ViewHolder holder) {
		View view;
		TextView tv;
		if (mItemLayoutId == -1) {
			RelativeLayout layout = new RelativeLayout(getContext());
			layout.setLayoutParams(new AbsListView.LayoutParams(-1, -2));			
			layout.setBackgroundColor(Color.TRANSPARENT);
			tv = new TextView(getContext());
			RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(-2, -2);
			params.addRule(RelativeLayout.CENTER_IN_PARENT);
			tv.setLayoutParams(params);
			tv.setId(mItemLabelTvId);			
			tv.setSingleLine();
			layout.addView(tv);			
			view = layout;
		} else {
			view = LayoutInflater.from(getContext()).inflate(mItemLayoutId, null);
			tv = (TextView) view.findViewById(mItemLabelTvId);			
		}
		
		view.setPadding(0, dip2px(itemSpace), 0, dip2px(itemSpace));
		tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);
		
		if (holder != null) {
			view.setTag(holder);
			holder.tv = tv;
		}
		return view;
	}

	public int dip2px(float dpValue) {
		float scale = getResources().getDisplayMetrics().density;
		return (int) (dpValue * scale + 0.5f);
	}
}
