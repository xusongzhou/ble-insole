package com.advanpro.fwtools.module.more;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.advanpro.fwtools.Constant;
import com.advanpro.fwtools.MyApplication;
import com.advanpro.fwtools.R;
import com.advanpro.fwtools.ble.BleDevice;
import com.advanpro.fwtools.ble.BleObservable;
import com.advanpro.fwtools.ble.BleObserver;
import com.advanpro.fwtools.ble.DeviceMgr;
import com.advanpro.fwtools.common.base.BaseHolder;
import com.advanpro.fwtools.common.base.BaseListAdapter;
import com.advanpro.fwtools.common.base.BasePagerAdapter;
import com.advanpro.fwtools.common.manager.ToastMgr;
import com.advanpro.fwtools.common.util.LogUtil;
import com.advanpro.fwtools.common.util.UiUtils;
import com.advanpro.fwtools.common.view.MyViewPager;
import com.advanpro.fwtools.common.view.TitleBar;
import com.advanpro.fwtools.common.view.ViewPagerIndicator;
import com.advanpro.fwtools.entity.State;
import com.advanpro.fwtools.module.BaseActivity;
import com.advanpro.fwtools.module.MainActivity;
import com.advanpro.ascloud.ASCloud;
import com.advanpro.ascloud.CloudCallback;
import com.advanpro.ascloud.CloudException;
import com.advanpro.ascloud.CloudMsg;
import com.zxing.android.ScanQRCodeActivity;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by zengfs on 2016/1/28.
 * 设备管理
 */
public class DeviceManagerActivity extends BaseActivity implements DeviceTabView.OnUnbindClickListener, AdapterView.OnItemClickListener, ViewPagerIndicator.OnTabCheckListener {
	private static final int SCAN_PERIOD = 5000;
	private static final int REQUEST_SCAN_QR_CODE = 100;
	private TitleBar titleBar;
	private ViewPagerIndicator viewPagerIndicator;
	private MyViewPager viewPager;
	private List<DeviceInfoPager> pagers = new ArrayList<>();
	private DeviceTabView leftDeviceTab, rightDeviceTab;
	private DeviceInfoPager leftDeviceInfoPager, rightDeviceInfoPager;
	public BluetoothAdapter bluetoothAdapter;
	private TextView tvAction;
	private ImageView progressBar;
	private View scanView;
	private boolean isScanning;
	private ListView lvDevice;
	private DeviceListAdapter deviceListAdapter;
	private MainActivity mainActivity;    
    private MyObserver observer;
    private List<BleDevice> leftDevs = new ArrayList<>();
    private List<BleDevice> rightDevs = new ArrayList<>();
    private AlertDialog bindFaileDialog;
    private BleDevice lastBindFaileDev;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_device_manager);
		mainActivity = (MainActivity) BaseActivity.getActivity(MainActivity.class.getName());
		if (mainActivity == null) {
			finish();
			return;
		}
		init();
		assignViews();
		initViews();
	}

	private void init() {
		BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
		if (bluetoothManager != null) bluetoothAdapter = bluetoothManager.getAdapter();	        
        observer = new MyObserver(ObservableMgr.getBleObservable());
	}

	protected void assignViews() {
		titleBar = (TitleBar) findViewById(R.id.title_bar);
		viewPagerIndicator = (ViewPagerIndicator) findViewById(R.id.view_pager_indicator);
		viewPager = (MyViewPager) findViewById(R.id.view_pager);
		tvAction = (TextView) findViewById(R.id.tv_action);
		progressBar = (ImageView) findViewById(R.id.iv_progress);
		scanView = findViewById(R.id.ll_scan);
		lvDevice = (ListView) findViewById(R.id.lv_device);
	}

	protected void initViews() {
		initTitleBar();
		((AnimationDrawable) progressBar.getBackground()).start();//进度条转动起来
		viewPagerIndicator.setCheckedBackgroundEnabled(true);
		viewPagerIndicator.setIndicatorRadio(0.45f);
		initDeviceTabAndDeviceInfoPager();
		viewPagerIndicator.setTabContentViews(getDeviceTab(true).getRootView(), getDeviceTab(false).getRootView());
		viewPagerIndicator.setOnTabCheckListener(this);
		
		viewPager.setAdapter(new BasePagerAdapter(pagers));
		viewPager.setTouchEnabled(false);
		viewPagerIndicator.setViewPager(viewPager);
		viewPagerIndicator.setPagerSmoothScrollEnabled(false);

		updateScanView();
		deviceListAdapter = new DeviceListAdapter(leftDevs);
		lvDevice.setAdapter(deviceListAdapter);
		lvDevice.setOnItemClickListener(this);
		tvAction.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {	
				if (!isScanning) {
					if (!mainActivity.isBluetoothAvailable()) {
						new AlertDialog.Builder(DeviceManagerActivity.this).setMessage(R.string.not_support_ble)
								.setNegativeButton(R.string.ok, null).show();
					} else if (bluetoothAdapter == null) {
						new AlertDialog.Builder(DeviceManagerActivity.this).setCancelable(false)
								.setMessage(R.string.ble_init_failed)
								.setNegativeButton(R.string.ignore, null)
								.setPositiveButton(R.string.exit, new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog, int which) {
										completeExit();
									}
								}).show();
					} else if (!bluetoothAdapter.isEnabled()) {
						mainActivity.requestEnableBle();
					} else {
						scanDevice(true);
					}
				} else {
				    scanDevice(false);
				}			
			}
		});
		showScanView(isUnbound(viewPagerIndicator.getCurrentPosition()));
	}

	@Override
	protected void onResume() {
		super.onResume();
		//选中标签未绑定，搜索设备
		if (isUnbound(viewPagerIndicator.getCurrentPosition()) && 
				bluetoothAdapter != null && bluetoothAdapter.isEnabled()) {
			scanDevice(true);
		}
        getDeviceInfoPager(true).updateView();
        getDeviceInfoPager(false).updateView();
	}

	/**
	 * 选中标签未绑定
	 * @param position 指示器标签索引
	 */
	private boolean isUnbound(int position) {
		return DeviceMgr.getBoundDevice(position == 0) == null;
	}
	
	private void showScanView(boolean enable) {
		if (enable) {
			scanView.setVisibility(View.VISIBLE);
			viewPager.setVisibility(View.INVISIBLE);
		} else {
			scanView.setVisibility(View.INVISIBLE);
			viewPager.setVisibility(View.VISIBLE);
		}
	}
	
	private void updateScanView() {
		Drawable drawable;
		if (isScanning) {
			drawable = getResources().getDrawable(R.drawable.clear);
			tvAction.setText(R.string.stop_search);
			progressBar.setVisibility(View.VISIBLE);
		} else {
			drawable = getResources().getDrawable(R.drawable.search);
			tvAction.setText(R.string.search_device);
			progressBar.setVisibility(View.INVISIBLE);
		}

		//设置TextView的Drawable大小
		if (drawable != null) {
			drawable.setBounds(0, 0, UiUtils.dip2px(32), UiUtils.dip2px(32));
			tvAction.setCompoundDrawables(null, drawable, null, null);
		}
	}

	// 扫描与停止扫描设备
	private void scanDevice(boolean enable) {
		if (bluetoothAdapter == null || isScanning == enable) return;		
		if (enable) {
			deviceListAdapter.clear();
			//扫描周期
			MyApplication.getHandler().postDelayed(new Runnable() {
				@Override
				public void run() {
                    bluetoothAdapter.stopLeScan(leScanCallback);
                    isScanning = false;
                    updateScanView();					
				}
			}, SCAN_PERIOD);
			bluetoothAdapter.startLeScan(leScanCallback);
			isScanning = true;
		} else {
			bluetoothAdapter.stopLeScan(leScanCallback);
			isScanning = false;
		}
		updateScanView();
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		BleDevice device = deviceListAdapter.getData().get(position);
		if (DeviceMgr.existDiffType(device.type)) {
			ToastMgr.showTextToast(this, 0, R.string.can_not_bind_diff_type_device);
		} else {
		    DeviceMgr.bindDevice(device);
			getDeviceTab(device.isLeft).setBound(true);
			deviceListAdapter.getData().remove(position);
			deviceListAdapter.notifyDataSetChanged();
            getDeviceInfoPager(device.isLeft).updateView();
		}
	}
	
	//初始化指示器Tab和ViewPager的pager
	private void initDeviceTabAndDeviceInfoPager() {		;
		for (int i = 0; i < 2; i++) {
			boolean isLeft = i == 0;
			pagers.add(getDeviceInfoPager(isLeft));
			getDeviceTab(isLeft).setOnUnbindClickListener(this);
			BleDevice bleDevice = DeviceMgr.getBoundDevice(isLeft);
			if (bleDevice != null) {
				getDeviceTab(isLeft).setBound(true);
				State.ConnectionState connectionState = State.getConnectionState(bleDevice.mac);
				if (connectionState != null) {					
					getDeviceTab(isLeft).updateView(connectionState);
				}
			}
		}		
	}

	private void initTitleBar() {
		titleBar.setTitle(R.string.connect_device);
		titleBar.setStartImageButtonVisible(true);
		titleBar.setEndImageButtonVisible(true);
		titleBar.setEndImageButtonSrc(R.drawable.scan);
		titleBar.setOnMenuClickListener(new TitleBar.OnMenuClickListener() {
			@Override
			public void onMenuClick(View v) {
				switch(v.getId()) {
				    case R.id.btn_end://扫描二维码
						startActivityForResult(new Intent(DeviceManagerActivity.this, ScanQRCodeActivity.class),
								REQUEST_SCAN_QR_CODE);
						break;
					case R.id.btn_start:
                        onBackPressed();
                        break;
				}
			}
		});
	}

    @Override
    public void onBackPressed() {
        //要么一只不绑定，要么必须绑定两只        
        if (DeviceMgr.getBoundDeviceCount() < 2 && DeviceMgr.getBoundDeviceCount() > 0) {
//            ToastMgr.showTextToast(this, 0, getString(R.string.only_bind_one));
        }
        super.onBackPressed();
    }

    private DeviceInfoPager getDeviceInfoPager(boolean isLeft) {
		if (isLeft) {
		    if (leftDeviceInfoPager == null) leftDeviceInfoPager = new DeviceInfoPager(this, true);
			return leftDeviceInfoPager;
		} else {
		    if (rightDeviceInfoPager == null) rightDeviceInfoPager = new DeviceInfoPager(this, false);
			return rightDeviceInfoPager;
		}
	}
	
	public DeviceTabView getDeviceTab(boolean isLeft) {
		if (isLeft) {
		    if (leftDeviceTab == null) leftDeviceTab = new DeviceTabView(true, this);
			return leftDeviceTab;
		} else {
		    if (rightDeviceTab == null) rightDeviceTab = new DeviceTabView(false, this);
			return rightDeviceTab;
		}
	}
		
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		//成功扫描到二维码
		if (requestCode == REQUEST_SCAN_QR_CODE && resultCode == RESULT_OK) {
			String devId = data.getStringExtra(ScanQRCodeActivity.EXTRA_QR_RESULT);
			LogUtil.d("tag", "ansobuy--" + devId);
			if (devId != null) {
//				if (!devId.startsWith("AVPSIS")) {
//					ToastMgr.showTextToast(DeviceManagerActivity.this, 0, R.string.invalid_qr_code);
//					return;
//				}
			    //从云端获取设备信息
				CloudMsg req = new CloudMsg("/device/get");
				req.put("devId", devId);
				ASCloud.sendMsg(req, new CloudCallback() {
					@Override
					public void success(CloudMsg cloudMsg) {
						processBind(cloudMsg);
					}

					@Override
					public void error(CloudException e) {
						if (e.error == 3003) {
							ToastMgr.showTextToast(DeviceManagerActivity.this, 0, R.string.no_device_info);
                            CloudMsg cloudMsg = new CloudMsg("");
                            
						} else {
							ToastMgr.showTextToast(DeviceManagerActivity.this, 0, e.msg);
						}
					}
				});
			}
		}
	}

	//获取到设备信息后，绑定设备
	private void processBind(CloudMsg msg) {
		try {
			BleDevice device = new BleDevice();
			device.devId = msg.getString("devId");
			device.isLeft = Integer.parseInt(device.devId.substring(device.devId.length() - 1)) % 2 != 0;
			switch(device.devId.charAt(6)) {
			    case 'A':
					device.type = Constant.DEVICE_TYPE_BASIC;
					break;
				case 'B':
					device.type = Constant.DEVICE_TYPE_POPULARITY;
					break;
				case 'C':
					device.type = Constant.DEVICE_TYPE_ENHANCED;
					break;
			}
			device.mac = msg.getString("macAddr");
			//绑定
			if (DeviceMgr.existDiffType(device.type)) {
				ToastMgr.showTextToast(this, 0, R.string.can_not_bind_diff_type_device);
			} else {
				DeviceMgr.bindDevice(device);
				getDeviceTab(device.isLeft).setBound(true);
				deviceListAdapter.getData().remove(device);
				deviceListAdapter.notifyDataSetChanged();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}

	@Override
	public void onUnbindClick(boolean isLeft) {
		DeviceMgr.unbindDevice(isLeft);
        getDeviceInfoPager(isLeft).updateView();
		if (isScanning) scanDevice(false);//如果正在搜索，停止
		scanDevice(true);
	}

	@Override
	protected void onDestroy() {
		ObservableMgr.getBleObservable().deleteObserver(observer);
        for (DeviceInfoPager pager : pagers) {
            pager.destroy();
        }        
        super.onDestroy();
	}

	private BluetoothAdapter.LeScanCallback leScanCallback = new BluetoothAdapter.LeScanCallback() {
		@Override
		public void onLeScan(final BluetoothDevice device, final int rssi, final byte[] scanRecord) {
			UiUtils.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					ByteBuffer byteBuffer = ByteBuffer.wrap(scanRecord);
					while (byteBuffer.remaining() != 0) {
						int len = byteBuffer.get() & 0xFF;
						int type = byteBuffer.get() & 0xFF;
						if (len < 2) break;
						byte[] value = new byte[len - 1];
						byteBuffer.get(value);
						//自定义广播字段
						if (type == 0xFF) {
							//解析广播内容，过滤及确定左右
							if ((value[0] & 0xFF) == 0xC1 && value[1] == 0x04) {
								BleDevice bleDevice = new BleDevice();
								bleDevice.name = TextUtils.isEmpty(device.getName()) ?
										getString(R.string.unknown_device) : device.getName();
								bleDevice.mac = device.getAddress();
								bleDevice.rssi = rssi;
								bleDevice.isLeft = (value[2] & 0x40) == 0;//bit6等于0为左，为1为右
								//判断产品版本：传感器个数/(基础、普及、增强)
								switch(value[2] & 0x38) {
								    case 0x00:
										bleDevice.type = Constant.DEVICE_TYPE_BASIC;
										break;
									case 0x08:
										bleDevice.type = Constant.DEVICE_TYPE_POPULARITY;
										break;
									case 0x10:
										bleDevice.type = Constant.DEVICE_TYPE_ENHANCED;
										break;
									default:
										continue;
								}
								//已绑定设备不添加到列表中
								if (DeviceMgr.getBoundDevice(bleDevice.mac) == null) {
                                    List<BleDevice> devs = bleDevice.isLeft ? leftDevs : rightDevs;
                                    if (!devs.contains(bleDevice)) {
                                        devs.add(bleDevice);
                                        Collections.sort(devs);
                                        deviceListAdapter.notifyDataSetChanged();
                                    }
								}
							}
						}
					} 
				}
			});
		}		
	};

	@Override
	public void onTabCheck(View contentView, int position) {		
		showScanView(isUnbound(position));
        deviceListAdapter.setData(position == 0 ? leftDevs : rightDevs);
	}

	private class DeviceListAdapter extends BaseListAdapter<BleDevice> {

        public DeviceListAdapter(List<BleDevice> leftDevs) {
            super(leftDevs);
        }

        public void clear() {
			getData().clear();
			notifyDataSetChanged();
		}

		@Override
		protected BaseHolder<BleDevice> getHolder() {
			return new BaseHolder<BleDevice>() {
				private TextView tvName;
				private TextView tvMac;
				private TextView tvLr;

				@Override
				protected void setData(BleDevice data, int position) {
					tvName.setText("VV: " + data.name);
					tvMac.setText(data.mac);
					tvLr.setText(data.isLeft ? R.string.left : R.string.right);
				}

				@Override
				protected View createConvertView() {
					View convertView = View.inflate(DeviceManagerActivity.this, R.layout.item_device, null);
					tvName = (TextView) convertView.findViewById(R.id.tv_name);
					tvMac = (TextView) convertView.findViewById(R.id.tv_mac);
					tvLr = (TextView) convertView.findViewById(R.id.tv_lr);
					return convertView;
				}
			};
		}
	}
	
	private class MyObserver extends BleObserver {
        public MyObserver(BleObservable bleObservable) {
            super(bleObservable);
        }

        @Override
        public void onConnectionStateChange(final BleDevice device, final State.ConnectionState state) {
            UiUtils.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    DeviceTabView deviceTab = getDeviceTab(device.isLeft);
                    DeviceInfoPager infoPager = getDeviceInfoPager(device.isLeft);
                    infoPager.setVisibility(state == State.ConnectionState.CONNECTED);
                    deviceTab.updateView(state);
                    showScanView(isUnbound(viewPagerIndicator.getCurrentPosition()));
                    if (state == State.ConnectionState.CONNECTED) {
                        DeviceMgr.cancelBindCheck(device.mac);
                        if (bindFaileDialog != null && device.equals(lastBindFaileDev))
                            bindFaileDialog.dismiss();
                    }
                }
            });
        }

        @Override
        public void onBatteryRead(final BleDevice device, final int battery) {
            UiUtils.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    getDeviceTab(device.isLeft).setBattery(battery);
                }
            });
        }

        @Override
        public void onBindStateChange(final BleDevice device, final int status) {
            UiUtils.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (status == Constant.BIND_FAILE) {
                        lastBindFaileDev = device;
                        if (bindFaileDialog == null) {
                            bindFaileDialog = new AlertDialog.Builder(DeviceManagerActivity.this)
                                    .setMessage(R.string.device_bind_faile)
                                    .setNegativeButton(R.string.ok, null).create();
                        }
                        if (!bindFaileDialog.isShowing()) bindFaileDialog.show();
                        getDeviceTab(device.isLeft).setBound(false);
                    }
                }
            });
        }
    }
}
