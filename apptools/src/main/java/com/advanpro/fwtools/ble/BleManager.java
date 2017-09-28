package com.advanpro.fwtools.ble;

import android.os.Handler;
import android.os.Looper;

import com.advanpro.fwtools.Constant;
import com.advanpro.fwtools.common.util.DateUtils;
import com.advanpro.fwtools.common.util.UiUtils;
import com.advanpro.fwtools.entity.ByteDate;

import java.util.Calendar;

/**
 * Created by zengfs on 2016/2/17.
 * 蓝牙连接、通信等操作
 */
public enum  BleManager {
	INSTANCE;	
	private BleService bleService;

	/**
	 * 初始化
	 */
	public void setBleService(BleService bleService) {
		this.bleService = bleService;
	}

    public BleService getBleService() {
        return this.bleService;
    }
	/**
	 * 建立连接
	 * @param deviceMac 蓝牙设备
	 */
	public void connect(String deviceMac) {
        Looper looper = bleService.getBleServiceLooper();
		if (!bleService.connect(deviceMac, new ConnectionHandler(deviceMac, looper))){
            return;
        }
		BleMessageHandler.INSTANCE.createHandler(deviceMac, looper);
	}

    /**
     * 建立连接
     * @param deviceMac 蓝牙设备
     */
    public void connect(final String deviceMac, final Handler handler) {
        bleService.connect(deviceMac, handler);
    }
    
	/**
	 * 设置设备时间
	 */
	public void setDeviceTime(String deviceMac) {
		if (bleService != null) {
			ByteDate date = DateUtils.getByteDate(Calendar.getInstance());
			byte[] data = new byte[]{(byte) (0xAA & 0xFF), (byte) (0xBB & 0xFF), date.year, date.month, date.date, 
					date.hour, date.minute, date.second};            
			bleService.writeCharacteristicValue(deviceMac, Constant.REQUEST_SET_TIME, UuidLib.PRIVATE_SERVICE,
					UuidLib.REAL_TIME_WRITE, data, BleMessageHandler.INSTANCE.getHandler(deviceMac));
		}
	}
	
	/**
	 * 开启实时数据NOTIFICATION
	 */
	public void registerRealtimeNotification(String deviceMac) {
		if (bleService != null) {
		    bleService.requestCharacteristicNotification(deviceMac, Constant.REQUEST_REAL_TIME_NOTIFICATION, UuidLib.PRIVATE_SERVICE,
					UuidLib.REAL_TIME_NOTIFY, BleMessageHandler.INSTANCE.getHandler(deviceMac));
		}
	}

	/**
	 * 开启历史数据NOTIFICATION
	 */
	public void registerHistoryNotification(String deviceMac) {
		if (bleService != null) {
			bleService.requestCharacteristicNotification(deviceMac, Constant.REQUEST_HISTORY_NOTIFICATION, UuidLib.PRIVATE_SERVICE,
					UuidLib.HISTORY_NOTIFY, BleMessageHandler.INSTANCE.getHandler(deviceMac));
		}
	}

	/**
	 * 读取设备id
	 */
	public void readDeviceId(String deviceMac) {
		if (bleService != null) {
			bleService.requestCharacteristicValue(deviceMac, Constant.REQUEST_READ_DEVICE_ID, UuidLib.DEVICE_INFO_SERVICE,
					UuidLib.DEVICE_ID, BleMessageHandler.INSTANCE.getHandler(deviceMac));
		}
	}
	
	/**
	 * 读取电量
	 */
	public void readBattery(String deviceMac) {
		if (bleService != null) {
		    bleService.requestCharacteristicValue(deviceMac, Constant.REQUEST_READ_BATTERY, UuidLib.BATTERY_SERVICE, 
					UuidLib.BATTERY, BleMessageHandler.INSTANCE.getHandler(deviceMac));
		}
	}

	/**
	 * 读取所有绑定设备电量
	 */
	public void readAllBattery() {
		for (BleDevice device : DeviceMgr.getBoundDevices()) {
			readBattery(device.mac);
		}
	}

	/**
	 * 读取固件版本
	 */
	public void readFirmwareVersion(String deviceMac) {
		if (bleService != null) {
		    bleService.requestCharacteristicValue(deviceMac, Constant.REQUEST_READ_FIRMWARE_VERSION, UuidLib.DEVICE_INFO_SERVICE,
					UuidLib.DEVICE_FIRMWARE_VERSION, BleMessageHandler.INSTANCE.getHandler(deviceMac));
		}
	}

    /**
     * 读取VV号
     */
    public void readVV(String deviceMac) {
        if (bleService != null) {
            bleService.requestCharacteristicValue(deviceMac, Constant.REQUEST_READ_VV, UuidLib.DEVICE_INFO_SERVICE, UuidLib.DEVICE_VV,
                    BleMessageHandler.INSTANCE.getHandler(deviceMac));
        }
    }
    
	/**
	 * 读取历史计步统计数据
	 */
	public void readHistoryStepStat(String deviceMac, Calendar c) {
		if (bleService != null) {
			ByteDate date = DateUtils.getByteDate(c);
		    byte[] data = {0x06, 0x02, date.year, date.month, date.date};
			bleService.writeCharacteristicValue(deviceMac, Constant.REQUEST_READ_HISTORY_STEP_STAT, UuidLib.PRIVATE_SERVICE,
                    UuidLib.HISTORY_WRITE, data, BleMessageHandler.INSTANCE.getHandler(deviceMac));
		}
	}

	/**
	 * 读取所有绑定设备历史计步统计数据
	 */
	public void readAllHistoryStepStat(Calendar c) {
		for (BleDevice device : DeviceMgr.getBoundDevices()) {
			readHistoryStepStat(device.mac, c);
		}
	}
	
	/**
	 * 读取历史步态次数统计数据
	 */
	public void readHistoryGaitStat(String deviceMac, Calendar c) {
		if (bleService != null) {
			ByteDate date = DateUtils.getByteDate(c);
			byte[] data = {0x06, 0x01, date.year, date.month, date.date};
			bleService.writeCharacteristicValue(deviceMac, Constant.REQUEST_READ_HISTORY_GAIT_STAT, UuidLib.PRIVATE_SERVICE,
					UuidLib.HISTORY_WRITE, data, BleMessageHandler.INSTANCE.getHandler(deviceMac));
		}
	}
	
	public void readAllHistoryGaitStat(Calendar c) {
		for (BleDevice device : DeviceMgr.getBoundDevices()) {
			readHistoryGaitStat(device.mac, c);
		}
	}
	
	/**
	 * 读取历史步态原始数据
	 * @param deviceMac 设备地址
	 * @param c 日期
	 * @param start 起始段号 0x01~0x60
	 * @param total 请求段数 0x01~0x60
	 */
	public void readHistoryGaitOriginal(String deviceMac, Calendar c, int start, int total) {
		if (bleService != null) {
			ByteDate date = DateUtils.getByteDate(c);
		    byte[] data = {0x06, 0x03, date.year, date.month, date.date, (byte) start, (byte) total};
			bleService.writeCharacteristicValue(deviceMac, Constant.REQUEST_READ_HISTORY_GAIT_ORIGINAL, UuidLib.PRIVATE_SERVICE,
					UuidLib.HISTORY_WRITE, data, BleMessageHandler.INSTANCE.getHandler(deviceMac));
		}
	}
	
	/**
	 * 读取历史计步分段数据
	 * @param deviceMac 设备地址
	 * @param c 日期
	 * @param start 起始段号 0x01~0x60
	 * @param total 请求段数 0x01~0x60
	 */
	public void readHistoryStepSection(String deviceMac, Calendar c, int start, int total) {
		if (bleService != null) {
			ByteDate date = DateUtils.getByteDate(c);
		    byte[] data = {0x06, 0x04, date.year, date.month, date.date, (byte) start, (byte) total};
			bleService.writeCharacteristicValue(deviceMac, Constant.REQUEST_READ_HISTORY_STEP_SECTION, UuidLib.PRIVATE_SERVICE,
					UuidLib.HISTORY_WRITE, data, BleMessageHandler.INSTANCE.getHandler(deviceMac));
		}
	}

	/**
	 * 读取历史姿势原始数据
	 * @param deviceMac 设备地址
	 * @param c 日期
	 * @param start 起始段号 0x01~0x48
	 * @param total 请求段数 0x01~0x48
	 */
	public void readHistoryPoseOriginal(String deviceMac, Calendar c, int start, int total) {
		if (bleService != null) {
			ByteDate date = DateUtils.getByteDate(c);
			byte[] data = {0x06, 0x05, date.year, date.month, date.date, (byte) start, (byte) total};
			bleService.writeCharacteristicValue(deviceMac, Constant.REQUEST_READ_HISTORY_POSE_ORIGINAL, UuidLib.PRIVATE_SERVICE,
					UuidLib.HISTORY_WRITE, data, BleMessageHandler.INSTANCE.getHandler(deviceMac));
		}
	}

    /**
     * 发送停止历史数据继续发包命令
     * @param deviceMac 设备地址
     */
    public void stopHistoryDataTransfer(String deviceMac, Calendar c) {
        if (bleService != null) {
            ByteDate date = DateUtils.getByteDate(c);
            byte[] data = {(byte) (0xAA & 0xFF), (byte) (0xCC & 0xFF), date.year, date.month, date.date, 0};
            bleService.writeCharacteristicValue(deviceMac, Constant.REQUEST_STOP_HISTORY_DATA_TRANSFER, UuidLib.PRIVATE_SERVICE,
                    UuidLib.HISTORY_WRITE, data, BleMessageHandler.INSTANCE.getHandler(deviceMac));
        }
    }

    /**
     * 重启到OTA模式
     */
    public void enableOTAMode(String deviceMac, Handler handler) {
        if (bleService != null) {
            bleService.writeCharacteristicValue(deviceMac, Constant.REQUEST_OTA_MODE, UuidLib.CSR_OTA_APPLICATION_SERVICE,
                    UuidLib.CSR_OTA_CURRENT_APPLICATION, new byte[]{0}, handler);
        }
    }
    
    public void clearAllDevicesData() {
        if (bleService != null) {
            for (BleDevice dev : DeviceMgr.getConnectedDevices()) {
                bleService.writeCharacteristicValue(dev.mac, Constant.REQUEST_CLEAR_DEVICE_DATA, UuidLib.PRIVATE_SERVICE, 
                        UuidLib.REAL_TIME_WRITE, new byte[]{(byte) (0xAA & 0xFF), (byte) (0xFF), (byte) (0xFF), 
                                (byte) (0xFF), (byte) (0xFF)}, BleMessageHandler.INSTANCE.getHandler(dev.mac));
            }
        }
    }
    
    public void restartAllDevices() {
        if (bleService != null) {
            for (BleDevice dev : DeviceMgr.getConnectedDevices()) {
                bleService.writeCharacteristicValue(dev.mac, Constant.REQUEST_RESTART_DEVICE, UuidLib.PRIVATE_SERVICE,
                        UuidLib.REAL_TIME_WRITE, new byte[]{(byte) (0xAA & 0xFF), (byte) (0xAF & 0xFF)}, BleMessageHandler.INSTANCE.getHandler(dev.mac));
            }
        }
    }
    
	/**
	 * 重连此设备
	 */
	public void reconnect(String deviceMac) {
		if (bleService != null) {
			bleService.reconnect(deviceMac);
		}
	}
	
	/**
	 * 解绑设备时移除连接
	 * @param deviceMac 设备地址
	 */
	public void removeConnection(String deviceMac) {
		if (bleService != null) {
		    bleService.removeConnection(deviceMac);
		}
	}

	/**
	 * 移除所有连接
	 */
	public void removeAllConnections() {
		if (bleService != null) {
			bleService.removeAllConnections();
		}
	}
}
