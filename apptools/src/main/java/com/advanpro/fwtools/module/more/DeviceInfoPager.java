package com.advanpro.fwtools.module.more;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.widget.TextView;

import com.advanpro.fwtools.Constant;
import com.advanpro.fwtools.R;
import com.advanpro.fwtools.ble.BleDevice;
import com.advanpro.fwtools.ble.BleManager;
import com.advanpro.fwtools.ble.BleObserver;
import com.advanpro.fwtools.ble.DeviceMgr;
import com.advanpro.fwtools.common.MyTimer;
import com.advanpro.fwtools.common.base.BasePager;
import com.advanpro.fwtools.common.manager.ToastMgr;
import com.advanpro.fwtools.common.util.DateUtils;
import com.advanpro.fwtools.common.util.LogUtil;
import com.advanpro.fwtools.common.util.UiUtils;
import com.advanpro.fwtools.common.view.ArrowItemView;
import com.advanpro.fwtools.entity.State;
import com.advanpro.fwtools.entity.VersionChecker;
import com.advanpro.fwtools.fwdebug.FwDebugActivity;
import com.advanpro.aswear.ASWear;
import com.advanpro.ota.OtaCallback;
import com.advanpro.ota.OtaUpdater;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Date;

/**
 * Created by zengfs on 2016/2/21.
 * 设备管理的设备信息
 */
public class DeviceInfoPager extends BasePager implements View.OnClickListener, OtaCallback, MyTimer.UiTimerTaskCallback {	
	private boolean isLeft;	
    private ArrowItemView itemUpgradeFw;
    private ArrowItemView itemDebugFw;
    private BleDevice device;
	private ProgressDialog progressDialog;
    private ProgressDialog dialog;
    private BleObserver observer;
    private OtaUpdater updater;
    private boolean downloading;
    private String currentVersion;
    private VersionChecker versionChecker;
    private String filePath;  
    private boolean initializing;
    private AlertDialog initFaileDialog;
    private MyTimer myTimer;
    private int otaTimeCount;
    private int initFaileCount;
    private File logFile;
    private DeviceManagerActivity activity;
	private boolean isCheckOtaResult;
	private int checkTimeCount;
    private AlertDialog disBleDialog;
    private TextView tvVV;
    private int progTimeCount;
    
    public DeviceInfoPager(Context context, boolean isLeft) {
		super(context);
        activity = (DeviceManagerActivity) context;        
        this.isLeft = isLeft;
        myTimer = new MyTimer();
        device = DeviceMgr.getBoundDevice(isLeft);
        if (device != null) {
            if (device.vv != null) {
                tvVV.setText("VV: " + device.vv);
            }
            if (State.getWorkMode(device.mac) == State.WorkMode.APP && device.firmware != null) {
                currentVersion = device.firmware;
                itemUpgradeFw.setVisibility(View.VISIBLE);
                itemUpgradeFw.setHint(device.firmware);
                //rootView.findViewById(R.id.btn_upgrade).setEnabled(true);
            } else if (State.getWorkMode(device.mac) == State.WorkMode.OTA) {
                itemUpgradeFw.setVisibility(View.VISIBLE);
                //rootView.findViewById(R.id.btn_upgrade).setEnabled(true);
            }            
        }
        logFile = new File(ASWear.AppSoreDir, "/ansobuy_ota_log.txt");
	}

	@Override
	protected View getRootView() {
		return View.inflate(context, R.layout.pager_device_info, null);
	}

	@Override
	protected void assignViews() {	
		itemUpgradeFw = (ArrowItemView) rootView.findViewById(R.id.item_upgrade_firmware);
        tvVV = (TextView) rootView.findViewById(R.id.tv_vv);
        itemDebugFw = (ArrowItemView)rootView.findViewById(R.id.debug_firmware);
	}

	@Override
	protected void initViews() {
        initDialogs();
		itemUpgradeFw.setOnClickListener(this);
        itemDebugFw.setOnClickListener(this);
        observer = new BleObserver(ObservableMgr.getBleObservable()) {
            @Override
            public void onFirmwareRead(BleDevice device, final String firmware) {
                if (device.isLeft == isLeft) {
                    currentVersion = firmware;
                    UiUtils.runOnUiThread(new Runnable() {
                      @Override
                      public void run() {
                          itemUpgradeFw.setHint(firmware);
                      }
                  });
                }
            }

            @Override
            public void onVVRead(BleDevice device, final String vv) {
                if (device.isLeft == isLeft) {
                    UiUtils.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tvVV.setText("VV: " + vv);
                        }
                    });
                }
            }
        };

/*
        
        rootView.findViewById(R.id.btn_upgrade).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (device == null) return;
                new AlertDialog.Builder(context).setMessage("升级固件后如果出现长时间无法连接设备，请将手机蓝牙关闭再打开，然后重试。确定升级固件吗？")
                        .setNegativeButton(R.string.cancel, null)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                filePath = DeviceInfoPager.getFirmwareFilePath(isLeft);
                                rootView.findViewById(R.id.btn_upgrade).setEnabled(false);
                                processUpgradeFirmware(filePath);
                            }
                        }).show();
            }
        });
*/
	}
    
    public void updateView() {
        device = DeviceMgr.getBoundDevice(isLeft);
        if (device != null) {
            itemUpgradeFw.setHint(device.firmware == null ? "" : device.firmware);
            if (updater == null || !device.mac.equals(updater.getBluetoothDevice().getAddress())) {
                updater = new OtaUpdater(activity, activity.bluetoothAdapter.getRemoteDevice(device.mac));                
            }
        }
    }
    
    private void initDialogs() {
        progressDialog = new ProgressDialog(context);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setMessage(context.getString(R.string.upgrading_firmware));
        progressDialog.setCancelable(false);
        dialog = new ProgressDialog(context);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);        
        dialog.setCancelable(false);
        initFaileDialog = new AlertDialog.Builder(context)
                .setCancelable(false)
                .setMessage(R.string.ota_initialize_failed_msg)
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        updater.cancelOta();
                        setUpgradeEnd();
                    }
                })
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        otaTimeCount = 0;
                        updater.cancelOta();
                        processUpgradeFirmware(filePath);
                    }
                }).create();
        
        disBleDialog = new AlertDialog.Builder(context)
                .setMessage("无法完成初始化，请等待设备连接成功后重试。")
                .setNegativeButton(R.string.ok, null).create();
    }

    private void processUpgradeFirmware(final String filePath) {
        if (device == null) return;
        if (updater == null) {
            updater = new OtaUpdater(activity, activity.bluetoothAdapter.getRemoteDevice(device.mac));
        }
		if (State.runState != State.RunState.STOP) {
			ToastMgr.showTextToast(context, 0, R.string.please_stop_running_first);
			return;
		}
        if (filePath == null || !new File(filePath).exists()) {
            ToastMgr.showTextToast(context, 0, R.string.upgrade_file_not_exist);
            return;
        }
        //断开当前连接
        DeviceMgr.cancelBindCheck(device.mac);
        State.setWorkMode(device.mac, State.WorkMode.OTA);
        BleManager.INSTANCE.removeConnection(device.mac);
        UiUtils.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (activity == null || activity.bluetoothAdapter == null) {
                    setUpgradeEnd();
                    return;
                }
                myTimer.startTimer(1000, 1000, DeviceInfoPager.this);
                itemUpgradeFw.setHint(device.firmware = "");
                activity.getDeviceTab(isLeft).setBattery(device.battery = 0);
                updater.startOta(filePath, false, DeviceInfoPager.this);
                dialog.setMessage(context.getString(R.string.initializing));
                if (!dialog.isShowing()) dialog.show();
                initializing = true;
            }
        }, 500);        
    }

    @Override
    public void runOnUiTimerTask() {
        if (++otaTimeCount >= 30) {
            otaTimeCount = 0;
            if (initializing) {
                addInitFaileCount();
            }            
        }

        if (downloading) {
            if (++progTimeCount >= 10) {
                downloading = false;
                progTimeCount = 0;
                progressDialog.dismiss();
                updater.cancelOta();
                setUpgradeEnd();
                ToastMgr.showTextToast(context, 0, R.string.firmware_upgrade_failed);
            }
        }        
        
		if (isCheckOtaResult) {
		    if (++checkTimeCount >= 10 && activity != null && activity.bluetoothAdapter != null) {
                BleManager.INSTANCE.removeConnection(device.mac);
		        activity.bluetoothAdapter.disable();
				ToastMgr.showTextToast(context, 0, R.string.firmware_upgrade_success);
				setUpgradeEnd();
		    }
		}
    }

    private void addInitFaileCount() {
        dialog.dismiss();
        initializing = false;
        if (++initFaileCount >= 2) {
            initFaileCount = 0;
            setUpgradeEnd();
            if (activity != null && activity.bluetoothAdapter != null) {
                activity.bluetoothAdapter.disable();
            }
            if (!disBleDialog.isShowing()) disBleDialog.show();
        } else {
            if (!initFaileDialog.isShowing()) initFaileDialog.show();
        }
    }

    public void destroy() {
        ObservableMgr.getBleObservable().deleteObserver(observer);
        if (versionChecker != null) versionChecker.destroy();
    }
    
    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.debug_firmware:
                Intent intent = new Intent();
                intent.setClass(activity, FwDebugActivity.class);
                intent.putExtra("device", device);
                activity.startActivity(intent);
                break;

            case R.id.item_upgrade_firmware:
                if (device == null) return;
                if (versionChecker == null) versionChecker = new VersionChecker((Activity) context);
                new AlertDialog.Builder(context).setMessage(R.string.ota_warning)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                filePath = DeviceInfoPager.getFirmwareFilePath(isLeft);
//                                rootView.findViewById(R.id.btn_upgrade).setEnabled(false);
                                processUpgradeFirmware(filePath);
                                
//                                VersionChecker.Product product = VersionChecker.Product.SISA;
//                                switch(device.type) {
//                                    case Constant.DEVICE_TYPE_BASIC:
//                                        product = VersionChecker.Product.SISA;
//                                        break;
//                                    case Constant.DEVICE_TYPE_POPULARITY:
//                                        product = VersionChecker.Product.SISB;
//                                        break;
//                                    case Constant.DEVICE_TYPE_ENHANCED:
//                                        product = VersionChecker.Product.SISC;
//                                        break;
//                                }
//                                versionChecker.checkNewVersion(product, currentVersion, new VersionChecker.FileCallback() {
//                                    @Override
//                                    public void onSuccess(File file) {
//                                        if (file == null) ToastMgr.showTextToast(context, 0, R.string.current_newest_version);
//                                        else {
//                                            //将固件解压出来
//                                            if (file.exists()) {
//                                                ZipUtils.unZip(file.getAbsolutePath());                                                
//                                                String name = device.isLeft ? "L.bin" : "R.bin";
//                                                File firmware = new File(file.getParent(), name);
//                                                if (firmware.exists()) {
////                                                    filePath = firmware.getAbsolutePath();
////                                                    processUpgradeFirmware(filePath);
//                                                }
//                                            }      
//                                        }
//                                    }
//
//                                    @Override
//                                    public void onError(String msg) {
//                                        ToastMgr.showTextToast(context, 0, msg);
//                                    }
//
//                                    @Override
//                                    public void onCancel() {
//                                        setUpgradeEnd();
//                                    }
//                                });
                            }
                        })
                        .setNegativeButton(R.string.cancel, null).show();                             
        		break;
        }
    }

    public static String getFirmwareFilePath(boolean left) {
        return ASWear.AppSoreDir + (left ? "/L.flash.xuv" : "/R.flash.xuv");
	}
	
    @Override
    public void onSuccess() {
        if (activity != null && activity.bluetoothAdapter != null) {
			progressDialog.dismiss();
			dialog.setMessage(context.getString(R.string.wait_ota_complete));
			dialog.show();
            isCheckOtaResult = true;
            UiUtils.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (device != null) BleManager.INSTANCE.connect(device.mac, handler);
                }
            }, 500);			
		}
    }

    @Override
    public void onProgress(int total, int sent) {        
        dialog.dismiss();
        if (initFaileDialog != null) initFaileDialog.dismiss(); 
        initializing = false;
        otaTimeCount = 0;
        progTimeCount = 0;
        if(!downloading) {
            progressDialog.setMax(100);
            progressDialog.show();
            downloading = true;
        }
        progressDialog.setProgress((int) (sent  * 100f/ total));
        if (sent >= total) {
            downloading = false;
            progressDialog.dismiss();
        }
    }

    @Override
    public void onConnectionStateUpdate(int state) {
        if (state == OtaUpdater.MESSAGE_UNKNOWN || state == OtaUpdater.MESSAGE_INTERRUPTED) {
            updater.cancelOta();
            processUpgradeFirmware(filePath);
        }
    }

    @Override
    public void onError(int code, String msg) {
        if (downloading) {
            ToastMgr.showTextToast(context, 0, R.string.firmware_upgrade_failed);
            setUpgradeEnd();
        }               
    }
    
	private void setUpgradeEnd() {
        isCheckOtaResult = false;
        checkTimeCount = 0;
        progTimeCount = 0;
        myTimer.stopTimer();
        otaTimeCount = 0;
        initializing = false;
        downloading = false;
        progressDialog.dismiss();
        initFaileDialog.dismiss();
        dialog.dismiss();
        State.setWorkMode(device.mac, State.WorkMode.APP);
        if (updater != null) {
            updater.destroy();
            updater = null;
        }
        UiUtils.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (device != null) BleManager.INSTANCE.connect(device.mac);
            }
        }, 500);
    }
    
    @Override
    public void onLog(String log) {
        LogUtil.d("DeviceInfoPager", "ansobuy--OTAU: " + log);        
        otaTimeCount = 0;
        progTimeCount = 0;
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(logFile, true));
            out.write(device.mac + "--" + DateUtils.formatDate(new Date(), "MM/dd HH:mm:ss.SSS") + "> " + log);
            out.newLine();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setVisibility(boolean visible) {
        device = DeviceMgr.getBoundDevice(isLeft);
        if (device == null) return;
        rootView.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
    }
    
    private final Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case Constant.MESSAGE_CONNECTED:	
                    
            		break;
                case Constant.MESSAGE_DISCONNECTED:
                    
                    break;
                case Constant.MESSAGE_SERVICES_DISCOVERED:
                    BleManager.INSTANCE.removeConnection(device.mac);
                    ToastMgr.showTextToast(context, 0, R.string.firmware_upgrade_success);
                    setUpgradeEnd();
                    break;
            }
        }
    };
}
