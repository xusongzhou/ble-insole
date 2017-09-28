package com.advanpro.fwtools.module.more;

import android.os.Bundle;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ToggleButton;

import com.advanpro.fwtools.R;
import com.advanpro.fwtools.common.manager.ToastMgr;
import com.advanpro.fwtools.common.view.TitleBar;
import com.advanpro.fwtools.db.Dao;
import com.advanpro.fwtools.entity.StringLengthFilter;
import com.advanpro.fwtools.module.BaseActivity;

/**
 * Created by zengfs on 2016/2/3.
 * 新建歌单
 */
public class NewSonglistActivity extends BaseActivity implements TitleBar.OnMenuClickListener, CompoundButton.OnCheckedChangeListener {

	private EditText et;
	private ToggleButton toggleButton;
	private boolean isChecked;	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_new_songlist);
		initViews();
	}

	protected void initViews() {
		//初始化标题栏
		TitleBar titleBar = (TitleBar) findViewById(R.id.title_bar);
		titleBar.setTitle(R.string.new_songlist);
		titleBar.setStartImageButtonVisible(true);
		titleBar.setEndTextViewVisible(true);
		titleBar.setEndText(R.string.save);
		titleBar.setOnMenuClickListener(this);
		et = (EditText) findViewById(R.id.et_name);
		InputFilter[] filters = { new StringLengthFilter(40) };
		et.setFilters(filters);
		toggleButton = (ToggleButton) findViewById(R.id.toggle_button);
		toggleButton.setOnCheckedChangeListener(this);
		findViewById(R.id.tv).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				toggleButton.setChecked(!isChecked);
			}
		});
	}

	@Override
	public void onMenuClick(View v) {
		switch(v.getId()) {
		    case R.id.btn_start:	
				finish();
				break;
			case R.id.tv_end:
				String name = et.getText().toString().trim();
				if (TextUtils.isEmpty(name)) {
                    ToastMgr.showTextToast(this, 0, R.string.input_songlist_name);
					return;
				}
				if (Dao.INSTANCE.isSonglistExist(name)) {
                    ToastMgr.showTextToast(this, 0, R.string.songlist_exist);
					return;
				}
				Dao.INSTANCE.insertSonglist(name); 
				finish();
				break;
		}
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		this.isChecked = isChecked;
	}
}
