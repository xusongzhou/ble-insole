package com.advanpro.fwtools.fwdebug;

/**
 * Created by AdvanPro on 2016/6/17.
 */
public interface IFwDebugParser {
    public void start(int arg1, int arg2, FwDataAsyncHandler.CompleteListener mListener);
    public Object getData();
    public void storeBuffer(byte[] bytes, int len);
    public void dataEnd();
}
