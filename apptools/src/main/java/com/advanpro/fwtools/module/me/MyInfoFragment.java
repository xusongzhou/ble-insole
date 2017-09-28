package com.advanpro.fwtools.module.me;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.InputType;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.advanpro.fwtools.Constant;
import com.advanpro.fwtools.R;
import com.advanpro.fwtools.common.base.BaseFragment;
import com.advanpro.fwtools.common.manager.ToastMgr;
import com.advanpro.fwtools.common.util.FileUtils;
import com.advanpro.fwtools.common.util.PreferencesUtils;
import com.advanpro.fwtools.common.util.StringUtils;
import com.advanpro.fwtools.common.util.UiUtils;
import com.advanpro.fwtools.common.view.ClearEditText;
import com.advanpro.fwtools.common.view.CycleWheelView;
import com.advanpro.fwtools.common.view.TitleBar;
import com.advanpro.fwtools.module.BaseActivity;
import com.advanpro.fwtools.module.ClipActivity;
import com.advanpro.fwtools.module.MainActivity;
import com.advanpro.fwtools.module.SignInActivity;
import com.advanpro.ascloud.ASCloud;
import com.advanpro.ascloud.CloudCallback;
import com.advanpro.ascloud.CloudException;
import com.advanpro.ascloud.CloudMsg;
import com.advanpro.ascloud.FileCallback;
import com.advanpro.ascloud.UserInfo;
import com.advanpro.aswear.ASWear;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MyInfoFragment extends BaseFragment<MainActivity> implements View.OnClickListener {

    private Button updatePassword, logout;
    private TextView account, integral;
    private ClearEditText nickname, height, weight, birthday;
    private TitleBar titleBar;
    private ImageView ivHead;
    private File faceFileTemp;
    private RadioButton rbMale;
    private RadioButton rbFemale;
    private CycleWheelView numpicker, numpicker_month, numpicker_day;
    private List<String> pickerLabels;
    private List<String> pickerLabels_month;
    private List<String> pickerLabels_day;
    private Dialog selectDialog;
    private int currentEdit;
    private int[] cacheValues = new int[3];
    private boolean isUpdate = true;//未修改
    private boolean updateState = true;//修改状态

//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_my_info);
//        assignViews();
//        initViews();
//    }


    @Override
    protected void assignViews() {
        titleBar = (TitleBar) rootView.findViewById(R.id.title_bar);
        ivHead = (ImageView) rootView.findViewById(R.id.iv_head);
        ivHead.setEnabled(false);
        updatePassword = (Button) rootView.findViewById(R.id.updatePassword);
        logout = (Button) rootView.findViewById(R.id.logout);
        integral = (TextView) rootView.findViewById(R.id.edit_integral);
        account = (TextView) rootView.findViewById(R.id.edit_account);
        nickname = (ClearEditText) rootView.findViewById(R.id.edit_nickname);
        height = (ClearEditText) rootView.findViewById(R.id.edit_height);
        weight = (ClearEditText) rootView.findViewById(R.id.edit_weight);
        birthday = (ClearEditText) rootView.findViewById(R.id.edit_birthday);
        birthday.setInputType(InputType.TYPE_NULL);
        rbMale = (RadioButton) rootView.findViewById(R.id.rb_male);
        rbFemale = (RadioButton) rootView.findViewById(R.id.rb_female);
    }

    @Override
    protected View getRootView(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(R.layout.activity_my_info, container, false);
    }

    @Override
    protected void initViews() {
        faceFileTemp = new File(ASWear.getFaceImageDir(), "face.jpg");
        initTitleBar();
        initSelectDialog();
        setIntegral();
        setHeadImage();
        updatePassword.setOnClickListener(this);
        logout.setOnClickListener(this);
        ivHead.setOnClickListener(this);
        if (ASCloud.userInfo.Account.contains("&qq")) {
            PreferencesUtils.putString(Constant.SP_LAST_SIGN_IN_USER, "");
            account.setText("QQ登录");
        } else if (ASCloud.userInfo.Account.contains("&weixin")) {
            PreferencesUtils.putString(Constant.SP_LAST_SIGN_IN_USER, "");
            account.setText("微信登录");
        } else {
            account.setText(ASCloud.userInfo.Account);
        }
        birthday.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    currentEdit = v.getId();
                    setNumPickerData(currentEdit);
                    selectDialog.show();
                }
            }
        });
        rbFemale.setEnabled(false);
        rbMale.setEnabled(false);
        birthday.setOnClickListener(this);
        nickname.setText(ASCloud.userInfo.UserName);
        height.setText(ASCloud.userInfo.High + "");
        height.setInputType(InputType.TYPE_CLASS_NUMBER);
        weight.setText(ASCloud.userInfo.Weight + "");
        weight.setInputType(InputType.TYPE_CLASS_NUMBER);
        //birthday.setText(ASCloud.userInfo.Birthday.length() > 10 ? ASCloud.userInfo.Birthday.substring(0, 10) : ASCloud.userInfo.Birthday);
        birthday.setText(ASCloud.userInfo.getBirthYear() + "");        
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

    @Override
    public void onResume() {
        super.onResume();
        if (updateState) {
            if ("M".equals(ASCloud.userInfo.Gender)) {
                rbMale.setChecked(true);
            } else {
                rbFemale.setChecked(true);
            }            
        }
    }

    private void initTitleBar() {
        titleBar.setTitle(R.string.personal_info);
        titleBar.setEndText("编辑");
        titleBar.setEndTextViewVisible(true);
        //titleBar.setStartImageButtonVisible(true);
        titleBar.setOnMenuClickListener(new TitleBar.OnMenuClickListener() {
            @Override
            public void onMenuClick(View v) {
                switch (v.getId()) {
                    case R.id.btn_start:

                        //finish();
                        break;

                    case R.id.tv_end:
                        if (updateState) {
                            updateState = false;
                            titleBar.setEndText("保存");
                            nickname.setEnabled(true);
                            nickname.setFocusableInTouchMode(true);
                            height.setEnabled(true);
                            height.setFocusableInTouchMode(true);
                            weight.setEnabled(true);
                            weight.setFocusableInTouchMode(true);
                            birthday.setEnabled(true);
                            birthday.setFocusable(false);
                            ivHead.setEnabled(true);
                            rbMale.setEnabled(true);
                            rbFemale.setEnabled(true);
                        } else {
                            if (checkerData()) {
                                updateState = true;
                                titleBar.setEndText("编辑");
                                nickname.setEnabled(false);
                                nickname.setFocusable(false);
                                height.setEnabled(false);
                                height.setFocusable(false);
                                weight.setEnabled(false);
                                weight.setFocusable(false);
                                birthday.setEnabled(false);
                                birthday.setFocusable(false);
                                ivHead.setEnabled(false);
                                rbMale.setEnabled(false);
                                rbFemale.setEnabled(false);
                            }
                        }
                        break;
                }
            }
        });
    }


    //检查数据，然后提交服务器
    private boolean checkerData() {
        if (nickname.getText().toString().isEmpty()) {
            ToastMgr.showTextToast(getActivity(), 0, R.string.input_username);
            return false;
        } else if (isUpdate) {
            isUpdate = ASCloud.userInfo.UserName.equals(nickname.getText().toString());
        }

        if (isUpdate) {
            isUpdate = ASCloud.userInfo.Gender.equals(rbMale.isChecked() ? "M" : "F");
        }

        if (height.getText().toString().isEmpty()) {
            ToastMgr.showTextToast(getActivity(), 0, R.string.input_height);
            return false;
        } else if (Double.parseDouble(height.getText().toString()) < 50 || Double.parseDouble(height.getText().toString()) > 300) {
            ToastMgr.showTextToast(getActivity(), 0, R.string.input_correct_height);
            return false;
        } else if (isUpdate) {
            isUpdate = ASCloud.userInfo.High == Double.parseDouble(height.getText().toString().equals("") ? "0" : height.getText().toString());
        }

        if (weight.getText().toString().isEmpty()) {
            ToastMgr.showTextToast(getActivity(), 0, R.string.input_weight);
            return false;
        } else if (Double.parseDouble(weight.getText().toString()) < 3 || Double.parseDouble(weight.getText().toString()) > 200) {
            ToastMgr.showTextToast(getActivity(), 0, R.string.input_correct_weight);
            return false;
        } else if (isUpdate) {
            isUpdate = ASCloud.userInfo.Weight == Double.parseDouble(weight.getText().toString().equals("") ? "0" : weight.getText().toString());
        }
        if (birthday.getText().toString().isEmpty()) {
            ToastMgr.showTextToast(getActivity(), 0, "请输入出生日期");
            return false;
        } else if (isUpdate) {
            isUpdate = ASCloud.userInfo.Birthday.equals(birthday.getText().toString().equals("") ? "0" : birthday.getText().toString() + "-01-01");
        }
        if (!isUpdate) {
            uploadUserInfo();
        }
        return true;
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.logout:
                ASCloud.logout();
                ASCloud.userInfo = new UserInfo();//清除已加载的用户信息
                ASWear.putPreferencesString("app_user", "");
                Intent intent = new Intent(getActivity(), SignInActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                getActivity().finish();
                BaseActivity.getActivity(MainActivity.class.getName()).finish();
                break;
            case R.id.updatePassword:
                startActivity(new Intent(getActivity(), ChangePwdActivity.class));
                break;
            case R.id.iv_head:
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                String[] items = new String[]{getString(R.string.take_photo), getString(R.string.select_from_album)};
                builder.setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            Intent openCameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            openCameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(faceFileTemp));
                            startActivityForResult(openCameraIntent, Constant.REQUEST_TAKE_PHOTO);
                        } else if (which == 1) {
                            Intent openAlbumIntent = new Intent(Intent.ACTION_PICK);
                            openAlbumIntent.setType("image/*");
                            startActivityForResult(openAlbumIntent, Constant.REQUEST_SELECT_FROM_ALBUM);
                        }
                    }
                });
                builder.show();
                break;
            case R.id.edit_birthday:
                currentEdit = v.getId();
                setNumPickerData(currentEdit);
                selectDialog.show();
                break;
            case R.id.tv_cancel:
                selectDialog.dismiss();
                break;
            case R.id.tv_ok:
                selectDialog.dismiss();
                //birthday.setText(numpicker.getSelectLabel().split("年")[0] + "-" + numpicker_month.getSelectLabel().split("月")[0] + "-" + numpicker_day.getSelectLabel().split("日")[0]);
                birthday.setText(numpicker.getSelectLabel().substring(0,4));
                break;
            default:
                break;
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == -1) {
            switch (requestCode) {
                case Constant.REQUEST_TAKE_PHOTO:
                case Constant.REQUEST_SELECT_FROM_ALBUM:
                    Intent intent = new Intent(getActivity(), ClipActivity.class);
                    String path;
                    if (requestCode == Constant.REQUEST_TAKE_PHOTO)
                        path = faceFileTemp.getAbsolutePath();
                    else path = StringUtils.getImagePath(getActivity(), data.getData());
                    intent.putExtra(Constant.EXTRA_PATH, path);
                    startActivityForResult(intent, Constant.REQUEST_HEAD_IMAGE_CLIP);
                    break;
                case Constant.REQUEST_HEAD_IMAGE_CLIP:
                    ivHead.setImageBitmap(FileUtils.getBitmap(data.getStringExtra(Constant.EXTRA_PATH)));
                    break;
            }
        }
        //不管是否剪裁，删除拍照的图片
        if (requestCode == Constant.REQUEST_HEAD_IMAGE_CLIP) faceFileTemp.delete();
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void initNumPicker() {
        pickerLabels = new ArrayList<>();
        pickerLabels_month = new ArrayList<>();
        pickerLabels_day = new ArrayList<>();
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

        numpicker_month.setLabels(pickerLabels_month);
        try {
            numpicker_month.setWheelSize(7);
        } catch (CycleWheelView.CycleWheelViewException e) {
            e.printStackTrace();
        }
        numpicker_month.setCycleEnable(true);
        numpicker_month.setSelection(30);
        numpicker_month.setItemSpace(3);
        numpicker_month.setAlphaGradual(0.6f);
        numpicker_month.setDivider(getResources().getColor(R.color.content_text), 2);
        numpicker_month.setSolid(Color.TRANSPARENT, Color.TRANSPARENT);
        numpicker_month.setLabelColor(Color.parseColor("#777777"));
        numpicker_month.setLabelSelectColor(getResources().getColor(R.color.content_text));


        numpicker_day.setLabels(pickerLabels_day);
        try {
            numpicker_day.setWheelSize(7);
        } catch (CycleWheelView.CycleWheelViewException e) {
            e.printStackTrace();
        }
        numpicker_day.setCycleEnable(true);
        numpicker_day.setSelection(30);
        numpicker_day.setItemSpace(3);
        numpicker_day.setAlphaGradual(0.6f);
        numpicker_day.setDivider(getResources().getColor(R.color.content_text), 2);
        numpicker_day.setSolid(Color.TRANSPARENT, Color.TRANSPARENT);
        numpicker_day.setLabelColor(Color.parseColor("#777777"));
        numpicker_day.setLabelSelectColor(getResources().getColor(R.color.content_text));

    }

    private void initSelectDialog() {
        selectDialog = new Dialog(getActivity(), R.style.DialogStyle);
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
        View view = View.inflate(getActivity(), R.layout.dialog_num_picker, null);
        TextView tvCancel = (TextView) view.findViewById(R.id.tv_cancel);
        TextView tvOk = (TextView) view.findViewById(R.id.tv_ok);
        numpicker = (CycleWheelView) view.findViewById(R.id.picker1);
        numpicker_month = (CycleWheelView) view.findViewById(R.id.picker2);
        numpicker_day = (CycleWheelView) view.findViewById(R.id.picker3);
        tvCancel.setOnClickListener(this);
        tvOk.setOnClickListener(this);
        initNumPicker();
        return view;
    }

    private void setNumPickerData(int currentEdit) {
        int start = 0, end = 0, selected = 0, start_month = 0, end_month = 0, selected_month = 0, start_day = 0, end_day = 0, selected_day = 0;
        switch (currentEdit) {
            case R.id.edit_birthday:
                start = 1900;
                end = Calendar.getInstance().get(Calendar.YEAR);
                selected = cacheValues[0] - start;
                start_month = 1;
                end_month = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
                selected_month = cacheValues[0] - start_month;
                start_day = 1;
                end_day = 31;
                selected_day = cacheValues[0] - start_day;
                break;
        }
        pickerLabels.clear();

        for (int i = start; i <= end; i++) {
            pickerLabels.add(String.valueOf(i) + "年");
        }
        numpicker.setSelection(selected);
        numpicker.setLabels(pickerLabels);

        pickerLabels_month.clear();
        for (int i = start_month; i < end_month; i++) {
            pickerLabels_month.add(i > 10 ? String.valueOf(i) + "月" : "0" + String.valueOf(i) + "月");
        }
        numpicker_month.setSelection(selected_month);
        numpicker_month.setLabels(pickerLabels_month);

        pickerLabels_day.clear();
        for (int i = start_day; i <= end_day; i++) {
            pickerLabels_day.add(i > 10 ? String.valueOf(i) + "日" : "0" + String.valueOf(i) + "日");
        }
        numpicker_day.setSelection(selected_day);
        numpicker_day.setLabels(pickerLabels_day);

    }

    //将信息提交到云端
    private void uploadUserInfo() {
        final String username = nickname.getText().toString().trim();
        if (username.isEmpty()) {
            ToastMgr.showTextToast(getActivity(), 0, R.string.input_username);
            return;
        }
        // 提交资料
        CloudMsg req = new CloudMsg("/user/update");
        req.put("UserID", ASCloud.userInfo.ID);
        req.put("UserName", username);
        req.put("Birthday", birthday.getText().toString() + "-01-01");
        req.put("Gender", rbMale.isChecked() ? "M" : "F");
        req.put("Email", ASCloud.userInfo.Email);
        req.put("Mobile", ASCloud.userInfo.Mobile);
        req.put("Country", ASCloud.userInfo.Country);
        req.put("Province", ASCloud.userInfo.Province);
        req.put("City", ASCloud.userInfo.City);
        req.put("High", Double.parseDouble(height.getText().toString().equals("") ? 0 + "" : height.getText().toString()));
        req.put("Weight", Double.parseDouble(weight.getText().toString().equals("") ? 0 + "" : weight.getText().toString()));
        ASCloud.sendMsg(req, new CloudCallback() {
            @Override
            public void success(CloudMsg msg) {
                ToastMgr.showTextToast(getActivity(), 0, getString(R.string.change_success));
                ASCloud.userInfo.UserName = username;
                ASCloud.userInfo.Birthday = birthday.getText().toString() + "-01-01";
                ASCloud.userInfo.High = Double.parseDouble(height.getText().toString().equals("") ? 0 + "" : height.getText().toString());
                ASCloud.userInfo.Weight = Double.parseDouble(weight.getText().toString());
                ASCloud.userInfo.Gender = rbMale.isChecked() ? "M" : "F";
                ASCloud.userInfo.save();
            }

            @Override
            public void error(CloudException e) {
                ToastMgr.showTextToast(getActivity(), 0, e.msg);
            }
        });
    }

    private void setIntegral() {
        CloudMsg req = new CloudMsg("/user/getIntegral");
        req.put("userId", ASCloud.userInfo.ID);
        ASCloud.sendMsg(req, new CloudCallback() {
            @Override
            public void success(CloudMsg cloudMsg) {
                try {
                    integral.setText(String.valueOf(cloudMsg.getLong("integral")));
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
