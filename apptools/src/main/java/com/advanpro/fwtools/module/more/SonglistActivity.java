package com.advanpro.fwtools.module.more;

import android.app.Dialog;
import android.database.Cursor;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.*;
import com.advanpro.fwtools.Constant;
import com.advanpro.fwtools.R;
import com.advanpro.fwtools.common.base.BaseHolder;
import com.advanpro.fwtools.common.base.BaseListAdapter;
import com.advanpro.fwtools.common.manager.ToastMgr;
import com.advanpro.fwtools.common.util.UiUtils;
import com.advanpro.fwtools.common.view.CheckableLayout;
import com.advanpro.fwtools.common.view.TitleBar;
import com.advanpro.fwtools.db.Dao;
import com.advanpro.fwtools.db.Song;
import com.advanpro.fwtools.entity.CheckableItem;
import com.advanpro.fwtools.entity.SimpleObserver;
import com.advanpro.fwtools.module.BaseActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

/**
 * Created by zengfs on 2016/2/7.
 * 单个歌单内容
 */
public class SonglistActivity extends BaseActivity implements View.OnClickListener {	
	private List<CheckableItem<Song>> checkableItems;
	private TitleBar titleBar;
	private RelativeLayout rlAction;
	private TextView tvSelectCancel;
	private TextView tvSelectAll;
	private TextView tvSelectNum;
	private LinearLayout llEmpty;
	private ListView lv;
	private LinearLayout llDelete;
	private TextView tvDelete;
	private long songlistId;
	private boolean isActionLayoutShowing;
	private boolean isSelectAll;
	private SimpleObserver simpleObserver;
	//----全部歌曲对话框----
	private Dialog allSongsDialog;
	private LinearLayout llEmptyDialog;
	private List<CheckableItem<Song>> dialogCheckableItems;
	private int appHeight;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_songlist);
		init();
		assignViews();
		initViews();
	}

	private void init() {
		songlistId = getIntent().getLongExtra(Constant.EXTRA_SONGLIST_ID, -1);
		//歌单不存在直接返回
		if (!Dao.INSTANCE.isSonglistExist(songlistId)) {
            ToastMgr.showTextToast(this, 0, R.string.songlist_not_exist);
		    finish();
			return;
		}
		checkableItems = new ArrayList<>();
		simpleObserver = new SimpleObserver(ObservableMgr.getSonglistObservable()) {
			@Override
			public void update(Observable observable, Object data) {
				loadListData();
				updateView();
			}
		};
	}

	protected void assignViews() {
		titleBar = (TitleBar) findViewById(R.id.title_bar);
		rlAction = (RelativeLayout) findViewById(R.id.rl_select);
		tvSelectCancel = (TextView) findViewById(R.id.tv_select_cancel);
		tvSelectAll = (TextView) findViewById(R.id.tv_select_all);
		tvSelectNum = (TextView) findViewById(R.id.tv_select_num);
		llEmpty = (LinearLayout) findViewById(R.id.ll_empty);
		lv = (ListView) findViewById(R.id.lv);
		llDelete = (LinearLayout) findViewById(R.id.ll_delete);
		tvDelete = (TextView) findViewById(R.id.tv_delete);
	}

	protected void initViews() {
		initTitleBar();		
		listAdapter.setData(checkableItems);
		loadListData();
		lv.setAdapter(listAdapter);
		tvSelectAll.setOnClickListener(this);
		tvDelete.setOnClickListener(this);
		tvSelectCancel.setOnClickListener(this);
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		Rect rect = new Rect();
		getWindow().getDecorView().getWindowVisibleDisplayFrame(rect);
		appHeight = rect.height();
	}

	private void initAllSongsDialog() {
		allSongsDialog = new Dialog(this, R.style.DialogStyle);
		allSongsDialog.setContentView(getAllSongsDialogView());
		Window window = allSongsDialog.getWindow();
		window.setWindowAnimations(R.style.DialogAnimation);
		window.getDecorView().setPadding(0, 0, 0, 0);
		WindowManager.LayoutParams params = window.getAttributes();		
		window.setGravity(Gravity.BOTTOM);
		params.width = -1;
		params.height = appHeight;
		params.alpha = 1;
		window.setAttributes(params);
	}

	private View getAllSongsDialogView() {
		View view = View.inflate(this, R.layout.dialog_add_song, null);
		llEmptyDialog = (LinearLayout) view.findViewById(R.id.ll_empty);
		ListView lvDialog = (ListView) view.findViewById(R.id.lv);
		dialogCheckableItems = new ArrayList<>();
		dialogListAdapter.setData(dialogCheckableItems);
		loadDialogListData();
		lvDialog.setAdapter(dialogListAdapter);
		view.findViewById(R.id.tv_complete).setOnClickListener(this);
		return view;
	}
	
	//加载数据
	private void loadListData() {
		checkableItems.clear();
		for (Song song : Dao.INSTANCE.querySongs(songlistId)) {
			checkableItems.add(new CheckableItem<>(song, false));
		}
		updateView();
	}

	private void initTitleBar() {
		titleBar.setTitle(Dao.INSTANCE.querySonglist(songlistId).getName());
		titleBar.setStartImageButtonVisible(true);
		titleBar.setEndTextViewVisible(true);
		titleBar.setEndText("+");
		titleBar.setEndTextSize(TypedValue.COMPLEX_UNIT_SP, 36);
		titleBar.setEndTextTypeface(Typeface.MONOSPACE);
		titleBar.setOnMenuClickListener(new TitleBar.OnMenuClickListener() {
			@Override
			public void onMenuClick(View v) {
				switch(v.getId()) {
					case R.id.btn_start:
						finish();
						break;
					case R.id.tv_end:
						if (allSongsDialog == null) initAllSongsDialog();
						loadDialogListData();
						allSongsDialog.show();
						break;
				}
			}
		});
	}

	private void loadDialogListData() {
		dialogCheckableItems.clear();
		int minSize = 1024 * 500;//过滤500K以下文件
		//从媒体库中获取音乐文件信息
		Cursor cursor = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null,
				MediaStore.Audio.Media.IS_MUSIC + " IS NOT ? AND " + MediaStore.Audio.Media.SIZE + ">= ?",
				new String[]{"0", minSize + ""}, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
		while (cursor.moveToNext()) {
			String tilte = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
			String path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
			String displayName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME));
			int duration = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));
			long size = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE));
			tilte = tilte == null ? getString(R.string.unkown) : tilte;
			dialogCheckableItems.add(new CheckableItem<>(new Song(null, tilte, path, displayName, duration, size, 0), false));
		}
		cursor.close();
		updateDialogView();
	}

	private void updateView() {
		if (checkableItems.size() > 0) llEmpty.setVisibility(View.GONE);
		else llEmpty.setVisibility(View.VISIBLE);
		listAdapter.notifyDataSetChanged();
	}

	private void updateDialogView() {
		if (dialogCheckableItems.size() > 0) llEmptyDialog.setVisibility(View.GONE);
		else llEmptyDialog.setVisibility(View.VISIBLE);
		dialogListAdapter.notifyDataSetChanged();
	}
	
	/**
	 * 显示操作栏
	 */
	public void showActionLayout() {
		if (!isActionLayoutShowing) {
			isActionLayoutShowing = true;
			rlAction.setVisibility(View.VISIBLE);
			titleBar.setVisibility(View.GONE);
		}
	}

	/**
	 * 隐藏操作栏
	 */
	public void hideActionLayout() {
		if (isActionLayoutShowing) {
			isActionLayoutShowing = false;
			rlAction.setVisibility(View.GONE);
			titleBar.setVisibility(View.VISIBLE);
		}
	}

	public void selectAll(boolean isSelectAll) {
		if (isSelectAll) llDelete.setVisibility(View.VISIBLE);
		else llDelete.setVisibility(View.GONE);
		for (CheckableItem<Song> item : checkableItems) {
			item.isChecked = isSelectAll;
		}
		updateView();
	}
	
	private BaseListAdapter<CheckableItem<Song>> listAdapter = new BaseListAdapter<CheckableItem<Song>>() {
		@Override
		protected BaseHolder<CheckableItem<Song>> getHolder() {
			return new BaseHolder<CheckableItem<Song>>() {

				private TextView tvTitle;
				private ImageView ivDelete;
				private CheckableLayout chk;
				private ImageView ivBox;
				private View convertView;
				
				@Override
				protected void setData(final CheckableItem<Song> data, int position) {
					tvTitle.setText(data.obj.getTitle());
					chk.setOnCheckedChangeListener(new CheckableLayout.OnCheckedChangeListener() {
						@Override
						public void onCheckedChanged(CheckableLayout layout, boolean isChecked) {
							ivBox.setBackgroundResource(isChecked ? R.drawable.chk_checked : R.drawable.chk_normal);
							if(isChecked && llDelete.getVisibility() != View.VISIBLE) llDelete.setVisibility(View.VISIBLE);
							if (isChecked) showActionLayout();
							data.isChecked = isChecked;
							int checkedNum = 0;
							for (CheckableItem<Song> item : checkableItems) {
								if (item.isChecked) checkedNum++;
							}
							tvSelectNum.setText(getString(R.string.selected_items).replace("?", checkedNum + ""));
							if (checkedNum == 0) {
								tvSelectAll.setText("全选");//显示全选的时候说明不是全选中状态
								isSelectAll = false;
								llDelete.setVisibility(View.GONE);
							} else if (checkedNum == checkableItems.size()) {
								tvSelectAll.setText("全不选");
								isSelectAll = true;
							}
						}
					});
					chk.setChecked(data.isChecked);
				}

				@Override
				protected View createConvertView() {
					convertView = View.inflate(SonglistActivity.this, R.layout.item_song, null);
					tvTitle = (TextView) convertView.findViewById(R.id.tv_title);
					ivDelete = (ImageView) convertView.findViewById(R.id.iv_delete);
					chk = (CheckableLayout) convertView.findViewById(R.id.layout_chk);
					ivBox = (ImageView) convertView.findViewById(R.id.iv_box);
					ivDelete.setVisibility(View.GONE);
					chk.setVisibility(View.VISIBLE);
					return convertView;
				}
			};
		}
	};

	private BaseListAdapter<CheckableItem<Song>> dialogListAdapter = new BaseListAdapter<CheckableItem<Song>>() {
		@Override
		protected BaseHolder<CheckableItem<Song>> getHolder() {
			return new BaseHolder<CheckableItem<Song>>() {

				private TextView tvTitle;
				private CheckableLayout chk;
				private ImageView ivBox;
				
				@Override
				protected void setData(final CheckableItem<Song> data, int position) {
					tvTitle.setText(data.obj.getTitle());
					//歌单已有的歌曲颜色设置为灰色
					if (checkableItems.contains(data)) tvTitle.setTextColor(UiUtils.getColor(R.color.content_text) & 0x66FFFFFF);
					else tvTitle.setTextColor(UiUtils.getColor(R.color.content_text));
					chk.setOnCheckedChangeListener(new CheckableLayout.OnCheckedChangeListener() {
						@Override
						public void onCheckedChanged(CheckableLayout layout, boolean isChecked) {
							ivBox.setBackgroundResource(isChecked ? R.drawable.chk_checked : R.drawable.chk_normal);
							data.isChecked = isChecked;
						}
					});
					chk.setChecked(data.isChecked);
				}

				@Override
				protected View createConvertView() {
					View convertView = View.inflate(SonglistActivity.this, R.layout.item_song, null);
					tvTitle = (TextView) convertView.findViewById(R.id.tv_title);
					ImageView ivDelete = (ImageView) convertView.findViewById(R.id.iv_delete);
					chk = (CheckableLayout) convertView.findViewById(R.id.layout_chk);
					ivBox = (ImageView) convertView.findViewById(R.id.iv_box);
					ivDelete.setVisibility(View.GONE);
					chk.setVisibility(View.VISIBLE);
					return convertView;
				}
			};
		}
	};
	
	@Override
	public void onClick(View v) {
		switch(v.getId()) {
			case R.id.tv_select_all:
				selectAll(isSelectAll = !isSelectAll);
				break;
			case R.id.tv_select_cancel:
				selectAll(false);
				hideActionLayout();
				break;
			case R.id.tv_delete:
				List<Song> songModels = new ArrayList<>();
				for (CheckableItem<Song> item : checkableItems) {
					if (item.isChecked) songModels.add(item.obj);
				}
				Dao.INSTANCE.deleteSongs(songModels);
				hideActionLayout();
				llDelete.setVisibility(View.GONE);
				break;
			case R.id.tv_complete:
				List<Song> songs = new ArrayList<>();
				for (CheckableItem<Song> checkableItem : dialogCheckableItems) {
					if (checkableItem.isChecked) songs.add(checkableItem.obj);
				}
				Dao.INSTANCE.insertSongs(songlistId, songs);
				allSongsDialog.dismiss();
				break;
		}
	}

	@Override
	protected void onDestroy() {
		ObservableMgr.getSonglistObservable().deleteObserver(simpleObserver);
		super.onDestroy();
	}
}
