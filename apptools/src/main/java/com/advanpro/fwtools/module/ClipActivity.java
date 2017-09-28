package com.advanpro.fwtools.module;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ProgressBar;

import com.advanpro.fwtools.Constant;
import com.advanpro.fwtools.R;
import com.advanpro.fwtools.common.manager.ToastMgr;
import com.advanpro.fwtools.common.util.FileUtils;
import com.advanpro.fwtools.common.view.ClipImageLayout;
import com.advanpro.fwtools.common.view.TitleBar;
import com.advanpro.ascloud.ASCloud;
import com.advanpro.ascloud.CloudCallback;
import com.advanpro.ascloud.CloudException;
import com.advanpro.ascloud.CloudMsg;
import com.advanpro.aswear.ASWear;

import java.io.File;
import java.util.UUID;

/**
 * Created by zeng on 2016/4/10.
 * 选择图片或拍照并剪裁
 */
public class ClipActivity extends BaseActivity{
    private TitleBar titleBar;
    private ClipImageLayout layoutClipImage;	
    private ProgressBar progressBar;

	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String path = getIntent().getStringExtra(Constant.EXTRA_PATH);
        if (TextUtils.isEmpty(path) || !(new File(path).exists())) {
            ToastMgr.showTextToast(this, 0, R.string.image_not_exist);
            finish();
            return;
        }
        setContentView(R.layout.activity_clip);
        assignViews();
        initTitleBar();
		Bitmap originalBitmap = FileUtils.getBitmap(path, 600, 600);
        if (originalBitmap == null) {
            ToastMgr.showTextToast(this, 0, R.string.image_load_failed);
            finish();
            return;
        }
        layoutClipImage.setBitmap(originalBitmap);
    }

    private void assignViews() {
        titleBar = (TitleBar) findViewById(R.id.title_bar);
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        layoutClipImage = (ClipImageLayout) findViewById(R.id.layout_clip_image);  
        
    }
    
    private void initTitleBar() {
        titleBar.setStartImageButtonVisible(true);
        titleBar.setEndTextViewVisible(true);
        titleBar.setTitle(R.string.move_and_zoom);
        titleBar.setEndText(R.string.ok);
        titleBar.setOnMenuClickListener(new TitleBar.OnMenuClickListener() {
            @Override
            public void onMenuClick(View v) {
                switch(v.getId()) {
                    case R.id.btn_start:
                        finish();
                        break;
                    case R.id.tv_end:
                        final Bitmap bitmap = layoutClipImage.clip();
                        final File fileFace = new File(ASWear.getFaceImageDir(), UUID.randomUUID().toString() + ".jpg");
                        FileUtils.saveBitmapToFile(bitmap, fileFace);
                        progressBar.setVisibility(View.VISIBLE);
                        titleBar.setEndTextViewVisible(false);
                        //上传头像
                        ASCloud.setUserFaceImg(ASCloud.userInfo.ID, fileFace, new CloudCallback() {
                            @Override
                            public void success(CloudMsg resp) {
                                ASCloud.reqUserFace(ASCloud.userInfo);                            
                                Intent intent = new Intent();
                                intent.putExtra(Constant.EXTRA_PATH, fileFace.getAbsolutePath());
                                setResult(RESULT_OK, intent);
                                finish();
                            }

                            @Override
                            public void error(CloudException e) {   
                                ToastMgr.showTextToast(ClipActivity.this, 0, e.msg);
                                fileFace.delete();
                                finish();
                            }
                        });                        
                        break;
                }
            }
        });
    }
}
