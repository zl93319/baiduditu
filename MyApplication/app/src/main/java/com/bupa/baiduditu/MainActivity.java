package com.bupa.baiduditu;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.CircleOptions;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.Stroke;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiIndoorResult;
import com.baidu.mapapi.search.poi.PoiNearbySearchOption;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;
import com.baidu.mapapi.search.route.BikingRouteResult;
import com.baidu.mapapi.search.route.DrivingRouteLine;
import com.baidu.mapapi.search.route.DrivingRoutePlanOption;
import com.baidu.mapapi.search.route.DrivingRouteResult;
import com.baidu.mapapi.search.route.IndoorRouteResult;
import com.baidu.mapapi.search.route.MassTransitRouteResult;
import com.baidu.mapapi.search.route.OnGetRoutePlanResultListener;
import com.baidu.mapapi.search.route.PlanNode;
import com.baidu.mapapi.search.route.RoutePlanSearch;
import com.baidu.mapapi.search.route.TransitRouteLine;
import com.baidu.mapapi.search.route.TransitRoutePlanOption;
import com.baidu.mapapi.search.route.TransitRouteResult;
import com.baidu.mapapi.search.route.WalkingRouteResult;
import com.bupa.baiduditu.overlayutil.DrivingRouteOverlay;
import com.bupa.baiduditu.overlayutil.PoiOverlay;
import com.bupa.baiduditu.overlayutil.TransitRouteOverlay;
import com.labo.kaji.relativepopupwindow.RelativePopupWindow;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends ActionBarActivity implements
        BaiduMap.OnMarkerClickListener, OnGetPoiSearchResultListener, OnGetRoutePlanResultListener, BDLocationListener {

    private MapView mMapView; // 核心类
    private BaiduMap mBaiduMap; // 核心类
    protected LatLng zlLatLng = new LatLng(22.584812, 113.929805);//中粮所在位置
    protected LatLng stLatLng = new LatLng(22.585317, 113.929028);//申通所在位置
    protected LatLng btLatLng = new LatLng(22.566541, 113.887784);//宝体所在位置
    protected LatLng xxLatLng = new LatLng(22.582533, 113.868651);//西乡地铁站所在位置
    private View mPopview;  // popupwindow
    private TextView mTitle; // pop提示的名字
    private PoiSearch mSearch; // 搜索
    private PoiNearbySearchOption mOption; //位置
    private TransitRoutePlanOption mOption1;
    private RoutePlanSearch mSearch1;
    // 定位
    private static final int REQ_CODE = 101;//申请权限请求码
    private Context mContext;
    private LocationClient mLocationClient;
    private View mView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //获取地图控件引用
        mMapView = (MapView) findViewById(R.id.mapview);
        mBaiduMap = mMapView.getMap();
        mMapView.showZoomControls(false);//隐藏默认缩放控件
        //放大到18
        mBaiduMap.animateMapStatus(MapStatusUpdateFactory.zoomTo(18));
        //默认移动到中粮位置
        mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newLatLng(zlLatLng));

        // TODO: 2017/2/5 定位 初始化 start
        initDW();
        // TODO: 2017/2/5 定位 初始化 end
        // TODO: 2017/2/5 点击定位图标  start
        mView = findViewById(R.id.iv_loc);
        mView.setOnClickListener(new View.OnClickListener() {


            @Override
            public void onClick(View v) {
                // TODO: 2017/2/11 点击定位

                // 弹出win
                //判断系统版本，android 6.0以上，定位运行时权限
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {//android 6.0以上
                    //网络定位、GPS定位未获取，申请权限
                    if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_DENIED
                            || checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED) {
                        requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, REQ_CODE);
                    }
                }
                //开始定位
                Toast.makeText(mContext, "定位", Toast.LENGTH_SHORT).show();
                mLocationClient.start();
            }

        });
        // TODO: 2017/2/5 点击定位图标 end
    }

    private void initDW() {
        mContext = this;
        //第一步，初始化LocationClient类
        mLocationClient = new LocationClient(mContext);
        //注册定位监听
        mLocationClient.registerLocationListener(this);

        //开启定位图层
        mBaiduMap.setMyLocationEnabled(true);
        //设置定位图层参数
        mBaiduMap.setMyLocationConfigeration(getMyLocationConfiguration());


        //第二步，配置定位SDK参数
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy
        );//可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
        option.setCoorType("bd09ll");//可选，默认gcj02，设置返回的定位结果坐标系
        int span = 1000;
        option.setScanSpan(span);//可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的
        option.setIsNeedAddress(true);//可选，设置是否需要地址信息，默认不需要
        option.setOpenGps(true);//可选，默认false,设置是否使用gps
        option.setLocationNotify(true);//可选，默认false，设置是否当GPS有效时按照1S/1次频率输出GPS结果
        option.setIsNeedLocationDescribe(true);//可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”
        option.setIsNeedLocationPoiList(true);//可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
        option.setIgnoreKillProcess(false);//可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死
        option.SetIgnoreCacheException(false);//可选，默认false，设置是否收集CRASH信息，默认收集
        option.setEnableSimulateGps(false);//可选，默认false，设置是否需要过滤GPS仿真结果，默认需要

        mLocationClient.setLocOption(option);
    }

    /**
     * 定位模式 // TODO: 2017/2/11 定位模式
     *
     * @return
     */
    @NonNull
    private MyLocationConfiguration getMyLocationConfiguration() {
        return new MyLocationConfiguration(MyLocationConfiguration.LocationMode.FOLLOWING, true, null);
    }

    // TODO: 2017/2/5  // 定位 start
    @Override
    public void onReceiveLocation(BDLocation bdLocation) {
        if (bdLocation == null) {
            showLog("定位失败");
            return;
        }
        //获取定位方式
        //定位成功：gps、网络、离线定位
        int locType = bdLocation.getLocType();
        showLog("定位类型=" + locType);
        if (locType == BDLocation.TypeGpsLocation
                || locType == BDLocation.TypeNetWorkLocation || locType == BDLocation.TypeOffLineLocation) {
            showLog("定位成功");
            String addrStr = bdLocation.getAddrStr();
            showLog("addreStr=" + addrStr);
            //移动到当前位置
            double latitude = bdLocation.getLatitude();
            double longitude = bdLocation.getLongitude();
            LatLng latLng = new LatLng(latitude, longitude);
            mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newLatLng(latLng));

            String city = bdLocation.getCity();
            String province = bdLocation.getProvince();
            String district = bdLocation.getDistrict();


            showLog("province=" + province + ",district=" + district + ",city=" + city);

            //定位数据设置到定位图层
            MyLocationData myLocData = new MyLocationData.Builder()
                    .latitude(latitude)
                    .longitude(longitude)
                    .build();
            mBaiduMap.setMyLocationData(myLocData);

            //停止定位
            mLocationClient.stop();

        }
    }

    /**
     * 申请权限回调
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    // TODO: 2017/2/5 定位end
    private void showLog(String s) {
        Log.i("result", "" + s);
    }

    // 显示菜单
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_map_layer, menu);
        return super.onCreateOptionsMenu(menu);
    }
    // 监听菜单的点击

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_normal:// 普通地图
                mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
                mBaiduMap.setTrafficEnabled(false);//关闭实时交通图
                break;

            case R.id.action_sellate:// 卫星地图
                mBaiduMap.setMapType(BaiduMap.MAP_TYPE_SATELLITE);
                mBaiduMap.setTrafficEnabled(false);//关闭实时交通图
                break;

            case R.id.action_traffic:// 交通流量
                mBaiduMap.setTrafficEnabled(true);//开启实时交通图
                break;
            case R.id.action_zoomIn:// 放大
                MapStatusUpdate mapStatusUpdate = MapStatusUpdateFactory.zoomIn();
                //mBaiduMap.setMapStatus(mapStatusUpdate);//不带动画方式
                mBaiduMap.animateMapStatus(mapStatusUpdate);//带动画方式
                break;
            case R.id.action_zoomOut:// 缩小
                MapStatusUpdate mapStatusUpdate2 = MapStatusUpdateFactory.zoomOut();
                mBaiduMap.animateMapStatus(mapStatusUpdate2);
                break;
            case R.id.action_translate://移动地图到指定位置，作用：当定位成功，调用该功能，移动地图
                MapStatusUpdate mapStatusUpdate3 = MapStatusUpdateFactory.newLatLng(zlLatLng);
                mBaiduMap.animateMapStatus(mapStatusUpdate3);
                break;
            case R.id.action_rotate://旋转:百度地图没有提供旋转功能，需要构造
                //获取上次地图状体的旋转角度
                float old_rotate = mBaiduMap.getMapStatus().rotate;
                MapStatus mapStatus = new MapStatus.Builder().
                        rotate(old_rotate - 45.0F).build();//顺时针
                MapStatusUpdate mapStatusUpdate4 = MapStatusUpdateFactory.newMapStatus(mapStatus);
                mBaiduMap.animateMapStatus(mapStatusUpdate4);
                break;
            case R.id.action_mantle: // 覆盖物
                addOverlay(); // 添加覆盖物
                initPopView();// 初始化popupwindow
                break;
            case R.id.action_circle: // 圆形覆盖物
                addCircleOverlay();
                break;
            case R.id.action_nearbysearch: // 搜索附近
                searchNearby();
                initPopView();// 初始化popupwindowcase
                break;
            case R.id.action_drivingrouteplane: // 驾车导航
                routePlane();
                break;
            case R.id.action_transitrouteplane: // 路线规划
                transitRoutePlane();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    // 路线规划
    private void transitRoutePlane() {
        //路线规划四部曲

        //1.创建路线规划对象

        //2.设置路线规划参数
        mOption1 = new TransitRoutePlanOption();
        mOption1.from(PlanNode.withLocation(zlLatLng));//起点
        mOption1.to(PlanNode.withLocation(btLatLng));//终点
        mOption1.city("深圳");
        //最少步行
        mOption1.policy(TransitRoutePlanOption.TransitPolicy.EBUS_WALK_FIRST);//换乘策略：最少步行、时间优先...

        mSearch1 = RoutePlanSearch.newInstance();
        //3.调用对应的路线规划方法
        mSearch1.transitSearch(mOption1);
        //4.处理路线规划结果
        mSearch1.setOnGetRoutePlanResultListener(this);
    }

    // 驾车导航
    private void routePlane() {
        //1.创建路线规划对象
        RoutePlanSearch search = RoutePlanSearch.newInstance();
        //2.设置路线规划参数
        DrivingRoutePlanOption option = new DrivingRoutePlanOption();
        option.from(PlanNode.withLocation(zlLatLng));//起点
        option.to(PlanNode.withLocation(btLatLng));//终点
        List<PlanNode> planeNodes = new ArrayList<PlanNode>();
        planeNodes.add(PlanNode.withLocation(xxLatLng));//可以经过多个节点

        option.passBy(planeNodes);//路过节点

        //3.调用对应的路线规划方法（驾车、骑行、步行、换乘路线规划）
        search.drivingSearch(option);

        //4.监听路线规划结果回调
        search.setOnGetRoutePlanResultListener(this);
    }

    // 搜索附近
    private void searchNearby() {
        //1.创建poi搜索对象
        mSearch = PoiSearch.newInstance();
        mOption = new PoiNearbySearchOption();
        mOption.radius(1000);//半径
        mOption.keyword("超市");//搜索关键字
        mOption.location(zlLatLng);//搜索位置，相当于圆心
        //option.pageCapacity(10);每一页条数
        //option.pageNum();//页面，可以不设置

        //2.调用对应的搜索方法（附近、城市范围内、矩形范围内）
        mSearch.searchNearby(mOption);

        //3.监听搜索结果回调
        mSearch.setOnGetPoiSearchResultListener(MainActivity.this);

        //4.处理poi点击事件
    }

    // 显示圆形覆盖物
    private void addCircleOverlay() {
        //center:设置覆盖物中心点，圆心
        //radius:半径
        //fillColor:填充色
        //stroke:边框（颜色，宽度）
        OverlayOptions options = new CircleOptions()
                .center(zlLatLng)
                .radius(500)
                .fillColor(0x6676C8E2)
                .stroke(new Stroke(5, 0xaaff0000));
        mBaiduMap.addOverlay(options);//添加覆盖物核心代码

        /**
         * 覆盖物4部曲
         * 1.创建对应的覆盖物（标注、圆形、文本覆盖物..）
         * 2.设置覆盖物参数（图标、位置、半径、文本）
         * 3.添加覆盖物：mBaiduMap.addOverlay
         * 4.处理覆盖物点击事件（不一定需要）：mBaiduMap.setOnMarkerClickListener
         */
    }

    // 初始化pop
    private void initPopView() {
        mPopview = View.inflate(this, R.layout.pop, null);
        mTitle = (TextView) mPopview.findViewById(R.id.title);
    }

    // 用来显示覆盖物
    private void addOverlay() {
        //创建对应的覆盖物类型
        MarkerOptions options = new MarkerOptions();
        options.title("中粮商务公园");//设置标题，不显示，为了后面点击获取
        //设置覆盖物参数
        options.position(zlLatLng);//显示的位置
        options.icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_gcoding));//设置图标


        MarkerOptions options2 = new MarkerOptions();
        //设置覆盖物参数
        options2.position(stLatLng);//显示的位置
        options2.title("申通快递服务点");
        options2.icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_gcoding));//设置图标

        mBaiduMap.addOverlay(options);//添加覆盖物核心代码
        mBaiduMap.addOverlay(options2);

        //设置覆盖物点击
        mBaiduMap.setOnMarkerClickListener(this);
    }

    /*百度地图 性能优化 start*/
    @Override
    protected void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
        mMapView.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
        mMapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理
        mMapView.onPause();
    }

    /*百度地图 性能优化 end*/
    // 点击覆盖物 要做的事情 start
    @Override
    public boolean onMarkerClick(Marker marker) {
        //地图移动到覆盖物所在位置:修改地图状体
        LatLng position = marker.getPosition();
        mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newLatLng(position));

        String title = marker.getTitle();
        //弹窗覆盖物: 2-显示位置，3-y轴偏移量(为了防止弹窗图层覆盖后面的标记覆盖物)
        InfoWindow infoWindow = new InfoWindow(mPopview, marker.getPosition(), -100);
        mTitle.setText(title);
        mBaiduMap.showInfoWindow(infoWindow);
        return true;
    }

    // 点击覆盖物 要做的事情 end
    // 搜索的接口回调 start
    @Override
    public void onGetPoiResult(PoiResult poiResult) {
        if (poiResult == null || poiResult.error == PoiResult.ERRORNO.RESULT_NOT_FOUND) {
            Toast.makeText(this, "搜索失败", Toast.LENGTH_SHORT).show();
            //重新搜索
            mSearch.searchNearby(mOption);
        } else {
            Toast.makeText(this, "搜索成功", Toast.LENGTH_SHORT).show();
            //将POI添加到地图上（添加覆盖物，POI覆盖物）
            MyPoiOverlay overlay = new MyPoiOverlay(mBaiduMap);
            //给覆盖物设置数据
            overlay.setData(poiResult);// 注意：必须先设置数据，才能添加到地图，否则无法显示poi点
            overlay.addToMap();//覆盖物添加到地图
            overlay.zoomToSpan();//缩放到可视范围内，为了提高用户体验
            mBaiduMap.setOnMarkerClickListener(overlay);
        }
    }

    // 导航 start
    @Override
    public void onGetWalkingRouteResult(WalkingRouteResult walkingRouteResult) {

    }

    @Override
    public void onGetTransitRouteResult(TransitRouteResult transitRouteResult) {
        if (transitRouteResult == null || transitRouteResult.error == TransitRouteResult.ERRORNO.RESULT_NOT_FOUND) {
            Toast.makeText(this, "换乘路线规划失败", Toast.LENGTH_SHORT).show();
            //重新规划
            mSearch1.transitSearch(mOption1);
        } else if (transitRouteResult != null && transitRouteResult.error == TransitRouteResult.ERRORNO.NO_ERROR) {
            Toast.makeText(this, "换乘路线规划成功", Toast.LENGTH_SHORT).show();
            //覆盖物设置地图上
            //创建覆盖物
            TransitRouteOverlay overlay = new TransitRouteOverlay(mBaiduMap);
            //获取其中一条线路
            TransitRouteLine routeLine = transitRouteResult.getRouteLines().get(0);
            //覆盖物设置数据
            overlay.setData(routeLine);
            //显示到地图
            overlay.addToMap();
            overlay.zoomToSpan();//缩放到可视范围内
        }
    }

    @Override
    public void onGetMassTransitRouteResult(MassTransitRouteResult massTransitRouteResult) {

    }

    @Override
    public void onGetDrivingRouteResult(DrivingRouteResult drivingRouteResult) {
        if (drivingRouteResult == null || drivingRouteResult.error == DrivingRouteResult.ERRORNO.RESULT_NOT_FOUND) {
            Toast.makeText(this, "路线规划失败", Toast.LENGTH_SHORT).show();
        } else if (drivingRouteResult.error == DrivingRouteResult.ERRORNO.NO_ERROR) {//检索结果正常返回
            Toast.makeText(this, "路线规划成功", Toast.LENGTH_SHORT).show();
            //处理搜索结果：显示到地图
            //驾车路线规划覆盖物
            MyDrivingRouteOverlay overlay = new MyDrivingRouteOverlay(mBaiduMap);
            DrivingRouteLine routeLine = drivingRouteResult.getRouteLines().get(0);//获取第一条路线（路线很多）
            overlay.setData(routeLine);//设置路线数据
            overlay.addToMap();//覆盖物添加到地图
            overlay.zoomToSpan();//缩放到可视范围内
        }
    }


    private class MyDrivingRouteOverlay extends DrivingRouteOverlay {

        public MyDrivingRouteOverlay(BaiduMap baiduMap) {
            super(baiduMap);
        }

        /**
         * 起点图标
         */
        @Override
        public BitmapDescriptor getTerminalMarker() {
            return BitmapDescriptorFactory.fromResource(R.drawable.icon_en);
        }

        /**
         * 路线颜色
         */
        @Override
        public int getLineColor() {
            return Color.GREEN;
        }

        /**
         * 起点图标
         */
        @Override
        public BitmapDescriptor getStartMarker() {
            return BitmapDescriptorFactory.fromResource(R.drawable.icon_st);
        }
    }

    @Override
    public void onGetIndoorRouteResult(IndoorRouteResult indoorRouteResult) {

    }

    @Override
    public void onGetBikingRouteResult(BikingRouteResult bikingRouteResult) {

    }

    // 导航end
    // 点击附近坐标 start
    private class MyPoiOverlay extends PoiOverlay {


        public MyPoiOverlay(BaiduMap baiduMap) {
            super(baiduMap);
        }

        /**
         * poi点击回调方法
         */
        @Override
        public boolean onPoiClick(int i) {
            //获取当前点击的poi点
            PoiInfo poiInfo = getPoiResult().getAllPoi().get(i);
            String name = poiInfo.name;//超市名称
            Toast.makeText(MainActivity.this, ("name=" + name), Toast.LENGTH_SHORT).show();
            mTitle.setText(name);
            //移动地图到当前poi点的位置
            mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newLatLng(poiInfo.location));
            return super.onPoiClick(i);
        }
    }

    // 点击附近坐标 end
    @Override
    public void onGetPoiDetailResult(PoiDetailResult poiDetailResult) {

    }

    @Override
    public void onGetPoiIndoorResult(PoiIndoorResult poiIndoorResult) {

    }

    // 搜索的接口回调 end
}
