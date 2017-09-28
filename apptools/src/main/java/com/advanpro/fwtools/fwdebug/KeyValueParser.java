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
 * Created by Administrator on 2016/6/19.
 */
public class KeyValueParser implements IFwDebugParser {
    private static int START_ADDRESS = 0x01B154;
    private static int LENGTH = 54;

    private BleDevice mDevice;
    private BleService mBleService;
    private FwParseBuffer mBuffer;
    private FwDataAsyncHandler.CompleteListener mListener;
    private List<String> mKeyValueList = new ArrayList<String>();

    public KeyValueParser(FwParseBuffer buffer, BleDevice device) {
        mBuffer = buffer;
        mDevice = device;
        mBleService = BleManager.INSTANCE.getBleService();
    }

    @Override
    public void start(int arg1, int arg2, FwDataAsyncHandler.CompleteListener listener) {
        mListener = listener;
        mBuffer.setLength(LENGTH);
        if (mBleService != null) {
            byte[] data = {(byte)0xAA, (byte)0xAA, 0x54, (byte)0xB1, 0x01, 0x36, 0x00, 0x00};
            mBleService.writeCharacteristicValue(mDevice.mac, Constant.REQUEST_READ_FW_DATA, UuidLib.PRIVATE_SERVICE,
                    UuidLib.REAL_TIME_WRITE, data, BleMessageHandler.INSTANCE.getHandler(mDevice.mac));
        }
    }

    @Override
    public Object getData() {
        return null;
    }
/*
    private String formatSecTime(int sec) {
        int year = sec/(365*24*60*60);
        int mon =

        return
    }
*/
    private String toWorkStateStr(byte state) {
        String sstr;
        switch (state) {
            case 0:
                sstr = "初始化";
                break;
            case 1:
                sstr = "正常";
                break;
            case 2:
                sstr = "准备";
                break;
            case 3:
                sstr = "休眠";
                break;
            case 4:
                sstr = "低电";
                break;
            case 5:
                sstr = "升级中";
                break;
            default:
                sstr = String.valueOf(state);
        }

        return sstr;
    }

    @Override
    public void storeBuffer(byte[] bytes, int len) {
        int result = mBuffer.storeData(bytes, len);
        if(result == 0) {
            return;
        }
        byte[] tbytes = mBuffer.getData();
        mKeyValueList.clear();
        mKeyValueList.add("状态：" + toWorkStateStr(tbytes[40]));
        mKeyValueList.add(String.valueOf(2000 + (tbytes[6]&0xff)) +  "年" + (tbytes[5]&0xff) + "月"
                + (tbytes[4]&0xff) + "日    第" + (tbytes[7]&0xff) + "天");
        mKeyValueList.add("重启次数：" + ((tbytes[8]&0xff) + (tbytes[9]<<8&0xff00)));
        mKeyValueList.add("fw错误码：" + ((tbytes[48]&0xff) + (tbytes[49]<<8&0xff00))  + "   app错误码:" + ((tbytes[46]&0xff) + (tbytes[47]<<8&0xff00)));
        mKeyValueList.add("UTC_SaveTime: " + ((tbytes[0]&0xff) + (tbytes[1]<<8&0xff00)
                + (tbytes[2]<<16&0xff0000) + (tbytes[3]<<24&0xff000000)));
        mKeyValueList.add("PSAS: " + (tbytes[14]&0xff) +  "    PSAS_INDEX:" + (tbytes[10]&0xff));
        mKeyValueList.add("上一段号:" + (tbytes[11]&0xff)
                + "    当前段总数:" +  ((tbytes[12]&0xff) + (tbytes[13]<<8&0xff00)));
        mKeyValueList.add("上段步行數:" + ((tbytes[16]&0xff) + (tbytes[17]<<8&0xff00))
                + "    上段跑步數:" + ((tbytes[18]&0xff) + (tbytes[19]<<8&0xff00)));
        mKeyValueList.add("步态边界:0x" + Integer.toHexString((tbytes[42]&0xff) + (tbytes[43]<<8&0xff00) + (tbytes[44]<<16&0xff0000)));

        if(mListener != null) {
            mListener.onLoadComplete(FwDebugActivity.LIST_TYPE_KEYVALUE, 0, mKeyValueList);
        }
    }

    @Override
    public void dataEnd() {

    }
}
