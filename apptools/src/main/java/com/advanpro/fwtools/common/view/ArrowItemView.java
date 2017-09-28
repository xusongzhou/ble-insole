package com.advanpro.fwtools.common.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.advanpro.fwtools.R;

/**
 * Created by zengfs on 2016/2/24.
 * 右边带箭头的条目
 */
public class ArrowItemView extends FrameLayout implements CompoundButton.OnCheckedChangeListener {
	
	private TextView tvTitle;
    private TextView tvHint;
	private ImageView ivArrow;
    private View topDivider, bottomDivider;
    private SwitchButton switchButton;
    private boolean switchChecked;
    private CompoundButton.OnCheckedChangeListener checkedChangeListener;

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switchChecked = isChecked;
        if (checkedChangeListener != null) checkedChangeListener.onCheckedChanged(buttonView, isChecked);
    }

    /** 条目风格：右边不带任何内容、带前头、带SwitchButton */
    public enum Style {
        ARROW(0), TOGGLE(1), NOTHING(2);

        public int value;
        Style(int value) {
            this.value = value;
        }
    }
	
	public ArrowItemView(Context context) {
		this(context, null);
	}

	public ArrowItemView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public ArrowItemView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ArrowItemView);
		String title = typedArray.getString(R.styleable.ArrowItemView_itemTitle);
		int titleColor = typedArray.getColor(R.styleable.ArrowItemView_itemTitleColor, 
				context.getResources().getColor(R.color.content_text));
		int arrowColor = typedArray.getColor(R.styleable.ArrowItemView_arrowColor, Color.WHITE);
        int style = typedArray.getInt(R.styleable.ArrowItemView_style, Style.ARROW.value);
        boolean topDividerVisible = typedArray.getBoolean(R.styleable.ArrowItemView_topDividerVisible, false);
        boolean bottomDividerVisible = typedArray.getBoolean(R.styleable.ArrowItemView_bottomDividerVisible, false);
        String hint = typedArray.getString(R.styleable.ArrowItemView_hint);
        boolean hintVisible = typedArray.getBoolean(R.styleable.ArrowItemView_hintVisible, false);
        boolean checked = typedArray.getBoolean(R.styleable.ArrowItemView_switchChecked, false);
        typedArray.recycle();

		View view = View.inflate(context, R.layout.item_arrow_toggle, this);
		tvTitle = (TextView) view.findViewById(R.id.tv_title);
        tvHint = (TextView) view.findViewById(R.id.tv_hint);
		ivArrow = (ImageView) view.findViewById(R.id.iv_arrow);
        topDivider = view.findViewById(R.id.top_divider);
        bottomDivider = view.findViewById(R.id.bottom_divider);
        switchButton = (SwitchButton) view.findViewById(R.id.switch_button);
        switchButton.setOnCheckedChangeListener(this);
        setTitle(title == null ? "" : title);
		setTitleColor(titleColor);
		setArrowColor(arrowColor);
        setHint(hint);
        setHintVisible(hintVisible);
        setStyle(style);
        setTopDividerVisible(topDividerVisible);
        setBottomDividerVisible(bottomDividerVisible);
        setSwitchCheckedImmediately(checked);
	}
	
	public void setTitle(String text) {
		tvTitle.setText(text);
	}
	
	public void setTitle(int resid) {
		tvTitle.setText(resid);
	}
	
    public void setHint(String text) {
        tvHint.setText(text);
    }

    public void setHint(int resid) {
        tvHint.setText(resid);
    }
    
    public void setHintVisible(boolean visible) {
        tvHint.setVisibility(visible ? VISIBLE : INVISIBLE);
    }
    
	public void setTitleColor(int color) {
		tvTitle.setTextColor(color);
	}
	
	public void setArrowColor(int color) {
		ivArrow.setColorFilter(color);
	}
    
    public void setTopDividerVisible(boolean visible) {
        topDivider.setVisibility(visible ? VISIBLE : INVISIBLE);
    }
    
    public void setBottomDividerVisible(boolean visible) {
        bottomDivider.setVisibility(visible ? VISIBLE : INVISIBLE);
    }
    
    public void setStyle(Style style) {
        setStyle(style.value);
    }
    
    private void setStyle(int vale) {
        ivArrow.setVisibility(vale == Style.ARROW.value ? VISIBLE : INVISIBLE);
        switchButton.setVisibility(vale == Style.TOGGLE.value ? VISIBLE : INVISIBLE);
    }
    
    public void setSwitchTintColor(int color) {
        switchButton.setTintColor(color);
    }

    /**
     * 设置Switch开关状态，不使用开关过渡动画
     */
    public void setSwitchCheckedImmediately(boolean checked) {        
        switchButton.setCheckedImmediately(checked);
    }

    /**
     * 设置Switch开关状态，使用开关过渡动画
     */
    public void setSwitchChecked(boolean checked) {
        switchButton.setChecked(checked);
    }
    
    public void setOnSwitchCheckedChangeListener(CompoundButton.OnCheckedChangeListener listener) {
        this.checkedChangeListener = listener;
    }
    
    public boolean getSwitchChecked() {
        return switchChecked;
    }
}
