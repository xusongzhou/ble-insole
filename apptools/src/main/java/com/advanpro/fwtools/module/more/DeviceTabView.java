package com.advanpro.fwtools.module.more;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.advanpro.fwtools.R;
import com.advanpro.fwtools.ble.BleDevice;
import com.advanpro.fwtools.ble.DeviceMgr;
import com.advanpro.fwtools.common.manager.ToastMgr;
import com.advanpro.fwtools.entity.State;

/**
 * Created by zengfs on 2016/2/9.
 * 设备管理的头部，显示设备连接状态、电量，进行解绑操作
 */
public class DeviceTabView {
	private View rootView;
	private boolean isLeft;
	private Context context;
	private ImageView ivProgress;
	private ImageButton btnUnbind;
	private ImageView ivFoot;
	private BatteryView batteryView;
	private OnUnbindClickListener listener;
	private TextView tvFirmwareError;
	
	public interface OnUnbindClickListener {
		void onUnbindClick(boolean isLeft);
	}	
	
	public DeviceTabView(boolean isLeft, Context context) {
		this.isLeft = isLeft;
		this.context = context;
		assignViews();
		initViews();
	}

	/**
	 * 解绑按键监听
	 */
	public void setOnUnbindClickListener(OnUnbindClickListener listener) {
		this.listener = listener;
	}
	
	private void assignViews() {
		rootView = View.inflate(context, isLeft ? R.layout.tab_left_device : R.layout.tab_right_device, null);
		ivProgress = (ImageView) rootView.findViewById(R.id.iv_progress);        
		btnUnbind = (ImageButton) rootView.findViewById(R.id.btn_unbind);
		ivFoot = (ImageView) rootView.findViewById(R.id.iv_foot);
		batteryView = (BatteryView) rootView.findViewById(R.id.battery_view);
		tvFirmwareError = (TextView) rootView.findViewById(R.id.tv_firmware_error);
	}
	
	private void initViews() {
        ((AnimationDrawable) ivProgress.getBackground()).start();//进度条转动起来
		btnUnbind.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
                if (State.runState == State.RunState.STOP) {
                    setBound(false);
                    if (listener != null) {
                        listener.onUnbindClick(isLeft);
                    }
                } else {
                    ToastMgr.showTextToast(context, 0, R.string.please_stop_running_first);
                }				
			}
		});
		updateView(State.ConnectionState.DISCONNECTED);
		setBound(false);
	}
	
	public View getRootView() {
		return rootView;
	}

	/**
	 * 设置绑定状态，显示或隐藏解绑按键
	 */
	public void setBound(boolean isBound) {
		btnUnbind.setVisibility(isBound ? View.VISIBLE : View.INVISIBLE);
		if (!isBound) updateView(State.ConnectionState.DISCONNECTED);
	}
	
	/**
	 * 根据连接状态更新界面
	 * @param state 连接状态：DISCONNECTED，CONNECTING，CONNECTED。 
	 */
	public void updateView(State.ConnectionState state) {			
		if (state == State.ConnectionState.DISCONNECTED || state == State.ConnectionState.CONNECTING) {
			ivProgress.setVisibility(state == State.ConnectionState.DISCONNECTED ? View.INVISIBLE : View.VISIBLE);			
			ivFoot.setBackgroundResource(isLeft ? R.drawable.left_foot_dark : R.drawable.right_foot_dark);
			batteryView.setVisibility(View.INVISIBLE);
            setBattery(0);
            tvFirmwareError.setVisibility(View.INVISIBLE);
		} else if (state == State.ConnectionState.CONNECTED) {
			ivProgress.setVisibility(View.INVISIBLE);
            tvFirmwareError.setVisibility(View.INVISIBLE);
			ivFoot.setBackgroundResource(isLeft ? R.drawable.left_foot_bright : R.drawable.right_foot_bright);
			batteryView.setVisibility(View.VISIBLE);            
            BleDevice device = DeviceMgr.getBoundDevice(isLeft);
            if (device != null) {
                setBattery(device.battery);
                if (State.getWorkMode(device.mac) == State.WorkMode.OTA) {
                    tvFirmwareError.setVisibility(View.VISIBLE);
                }
            }
		}
	}

	/**
	 * 设置电量
	 * @param battery 电量：0~100  
	 */
	public void setBattery(int battery) {
		if (battery < 0) battery = 0;
		else if (battery > 100) battery = 100;
		batteryView.setPercent(battery);
	}
}
