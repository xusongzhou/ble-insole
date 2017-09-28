package com.advanpro.fwtools.fwdebug;

import com.advanpro.fwtools.Constant;
import com.advanpro.fwtools.ble.BleDevice;
import com.advanpro.fwtools.ble.BleManager;
import com.advanpro.fwtools.ble.BleMessageHandler;
import com.advanpro.fwtools.ble.BleService;
import com.advanpro.fwtools.ble.UuidLib;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by AdvanPro on 2017/4/21.
 */

public class FootPressParser implements IFwDebugParser {
    private BleDevice mDevice;
    private BleService mBleService;
    private FwParseBuffer mBuffer;
    private FwDataAsyncHandler.CompleteListener mListener;
    private List<String> mKeyValueList = new ArrayList<String>();

    private int mAddr;
    private int mSteps;
    private int mLen;

    public FootPressParser(FwParseBuffer buffer, BleDevice device) {
        mBuffer = buffer;
        mDevice = device;
        mBleService = BleManager.INSTANCE.getBleService();
    }


    @Override
    public void start(int arg1, int arg2, FwDataAsyncHandler.CompleteListener listener) {
        mAddr = arg1;
        mSteps = arg2;
        mLen = mSteps*2 + 3;
        mListener = listener;
        mBuffer.setLength(mLen);
        if (mBleService != null) {
            byte[] data = {(byte)0xAA, (byte)0xAA, (byte)(mAddr&0xff), (byte)((mAddr>>8)&0xff),
                    (byte)((mAddr>>16)&0xff), (byte)(mLen&0xff), (byte)((mLen>>8)&0xff), (byte)((mLen>>16)&0xff)};
            mBleService.writeCharacteristicValue(mDevice.mac, Constant.REQUEST_READ_FW_DATA, UuidLib.PRIVATE_SERVICE,
                    UuidLib.REAL_TIME_WRITE, data, BleMessageHandler.INSTANCE.getHandler(mDevice.mac));
        }
    }

    @Override
    public Object getData() {
        return null;
    }


    private String getSwitchStatus(byte press) {
        StringBuilder builder = new StringBuilder(" ");
        if((press&0x80) != 0) {
            builder.append("A");
        }
        if((press&0x40) != 0) {
            builder.append("B");
        }
        if((press&0x20) != 0) {
            builder.append("C");
        }
        if((press&0x10) != 0) {
            builder.append("D");
        }

        return builder.append(" ").toString();
    }

    private String getPressStatus(byte press) {
        StringBuilder builder = new StringBuilder(" ");

        switch(press&0xC0) {
            case 0:
                builder.append("全掌");
                break;
            case 0x40:
                builder.append("前掌");
                break;
            case 0x80:
                builder.append("脚跟");
                break;
            default:
                builder.append("无效");
        }
        switch(press&0x30) {
            case 0:
                builder.append("正常");
                break;
            case 0x10:
                builder.append("内翻");
                break;
            case 0x20:
                builder.append("外翻");
                break;
            default:
                builder.append("无效");
        }

        return builder.append(" ").toString();
    }

    @Override
    public void storeBuffer(byte[] bytes, int len) {
        int result = mBuffer.storeData(bytes, len);
        if(result == 0) {
            return;
        }
        byte[] tbytes = mBuffer.getData();
        mKeyValueList.clear();
        mKeyValueList.add("段号:" + tbytes[0] + " 步数:" + ((tbytes[2]<<8&0xff00) + (tbytes[1]&0xff)));
        for(int i = 0; i < mSteps; i++) {
            mKeyValueList.add((i+1)+ " 时间偏移:" + (tbytes[3+ i*2 + 1]&0x0f)
                    + ((tbytes[3+ i*2]&0x08)>0?" 跑步":" 走路")
                    + " 冲击力:" + (tbytes[3+ i*2]&0x07)
                    + getSwitchStatus(tbytes[3+ i*2])
                    + getPressStatus(tbytes[3+ i*2 + 1]));
        }
        if (mListener != null) {
            mListener.onLoadComplete(FwDebugActivity.LIST_TYPE_FOOTPRESS, 0, mKeyValueList);
        }
    }

    @Override
    public void dataEnd() {

    }
}
