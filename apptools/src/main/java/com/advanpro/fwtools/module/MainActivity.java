package com.advanpro.fwtools.module;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.app.FragmentTabHost;
import android.widget.TabHost;

import com.advanpro.fwtools.R;
import com.advanpro.fwtools.ble.BleManager;
import com.advanpro.fwtools.ble.BleService;
import com.advanpro.fwtools.ble.DeviceMgr;
import com.advanpro.fwtools.common.manager.ToastMgr;
import com.advanpro.fwtools.common.util.LogUtil;
import com.advanpro.fwtools.module.activity.ActivityFragment;
import com.advanpro.fwtools.module.me.MyInfoFragment;
import com.advanpro.fwtools.module.more.DeviceManagerActivity;
import com.advanpro.fwtools.module.more.MoreFragment;
import com.advanpro.fwtools.module.stat.StatFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zengfs on 2016/1/13.
 */
public class MainActivity extends BaseActivity {
	private static final String[] tags = {"activity", "stat", "me", "more"};
	//正常与选中时的图片资源id
	private static final int[] tabResIds = {R.drawable.tab_actvity, R.drawable.tab_statistics,
			R.drawable.tab_me, R.drawable.tab_more};
	private static final Class[] fragClasses = {ActivityFragment.class, StatFragment.class,
			MyInfoFragment.class, MoreFragment.class};
    private static final int REQUEST_ENABLE_BT = 100;
    private List<TabIndicatorView> tabIndicators = new ArrayList<>();
	private FragmentTabHost tabHost;
	private BluetoothAdapter bluetoothAdapter;
	private boolean isBluetoothAvailable;
    private long lastClickTime;
    private AlertDialog firmwareErrorDialog;
    private AlertDialog restartBleDialog;
    private boolean isAllowEnableBle = true;
    private PowerManager.WakeLock wakeLock;
    private boolean isFocusable;

    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		DeviceMgr.loadCurrentPairInsoles();
        //检查是否绑定两只，没有则跳转到设备管理界面
        if (DeviceMgr.getBoundDeviceCount() < 2 && DeviceMgr.getBoundDeviceCount() > 0) {
//            ToastMgr.showTextToast(this, 0, getString(R.string.only_bind_one));
        }
		assignViews();
		initView();
		isBluetoothAvailable = initBle();
		initBroadcast();
		bindService(new Intent(this, BleService.class), serviceConn, BIND_AUTO_CREATE);
        startService(new Intent(this, DataSyncService.class));
        wakeLock = ((PowerManager) getSystemService(POWER_SERVICE))
                .newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE, "MainActivity");
	}

	private void initBroadcast() {
		IntentFilter intentFilter = new IntentFilter();
//        intentFilter.addAction(Intent.ACTION_TIME_TICK);
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
		registerReceiver(receiver, intentFilter);
	}


	private ServiceConnection serviceConn = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			if (isBluetoothAvailable) {
				BleManager.INSTANCE.setBleService(((BleService.LocalBinder) service).getService());
                DeviceMgr.connectCurrentPairInsoles();
			}
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			BleManager.INSTANCE.setBleService(null);
		}
	};

	private boolean initBle() {
		if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
			LogUtil.e("MainActivity", "ansobuy--not_support_ble.");
			return false;
		}

		BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
		if (bluetoothManager == null) {
			LogUtil.e("MainActivity", "ansobuy--Unable to initialize BluetoothManager.");
			return false;
		}

		bluetoothAdapter = bluetoothManager.getAdapter();
		if (bluetoothAdapter == null) {
			LogUtil.e("MainActivity", "ansobuy--Unable to obtain a BluetoothAdapter.");
			return false;
		}

		return true;
	}

	public boolean isBluetoothAvailable() {
		return isBluetoothAvailable;
	}

	protected void assignViews() {
		tabHost = (FragmentTabHost) findViewById(android.R.id.tabhost);
	}

	private void initView() {
		tabHost.setup(this, getSupportFragmentManager(), R.id.fl_container);
		//设置标签内容
		for (int i = 0; i < tags.length; i++) {
			TabIndicatorView tabIndicator = new TabIndicatorView(this);
			tabIndicators.add(tabIndicator);
			addTabSpec(tabIndicator, tags[i], tabResIds[i], fragClasses[i]);
		}
		tabHost.setCurrentTabByTag(tags[0]);
		tabIndicators.get(0).setSelected(true);
		//去掉中间的分隔线
		tabHost.getTabWidget().setDividerDrawable(android.R.color.transparent);
		tabHost.setOnTabChangedListener(listener);//设置切换监听
	}

	//添加并初始化标签
	private void addTabSpec(TabIndicatorView tabIndicator, String tag, int normalTabResId, Class fragClass) {
		TabHost.TabSpec tabSpec = tabHost.newTabSpec(tag);
		tabIndicator.setTabResource(normalTabResId);
		tabSpec.setIndicator(tabIndicator);
		tabHost.addTab(tabSpec, fragClass, null);
	}

	private TabHost.OnTabChangeListener listener = new TabHost.OnTabChangeListener() {
		@Override
		public void onTabChanged(String tabId) {
			for (int i = 0; i < tags.length; i++) {
				tabIndicators.get(i).setSelected(tags[i].equals(tabId));
			}
		}
	};

	@Override
	protected void onResume() {
		super.onResume();
        isFocusable = true;
		if (bluetoothAdapter != null && !bluetoothAdapter.isEnabled() && isAllowEnableBle) {
			requestEnableBle();
		}
	}

    @Override
    protected void onPause() {
        super.onPause();
        isFocusable = false;
        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();//释放屏幕常亮
        }
    }

    public void showFirmwareErrorDialog() {
        if (firmwareErrorDialog == null) {
            firmwareErrorDialog = new AlertDialog.Builder(this)
                    .setMessage(R.string.find_firmware_error)
                    .setPositiveButton(R.string.show_detail, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            startActivity(new Intent(MainActivity.this, DeviceManagerActivity.class));
                        }
                    }).create();
        }
        if (!firmwareErrorDialog.isShowing()) firmwareErrorDialog.show();
    }

    public void showRestartBleDialog() {
        if (restartBleDialog == null) {
            restartBleDialog = new AlertDialog.Builder(this)
                    .setMessage(R.string.restart_ble_request)
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (bluetoothAdapter != null) bluetoothAdapter.disable();
                        }
                    })
                    .setNegativeButton(R.string.cancel, null).create();
        }
        if (!restartBleDialog.isShowing()) restartBleDialog.show();
    }

	@Override
	protected void onDestroy() {
		BleManager.INSTANCE.removeAllConnections();
		unregisterReceiver(receiver);
		unbindService(serviceConn);
		super.onDestroy();
	}

	@Override
	public void onBackPressed() {
        //点击两次退出应用
        if (System.currentTimeMillis() - lastClickTime > 2000) {
            lastClickTime = System.currentTimeMillis();
            ToastMgr.showTextToast(this, 0, R.string.click_again_exit);
            return;
        }

		super.onBackPressed();
	}

	//监听手机蓝牙状态
	private BroadcastReceiver receiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(intent.getAction())) {
				BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
				if (bluetoothAdapter != null) {
					if (bluetoothAdapter.getState() == BluetoothAdapter.STATE_OFF) {
                        // 请求用户打开蓝牙
                        if (isAllowEnableBle) requestEnableBle();
					} else if ((bluetoothAdapter.getState() == BluetoothAdapter.STATE_ON ||
							bluetoothAdapter.getState() == BluetoothAdapter.STATE_TURNING_ON)) {
					    isAllowEnableBle = true;
					}
				}
			} else if (Intent.ACTION_TIME_TICK.equals(intent.getAction())) {
                if (ActivityFragment.instance != null) ActivityFragment.instance.onTimeChanged();
            }
		}
	};

    public void requestEnableBle() {
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT) {
            isAllowEnableBle = resultCode == RESULT_OK;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
