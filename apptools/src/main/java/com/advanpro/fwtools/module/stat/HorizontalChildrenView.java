package com.advanpro.fwtools.module.stat;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.advanpro.fwtools.R;


/**
 * Created by DEV002 on 2016/3/28.
 */
public class HorizontalChildrenView extends LinearLayout {


    private LayoutInflater inflater;
    private int type;
    private TextView innerText, outText;

    public HorizontalChildrenView(Context context) {
        super(context);
    }

    public HorizontalChildrenView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.HorizontalChildrenView);
        type = array.getInt(R.styleable.HorizontalChildrenView_myviewType, 1);
        inflater = LayoutInflater.from(context);
        if (type == 1) {
            View v = inflater.inflate(R.layout.content, null);
            innerText = (TextView) v.findViewById(R.id.innerText);
            outText = (TextView) v.findViewById(R.id.outText);
//            switch (DeviceMgr.getBoundDeviceType()) {
//                case Constant.DEVICE_TYPE_BASIC:
//                    innerText.setVisibility(View.INVISIBLE);
//                    outText.setVisibility(View.INVISIBLE);
//                    break;
//                case Constant.DEVICE_TYPE_POPULARITY:
//                    innerText.setVisibility(View.VISIBLE);
//                    outText.setVisibility(View.VISIBLE);
//                    break;
//                case Constant.DEVICE_TYPE_ENHANCED:
//                    innerText.setVisibility(View.VISIBLE);
//                    outText.setVisibility(View.VISIBLE);
//                    break;
//                case -1:
//                    innerText.setVisibility(View.INVISIBLE);
//                    outText.setVisibility(View.INVISIBLE);
//                    break;
//            }
            addView(v);
        } else if (type == 2) {
            View v = inflater.inflate(R.layout.pose_content, null);
            v.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            addView(v);
        }


    }

    public HorizontalChildrenView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        inflater = LayoutInflater.from(context);
        View v = inflater.inflate(R.layout.content, null);
        addView(v);
    }
}
