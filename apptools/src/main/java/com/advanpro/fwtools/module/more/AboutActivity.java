package com.advanpro.fwtools.module.more;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.advanpro.fwtools.MyApplication;
import com.advanpro.fwtools.R;
import com.advanpro.fwtools.common.manager.ToastMgr;
import com.advanpro.fwtools.common.view.ArrowItemView;
import com.advanpro.fwtools.common.view.TitleBar;
import com.advanpro.fwtools.entity.VersionChecker;
import com.advanpro.fwtools.module.BaseActivity;

import java.io.File;

/**
 * Created by zeng on 2016/4/23.
 * 关于安小白
 */
public class AboutActivity extends BaseActivity implements View.OnClickListener {
    private TextView tvVersion;
    private ArrowItemView itemCheckNewVersion;
    private TitleBar titleBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        assignViews();
        initViews();
    }

    private void assignViews() {
        tvVersion = (TextView) findViewById(R.id.tv_version);
        itemCheckNewVersion = (ArrowItemView) findViewById(R.id.item_check_new_version);
        titleBar = (TitleBar) findViewById(R.id.title_bar);
    }
    
    private void initViews() {
        initTitleBar();
        tvVersion.setText(getAppVersion());
        itemCheckNewVersion.setOnClickListener(this);
    }

    private void initTitleBar() {
        titleBar.setTitle(R.string.about_ansobuy);
        titleBar.setStartImageButtonVisible(true);
        titleBar.setOnMenuClickListener(new TitleBar.OnMenuClickListener() {
            @Override
            public void onMenuClick(View v) {
                switch(v.getId()) {
                    case R.id.btn_start:
                        finish();
                        break;
                }
            }
        });
    }
    
    private String getAppVersion() {
        String version = MyApplication.getAppVersion();
        if (version == null) version = getString(R.string.unkown);
        return "V " + version;
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.item_check_new_version:
                VersionChecker checker = new VersionChecker(this);
                checker.checkNewVersion(VersionChecker.Product.ANSOBUY, MyApplication.getAppVersion(), new VersionChecker.FileCallback() {
                    @Override
                    public void onSuccess(File file) {
                        if (file == null) {
                            ToastMgr.showTextToast(AboutActivity.this, 0, R.string.current_newest_version);
                        } else {
                            //安装APP
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        }
                    }

                    @Override
                    public void onError(String msg) {
                        ToastMgr.showTextToast(AboutActivity.this, 0, msg);
                    }

                    @Override
                    public void onCancel() {}
                });
        		break;
        }
    }
}
