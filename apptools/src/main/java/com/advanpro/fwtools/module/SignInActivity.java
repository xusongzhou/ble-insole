package com.advanpro.fwtools.module;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.advanpro.fwtools.Constant;
import com.advanpro.fwtools.R;
import com.advanpro.fwtools.alg.AlgLib;
import com.advanpro.fwtools.common.manager.ToastMgr;
import com.advanpro.fwtools.common.util.PreferencesUtils;
import com.advanpro.fwtools.common.util.UiUtils;
import com.advanpro.fwtools.common.view.ClearEditText;
import com.advanpro.fwtools.common.view.TitleBar;
import com.advanpro.fwtools.db.Dao;
import com.advanpro.fwtools.entity.TextChangeListener;
import com.advanpro.fwtools.thridapp.QQLogin;
import com.advanpro.fwtools.thridapp.WXLogin;
import com.advanpro.ascloud.ASCloud;
import com.advanpro.ascloud.CloudCallback;
import com.advanpro.ascloud.CloudException;
import com.advanpro.ascloud.CloudMsg;
import com.advanpro.ascloud.UserInfo;
import com.tencent.tauth.UiError;

import org.json.JSONException;
import org.json.JSONObject;

public class SignInActivity extends BaseActivity implements View.OnClickListener {
    private TitleBar titleBar;
    private ClearEditText etAccount;
    private ClearEditText etPwd;
    private Button btnSignIn, qqLogin, wxLogin;
    private ProgressDialog progressDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WXLogin.newInstance().create(this);
        long lastSignIn = PreferencesUtils.getLong(Constant.SP_LAST_SIGN_IN_TIME, 0);
        //7天未登录过，重新手动登录
        if (ASCloud.isLogin() && System.currentTimeMillis() - lastSignIn < 7 * 24 * 3600000L) {
            Dao.INSTANCE.insertOrIgnoreUser(ASCloud.userInfo.ID);
            enter(true);
        } else {
            setContentView(R.layout.activity_sign_in);
            assignViews();
            initViews();
        }
    }

    private void initViews() {
        initTitleBar();
        wxLogin.setOnClickListener(this);
        qqLogin.setOnClickListener(this);
        btnSignIn.setOnClickListener(this);
        etAccount.addTextChangedListener(new TextChangeListener() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0 && etPwd.length() > 0) btnSignIn.setEnabled(true);
                else btnSignIn.setEnabled(false);
            }
        });
        etPwd.addTextChangedListener(new TextChangeListener() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0 && etAccount.length() > 0) btnSignIn.setEnabled(true);
                else btnSignIn.setEnabled(false);
            }
        });
        etAccount.setText(PreferencesUtils.getString(Constant.SP_LAST_SIGN_IN_USER));
        progressDialog = new ProgressDialog(this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setMessage(getString(R.string.sign_in_ing));
    }

    private void initTitleBar() {
        titleBar.setTitle(R.string.sign_in);
        titleBar.setEndText(R.string.sign_up);
        titleBar.setEndTextViewVisible(true);
        titleBar.setOnMenuClickListener(new TitleBar.OnMenuClickListener() {
            @Override
            public void onMenuClick(View v) {
                if (v.getId() == R.id.tv_end) {
                    startActivity(new Intent(SignInActivity.this, SignUpActivity.class));
                }
            }
        });
    }


    private void assignViews() {
        titleBar = (TitleBar) findViewById(R.id.title_bar);
        etAccount = (ClearEditText) findViewById(R.id.et_account);
        etPwd = (ClearEditText) findViewById(R.id.et_pwd);
        btnSignIn = (Button) findViewById(R.id.btn_sign_in);
        qqLogin = (Button) findViewById(R.id.qqLogin);
        wxLogin = (Button) findViewById(R.id.wxLogin);
    }


    @Override
    protected void onResume() {
        super.onResume();
        WXLogin.newInstance().resume(new WXLogin.OnDataCallback() {
            @Override
            public void onData(JSONObject jsonObject) {
                if (jsonObject != null) {
                    final UserInfo userInfo = new UserInfo();
                    try {
                        userInfo.Account = jsonObject.getString("unionid")+"&weixin";
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    try {
                        userInfo.UserName = jsonObject.getString("nickname");
                        if (jsonObject.getInt("sex") == 1) {
                            userInfo.Gender = "M";
                            userInfo.High = 170;
                            userInfo.Weight = 60;
                        } else {
                            userInfo.Gender = "F";
                            userInfo.High = 160;
                            userInfo.Weight = 50;
                        }
                        userInfo.Password = "";
                        ASCloud.register(userInfo, new CloudCallback() {
                            @Override
                            public void success(CloudMsg cloudMsg) {
                                login(userInfo.Account, userInfo.Password);
                            }

                            @Override
                            public void error(CloudException e) {
                                if (e.getErrorCode() == 3100)  // 用户已存在
                                {
                                    login(userInfo.Account, userInfo.Password);
                                } else {
                                    progressDialog.dismiss();
                                    ToastMgr.showTextToast(SignInActivity.this, 0, e.getMessage());
                                }
                            }
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_sign_in:
                if (!progressDialog.isShowing()) progressDialog.show();
                String account = etAccount.getText().toString();
                String pwd = etPwd.getText().toString();
                signIn(account, pwd);
                break;
            case R.id.qqLogin:
                if (!progressDialog.isShowing()) progressDialog.show();
                QQLogin.executeLogin(this, new QQLogin.OnCallBack() {
                    @Override
                    public void onSuccess(Object object) {
                        JSONObject jsonObject = (JSONObject) object;
                        final UserInfo userInfo = new UserInfo();
                        try {
                            userInfo.Account = jsonObject.getString("openid")+"&qq";
                            userInfo.UserName = jsonObject.get("name").toString();
                            if (jsonObject.get("gender").toString().equals("男")) {//使用默认身高和体重
                                userInfo.Gender = "M";
                                userInfo.High = 170;
                                userInfo.Weight = 60;
                            } else {
                                userInfo.Gender = "F";
                                userInfo.High = 160;
                                userInfo.Weight = 50;
                            }
                            userInfo.Password = "";
                            ASCloud.register(userInfo, new CloudCallback() {
                                @Override
                                public void success(CloudMsg resp) {
                                    login(userInfo.Account, userInfo.Password);
                                }

                                @Override
                                public void error(CloudException e) {
                                    if (e.getErrorCode() == 3100)  // 用户已存在
                                    {
                                        login(userInfo.Account, userInfo.Password);
                                    } else {
                                        progressDialog.dismiss();
                                        ToastMgr.showTextToast(SignInActivity.this, 0, e.getMessage());
                                    }
                                }
                            });
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(final Object object) {
                        UiUtils.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                progressDialog.dismiss();
                                if (object != null) {
                                    if (object instanceof UiError) {
                                        UiError e = (UiError) object;
                                        ToastMgr.showTextToast(SignInActivity.this, 0, e.errorDetail);
                                    } else if (object instanceof String) {
                                        String msg = (String) object;
                                        ToastMgr.showTextToast(SignInActivity.this, 0, msg);
                                    }
                                }
                            }
                        });                                                
                    }

                    @Override
                    public void onCancel() {
                        progressDialog.dismiss();
                    }
                });

                break;
            case R.id.wxLogin:
                if (!progressDialog.isShowing()) progressDialog.show();
                WXLogin.newInstance().sendMsgToWx();
                break;

        }
    }


    //请求网络登录
    private void signIn(String account, String pwd) {
        btnSignIn.setEnabled(false);
        ASCloud.login(account, pwd, new CloudCallback() {
            @Override
            public void success(CloudMsg msg) {
                Dao.INSTANCE.insertOrIgnoreUser(ASCloud.userInfo.ID);
                enter(false);
            }

            @Override
            public void error(CloudException e) {
                progressDialog.dismiss();
                ToastMgr.showTextToast(SignInActivity.this, 0, e.getMessage());
                btnSignIn.setEnabled(true);
            }
        });
    }

    private void login(String account, String password) {
        ASCloud.login(account, password, new CloudCallback() {
            @Override
            public void success(CloudMsg msg) {
                enter(false);
            }

            @Override
            public void error(CloudException e) {
                progressDialog.dismiss();
                ToastMgr.showTextToast(SignInActivity.this, 0, e.getMessage());
            }
        });
    }

    //跳转到主页
    public void enter(boolean auto) {
        ASCloud.setExperience(false);
        AlgLib.setHeightAndWeight(ASCloud.userInfo.High, ASCloud.userInfo.Weight);
        if (auto) ASCloud.reqUserInfo(ASCloud.userInfo, null); //如果自动登陆，重新从云端获取用户资料
        // 记录本次登陆时间
        PreferencesUtils.putString(Constant.SP_LAST_SIGN_IN_USER, ASCloud.userInfo.Account);
        PreferencesUtils.putLong(Constant.SP_LAST_SIGN_IN_TIME, System.currentTimeMillis());
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        QQLogin.tencentOnActivityResult(requestCode, resultCode, data);
    }
}
