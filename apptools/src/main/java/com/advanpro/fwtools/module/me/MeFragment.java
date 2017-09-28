package com.advanpro.fwtools.module.me;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.advanpro.fwtools.R;
import com.advanpro.fwtools.common.base.BaseFragment;
import com.advanpro.fwtools.common.manager.ToastMgr;
import com.advanpro.fwtools.common.util.FileUtils;
import com.advanpro.fwtools.module.MainActivity;
import com.advanpro.ascloud.ASCloud;
import com.advanpro.ascloud.CloudCallback;
import com.advanpro.ascloud.CloudException;
import com.advanpro.ascloud.CloudMsg;
import com.advanpro.ascloud.FileCallback;

import java.io.File;

/**
 * Created by zengfs on 2016/1/14.
 * 我模块
 */
public class MeFragment extends BaseFragment<MainActivity> implements View.OnClickListener {

    private ImageView ivHead;
    private TextView tvUsername;
    private TextView tvPoints;
    private RelativeLayout pay_zfb,pay_wx,pay_log;

    @Override
    protected View getRootView(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(R.layout.frag_me, container, false);
    }

    @Override
    protected void assignViews() {
        ivHead = (ImageView) rootView.findViewById(R.id.iv_head);
        tvUsername = (TextView) rootView.findViewById(R.id.tv_username);
        tvPoints = (TextView) rootView.findViewById(R.id.tv_points);
        pay_zfb= (RelativeLayout) rootView.findViewById(R.id.pay_zfb);
        pay_wx= (RelativeLayout) rootView.findViewById(R.id.pay_wx);        
        pay_log= (RelativeLayout) rootView.findViewById(R.id.pay_log);
    }

    @Override
    protected void initViews() {
        ivHead.setOnClickListener(this);
        pay_wx.setOnClickListener(this);
        pay_zfb.setOnClickListener(this);
        pay_log.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.iv_head:
                //startActivity(new Intent(getActivity(), UserInfoActivity.class));
                startActivity(new Intent(getActivity(), MyInfoFragment.class));
                break;
            case R.id.pay_zfb:
                //跳转支付宝界面
                ToastMgr.showTextToast(getActivity(),Toast.LENGTH_SHORT,"暂时未开放此功能！");
                // startActivity(new Intent(getActivity(), UserInfoActivity.class));
                break;
            case R.id.pay_wx:
                //跳转微信界面
                ToastMgr.showTextToast(getActivity(),Toast.LENGTH_SHORT,"暂时未开放此功能！");
                //startActivity(new Intent(getActivity(), UserInfoActivity.class));
                break;
            case R.id.pay_log:
                //跳转日志信息界面
                startActivity(new Intent(getActivity(), PayLoginfoActivity.class));
                break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        updateViews();
    }

    private void updateViews() {
        setHeadImage();
        tvUsername.setText(ASCloud.userInfo.UserName);
        setIntegral();
    }

    private void setIntegral() {
        CloudMsg req = new CloudMsg("/user/getIntegral");
        req.put("userId", ASCloud.userInfo.ID);
        ASCloud.sendMsg(req, new CloudCallback() {
            @Override
            public void success(CloudMsg cloudMsg) {
                try {
                    tvPoints.setText(String.valueOf(cloudMsg.getLong("integral")));
                } catch (CloudException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void error(CloudException e) {

            }
        });
    }

    private void setHeadImage() {
        ASCloud.userInfo.getFaceImg(new FileCallback() {
            @Override
            public void success(File file) {
                ivHead.setImageBitmap(FileUtils.getBitmap(file.getAbsolutePath()));
            }

            @Override
            public void error() {				
            }
        });
    }
}
