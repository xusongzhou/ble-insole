package com.advanpro.fwtools.ble;

import com.advanpro.fwtools.Constant;
import com.advanpro.fwtools.MyApplication;
import com.advanpro.fwtools.R;
import com.advanpro.fwtools.common.manager.ToastMgr;
import com.advanpro.fwtools.common.util.UiUtils;
import com.advanpro.fwtools.db.Dao;
import com.advanpro.fwtools.db.Device;
import com.advanpro.fwtools.entity.State;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zengfs on 2016/2/19.
 * 设备管理类，绑定、解绑设备，保存、获取当前绑定设备
 */
public class DeviceMgr {
	private static Map<String, BleDevice> boundDevices = new HashMap<>();
    private static Map<String, CheckBindRunable> checkBindRunableMap = new HashMap<>();

	/**
	 * 从数据库中取得绑定的设备信息添加到集合中
	 */
	public static void loadCurrentPairInsoles() {
		List<Device> devices = Dao.INSTANCE.queryDevices();
		for (Device device : devices) {
			//保存绑定设备实例，便于取用
			BleDevice bleDevice = new BleDevice(device.name, device.mac, device.isLeft, device.type);
			boundDevices.put(bleDevice.mac, bleDevice);
		}
	}

	/**
	 * 连接当前绑定的设备
	 */	
	public static void connectCurrentPairInsoles() {
		for (BleDevice bleDevice : boundDevices.values()) {
			//连接设备
			BleManager.INSTANCE.connect(bleDevice.mac);
		}
	}
	
	/**
	 * 添加绑定设备
	 */
	public static void bindDevice(final BleDevice device) {		
		if (device == null || boundDevices.containsValue(device)) return;
        CheckBindRunable runable = checkBindRunableMap.get(device.mac);
        if (runable != null) {
            UiUtils.cancel(runable);            
        } else {
            runable = new CheckBindRunable(device);
            checkBindRunableMap.put(device.mac, runable);
        }
		//如果已有绑定设备，先解绑原有设备，再绑定新设备
		BleDevice bleDevice = getBoundDevice(device.isLeft);
		if (bleDevice != null) {
		    unbindDevice(bleDevice.mac);
		}
		boundDevices.put(device.mac, device);
		Dao.INSTANCE.insertOrUpdateDevice(device);
		//连接设备
		BleManager.INSTANCE.connect(device.mac);        
        UiUtils.postDelayed(runable, 30000);
	}

    public static void cancelBindCheck(String deviceMac) {
        CheckBindRunable runable = checkBindRunableMap.get(deviceMac);
        if (runable != null) UiUtils.cancel(runable);
    }
    
	/**
	 * 解绑设备
	 */
	public static void unbindDevice(String deviceMac) {
		BleDevice device = boundDevices.remove(deviceMac);
		if (device != null) {
            cancelBindCheck(device.mac);
			//从数据库删除
			Dao.INSTANCE.deleteDevice(device.mac);
			//断开设备连接
			BleManager.INSTANCE.removeConnection(device.mac);
		}		
	}
    
    /**
	 * 解绑设备
	 */
	public static void unbindDevice(boolean isLeft) {
		BleDevice bleDevice = getBoundDevice(isLeft);
		if (bleDevice != null) unbindDevice(bleDevice.mac);		
	}

	/**
	 * 根据左右获取已绑定设备
	 */
	public static BleDevice getBoundDevice(boolean isLeft) {
		for (BleDevice bleDevice : boundDevices.values()) {
			if (bleDevice.isLeft == isLeft) return bleDevice;
		}
		return null;
	}

	/**
	 * 根据设备地址获取已绑定设备
	 */
	public static BleDevice getBoundDevice(String deviceMac) {
		return boundDevices.get(deviceMac);
	}

	/**
	 * 获取全部绑定的设备
	 */
	public static List<BleDevice> getBoundDevices() {
		List<BleDevice> devices = new ArrayList<>();
		for (BleDevice bleDevice : boundDevices.values()) {
			devices.add(bleDevice.clone());
		}
		return devices;
	}

    public static List<BleDevice> getConnectedDevices() {
        List<BleDevice> devices = new ArrayList<>();
        for (BleDevice dev : boundDevices.values()) {
            State.ConnectionState state = State.getConnectionState(dev.mac);
            if (state != null && state == State.ConnectionState.CONNECTED) {
                devices.add(dev);
            }
        }
        return devices;
    }
    
    public static int getConnectedCount() {
        int count = 0;
        for (BleDevice dev : boundDevices.values()) {
            State.ConnectionState state = State.getConnectionState(dev.mac);
            if (state != null && state == State.ConnectionState.CONNECTED) count++;
        }
        return count;
    }
    
	/**
	 * 绑定设备的数量
	 */
	public static int getBoundDeviceCount() {
		return boundDevices.size();
	}

	/**
	 * 判断绑定的设备是否都已连接
	 */
	public static boolean isAllConnected() {
		for (BleDevice device : boundDevices.values()) {
			State.ConnectionState state = State.getConnectionState(device.mac);
			if (state != null && state != State.ConnectionState.CONNECTED) return false;
		}
		return !boundDevices.isEmpty();
	}

    /**
     * 判断是否全都未连接
     */
    public static boolean isAllDisconnected() {
        for (BleDevice device : boundDevices.values()) {
            State.ConnectionState state = State.getConnectionState(device.mac);
            if (state != null && state == State.ConnectionState.CONNECTED) return false;
        }
        return true;
    }
    
    /**
	 * 是否已绑定不同类型的设备
	 */
	public static boolean existDiffType(int type) {
		for (BleDevice device : boundDevices.values()) {
			if (device.type != type) return true;
		}
		return false;
	}

	/**
	 * 获取绑定设备的类型，因为不能绑定不同类型，获取其中一个即可
	 * @return 返回绑定设备类型，未绑定则返回-1
	 */
	public static int getBoundDeviceType() {
		return boundDevices.isEmpty() ? -1 : ((BleDevice) boundDevices.values().toArray()[0]).type;
	}

	/**
	 * 获取设备id
	 */
	public static String getDeviceId(boolean isLeft) {
		for (BleDevice device : boundDevices.values()) {
			if (device.isLeft == isLeft) return device.devId;
		}
		return "";
	}

	/**
	 * 设置设备id
	 */
	public static void setDeviceId(String deviceMac, String devId) {
		BleDevice device = boundDevices.get(deviceMac);
		if (device != null) {
			device.devId = devId;
			Dao.INSTANCE.insertOrUpdateDevice(device);
		}
	}

    /**
     * 设置电量
     */
    public static void setBattery(String deviceMac, int battery) {
        BleDevice device = boundDevices.get(deviceMac);
        if (device != null) {
            if (battery < 0) battery = 0;
            else if (battery > 100) battery = 100;
            device.battery = battery;
            ObservableMgr.getBleObservable().notifyAllBatteryRead(device, battery);
        }
    }
    
	/**
	 * 设置设备固件版本
	 */
	public static void setDeviceFirmwareVersion(String deviceMac, String firmware) {
		BleDevice device = boundDevices.get(deviceMac);
		if (device != null && firmware != null) {
            device.firmware = firmware;
            //验证左右是否正确绑定
            String s = device.isLeft ? "L" : "R";
            String s1 = firmware.substring(firmware.length() - 1);
            BleDevice another = getBoundDevice(!device.isLeft);
            if (another != null && another.firmware != null) {
                String s2 = firmware.substring(0, firmware.length() - 1);
                String s3 = another.firmware.substring(0, another.firmware.length() - 1);
                if (!s2.equals(s3)) {
                    ToastMgr.showTextToast(MyApplication.getInstance(), 0, R.string.fireware_version_different);                    
                }
            }
            if (s.equalsIgnoreCase(s1)) {                
                ObservableMgr.getBleObservable().notifyAllFirmwareRead(device, firmware);
            } else {
                unbindDevice(device.mac);
                ObservableMgr.getBleObservable().notifyAllBindStateChange(device, Constant.BIND_FAILE);
            }            
        }
	}

    /**
     * 设置设备固件VV
     */
    public static void setDeviceVV(String deviceMac, String vv) {
        BleDevice device = boundDevices.get(deviceMac);
        if (device != null) {
            device.vv = vv;
            ObservableMgr.getBleObservable().notifyAllVVRead(device, vv);
        }
    }

    private static class CheckBindRunable implements Runnable {
        private BleDevice device;
        
        public CheckBindRunable(BleDevice device) {
            this.device = device;
        }

        @Override
        public void run() {
            State.ConnectionState state = State.getConnectionState(device.mac);
            if (state != null && state != State.ConnectionState.CONNECTED) {
                unbindDevice(device.mac);
                ObservableMgr.getBleObservable().notifyAllBindStateChange(device, Constant.BIND_FAILE);
            }
        }
    }
}
