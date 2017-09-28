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
 * Created by AdvanPro on 2016/6/17.
 */
public class SumInfoParser implements IFwDebugParser {
    private static int START_ADDRESS = 0x01B000;
    private static int LENGTH = 320;

    private BleDevice mDevice;
    private BleService mBleService;
    private FwParseBuffer mBuffer;
    private FwDataAsyncHandler.CompleteListener mListener;
    private List<SumInfo> mSumList = new ArrayList<SumInfo>();

    public static class SumInfo {
        public String date;
        public int pressFront;
        public int pressFull;
        public int pressEnd;
        public int pressInward;
        public int pressOutward;
        public int walkingSteps;
        public int walkingTime;
        public int runningSteps;
        public int runningTime;
        public int standTime;
        public int sitTime;
    }

    public SumInfoParser(FwParseBuffer buffer, BleDevice device) {
        mBuffer = buffer;
        mDevice = device;
        mBleService = BleManager.INSTANCE.getBleService();
    }

    @Override
    public void start(int arg1, int arg2, FwDataAsyncHandler.CompleteListener listener) {
        mListener = listener;
        mBuffer.setLength(LENGTH);
        if (mBleService != null) {
            byte[] data = {(byte)0xAA, (byte)0xAA, 0x00, (byte)0xB0, 0x01, 0x40, 0x01, 0x00};
            mBleService.writeCharacteristicValue(mDevice.mac, Constant.REQUEST_READ_FW_DATA, UuidLib.PRIVATE_SERVICE,
                    UuidLib.REAL_TIME_WRITE, data, BleMessageHandler.INSTANCE.getHandler(mDevice.mac));
        }
    }

    @Override
    public Object getData() {
        return null;
    }

    @Override
    public void storeBuffer(byte[] bytes, int len) {
        int result = mBuffer.storeData(bytes, len);
        if(result == 0) {
            return;
        }

        SumInfo info;
        int offset;
        mSumList.clear();
        byte[] tbtyes = mBuffer.getData();
        for(int dayn = 0; dayn < 10; dayn++) {
            info = new SumInfo();
            mSumList.add(info);
            offset = dayn*32;
            info.date = (2000 + (tbtyes[offset + 2]&0xff)) + "年"
                    + (tbtyes[offset + 1]&0xff) + "月"
                    + (tbtyes[offset]&0xff) + "日";

            info.pressFront = (tbtyes[offset + 4]&0xff) + (tbtyes[offset + 5]<<8&0xff00);
            info.pressFull = (tbtyes[offset + 6]&0xff) + (tbtyes[offset + 7]<<8&0xff00);
            info.pressEnd = (tbtyes[offset + 8]&0xff) + (tbtyes[offset + 9]<<8&0xff00);
            info.pressInward = (tbtyes[offset + 10]&0xff) + (tbtyes[offset + 11]<<8&0xff00);
            info.pressOutward = (tbtyes[offset + 12]&0xff) + (tbtyes[offset + 13]<<8&0xff00);

            info.walkingSteps = (tbtyes[offset + 14]&0xff)+(tbtyes[offset + 15]<<8&0xff00)+(tbtyes[offset + 16]<<16&0xff0000);
            info.walkingTime = (tbtyes[offset + 17]&0xff)+(tbtyes[offset + 18]<<8&0xff00)+(tbtyes[offset + 19]<<16&0xff0000);
            info.runningSteps = (tbtyes[offset + 20]&0xff)+(tbtyes[offset + 21]<<8&0xff00)+(tbtyes[offset + 22]<<16&0xff0000);
            info.runningTime = (tbtyes[offset + 23]&0xff)+(tbtyes[offset + 24]<<8&0xff00)+(tbtyes[offset + 25]<<16&0xff0000);

            info.standTime =(tbtyes[offset + 26]&0xff)+(tbtyes[offset + 27]<<8&0xff00)+(tbtyes[offset + 28]<<16&0xff0000);
            info.sitTime = (tbtyes[offset + 29]&0xff)+(tbtyes[offset + 30]<<8&0xff00)+(tbtyes[offset + 31]<<16&0xff0000);
        }
        if(mListener != null) {
            mListener.onLoadComplete(FwDebugActivity.LIST_TYPE_SUMMARY, 0, mSumList);
        }
    }

    @Override
    public void dataEnd() {

    }
}
