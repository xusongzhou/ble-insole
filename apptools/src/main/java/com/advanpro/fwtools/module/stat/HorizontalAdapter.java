package com.advanpro.fwtools.module.stat;

import android.content.Context;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by DEV002 on 2016/3/25.
 */
public abstract class HorizontalAdapter {

    public Context context;

    public HorizontalAdapter(Context context) {
        this.context = context;
    }

    public List<String> array = new ArrayList<String>();

    public abstract View getView(int position);

    public int getCount() {
        return array.size();
    }

    public abstract void setData(List<String> str);

}
