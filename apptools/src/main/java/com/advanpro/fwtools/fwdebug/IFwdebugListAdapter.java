package com.advanpro.fwtools.fwdebug;

import android.widget.BaseAdapter;

import java.util.List;

/**
 * Created by AdvanPro on 2016/6/28.
 */
public abstract class IFwdebugListAdapter extends BaseAdapter {
    abstract void updateData(List data);
}
