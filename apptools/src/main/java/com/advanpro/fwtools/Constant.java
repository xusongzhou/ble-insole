package com.advanpro.fwtools;

/**
 * Created by zengfs on 2016/2/25.
 * 常量
 */
public interface Constant {
	//--------设备类型--------
	/**基础版，2传感器*/
	int DEVICE_TYPE_BASIC = 0;	
	/**普及版，4传感器*/
	int DEVICE_TYPE_POPULARITY = 1;
	/**增强版，6传感器*/
	int DEVICE_TYPE_ENHANCED = 2;
	
	//----------步态----------	
	/**脚跟*/
	int GAIT_HEEL = 0;
	/**脚掌*/
	int GAIT_FOREFOOT = 1;
	/**全脚*/
	int GAIT_SOLE = 2;
	/**前脚掌外翻*/
	int GAIT_FOREFOOT_ECTROPION = 3;
	/**前脚掌内翻*/
	int GAIT_FOREFOOT_VARUS = 4;
    /**全脚外翻*/
    int GAIT_SOLE_ECTROPION = 5;
    /**全脚内翻*/
    int GAIT_SOLE_VARUS = 6;
    /**大脚趾着地*/
    int GAIT_BIG_TOE = 7;

	//--------坐站姿----------
	int POSE_RUN = 1;
	int POSE_WALK = 2;
	int POSE_STAND = 3;
	int POSE_SIT = 4;
	
    //--------设备绑定结果-------
    int BIND_SUCCESS = 1;
    int BIND_FAILE = 2;
    
    //--------疲劳及损伤等级----------
    /**有点疲劳*/
    int FATIGUE_SOMEWHAT_HARD = 1;
    /**疲劳*/
    int FATIGUE_HARD = 2;
    /**非常疲劳*/
    int FATIGUE_VERY_HARD = 3;
    /**极度疲劳*/
    int FATIGUE_VERY_VERY_HARD = 4;
    /**损伤低风险*/
    int INJURE_LOW = 5;
    /**损伤中风险*/
    int INJURE_MIDDLE = 6;
    /**损伤高风险*/
    int INJURE_HIGH = 7;
    
    //---------数据表类型---------
    int TABLE_ACTIVITY = 1;
    int TABLE_GAIT = 2;
    int TABLE_ALARM = 3;
    int TABLE_RUN_PLAN = 4;
    int TABLE_RUN_RECORD = 5;
    
	//----------跑步计划----------
	int PLAN_DAILY_FITNESS = 1;
	int PLAN_LOSE_WEIGHT_EXERCISE = 2;
	int PLAN_MARATHON_TRAINING_5KM = 3;
	int PLAN_MARATHON_TRAINING_10KM = 4;
	int PLAN_MARATHON_TRAINING_FULL = 5;
    
	//----------startActivityForResult的请求码-------
	/**调用系统相机拍照*/
	int REQUEST_TAKE_PHOTO = 100;
	/**选择媒体库图片*/
	int REQUEST_SELECT_FROM_ALBUM = 101;
	/**头像剪裁完毕*/
	int REQUEST_HEAD_IMAGE_CLIP = 102;
	
	//----------SharedPreferences的keys---------
	String SP_VOICE_ENABLE = "voice_enable";
    String SP_LAST_SIGN_IN_TIME = "last_sign_in_time";	
    String SP_LAST_SIGN_IN_USER = "last_sign_in_user";	
    
	//----------Intent的Extra的keys----------
	String EXTRA_FROM_CLASS_NAME = "FROM_CLASS_NAME";
	String EXTRA_SONGLIST_ID = "SONGLIST_ID";
	String EXTRA_VALUE = "VALUE";
	String EXTRA_SERVICE_UUID = "SERVICE_UUID";
	String EXTRA_CHARACTERISTIC_UUID = "CHARACTERISTIC_UUID";
	String EXTRA_DESCRIPTOR_UUID = "DESCRIPTOR_UUID";
	String EXTRA_REQUEST_ID = "REQUEST_ID";
	String EXTRA_RUN_PLAN_TYPE = "RUN_PLAN_TYPE";
	String EXTRA_TIME = "TIME";
	String EXTRA_PATH = "PATH";
	String EXTRA_REQUEST_TYPE = "REQUEST_TYPE";
	String EXTRA_RESULT_ID = "RESULT_ID";

    //----------Action--------------
    String ACTION_SEND_RESTART_CMD = "com.advanpro.ansobuy.ACTION_SEND_RESTART_CMD";
    String ACTION_CLEAR_DEVICE_DATA = "com.advanpro.ansobuy.ACTION_CLEAR_DEVICE_DATA";
    
    //---------操作结果-------
    int RESULT_OK = 0;
    int RESULT_CANCEL = 1;
    int RESULT_FAILE = 2;
    
	//----------Handler的message的what----------
	int MESSAGE_CONNECTED = 1;
	int MESSAGE_DISCONNECTED = 2;
	int MESSAGE_SERVICES_DISCOVERED = 3;
	int MESSAGE_CHARACTERISTIC_VALUE = 4;
	int MESSAGE_REQUEST_FAILED = 5;
	int MESSAGE_DESCRIPTOR_VALUE = 6;
	int MESSAGE_WRITE_COMPLETE = 7;
	int MESSAGE_INDICATION_REGISTERED = 8;
	int MESSAGE_NOTIFICATION_REGISTERED = 9;
	int MESSAGE_REQUEST_NULL_CHARACTERISTIC = 10;
	int MESSAGE_REQUEST_NULL_DESCRIPTOR = 11;
	int MESSAGE_REQUEST_NULL_SERVICE = 12;
	int MESSAGE_GATT_STATUS_REQUEST_NOT_SUPPORTED = 13;

	//----------硬件请求码----------
	/**开启实时数据NOTIFICATION*/
	int REQUEST_REAL_TIME_NOTIFICATION = 1;
	/**开启历史数据NOTIFICATION*/
	int REQUEST_HISTORY_NOTIFICATION = 2;
	/**读取设备id*/
	int REQUEST_READ_DEVICE_ID = 3;
	/**读取电池电量*/
	int REQUEST_READ_BATTERY = 4;
	/**读取固件版本*/
	int REQUEST_READ_VV = 5;
    /**读取固件VV号*/
	int REQUEST_READ_FIRMWARE_VERSION = 6;
	/**设置设备时间*/
	int REQUEST_SET_TIME = 7;
	/**读取历史计步统计数据*/
	int REQUEST_READ_HISTORY_STEP_STAT = 8;
	/**读取历史步态次数统计数据*/
	int REQUEST_READ_HISTORY_GAIT_STAT = 9;
	/**读取历史姿势原始数据*/
	int REQUEST_READ_HISTORY_POSE_ORIGINAL = 10;
	/**读取历史步态原始数据*/
	int REQUEST_READ_HISTORY_GAIT_ORIGINAL = 11;
	/**读取历史计步分段统计数据*/
	int REQUEST_READ_HISTORY_STEP_SECTION = 12;	
    /**停止历史数据传输*/
	int REQUEST_STOP_HISTORY_DATA_TRANSFER = 13;
    /**使能OTA模式*/
    int REQUEST_OTA_MODE = 14;
    /**清硬件数据*/
    int REQUEST_CLEAR_DEVICE_DATA = 15;    
    /**重启设备*/
    int REQUEST_RESTART_DEVICE = 16;

	/**读取固件数据(如：历史数据)，用于调试*/
    int REQUEST_READ_FW_DATA = 18;

	//-----QQ APP_ID------
	String APPID="1105290285";
	//String APPID="222222";

	//-----WX APP_ID  AND SECRET------
	String WXAPPID="wx6fa9634a66db3c1b";
	String SECRET="c7f64ab59dbbb6ded1c0e1f258d1a1c1";

}
