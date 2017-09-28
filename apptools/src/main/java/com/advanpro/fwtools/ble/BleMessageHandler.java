package com.advanpro.fwtools.ble;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.ParcelUuid;
import android.view.WindowManager;

import com.advanpro.fwtools.Constant;
import com.advanpro.fwtools.MyApplication;
import com.advanpro.fwtools.R;
import com.advanpro.fwtools.common.manager.MediaPlayerMgr;
import com.advanpro.fwtools.common.util.LogUtil;
import com.advanpro.fwtools.common.util.StringUtils;
import com.advanpro.fwtools.common.util.SystemUtils;
import com.advanpro.fwtools.common.util.UiUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by zengfs on 2016/2/19.
 * 处理设备数据
 */
public enum  BleMessageHandler {
	INSTANCE;
	private Map<String, MsgHandler> handlers = new HashMap<>();
	private AlertDialog lowBatteryDialog;
	
	/**
	 * 创建一个handler
	 * @param deviceMac 设备地址
	 */
	public void createHandler(String deviceMac, Looper looper) {
		handlers.put(deviceMac, new MsgHandler(DeviceMgr.getBoundDevice(deviceMac), looper));
	}
	
	public final Handler getHandler(String deviceMac) {
		return handlers.get(deviceMac);
	}
	
	private class MsgHandler extends Handler {
		private BleDevice device;

		public MsgHandler(BleDevice device, Looper looper) {
			super(looper!=null? looper : Looper.getMainLooper());
			this.device = device;
		}

		@Override
		public void handleMessage(Message msg) {
            if (device == null) return;
			int requestId = msg.getData().getInt(Constant.EXTRA_REQUEST_ID);
            String type;
            switch(requestId) {
                case Constant.REQUEST_REAL_TIME_NOTIFICATION:		
                    type = "开启实时数据NOTIFICATION";
            		break;
                case Constant.REQUEST_HISTORY_NOTIFICATION:
                    type = "开启历史数据NOTIFICATION";
                    break;
                case Constant.REQUEST_READ_DEVICE_ID:
                    type = "读取设备id";
                    break;
                case Constant.REQUEST_READ_BATTERY:
                    type = "读取电池电量";
                    break;
                case Constant.REQUEST_READ_FIRMWARE_VERSION:
                    type = "读取固件版本";
                    break;
                case Constant.REQUEST_READ_VV:
                    type = "读取固件VV号";
                    break;
                case Constant.REQUEST_SET_TIME:
                    type = "设置设备时间";
                    break;
                case Constant.REQUEST_READ_HISTORY_STEP_STAT:
                    type = "读取历史计步统计数据";
                    break;
                case Constant.REQUEST_READ_HISTORY_GAIT_STAT:
                    type = "读取历史步态次数统计数据";
                    break;
                case Constant.REQUEST_READ_HISTORY_POSE_ORIGINAL:
                    type = "读取历史姿势原始数据";
                    break;
                case Constant.REQUEST_READ_HISTORY_GAIT_ORIGINAL:
                    type = "读取历史步态原始数据";
                    break;
                case Constant.REQUEST_READ_HISTORY_STEP_SECTION:
                    type = "读取历史计步分段统计数据";
                    break;
                case Constant.REQUEST_STOP_HISTORY_DATA_TRANSFER:
                    type = "停止历史数据传输";
                    break;
                case Constant.REQUEST_CLEAR_DEVICE_DATA:
                    type = "清除设备所有数据";
                    break;
                case Constant.REQUEST_RESTART_DEVICE:
                    type = "重启设备";
                    break;
                default:
                    type = "未知";
            		break;
            }
            
			switch(msg.what) {
			    case Constant.MESSAGE_CHARACTERISTIC_VALUE:	
					byte[] value = msg.getData().getByteArray(Constant.EXTRA_VALUE);
					if (value == null) break; 
					switch(requestId) {
					    case Constant.REQUEST_READ_BATTERY:
                            LogUtil.d("MsgHandler", "ansobuy--mac: " + device.mac + ", battery: " + value[0]);
							DeviceMgr.setBattery(device.mac, value[0]);
//							if (value[0] <= 20) {
//								if (lowBatteryDialog == null) {
//									//弹出系统级对话框，提示电量低
//									lowBatteryDialog = new AlertDialog.Builder(MyApplication.getInstance().getApplicationContext())
//											.setMessage(R.string.low_battery)
//											.setNegativeButton(R.string.ok, null).create();
//									lowBatteryDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
//								}
//								if (!lowBatteryDialog.isShowing()) lowBatteryDialog.show();
//                                MediaPlayerMgr.INSTANCE.play(MyApplication.getInstance(), R.raw.low_battery);
//                                SystemUtils.vibrate(MyApplication.getInstance(), new long[]{100, 400, 100, 400}, -1);
//							}
							break;
						case Constant.REQUEST_READ_DEVICE_ID:
                            LogUtil.d("MsgHandler", "ansobuy--mac: " + device.mac + ", devId: " + new String(value));
							DeviceMgr.setDeviceId(device.mac, new String(value));
							break;
						case Constant.REQUEST_READ_FIRMWARE_VERSION:
                            LogUtil.d("MsgHandler", "ansobuy--mac: " + device.mac + ", firmware: " + new String(value));
							DeviceMgr.setDeviceFirmwareVersion(device.mac, new String(value));
							break;
                        case Constant.REQUEST_READ_VV:
                            LogUtil.d("MsgHandler", "ansobuy--mac: " + device.mac + ", VV: " + new String(value));
                            DeviceMgr.setDeviceVV(device.mac, new String(value));
                            break;
						default:
							ParcelUuid service = msg.getData().getParcelable(Constant.EXTRA_SERVICE_UUID);
							ParcelUuid characteristic = msg.getData().getParcelable(Constant.EXTRA_CHARACTERISTIC_UUID);
							if (service != null && characteristic != null) {
							    if (UuidLib.PRIVATE_SERVICE.equals(service.getUuid())) {
									//实时数据
							        if (UuidLib.REAL_TIME_NOTIFY.equals(characteristic.getUuid())) {
										LogUtil.d("MsgHandler", "ansobuy--mac: " + device.mac + ", realtime data: " + StringUtils.bytesToHexString(value));
										if ((value[0] & 0xff) == 0xAA && (value[1] & 0xff) == 0xFF) {
                                            if (value[2] == 1) {
                                                sendClearDevDataBroadcast(true);
                                            } else {
                                                sendClearDevDataBroadcast(false);
                                            }
										} else {
                                            BleDataParser.getParser(device).parseRealtimeData(value);										    
										}
							        } else if (UuidLib.HISTORY_NOTIFY.equals(characteristic.getUuid())) {//历史数据
										LogUtil.d("MsgHandler", "ansobuy--mac: " + device.mac + ", his data: " + StringUtils.bytesToHexString(value));
							            BleDataParser.getParser(device).parseHistoryData(value);
							        }
							    } 
							} 							
							break;
					}
					break;
				case Constant.MESSAGE_WRITE_COMPLETE:                                        
					LogUtil.d("MsgHandler", "ansobuy--成功写入！请求类型：" + type);
					break;
				case Constant.MESSAGE_INDICATION_REGISTERED:
					LogUtil.d("MsgHandler", "ansobuy--INDICATION_REGISTERED！请求类型：" + type);
					break;
				case Constant.MESSAGE_NOTIFICATION_REGISTERED:					
					LogUtil.d("MsgHandler", "ansobuy--NOTIFICATION_REGISTERED！请求类型：" + type);
					break;
				case Constant.MESSAGE_REQUEST_FAILED:
                    if (requestId == Constant.REQUEST_CLEAR_DEVICE_DATA) {
                        sendClearDevDataBroadcast(false);
                    }
					LogUtil.d("MsgHandler", "ansobuy--请求失败！请求类型：" + type);					
					break;
				case Constant.MESSAGE_REQUEST_NULL_SERVICE:
                    if (requestId == Constant.REQUEST_RESTART_DEVICE) {
                        sendRestartDevBroadcast(false);
                    } else if (requestId == Constant.REQUEST_CLEAR_DEVICE_DATA) {
                        sendClearDevDataBroadcast(false);
                    }
					LogUtil.d("MsgHandler", "ansobuy--SERVICE不存在！请求类型：" + type);
					break;
				case Constant.MESSAGE_REQUEST_NULL_CHARACTERISTIC:
                    if (requestId == Constant.REQUEST_RESTART_DEVICE) {
                        sendRestartDevBroadcast(false);
                    } else if (requestId == Constant.REQUEST_CLEAR_DEVICE_DATA) {
                        sendClearDevDataBroadcast(false);
                    }
					LogUtil.d("MsgHandler", "ansobuy--CHARACTERISTIC不存在！请求类型：" + type);
					break;
				case Constant.MESSAGE_REQUEST_NULL_DESCRIPTOR:
					LogUtil.d("MsgHandler", "ansobuy--DESCRIPTOR不存在！请求类型：" + type);
					break;
				case Constant.MESSAGE_GATT_STATUS_REQUEST_NOT_SUPPORTED:
                    if (requestId == Constant.REQUEST_RESTART_DEVICE) {
                        sendRestartDevBroadcast(false);
                    } else if (requestId == Constant.REQUEST_CLEAR_DEVICE_DATA) {
                        sendClearDevDataBroadcast(false);
                    }
					LogUtil.d("MsgHandler", "ansobuy--请求不被支持！请求类型：" + type);
					break;
			}
		}
        
        private void sendRestartDevBroadcast(boolean result) {
            Intent intent = new Intent(Constant.ACTION_SEND_RESTART_CMD);
            intent.putExtra(Constant.EXTRA_RESULT_ID, result ? Constant.RESULT_OK : Constant.RESULT_FAILE);
            intent.putExtra(Constant.EXTRA_VALUE, device);
            UiUtils.sendBroadcast(intent);
        }
        
        private void sendClearDevDataBroadcast(boolean result) {
            Intent intent = new Intent(Constant.ACTION_CLEAR_DEVICE_DATA);
            intent.putExtra(Constant.EXTRA_RESULT_ID, result ? Constant.RESULT_OK : Constant.RESULT_FAILE);
            intent.putExtra(Constant.EXTRA_VALUE, device);
            UiUtils.sendBroadcast(intent);
        }
	}
}
