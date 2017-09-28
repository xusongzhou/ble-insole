package com.advanpro.fwtools.module.me;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputFilter;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.advanpro.fwtools.Constant;
import com.advanpro.fwtools.R;
import com.advanpro.fwtools.common.manager.ToastMgr;
import com.advanpro.fwtools.common.util.FileUtils;
import com.advanpro.fwtools.common.util.StringUtils;
import com.advanpro.fwtools.common.util.UiUtils;
import com.advanpro.fwtools.common.view.ClearEditText;
import com.advanpro.fwtools.common.view.CycleWheelView;
import com.advanpro.fwtools.common.view.RoundProgressBar;
import com.advanpro.fwtools.common.view.TitleBar;
import com.advanpro.fwtools.entity.StringLengthFilter;
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

/**
 * Created by zeng on 2016/4/14.
 * 用户资料
 */
public class UserInfoActivity extends BaseActivity implements View.OnClickListener {
    private TitleBar titleBar;
    private ImageView ivHead;
    private TextView tvAccount;
    private ClearEditText etUsername;
	private TextView tvUsername;
	private TextView tvGender;
	private TextView tvBirthyear;
	private TextView tvHeight;
	private TextView tvWeight;
	private RoundProgressBar ringGender;
	private RoundProgressBar ringBirthyear;
	private RoundProgressBar ringHeight;
	private RoundProgressBar ringWeight;
	private Button btnChangeInfo;
	private Button btnChangePwd;
	private boolean editable;
	private Dialog selectDialog;
	private int currentEdit;
	private CycleWheelView numpicker;
	private int[] cacheValues = new int[3];
	private List<String> pickerLabels;
    private File faceFileTemp;

	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);
        assignViews();
        initViews();
    }    

    private void assignViews() {
        titleBar = (TitleBar) findViewById(R.id.title_bar);
        ivHead = (ImageView) findViewById(R.id.iv_head);
        tvAccount = (TextView) findViewById(R.id.tv_account);
        etUsername = (ClearEditText) findViewById(R.id.et_username);
		tvUsername = (TextView) findViewById(R.id.tv_username);
		tvGender = (TextView) findViewById(R.id.tv_gender);
		tvBirthyear = (TextView) findViewById(R.id.tv_birthyear);
		tvHeight = (TextView) findViewById(R.id.tv_height);
		tvWeight = (TextView) findViewById(R.id.tv_weight);
		ringGender = (RoundProgressBar) findViewById(R.id.ring_gender);
		ringBirthyear = (RoundProgressBar) findViewById(R.id.ring_birthyear);
		ringHeight = (RoundProgressBar) findViewById(R.id.ring_height);
		ringWeight = (RoundProgressBar) findViewById(R.id.ring_weight);
		btnChangeInfo = (Button) findViewById(R.id.btn_change_info);
		btnChangePwd = (Button) findViewById(R.id.btn_change_pwd);		
    }

    private void initViews() {
        faceFileTemp = new File(ASWear.getFaceImageDir(), "face.jpg");
        initTitleBar();		
		initSelectDialog();
		initRing(ringGender, 0xFF94E0F8);
		initRing(ringBirthyear, 0xFFFFD441);
		initRing(ringHeight, 0xFFCBFFA2);
		initRing(ringWeight, 0xFFD57C91);
        ivHead.setOnClickListener(this);
        ringBirthyear.setOnClickListener(this);
        ringHeight.setOnClickListener(this);
        ringWeight.setOnClickListener(this);
		btnChangePwd.setOnClickListener(this);
		btnChangeInfo.setOnClickListener(this);
		InputFilter[] filters = {new StringLengthFilter(20)};
		etUsername.setFilters(filters);
		tvAccount.setText(ASCloud.userInfo.Account);
		tvUsername.setText(ASCloud.userInfo.UserName);
		tvGender.setText("F".equals(ASCloud.userInfo.Gender) ? getString(R.string.female) :
				getString(R.string.male));
		cacheValues[0] = ASCloud.userInfo.getBirthYear();
		cacheValues[1] = (int) ASCloud.userInfo.High;
		cacheValues[2] = (int) ASCloud.userInfo.Weight;
		tvBirthyear.setText(String.valueOf(cacheValues[0]));
		tvHeight.setText(String.valueOf(cacheValues[1]));
		tvWeight.setText(String.valueOf(cacheValues[2]));
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

	private void initNumPicker() {
		pickerLabels = new ArrayList<>();
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
		//numpicker = (CycleWheelView) view.findViewById(R.id.num_picker);
		tvCancel.setOnClickListener(this);
		tvOk.setOnClickListener(this);
		initNumPicker();
		return view;
	}
	
	private void initRing(RoundProgressBar ring, int color) {
		ring.setDotRadius(0);
		ring.setDefaultRoundColor(color);
		ring.setDefaultRoundWidth(UiUtils.dip2px(1));
	}
	
    private void initTitleBar() {
        titleBar.setTitle(R.string.personal_info);
        titleBar.setStartImageButtonVisible(true);
        titleBar.setEndText(R.string.logout);
        titleBar.setEndTextViewVisible(true);
        titleBar.setOnMenuClickListener(new TitleBar.OnMenuClickListener() {
            @Override
            public void onMenuClick(View v) {
                switch(v.getId()) {
                    case R.id.btn_start:		
                        finish();
                		break;
                    case R.id.tv_end://注销                        
                        ASCloud.logout();
                        ASCloud.userInfo = new UserInfo();//清除已加载的用户信息
						ASWear.putPreferencesString("app_user", "");
                        Intent intent = new Intent(UserInfoActivity.this, SignInActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                        BaseActivity.getActivity(MainActivity.class.getName()).finish();
                        break;					
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case Constant.REQUEST_TAKE_PHOTO:
                case Constant.REQUEST_SELECT_FROM_ALBUM:
                    Intent intent = new Intent(this, ClipActivity.class);
                    String path;
                    if (requestCode == Constant.REQUEST_TAKE_PHOTO) path = faceFileTemp.getAbsolutePath();
                    else path = StringUtils.getImagePath(this, data.getData());
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
    
    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.iv_head:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
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
			case R.id.ring_birthyear:
			case R.id.ring_height:
			case R.id.ring_weight:
				if (editable) {
					currentEdit = v.getId();
					setNumPickerData(currentEdit);
					selectDialog.show();
				}				
				break;
			case R.id.btn_change_info:
                //从不可编辑状态进来，将view状态更改为可编辑，否则提交信息到云端
				if (!editable) {
					btnChangeInfo.setText(getString(R.string.save));
					etUsername.setText(tvUsername.getText());
					etUsername.setVisibility(View.VISIBLE);
					tvUsername.setVisibility(View.INVISIBLE);
				} else {
					uploadUserInfo();
				}
				editable = true;											
				break;
			case R.id.btn_change_pwd:
                startActivity(new Intent(this, ChangePwdActivity.class));
				break;
			case R.id.tv_cancel:
				selectDialog.dismiss();
				break;
			case R.id.tv_ok:
				selectDialog.dismiss();
				switch(currentEdit) {
					case R.id.ring_birthyear:
						tvBirthyear.setText(numpicker.getSelectLabel());
						cacheValues[0] = Integer.parseInt(numpicker.getSelectLabel());
						break;
					case R.id.ring_height:
						tvHeight.setText(numpicker.getSelectLabel());
						cacheValues[1] = Integer.parseInt(numpicker.getSelectLabel());
						break;
					case R.id.ring_weight:
						tvWeight.setText(numpicker.getSelectLabel());
						cacheValues[2] = Integer.parseInt(numpicker.getSelectLabel());
						break;
				}
				break;
        }
    }

	//将信息提交到云端
	private void uploadUserInfo() {	
		final String username = etUsername.getText().toString().trim();
		if (username.isEmpty()) {
			ToastMgr.showTextToast(this, 0, R.string.input_username);
			editable = true;
			return;
		}
		// 提交资料
		CloudMsg req = new CloudMsg("/user/update");
		req.put("UserID", ASCloud.userInfo.ID);
		req.put("UserName", username);
		req.put("Birthday", cacheValues[0] + "-01-01");
		req.put("Gender", ASCloud.userInfo.Gender);
		req.put("Email", ASCloud.userInfo.Email);
		req.put("Mobile", ASCloud.userInfo.Mobile);
		req.put("Country", ASCloud.userInfo.Country);
		req.put("Province", ASCloud.userInfo.Province);
		req.put("City", ASCloud.userInfo.City);
		req.put("High", cacheValues[1]);
		req.put("Weight", cacheValues[2]);		
		btnChangeInfo.setEnabled(false);
		ASCloud.sendMsg(req, new CloudCallback() {
			@Override
			public void success(CloudMsg msg) {
				ToastMgr.showTextToast(UserInfoActivity.this, 0, getString(R.string.change_success));
				ASCloud.userInfo.UserName = username;
				ASCloud.userInfo.Birthday = cacheValues[0] + "-01-01";
				ASCloud.userInfo.High = cacheValues[1];
				ASCloud.userInfo.Weight = cacheValues[2];
				ASCloud.userInfo.save();
				btnChangeInfo.setEnabled(true);
				tvUsername.setText(username);
				btnChangeInfo.setText(getString(R.string.change_info));
				etUsername.setVisibility(View.INVISIBLE);
				tvUsername.setVisibility(View.VISIBLE);
				editable = false;
			}

			@Override
			public void error(CloudException e) {
				ToastMgr.showTextToast(UserInfoActivity.this, 0, e.msg);				
				btnChangeInfo.setEnabled(true);
			}
		});
	}
	
	private void setNumPickerData(int currentEdit) {		
		int start = 0, end = 0, selected = 0;
		switch(currentEdit) {
			case R.id.ring_birthyear:
				start = 1900;
				end = Calendar.getInstance().get(Calendar.YEAR);
				selected = cacheValues[0] - start;
				break;
			case R.id.ring_height:
				start = 50;
				end = 300;
				selected = cacheValues[1] - start;
				break;
			case R.id.ring_weight:
				start = 3;
				end = 200;
				selected = cacheValues[2] - start;
				break;
		}
		pickerLabels.clear();
		for (int i = start; i <= end; i++) {
			pickerLabels.add(String.valueOf(i));
		}
		numpicker.setSelection(selected);
		numpicker.setLabels(pickerLabels);
	}
}
