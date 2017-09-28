package com.advanpro.fwtools.module.more;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.MediaStore.Audio.Media;
import android.support.v4.view.ViewPager;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.advanpro.fwtools.Constant;
import com.advanpro.fwtools.R;
import com.advanpro.fwtools.common.base.BasePager;
import com.advanpro.fwtools.common.base.BasePagerAdapter;
import com.advanpro.fwtools.common.view.MyViewPager;
import com.advanpro.fwtools.common.view.TextTabContentView;
import com.advanpro.fwtools.common.view.ViewPagerIndicator;
import com.advanpro.fwtools.db.Dao;
import com.advanpro.fwtools.db.Song;
import com.advanpro.fwtools.entity.CheckableItem;
import com.advanpro.fwtools.entity.SimpleObserver;
import com.advanpro.fwtools.module.BaseActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

/**
 * Created by zengfs on 2016/2/1.
 * 伴跑音乐管理界面
 */
public class MusicManagerActivity extends BaseActivity implements View.OnClickListener {
	private static final int REQUEST_ADD_TO_SONGLIST = 100;
	private List<BasePager> pagers;
	private MyViewPager viewPager;
	private ViewPagerIndicator pagerIndicator;
	private ImageButton btnBack;
	private MySonglistPager songlistPager;
	private SongsPager songsPager;
	private RelativeLayout rlTitle;
	private RelativeLayout rlAction;
	private TextView tvStart;
	public TextView tvEnd;
	public TextView tvTitle;
	public boolean isSelectAll;//当前是是否是全选中状态
	private boolean isActionLayoutShowing;//选择操作栏是否正在显示
	public List<Song> selectedSongs;
	private SimpleObserver simpleObserver;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_music_manager);
		assignViews();
		initViews();
	}

	protected void assignViews() {
		viewPager = (MyViewPager) findViewById(R.id.view_pager);
		btnBack = (ImageButton) findViewById(R.id.btn_back);
		pagerIndicator = (ViewPagerIndicator) findViewById(R.id.view_pager_indicator);
		rlTitle = (RelativeLayout) findViewById(R.id.rl_title);
		rlAction = (RelativeLayout) findViewById(R.id.rl_select);
		tvStart = (TextView) findViewById(R.id.tv_start);
		tvEnd = (TextView) findViewById(R.id.tv_end);
		tvTitle = (TextView) findViewById(R.id.tv_title);
	}

	protected void initViews() {	
		pagers = new ArrayList<>();
		songlistPager = new MySonglistPager(this);
		songlistPager.setData(Dao.INSTANCE.querySonglists());
		pagers.add(songlistPager);
		songsPager = new SongsPager(this);
		songsPager.setData(getSongs());
		pagers.add(songsPager);
		simpleObserver = new SimpleObserver(ObservableMgr.getSonglistObservable()) {
			@Override
			public void update(Observable observable, Object data) {
				songlistPager.setData(Dao.INSTANCE.querySonglists());
			}
		};
		viewPager.setAdapter(new BasePagerAdapter(pagers));		
		viewPager.addOnPageChangeListener(pageChangeListener);
		pagerIndicator.setIndicatorEnabled(false);
		pagerIndicator.setDividerEnabled(false);
		pagerIndicator.setTabContentViews(createTextTabViews());
		pagerIndicator.setViewPager(viewPager);	
		btnBack.setOnClickListener(this);
		tvEnd.setOnClickListener(this);
		tvStart.setOnClickListener(this);
	}

	private ViewPager.SimpleOnPageChangeListener pageChangeListener = new ViewPager.SimpleOnPageChangeListener() {
		@Override
		public void onPageSelected(int position) {
			switch(position) {
			    case 0:
					tvStart.setText(R.string.complete);
					tvStart.setBackgroundColor(Color.TRANSPARENT);
					tvEnd.setText(R.string.clear);
					tvEnd.setBackgroundColor(Color.TRANSPARENT);
					tvTitle.setText(R.string.manager);
					tvTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
					break;
				case 1:
					tvStart.setText(R.string.cancel);
					tvStart.setBackgroundResource(R.drawable.cancel);
					tvEnd.setText(R.string.select_all);
					tvEnd.setBackgroundResource(R.drawable.select_all);
					tvTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
					break;
			}
		}
	};
	
	private TextTabContentView[] createTextTabViews() {
		int[] resIds = {R.string.my_song_list, R.string.all_songs};
		TextTabContentView[] textTabContentViews = new TextTabContentView[resIds.length];
		for (int i = 0; i < resIds.length; i++) {
			textTabContentViews[i] = new TextTabContentView(this);
			textTabContentViews[i].setText(resIds[i]);
		}
		return textTabContentViews;
	}

	//从媒体库中获取音乐文件信息
	private List<CheckableItem<Song>> getSongs() {
		List<CheckableItem<Song>> checkableItems = new ArrayList<>();
		int minSize = 1024 * 500;//过滤500K以下文件
		Cursor cursor = getContentResolver().query(Media.EXTERNAL_CONTENT_URI, null, 
				Media.IS_MUSIC + " IS NOT ? AND " + Media.SIZE + ">= ?",
				new String[]{"0", minSize + ""}, Media.DEFAULT_SORT_ORDER);
		if (cursor != null) {
			while (cursor.moveToNext()) {
				String tilte = cursor.getString(cursor.getColumnIndexOrThrow(Media.TITLE));
				String path = cursor.getString(cursor.getColumnIndexOrThrow(Media.DATA));
				String displayName = cursor.getString(cursor.getColumnIndexOrThrow(Media.DISPLAY_NAME));
				int duration = cursor.getInt(cursor.getColumnIndexOrThrow(Media.DURATION));
				long size = cursor.getLong(cursor.getColumnIndexOrThrow(Media.SIZE));
				tilte = tilte == null ? getString(R.string.unkown) : tilte;
				checkableItems.add(new CheckableItem<>(new Song(null, tilte, path, displayName, duration, size, 0), false));
			}
			cursor.close();
		}		
		return checkableItems;
	}
	
	@Override
	public void onClick(View v) {
		switch(v.getId()) {
		    case R.id.btn_back:		
				finish();
				break;
			case R.id.tv_end:
				if (pagers.get(viewPager.getCurrentItem()).equals(songlistPager)) {
					songlistPager.clear();
				} else if (pagers.get(viewPager.getCurrentItem()).equals(songsPager)) {
					songsPager.selectAll(isSelectAll = !isSelectAll);
				}				
				break;
			case R.id.tv_start:
				completeAction();
				break;
		}
	}

	/*
	 * 完成操作，还原控件状态
	 */
	private void completeAction() {
		switch(viewPager.getCurrentItem()) {
		    case 0:
				songlistPager.completeManager();
				break;
			case 1:
				songsPager.selectAll(false);
				break;
		}

		hideActionLayout();
	}

	/**
	 * 显示操作栏
 	 */
	public void showActionLayout() {		
		if (!isActionLayoutShowing) {
		    isActionLayoutShowing = true;
			viewPager.setTouchEnabled(false);
			rlAction.setVisibility(View.VISIBLE);
			rlTitle.setVisibility(View.GONE);
		}		
	}

	/**
	 * 隐藏操作栏
	 */
	public void hideActionLayout() {
		if (isActionLayoutShowing) {
			isActionLayoutShowing = false;
			viewPager.setTouchEnabled(true);
			rlAction.setVisibility(View.GONE);
			rlTitle.setVisibility(View.VISIBLE);
		}				
	}
	
	/**
	 * 添加到播放列表
	 */
	public void addToPlaylist(List<Song> selectedSongs) {
		if (selectedSongs != null && selectedSongs.size() > 0) {
		    this.selectedSongs = selectedSongs;
			Intent intent = new Intent(this, AddToSonglistActivity.class);
			intent.putExtra(Constant.EXTRA_FROM_CLASS_NAME, MusicManagerActivity.class.getName());
			startActivityForResult(intent, REQUEST_ADD_TO_SONGLIST);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == REQUEST_ADD_TO_SONGLIST && resultCode == RESULT_OK) viewPager.setCurrentItem(0, false);
	}

	@Override
	public void onBackPressed() {
		if (isActionLayoutShowing) {
			completeAction();
		    return;
		}
		super.onBackPressed();
	}

	@Override
	protected void onDestroy() {
		ObservableMgr.getSonglistObservable().deleteObserver(simpleObserver);
		super.onDestroy();
	}
}
