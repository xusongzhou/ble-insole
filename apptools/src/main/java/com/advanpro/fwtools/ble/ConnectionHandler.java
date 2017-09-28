package com.advanpro.fwtools.ble;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.advanpro.fwtools.Constant;
import com.advanpro.fwtools.entity.State;
import com.advanpro.fwtools.module.activity.ActivityFragment;

/**
 * Created by zengfs on 2016/2/18.
 * 处理设备连接状态
 */
public class ConnectionHandler extends Handler {
	private BleDevice device;    

    public ConnectionHandler(String deviceMac, Looper looper) {
		super(looper!=null? looper : Looper.getMainLooper());
		this.device = DeviceMgr.getBoundDevice(deviceMac);        
    }

	@Override
	public void handleMessage(Message msg) {
        if (device == null) return;
		switch(msg.what) {
			case Constant.MESSAGE_SERVICES_DISCOVERED:     
				//重置状态
				ActivityFragment.setFirstState(device.mac, true);
                HisDataReaderMgr.INSTANCE.createReader(device.mac);
				State.setConnectionState(device.mac, State.ConnectionState.CONNECTED);
				ObservableMgr.getBleObservable().notifyAllConnectionStateChange(device, State.ConnectionState.CONNECTED);
				BleManager.INSTANCE.registerRealtimeNotification(device.mac);
				BleManager.INSTANCE.registerHistoryNotification(device.mac);
				BleManager.INSTANCE.readDeviceId(device.mac);//读取设备Id
				BleManager.INSTANCE.readBattery(device.mac);//读取电量
				BleManager.INSTANCE.readFirmwareVersion(device.mac);//读取固件版本
				BleManager.INSTANCE.readVV(device.mac);//读取固件VV
				BleManager.INSTANCE.setDeviceTime(device.mac);
                //BleManager.INSTANCE.readHistoryPoseOriginal(device.mac, Calendar.getInstance(), 0, 0);//读取姿势原始数据
				break;
			case Constant.MESSAGE_DISCONNECTED:
				HisDataReaderMgr.INSTANCE.removeReader(device.mac);
				State.setConnectionState(device.mac, State.ConnectionState.DISCONNECTED);
				ObservableMgr.getBleObservable().notifyAllConnectionStateChange(device, State.ConnectionState.DISCONNECTED);
				break;
		}
	}
}
