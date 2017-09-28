package com.advanpro.fwtools.module.activity;

import android.content.Context;

import com.advanpro.fwtools.Constant;
import com.advanpro.fwtools.MyApplication;
import com.advanpro.fwtools.R;
import com.advanpro.fwtools.common.manager.MediaPlayerMgr;
import com.advanpro.fwtools.common.util.DateUtils;
import com.advanpro.fwtools.db.Dao;
import com.advanpro.fwtools.db.RunPlan;

import java.util.Date;

/**
 * Created by zeng on 2016/5/2.
 * 语音提醒
 */
public class VoiceWarner {
	private int planType = -1;
    private boolean isFulfill;
    private Context context;
    private int week;
    private int dayOfWeek;

    public VoiceWarner(Context context) {
        this.context = context;
        RunPlan plan = Dao.INSTANCE.queryRunPlan();
        if (plan != null) {
            planType = plan.type;
            int dayIndexOfPlan = DateUtils.daysBetween(plan.startDate, new Date()) + 1;
            char[] chars = plan.getFulfillState().toCharArray();
            if (dayIndexOfPlan < 1 || dayIndexOfPlan > chars.length) {
                planType = -1;
                return;
            }
            isFulfill = chars[dayIndexOfPlan - 1] == '1';
            week = dayIndexOfPlan / 7;
            dayOfWeek = dayIndexOfPlan % 7;
        }
    }
    
    public void processVoice(int duration) {
        if (planType == -1 || isFulfill || !MyApplication.isVoiceEnable) return;
        switch(planType) {
            case Constant.PLAN_DAILY_FITNESS:
            case Constant.PLAN_MARATHON_TRAINING_5KM: {
                int runMin = 0;
                int totalMin = 0;
                int cycle = 0;
                int runEndMin = 0;
                switch (week) {
                    case 0:
                        runMin = 5;
                        totalMin = 6;
                        if (dayOfWeek == 6) {
                            runMin = 6;
                            totalMin = 7;
                        }
                        cycle = 5;
                        break;
                    case 1:
                        runMin = 7;
                        totalMin = 8;
                        if (dayOfWeek == 6) {
                            runMin = 8;
                            totalMin = 9;
                        }
                        cycle = 5;
                        break;
                    case 2:
                        runMin = 9;
                        totalMin = 10;
                        if (dayOfWeek == 6) {
                            runMin = 10;
                            totalMin = 11;
                        }
                        cycle = 4;
                        break;
                    case 3:
                        runMin = 10;
                        totalMin = 11;
                        if (dayOfWeek == 3) {
                            runMin = 11;
                            totalMin = 12;
                        } else if (dayOfWeek == 6) {
                            runMin = 12;
                            totalMin = 13;
                        }
                        cycle = 3;
                        break;
                    case 4:
                        if (dayOfWeek == 1) {
                            runMin = 15;
                            runEndMin = 12;
                        } else if (dayOfWeek == 3) {
                            runMin = 17;
                            runEndMin = 10;
                        } else if (dayOfWeek == 6) {
                            runMin = 20;
                            runEndMin = 10;
                        }
                        break;
                    case 5:
                        runMin = 25;
                        runEndMin = 5;
                        break;
                    case 6:
                        if (dayOfWeek == 1) {
                            runMin = 26;
                        } else if (dayOfWeek == 3) {
                            runMin = 28;
                        } else if (dayOfWeek == 6) {
                            runMin = 30;
                        }
                        break;
                    case 7:
                        runMin = 30;
                        break;
                }
                //第1到4周
                if (week >=0 && week <= 3) {
                    if (dayOfWeek == 1 || dayOfWeek == 3 || dayOfWeek == 6) {
                        for (int i = 0; i < cycle; i++) {    
                            if (duration == 3) {
                                playRunFirst(runMin);
                            } else if (duration > 0 && duration == i * totalMin * 60) {
                                playRun(runMin);
                            } else if (duration == runMin * 60 + i * totalMin * 60) {
                                playWalk(1);
                            }
                        }
                    }
                }
                //第5、6周
                if (week == 4 || week == 5) {
                    if (dayOfWeek == 1 || dayOfWeek == 3 || dayOfWeek == 6) {
                        if (duration == 3) {
                            playRunFirst(runMin);
                        } else if (duration == runMin * 60) {
                            playWalk(1);
                        } else if (duration == runMin * 60 + 60) {
                            playRunEnd(runEndMin);
                        }
                    }
                }
                //第7、8周
                if (week == 6 || week == 7) {
                    if (dayOfWeek == 1 || dayOfWeek == 3 || dayOfWeek == 6) {
                        if (duration == 3) {
                            playRun(runMin);
                        } 
                    } else if (dayOfWeek == 7 && week == 7) {
                        if (duration == 3) {
                            if (planType == Constant.PLAN_DAILY_FITNESS) {
                                MediaPlayerMgr.INSTANCE.play(context, R.raw.finish_daily_plan);                                
                            } else {
                                MediaPlayerMgr.INSTANCE.play(context, R.raw.finish_5_km);
                            }
                        }
                    }
                }
                break;
            }
            case Constant.PLAN_LOSE_WEIGHT_EXERCISE: {
                switch (week) {
                    case 0:
                        if (dayOfWeek == 1) {                            
                            if (duration == 300 || duration == 600 || duration == 900 || duration == 1200) {
                                playFastRun(3);
                            } else if (duration == 480 || duration == 780 || duration == 1080 || duration == 1380 ||
                                    duration == 1920) {
                                playWalk(2);
                            } else if (duration == 1500 || duration == 1800) {
                                playFastRun(2);
                            } else if (duration == 1620) {
                                playWalk(3);
                            }
                        } else if (dayOfWeek == 3) {
                            if (duration == 300 || duration == 615 || duration == 930 || duration == 1230) {
                                playFastRun(3);
                            } else if (duration == 495 || duration == 810 || duration == 1110 || duration == 1725) {
                                playWalk(2);
                            } else if (duration == 480 || duration == 795 || duration == 1410) {
                                playSprintRun(80, 15);
                            } else if (duration == 1425) {
                                playWalk(3);
                            } else if (duration == 1605) {
                                playFastRun(2);
                            }
                        } else if (dayOfWeek == 5) {
                            if (duration == 300 || duration == 615 || duration == 930 || duration == 1245 || 
                                    duration == 1560 || duration == 1860) {
                                playFastRun(3);
                            } else if (duration == 495 || duration == 810 || duration == 1125 || duration == 1440 ||
                                    duration == 1740 || duration == 2040) {
                                playWalk(2);
                            } else if (duration == 480 || duration == 795 || duration == 1110 || duration == 1425) {
                                playSprintRun(80, 15);
                            }
                        }
                        break;
                    case 1:
                        if (dayOfWeek == 1) {
                            if (duration == 300 || duration == 615 || duration == 930 || duration == 1245 ||
                                    duration == 1560 || duration == 1860) {
                                playFastRun(3);
                            } else if (duration == 495 || duration == 810 || duration == 1125 || duration == 1440 ||
                                    duration == 1740 || duration == 2040) {
                                playWalk(2);
                            } else if (duration == 480 || duration == 795 || duration == 1110 || duration == 1425) {
                                playSprintRun(80, 15);
                            }
                        } else if (dayOfWeek == 3) {
                            if (duration == 300 || duration == 615 || duration == 930 || duration == 1245 ||
                                    duration == 1560 || duration == 1830) {
                                playFastRun(3);
                            } else if (duration == 495 || duration == 810 || duration == 1125 || duration == 1440) {
                                playWalk(2);
                            } else if (duration == 480 || duration == 795 || duration == 1110 || duration == 1425) {
                                playSprintRun(80, 15);
                            } else if (duration == 1740 || duration == 2010) {
                                playWalk(1.5f);
                            }
                        } else if (dayOfWeek == 5) {
                            if (duration == 300 || duration == 615 || duration == 930 || duration == 1230) {
                                playFastRun(3);
                            } else if (duration == 495 || duration == 810 || duration == 1110 || duration == 1725) {
                                playWalk(2);
                            } else if (duration == 480 || duration == 795 || duration == 1410) {
                                playSprintRun(80, 15);
                            } else if (duration == 1425) {
                                playWalk(3);
                            } else if (duration == 1605) {
                                playFastRun(2);
                            }
                        }
                        break;
                    case 2:
                        if (dayOfWeek == 1) {
                            if (duration == 300 || duration == 585 || duration == 870) {
                                playFastRun(3);
                            } else if (duration == 480 || duration == 765) {
                                playSprintRun(80, 15);
                            } else if (duration == 495 || duration == 780 || duration == 1285 || duration == 1615 ||
                                    duration == 1945 || duration == 2290) {
                                playWalk(1.5f);
                            } else if (duration == 1050 || duration == 2275) {
                                playSprintRun(100, 15);
                            } else if (duration == 1375 || duration == 1705 || duration == 2035) {
                                playFastRun(4);
                            }
                        } else if (dayOfWeek == 3) {
                            if (duration == 300 || duration == 615 || duration == 1500 || duration == 1800) {
                                playFastRun(4);
                            } else if (duration == 540 || duration == 855) {
                                playSprintRun(80, 15);
                            } else if (duration == 545 || duration == 870 || duration == 1155 || duration == 1440 ||
                                    duration == 1740 || duration == 2040) {
                                playWalk(1);
                            } else if (duration == 930 || duration == 1215) {
                                playFastRun(3.5f);
                            } else if (duration == 1140 || duration == 1425) {
                                playSprintRun(100, 15);
                            }
                        } else if (dayOfWeek == 5) {
                            if (duration == 300 || duration == 495 || duration == 690 || duration == 885 ||
                                    duration == 1080 || duration == 1365) {
                                playFastRun(3);
                            } else if (duration == 480 || duration == 675 || duration == 870 || duration == 1065) {
                                playSprintRun(80, 15);
                            } else if (duration == 1260 || duration == 1545 || duration == 1800 || duration == 2025) {
                                playSprintRun(100, 15);
                            } else if (duration == 1275 || duration == 1560) {
                                playWalk(1.5f);
                            } else if (duration == 1815 || duration == 2040) {
                                playWalk(1);
                            }
                        }
                        break;
                    case 3:
                        if (dayOfWeek == 1) {
                            if (duration == 300 || duration == 510 || duration == 720 || duration == 930 ||
                                    duration == 1140 || duration == 1395 || duration == 1650 || duration == 1905) {
                                playFastRun(3);
                            } else if (duration == 480 || duration == 690 || duration == 900 || duration == 1110) {
                                playSprintRun(80, 30);
                            } else if (duration == 1320 || duration == 1575 || duration == 1830 || duration == 2085) {
                                playSprintRun(100, 15);
                            } else if (duration == 1335 || duration == 1590 || duration == 1845 || duration == 2100) {
                                playWalk(1);
                            }
                        } else if (dayOfWeek == 3) {
                            if (duration == 300 || duration == 540 || duration == 780 || duration == 1020) {
                                playFastRun(3.5f);
                            } else if (duration == 510 || duration == 750 || duration == 990 || duration == 1230) {
                                playSprintRun(80, 30);
                            } else if (duration == 1260 || duration == 1515 || duration == 1770 || duration == 2025) {
                                playFastRun(3);
                            } else if (duration == 1440 || duration == 1695 || duration == 1950 || duration == 2205) {
                                playSprintRun(100, 15);
                            } else if (duration == 1455 || duration == 1710 || duration == 1965 || duration == 2040) {
                                playWalk(1);
                            }
                        } else if (dayOfWeek == 5) {
                            if (duration == 300 || duration == 540 || duration == 780 || duration == 1020 || duration == 1260 ||
                                    duration == 1545 || duration == 1830 || duration == 2115) {
                                playFastRun(3.5f);
                            } else if (duration == 510 || duration == 750 || duration == 990 || duration == 1230) {
                                playSprintRun(80, 30);
                            } else if (duration == 1470 || duration == 1755 || duration == 2040 || duration == 2325) {
                                playSprintRun(100, 15);
                            } else if (duration == 1485 || duration == 1770 || duration == 2055) {
                                playWalk(1);
                            }
                        }
                        break;
                    case 4:
                        if (dayOfWeek == 1) {
                            if (duration == 300 || duration == 510 || duration == 720 || duration == 930 || duration == 1125 ||
                                    duration == 1320) {
                                playFastRun(3);
                            } else if (duration == 480 || duration == 690 || duration == 900) {
                                playSprintRun(80, 30);
                            } else if (duration == 1515 || duration == 1815 || duration == 2295) {
                                playFastRun(4);
                            } else if (duration == 1110 || duration == 1305 || duration == 1500) {
                                playSprintRun(100, 15);
                            } else if (duration == 1755 || duration == 2055) {
                                playWalk(1);
                            }
                        } else if (dayOfWeek == 3) {
                            if (duration == 300 || duration == 570 || duration == 840 || duration == 1110) {
                                playFastRun(4);
                            } else if (duration == 1380 || duration == 1605 || duration == 1830 || duration == 2055) {
                                playFastRun(3.5f);
                            } else if (duration == 540 || duration == 810 || duration == 1080 || duration == 1350) {
                                playSprintRun(80, 30);
                            } else if (duration == 1590 || duration == 1815 || duration == 2040 || duration == 2265) {
                                playSprintRun(100, 15);
                            } else if (duration == 2280) {
                                playWalk(2);
                            }
                        } else if (dayOfWeek == 5) {
                            if (duration == 300 || duration == 570 || duration == 840 || duration == 1110 ||
                                    duration == 1380 || duration == 1635 || duration == 1890 || duration == 2145) {
                                playFastRun(4);
                            } else if (duration == 540 || duration == 810 || duration == 1080 || duration == 1350) {
                                playSprintRun(80, 30);
                            } else if (duration == 1620 || duration == 1875 || duration == 2130 || duration == 2385) {
                                playSprintRun(100, 15);
                            } else if (duration == 2400) {
                                playWalk(1);
                            }
                        }
                        break;
                    case 5:
                        if (dayOfWeek == 1) {
                            if (duration == 300 || duration == 570 || duration == 840 || duration == 1110 || 
                                    duration == 1380 || duration == 1635 || duration == 1890 || duration == 2145) {
                                playFastRun(4);
                            } else if (duration == 540 || duration == 810 || duration == 1080 || duration == 1350) {
                                playSprintRun(80, 30);
                            } else if (duration == 1620 || duration == 1875 || duration == 2130 || duration == 2385) {
                                playSprintRun(100, 15);
                            }
                        } else if (dayOfWeek == 3) {
                            if (duration == 300 || duration == 570 || duration == 840 || duration == 1110 ||
                                    duration == 1380 || duration == 1650 || duration == 1920 || duration == 2190) {
                                playFastRun(4);
                            } else if (duration == 540 || duration == 810 || duration == 1080 || duration == 1350) {
                                playSprintRun(80, 30);
                            } else if (duration == 1620 || duration == 1890 || duration == 2160 || duration == 2430) {
                                playSprintRun(100, 30);
                            }
                        } else if (dayOfWeek == 5) {
                            if (duration == 300 || duration == 540 || duration == 780 || duration == 1020 || duration == 1260 ||
                                    duration == 1500 || duration == 1740 || duration == 1980 || duration == 2220) {
                                playFastRun(3.5f);
                            } else if (duration == 510 || duration == 750 || duration == 990 || duration == 1230 || duration == 1470) {
                                playSprintRun(80, 30);
                            } else if (duration == 1710 || duration == 1950 || duration == 2190 || duration == 2430) {
                                playSprintRun(100, 30);
                            }
                        }
                        break;
                    case 6:
                        if (dayOfWeek == 1) {
                            if (duration == 300 || duration == 570 || duration == 840 || duration == 1110) {
                                playFastRun(4);
                            } else if (duration == 1380 || duration == 1605 || duration == 1830 || duration == 2055) {
                                playFastRun(3.5f);
                            } else if (duration == 540 || duration == 810 || duration == 1080 || duration == 1350) {
                                playSprintRun(80, 30);
                            } else if (duration == 1590 || duration == 1815 || duration == 2040 || duration == 2265) {
                                playSprintRun(100, 15);
                            } else if (duration == 2280) {
                                playWalk(2);
                            }
                        } else if (dayOfWeek == 3) {
                            if (duration == 300 || duration == 480) {
                                playFastRun(2.5f);
                            } else if (duration == 450 || duration == 840 || duration == 1050 || duration == 1260 ||
                                    duration == 1470 || duration == 1680) {
                                playSprintRun(80, 30);
                            } else if (duration == 630 || duration == 1890 || duration == 2100 || duration == 2310 ||
                                    duration == 2520) {
                                playSprintRun(100, 30);
                            } else if (duration == 660 || duration == 870 || duration == 1080 || duration == 1290 ||
                                    duration == 1500 || duration == 1710 || duration == 1920 || duration == 2130 ||
                                    duration == 2340) {
                                playFastRun(3);
                            }
                        } else if (dayOfWeek == 5) {
                            if (duration == 300 || duration == 480) {
                                playFastRun(2.5f);
                            } else if (duration == 450 || duration == 840 || duration == 1050 || duration == 1260 ||
                                    duration == 1470 || duration == 1680) {
                                playSprintRun(80, 30);
                            } else if (duration == 630 || duration == 1890 || duration == 2100 || duration == 2310 ||
                                    duration == 2520) {
                                playSprintRun(100, 30);
                            } else if (duration == 660 || duration == 870 || duration == 1080 || duration == 1290 ||
                                    duration == 1500 || duration == 1710 || duration == 1920 || duration == 2130 ||
                                    duration == 2340) {
                                playFastRun(3);
                            }
                        }
                        break;
                    case 7:
                        if (dayOfWeek == 1) {
                            if (duration == 300 || duration == 480 || duration == 660 || duration == 840 ||
                                    duration == 1020 || duration == 1200 || duration == 1380 || duration == 1560) {
                                playFastRun(2.5f);
                            } else if (duration == 450 || duration == 630 || duration == 810 || duration == 990 ||
                                    duration == 2280) {
                                playSprintRun(80, 30);
                            } else if (duration == 1170 || duration == 1350 || duration == 1530 || duration == 1710 ||
                                    duration == 2490) {
                                playSprintRun(100, 30);
                            } else if (duration == 1740 || duration == 1920) {
                                playFastRun(2);
                            } else if (duration == 1860) {
                                playSprintRun(80, 60);
                            } else if (duration == 2040) {
                                playSprintRun(100, 60);
                            } else if (duration == 2100 || duration == 2310) {
                                playFastRun(3);
                            }
                        } else if (dayOfWeek == 3) {
                            if (duration == 300 || duration == 480 || duration == 660 || duration == 840 ||
                                    duration == 1020 || duration == 1200) {
                                playFastRun(2.5f);
                            } else if (duration == 450 || duration == 630 || duration == 810 || duration == 2280) {
                                playSprintRun(80, 30);
                            } else if (duration == 990 || duration == 1170 || duration == 1350 || duration == 2490) {
                                playSprintRun(100, 30);
                            } else if (duration == 1500 || duration == 1680) {
                                playSprintRun(80, 60);
                            } else if (duration == 1860 || duration == 2040) {
                                playSprintRun(100, 60);
                            } else if (duration == 1380 || duration == 1560 || duration == 1740 || duration == 1920) {
                                playFastRun(2);
                            } else if (duration == 2100 || duration == 2310) {
                                playFastRun(3);
                            }
                        } else if (dayOfWeek == 5) {
                            if (duration == 300 || duration == 480 || duration == 660 || duration == 840 ||
                                    duration == 1020 || duration == 1200 || duration == 1380) {
                                playFastRun(2);
                            } else if (duration == 420 || duration == 600 || duration == 780 || duration == 1500) {
                                playSprintRun(80, 60);
                            } else if (duration == 960 || duration == 1140 || duration == 1320) {
                                playSprintRun(100, 60);
                            } else if (duration == 1710 || duration == 1890) {
                                playSprintRun(80, 30);
                            } else if (duration == 2070 || duration == 2250) {
                                playSprintRun(100, 30);
                            } else if (duration == 1560 || duration == 1740 || duration == 1920 || duration == 2100) {
                                playFastRun(2.5f);
                            }
                        } else if (dayOfWeek == 7) {
                            if (duration == 3) {
                                MediaPlayerMgr.INSTANCE.play(context, R.raw.finish_lose_weight_plan);
                            }
                        }
                        break;
                }
                
                if (dayOfWeek == 1 || dayOfWeek == 3 || dayOfWeek == 5) {
                    if (duration == 3) {
                        MediaPlayerMgr.INSTANCE.play(context, R.raw.warm_up_5_min_first);
                    }
                }
                break;
            }
            case Constant.PLAN_MARATHON_TRAINING_10KM: {
                switch (week) {
                    case 0:
                        if (dayOfWeek == 1 || dayOfWeek == 3 || dayOfWeek == 5) {
                            if (duration == 3) {
                                playFastRun(1);
                            } else if (duration == 180 || duration == 360 || duration == 540 ||
                                    duration == 720 || duration == 900 || duration == 1080 || duration == 1260 ||
                                    duration == 1440 || duration == 1620) {
                                playRun(1);
                            } else if (duration == 60 || duration == 240 || duration == 420 || duration == 600 ||
                                    duration == 780 || duration == 960 || duration == 1140 || duration == 1320 ||
                                    duration == 1500 || duration == 1680) {
                                playWalk(2);
                            }
                        } else if (dayOfWeek == 2 || dayOfWeek == 6) {
                            if (duration == 3) {
                                playWalk(30);
                            }
                        }
                        break;
                    case 1:
                        if (dayOfWeek == 1) {
                            if (duration == 3) {
                                playFastRun(2);
                            } else if (duration == 180 || duration == 360 || duration == 540 ||
                                    duration == 720 || duration == 900 || duration == 1080 || duration == 1260 ||
                                    duration == 1440 || duration == 1620) {
                                playRun(2);
                            } else if (duration == 120 || duration == 300 || duration == 480 || duration == 660 ||
                                    duration == 840 || duration == 1020 || duration == 1200 || duration == 1380 ||
                                    duration == 1560 || duration == 1740) {
                                playWalk(1);
                            }
                        } else if (dayOfWeek == 3) {
                            if (duration == 3) {
                                playFastRun(3);
                            } else if (duration == 240 || duration == 480 || duration == 720 ||
                                    duration == 960 || duration == 1200 || duration == 1440) {
                                playRun(3);
                            } else if (duration == 180 || duration == 420 || duration == 660 || duration == 900 ||
                                    duration == 1140 || duration == 1380 || duration == 1620) {
                                playWalk(1);
                            } else if (duration == 1680) {
                                playWalk(2);
                            }
                        } else if (dayOfWeek == 5) {
                            if (duration == 3) {
                                playFastRun(4);
                            } else if (duration == 300 || duration == 600 || duration == 900 ||
                                    duration == 1200 || duration == 1500) {
                                playRun(4);
                            } else if (duration == 240 || duration == 540 || duration == 840 || duration == 1140 ||
                                    duration == 1440 || duration == 1740) {
                                playWalk(1);
                            }
                        } else if (dayOfWeek == 2 || dayOfWeek == 6) {
                            if (duration == 3) {
                                playWalk(30);
                            }
                        }
                        break;
                    case 2:
                        if (dayOfWeek == 1 || dayOfWeek == 3) {
                            if (duration == 3) {
                                playFastRun(5);
                            } else if (duration == 360 || duration == 720 || duration == 1080 || duration == 1440) {
                                playRun(5);
                            } else if (duration == 300 || duration == 660 || duration == 1020 || duration == 1380 || duration == 1740) {
                                playWalk(1);
                            }
                        } else if (dayOfWeek == 5) {
                            if (duration == 3) {
                                playFastRun(6);
                            } else if (duration == 420 || duration == 840 || duration == 1260) {
                                playRun(6);
                            } else if (duration == 360 || duration == 780 || duration == 1200 || duration == 1620) {
                                playWalk(1);
                            } else if (duration == 1680) {
                                playRun(2);
                            }
                        } else if (dayOfWeek == 2 || dayOfWeek == 6) {
                            if (duration == 3) {
                                playWalk(30);
                            }
                        }
                        break;
                    case 3:
                        if (dayOfWeek == 1) {
                            if (duration == 3) {
                                playFastRun(8);
                            } else if (duration == 540 || duration == 1080) {
                                playRun(8);
                            } else if (duration == 480 || duration == 1020 || duration == 1560) {
                                playWalk(1);
                            } else if (duration == 1620) {
                                playRun(3);
                            }
                        } else if (dayOfWeek == 3) {
                            if (duration == 3) {
                                playFastRun(9);
                            } else if (duration == 600 || duration == 1200) {
                                playRun(9);
                            } else if (duration == 540 || duration == 1140 || duration == 1740) {
                                playWalk(1);
                            }
                        } else if (dayOfWeek == 5) {
                            if (duration == 3) {
                                playFastRun(10);
                            } else if (duration == 660) {
                                playRun(10);
                            } else if (duration == 600 || duration == 1260) {
                                playWalk(1);
                            } else if (duration == 1320) {
                                playRun(8);
                            }
                        } else if (dayOfWeek == 2 || dayOfWeek == 6) {
                            if (duration == 3) {
                                playWalk(30);
                            }
                        }
                        break;
                    case 4:
                        if (dayOfWeek == 1) {
                            if (duration == 3) {
                                playFastRun(12);
                            } else if (duration == 780) {
                                playRun(12);
                            } else if (duration == 720 || duration == 1500) {
                                playWalk(1);
                            } else if (duration == 1560) {
                                playRun(4);
                            }
                        } else if (dayOfWeek == 3) {
                            if (duration == 3) {
                                playFastRun(13);
                            } else if (duration == 840) {
                                playRun(13);
                            } else if (duration == 780 || duration == 1620) {
                                playWalk(1);
                            } else if (duration == 1680) {
                                playRun(2);
                            }
                        } else if (dayOfWeek == 5) {
                            if (duration == 3) {
                                playFastRun(14);
                            } else if (duration == 900) {
                                playRun(14);
                            } else if (duration == 840 || duration == 1740) {
                                playWalk(1);
                            }
                        } else if (dayOfWeek == 2 || dayOfWeek == 6) {
                            if (duration == 3) {
                                playWalk(30);
                            }
                        }
                        break;
                    case 5:
                        if (dayOfWeek == 1) {
                            if (duration == 3) {
                                playFastRun(16);
                            }  else if (duration == 960) {
                                playWalk(1);
                            } else if (duration == 1020) {
                                playRun(13);
                            }
                        } else if (dayOfWeek == 3) {
                            if (duration == 3) {
                                playFastRun(17);
                            } else if (duration == 1020) {
                                playWalk(1);
                            } else if (duration == 1080) {
                                playRun(12);
                            }
                        } else if (dayOfWeek == 5) {
                            if (duration == 3) {
                                playFastRun(18);
                            } else if (duration == 1080) {
                                playWalk(1);
                            } else if (duration == 1140) {
                                playRun(11);
                            }
                        } else if (dayOfWeek == 2 || dayOfWeek == 6) {
                            if (duration == 3) {
                                playWalk(30);
                            }
                        }
                        break;
                    case 6:
                        if (dayOfWeek == 1 || dayOfWeek == 3) {
                            if (duration == 3) {
                                playRunFirst(20);
                            } else if (duration == 1200) {
                                playWalk(1);
                            } else if (duration == 1260) {
                                playRun(9);
                            }
                        } else if (dayOfWeek == 5) {
                            if (duration == 3) {
                                playRunFirst(24);
                            } else if (duration == 1440) {
                                playWalk(1);
                            } else if (duration == 1500) {
                                playRun(5);
                            }
                        } else if (dayOfWeek == 2 || dayOfWeek == 6) {
                            if (duration == 3) {
                                playWalk(30);
                            }
                        }
                        break;
                    case 7:
                        if (dayOfWeek == 1) {
                            if (duration == 3) {
                                playRunFirst(27);
                            } else if (duration == 1620) {
                                playWalk(1);
                            } else if (duration == 1680) {
                                playRun(2);
                            }
                        } else if (dayOfWeek == 3) {
                            if (duration == 3) {
                                playRunFirst(28);
                            } else if (duration == 1680) {
                                playWalk(1);
                            } else if (duration == 1740) {
                                playRun(1);
                            }
                        } else if (dayOfWeek == 5) {
                            if (duration == 3) {
                                playRunFirst(26);
                            } else if (duration == 1560) {
                                playWalk(1);
                            } else if (duration == 1620) {
                                playRun(3);
                            } 
                        } else if (dayOfWeek == 2 || dayOfWeek == 6) {
                            if (duration == 3) {
                                playWalk(30);
                            }
                        } else if (dayOfWeek == 7) {
                            if (duration == 3) {
                                MediaPlayerMgr.INSTANCE.play(context, R.raw.finish_10_km);
                            }
                        }
                        break;
                }
                break;
            }
            case Constant.PLAN_MARATHON_TRAINING_FULL: {
                switch (week) {
                    case 0:
                        if (dayOfWeek == 2 || dayOfWeek == 3 || dayOfWeek == 4) {
                            if (duration == 3) playRunKm(5);
                        } else if (dayOfWeek == 6) {
                            if (duration == 3) playRunKm(10);
                        }
                        break;
                    case 1:
                        if (dayOfWeek == 2 || dayOfWeek == 4) {
                            if (duration == 3) playRunKm(5);
                        } else if (dayOfWeek == 3) {
                            if (duration == 3) playRunKm(6);
                        } else if (dayOfWeek == 6) {
                            if (duration == 3) playRunKm(8);
                        }
                        break;
                    case 2:
                        if (dayOfWeek == 2 || dayOfWeek == 4) {
                            if (duration == 3) playRunKm(5);
                        } else if (dayOfWeek == 3) {
                            if (duration == 3) playRunKm(8);
                        } else if (dayOfWeek == 6) {
                            if (duration == 3) playRunKm(15);
                        }
                        break;
                    case 3:
                        if (dayOfWeek == 2 || dayOfWeek == 4) {
                            if (duration == 3) playRunKm(5);
                        } else if (dayOfWeek == 3) {
                            if (duration == 3) playRunKm(10);
                        } else if (dayOfWeek == 6) {
                            if (duration == 3) playRunKm(21);
                        }
                        break;
                    case 4:
                        if (dayOfWeek == 2 || dayOfWeek == 4) {
                            if (duration == 3) playRunKm(6);
                        } else if (dayOfWeek == 3) {
                            if (duration == 3) playRunKm(12);
                        } else if (dayOfWeek == 6) {
                            if (duration == 3) playRunKm(25);
                        }
                        break;
                    case 5:
                        if (dayOfWeek == 2) {
                            if (duration == 3) playRunKm(6);
                        } else if (dayOfWeek == 3) {
                            if (duration == 3) playRunKm(15);
                        } else if (dayOfWeek == 4) {
                            if (duration == 3) playRunKm(8);
                        } else if (dayOfWeek == 6) {
                            if (duration == 3) playRunKm(30);
                        }
                        break;
                    case 6:
                        if (dayOfWeek == 2 || dayOfWeek == 4) {
                            if (duration == 3) playRunKm(8);
                        } else if (dayOfWeek == 3 || dayOfWeek == 6) {
                            if (duration == 3) playRunKm(15);
                        }
                        break;
                    case 7:
                        if (dayOfWeek == 2) {
                            if (duration == 3) playRunKm(5);
                        } else if (dayOfWeek == 3) {
                            if (duration == 3) playRunKm(8);
                        } else if (dayOfWeek == 4) {
                            if (duration == 3) playRunKm(3);
                        } else if (dayOfWeek == 7) {
                            if (duration == 3) {
                                MediaPlayerMgr.INSTANCE.play(context, R.raw.finish_10_km);
                            }
                        }
                        break;
                }
                break;
            }
        }
    }
    
    //先跑步几分钟
    private void playRunFirst(int minutes) {
        int resId;
        switch(minutes) {
            case 1:
                resId = R.raw.run_1_min_first;
                break;
            case 2:
                resId = R.raw.run_2_min_first;
                break;
            case 3:
                resId = R.raw.run_3_min_first;
                break;
            case 4:
                resId = R.raw.run_4_min_first;
                break;
            case 5:
                resId = R.raw.run_5_min_first;
                break;
            case 6:
                resId = R.raw.run_6_min_first;
                break;
            case 7:
                resId = R.raw.run_7_min_first;
                break;
            case 8:
                resId = R.raw.run_8_min_first;
                break;
            case 9:
                resId = R.raw.run_9_min_first;
                break;
            case 10:
                resId = R.raw.run_10_min_first;
                break;
            case 11:
                resId = R.raw.run_11_min_first;
                break;
            case 12:
                resId = R.raw.run_12_min_first;
                break;
            case 13:
                resId = R.raw.run_13_min_first;
                break;
            case 14:
                resId = R.raw.run_14_min_first;
                break;
            case 15:
                resId = R.raw.run_15_min_first;
                break;
            case 16:
                resId = R.raw.run_16_min_first;
                break;
            case 17:
                resId = R.raw.run_17_min_first;
                break;
            case 18:
                resId = R.raw.run_18_min_first;
                break;
            case 20:
                resId = R.raw.run_20_min_first;
                break;
            case 24:
                resId = R.raw.run_24_min_first;
                break;
            case 25:
                resId = R.raw.run_25_min_first;
                break;
            case 26:
                resId = R.raw.run_26_min_first;
                break;
            case 27:
                resId = R.raw.run_27_min_first;
                break;
            case 28:
                resId = R.raw.run_28_min_first;
                break;
            default:		
        		return;
        }
        MediaPlayerMgr.INSTANCE.play(context, resId);
    }

    //再跑几分钟
    private void playRunEnd(int minutes) {
        int resId;
        switch(minutes) {
            case 1:
                resId = R.raw.run_1_min_end;
                break;
            case 2:
                resId = R.raw.run_2_min_end;
                break;
            case 3:
                resId = R.raw.run_3_min_end;
                break;
            case 4:
                resId = R.raw.run_4_min_end;
                break;
            case 5:
                resId = R.raw.run_5_min_end;
                break;
            case 9:
                resId = R.raw.run_9_min_end;
                break;
            case 10:
                resId = R.raw.run_10_min_end;
                break;
            case 11:
                resId = R.raw.run_11_min_end;
                break;
            case 12:
                resId = R.raw.run_12_min_end;
                break;
            case 13:
                resId = R.raw.run_13_min_end;
                break;
            default:
                return;
        }
        MediaPlayerMgr.INSTANCE.play(context, resId);
    }
    
    //跑步几分钟
    private void playRun(int minutes) {
        int resId;
        switch(minutes) {
            case 1:
                resId = R.raw.run_1_min;
                break;
            case 2:
                resId = R.raw.run_2_min;
                break;
            case 3:
                resId = R.raw.run_3_min;
                break;
            case 4:
                resId = R.raw.run_4_min;
                break;
            case 5:
                resId = R.raw.run_5_min;
                break;
            case 6:
                resId = R.raw.run_6_min;
                break;
            case 7:
                resId = R.raw.run_7_min;
                break;
            case 8:
                resId = R.raw.run_8_min;
                break;
            case 9:
                resId = R.raw.run_9_min;
                break;
            case 10:
                resId = R.raw.run_10_min;
                break;
            case 11:
                resId = R.raw.run_11_min;
                break;
            case 12:
                resId = R.raw.run_12_min;
                break;
            case 26:	
                resId = R.raw.run_26_min;
        		break;
            case 28:
                resId = R.raw.run_28_min;
                break;
            case 30:
                resId = R.raw.run_30_min;
                break;
            default:		
        		return;
        }
        MediaPlayerMgr.INSTANCE.play(context, resId);
    }
    
    //步行几分钟
    private void playWalk(float minutes) {
        int resId;
        if (minutes == 1) {
            resId = R.raw.walk_1_min;
        } else if (minutes == 1.5){
            resId = R.raw.walk_1_5_min;
        } else if (minutes == 2){
            resId = R.raw.walk_2_min;
        } else if (minutes == 3) {
            resId = R.raw.walk_3_min;
        } else {
            return;
        }
        MediaPlayerMgr.INSTANCE.play(context, resId);
    }

    //快跑几分钟
    private void playFastRun(float minutes) {
        int resId;
        if (minutes == 2) {
            resId = R.raw.fast_run_2_min;
        } else if (minutes == 2.5) {
            resId = R.raw.fast_run_2_5_min;
        } else if (minutes == 3) {
            resId = R.raw.fast_run_3_min;
        } else if (minutes == 3.5) {
            resId = R.raw.fast_run_3_5_min;
        } else if (minutes == 4) {
            resId = R.raw.fast_run_4_min;
        } else {
            return;
        }
        MediaPlayerMgr.INSTANCE.play(context, resId);
    }
    
    //跑步几km
    private void playRunKm(int km) {
        int resId;
        switch(km) {
            case 3:
                resId = R.raw.go_run_3_km;
                break;
            case 5:
                resId = R.raw.go_run_5_km;
                break;
            case 6:
                resId = R.raw.go_run_6_km;
                break;
            case 8:
                resId = R.raw.go_run_8_km;
                break;
            case 10:
                resId = R.raw.go_run_10_km;
                break;
            case 12:
                resId = R.raw.go_run_12_km;
                break;
            case 15:
                resId = R.raw.go_run_15_km;
                break;
            case 25:
                resId = R.raw.go_run_25_km;
                break;
            case 30:
                resId = R.raw.go_run_30_km;
                break;
            default:
                return;
        }
        MediaPlayerMgr.INSTANCE.play(context, resId);
    }
    
    //冲刺跑
    private void playSprintRun(int percent, int seconds) {
        int resId;
        if (percent == 80 && seconds == 15) {
            resId = R.raw.p_80_s_15;
        } else if (percent == 80 && seconds == 30) {
            resId = R.raw.p_80_s_30;
        } else if (percent == 80 && seconds == 60) {
            resId = R.raw.p_80_s_60;
        } else if (percent == 100 && seconds == 15) {
            resId = R.raw.p_100_s_15;
        } else if (percent == 100 && seconds == 30) {
            resId = R.raw.p_100_s_30;
        } else if (percent == 100 && seconds == 60) {
            resId = R.raw.p_100_s_60;
        } else {
            return;
        }
        MediaPlayerMgr.INSTANCE.play(context, resId);
    }
}