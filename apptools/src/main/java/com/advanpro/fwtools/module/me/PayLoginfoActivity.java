package com.advanpro.fwtools.module.me;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.advanpro.fwtools.R;
import com.advanpro.fwtools.common.base.BaseHolder;
import com.advanpro.fwtools.common.base.BaseListAdapter;
import com.advanpro.fwtools.common.util.DateUtils;
import com.advanpro.fwtools.common.view.TitleBar;
import com.advanpro.fwtools.entity.PayInfo;
import com.advanpro.fwtools.module.BaseActivity;
import com.advanpro.ascloud.ASCloud;
import com.advanpro.ascloud.CloudCallback;
import com.advanpro.ascloud.CloudException;
import com.advanpro.ascloud.CloudMsg;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PayLoginfoActivity extends BaseActivity {

    private Spinner spinner;
    private ListView payLogLv;
    private ArrayAdapter spinnerAdapter;
    private TextView time,timeText,content,money;
    private List<PayInfo> cloudlist=new ArrayList<>();
    private TitleBar titleBar;
    private String[] str;
    private PayLogLvAdapter payLogLvAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pay_loginfo);
        initData();
        initView();
        initEven();
    }


    //初始化数据
    private void initData(){
        payLogLvAdapter=new PayLogLvAdapter();
        cloudlist=new ArrayList<>();
        str=getResources().getStringArray(R.array.spinner_text);
        spinnerAdapter=new ArrayAdapter(this,android.R.layout.simple_list_item_1,str);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        loadData(DateUtils.formatDate(new Date(), "yyyy-MM-dd"), DateUtils.formatDate(new Date(), "yyyy-MM-dd"));

    }

    private void initView(){
        titleBar= (TitleBar) findViewById(R.id.title_bar);
        spinner= (Spinner) findViewById(R.id.spinner);
        payLogLv= (ListView) findViewById(R.id.pay_log_lv);
    }

    private void initEven(){
        spinner.setAdapter(spinnerAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String time=str[position].toString();
                if(time.equals("本周")){
                    String firstWeek=DateUtils.formatDate(DateUtils.getFirstDayOfWeek(new Date()),"yyyy-MM-dd");
                    String lastWeek=DateUtils.formatDate(DateUtils.getLastDayOfWeek(new Date()),"yyyy-MM-dd");
                    loadData(firstWeek,lastWeek);

                }else if(time.equals("本月")){
                    String firstMonth=DateUtils.formatDate(DateUtils.getEveryMonthFistDay(DateUtils.formatDate(new Date(),"yyyy"),DateUtils.formatDate(new Date(),"MM")),"yyy-MM-dd");
                    String lastMonth=DateUtils.formatDate(DateUtils.getEveryMonthLastDay(DateUtils.formatDate(new Date(), "yyyy"), DateUtils.formatDate(new Date(), "MM")),"yyyy-MM-dd");
                    loadData(firstMonth,lastMonth);
                }else if(time.equals("最近三月")){
                    int month=Integer.parseInt(DateUtils.formatDate(new Date(), "MM"));
                    String first=DateUtils.formatDate(DateUtils.getEveryMonthFistDay(DateUtils.formatDate(new Date(), "yyyy"), month - 1 + ""),"yyyy-MM-dd");
                    String last=DateUtils.formatDate(DateUtils.getEveryMonthLastDay(DateUtils.formatDate(new Date(), "yyyy"), month + 1 + ""),"yyyy-MM-dd");
                    loadData(first,last);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        payLogLv.setAdapter(payLogLvAdapter);
        titleBar.setOnMenuClickListener(new TitleBar.OnMenuClickListener() {
            @Override
            public void onMenuClick(View v) {
                if (v.getId() == R.id.btn_start) {
                    finish();
                }
            }
        });

    }



    //加载服务器数据
    private void loadData(String startDate,String stopData){
        CloudMsg msg = new CloudMsg("/user/getExchangeLog");
        msg.put("userId", ASCloud.userInfo.ID);
        msg.put("beginTime", startDate);
        msg.put("endTime", stopData);
        ASCloud.sendMsg(msg, new CloudCallback() {
            @Override
            public void success(CloudMsg cloudMsg) {
                if(cloudMsg!=null){
                    List<CloudMsg> cloudMsgs= null;
                    try {
                        cloudMsgs = cloudMsg.getList("datas");
                        if(cloudlist.size()>0){
                            for(CloudMsg cloudmsg:cloudMsgs){
                                cloudmsg.getBean(PayInfo.class);
                                cloudlist.add(new PayInfo());
                            }
                        }else{
                            //test
                            PayInfo payInfo=new PayInfo();
                            payInfo.setDetail("xxxxx");
                            payInfo.setMoney(235);
                            payInfo.setTime("2016-4-18 10:23:24");
                            cloudlist.add(payInfo);
                            payLogLvAdapter.setData(cloudlist);
                        }
                    } catch (CloudException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void error(CloudException e) {
                Log.e("TAG",e.toString());
            }
        });
    }




    public class PayLogLvAdapter extends BaseListAdapter<PayInfo>{

        public PayLogLvAdapter(List<PayInfo> data) {
            super(data);
        }

        public PayLogLvAdapter(){

        }



        @Override
        protected BaseHolder getHolder() {
            return new BaseHolder() {
                @Override
                protected void setData(Object data, int position) {
                    time= (TextView) getConvertView().findViewById(R.id.time);
                    timeText= (TextView) getConvertView().findViewById(R.id.timeText);
                    money= (TextView) getConvertView().findViewById(R.id.money);
                    content= (TextView) getConvertView().findViewById(R.id.content);
                    try {
                        PayInfo payInfo=(PayInfo)data;
                        if(payInfo!=null){

                            Date d=DateUtils.parseStringDate(DateUtils.formatDate(new Date(),"yyyy-MM-dd"),"yyyy-MM-dd");
                            Date da=DateUtils.parseStringDate(payInfo.getTime(),"yyyy-MM-dd HH:ss:mm");
                            if(d.equals(da) || d==da){
                                time.setText("今日");
                                timeText.setText(DateUtils.formatDate(DateUtils.parseStringDate(payInfo.getTime(),"yyyy-MM-dd HH:ss:mm"),"HH:ss"));
                            }else{
                                int state=DateUtils.getDayOfWeek(da);
                                switch (state){
                                    case 1:
                                        time.setText("周日");
                                        break;
                                    case 2:
                                        time.setText("周一");
                                        break;
                                    case 3:
                                        time.setText("周二");
                                        break;
                                    case 4:
                                        time.setText("周三");
                                        break;
                                    case 5:
                                        time.setText("周四");
                                        break;
                                    case 6:
                                        time.setText("周五");
                                        break;
                                    case 7:
                                        time.setText("周六");
                                        break;
                                }

                                timeText.setText(DateUtils.formatDate(DateUtils.parseStringDate(payInfo.getTime(),"yyyy-MM-dd HH:ss:mm"),"MM-dd"));
                            }

                            money.setText(payInfo.getMoney()+"");
                            content.setText(payInfo.getDetail());
                        }
                    }catch (ClassCastException e){
                        Log.e("TAG",e.getMessage());
                    }


                }

                @Override
                protected View createConvertView() {
                    return View.inflate(PayLoginfoActivity.this,R.layout.pay_loginfo_lv_item,null);
                }
            };
        }
    }

}
