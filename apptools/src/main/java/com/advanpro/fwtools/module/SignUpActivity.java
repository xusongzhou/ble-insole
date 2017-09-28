package com.advanpro.fwtools.module;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.InputFilter;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;

import com.advanpro.fwtools.R;
import com.advanpro.fwtools.common.manager.ToastMgr;
import com.advanpro.fwtools.common.util.UiUtils;
import com.advanpro.fwtools.common.view.ClearEditText;
import com.advanpro.fwtools.common.view.CycleWheelView;
import com.advanpro.fwtools.common.view.TitleBar;
import com.advanpro.fwtools.entity.PasswordFilter;
import com.advanpro.fwtools.entity.StringLengthFilter;
import com.advanpro.ascloud.ASCloud;
import com.advanpro.ascloud.CloudCallback;
import com.advanpro.ascloud.CloudException;
import com.advanpro.ascloud.CloudMsg;
import com.advanpro.ascloud.UserInfo;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by zeng on 2016/4/10.
 * 注册界面
 */
public class SignUpActivity extends BaseActivity implements View.OnClickListener {
    private TitleBar titleBar;
    private ClearEditText etAccount;
    private ClearEditText etPwd;
    private ClearEditText etConfirmPwd;
    private ClearEditText etName;
    private RadioButton rbMale;
    private RadioButton rbFemale;
    private ClearEditText etHeight;
    private ClearEditText etWeight;
    private ClearEditText check_input;
    private TextView tvBirthyear;
    private Button btnSignUp, getCheck_code_btn;
    private UserInfo userInfo = new UserInfo();
    private Dialog selectDialog;
    private CycleWheelView numpicker;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            JSONObject object= (JSONObject) msg.obj;
            try {
                switch (Integer.parseInt(object.getString("statusCode"))){
                    case 3000:
                        ToastMgr.showTextToast(SignUpActivity.this,1,object.toString());
                        register();
                        break;
                    case 3001:
                        ToastMgr.showTextToast(SignUpActivity.this,1,"请一小时后再次获取");
                        break;
                    case 3107:
                        ToastMgr.showTextToast(SignUpActivity.this,1,"验证码已过期");
                        break;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        assignViews();
        initViews();
    }

    private void assignViews() {
        titleBar = (TitleBar) findViewById(R.id.title_bar);
        etAccount = (ClearEditText) findViewById(R.id.et_account);
        etPwd = (ClearEditText) findViewById(R.id.et_pwd);
        etConfirmPwd = (ClearEditText) findViewById(R.id.et_confirm_pwd);
        etName = (ClearEditText) findViewById(R.id.et_name);
        rbMale = (RadioButton) findViewById(R.id.rb_male);
        rbFemale = (RadioButton) findViewById(R.id.rb_female);
        etHeight = (ClearEditText) findViewById(R.id.et_height);
        etWeight = (ClearEditText) findViewById(R.id.et_weight);
        btnSignUp = (Button) findViewById(R.id.btn_sign_up);
        tvBirthyear = (TextView) findViewById(R.id.tv_birthyear);
        getCheck_code_btn = (Button) findViewById(R.id.getCheck_code_btn);
        check_input = (ClearEditText) findViewById(R.id.check_input);

    }

    private void initViews() {
        initTitleBar();
        getCheck_code_btn.setOnClickListener(this);
        btnSignUp.setOnClickListener(this);
        InputFilter[] filters = {new StringLengthFilter(20)};
        etName.setFilters(filters);
        InputFilter[] inputFilters1 = {new PasswordFilter(), new StringLengthFilter(20)};
        InputFilter[] inputFilters2 = {new PasswordFilter(), new StringLengthFilter(20)};
        etPwd.setFilters(inputFilters1);
        etConfirmPwd.setFilters(inputFilters2);
        tvBirthyear.setOnClickListener(this);
        initSelectDialog();
    }

    private void initNumPicker() {
        List<String> pickerLabels = new ArrayList<>();
        for (int i = 1900; i <= Calendar.getInstance().get(Calendar.YEAR); i++) {
            pickerLabels.add(String.valueOf(i));
        }
        numpicker.setLabels(pickerLabels);
        try {
            numpicker.setWheelSize(7);
        } catch (CycleWheelView.CycleWheelViewException e) {
            e.printStackTrace();
        }
        numpicker.setCycleEnable(true);
        numpicker.setSelection(30);
        numpicker.setItemSpace(3);
        numpicker.setAlphaGradual(0.6f);
        numpicker.setDivider(getResources().getColor(R.color.content_text), 2);
        numpicker.setSolid(Color.TRANSPARENT, Color.TRANSPARENT);
        numpicker.setLabelColor(Color.parseColor("#777777"));
        numpicker.setLabelSelectColor(getResources().getColor(R.color.content_text));
    }

    private void initSelectDialog() {
        selectDialog = new Dialog(this, R.style.DialogStyle);
        selectDialog.setContentView(getSelectDialogView());
        Window window = selectDialog.getWindow();
        window.setWindowAnimations(R.style.DialogAnimation);
        window.getDecorView().setPadding(0, 0, 0, 0);
        window.setBackgroundDrawable(new ColorDrawable(UiUtils.getColor(R.color.playlist_bg)));
        WindowManager.LayoutParams params = window.getAttributes();
        window.setGravity(Gravity.BOTTOM);
        params.width = -1;
        params.height = -2;
        window.setAttributes(params);
    }

    private View getSelectDialogView() {
        View view = View.inflate(this, R.layout.dialog_num_picker, null);
        TextView tvCancel = (TextView) view.findViewById(R.id.tv_cancel);
        TextView tvOk = (TextView) view.findViewById(R.id.tv_ok);
        numpicker = (CycleWheelView) view.findViewById(R.id.picker1);
        tvCancel.setOnClickListener(this);
        tvOk.setOnClickListener(this);
        initNumPicker();
        return view;
    }

    private void initTitleBar() {
        titleBar.setTitle(R.string.sign_up);
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

    @Override
    public void onClick(final View v) {
        switch (v.getId()) {
            case R.id.btn_sign_up:
                if (check_input.getText().toString().length() > 0) {
                    try {
                        httpHelp(new JSONObject().put("account", etAccount.getText().toString().trim()).put("verifyCode", check_input.getText().toString().trim()), "checkVerifyCode");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }else{
                    ToastMgr.showTextToast(this, 0, R.string.input_code);
                }
                break;
            case R.id.tv_birthyear:
                numpicker.setSelection(Integer.parseInt(tvBirthyear.getText().toString()) - 1900);
                selectDialog.show();
                break;
            case R.id.tv_cancel:
                selectDialog.dismiss();
                break;
            case R.id.tv_ok:
                selectDialog.dismiss();
                tvBirthyear.setText(numpicker.getSelectLabel());
                break;
            case R.id.getCheck_code_btn:
                if (etAccount.getText().toString().length() > 0 || !etAccount.getText().toString().equals("")) {
                    try {
                        httpHelp(new JSONObject().put("Account", etAccount.getText().toString().trim()), "getVerifyCode");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    ToastMgr.showTextToast(this, 0, R.string.input_correct_phone);
                }
                break;
        }
    }

    private void register() {
        if (checkInputValidity()) {
            btnSignUp.setEnabled(false);
            ASCloud.register(userInfo, new CloudCallback() {
                @Override
                public void success(CloudMsg acMsg) {
                    ToastMgr.showTextToast(SignUpActivity.this, 0, R.string.sign_up_success);
                    ASCloud.login(userInfo.Account, userInfo.Password, new CloudCallback() {
                        @Override
                        public void success(CloudMsg msg) {
                            finish();
                            SignInActivity signIn = (SignInActivity) BaseActivity.getActivity(SignInActivity.class.getName());
                            signIn.enter(true);
                        }

                        @Override
                        public void error(CloudException e) {
                            finish();
                        }
                    });
                }

                @Override
                public void error(CloudException e) {
                    if (e.getErrorCode() == 3924) {
                        ToastMgr.showTextToast(SignUpActivity.this, 0, getString(R.string.phone_exist));
                    } else {
                        ToastMgr.showTextToast(SignUpActivity.this, 0, e.getMessage());
                    }
                    btnSignUp.setEnabled(true);
                }
            });
        }
    }


    //检查各项输入的合法性
    private boolean checkInputValidity() {
        userInfo.Account = etAccount.getText().toString().trim();
        if (userInfo.Account.isEmpty()) {
            ToastMgr.showTextToast(this, 0, R.string.input_phone);
            return false;
        }
        //如果是纯数字，但不是手机号码
        if (userInfo.Account.matches("^\\d+$") && !userInfo.Account.matches("^\\d{7,11}$")) {
            ToastMgr.showTextToast(this, 0, R.string.input_correct_phone);
            return false;
        }
        String regex = "^\\s*\\w+(?:\\.?[\\w-]+)*@[a-zA-Z0-9]+(?:[-.][a-zA-Z0-9]+)*\\.[a-zA-Z]+\\s*$";
        if (!userInfo.Account.matches("^\\d+$") && !userInfo.Account.matches(regex)) {
            ToastMgr.showTextToast(this, 0, R.string.input_correct_phone);
            return false;
        }


        userInfo.Password = etPwd.getText().toString().trim();
        if (userInfo.Password.isEmpty()) {
            ToastMgr.showTextToast(this, 0, R.string.input_password);
            return false;
        }
        if (userInfo.Password.length() < 6) {
            ToastMgr.showTextToast(this, 0, R.string.password_at_least_6_char);
            return false;
        }
        String confirmPwd = etConfirmPwd.getText().toString().trim();
        if (!userInfo.Password.equals(confirmPwd)) {
            ToastMgr.showTextToast(this, 0, R.string.comfirm_password_different);
            return false;
        }

        userInfo.UserName = etName.getText().toString().trim();
        if (userInfo.UserName.isEmpty()) {
            ToastMgr.showTextToast(this, 0, R.string.input_username);
            return false;
        }

        if (rbFemale.isChecked()) {
            userInfo.Gender = "F";
        } else if (rbMale.isChecked()) {
            userInfo.Gender = "M";
        }

        String height = etHeight.getText().toString().trim();
        if (height.isEmpty()) {
            ToastMgr.showTextToast(this, 0, R.string.input_height);
            return false;
        }
        userInfo.High = Integer.parseInt(height);
        if (userInfo.High < 50 || userInfo.High > 300) {
            ToastMgr.showTextToast(this, 0, R.string.input_correct_height);
            return false;
        }

        String weight = etWeight.getText().toString().trim();
        if (weight.isEmpty()) {
            ToastMgr.showTextToast(this, 0, R.string.input_weight);
            return false;
        }
        userInfo.Weight = Double.parseDouble(weight);
        if (userInfo.Weight < 3 || userInfo.Weight > 200) {
            ToastMgr.showTextToast(this, 0, R.string.input_correct_weight);
            return false;
        }
        return true;
    }


    private void httpHelp(final JSONObject jsonObject, final String u) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL("http://test.ansobuy.cn:9000/" + u);//暂时先用测试URL，正式上线时切换到正式URL
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setDoInput(true);
                    connection.setDoOutput(true);
                    connection.setRequestMethod("POST");
                    connection.setUseCaches(false);
                    connection.setConnectTimeout(5000);
                    connection.setRequestProperty("Connection", "Keep-Alive");
                    connection.setRequestProperty("Content-length", jsonObject.toString().length() + "");
                    connection.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
                    connection.connect();

                    DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());
                    outputStream.writeBytes(jsonObject.toString());
                    outputStream.flush();
                    outputStream.close();

                    BufferedReader inputStream = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    String line;
                    StringBuffer buffer = new StringBuffer();
                    while ((line = inputStream.readLine()) != null) {
                        buffer.append(line);
                    }
                    try {
                        JSONObject value=new JSONObject(buffer.toString());
                        inputStream.close();
                        connection.disconnect();
                        Message message = Message.obtain();
                        message.obj=value;
                        handler.sendMessage(message);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

}
