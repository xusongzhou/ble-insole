package com.advanpro.fwtools.ble;

import com.advanpro.fwtools.common.util.UiUtils;
import com.advanpro.fwtools.entity.State;
import com.advanpro.fwtools.entity.Step;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zengfs on 2016/2/18.
 * 蓝牙设备状态、数据被观察者
 */
public class BleObservable {	
	private List<BleObserver> observers = new ArrayList<>();
	
	public void addObserver(BleObserver observer) {
		if (observer == null) {
			throw new NullPointerException("observer == null");
		}
		synchronized (this) {
			if (!observers.contains(observer))
				observers.add(observer);
		}
	}

	public int countObservers() {
		return observers.size();
	}

	public synchronized void deleteObserver(BleObserver observer) {
		observers.remove(observer);
	}
	
	private BleObserver[] getAllObservers() {
		int size;
		BleObserver[] arrays;
		synchronized (this) {
			size = observers.size();
			arrays = new BleObserver[size];
			observers.toArray(arrays);
		}
		return arrays;
	}

	public synchronized void deleteObservers() {
		observers.clear();
	}
	
	public void notifyAllConnectionStateChange(final BleDevice device, final State.ConnectionState state) {
		for (final BleObserver observer : getAllObservers()) {
			UiUtils.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    observer.onConnectionStateChange(device, state);
                }
            });
		}
	}

	public void notifyAllBatteryRead(final BleDevice device, final int battery) {
		for (final BleObserver observer : getAllObservers()) {
            UiUtils.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    observer.onBatteryRead(device, battery);
                }
            });			
		}
	}

    public void notifyAllFirmwareRead(final BleDevice device, final String firmware) {
        for (final BleObserver observer : getAllObservers()) {
            UiUtils.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    observer.onFirmwareRead(device, firmware);
                }
            });            
        }
    }

    public void notifyAllVVRead(final BleDevice device, final String vv) {
        for (final BleObserver observer : getAllObservers()) {
            UiUtils.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    observer.onVVRead(device, vv);
                }
            });
        }
    }
    
	public void notifyAllGaitRealtimeDataChanged(final BleDevice device, final int data, final int impactRank) {
		for (final BleObserver observer : getAllObservers()) {
            UiUtils.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    observer.onGaitRealtimeDataChanged(device, data, impactRank);
                }
            });			
		}
	}

	public void notifyAllPoseRealtimeDataChanged(final BleDevice device, final int data) {
		for (final BleObserver observer : getAllObservers()) {
            UiUtils.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    observer.onPoseRealtimeDataChanged(device, data);
                }
            });			
		}
	}

	public void notifyAllStepRealtimeDataChanged(final BleDevice device, final Step.Apart info) {
		for (final BleObserver observer : getAllObservers()) {
            UiUtils.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    observer.onStepRealtimeDataChanged(device, info);
                }
            });			
		}
	}

	public void notifyAllHisDataReadResult(final BleDevice device, final boolean success) {
		for (final BleObserver observer : getAllObservers()) {
            UiUtils.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    observer.onHistoryDataReadResult(device, success);
                }
            });			
		}
	}
    
    public void notifyAllBindStateChange(final BleDevice device, final int status) {
        for (final BleObserver observer : observers) {
            UiUtils.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    observer.onBindStateChange(device, status);
                }
            });
        }
    }
}
