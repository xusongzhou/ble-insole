package com.advanpro.fwtools.fwdebug;

/**
 * Created by AdvanPro on 2016/6/17.
 */
public class FwParseBuffer {
    private byte[] mBuffer;
    private int mMaxLength;
    private int mLength;
    private int mCurrent;

    FwParseBuffer(int maxlen) {
        mMaxLength = maxlen;
        mBuffer = new byte[maxlen];
    }

    public void setLength(int len) {
        mLength = len;
        mCurrent = 0;
    }

    public int storeData(byte[] bytes, int len) {
        System.arraycopy(bytes, 0, mBuffer, mCurrent, len);
        mCurrent += len;
        if(mCurrent >= (mLength - 1)) {
            return 1;
        }
        return 0;
    }

    public byte[] getData() {
        return mBuffer;
    }
}
