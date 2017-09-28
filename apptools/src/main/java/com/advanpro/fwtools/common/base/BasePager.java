package com.advanpro.fwtools.common.base;

import android.content.Context;
import android.view.View;

/**
 * Created by zengfs on 2016/1/15.
 */
public abstract class BasePager{
	public Context context;
    public View rootView;

    public BasePager(Context context) {
        this.context = context;
        rootView = getRootView();
        assignViews();
        beforeInitViews();
        initViews();
    }

    protected void beforeInitViews() {System.out.println("beforeInitViews");}

    protected void assignViews() {System.out.println("assignViews");}

    protected void initViews() {System.out.println("initViews");}

    protected abstract View getRootView();
}
