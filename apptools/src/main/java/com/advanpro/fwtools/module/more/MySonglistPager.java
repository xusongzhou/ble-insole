package com.advanpro.fwtools.module.more;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.*;
import com.advanpro.fwtools.Constant;
import com.advanpro.fwtools.R;
import com.advanpro.fwtools.common.base.BaseHolder;
import com.advanpro.fwtools.common.base.BaseListAdapter;
import com.advanpro.fwtools.common.base.BasePager;
import com.advanpro.fwtools.db.Dao;
import com.advanpro.fwtools.db.Songlist;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zengfs on 2016/2/1.
 * 歌单列表
 */
public class MySonglistPager extends BasePager implements View.OnClickListener, AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {
	private TextView tvEmpty;
	private ListView lv;
	private TextView tvNewSonglist;
	private List<Songlist> songlists;
	private SonglistAdapter listAdapter;
	private MusicManagerActivity activity;
	private LinearLayout llBottom;//底部按键
	private boolean isManagering;

	public MySonglistPager(Context context) {
		super(context);
		activity = (MusicManagerActivity) context;
	}

	@Override
	protected View getRootView() {
		return View.inflate(context, R.layout.pager_my_songlist, null);
	}

	@Override
	protected void assignViews() {
		tvEmpty = (TextView) rootView.findViewById(R.id.tv_empty);
		lv = (ListView) rootView.findViewById(R.id.lv);
		tvNewSonglist = (TextView) rootView.findViewById(R.id.tv_new_songlist);
		llBottom = (LinearLayout) rootView.findViewById(R.id.ll_bottom);
	}

	@Override
	protected void initViews() {
		tvNewSonglist.setOnClickListener(this);
		songlists = new ArrayList<>();
		listAdapter = new SonglistAdapter(songlists);
		lv.setAdapter(listAdapter);
		lv.setOnItemClickListener(this);
		lv.setOnItemLongClickListener(this);
		updateView();
	}

	public void setData(List<Songlist> songlistModels) {
		if (songlistModels == null) return;
		this.songlists.clear();
		this.songlists.addAll(songlistModels);
		updateView();
	}
	
	public void updateView() {
		if (songlists.size() > 0) tvEmpty.setVisibility(View.GONE);
		else tvEmpty.setVisibility(View.VISIBLE);
		listAdapter.notifyDataSetChanged();
	}
	
	@Override
	public void onClick(View v) {
		switch(v.getId()) {
		    case R.id.tv_new_songlist:
				context.startActivity(new Intent(context, NewSonglistActivity.class));
				break;
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		Intent intent = new Intent(context, SonglistActivity.class);
		intent.putExtra(Constant.EXTRA_SONGLIST_ID, songlists.get(position).getId());
		context.startActivity(intent);		
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
		activity.showActionLayout();
		startManager();
		return true;
	}

	/**
	 * 清空歌单
	 */
	public void clear() {
		songlists.clear();
		updateView();
		Dao.INSTANCE.deleteSonglists();
	}

	public void completeManager() {
		llBottom.setVisibility(View.VISIBLE);
		isManagering = false;
		listAdapter.notifyDataSetChanged();
	}

	public void startManager() {
		llBottom.setVisibility(View.GONE);
		isManagering = true;
		listAdapter.notifyDataSetChanged();
	}
	
	private class SonglistAdapter extends BaseListAdapter<Songlist> {

		public SonglistAdapter(List<Songlist> data) {
			super(data);
		}

		@Override
		protected BaseHolder<Songlist> getHolder() {
			return new BaseHolder<Songlist>() {

				private TextView tvName;
				private TextView tvNum;
				private TextView tvSelected;
				private ImageView ivIcon;
				private ImageView ivDelete;
				private ImageView ivArrow;
				
				@Override
				protected void setData(Songlist data, final int position) {
					tvName.setText(data.getName());
					tvNum.setText(context.getString(R.string.songs_number).replace("?", Dao.INSTANCE.querySongs(data.getId()).size() + ""));
					boolean isChecked = Dao.INSTANCE.queryRunSonglistId() == data.getId();
					tvSelected.setVisibility(isChecked ? View.VISIBLE : View.INVISIBLE);
					ivIcon.setVisibility(isManagering ? View.GONE : View.VISIBLE);
					ivDelete.setVisibility(isManagering ? View.VISIBLE : View.GONE);
					ivArrow.setVisibility(isManagering ? View.GONE : View.VISIBLE);
					ivIcon.setImageResource(isChecked ? R.drawable.selected_icon : R.drawable.gray_ring);
					ivIcon.setOnClickListener(new MyOnClickListener(position));
					ivDelete.setOnClickListener(new MyOnClickListener(position));
				}

				@Override
				protected View createConvertView() {
					View convertView = View.inflate(context, R.layout.item_songlist, null);
					tvName = (TextView) convertView.findViewById(R.id.tv_name);
					tvNum = (TextView) convertView.findViewById(R.id.tv_num);
					tvSelected = (TextView) convertView.findViewById(R.id.tv_selected);
					ivIcon = (ImageView) convertView.findViewById(R.id.iv_icon);
					ivDelete = (ImageView) convertView.findViewById(R.id.iv_delete);	
					ivArrow = (ImageView) convertView.findViewById(R.id.iv_arrow);
					return convertView;
				}
				
				class MyOnClickListener implements View.OnClickListener {
					private int position;
					
					public MyOnClickListener(int position) {
						this.position = position;
					}

					@Override
					public void onClick(View v) {
						switch(v.getId()) {
						    case R.id.iv_icon:
								Dao.INSTANCE.insertOrUpdateRunSonglist(getItem(position).getId());
								break;
							case R.id.iv_delete:
								Songlist songlist = getItem(position);
								getData().remove(songlist);
								Dao.INSTANCE.deleteSonglist(songlist.getId());
								notifyDataSetChanged();
								break;
						}
					}
				} 
			};
		}
	}
}
