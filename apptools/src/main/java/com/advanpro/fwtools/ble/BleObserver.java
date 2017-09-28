package com.advanpro.fwtools.ble;

import com.advanpro.fwtools.entity.State;
import com.advanpro.fwtools.entity.Step;

/**
 * Created by zengfs on 2016/2/18.
 * 蓝牙设备状态、数据观察者，观察设备状态、数据变化
 */
public class BleObserver {

	public BleObserver(BleObservable bleObservable) {
		if (bleObservable != null) {
			bleObservable.addObserver(this);
		}
	}

    /**
     * 设备绑定状态变化
     */
    public void onBindStateChange(BleDevice device, int status) {}
    
	/**
	 * 设备连接状态变化
	 * @param device 蓝牙设备
	 * @param state 连接状态
	 */
	public void onConnectionStateChange(BleDevice device, State.ConnectionState state) {}

	/**
	 * 读到设备电量值
	 * @param device 蓝牙设备
	 * @param battery 电量
	 */
	public void onBatteryRead(BleDevice device, int battery) {}

    /**
     * 读到设备固件版本号
     * @param device 蓝牙设备
     * @param firmware 固件版本
     */
    public void onFirmwareRead(BleDevice device, String firmware) {}

    /**
     * 读到设备固件VV号
     * @param device 蓝牙设备
     * @param vv 固件版本
     */
    public void onVVRead(BleDevice device, String vv) {}

	/**
	 * 收到设备步态实时数据上报值
	 * @param device 蓝牙设备
	 * @param data 步态原始值(压力开关值)   
	 * @param impactRank 冲击力等级
	 */
	public void onGaitRealtimeDataChanged(BleDevice device, int data, int impactRank) {}

	/**
	 * 收到设备坐站姿实时数据上报值
	 * @param device 蓝牙设备
	 * @param data 坐站原始数据
	 */
	public void onPoseRealtimeDataChanged(BleDevice device, int data) {}

	/**
	 * 收到设备计步实时数据上报值
	 * @param device 蓝牙设备
	 * @param info 包含步数及时长
	 */
	public void onStepRealtimeDataChanged(BleDevice device, Step.Apart info) {}

	/**
	 * 历史数据读取结果回调
	 */
	public void onHistoryDataReadResult(BleDevice device, boolean success) {}
}
