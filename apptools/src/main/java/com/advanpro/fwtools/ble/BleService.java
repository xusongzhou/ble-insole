package com.advanpro.fwtools.ble;

import android.app.Activity;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.ParcelUuid;
import android.support.annotation.Nullable;

import com.advanpro.fwtools.Constant;
import com.advanpro.fwtools.common.MyTimer;
import com.advanpro.fwtools.common.manager.ThreadMgr;
import com.advanpro.fwtools.common.util.DateUtils;
import com.advanpro.fwtools.common.util.LogUtil;
import com.advanpro.fwtools.common.util.UiUtils;
import com.advanpro.fwtools.entity.State;
import com.advanpro.fwtools.module.BaseActivity;
import com.advanpro.fwtools.module.MainActivity;
import com.advanpro.fwtools.module.more.DeviceManagerActivity;
import com.advanpro.aswear.ASWear;

import java.io.File;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by zengfs on 2016/1/14.
 * 蓝牙服务，连接及数据读写
 */
public class BleService extends Service implements MyTimer.TimerTaskCallback {
	private static final String TAG = BleService.class.getSimpleName();			
	private static final short  GATT_REQ_NOT_SUPPORTED = 0x0006;

    private HandlerThread mBleServiceThread;
	private LocalBinder binder = new LocalBinder();
	private BluetoothManager bluetoothManager;
	private BluetoothAdapter bluetoothAdapter;
	private Map<String, Connection> connections = new HashMap<>();
	private CharacteristicHandlersContainer notificationHandlers = new CharacteristicHandlersContainer();
	private MyTimer myTimer;
    private File logFile;

    private static class Connection {
		public BluetoothDevice bluetoothDevice;
		public BluetoothGatt bluetoothGatt;
		public Queue<BleRequest> requestQueue = new ConcurrentLinkedQueue<>();
		public BleRequest currentRequest;
		public BluetoothGattCharacteristic pendingCharacteristic;
		public Handler handler;
        public int contectedTimeCount;//心跳机制计时
		public int connectingTimeCount;//尝试连接到连接上，计时
		public boolean connecting;
	}
		
	@Override
	public void onCreate() {
		bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
		if (bluetoothManager != null) bluetoothAdapter = bluetoothManager.getAdapter();
		myTimer = new MyTimer();
        logFile = new File(ASWear.AppSoreDir, "/ansobuy_conn_log.txt");

        mBleServiceThread = new HandlerThread("BleServiceThread");
        mBleServiceThread.start();
	}

    public Looper getBleServiceLooper() {
        return mBleServiceThread.getLooper();
    }

	@Override
	public void onDestroy() {
        mBleServiceThread.quit();
		myTimer.stopTimer();
		super.onDestroy();		
	}

	@Nullable
	@Override
	public IBinder onBind(Intent intent) {
		return binder;
	}	

	public class LocalBinder extends Binder {
		public BleService getService() {
			return BleService.this;
		}
	}
    
	/**
	 * 连接蓝牙设备
	 */
	public boolean connect(String deviceMac, android.os.Handler handler) {
		if (bluetoothAdapter == null || deviceMac == null || !deviceMac.matches("^[0-9A-Fa-f]{2}(:[0-9A-Fa-f]{2}){5}$")) {
			LogUtil.e(TAG, "BluetoothAdapter not initialized or unspecified address.");
			return false;
		}
        Connection conn = connections.get(deviceMac);
        if (conn != null && conn.bluetoothGatt != null) {
            conn.bluetoothGatt.disconnect();
            refresh(conn.bluetoothGatt);
            conn.bluetoothGatt.close();
            conn.bluetoothGatt = null;
        }
		setConnectionStateToConnecting(deviceMac);	        
		Connection connection = new Connection();
		connection.handler = handler;
		connection.bluetoothDevice = bluetoothAdapter.getRemoteDevice(deviceMac);
		connection.connecting = true;
		connection.bluetoothGatt = connection.bluetoothDevice.connectGatt(this, false, gattCallback);
		connections.put(deviceMac, connection);		
		if (!myTimer.isRunning()) myTimer.startTimer(1000, 5000, this);
		return true;
	}

	//设置连接状态为正在连接，并通知观察者
	private void setConnectionStateToConnecting(String deviceMac) {
		BleDevice bleDevice = DeviceMgr.getBoundDevice(deviceMac);
		if (bleDevice != null) {
			State.setConnectionState(deviceMac, State.ConnectionState.CONNECTING);
			ObservableMgr.getBleObservable().notifyAllConnectionStateChange(bleDevice, State.ConnectionState.CONNECTING);
		}
	}
	
	@Override
	public void runTimerTask() {
		for (Connection connection : connections.values()) {
			//设置连接时间为6秒，如果6秒还没连上，判定为连接失败，重连
			if (connection.connecting) {
			    connection.connectingTimeCount++;
				if (connection.connectingTimeCount >= 10) {
					connection.connecting = false;
					connection.connectingTimeCount = 0;
				}
			} else if (State.getWorkMode(connection.bluetoothDevice.getAddress()) == State.WorkMode.APP && 
                    isDisconnected(connection.bluetoothDevice)) {
				reconnect(connection.bluetoothDevice.getAddress());
			}else if(++connection.contectedTimeCount > 2) {
                connection.contectedTimeCount = 0;
                byte[] data = new byte[]{(byte) (0xBB & 0xFF), (byte) (0xBB & 0xFF), 0, 0, 0, 0, 0, 0};
                String mac = connection.bluetoothDevice.getAddress();
                this.writeCharacteristicValue(mac, Constant.REQUEST_SET_TIME, UuidLib.PRIVATE_SERVICE,
                        UuidLib.REAL_TIME_WRITE, data, BleMessageHandler.INSTANCE.getHandler(mac));
            }
		}
	}

    /**
     * Clears the internal cache and forces a refresh of the services from the
     * remote device.
     */
    public void refresh(String deviceMac) {
        Connection connection = connections.get(deviceMac);
        if (connection != null && connection.bluetoothGatt != null) {
            connection.bluetoothGatt.disconnect();
            refresh(connection.bluetoothGatt);
        }       
    }

    /**
     * Clears the internal cache and forces a refresh of the services from the
     * remote device.
     */
    private boolean refresh(BluetoothGatt bluetoothGatt) {
        try {
            Method localMethod = bluetoothGatt.getClass().getMethod("refresh");
            if (localMethod != null) {
                return (Boolean) localMethod.invoke(bluetoothGatt);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
	 * 重连
	 * @param deviceMac 设备地址
	 */
	public void reconnect(String deviceMac) {
        final Connection connection = connections.get(deviceMac);
        if (connection != null) {
            if (connection.bluetoothGatt != null) {
                connection.bluetoothGatt.disconnect();
                refresh(connection.bluetoothGatt);
                connection.bluetoothGatt.close();
                connection.bluetoothGatt = null;
            }
            connection.connecting = true;
            connection.connectingTimeCount = 0;
            setConnectionStateToConnecting(deviceMac);
            ThreadMgr.INSTANCE.getSPool().execute(new Runnable() {
                @Override
                public void run() {
                    LogUtil.saveLog(logFile, connection.bluetoothDevice.getAddress() + "--" +
                            DateUtils.formatDate(new Date(), "MM/dd HH:mm:ss.SSS") + "> start reconnect");
                }
            });

            connection.bluetoothGatt = connection.bluetoothDevice.connectGatt(BleService.this, false, gattCallback);
            LogUtil.d(TAG, "正在重连: " + connection.bluetoothDevice.getAddress());
        }
	}

	/**
	 * 移除所有连接
	 */
	public void removeAllConnections() {
		Iterator<Map.Entry<String, Connection>> iterator = connections.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<String, Connection> entry = iterator.next();
            if (entry.getValue().bluetoothGatt != null) {
                entry.getValue().bluetoothGatt.disconnect();
                refresh(entry.getValue().bluetoothGatt);
                entry.getValue().bluetoothGatt.close();
                entry.getValue().bluetoothGatt = null;
            }
			State.removeConnectionState(entry.getValue().bluetoothDevice.getAddress());
			entry.getValue().handler.sendEmptyMessage(Constant.MESSAGE_DISCONNECTED);
			iterator.remove();
		}
	}

	/**
	 * 移除连接
	 * @param deviceMac 要断开连接的设备地址
	 */
	public void removeConnection(String deviceMac) {
		Connection connection = connections.get(deviceMac);
		if (connection != null) {
            if (connection.bluetoothGatt != null) {
                connection.bluetoothGatt.disconnect();
                refresh(connection.bluetoothGatt);
                connection.bluetoothGatt.close();
                connection.bluetoothGatt = null;
            }
			State.removeConnectionState(connection.bluetoothDevice.getAddress());
			connection.handler.sendEmptyMessage(Constant.MESSAGE_DISCONNECTED);
			connections.remove(deviceMac);
		}
	}
    
	/**
	 * 已断开
	 */
	public boolean isDisconnected(BluetoothDevice bluetoothDevice) {
		if (bluetoothManager == null) return true;
		int state = bluetoothManager.getConnectionState(bluetoothDevice, BluetoothProfile.GATT);
		return state == BluetoothProfile.STATE_DISCONNECTED;
	}

	/**
	 * 请求读取characteristic的值
	 * @param deviceMac 设备地址
	 * @param requestId 请求码
	 */
	public synchronized void requestCharacteristicValue(String deviceMac, int requestId, UUID service, UUID characteristic, Handler notifyHandler) {
		Connection connection = connections.get(deviceMac);
		if (connection != null) {
			if (connection.currentRequest == null) {
				performCharacteristicValueRequest(deviceMac, requestId, service, characteristic, notifyHandler);
			} else {
				connection.requestQueue.add(new BleRequest(BleRequest.RequestType.READ_CHARACTERISTIC, requestId, service, characteristic,
						null, notifyHandler));
			}
		}
	}

	/**
	 * 打开Notifications
	 * @param deviceMac 设备地址
	 * @param requestId 请求码
	 */
	public synchronized void requestCharacteristicNotification(String deviceMac, int requestId, UUID service, UUID characteristic, Handler notifyHandler) {
		Connection connection = connections.get(deviceMac);
		if (connection != null) {
			if (connection.currentRequest == null) {
				performNotificationRequest(deviceMac, requestId, service, characteristic, notifyHandler);
			} else {
				connection.requestQueue.add(new BleRequest(BleRequest.RequestType.CHARACTERISTIC_NOTIFICATION, requestId, service,
						characteristic, null, notifyHandler));
			}
		}
	}

	public synchronized void requestCharacteristicIndication(String deviceMac, int requestId, UUID service, UUID characteristic, Handler notifyHandler) {
		Connection connection = connections.get(deviceMac);
		if (connection != null) {
			if (connection.currentRequest == null) {
				performIndicationRequest(deviceMac, requestId, service, characteristic, notifyHandler);
			} else {
				connection.requestQueue.add(new BleRequest(BleRequest.RequestType.CHARACTERISTIC_INDICATION, requestId, service,
						characteristic, null, notifyHandler));
			}
		}
	}

	public synchronized void requestDescriptorValue(String deviceMac, int requestId, UUID service, UUID characteristic, UUID descriptor,
													Handler notifyHandler) {
		Connection connection = connections.get(deviceMac);
		if (connection != null) {
			if (connection.currentRequest == null) {
				performDescriptorValueRequest(deviceMac, requestId, service, characteristic, descriptor, notifyHandler);
			} else {
				connection.requestQueue.add(new BleRequest(BleRequest.RequestType.READ_DESCRIPTOR, requestId, service, characteristic,
						descriptor, notifyHandler));
			}
		}
	}

	public synchronized void writeCharacteristicValue(String deviceMac, int requestId, UUID service, UUID characteristic, byte[] value,
													  Handler notifyHandler) {
		Connection connection = connections.get(deviceMac);
		if (connection != null) {
			if (connection.currentRequest == null) {
				performCharacteristicWrite(deviceMac, requestId, service, characteristic, notifyHandler, value);
			} else {
				connection.requestQueue.add(new BleRequest(BleRequest.RequestType.WRITE_CHARACTERISTIC, requestId, service, characteristic,
						null, notifyHandler, value));
			}
		}
	}

	private void sendMessage(Handler h, int requestId, int msgId) {
		if (h != null) {
			Bundle bundle = new Bundle();
			Message msg = Message.obtain(h, msgId);
			bundle.putInt(Constant.EXTRA_REQUEST_ID, requestId);
			msg.setData(bundle);
			msg.sendToTarget();
		}
	}

	private synchronized void processNextRequest(String deviceMac) {
		Connection connection = connections.get(deviceMac);
		if (connection == null) return;
		if (connection.requestQueue.isEmpty()) {
			connection.currentRequest = null;
			return;
		}
		BleRequest request = connection.requestQueue.remove();
		switch (request.type) {
			case CHARACTERISTIC_NOTIFICATION:
				performNotificationRequest(deviceMac, request.requestId, request.service, request.characteristic,
						request.notifyHandler);
				break;
			case CHARACTERISTIC_INDICATION:
				performIndicationRequest(deviceMac, request.requestId, request.service, request.characteristic,
						request.notifyHandler);
				break;
			case READ_CHARACTERISTIC:
				performCharacteristicValueRequest(deviceMac, request.requestId, request.service, request.characteristic,
						request.notifyHandler);
				break;
			case READ_DESCRIPTOR:
				performDescriptorValueRequest(deviceMac, request.requestId, request.service, request.characteristic,
						request.descriptor, request.notifyHandler);
				break;
			case WRITE_CHARACTERISTIC:
				performCharacteristicWrite(deviceMac, request.requestId, request.service, request.characteristic,
						request.notifyHandler, request.value);
				break;
		}
	}

	private void performCharacteristicValueRequest(String deviceMac, int requestId, UUID service, UUID characteristic, Handler notifyHandler) {
		Connection connection = connections.get(deviceMac);
		if (connection != null) {
			connection.currentRequest = new BleRequest(BleRequest.RequestType.READ_CHARACTERISTIC, requestId, service, characteristic,
					null, notifyHandler);
			BluetoothGattService gattService = connection.bluetoothGatt.getService(service);
			if (gattService != null) {
				BluetoothGattCharacteristic gattCharacteristic = gattService.getCharacteristic(characteristic);
				if (gattCharacteristic != null) {
					if (!connection.bluetoothGatt.readCharacteristic(gattCharacteristic)) {
						sendMessage(notifyHandler, connection.currentRequest.requestId, Constant.MESSAGE_REQUEST_FAILED);
						processNextRequest(deviceMac);
					}
				} else {
					sendMessage(notifyHandler, connection.currentRequest.requestId, Constant.MESSAGE_REQUEST_NULL_CHARACTERISTIC);
					processNextRequest(deviceMac);
				}
			} else {
				sendMessage(notifyHandler, connection.currentRequest.requestId, Constant.MESSAGE_REQUEST_NULL_SERVICE);
				processNextRequest(deviceMac);
			}
		}
	}

	private void performCharacteristicWrite(String deviceMac, int requestId, UUID service, UUID characteristic, Handler notifyHandler, byte[] value) {
		Connection connection = connections.get(deviceMac);
		if (connection != null) {
			connection.currentRequest = new BleRequest(BleRequest.RequestType.WRITE_CHARACTERISTIC, requestId, service, characteristic,
					null, notifyHandler, value);
			BluetoothGattService gattService = connection.bluetoothGatt.getService(service);
			if (gattService != null) {
				BluetoothGattCharacteristic gattCharacteristic = gattService.getCharacteristic(characteristic);
				if (gattCharacteristic != null) {
					gattCharacteristic.setValue(value);
					if (!connection.bluetoothGatt.writeCharacteristic(gattCharacteristic)) {
						sendMessage(notifyHandler, connection.currentRequest.requestId, Constant.MESSAGE_REQUEST_FAILED);
						processNextRequest(deviceMac);
					}
				} else {
					sendMessage(notifyHandler, connection.currentRequest.requestId, Constant.MESSAGE_REQUEST_NULL_CHARACTERISTIC);
					processNextRequest(deviceMac);
				}
			} else {
				sendMessage(notifyHandler, connection.currentRequest.requestId, Constant.MESSAGE_REQUEST_NULL_SERVICE);
				processNextRequest(deviceMac);
			}
		}
	}

	private void performDescriptorValueRequest(String deviceMac, int requestId, UUID service, UUID characteristic, UUID descriptor, Handler notifyHandler) {
		Connection connection = connections.get(deviceMac);
		if (connection != null) {
			connection.currentRequest = new BleRequest(BleRequest.RequestType.READ_CHARACTERISTIC, requestId, service, characteristic,
					descriptor, notifyHandler);
			BluetoothGattService gattService = connection.bluetoothGatt.getService(service);
			if (gattService != null) {
				BluetoothGattCharacteristic gattCharacteristic = gattService.getCharacteristic(characteristic);
				if (gattCharacteristic != null) {
					BluetoothGattDescriptor gattDescriptor = gattCharacteristic.getDescriptor(descriptor);
					if (gattDescriptor != null) {
						if (!connection.bluetoothGatt.readDescriptor(gattDescriptor)) {
							sendMessage(notifyHandler, connection.currentRequest.requestId, Constant.MESSAGE_REQUEST_FAILED);
							processNextRequest(deviceMac);
						}
					} else {
						sendMessage(notifyHandler, connection.currentRequest.requestId, Constant.MESSAGE_REQUEST_NULL_DESCRIPTOR);
						processNextRequest(deviceMac);
					}
				} else {
					sendMessage(notifyHandler, connection.currentRequest.requestId, Constant.MESSAGE_REQUEST_NULL_CHARACTERISTIC);
					processNextRequest(deviceMac);
				}
			} else {
				sendMessage(notifyHandler, connection.currentRequest.requestId, Constant.MESSAGE_REQUEST_NULL_SERVICE);
				processNextRequest(deviceMac);
			}
		}
	}

	private void performIndicationRequest(String deviceMac, int requestId, UUID service, UUID characteristic, Handler notifyHandler) {
		Connection connection = connections.get(deviceMac);
		if (connection != null) {
			connection.currentRequest = new BleRequest(BleRequest.RequestType.CHARACTERISTIC_INDICATION, requestId, service, characteristic,
					null, notifyHandler);
			BluetoothGattService gattService = connection.bluetoothGatt.getService(service);
			if (gattService != null) {
				connection.pendingCharacteristic = gattService.getCharacteristic(characteristic);
				if (connection.pendingCharacteristic != null) {
					BluetoothGattDescriptor gattDescriptor = connection.pendingCharacteristic.getDescriptor(UuidLib.CLIENT_CHARACTERISTIC_CONFIG);
					if (gattDescriptor == null || !connection.bluetoothGatt.readDescriptor(gattDescriptor)) {
						sendMessage(notifyHandler, connection.currentRequest.requestId, Constant.MESSAGE_REQUEST_FAILED);
						processNextRequest(deviceMac);
					}
				} else {
					sendMessage(notifyHandler, connection.currentRequest.requestId, Constant.MESSAGE_REQUEST_NULL_CHARACTERISTIC);
					processNextRequest(deviceMac);
				}
			} else {
				sendMessage(notifyHandler, connection.currentRequest.requestId, Constant.MESSAGE_REQUEST_NULL_SERVICE);
				processNextRequest(deviceMac);
			}
		}
	}

	private void performNotificationRequest(String deviceMac, int requestId, UUID service, UUID characteristic, Handler notifyHandler) {
		Connection connection = connections.get(deviceMac);
		if (connection != null) {
			connection.currentRequest = new BleRequest(BleRequest.RequestType.CHARACTERISTIC_NOTIFICATION, requestId, service, characteristic,
					null, notifyHandler);
			BluetoothGattService gattService = connection.bluetoothGatt.getService(service);
			if (gattService != null) {
				connection.pendingCharacteristic = gattService.getCharacteristic(characteristic);
				if (connection.pendingCharacteristic != null) {
					BluetoothGattDescriptor gattDescriptor = connection.pendingCharacteristic.getDescriptor(UuidLib.CLIENT_CHARACTERISTIC_CONFIG);
					if (gattDescriptor == null || !connection.bluetoothGatt.readDescriptor(gattDescriptor)) {
						sendMessage(notifyHandler, connection.currentRequest.requestId, Constant.MESSAGE_REQUEST_FAILED);
						processNextRequest(deviceMac);
					}
				} else {
					sendMessage(notifyHandler, connection.currentRequest.requestId, Constant.MESSAGE_REQUEST_NULL_CHARACTERISTIC);
					processNextRequest(deviceMac);
				}
			} else {
				sendMessage(notifyHandler, connection.currentRequest.requestId, Constant.MESSAGE_REQUEST_NULL_SERVICE);
				processNextRequest(deviceMac);
			}
		}
	}

	private boolean enableNotification(String deviceMac, boolean enable, BluetoothGattCharacteristic characteristic) {
		Connection connection = connections.get(deviceMac);
		if (connection == null) return false;
		if (!connection.bluetoothGatt.setCharacteristicNotification(characteristic, enable)) return false;
		BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UuidLib.CLIENT_CHARACTERISTIC_CONFIG);
		if (descriptor == null) return false;

		if (enable) {
			descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
		} else {
			descriptor.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
		}

		return connection.bluetoothGatt.writeDescriptor(descriptor);
	}

	private boolean enableIndication(String deviceMac, boolean enable, BluetoothGattCharacteristic characteristic) {
		Connection connection = connections.get(deviceMac);
		if (connection == null) return false;
		if (!connection.bluetoothGatt.setCharacteristicNotification(characteristic, enable)) return false;
		BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UuidLib.CLIENT_CHARACTERISTIC_CONFIG);
		if (descriptor == null) return false;

		if (enable) {
			descriptor.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
		}
		else {
			descriptor.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
		}

		return connection.bluetoothGatt.writeDescriptor(descriptor);
	}
	
	private BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
		@Override
		public void onConnectionStateChange(final BluetoothGatt gatt, final int status, int newState) {            
			final Connection connection = connections.get(gatt.getDevice().getAddress());
			if (connection == null) return;
			if (status == BluetoothGatt.GATT_SUCCESS && newState == BluetoothProfile.STATE_CONNECTED) {
                ThreadMgr.INSTANCE.getSPool().execute(new Runnable() {
                    @Override
                    public void run() {
                        LogUtil.saveLog(logFile, gatt.getDevice().getAddress() + "--" +
                                DateUtils.formatDate(new Date(), "MM/dd HH:mm:ss.SSS") + "> Connected to GATT server");                        
                    }
                });
                connection.handler.sendEmptyMessage(Constant.MESSAGE_CONNECTED);
				LogUtil.d(TAG, "Connected to GATT server. " + connection.bluetoothDevice.getAddress());
				// 搜索支持的服务
/*                UiUtils.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (connection.bluetoothGatt != null) {
                            connection.bluetoothGatt.discoverServices();
                        }
                    }
                }, 500); */
				if (connection.bluetoothGatt != null) {
					connection.bluetoothGatt.discoverServices();
				}
			} else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                ThreadMgr.INSTANCE.getSPool().execute(new Runnable() {
                    @Override
                    public void run() {
                        LogUtil.saveLog(logFile, gatt.getDevice().getAddress() + "--" +
                                DateUtils.formatDate(new Date(), "MM/dd HH:mm:ss.SSS") +
                                "> Disconnected from GATT server. status code: " + status);
                    }
                });
                
				LogUtil.d(TAG, "Disconnected from GATT server. " + connection.bluetoothDevice.getAddress());				
				connection.requestQueue.clear();
				connection.currentRequest = null;
                refresh(connection.bluetoothGatt);
                connection.bluetoothGatt.close();
				connection.handler.sendEmptyMessage(Constant.MESSAGE_DISCONNECTED);	
			} 
		}

		@Override
		public void onServicesDiscovered(final BluetoothGatt gatt, final int status) {            
            Connection connection = connections.get(gatt.getDevice().getAddress());                
			if (status == BluetoothGatt.GATT_SUCCESS) {
                LogUtil.d(TAG, "onServicesDiscovered. " + gatt.getDevice().getAddress());
				if (connection != null) {
					List<BluetoothGattService> services = connection.bluetoothGatt.getServices();
					if (services != null) {
                        boolean bootloader = false;
						for (BluetoothGattService gattService : services) {
							//如果还是BOOTLOADER模式
							if (gattService.getUuid().equals(UuidLib.CSR_OTA_BOOTLOADER_SERVICE)) {
								bootloader = true;
                                UiUtils.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        MainActivity mainActivity = (MainActivity) BaseActivity.getActivity(MainActivity.class.getName());
                                        Activity devMgrActivity = BaseActivity.getActivity(DeviceManagerActivity.class.getName());
                                        if (devMgrActivity == null && mainActivity != null) {
                                            mainActivity.showFirmwareErrorDialog();
                                        }
                                    }
                                });
                            }
						}
                        State.setWorkMode(connection.bluetoothDevice.getAddress(), bootloader ? State.WorkMode.OTA : State.WorkMode.APP);
                        ThreadMgr.INSTANCE.getSPool().execute(new Runnable() {
                            @Override
                            public void run() {
                                LogUtil.saveLog(logFile, gatt.getDevice().getAddress() + "--" +
                                        DateUtils.formatDate(new Date(), "MM/dd HH:mm:ss.SSS") + "> Discover Services Success.");
                            }
                        });                        
						connection.connecting = false;
						connection.connectingTimeCount = 0;
						connection.handler.sendEmptyMessage(Constant.MESSAGE_SERVICES_DISCOVERED);
					}
				}
			} else {
                gatt.disconnect();
                refresh(gatt);
                gatt.close();
                if (connection != null && connection.bluetoothGatt != null) {
                    connection.bluetoothGatt.disconnect();
                    refresh(connection.bluetoothGatt);
                    connection.bluetoothGatt.close();
                }
                if (status == 133) {
                    UiUtils.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            bluetoothAdapter.disable();
                        }
                    });
                }
                ThreadMgr.INSTANCE.getSPool().execute(new Runnable() {
                    @Override
                    public void run() {
                        LogUtil.saveLog(logFile, gatt.getDevice().getAddress() + "--" + DateUtils.formatDate(new Date(), "MM/dd HH:mm:ss.SSS") +
                                "> Discover Services error. status code: " + status);
                    }
                });
                LogUtil.d(TAG, "onServicesDiscovered error, status: " + status);
			}
		}

		@Override
		public void onCharacteristicRead(BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic, int status) {
			// 读取到值
			Connection connection = connections.get(gatt.getDevice().getAddress());
			if (connection == null) return;
			if (connection.currentRequest.type == BleRequest.RequestType.READ_CHARACTERISTIC) {
				if (connection.currentRequest.notifyHandler != null) {					
					if (status == BluetoothGatt.GATT_SUCCESS) {
						Bundle bundle = new Bundle();
						Message msg = Message.obtain(connection.currentRequest.notifyHandler, Constant.MESSAGE_CHARACTERISTIC_VALUE);
						bundle.putByteArray(Constant.EXTRA_VALUE, characteristic.getValue());
						bundle.putParcelable(Constant.EXTRA_SERVICE_UUID, new ParcelUuid(characteristic.getService().getUuid()));
						bundle.putParcelable(Constant.EXTRA_CHARACTERISTIC_UUID, new ParcelUuid(characteristic.getUuid()));
						bundle.putInt(Constant.EXTRA_REQUEST_ID, connection.currentRequest.requestId);
						msg.setData(bundle);
						msg.sendToTarget();
					} else {
						sendMessage(connection.currentRequest.notifyHandler, connection.currentRequest.requestId, Constant.MESSAGE_REQUEST_FAILED);
					}
				}
				processNextRequest(gatt.getDevice().getAddress());
			}
		}

		@Override
		public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
			String deviceMac = gatt.getDevice().getAddress();
			Connection connection = connections.get(deviceMac);
			if (connection == null || connection.currentRequest == null) return;
			if (connection.currentRequest.type == BleRequest.RequestType.WRITE_CHARACTERISTIC) {
				if (connection.currentRequest.notifyHandler != null) {
					if (status == BluetoothGatt.GATT_SUCCESS) {
						Bundle bundle = new Bundle();
						Message msg = Message.obtain(connection.currentRequest.notifyHandler, Constant.MESSAGE_WRITE_COMPLETE);
						bundle.putByteArray(Constant.EXTRA_VALUE, characteristic.getValue());
						bundle.putParcelable(Constant.EXTRA_SERVICE_UUID, new ParcelUuid(characteristic.getService().getUuid()));
						bundle.putParcelable(Constant.EXTRA_CHARACTERISTIC_UUID, new ParcelUuid(characteristic.getUuid()));
						bundle.putInt(Constant.EXTRA_REQUEST_ID, connection.currentRequest.requestId);
						msg.setData(bundle);
						msg.sendToTarget();
					} else if (status == GATT_REQ_NOT_SUPPORTED) {
						sendMessage(connection.currentRequest.notifyHandler, connection.currentRequest.requestId, Constant.MESSAGE_GATT_STATUS_REQUEST_NOT_SUPPORTED);
					} else {
						sendMessage(connection.currentRequest.notifyHandler, connection.currentRequest.requestId, Constant.MESSAGE_REQUEST_FAILED);
					}
				}
				processNextRequest(deviceMac);
			}
		}

		@Override
		public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
			// 收到设备notify值 （设备上报值）
			Handler notificationHandler = notificationHandlers.getHandler(gatt.getDevice().getAddress(), 
					characteristic.getService().getUuid(), characteristic.getUuid());
			if (notificationHandler != null) {
				Bundle bundle = new Bundle();
				Message msg = Message.obtain(notificationHandler, Constant.MESSAGE_CHARACTERISTIC_VALUE);
				bundle.putByteArray(Constant.EXTRA_VALUE, characteristic.getValue());
				bundle.putParcelable(Constant.EXTRA_SERVICE_UUID, new ParcelUuid(characteristic.getService().getUuid()));
				bundle.putParcelable(Constant.EXTRA_CHARACTERISTIC_UUID, new ParcelUuid(characteristic.getUuid()));
				msg.setData(bundle);
				msg.sendToTarget();
			}			
		}

		@Override
		public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
			String deviceMac = gatt.getDevice().getAddress();
			Connection connection = connections.get(deviceMac);
			if (connection == null || connection.currentRequest == null) return;
			BluetoothGattCharacteristic characteristic = descriptor.getCharacteristic();
			if (connection.currentRequest.type == BleRequest.RequestType.CHARACTERISTIC_NOTIFICATION) {
				if (status != BluetoothGatt.GATT_SUCCESS) {					
					sendMessage(connection.currentRequest.notifyHandler, connection.currentRequest.requestId, Constant.MESSAGE_REQUEST_FAILED);
					notificationHandlers.removeHandler(deviceMac, characteristic.getService().getUuid(), characteristic.getUuid());
				}
				if (characteristic.getService().getUuid().equals(connection.pendingCharacteristic.getService().getUuid())
						&& characteristic.getUuid().equals(connection.pendingCharacteristic.getUuid())) {
					notificationHandlers.addHandler(deviceMac, characteristic.getService().getUuid(), characteristic.getUuid(),
							connection.currentRequest.notifyHandler);
					if (!enableNotification(deviceMac, true, characteristic)) {
						sendMessage(connection.currentRequest.notifyHandler, connection.currentRequest.requestId, Constant.MESSAGE_REQUEST_FAILED);
						notificationHandlers.removeHandler(deviceMac, characteristic.getService().getUuid(), characteristic.getUuid());
					}
				}
			} else if (connection.currentRequest.type == BleRequest.RequestType.CHARACTERISTIC_INDICATION) {
				if (status != BluetoothGatt.GATT_SUCCESS) {
					sendMessage(connection.currentRequest.notifyHandler, connection.currentRequest.requestId, Constant.MESSAGE_REQUEST_FAILED);
					notificationHandlers.removeHandler(deviceMac, characteristic.getService().getUuid(), characteristic.getUuid());
				}
				if (characteristic.getService().getUuid().equals(connection.pendingCharacteristic.getService().getUuid())
						&& characteristic.getUuid().equals(connection.pendingCharacteristic.getUuid())) {
					notificationHandlers.addHandler(deviceMac, characteristic.getService().getUuid(), characteristic.getUuid(),
							connection.currentRequest.notifyHandler);
					if (!enableIndication(deviceMac, true, characteristic)) {
						sendMessage(connection.currentRequest.notifyHandler, connection.currentRequest.requestId, Constant.MESSAGE_REQUEST_FAILED);
						notificationHandlers.removeHandler(deviceMac, characteristic.getService().getUuid(), characteristic.getUuid());
					}
				}
			} else if (connection.currentRequest.type == BleRequest.RequestType.READ_DESCRIPTOR) {
				if (status == BluetoothGatt.GATT_SUCCESS) {
					Bundle bundle = new Bundle();
					Message msg = Message.obtain(connection.currentRequest.notifyHandler, Constant.MESSAGE_DESCRIPTOR_VALUE);
					bundle.putByteArray(Constant.EXTRA_VALUE, characteristic.getValue());
					bundle.putParcelable(Constant.EXTRA_SERVICE_UUID, new ParcelUuid(characteristic.getService()
							.getUuid()));
					bundle.putParcelable(Constant.EXTRA_CHARACTERISTIC_UUID, new ParcelUuid(characteristic.getUuid()));
					bundle.putParcelable(Constant.EXTRA_DESCRIPTOR_UUID, new ParcelUuid(descriptor.getUuid()));
					bundle.putInt(Constant.EXTRA_REQUEST_ID, connection.currentRequest.requestId);
					msg.setData(bundle);
					msg.sendToTarget();
					processNextRequest(deviceMac);
				} else {
					sendMessage(connection.currentRequest.notifyHandler, connection.currentRequest.requestId, Constant.MESSAGE_REQUEST_FAILED);
				}
			}
		}

		@Override
		public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
			String deviceMac = gatt.getDevice().getAddress();
			Connection connection = connections.get(deviceMac);
			if (connection == null || connection.currentRequest == null) return;
			BluetoothGattCharacteristic characteristic = descriptor.getCharacteristic();
			if (connection.currentRequest.type == BleRequest.RequestType.CHARACTERISTIC_NOTIFICATION) {
				if (status != BluetoothGatt.GATT_SUCCESS) {
					sendMessage(connection.currentRequest.notifyHandler, connection.currentRequest.requestId, Constant.MESSAGE_REQUEST_FAILED);
					notificationHandlers.removeHandler(deviceMac, characteristic.getService().getUuid(), characteristic.getUuid());
				} else {
					Bundle bundle = new Bundle();
					Message msg = Message.obtain(connection.currentRequest.notifyHandler, Constant.MESSAGE_NOTIFICATION_REGISTERED);
					bundle.putParcelable(Constant.EXTRA_SERVICE_UUID, new ParcelUuid(characteristic.getService()
							.getUuid()));
					bundle.putParcelable(Constant.EXTRA_CHARACTERISTIC_UUID, new ParcelUuid(characteristic.getUuid()));
					bundle.putParcelable(Constant.EXTRA_DESCRIPTOR_UUID, new ParcelUuid(descriptor.getUuid()));
					bundle.putInt(Constant.EXTRA_REQUEST_ID, connection.currentRequest.requestId);
					msg.setData(bundle);
					msg.sendToTarget();
				}
				processNextRequest(deviceMac);
			} else if (connection.currentRequest.type == BleRequest.RequestType.CHARACTERISTIC_INDICATION) {
				if (status != BluetoothGatt.GATT_SUCCESS) {
					sendMessage(connection.currentRequest.notifyHandler, connection.currentRequest.requestId, Constant.MESSAGE_REQUEST_FAILED);
					notificationHandlers.removeHandler(deviceMac, characteristic.getService().getUuid(), characteristic.getUuid());
				} else {
					Bundle bundle = new Bundle();
					Message msg = Message.obtain(connection.currentRequest.notifyHandler, Constant.MESSAGE_INDICATION_REGISTERED);
					bundle.putParcelable(Constant.EXTRA_SERVICE_UUID, new ParcelUuid(characteristic.getService()
							.getUuid()));
					bundle.putParcelable(Constant.EXTRA_CHARACTERISTIC_UUID, new ParcelUuid(characteristic.getUuid()));
					bundle.putParcelable(Constant.EXTRA_DESCRIPTOR_UUID, new ParcelUuid(descriptor.getUuid()));
					bundle.putInt(Constant.EXTRA_REQUEST_ID, connection.currentRequest.requestId);
					msg.setData(bundle);
					msg.sendToTarget();
				}
				processNextRequest(deviceMac);
			}
		}
	};	
}
