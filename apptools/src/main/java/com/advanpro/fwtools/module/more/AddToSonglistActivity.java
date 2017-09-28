package com.advanpro.fwtools.module.more;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import com.advanpro.fwtools.Constant;
import com.advanpro.fwtools.R;
import com.advanpro.fwtools.common.base.BaseHolder;
import com.advanpro.fwtools.common.base.BaseListAdapter;
import com.advanpro.fwtools.common.view.TitleBar;
import com.advanpro.fwtools.db.Dao;
import com.advanpro.fwtools.db.Song;
import com.advanpro.fwtools.db.Songlist;
import com.advanpro.fwtools.entity.SimpleObserver;
import com.advanpro.fwtools.module.BaseActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

/**
 * Created by zengfs on 2016/2/6.
 * 将歌曲添加到歌单
 */
public class AddToSonglistActivity extends BaseActivity implements AdapterView.OnItemClickListener {
	
	private List<Song> selectedSongs;
	private ListView lvSonglist;
	private TitleBar titleBar;
	private List<Songlist> songlists;
	private TextView tvEmpty;
	private SimpleObserver simpleObserver;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_to_songlist);
		String extra = getIntent().getStringExtra(Constant.EXTRA_FROM_CLASS_NAME);
		//如果是从MusicManagerActivity跳转过来，获取到它的实例，从而拿到添加的歌曲的集合
		if (MusicManagerActivity.class.getName().equals(extra)) {
			MusicManagerActivity musicManagerActivity = (MusicManagerActivity) BaseActivity.getActivity(extra);
			selectedSongs = musicManagerActivity.selectedSongs;
		} else {
			finish();
			return;
		}
		assignViews();
		initViews();
	}

	protected void assignViews() {
		titleBar = (TitleBar) findViewById(R.id.title_bar);
		lvSonglist = (ListView) findViewById(R.id.lv_songlist);
		tvEmpty = (TextView) findViewById(R.id.tv_empty);
	}

	protected void initViews() {
		initTitleBar();
		loadListData();
		listAdapter.setData(songlists);
		lvSonglist.setAdapter(listAdapter);
		//添加歌单数据表变化观察者
		simpleObserver = new SimpleObserver(ObservableMgr.getSonglistObservable()) {
			@Override
			public void update(Observable observable, Object data) {
				loadListData();
			}
		};
		lvSonglist.setOnItemClickListener(this);
	}

	private void initTitleBar() {
		titleBar.setTitle(R.string.add_to_songlist);
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
						startActivity(new Intent(AddToSonglistActivity.this, NewSonglistActivity.class));
						break;
				}
			}
		});
	}

	private void loadListData() {
		if (songlists == null) songlists = new ArrayList<>();
		songlists.clear();
		songlists.addAll(Dao.INSTANCE.querySonglists());
		updateView();
	}

	private void updateView() {
		if (songlists.size() > 0) tvEmpty.setVisibility(View.GONE);
		else tvEmpty.setVisibility(View.VISIBLE);
		listAdapter.notifyDataSetChanged();
	}
	
	private BaseListAdapter<Songlist> listAdapter = new BaseListAdapter<Songlist>() {

		@Override
		protected BaseHolder<Songlist> getHolder() {
			return new BaseHolder<Songlist>() {

				private TextView tvName;
				private TextView tvNum;
				
				@Override
				protected void setData(Songlist data, int postion) {
					tvName.setText(data.getName());
					tvNum.setText(getString(R.string.songs_number).replace("?", Dao.INSTANCE.querySongs(data.getId()).size() + ""));
				}

				@Override
				protected View createConvertView() {
					View convertView = View.inflate(AddToSonglistActivity.this, R.layout.item_songlist_simple, null);
					tvName = (TextView) convertView.findViewById(R.id.tv_name);
					tvNum = (TextView) convertView.findViewById(R.id.tv_num);
					return convertView;
				}
			};
		}
	};

	@Override
	public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
		Dao.INSTANCE.insertSongs(listAdapter.getItem(position).getId(), selectedSongs);//将歌曲添加到指定的歌单		
		setResult(RESULT_OK);
		finish();
	}

	@Override
	protected void onDestroy() {
		ObservableMgr.getSonglistObservable().deleteObserver(simpleObserver);
		super.onDestroy();
	}
}
