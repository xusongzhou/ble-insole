package com.advanpro.fwtools.common.base;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.advanpro.fwtools.common.util.UiUtils;

/**
 * Created by zengfs on 2016/1/23.
 */
public abstract class BaseFragment<T> extends Fragment {
	protected View rootView;
	public T parentActivity;

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		parentActivity = (T) getActivity();
		if (rootView == null) {
		    rootView = getRootView(inflater, container);
			assignViews();
			initViews();
		}
		UiUtils.removeFromContainer(rootView);
		return rootView;
	}

	protected void initViews() {}

	protected void assignViews() {}

	protected abstract View getRootView(LayoutInflater inflater, ViewGroup container);
}
