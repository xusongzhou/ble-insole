package com.advanpro.fwtools.fwdebug;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.advanpro.fwtools.ble.BleDevice;

import java.util.HashMap;

/**
 * Created by AdvanPro on 2016/6/17.
 */
public class FwDataAsyncHandler extends Handler {

    public static final int MESSAGE_TYPE_PARSE_SUM = FwDebugActivity.LIST_TYPE_SUMMARY;
    public static final int MESSAGE_TYPE_PARSE_DETAIL = FwDebugActivity.LIST_TYPE_DETAIL;
    public static final int MESSAGE_TYPE_PARSE_KEYVALUE = FwDebugActivity.LIST_TYPE_KEYVALUE;
    public static final int MESSAGE_TYPE_PARSE_DAYN = FwDebugActivity.LIST_TYPE_DETAIL_DAYN;
    public static final int MESSAGE_TYPE_PARSE_FOOTPRESS = FwDebugActivity.LIST_TYPE_FOOTPRESS;
    public static final int MESSAGE_TYPE_FWDEBUG_DATA = 0x41;
    public static final int MESSAGE_TYPE_FWDEBUG_START = 0x42;
    public static final int MESSAGE_TYPE_FWDEBUG_END = 0x43;

    private BleDevice mDevice;
    private HashMap<String, IFwDebugParser> mAdapterMap = new HashMap<String, IFwDebugParser>();
    private IFwDebugParser mCurrentParser;
    private FwParseBuffer mBufferManager = new FwParseBuffer(2048);

    private CompleteListener mCompleteListener;

    public static interface CompleteListener {
        void onLoadComplete(int type, int errorNo, Object data);
    }

    public FwDataAsyncHandler(Looper looper, BleDevice device) {
        super(looper);
        mDevice = device;
    }

    public void startParseMessage(int type, int arg1, int arg2, CompleteListener listener) {
        mCompleteListener = listener;
        Message message = obtainMessage(type);
        message.arg1 = arg1;
        message.arg2 = arg2;
        message.sendToTarget();
    }

    public void storeFwData(byte[] bytes) {

        Message message = obtainMessage(MESSAGE_TYPE_FWDEBUG_DATA);
        message.obj = bytes;
        message.arg1 = bytes.length;
        message.sendToTarget();
    }

    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
        case MESSAGE_TYPE_PARSE_SUM:
            mCurrentParser = mAdapterMap.get("sum");
            if(mCurrentParser == null) {
                mCurrentParser = new SumInfoParser(mBufferManager, mDevice);
                mAdapterMap.put("sum", mCurrentParser);
            }
            mCurrentParser.start(0, 0, mCompleteListener);
            break;

        case MESSAGE_TYPE_PARSE_DETAIL:
        case MESSAGE_TYPE_PARSE_DAYN:
            mCurrentParser = mAdapterMap.get("detail");
            if(mCurrentParser == null) {
                mCurrentParser = new DetailInfoParser(mBufferManager, mDevice);
                mAdapterMap.put("detail", mCurrentParser);
            }
            mCurrentParser.start(msg.arg1, 0,mCompleteListener);
            break;

        case MESSAGE_TYPE_PARSE_KEYVALUE:
            mCurrentParser = mAdapterMap.get("keyvalue");
            if(mCurrentParser == null) {
                mCurrentParser = new KeyValueParser(mBufferManager, mDevice);
                mAdapterMap.put("keyvalue", mCurrentParser);
            }
            mCurrentParser.start(0, 0,mCompleteListener);
            break;

        case MESSAGE_TYPE_PARSE_FOOTPRESS:
            mCurrentParser = mAdapterMap.get("footpress");
            if(mCurrentParser == null) {
                mCurrentParser = new FootPressParser(mBufferManager, mDevice);
                mAdapterMap.put("footpress", mCurrentParser);
            }
            mCurrentParser.start(msg.arg1, msg.arg2, mCompleteListener);
            break;
        case MESSAGE_TYPE_FWDEBUG_DATA:
            mCurrentParser.storeBuffer((byte[]) msg.obj, msg.arg1);
            break;

        case MESSAGE_TYPE_FWDEBUG_START:
            //do something check
            break;

        case MESSAGE_TYPE_FWDEBUG_END:
            mCurrentParser.dataEnd();
             break;
        }
    }
}
