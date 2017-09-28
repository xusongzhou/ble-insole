package com.advanpro.fwtools.module.more;

import android.content.Context;
import android.view.View;
import android.widget.*;
import com.advanpro.fwtools.R;
import com.advanpro.fwtools.common.base.BaseHolder;
import com.advanpro.fwtools.common.base.BaseListAdapter;
import com.advanpro.fwtools.common.base.BasePager;
import com.advanpro.fwtools.common.view.CheckableLayout;
import com.advanpro.fwtools.db.Song;
import com.advanpro.fwtools.entity.CheckableItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zengfs on 2016/2/1.
 * 手机本地音乐列表
 */
public class SongsPager extends BasePager {	
	private ListView lv;
	private List<CheckableItem<Song>> checkableItems;
	private SongListAdapter listAdapter;
	private LinearLayout llEmpty;
	private MusicManagerActivity activity;
	private TextView tvAddTo;
	private LinearLayout llAddTo;
	
	public SongsPager(Context context) {
		super(context);	
		activity = (MusicManagerActivity) context;
	}
	
	@Override
	protected View getRootView() {
		return View.inflate(context, R.layout.pager_songs, null);
	}

	@Override
	protected void assignViews() {
		lv = (ListView) rootView.findViewById(R.id.lv);
		llEmpty = (LinearLayout) rootView.findViewById(R.id.ll_empty);
		llAddTo = (LinearLayout) rootView.findViewById(R.id.ll_add_to);
		tvAddTo = (TextView) rootView.findViewById(R.id.tv_add_to);
	}

	@Override
	protected void initViews() {
		checkableItems = new ArrayList<>();
		listAdapter = new SongListAdapter(checkableItems);
		lv.setAdapter(listAdapter);
		tvAddTo.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {				
				List<Song> songs = new ArrayList<>();
				for (CheckableItem<Song> item : checkableItems) {
					if (item.isChecked) {
						songs.add(item.obj);
						item.isChecked = false;//重置选中状态
					}
				}
				activity.addToPlaylist(songs);
				listAdapter.notifyDataSetChanged();//更新ListView
				activity.hideActionLayout();
				llAddTo.setVisibility(View.GONE);
			}
		});
		updateView();
	}

	public void setData(List<CheckableItem<Song>> checkableItems) {
		if (checkableItems == null) return;
		this.checkableItems.clear();
		this.checkableItems.addAll(checkableItems);		
		updateView();
	}

	public void updateView() {
		if (checkableItems.size() > 0) llEmpty.setVisibility(View.GONE);
		else llEmpty.setVisibility(View.VISIBLE);
		listAdapter.notifyDataSetChanged();
	}

	public void selectAll(boolean isSelectAll) {
		if (isSelectAll) llAddTo.setVisibility(View.VISIBLE);
		else llAddTo.setVisibility(View.GONE);
		for (CheckableItem<Song> item : checkableItems) {
			item.isChecked = isSelectAll;
		}
		updateView();
	}
	
	private class SongListAdapter extends BaseListAdapter<CheckableItem<Song>> {

		public SongListAdapter(List<CheckableItem<Song>> data) {
			super(data);
		}

		@Override
		protected BaseHolder<CheckableItem<Song>> getHolder() {
			return new SongHolder();
		}
	}
	
	private class SongHolder extends BaseHolder<CheckableItem<Song>> {

		private TextView tvTitle;
		private ImageView ivDelete;
		private CheckableLayout chk;
		private ImageView ivBox;

		@Override
		protected void setData(final CheckableItem<Song> data, int position) {
			tvTitle.setText(data.obj.getTitle());			
			chk.setOnCheckedChangeListener(new CheckableLayout.OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CheckableLayout layout, boolean isChecked) {	
					ivBox.setBackgroundResource(isChecked ? R.drawable.chk_checked : R.drawable.chk_normal);
					if(isChecked && llAddTo.getVisibility() != View.VISIBLE) llAddTo.setVisibility(View.VISIBLE);
					if (isChecked) activity.showActionLayout();					
					data.isChecked = isChecked;
					int checkedNum = 0;
					for (CheckableItem<Song> item : checkableItems) {
						if (item.isChecked) checkedNum++;
					}
					activity.tvTitle.setText(context.getString(R.string.selected_items).replace("?", checkedNum + ""));
					if (checkedNum == 0) {
						activity.tvEnd.setText("全选");//显示全选的时候说明不是全选中状态
						activity.isSelectAll = false;
						llAddTo.setVisibility(View.GONE);
					} else if (checkedNum == checkableItems.size()) {
						activity.tvEnd.setText("全不选");
						activity.isSelectAll = true;
					}					
				}
			});
			chk.setChecked(data.isChecked);
		}

		@Override
		protected View createConvertView() {
			View convertView = View.inflate(context, R.layout.item_song, null);
			tvTitle = (TextView) convertView.findViewById(R.id.tv_title);
			ivDelete = (ImageView) convertView.findViewById(R.id.iv_delete);
			chk = (CheckableLayout) convertView.findViewById(R.id.layout_chk);
			ivBox = (ImageView) convertView.findViewById(R.id.iv_box);
			ivDelete.setVisibility(View.GONE);
			chk.setVisibility(View.VISIBLE);
			return convertView;
		}
	}
}
