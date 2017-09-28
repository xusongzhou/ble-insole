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
public class DetailInfoParser implements IFwDebugParser {
    private static final int START_ADDRESS = 0x01C000;

    private static final int REQUEST_DATE_PART_ID = 0;
    private static final int REQUEST_PSAS_PART_ID = 1;
    private static final int REQUEST_SPORT_PART_ID = 2;
    private static final int REQUEST_FOOTADDR_PART_ID = 3;
    private static final int REQUEST_FOOTDETAIL_PART_ID = 4;

    private BleService mBleService;
    private BleDevice mDevice;
    private FwParseBuffer mBuffer;
    private FwDataAsyncHandler.CompleteListener mListener;
    private List<DetailInfo> mDetailList = new ArrayList<DetailInfo>();

    private DetailInfo mCurDetail;
    private int mQueryDayIndex;
    private int mQueryDayPart;
    private int mRequestPart;
    private int mRequestSector;

    public static class SectorInfo {
        int sectorNo;
        int walkingSteps;
        int runningSteps;
        int footNo;
        int footDataAddr;
        int footDataLen;
    }

    public static class DetailInfo {
        public int dayn;
        public String date;
        public List<SectorInfo> sectors;
    }

    public DetailInfoParser(FwParseBuffer buffer, BleDevice device) {
        mBuffer = buffer;
        mDevice = device;
        mBleService = BleManager.INSTANCE.getBleService();
    }

    @Override
    public void start(int arg1, int arg2, FwDataAsyncHandler.CompleteListener listener) {
        mListener = listener;
        mQueryDayIndex = arg1&0x0f;
        mQueryDayPart = (arg1>>4)&0x0f;

        if(mQueryDayPart == 1) {
            mRequestPart = REQUEST_SPORT_PART_ID;
            mBuffer.setLength(480);
            mCurDetail = mDetailList.get(mQueryDayIndex);
            requestFwdataByAddr(mQueryDayIndex*2048 + 3 + 360 + START_ADDRESS, 480);
        } else if(mQueryDayPart == 2) {
            mRequestSector = arg1&0xff00;
            mRequestPart = REQUEST_FOOTDETAIL_PART_ID;
            mCurDetail = mDetailList.get(mQueryDayIndex);
            SectorInfo sinfo= mCurDetail.sectors.get(mRequestSector);
            mBuffer.setLength(sinfo.footDataLen);
            requestFwdataByAddr(sinfo.footDataAddr, sinfo.footDataLen);
        } else if(mQueryDayPart == 3) {
            mRequestPart = REQUEST_PSAS_PART_ID;
            mCurDetail = mDetailList.get(mQueryDayIndex);
            mBuffer.setLength(360);
            requestFwdataByAddr(mQueryDayIndex*2048 + 3 + START_ADDRESS, 360);
        } else {
            mRequestPart = REQUEST_DATE_PART_ID;
            mBuffer.setLength(3);
            mCurDetail = new DetailInfo();
            mCurDetail.dayn = mQueryDayIndex;
            mDetailList.clear();
            mDetailList.add(mCurDetail);
            requestFwdataByAddr(START_ADDRESS, 3);
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
        if(mQueryDayPart == 0 || mRequestPart == REQUEST_DATE_PART_ID) {
            byte[]  buffer = mBuffer.getData();
            mCurDetail.date = (2000 + (buffer[2]&0xff)) + "年" + (buffer[1]&0xff) + "月" + (buffer[0]&0xff) + "日";
            if(mQueryDayIndex >= 9) {
                mListener.onLoadComplete(FwDebugActivity.LIST_TYPE_DETAIL, 0, mDetailList);
            } else {
                mQueryDayIndex++;
                mCurDetail = new DetailInfo();
                mCurDetail.dayn = mQueryDayIndex;
                mDetailList.add(mCurDetail);
                mBuffer.setLength(3);
                requestFwdataByAddr(mQueryDayIndex * 2048 + START_ADDRESS, 3);
            }
        } else if(mQueryDayPart == 1) {
            if(mRequestPart == REQUEST_SPORT_PART_ID) {
                processSportPart(mBuffer.getData(), 480);
                mRequestPart = REQUEST_FOOTADDR_PART_ID;
                mBuffer.setLength(576);
                requestFwdataByAddr(mQueryDayIndex * 2048 + 3 + 360 + 480 + START_ADDRESS, 576);
            } else if(mRequestPart == REQUEST_FOOTADDR_PART_ID) {
                processFootAddrPart(mBuffer.getData(), 576);
                mListener.onLoadComplete(FwDebugActivity.LIST_TYPE_DETAIL_DAYN, 0, mCurDetail.sectors);
            }
        } else if(mQueryDayPart == 2) {
            if(mRequestPart == REQUEST_FOOTDETAIL_PART_ID) {
                String fd = processFootDetailPart(mBuffer.getData(),
                                mCurDetail.sectors.get(mRequestSector).footDataLen);
                mListener.onLoadComplete(FwDebugActivity.LIST_TYPE_DETAIL_DAYN, 0, fd);
            }
        } else if(mQueryDayPart == 3) {
            if(mRequestPart == REQUEST_PSAS_PART_ID) {
                String psas = processPSASPart(mBuffer.getData(), 360);
                mListener.onLoadComplete(FwDebugActivity.LIST_TYPE_DETAIL_DAYN, 0, psas);
            }
        }
    }

    @Override
    public void dataEnd() {
        //do nothing
    }


    private void processSportPart(byte[] buffer, int len) {
        mCurDetail = mDetailList.get(mQueryDayIndex);
        mCurDetail.sectors = new ArrayList<>();
        for(int i = 0; i < 96; i++) {
            SectorInfo info = new SectorInfo();
            mCurDetail.sectors.add(info);
            info.sectorNo = (buffer[i*5]&0xff);
            info.walkingSteps = (buffer[i*5 + 1]&0xff) + (buffer[i*5 + 2]<<8&0xff00);
            info.runningSteps = (buffer[i*5 + 3]&0xff) + (buffer[i*5 + 4]<<8&0xff00);
        }
    }

    private void processFootAddrPart(byte[] buffer, int len) {
        mCurDetail = mDetailList.get(mQueryDayIndex);
        for(int i = 0; i < 96; i++) {
            SectorInfo info = mCurDetail.sectors.get(i);
            info.footNo = (buffer[i*6]&0xff);
            info.footDataLen = (buffer[i*6 + 1]&0xff) + (buffer[i*6 + 2]<<8&0xff00);
            info.footDataAddr = (buffer[i*6 + 3]&0xff) + (buffer[i*6 + 4]<<8&0xff00) + (buffer[i*6 + 5]<<16&0xff0000);
        }
    }

    private String processFootDetailPart(byte[] buffer, int len) {

        return null;
    }

    private String processPSASPart(byte[] buffer, int len) {
        return null;
    }

    private void requestFwdataByAddr(int addr, int len) {
        if (mBleService != null) {
            byte[] data = {(byte)0xAA, (byte)0xAA,
                    (byte)(addr&0xFF), (byte)((addr>>8)&0xFF), (byte)((addr>>16)&0xFF),
                    (byte)(len&0xFF), (byte)((len>>8)&0xFF), (byte)((len>>16)&0xFF)};
            mBleService.writeCharacteristicValue(mDevice.mac, Constant.REQUEST_READ_FW_DATA, UuidLib.PRIVATE_SERVICE,
                    UuidLib.REAL_TIME_WRITE, data, BleMessageHandler.INSTANCE.getHandler(mDevice.mac));
        }
    }
}
