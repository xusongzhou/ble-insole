package com.advanpro.fwtools.module.activity;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.advanpro.fwtools.R;
import com.advanpro.fwtools.common.view.TitleBar;
import com.advanpro.fwtools.db.RunLatlng;
import com.advanpro.fwtools.module.BaseActivity;
import com.amap.api.location.AMapLocation;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.maps.model.Polyline;
import com.amap.api.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zengfs on 2016/1/21.
 * 实时跑步轨迹
 */
public class PathActivity extends BaseActivity implements LocationSource {

	private AMap aMap;
	private MapView mapView;
	private TextView tvDistance;
	private TextView tvDuration;
	private OnLocationChangedListener listener;
	private Polyline polyline;
	private TitleBar titleBar;
	private boolean isSucceed;
	private List<LatLng> latLngs = new ArrayList<>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_path);
		assignViews();
		initMap(savedInstanceState);
		initViews();
	}

	protected void assignViews() {
		mapView = (MapView) findViewById(R.id.map_view);
		titleBar = (TitleBar) findViewById(R.id.title_bar);
		tvDistance = (TextView) findViewById(R.id.tv_distance);
		tvDuration = (TextView) findViewById(R.id.tv_duration);
	}

	protected void initViews() {		
		initTitleBar();				
		updateText();
	}

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
		// 自定义系统定位小蓝点
		MyLocationStyle myLocationStyle = new MyLocationStyle();
		myLocationStyle.myLocationIcon(BitmapDescriptorFactory.fromResource(R.drawable.location_marker));
		myLocationStyle.radiusFillColor(Color.argb(44, 66, 167, 254));// 设置圆形的填充颜色  
		myLocationStyle.strokeColor(Color.argb(44, 66, 167, 254));// 设置圆形的边框颜色
		aMap.setMyLocationStyle(myLocationStyle);
		aMap.setOnMapLoadedListener(new AMap.OnMapLoadedListener() {
			@Override
			public void onMapLoaded() {
				aMap.moveCamera(CameraUpdateFactory.zoomTo(17));//设置缩放级别，范围1~20之间，小数亦可
				updatePath();				
			}
		});
		aMap.getUiSettings().setZoomControlsEnabled(false);//隐藏缩放按键
		aMap.getUiSettings().setMyLocationButtonEnabled(true);// 设置默认定位按钮是否显示
		aMap.getUiSettings().setCompassEnabled(true);// 设置指南针可用
		aMap.setLocationSource(this);
		aMap.setMyLocationEnabled(true);// 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false
		// 设置定位的类型为定位模式 ，可以由定位、跟随或地图根据面向方向旋转几种
		aMap.setMyLocationType(AMap.LOCATION_TYPE_MAP_FOLLOW);
		//初始化画轨迹的参数
		polyline = aMap.addPolyline(new PolylineOptions());
		polyline.setWidth(25);
		polyline.setColor(Color.RED);        
	}

	/**
	 * 根据记录坐标集合，重画轨迹
	 */
	public void updatePath() {
		if (ActivityFragment.instance == null) return;
		latLngs.clear();
		for (RunLatlng runLatlng : ActivityFragment.instance.runLatlngs) {
			latLngs.add(new LatLng(runLatlng.getLatitude(), runLatlng.getLongitude()));
		}
		polyline.setPoints(latLngs);
	}

	/**
	 * 更新时长和距离文本
	 */
	public void updateText() {
		if (ActivityFragment.instance == null) return;
		tvDistance.setText((int) (ActivityFragment.instance.distance) + "");
		tvDuration.setText(String.format("%02d:%02d:%02d", ActivityFragment.instance.duration / 3600,
				ActivityFragment.instance.duration % 3600 / 60, ActivityFragment.instance.duration % 60));
	}
	
	/**
	 * 方法必须重写
	 */
	@Override
	protected void onResume() {
		super.onResume();
		mapView.onResume();
        aMap.moveCamera(CameraUpdateFactory.zoomTo(17));//设置缩放级别，范围1~20之间，小数亦可
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

	@Override
	public void activate(OnLocationChangedListener onLocationChangedListener) {
		listener = onLocationChangedListener;
	}

	@Override
	public void deactivate() {
	}

	public void onLocationChanged(AMapLocation aMapLocation) {
		//定位成功后，将提示隐藏
		if (!isSucceed) {
		    isSucceed = true;
			findViewById(R.id.tv_search).setVisibility(View.GONE);
		}
		if (listener != null) listener.onLocationChanged(aMapLocation);
	}
}
