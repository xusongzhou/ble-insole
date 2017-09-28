package com.advanpro.fwtools.module.stat;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.advanpro.fwtools.Constant;
import com.advanpro.fwtools.R;
import com.advanpro.fwtools.common.util.DateUtils;
import com.advanpro.fwtools.common.util.StringUtils;
import com.advanpro.fwtools.common.view.TitleBar;
import com.advanpro.fwtools.db.Dao;
import com.advanpro.fwtools.db.RunLatlng;
import com.advanpro.fwtools.db.RunRecord;
import com.advanpro.fwtools.module.BaseActivity;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zeng on 2016/4/5.
 * 跑步轨迹日记录，可切换显示多次跑步的运动轨迹
 */
public class PathRecordActivity extends BaseActivity {
    private AMap aMap;
    private MapView mapView;
    private Spinner spinner;
    private TitleBar titleBar;
    private List<String> timeSecs = new ArrayList<>();
    private Map<Long, List<LatLng>> latLngMap = new HashMap<>();
    private List<RunRecord> records = new ArrayList<>();
    private int selectPosition;
    private List<Integer> colors = new ArrayList<>();
    private TextView tvSteps;
    private TextView tvDur;
    private TextView tvDist;
    private TextView tvPace;
    private TextView tvCal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_path_record);
        assignViews();
        loadData();
        initMap(savedInstanceState);
        initViews();
    }

    //加载数据库数据
    private void loadData() {
        for (int i = 0; i <= 169; i++) {
            colors.add(Color.argb(255, 85, 255, i));
        }
        long time = getIntent().getLongExtra(Constant.EXTRA_TIME, new Date().getTime());
        List<RunRecord> recs = Dao.INSTANCE.queryRunRecords(new Date(time));
        if (recs.size() == 0) {
            finish();
            return;
        }
        Collections.reverse(recs);
        for (RunRecord r : recs) {
            List<RunLatlng> runLatlngs = Dao.INSTANCE.queryRunLatlngs(r.getId());
            List<LatLng> latLngs = new ArrayList<>();
            for (RunLatlng rlls : runLatlngs) {
                latLngs.add(new LatLng(rlls.getLatitude(), rlls.getLongitude()));
            }
            if (latLngs.size() > 0) {
                latLngMap.put(r.getId(), latLngs);
                //下拉菜单数据
                String start = DateUtils.formatDate(r.getStartTime(), "HH:mm");
                String end = DateUtils.formatDate(r.getEndTime(), "HH:mm");
                timeSecs.add(start + " ~ " + end);
                records.add(r);
            }           
        }
    }

    protected void assignViews() {
        spinner = (Spinner) findViewById(R.id.spinner);
        mapView = (MapView) findViewById(R.id.map_view);
        titleBar = (TitleBar) findViewById(R.id.title_bar);
        tvSteps = (TextView) findViewById(R.id.tv_steps);
        tvDur = (TextView) findViewById(R.id.tv_duration);
        tvDist = (TextView) findViewById(R.id.tv_distance);
        tvPace = (TextView) findViewById(R.id.tv_pace);
        tvCal = (TextView) findViewById(R.id.tv_cal);
    }

    protected void initViews() {
        initTitleBar();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, timeSecs);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(listener);
    }

    private AdapterView.OnItemSelectedListener listener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            selectPosition = position;
            updateView();
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
        }
    };

    private void initTitleBar() {
        titleBar.setTitle(R.string.path);
        titleBar.setStartImageButtonVisible(true);
        titleBar.setOnMenuClickListener(new TitleBar.OnMenuClickListener() {
            @Override
            public void onMenuClick(View v) {
                switch(v.getId()) {
                    case R.id.btn_start:
                        finish();
                        break;
                }
            }
        });
    }

    /*
     * 初始化 AMap 对象
	 */
    private void initMap(Bundle savedInstanceState) {
        mapView.onCreate(savedInstanceState);// 必须要写
        if (aMap == null) {
            aMap = mapView.getMap();
        }
        aMap.setOnMapLoadedListener(new AMap.OnMapLoadedListener() {
            @Override
            public void onMapLoaded() {
                aMap.moveCamera(CameraUpdateFactory.zoomTo(17));//设置缩放级别，范围1~20之间，小数亦可
                updateView();
            }
        });
        aMap.getUiSettings().setZoomControlsEnabled(false);//隐藏缩放按键
    }

    /**
     * 根据记录坐标集合，重画轨迹
     */
    private void updateView() {
        if (records.isEmpty()) return;
        RunRecord record = records.get(selectPosition);
        if (record == null) {
            aMap.clear();
            tvSteps.setText("--");
            tvDist.setText("--");
            tvPace.setText("--");
            tvCal.setText("--");
            tvDur.setText("--");
        } else {
            tvSteps.setText(String.valueOf(record.steps));
            tvDist.setText(record.distance > 0 ? StringUtils.formatDecimal(record.distance, 2) : "0");
            tvPace.setText(record.rate > 0 ? StringUtils.formatDecimal(record.rate, 1) : "0");
            tvCal.setText(record.calorie > 0 ? StringUtils.formatDecimal(record.calorie, 1) : "0");
            tvDur.setText(String.format("%02d′%02d″", record.duration / 60, record.duration % 60));
            List<LatLng> list = latLngMap.get(record.getId());
            aMap.clear();
            if (list == null) return;            
            aMap.addPolyline(new PolylineOptions().colorValues(colors).width(25).addAll(list));
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            for (int i = 0; i < list.size(); i++) {
                LatLng latLng = list.get(i);
                builder.include(latLng);
                if (i == 0 || i == list.size() - 1) {
                    BitmapDescriptor bitmapDescriptor;
                    if (i == 0) {
                        bitmapDescriptor = BitmapDescriptorFactory.fromResource(R.drawable.start);
                    } else {
                        bitmapDescriptor = BitmapDescriptorFactory.fromResource(R.drawable.end);
                    }
                    MarkerOptions options = new MarkerOptions();
                    options.position(latLng).icon(bitmapDescriptor).anchor(0.5f, 1);
                    aMap.addMarker(options);
                }
            }
            // 移动地图，所有marker自适应显示。LatLngBounds与地图边缘100像素的填充区域
            aMap.moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 100));
        }
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }
}
