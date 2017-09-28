package com.advanpro.fwtools.module.me;

import android.os.Bundle;
import android.text.InputFilter;
import android.view.View;
import android.widget.Button;

import com.advanpro.fwtools.R;
import com.advanpro.fwtools.common.manager.ToastMgr;
import com.advanpro.fwtools.common.view.ClearEditText;
import com.advanpro.fwtools.common.view.TitleBar;
import com.advanpro.fwtools.entity.PasswordFilter;
import com.advanpro.fwtools.entity.StringLengthFilter;
import com.advanpro.fwtools.module.BaseActivity;
import com.advanpro.ascloud.ASCloud;
import com.advanpro.ascloud.CloudCallback;
import com.advanpro.ascloud.CloudException;
import com.advanpro.ascloud.CloudMsg;
import com.advanpro.utils.Util;

/**
 * Created by zeng on 2016/4/18.
 * 修改密码
 */
public class ChangePwdActivity extends BaseActivity {
    private TitleBar titleBar;
    private ClearEditText etOldPwd;
    private ClearEditText etPwd;
    private ClearEditText etRePwd;
    private Button btnOk;  
    private String newPwd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_pwd);
        assignViews();
        initViews();
    }
    
    private void assignViews() {
        titleBar = (TitleBar) findViewById(R.id.title_bar);
        etOldPwd = (ClearEditText) findViewById(R.id.et_old_pwd);
        etPwd = (ClearEditText) findViewById(R.id.et_pwd);
        etRePwd = (ClearEditText) findViewById(R.id.et_re_pwd);
        btnOk = (Button) findViewById(R.id.btn_ok);
    }

    private void initViews() {
        initTitleBar();
        InputFilter[] inputFilters1 = {new PasswordFilter(), new StringLengthFilter(20)};
        InputFilter[] inputFilters2 = {new PasswordFilter(), new StringLengthFilter(20)};
        InputFilter[] inputFilters3 = {new PasswordFilter(), new StringLengthFilter(20)};
        etPwd.setFilters(inputFilters1);
        etRePwd.setFilters(inputFilters2);
        etOldPwd.setFilters(inputFilters3);
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkInputValidity()) {
                    btnOk.setEnabled(false);
                    CloudMsg req = new CloudMsg("/user/changePassword");
                    req.put("UserID", ASCloud.userInfo.ID);
                    req.put("Password", Util.MakeMd5(etOldPwd.getText().toString().trim()));
                    req.put("NewPassword", Util.MakeMd5(newPwd));
                    ASCloud.sendMsg(req, new CloudCallback() {
                        @Override
                        public void success(CloudMsg msg) {
                            ToastMgr.showTextToast(ChangePwdActivity.this, 0, R.string.change_success);
                            ASCloud.userInfo.Password = newPwd;
                            ASCloud.userInfo.save();
                            finish();
                        }

                        @Override
                        public void error(CloudException e) {
                            ToastMgr.showTextToast(ChangePwdActivity.this, 0, e.getMessage());
                            btnOk.setEnabled(true);
                        }
                    });
                }
            }
        });
    }

    private void initTitleBar() {
        titleBar.setTitle(R.string.change_pwd);
        titleBar.setStartImageButtonVisible(true);
        titleBar.setOnMenuClickListener(new TitleBar.OnMenuClickListener() {
            @Override
            public void onMenuClick(View v) {
                if (v.getId() == R.id.btn_start) {
                    finish();
                }
            }
        });
    }

    //检查合法性
    private boolean checkInputValidity() {        
        String old = etOldPwd.getText().toString().trim();
        if (old.isEmpty()) {
            ToastMgr.showTextToast(this, 0, R.string.input_old_pwd);
            return false;
        }
        String pwd = etPwd.getText().toString().trim();
        if (pwd.isEmpty()) {
            ToastMgr.showTextToast(this, 0, R.string.input_new_pwd);
            return false;
        }        
        if (pwd.length() < 6) {
            ToastMgr.showTextToast(this, 0, R.string.password_at_least_6_char);
            return false;
        }
        String confirmPwd = etRePwd.getText().toString().trim();
        if (!pwd.equals(confirmPwd)) {
            ToastMgr.showTextToast(this, 0, R.string.comfirm_password_different);
            return false;
        }
        newPwd = pwd;
        return true;
    }
}
