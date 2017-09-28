package com.advanpro.fwtools.ble;

import com.advanpro.fwtools.Constant;
import com.advanpro.fwtools.alg.AlgLib;
import com.advanpro.fwtools.common.util.DateUtils;
import com.advanpro.fwtools.db.Dao;
import com.advanpro.fwtools.db.ReadRecord;
import com.advanpro.fwtools.entity.ByteDate;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by zengfs on 2016/3/28.
 * 历史数据读取器管理
 */
public enum HisDataReaderMgr {
    INSTANCE;
    private Map<String, HisDataReader> readerMap = new HashMap<>();
    private Map<String, List<ByteDate>> validDate = new HashMap<>();

    /**
     * 创建一个历史数据读取器
     * @param deviceMac 设备地址
     */
    public void createReader(String deviceMac) {
        readerMap.put(deviceMac, new HisDataReader(DeviceMgr.getBoundDevice(deviceMac)));
    }

    /**
     * 添加硬件有效存储日期
     */
    public void addValidDate(String deviceMac, ByteDate... dates) {
        List<ByteDate> list = validDate.get(deviceMac);
        if (list == null) {
            list = new LinkedList<>();
            validDate.put(deviceMac, list);
        }
        for (ByteDate byteDate : dates) {
            if (!list.contains(byteDate)) list.add(byteDate); 
        }
    }

    /**
     * 移除一个历史数据读取器
     * @param deviceMac 设备地址
     */
    public void removeReader(String deviceMac) {
        HisDataReader reader = readerMap.get(deviceMac);
        if (reader != null) {
            reader.destroy();
            readerMap.remove(deviceMac);
        }
    }

    /**
     * 读取历史数据
     */
    public void readHisData() {
        for (HisDataReader reader : readerMap.values()) {
            reader.read();
        }
    }

    private class HisDataReader {
        private boolean isReading;
        private BleDevice device;
        private BleObserver observer;
        private int[] requests = {Constant.REQUEST_READ_HISTORY_STEP_STAT, Constant.REQUEST_READ_HISTORY_GAIT_STAT, 
                Constant.REQUEST_READ_HISTORY_POSE_ORIGINAL};
        private int currentRequest;
        private Calendar currentDate;
        private int lastRequest;
        private int requestFaileCount;

        public HisDataReader(BleDevice device) {
            this.device = device;
            observer = new BleObserver(ObservableMgr.getBleObservable()) {
                @Override
                public void onHistoryDataReadResult(BleDevice device, boolean success) {
                    if (currentDate == null || !device.equals(HisDataReader.this.device)) return;
                    //如果上次请求成功，处理下一数据类型的请求，否则继续原请求
                    //如果同一个请求3次失败，处理下一请求
                    if (success || requestFaileCount >= 3) processNextRequest(true);
                    else processNextRequest(false);
                }
            };
        }

        public void read() {
            if (currentDate == null) processNextDate();
            processRequest();
        }

        /**
         * @param next true：发新请求，false：重新发当前请求
         */
        private void processNextRequest(boolean next) {
            if (next) {
                currentRequest++;
                requestFaileCount = 0;
            }
            isReading = false;
            processRequest();
            if (lastRequest == currentRequest) {
                requestFaileCount++;
            }
            lastRequest = currentRequest;
        }
        
        //处理请求
        public void processRequest() {
            if (isReading) return;
            if (currentRequest >= requests.length) {
                currentRequest = 0;
                isReading = false;
                processNextDate();
                return;
            }
            switch(requests[currentRequest]) {
                case Constant.REQUEST_READ_HISTORY_STEP_STAT: {
                    ReadRecord record = Dao.INSTANCE.queryReadRecord(device.isLeft, Constant.REQUEST_READ_HISTORY_STEP_STAT, 
							currentDate.getTime());
                    //没有获取过硬件数据，并且时间不是今天，获取历史数据，如果时间到了今天，不再读取
                    if (record == null && !DateUtils.isSameDay(currentDate, Calendar.getInstance())) {
                        isReading = true;
                        BleManager.INSTANCE.readHistoryStepStat(device.mac, currentDate);
                    } else {
                        processNextRequest(true);
                    }
                    break;
                }
                case Constant.REQUEST_READ_HISTORY_GAIT_STAT: {
                    ReadRecord record = Dao.INSTANCE.queryReadRecord(device.isLeft, Constant.REQUEST_READ_HISTORY_GAIT_STAT,
							currentDate.getTime());
                    if (record == null && !DateUtils.isSameDay(currentDate, Calendar.getInstance())) {
                        isReading = true;
                        BleManager.INSTANCE.readHistoryGaitStat(device.mac, currentDate);
                    } else {
                        processNextRequest(true);
                    }
                    break;
                }
                case Constant.REQUEST_READ_HISTORY_STEP_SECTION: {
                    readHisSecData(Constant.REQUEST_READ_HISTORY_STEP_SECTION);
                    break;
                }
                case Constant.REQUEST_READ_HISTORY_POSE_ORIGINAL: {
					ReadRecord record = Dao.INSTANCE.queryReadRecord(device.isLeft, Constant.REQUEST_READ_HISTORY_POSE_ORIGINAL,
							currentDate.getTime());
					if (record == null && !DateUtils.isSameDay(currentDate, Calendar.getInstance())) {
						isReading = true;
						BleManager.INSTANCE.readHistoryPoseOriginal(device.mac, currentDate, 0, 0);
					} else {
						//如果到了今天，每4分钟拿一次数据
						if (Calendar.getInstance().get(Calendar.MINUTE) % 4 == 0) {
							isReading = true;
							BleManager.INSTANCE.readHistoryPoseOriginal(device.mac, currentDate, 0, 0);
						} else {
                            processNextRequest(true);
						}
					}
                    break;
                }
            }
        }

        //读取历史数据，根据读取记录计算起始段号及读取总段数
        private void readHisSecData(int requestId) {
            ReadRecord record = Dao.INSTANCE.queryReadRecord(device.isLeft, requestId, currentDate.getTime());
            int endSec = 0;
            if (record != null)  endSec = record.section;

            if (endSec == 96) {
                isReading = false;
                return;
            }
            int startSec = 96;
            //如果是今天
            if (DateUtils.isSameDay(Calendar.getInstance(), currentDate)){
                startSec = AlgLib.calcSection(96, Calendar.getInstance()) - 1;
                //当前处于当天第1段或此段数据已读取，不再读取
                if (startSec == 0 || startSec == endSec) {
                    isReading = false;
                    return;
                }
            }
            int totalSecs = startSec - endSec;
            switch(requestId) {
                case Constant.REQUEST_READ_HISTORY_STEP_SECTION:
                    isReading = true;
                    BleManager.INSTANCE.readHistoryStepSection(device.mac, currentDate, startSec, totalSecs);
                    break;
            }
        }

        //处理下一个有效日期请求
        public void processNextDate() {
            if (isReading) return;
            List<ByteDate> list = validDate.get(device.mac);
            if (list == null || list.isEmpty()) {
                currentDate = DateUtils.getStartOfDay(new Date());
            } else {
                //从最后一个元素拿
                currentDate = list.remove(list.size() - 1).toCalender();
            }
        }

        public void destroy() {
            ObservableMgr.getBleObservable().deleteObserver(observer);
        }
    }
}
