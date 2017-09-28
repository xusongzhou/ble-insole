package com.advanpro.fwtools.module.more;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.advanpro.fwtools.R;
import com.advanpro.fwtools.common.util.LogUtil;
import com.advanpro.fwtools.common.view.ArrowItemView;
import com.advanpro.fwtools.common.view.TitleBar;
import com.advanpro.fwtools.module.BaseActivity;
import com.advanpro.ota.OtaCallback;
import com.advanpro.ota.OtaUpdater;

/**
 * Created by zeng on 2016/5/24.
 */
public class OtaActivity extends BaseActivity{
    private ArrowItemView itemStartOta;
    private TitleBar titleBar;
    private BluetoothAdapter bluetoothAdapter;
    private static final String leftMac = "43:43:43:43:43:43";
    private static final String rightMac = "80:83:34:12:CF:F0";
    private boolean leftDowning;
    private boolean rightDowning;
    private OtaUpdater updater1;
    private OtaUpdater updater2;
    private TextView tvLeft;
    private TextView tvRight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ota);
        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        if (bluetoothManager != null) bluetoothAdapter = bluetoothManager.getAdapter();
        assignViews();
        initViews();
    }

    private void assignViews() {
        itemStartOta = (ArrowItemView) findViewById(R.id.item_start_ota);
        titleBar = (TitleBar) findViewById(R.id.title_bar);   
        tvLeft = (TextView) findViewById(R.id.tv_pro_left);
        tvRight = (TextView) findViewById(R.id.tv_pro_right);
    }

    private void initViews() {
        initTitleBar();
        itemStartOta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bluetoothAdapter == null) return;  
                if (!leftDowning) {
                    updater1 = new OtaUpdater(OtaActivity.this, bluetoothAdapter.getRemoteDevice(leftMac));
                    updater1.startOta(DeviceInfoPager.getFirmwareFilePath(true), false, new OtaCallback() {
                        @Override
                        public void onSuccess() {
                            leftDowning = false;
                            tvLeft.setText("左：升级完成");
                        }

                        @Override
                        public void onProgress(int total, int sent) {
                            leftDowning = true;
                            tvLeft.setText("左：" + sent + "/" + total);
                        }

                        @Override
                        public void onError(int code, String msg) {

                        }

                        @Override
                        public void onLog(String log) {
                            LogUtil.d("d", "ansobuy--left ota: " + log);
                        }

                        @Override
                        public void onConnectionStateUpdate(int state) {
                            
                        }
                    });
                }
                if (!rightDowning) {
                    updater2 = new OtaUpdater(OtaActivity.this, bluetoothAdapter.getRemoteDevice(rightMac));
                    updater2.startOta(DeviceInfoPager.getFirmwareFilePath(false), false, new OtaCallback() {
                        @Override
                        public void onSuccess() {
                            rightDowning = false;
                            tvRight.setText("左：升级完成");
                        }

                        @Override
                        public void onProgress(int total, int sent) {
                            rightDowning = true;
                            tvRight.setText("右：" + sent + "/" + total);
                        }

                        @Override
                        public void onError(int code, String msg) {

                        }

                        @Override
                        public void onConnectionStateUpdate(int state) {

                        }
                        
                        @Override
                        public void onLog(String log) {
                            LogUtil.d("d", "ansobuy--right ota: " + log);
                        }
                    });
                }                
            }
        });
    }
    
    private void initTitleBar() {
        titleBar.setTitle(R.string.firmware_upgrade);
        titleBar.setStartImageButtonVisible(true);
        titleBar.setOnMenuClickListener(new TitleBar.OnMenuClickListener() {
            @Override
            public void onMenuClick(View v) {
                switch(v.getId()) {
                    case R.id.btn_start:
                        finish();
                        break;
                }
            }
        });
    }
    
    @Override
    protected void onDestroy() {
        if (updater1 != null) updater1.destroy();
        if (updater2 != null) updater2.destroy();
        super.onDestroy();
    }
}
