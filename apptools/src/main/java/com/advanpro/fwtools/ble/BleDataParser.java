package com.advanpro.fwtools.ble;

import com.advanpro.fwtools.Constant;
import com.advanpro.fwtools.alg.AlgLib;
import com.advanpro.fwtools.common.MyTimer;
import com.advanpro.fwtools.common.util.DateUtils;
import com.advanpro.fwtools.common.util.LogUtil;
import com.advanpro.fwtools.db.Dao;
import com.advanpro.fwtools.db.Gait;
import com.advanpro.fwtools.db.PoseOriginal;
import com.advanpro.fwtools.db.StepSection;
import com.advanpro.fwtools.entity.ByteDate;
import com.advanpro.fwtools.entity.PoseSummary;
import com.advanpro.fwtools.entity.Step;
import com.advanpro.fwtools.fwdebug.FwDataAsyncHandler;
import com.advanpro.ascloud.ASCloud;

import java.nio.ByteBuffer;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by zengfs on 2016/2/23.
 * 蓝牙数据解析
 */
public class BleDataParser implements MyTimer.TimerTaskCallback{
    private static final int TYPE_GAIT_ORIGINAL = 1;
    private static final int TYPE_POSE_ORIGINAL = 2;
    private static final int TYPE_STEP_SECTION = 3;
    private static Map<BleDevice, BleDataParser> parserMap = new HashMap<>();
    private BleDevice device;
    private FwDataAsyncHandler mFwDebugHandler;
    private int fwdebugBytes;
    private boolean receivingFwdebug;
    private boolean receivingHis;
    private boolean receivingRealtime;
    private int currentType;
    private MyTimer myTimer;
    private int totalBytes;//数据总字节数
    private int readBytes;//已读取字节数
    private ByteBuffer byteBuffer;
    private Date dateOfData;//接收的数据属于的日期
    private int timeoutCount;
    //---有效数据存储的--
    private int realTotalBytes;
    private int realReadBytes;
    private ByteBuffer realByteBuffer;

    private BleDataParser() {
        myTimer = new MyTimer();        
    }

    /**
     * 获取解析器实例
     */
    public static BleDataParser getParser(BleDevice device) {
        BleDataParser parser = parserMap.get(device);
        if (parser == null) {
            parser = new BleDataParser();
            parser.device = device;
            parserMap.put(device, parser);
        }
        return parser;
    }

    public void enterFwdebugMode(FwDataAsyncHandler handler) {
        mFwDebugHandler = handler;
    }

    public void exitFwdebugMode() {
        mFwDebugHandler = null;
    }

    /**
     * 实时数据解析
     */
    public void parseRealtimeData(byte[] value) {
        if(receivingFwdebug &&(value.length == 20 || fwdebugBytes == value.length)) {
            mFwDebugHandler.storeFwData(value);
            fwdebugBytes -= value.length;
            if(fwdebugBytes <= 0) {
                receivingFwdebug = false;
            }
        } else if (receivingRealtime) {
            realReadBytes += value.length;
            if (value.length == 3) {
                int endFlag = (value[0] & 0xff) + (value[1] << 8 & 0xff00) + (value[2] << 16 & 0xff0000);
                if (realTotalBytes == (~endFlag & 0xFFFFFF)) {
                    receivingRealtime = false;
                    //解析
                    ByteBuffer buffer = ByteBuffer.wrap(realByteBuffer.array());
                    while (buffer.remaining() != 0) {
                        ByteDate byteDate = new ByteDate();
                        byteDate.year = buffer.get();
                        byteDate.month = buffer.get();
                        byteDate.date = buffer.get();
                        if (byteDate.year >= 0 && byteDate.year <= 99 && byteDate.month >= 1 &&
                                byteDate.month <= 12 && byteDate.date >= 1 && byteDate.date <= 31) {
                            HisDataReaderMgr.INSTANCE.addValidDate(device.mac, byteDate);
                            LogUtil.d("d", "ansobuy-- date: " + byteDate.toCalender().getTime().toLocaleString());
                        }
                    }
                } else {
                    if (realReadBytes <= realTotalBytes - 3) realByteBuffer.put(value);                    
                }
            } else {
                if (realReadBytes <= realTotalBytes - 3) realByteBuffer.put(value);
            }
            
            if (realReadBytes >= realTotalBytes) {
                receivingRealtime = false;
            }
        } else if (value[0] == 0x06 && value.length > 11) {
            if (value[1] == 0x03) {//步态	
                Gait gait = new Gait();
                gait.userId = ASCloud.userInfo.ID;
                gait.date = DateUtils.getStartOfDay(new Date()).getTime();
                gait.foot = device.isLeft ? 1 : 2;
                gait.forefoot = (value[3] & 0xff) + (value[4] << 8 & 0xff00);
                gait.sole = (value[5] & 0xff) + (value[6] << 8 & 0xff00);
                gait.heel = (value[7] & 0xff) + (value[8] << 8 & 0xff00);
                gait.varus = (value[9] & 0xff) + (value[10] << 8 & 0xff00);
                gait.ectropion = (value[11] & 0xff) + (value[12] << 8 & 0xff00);
				gait.sync = false;
                Dao.INSTANCE.insertOrUpdateGait(gait);
                ObservableMgr.getBleObservable().notifyAllGaitRealtimeDataChanged(device, value[2] >> 4 & 0x0F, value[2] & 0x07);
            } else if (value[1] == 0x05) {//坐站姿
                ObservableMgr.getBleObservable().notifyAllPoseRealtimeDataChanged(device, value[2] & 0x03);
            }
        } else if (value[0] == 0x26) {
            Step.Apart info = new Step.Apart();
            info.walkSteps = (value[8] & 0xff) + (value[9] << 8 & 0xff00) + (value[10] << 16 & 0xff0000);
            info.walkDuration = (value[11] & 0xff) + (value[12] << 8 & 0xff00) + (value[13] << 16 & 0xff0000);
            info.runSteps = (value[14] & 0xff) + (value[15] << 8 & 0xff00) + (value[16] << 16 & 0xff0000);
            info.runDuration = (value[17] & 0xff) + (value[18] << 8 & 0xff00) + (value[19] << 16 & 0xff0000);
            ObservableMgr.getBleObservable().notifyAllStepRealtimeDataChanged(device, info);
        } else if ((value[0] & 0xff) == 0xAA && (value[1] & 0xff) == 0xBB && value.length == 3) {//硬件存储的日期
            if (realTotalBytes - 3 < 0) return;
            receivingRealtime = true;
            realTotalBytes = value[2] & 0xff;
            realByteBuffer = ByteBuffer.allocate(realTotalBytes - 3);
            realReadBytes = 0;
        } else if((value[0] & 0xff) == 0xAA && (value[1] & 0xff) == 0xAA && value.length == 5) {
            receivingFwdebug = true;
            fwdebugBytes = (value[4]<<16 & 0xff0000) + (value[3]<<8 & 0xff00) + (value[2]&0xff);
        }
    }

    //是否接收完成，如果完成还原状态并停止计时器
    private boolean isReceiveCompleted(byte[] value) {
        readBytes += value.length;
        timeoutCount = 0;
        if (value.length == 3) {
            int endFlag = (value[0] & 0xff) + (value[1] << 8  & 0xff00) + (value[2] << 16 & 0xff0000);
            if (totalBytes == (~endFlag & 0xFFFFFF)) {
                receivingHis = false;
                myTimer.stopTimer();
                return true;
            }
        }
        //如果不是结束标志，把此包数据存起来
        if (readBytes <= totalBytes - 3) byteBuffer.put(value);
        //接收数据长度大于了总长度还没有收到结束标志，说明接收过程出错或硬件发送数据格式错误，停止
        if (readBytes >= totalBytes) {
            setTransferFailed();
        }
        return false;
    }

    //检验数据完整性
    private boolean checkDataIntegrality() {
        boolean result = readBytes == totalBytes;
        ObservableMgr.getBleObservable().notifyAllHisDataReadResult(device, result);
        readBytes = 0;
        return result;
    }

    /**
     * 历史数据解析
     */
    public void parseHistoryData(byte[] value) {
        if (receivingHis) {
            switch(currentType) {
                case TYPE_GAIT_ORIGINAL:
                    if (isReceiveCompleted(value) && checkDataIntegrality()) {

                    }
                    break;
                case TYPE_POSE_ORIGINAL:
                    if (isReceiveCompleted(value) && checkDataIntegrality()) {
                        //查询数据库，看另一只是否已保存数据，有则取出计算时长
                        //如果当前只绑定一只设备，直接计算时长，否则等待获取另一只设备数据后再计算
                        byte[] bytes = byteBuffer.array();
                        PoseSummary summary = null;
                        if (DeviceMgr.getConnectedCount() == 1) {
                            summary = AlgLib.parsePoseOriginal(bytes, null);
                        } else {
                            PoseOriginal original = Dao.INSTANCE.queryPoseOriginal(dateOfData, !device.isLeft);
                            if (original != null) {
                                summary = AlgLib.parsePoseOriginal(bytes, original.value);
                            }
                        }
                        if (summary != null) {
                            Dao.INSTANCE.insertOrUpdatePose(dateOfData, (int) summary.standDuration, (int) summary. sitDuration);
                            ObservableMgr.getActivityObservable().setChanged();
                        }
                        Dao.INSTANCE.insertOrUpdatePoseOriginal(dateOfData, device.isLeft, bytes);
                        Dao.INSTANCE.insertOrUpdateReadRecord(device.isLeft, Constant.REQUEST_READ_HISTORY_POSE_ORIGINAL, dateOfData, 0);
                    }
                    break;
                case TYPE_STEP_SECTION:
                    if (isReceiveCompleted(value) && checkDataIntegrality()) {
                        ByteBuffer buffer = ByteBuffer.wrap(byteBuffer.array());
                        while (buffer.remaining() != 0) {
                            StepSection section = new StepSection();
                            section.userId = ASCloud.userInfo.ID;
                            section.date = dateOfData;
                            section.isLeft = device.isLeft;
                            section.section = buffer.get();
                            section.walkSteps = (buffer.get() & 0xff) + (buffer.get() << 8 & 0xff00);
                            section.runningSteps = (buffer.get() & 0xff) + (buffer.get() << 8 & 0xff00);
                            Dao.INSTANCE.insertOrUpdateStepSection(section);//保存分段数据到数据库                            
                        }
                        //保存读取记录						
                        Dao.INSTANCE.insertOrUpdateReadRecord(device.isLeft, Constant.REQUEST_READ_HISTORY_STEP_SECTION,
                                dateOfData, (int) value[value.length - 5]);
                    }
                    break;
            }
        } else if (value[0] == 0x06 && value[1] == 0x01 && value.length == 14) {//步点统计
            Calendar c = DateUtils.getStartOfDay(new Date());
            c.set(Calendar.MONTH, value[2] - 1);
            c.set(Calendar.DATE, value[3]);
            Gait gait = new Gait();
            gait.date = c.getTime();
            gait.userId = ASCloud.userInfo.ID;
            gait.foot = device.isLeft ? 1 : 2;
            gait.forefoot = (value[4] & 0xff) + (value[5] << 8 & 0xff00);
            gait.sole = (value[6] & 0xff) + (value[7] << 8 & 0xff00);
            gait.heel = (value[8] & 0xff) + (value[9] << 8 & 0xff00);
            gait.ectropion = (value[10] & 0xff) + (value[11] << 8 & 0xff00);
            gait.varus = (value[12] & 0xff) + (value[13] << 8 & 0xff00);
            gait.sync = false;
            Dao.INSTANCE.insertOrUpdateGait(gait);
            if (!DateUtils.isSameDay(c.getTime(), new Date())) {
                Dao.INSTANCE.insertOrUpdateReadRecord(device.isLeft, Constant.REQUEST_READ_HISTORY_GAIT_STAT, c.getTime(), 0);
            }
            ObservableMgr.getBleObservable().notifyAllHisDataReadResult(device, true);
        } else if (value[0] == 0x06 && value[1] == 0x02 && value.length == 16) {//计步统计
            Calendar c = DateUtils.getStartOfDay(new Date());
            c.set(Calendar.MONTH, value[2] - 1);
            c.set(Calendar.DATE, value[3]);
            Step.Apart info = new Step.Apart();
            info.walkSteps = (value[4] & 0xff) + (value[5] << 8 & 0xff00) + (value[6] << 16 & 0xff0000);
            info.walkDuration = (value[7] & 0xff) + (value[8] << 8 & 0xff00) + (value[9] << 16 & 0xff0000);
            info.runSteps = (value[10] & 0xff) + (value[11] << 8 & 0xff00) + (value[12] << 16 & 0xff0000);
            info.runDuration = (value[13] & 0xff) + (value[14] << 8 & 0xff00) + (value[15] << 16 & 0xff0000);
            
            
            //如果不是今天，直接存数据库
            if (!DateUtils.isSameDay(c.getTime(), new Date())) {
				//单只作乘2处理，与现有数据比较，大则替换，小则不存
				Dao.INSTANCE.insertOrUpdateWalk(c.getTime(), (info.walkSteps + info.runSteps) * 2, info.walkDuration + info.runDuration);
                Dao.INSTANCE.insertOrUpdateReadRecord(device.isLeft, Constant.REQUEST_READ_HISTORY_STEP_STAT, c.getTime(), 0);
            } else {
				ObservableMgr.getBleObservable().notifyAllStepRealtimeDataChanged(device, info);
            }
            ObservableMgr.getBleObservable().notifyAllHisDataReadResult(device, true);
        } else if (value[0] == 0x06 && value[1] == 0x03 && value.length == 7) {//步态原始
            Calendar c = DateUtils.getStartOfDay(new Date());
            c.set(Calendar.MONTH, value[2] - 1);
            c.set(Calendar.DATE, value[3]);
            processFirstPackageData(TYPE_GAIT_ORIGINAL, c, value);
        } else if (value[0] == 0x06 && value[1] == 0x04 && value.length == 7) {//运动分段
            Calendar c = DateUtils.getStartOfDay(new Date());
            c.set(Calendar.MONTH, value[2] - 1);
            c.set(Calendar.DATE, value[3]);
            processFirstPackageData(TYPE_STEP_SECTION, c, value);
        } else if (value[0] == 0x06 && value[1] == 0x05 && value.length == 7) {//姿势原始
            Calendar c = DateUtils.getStartOfDay(new Date());
            c.set(Calendar.MONTH, value[2] - 1);
            c.set(Calendar.DATE, value[3]);
            processFirstPackageData(TYPE_POSE_ORIGINAL, c, value);
        }
    }

    //分段数据的首包处理
    private void processFirstPackageData(int type, Calendar c, byte[] value) {
        totalBytes = (value[4] & 0xff) + (value[5] << 8  & 0xff00) + (value[6] << 16 & 0xff0000);
        if (totalBytes - 3 < 0) return;
        receivingHis = true;
        currentType = type;        
        dateOfData = c.getTime();
        dateOfData = c.getTime();
        byteBuffer = ByteBuffer.allocate(totalBytes - 3);
        myTimer.startTimer(0, 1000, this);
    }

    @Override
    public void runTimerTask() {
        //6秒没有停止计时器，数据传输失败，超时重连
        if (++timeoutCount == 6) {
            timeoutCount = 0;
            BleManager.INSTANCE.reconnect(device.mac);
            setTransferFailed();
        }		
    }

    private void setTransferFailed() {
        myTimer.stopTimer();
        timeoutCount = 0;
        receivingHis = false;
        currentType = 0;
        readBytes = 0;
        ObservableMgr.getBleObservable().notifyAllHisDataReadResult(device, false);
    }
}
