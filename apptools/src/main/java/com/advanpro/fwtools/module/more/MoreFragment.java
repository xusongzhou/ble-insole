package com.advanpro.fwtools.module.more;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import com.advanpro.fwtools.Constant;
import com.advanpro.fwtools.MyApplication;
import com.advanpro.fwtools.R;
import com.advanpro.fwtools.ble.BleDevice;
import com.advanpro.fwtools.ble.BleManager;
import com.advanpro.fwtools.ble.DeviceMgr;
import com.advanpro.fwtools.common.base.BaseFragment;
import com.advanpro.fwtools.common.manager.ToastMgr;
import com.advanpro.fwtools.common.util.PreferencesUtils;
import com.advanpro.fwtools.common.view.ArrowItemView;
import com.advanpro.fwtools.module.MainActivity;

/**
 * Created by zengfs on 2016/1/14.
 * 更新Tab
 */
public class MoreFragment extends BaseFragment<MainActivity> implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {
    private ArrowItemView itemDeviceMgr;
    private ArrowItemView itemAbout;
    private ArrowItemView itemRunMusic;
    private ArrowItemView itemVoice;
    private ArrowItemView itemOta;
    private ArrowItemView itemClearDevData;
    private ArrowItemView itemRestartDev;
    private ProgressDialog progressDialog; 
    private String toastMsg;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
	protected View getRootView(LayoutInflater inflater, ViewGroup container) {
		return inflater.inflate(R.layout.frag_more, container, false);
	}

	@Override
	protected void assignViews() {
        itemDeviceMgr = (ArrowItemView) rootView.findViewById(R.id.item_device_mgr);
        itemAbout = (ArrowItemView) rootView.findViewById(R.id.item_about);
        itemRunMusic = (ArrowItemView) rootView.findViewById(R.id.item_run_music);
        itemVoice = (ArrowItemView) rootView.findViewById(R.id.item_voice);
        itemOta = (ArrowItemView) rootView.findViewById(R.id.item_ota);
        itemClearDevData = (ArrowItemView) rootView.findViewById(R.id.item_clear_dev_data);
        itemRestartDev = (ArrowItemView) rootView.findViewById(R.id.item_restart_dev);
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constant.ACTION_CLEAR_DEVICE_DATA);
        filter.addAction(Constant.ACTION_SEND_RESTART_CMD);
        getActivity().registerReceiver(receiver, filter);
	}

	@Override
	protected void initViews() {
        itemDeviceMgr.setOnClickListener(this);
        itemRunMusic.setOnClickListener(this);
        itemOta.setOnClickListener(this);
        itemAbout.setOnClickListener(this);
        itemClearDevData.setOnClickListener(this);
        itemRestartDev.setOnClickListener(this);
        itemVoice.setSwitchTintColor(0xFFFF4F00);
        itemVoice.setSwitchCheckedImmediately(MyApplication.isVoiceEnable);
        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setMessage(getString(R.string.clearing_data));
        progressDialog.setCancelable(false);
	}

    @Override
    public void onResume() {
        super.onResume();
        //底部Tab切换时，switch的setChecked方法会被调用，暂时不知原因，使用此方法重新设置状态
        itemVoice.setSwitchCheckedImmediately(MyApplication.isVoiceEnable);
        itemVoice.setOnSwitchCheckedChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        itemVoice.setOnSwitchCheckedChangeListener(null);
    }

    @Override
    public void onDestroy() {
        getActivity().unregisterReceiver(receiver);
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.item_device_mgr:
                startActivity(new Intent(getActivity(), DeviceManagerActivity.class));
        		break;
            case R.id.item_about:
                startActivity(new Intent(getActivity(), AboutActivity.class));
                break;
            case R.id.item_run_music:
                startActivity(new Intent(getActivity(), MusicManagerActivity.class));
                break;
            case R.id.item_ota:
                startActivity(new Intent(getActivity(), OtaActivity.class));
                break;
            case R.id.item_clear_dev_data:
                toastMsg = "";
                if (DeviceMgr.getConnectedCount() == 0) {
                    ToastMgr.showTextToast(getContext(), 0, R.string.device_disconnected);
                    return;
                }
                new AlertDialog.Builder(getActivity()).setMessage(R.string.clear_warn_msg)
                        .setNegativeButton(R.string.cancel, null)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (!progressDialog.isShowing()) progressDialog.show();
                                BleManager.INSTANCE.clearAllDevicesData();
                            }
                        }).show();                
                break;
            case R.id.item_restart_dev:
                toastMsg = "";
                if (DeviceMgr.getConnectedCount() == 0) {
                    ToastMgr.showTextToast(getContext(), 0, R.string.device_disconnected);
                    return;
                }
                new AlertDialog.Builder(getActivity()).setMessage(R.string.restart_warn_msg)
                        .setNegativeButton(R.string.cancel, null)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                BleManager.INSTANCE.restartAllDevices();
                            }
                        }).show();
                break;
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        PreferencesUtils.putBoolean(Constant.SP_VOICE_ENABLE, isChecked);
		MyApplication.isVoiceEnable = isChecked;
    }
    
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int resultId = intent.getIntExtra(Constant.EXTRA_RESULT_ID, Constant.RESULT_FAILE);
            BleDevice device = intent.getParcelableExtra(Constant.EXTRA_VALUE);
            String append;
            if (Constant.ACTION_CLEAR_DEVICE_DATA.equals(intent.getAction())) {
                if (progressDialog.isShowing()) progressDialog.dismiss();
                append = device.isLeft ? getString(R.string.left) : getString(R.string.right);
                append += resultId == Constant.RESULT_OK ? getString(R.string.clear_success) : 
                        getString(R.string.clear_faile);
                if (!toastMsg.isEmpty()) toastMsg += ", ";
                toastMsg += append;
                ToastMgr.showTextToast(context, 0, toastMsg);
            } else if (Constant.ACTION_SEND_RESTART_CMD.equals(intent.getAction())) {
                append = resultId == Constant.RESULT_OK ? getString(R.string.send_restart_success) :
                        getString(R.string.send_restart_faile);
                append = append.replace("?", device.isLeft ? getString(R.string.left) : getString(R.string.right));
                if (!toastMsg.isEmpty()) toastMsg += ", ";
                toastMsg += append;
                ToastMgr.showTextToast(context, 0, toastMsg);
            }
        }
    };
}
