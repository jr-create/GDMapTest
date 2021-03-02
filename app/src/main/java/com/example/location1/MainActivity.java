package com.example.location1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;
;
import com.amap.api.maps.AMapOptions;
import com.amap.api.services.core.*;
import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.geocoder.GeocodeAddress;
import com.amap.api.services.geocoder.GeocodeQuery;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.amap.api.services.help.Inputtips;
import com.amap.api.services.help.InputtipsQuery;
import com.amap.api.services.help.Tip;
import com.example.location1.user.user;
import com.example.location1.user.user_Activity;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;


import static com.amap.api.services.geocoder.GeocodeSearch.*;

// AMap.OnMarkerClickListener,
public class MainActivity extends AppCompatActivity implements LocationSource, AMapLocationListener, AMap.OnMyLocationChangeListener {
    public static Context context;
    //    static Resources myResources = MainActivity.context.getResources();
    public static MapView gmapView = null;//找到地图控件, AMap.OnMyLocationChangeListener
    static AMap aMap;//初始化地图控制器对象
    private boolean isFirstLoc = true;//记录是否是第一次定位
    //定位需要的数据
    static LocationSource.OnLocationChangedListener mListener;//定位按钮
    static AMapLocationClient mlocationClient;
    static AMapLocationClientOption mLocationOption;
    //定位蓝点
    static MyLocationStyle myLocationStyle;
    //     搜索
    Marker[] markerN = new Marker[10];//设置 marker 的经纬度位置(只能有十个标记点)
    static int totalMarker = 0;
    public static LatLng mylatlng;
    public LatLng latLngd;
    public LatLonPoint point;//目标Point
    public static LatLonPoint mpoint;//设置一个地图点,LatLonPoint就是一个经纬度的封装类
    //公交
    private Button btn_1, btn_bus;//
    private ImageButton btn_2, btn_3;//公交，驾车路线
    private ImageView imv_b;//步行路线
    static TextView text;
    private TextView tv_location;//显示我的位置
    //选项卡
    public TabHost tabhost;
    BottomSheetBehavior behavior;
    //登录
    FloatingActionButton btn_user;
    TextView tv_user;
   public static TextView tv_age,tv_sex,tv_phone;//个人资料
    //退出系统
    private Button btn_exit;
    //后台服务
//    private MyService.MyBinder myBinder;
    //user
    private ImageView imv_u, imv_p, imv_s;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        context = this;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);//全屏显示

        exit_system();//退出系统
        //后台服务
//        service_start();
        init();
        //user资料
        imv_u.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(getApplicationContext(), user_Activity.class);
                intent.putExtra("imv","u");
                startActivityForResult(intent, 0);
            }
        });
        imv_p.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(getApplicationContext(),user_Activity.class);
                intent.putExtra("imv","p");
                startActivity(intent);
            }
        });
        imv_s.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(getApplicationContext(),user_Activity.class);
                intent.putExtra("imv","s");
                startActivity(intent);
            }
        });
        //公交站点activity
        btn_user.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), user.class);
                startActivityForResult(intent, 1);
            }
        });

        //初始化TabHost容器
        tabhost.setup();
        //在TabHost创建标签，然后设置：标题／图标／标签页布局
        tabhost.addTab(tabhost.newTabSpec("tab1").setIndicator("首页",null).setContent(R.id.tab1));
        tabhost.addTab(tabhost.newTabSpec("tab2").setIndicator("起止公交", null).setContent(R.id.tab2));
        tabhost.addTab(tabhost.newTabSpec("tab3").setIndicator("我的", null).setContent(R.id.tab3));

        View bottomSheet = findViewById(R.id.bottom_sheet);//拖拉框
        behavior = BottomSheetBehavior.from(bottomSheet);//
        behavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View view, int i) {
                //这里是bottomSheet状态的改变
                String state = "null";
                switch (i) {
                    case 1:
                        state = "STATE_DRAGGING";//过渡状态此时用户正在向上或者向下拖动bottom sheet
                        break;
                    case 2:
                        state = "STATE_SETTLING"; // 视图从脱离手指自由滑动到最终停下的这一小段时间
                        break;
                    case 3:
                        state = "STATE_EXPANDED"; //处于完全展开的状态
                        break;
                    case 4:
                        state = "STATE_COLLAPSED"; //默认的折叠状态
                        break;
                    case 5:
                        state = "STATE_HIDDEN"; //下滑动完全隐藏 bottom sheet
                        break;
                }
            }

            @Override
            public void onSlide(@NonNull View view, float v) {
                //这里是拖拽中的回调，根据slideOffset可以做一些动画
            }
        });
        //在activity执行onCreate时执行mMapView.onCreate(savedInstanceState)，创建地图
        try {
            gmapView = findViewById(R.id.gmapView);
            gmapView.onCreate(savedInstanceState);
            if (aMap == null) {
                aMap = MainActivity.gmapView.getMap();//初始化地图控制器对象
            }
            addAmap();//加入Amap
            setUpMap();//设置地图启动定位
            search_date(); //搜索

        } catch (Exception e) {
            Toast.makeText(this, "出现异常", Toast.LENGTH_SHORT).show();
        }
        /**
         * 显示我的位置
         */
        tv_location = findViewById(R.id.tv_location);
        // 拖拽事件
        aMap.setOnMarkerDragListener(new AMap.OnMarkerDragListener() {
            // 当marker开始被拖动时回调此方法, 这个marker的位置可以通过getPosition()方法返回。
            // 这个位置可能与拖动的之前的marker位置不一样。
            // marker 被拖动的marker对象。
            @Override
            public void onMarkerDragStart(Marker marker) {
                marker.setPosition(marker.getPosition());
                Log.e("TAG", "onMarkerDragStart:" + marker.getTitle() + marker.getPosition());
            }

            // 在marker拖动完成后回调此方法, 这个marker的位置可以通过getPosition()方法返回。
            // 这个位置可能与拖动的之前的marker位置不一样。
            // marker 被拖动的marker对象。
            @Override
            public void onMarkerDrag(Marker marker) {
                marker.setPosition(marker.getPosition());
                Log.e("TAG", "  onMarkerDrag:" + marker.getTitle() + marker.getPosition());
            }

            // 在marker拖动过程中回调此方法, 这个marker的位置可以通过getPosition()方法返回。
            // 这个位置可能与拖动的之前的marker位置不一样。
            // marker 被拖动的marker对象。
            @Override
            public void onMarkerDragEnd(Marker marker) {
                latLngd = marker.getPosition();
                String formatAddress = overGetLocation(1, marker, latLngd, null);
                Log.e("拖拽事件", "formatAddress:" + formatAddress);
                marker.setTitle(formatAddress + "附近");
                Log.e("TAG", "onMarkerDragEnd:" + marker.getTitle() + marker.getPosition());
            }
        });

        text.setMovementMethod(ScrollingMovementMethod.getInstance());
        searchBusStation();
        bus_se();

    }

    private void init() {
        //user
        imv_u = findViewById(R.id.imv_u);
        imv_p = findViewById(R.id.imv_p);
        imv_s = findViewById(R.id.imv_s);
        //登录
        tv_user = findViewById(R.id.tv_user);
        btn_user = findViewById(R.id.btn_user);
        tv_age = findViewById(R.id.tv_age);
        tv_sex = findViewById(R.id.tv_sex);
        tv_phone = findViewById(R.id.tv_phone);
        //公交
        btn_1 = findViewById(R.id.btn_1);
        btn_2 = findViewById(R.id.btn_2);
        btn_3 = findViewById(R.id.btn_3);
        imv_b = findViewById(R.id.imv_b);
        btn_bus = findViewById(R.id.btn_bus);
        text = findViewById(R.id.text);
        //选项卡
        tabhost = findViewById(R.id.tabhost);
    }

    private void setUpMap() {//地图构建
        if (aMap == null) {
            aMap = MainActivity.gmapView.getMap();//初始化地图控制器对象
        }
        aMap.getUiSettings().setZoomPosition(AMapOptions.ZOOM_POSITION_RIGHT_CENTER);//设置缩放控件在右边中间位置
        aMap.moveCamera(CameraUpdateFactory.zoomTo(300));//设置地图的放缩级别
        aMap.setLocationSource(this);//设置定位监听 通过aMap对象设置定位数据源的监听
        aMap.setMyLocationEnabled(true);//设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false
        aMap.getUiSettings().setMyLocationButtonEnabled(true);//设置默认定位按钮是否显示，非必需设置。
        aMap.getUiSettings().setCompassEnabled(true);      // 设置地图默认的指南针是否显示
        aMap.setMyLocationStyle(myLocationStyle);
        aMap.showIndoorMap(true);     //true：显示室内地图；false：不显示；
        aMap.getUiSettings().setScaleControlsEnabled(true);//控制比例尺控件是否显示
        aMap.setMyLocationType(aMap.LOCATION_TYPE_LOCATE);//设置定位的类型为定位模式，有定位、跟随或地图根据面向方向旋转几种
        //初始化定位
        mlocationClient = new AMapLocationClient(this);
        //初始化定位参数
        mLocationOption = new AMapLocationClientOption();
        //设置定位回调监听
        mlocationClient.setLocationListener(this);
        // 高精度定位模式：会同时使用网络定位和GPS定位，优先返回最高精度的定位结果，以及对应的地址描述信息
        //设置为高精度定位模式
        // 低功耗定位模式：不会使用GPS和其他传感器，只会使用网络定位（Wi-Fi和基站定位）；
        //设置定位模式为AMapLocationMode.Battery_Saving，低功耗模式。
//        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Battery_Saving);
        // 仅用设备定位模式：不需要连接网络，只使用GPS进行定位，这种模式下不支持室内环境的定位，自 v2.9.0 版本支持返回地址描述信息。
        // 设置定位模式为AMapLocationMode.Device_Sensors，仅设备模式。
        //mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Device_Sensors);
        myLocationStyle.showMyLocation(true);//设置是否显示定位小蓝点，用于满足只想使用定位，不想使用定位小蓝点的场景，设置false以后图面上不再有定位蓝点的概念，但是会持续回调位置信息。

        myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_SHOW);

        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        // SDK默认采用连续定位模式，时间间隔2000ms
        // 设置定位间隔，单位毫秒，默认为2000ms，最低1000ms。
        mLocationOption.setInterval(3000);
        // 设置定位同时是否需要返回地址描述
        //设置是否返回地址信息（默认返回地址信息）
        mLocationOption.setNeedAddress(true);
        // 设置是否允许模拟软件Mock位置结果，多为模拟GPS定位结果，默认为false，不允许模拟位置。
        // 设置是否允许模拟位置,默认为false，不允许模拟位置
        mLocationOption.setMockEnable(false);
        // 设置定位请求超时时间，默认为30秒
        // 单位是毫秒，默认30000毫秒，建议超时时间不要低于8000毫秒。
        mLocationOption.setHttpTimeOut(50000);
//        缓存机制默认开启，可以通过以下接口进行关闭。
        // 当开启定位缓存功能，在高精度模式和低功耗模式下进行的网络定位结果均会生成本地缓存，不区分单次定位还是连续定位。GPS定位结果不会被缓存。
        // 关闭缓存机制
        mLocationOption.setLocationCacheEnable(false);
        // 设置是否只定位一次，默认为false
        mLocationOption.setOnceLocation(false);//false重复定位  true 只定位一次
        //设置定位参数
        mlocationClient.setLocationOption(mLocationOption);
        // 此方法为每隔固定时间会发起一次定位请求，为了减络少电量消耗或网流量消耗，
        // 注意设置合适的定位时间的间隔（最小间隔支持为2000ms），并且在合适时间调用stopLocation()方法来取消定位请求
        // 在定位结束后，在合适的生命周期调用onDestroy()方法
        // 在单次定位情况下，定位无论成功与否，都无需调用stopLocation()方法移除请求，定位sdk内部会移除
        mlocationClient.startLocation();//启动定位
    }

    /**
     * 启动时
     * 激活定位
     * 点击定位按钮时
     */
    @Override
    public void activate(OnLocationChangedListener onLocationChangedListener) {
        aMap.clear();
        mListener = onLocationChangedListener;
        if (mListener == null) {
            setUpMap();
        }
        Toast.makeText(this, "开始定位", Toast.LENGTH_SHORT).show();
    }

    /**
     * 停止定位
     */
    @Override
    public void deactivate() {
        mListener = null;
        if (mlocationClient != null) {
            Toast.makeText(this, "停止定位", Toast.LENGTH_SHORT).show();
            mlocationClient.stopLocation();
            mlocationClient.onDestroy();
        }
        mlocationClient = null;
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    public AMapLocation aMapLocation;
    public String mcity;//定位城市

    @Override//位置信息
    public void onLocationChanged(AMapLocation aMapLocation) {
        this.aMapLocation = aMapLocation;
        mpoint = new LatLonPoint(aMapLocation.getLatitude(), aMapLocation.getLongitude());
        Log.v("我的位置", "经纬度" + mpoint);
        if (mListener != null && aMapLocation != null) {
            if (aMapLocation != null && aMapLocation.getErrorCode() == 0) {

                Log.e("位置", "onLocationChanged: " + tv_location.getText());
                int locationType = aMapLocation.getLocationType(); // 获取当前定位结果来源，如网络定位结果，详见定位类型表
                double latitude = aMapLocation.getLatitude(); // 获取纬度
                double longitude = aMapLocation.getLongitude(); // 获取经度
                float accuracy = aMapLocation.getAccuracy(); // 获取精度信息
                String address = aMapLocation.getAddress(); // 地址，如果option中设置isNeedAddress为false，则没有此结果，
                // 网络定位结果中会有地址信息，GPS定位不返回地址信息。
                String country = aMapLocation.getCountry(); // 国家信息
                String province = aMapLocation.getProvince(); // 省信息
                String city = aMapLocation.getCity(); // 城市信息
                mcity = city;
                String district = aMapLocation.getDistrict(); // 城区信息
                String street = aMapLocation.getStreet(); // 街道信息
                String streetNum = aMapLocation.getStreetNum(); // 街道门牌号信息
                String cityCode = aMapLocation.getCityCode(); // 城市编码
                String adCode = aMapLocation.getAdCode(); // 地区编码
                String aoiName = aMapLocation.getAoiName(); // 获取当前定位点的AOI信息
                String buildingId = aMapLocation.getBuildingId(); // 获取当前室内定位的建筑物Id
                String floor = aMapLocation.getFloor(); // 获取当前室内定位的楼层
                int gpsAccuracyStatus = aMapLocation.getGpsAccuracyStatus(); //获取GPS的当前状态
                // 获取定位时间
                Date date = new Date();
                SimpleDateFormat df = new SimpleDateFormat("yyyy年-MM月-dd日 a HH:mm:ss");
                df.format(date);
                tv_location.setText("我的位置:" + aMapLocation.getProvince() + aMapLocation.getCity() + aMapLocation.getDistrict()
                        + aMapLocation.getStreet());
                Log.i("信息", "onLocationChanged()---" + "\n"
                        + "--locationType:" + locationType + "\n"
                        + "--latitude:" + latitude + "\n"
                        + "--longitude:" + longitude + "\n"
                        + "--accuracy:" + accuracy + "\n"
                        + "--address:" + address + "\n"
                        + "--country:" + country + "\n"
                        + "--province:" + province + "\n"
                        + "--city:" + city + "\n"
                        + "--district:" + district + "\n"
                        + "--street:" + street + "\n"
                        + "--streetNum:" + streetNum + "\n"
                        + "--cityCode:" + cityCode + "\n"
                        + "--adCode:" + adCode + "\n"
                        + "--aoiName:" + aoiName + "\n"
                        + "--buildingId:" + buildingId + "\n"
                        + "--floor:" + floor + "\n"
                        + "--gpsAccuracyStatus:" + gpsAccuracyStatus + "\n"
                        + "--date:" + date);

                if (isFirstLoc) { // 设置缩放级别
                    aMap.moveCamera(CameraUpdateFactory.zoomTo(15));
                    // 将地图移动到定位点
                    aMap.moveCamera(CameraUpdateFactory.changeLatLng(
                            new LatLng(aMapLocation.getLatitude(), aMapLocation.getLongitude())));
                    isFirstLoc = false;
                }
                // 点击定位按钮 能够将地图的中心移动到定位点
                mListener.onLocationChanged(aMapLocation);

            } else {
                // 定位失败时，可通过ErrCode（错误码）信息来确定失败的原因，errInfo是错误信息，详见错误码表。
                String errText = "定位失败,ErrCode:" + aMapLocation.getErrorCode() + ", errInfo: " + aMapLocation.getErrorInfo();

                Log.e("定位AmapErr", errText);
                Toast.makeText(this, "定位失败请开启定位", Toast.LENGTH_SHORT).show();
            }
        }
    }
//    /**
//     * 后台连接ibind后台连接service
//     */
//    private ServiceConnection connection = new ServiceConnection() {
//        @Override
//        public void onServiceConnected(ComponentName name, IBinder service) {
//            Log.v("MainActivity", "服务连接成功");
//            myBinder = (MyService.MyBinder) service;
//        }
//        @Override
//        public void onServiceDisconnected(ComponentName name) {
//            Log.v("Maintivity", "服务断开连接");
//            Toast.makeText(getApplicationContext(), "Service中count的值为：" + myBinder.getCount(), Toast.LENGTH_SHORT).show();
//        }
//    };
//    //后台服务
//    private void service_start() {
//        Intent intent = new Intent(this, MyService.class);
//        bindService(intent, connection, BIND_AUTO_CREATE);
//        Toast.makeText(this, "服务绑定成功", Toast.LENGTH_SHORT).show();
//    }

    /**
     * 退出系统
     */
    private void exit_system() {
        btn_exit = findViewById(R.id.btn_exit);
        btn_exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showNormalDialog();
            }
        });
    }
    /**
     * 退出的Dialog
     */
    private void showNormalDialog() {
        final AlertDialog.Builder normalDialog =
                new AlertDialog.Builder(MainActivity.this);
        normalDialog.setIcon(R.drawable.ic_launcher);
        normalDialog.setTitle("退出程序");
        normalDialog.setMessage("是否退出系统?");
        normalDialog.setPositiveButton("确定",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(MainActivity.this, "退出系统", Toast.LENGTH_SHORT).show();
                        android.os.Process.killProcess(android.os.Process.myPid());
                    }
                });
        normalDialog.setNegativeButton("关闭",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //...To-do
                    }
                });
        // 显示
        normalDialog.show();
    }

    //公交
    SearchView edit_start, edit_end;
    ListView inputtip_list1;
    LatLonPoint busPoint[] = new LatLonPoint[2];
    Message message = new Message();
    int y = 0;

    /**
     * 起止地点路线查询
     */
    void bus_se() {
        edit_start = findViewById(R.id.edit_start);
        edit_end = findViewById(R.id.edit_end);
        edit_start.onActionViewExpanded();
        edit_start.setQueryHint("请输入开始地点");
        edit_start.setIconified(false);
        edit_end.onActionViewExpanded();
        edit_end.setQueryHint("请输入终止地点");
        edit_end.setIconified(false);
        inputtip_list1 = findViewById(R.id.inputtip_list1);
        try {
            initSearchView(edit_start, inputtip_list1);
            initSearchView(edit_end, inputtip_list1);
            inputtip_list1.setOnItemClickListener(new AdapterView.OnItemClickListener() {    //列表点击事件
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Log.i("MY", "setOnItemClickListener");
                    if (mCurrentTipList != null && mCurrentTipList.size() > position) {
                        Tip tip = mCurrentTipList.get(position);
                        if (message.what == 1) {
                            edit_start.setIconified(true);
                            edit_start.setQueryHint(tip.getName());
                            edit_start.clearFocus();
                            Log.e("起始位置", "onItemClick: " + tip.getName());
                        } else {
                            edit_end.setIconified(true);
                            edit_end.setQueryHint(tip.getName());
                            edit_end.clearFocus();
                            Log.e("终止位置", "onItemClick: " + tip.getName());
                        }
                        //searchPoi(tip);
                        busPoint[y++] = searchPoi(tip);
                    }
                }
            });
        } catch (Exception e) {
            Toast.makeText(this, "定位出错", Toast.LENGTH_SHORT).show();
        }
    }
    private void searchBusStation() {
        /**
         * 两个地点
         */
        btn_bus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BusResultListAdapter busResultListAdapter = new BusResultListAdapter();
                busResultListAdapter.searchRouteResult(getApplicationContext(), busPoint[0], busPoint[1], 1);
            }
        });
        /**
         * 公交线路
         */
        btn_1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), BusActivity.class);
                startActivity(intent);
            }
        });

/**
 * 自己和目标路线
 */
        try {
            btn_2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {//公交路线
                    overGetLocation(1, null, new LatLng(point.getLatitude(), point.getLongitude()), null);
                    Log.e("目标城市", "onClick: " + city_end);

                    if (point != null) {
                        Log.e("TAG", "btn_2.setOnClickListener: " + mpoint.toString() + "目标" + point.toString());
                        BusResultListAdapter busResultListAdapter = new BusResultListAdapter();
                        busResultListAdapter.searchRouteResult(getApplicationContext(), mpoint, point, 1);
                    } else {
                        Toast.makeText(MainActivity.this, "终点过远，容易错误", Toast.LENGTH_SHORT).show();
                    }
                }

            });
            btn_3.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {//驾车路线
                    if (point != null ) {

                        Log.e("TAG", "btn_2.setOnClickListener: " + mpoint.toString() + "目标" + point.toString());
                        BusResultListAdapter busResultListAdapter = new BusResultListAdapter();
                        busResultListAdapter.searchRouteResult(getApplicationContext(), mpoint, point, 0);
                    } else {
                        Toast.makeText(MainActivity.this, "终点过远，容易错误", Toast.LENGTH_SHORT).show();
                    }
                }
            });
            imv_b.setOnClickListener(new View.OnClickListener() {//步行路线
                @Override
                public void onClick(View v) {
                    if (point != null && mcity.equals(city_end)) {
                        Log.e("TAG", "btn_2.setOnClickListener: " + mpoint.toString() + "目标" + point.toString());
                        BusResultListAdapter busResultListAdapter = new BusResultListAdapter();
                        busResultListAdapter.searchRouteResult(getApplicationContext(), mpoint, point, 2);
                    } else {
                        Toast.makeText(MainActivity.this, "路程过远，容易错误", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } catch (Exception e) {
            Toast.makeText(this, "不可查询", Toast.LENGTH_SHORT).show();
        }
    }

    private void addAmap() {
        aMap.setOnInfoWindowClickListener(new AMap.OnInfoWindowClickListener() { // 点击标记框的事件//通过点击标题栏 删除标点
            @Override
            public void onInfoWindowClick(Marker marker) {
                marker.remove();
                totalMarker--;
            }
        });
//标记
        myLocationStyle = new MyLocationStyle();
        aMap.setOnMapClickListener(new AMap.OnMapClickListener() {//地图长按事件
            @Override
            public void onMapClick(LatLng latLng) {
                addMarkerToMap(latLng.latitude, latLng.longitude);
                point = new LatLonPoint(latLng.latitude, latLng.longitude);
                Log.e("标点Point", "onMapClick: " + point);
            }

        });
    }
    @Override
    protected void onResume() {
        super.onResume();
        // 重新绘制加载地图
        gmapView.onResume();
    }
    @Override
    protected void onPause() {
        super.onPause();
        // 暂停地图的绘制
        gmapView.onPause();
    }
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        gmapView.onSaveInstanceState(outState);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 销毁地图
        gmapView.onDestroy();
        if (null != mlocationClient) {
            mlocationClient.onDestroy();
        }
    }

    /**
     * \搜索栏
     */
    public  String DEFAULT_CITY =mcity;
    private List<com.amap.api.services.help.Tip> mCurrentTipList;
    private InputTipsAdapter mIntipAdapter;
    ListView mInputListView;
    SearchView searchView;
    public static final int REQUEST_SUC = 1000;

    private void search_date() {
        mInputListView = findViewById(R.id.inputtip_list);
        searchView = findViewById(R.id.keyWord);
        searchView.onActionViewExpanded();
        searchView.setIconified(true);
        initSearchView(searchView, mInputListView);//显示提示列表
//                searchView.setIconified(false);//设置搜索框直接展开显示。左侧有放大镜(在搜索框中) 右侧有叉叉 可以关闭搜索框
//                searchView.onActionViewExpanded();//设置搜索框直接展开显示。左侧有无放大镜(在搜索框中) 右侧无叉叉 有输入内容后有叉叉 不能关闭搜索框
//                searchView.setIconifiedByDefault(false);// 设置搜索框直接展开显示。左侧有放大镜(在搜索框外) 右侧无叉叉 有输入内容后有叉叉 不能关闭搜索框
        searchView.setSubmitButtonEnabled(false);//设置是否显示搜索框展开时的提交按钮
        mInputListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {    //列表点击事件
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.i("MY", "setOnItemClickListener");
                if (mCurrentTipList != null && mCurrentTipList.size() > position) {
                    Tip tip = mCurrentTipList.get(position);
                    searchView.setIconified(true);
                    searchView.setQueryHint(tip.getName());
                    searchView.clearFocus();
                    searchPoi(tip);
                }
            }
        });
    }
    //查询列表的方法
    private void initSearchView(final SearchView searchView, final ListView mInputListView) {

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            /**
             * 按下确认键触发，本例为键盘回车或搜索键
             */
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            /**
             * 输入字符变化时触发
             */
            @Override
            public boolean onQueryTextChange(String newText) {
                if (searchView == edit_start) {
                    message.what = 1;
                } else message.what = 0;
                if (!TextUtils.isEmpty(newText)) {//显示列表
                    InputtipsQuery inputquery = new InputtipsQuery(newText, DEFAULT_CITY);
                    Inputtips inputTips = new Inputtips(getApplicationContext(), inputquery);
                    inputTips.setInputtipsListener(new Inputtips.InputtipsListener() {
                        @Override
                        public void onGetInputtips(List<com.amap.api.services.help.Tip> list, int i) {
                            // 正确返回
                            if (i == REQUEST_SUC) {
                                mCurrentTipList = list;
                                mIntipAdapter = new InputTipsAdapter(getApplicationContext(), mCurrentTipList);
                                mInputListView.setAdapter(mIntipAdapter);
                                mIntipAdapter.notifyDataSetChanged();
                            } else {
                                Toast.makeText(getApplicationContext(), "错误码 :" + i, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                    inputTips.requestInputtipsAsyn();
                } else {
                    // 如果输入为空  则清除 listView 数据
                    if (mIntipAdapter != null && mCurrentTipList != null) {
                        mCurrentTipList.clear();
                        mIntipAdapter.notifyDataSetChanged();
                    }
                }
                return true;
            }
        });
        //清除焦点，收软键盘
        searchView.clearFocus();
    }

    /**
     * 列表点击后方法
     */
    private String inputSearchKey;
    PoiItem poiItem;

    private LatLonPoint searchPoi(Tip result) {
        try {
            inputSearchKey = result.getName();//getAddress(); // + result.getRegeocodeAddress().getCity() + result.getRegeocodeAddress().getDistrict() + result.getRegeocodeAddress().getTownship();
            point = result.getPoint();
            poiItem = new PoiItem("tip", point, inputSearchKey, result.getAddress());
            poiItem.setCityName(result.getDistrict());
            poiItem.setAdName("");
            aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(point.getLatitude(), point.getLongitude()), 16f));//将位置移到地图中心

            addMarkerToMap(point.getLatitude(), point.getLongitude());
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "找不到地址", Toast.LENGTH_SHORT).show();
        }
        Log.e("两点公交", "searchPoi: " + point);
        return point;
    }

    /**
     * /加入标点
     */
//    LatLng latLng;//标点的位置
    public void addMarkerToMap(double latitude, double longitude) {
        final LatLng latLng = new LatLng(latitude, longitude);
        try {

            aMap.setOnMarkerClickListener(new AMap.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(final Marker marker) {

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            marker.showInfoWindow();//显示标题栏
                        }
                    }).start();
                    return true;
                }
            });
            mylatlng = new LatLng(aMap.getMyLocation().getLatitude(), aMap.getMyLocation().getLongitude());
            float dis = AMapUtils.calculateLineDistance(mylatlng, latLng);
            Log.e("标点", "addMarkerToMap前: " + formatAddress);
            markerN[totalMarker] = aMap.addMarker(new MarkerOptions().position(latLng).title("").snippet("直线距离：" + dis/1000+"公里"));//加入标题框内容
            overGetLocation(1, markerN[totalMarker], latLng, null);
            Log.e("标点", "addMarkerToMap后: " + formatAddress);
            markerN[totalMarker].setTitle(formatAddress + "附近");
            markerN[totalMarker].setIcon(BitmapDescriptorFactory.fromResource(R.drawable.amap_end));//图片
            markerN[totalMarker].setDraggable(true);
            totalMarker++;
        } catch (Exception e) {
            Toast.makeText(context, "标点问题错误", Toast.LENGTH_SHORT).show();
        }

    }

    /**
     * /坐标转地址
     */
    String formatAddress = null;
    LatLonPoint cityPoint = null;
    static String city_end;//标点城市
    static String mark_end;//标点位置
    //参数：1.功能码  2.标点（可为空）   3.经纬度   4.城市名
    private String overGetLocation(int i, final Marker marker, final LatLng latLng, String cityName) {
        //逆地理编码坐标转地址
        GeocodeSearch geocoderSearch = new GeocodeSearch(this);
        geocoderSearch.setOnGeocodeSearchListener(new OnGeocodeSearchListener() {//解析result获取地址描述信息
            @Override
            public void onRegeocodeSearched(RegeocodeResult result, int rCode) {
                if (rCode == 1000) {
                    formatAddress = result.getRegeocodeAddress().getFormatAddress();
                    city_end = result.getRegeocodeAddress().getCity();
                    mark_end = result.getRegeocodeAddress().getFormatAddress();
                    Log.e("formatAddress", "overGetLocation:" + result.getRegeocodeAddress().getFormatAddress());
                    Log.e("formatAddress", "overGetLocation:" + formatAddress.substring(9));
                    Log.e("overGetLocation", "rCode:" + rCode);//错误码
                    if (marker != null) {
                        marker.setTitle(formatAddress.substring(3));
                    }
                } else {
                    Toast.makeText(MainActivity.this, "地名出错", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onGeocodeSearched(GeocodeResult geocodeResult, int i) {
                if (i == 1000) { //解析result获取坐标信息
                    if (geocodeResult != null && geocodeResult.getGeocodeAddressList() != null && geocodeResult.getGeocodeAddressList().size() > 0) {

                        GeocodeAddress geocodeAddress = geocodeResult.getGeocodeAddressList().get(0);
                        double latitude = geocodeAddress.getLatLonPoint().getLatitude();//纬度
                        double longititude = geocodeAddress.getLatLonPoint().getLongitude();//经度
                        String adcode = geocodeAddress.getAdcode();//区域编码
                        cityPoint = geocodeAddress.getLatLonPoint();
                        Log.e("lgq地理编码", cityPoint + "");
                        Log.e("lgq地理编码", geocodeAddress.getAdcode() + "");
                        Log.e("lgq纬度latitude", latitude + "");
                        Log.e("lgq经度longititude", longititude + "");
                    } else {
                        Toast.makeText(MainActivity.this, "地名出错", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
        if (i == 1) {
            LatLonPoint lp = new LatLonPoint(latLng.latitude, latLng.longitude);
            RegeocodeQuery query = new RegeocodeQuery(lp, 200, AMAP);
            geocoderSearch.getFromLocationAsyn(query);
            return formatAddress;
        } else {
            GeocodeQuery geocodeQuery = new GeocodeQuery(cityName.trim(), "29");
            geocoderSearch.getFromLocationNameAsyn(geocodeQuery);
            return cityPoint + "";
        }
    }

    //定位事件
    @Override
    public void onMyLocationChange(Location location) {
        if (location != null) {
            Bundle bundle = location.getExtras();
            if (bundle != null) {
                Toast.makeText(context, "定位成功", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(context, "定位失败，请检查您的定位权限", Toast.LENGTH_SHORT).show();
        }
    }

    //个人信息
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 1:
                if (resultCode == Activity.RESULT_OK) {
                    //如果resultCode是RESULT_OK的话，就把内容显示出来。
                    tv_user.setText(data.getExtras().getString("user"));
                    tv_phone.setText(data.getExtras().getString("phone"));
                    tv_sex.setText(data.getExtras().getString("sex"));
                    tv_age.setText(data.getExtras().getString("age"));
                }
                break;
            case 0:
                if (resultCode == Activity.RESULT_OK) {
                    //如果resultCode是RESULT_OK的话，就把内容显示出来。
//                    tv_user.setText(data.getExtras().getString("user"));
                    tv_phone.setText(data.getExtras().getString("phone"));
                    tv_sex.setText(data.getExtras().getString("sex"));
                    tv_age.setText(data.getExtras().getString("age"));
                    hideSoftKeyboard(this);
                }
        }
    }
    public static void hideSoftKeyboard(Activity activity) {
        View view = activity.getCurrentFocus();
        if (view != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }
//    /**
//     * /获取系统所有的LocationProvide的名称
//     */
//    private void getlocationName() {
//        locationName = findViewById(R.id.provider);
//        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
////第一种
////        List<String> proname=locationManager.getAllProviders();
////        //Iterator迭代器
////        StringBuilder stringBuilder=new StringBuilder();//字符串构建器
////       for (Iterator<String> iterator = proname.iterator(); iterator.hasNext();){
////            stringBuilder.append(iterator.next()+"\n");
////       }
////       provider.setText(stringBuilder.toString());//显示名称
//        /**第二种
//         * 基于GPS的locationProvide
//         */
////        LocationProvider locationManagerProvider = locationManager.getProvider(LocationManager.GPS_PROVIDER);
////        provider.setText(locationManagerProvider.getName());//获取LocationProvider的名称
//        /**
//         * 第三种 获取最佳的LocationProvider
//         */
//        Criteria criteria = new Criteria();//创建一个过滤条件对象
//        criteria.setCostAllowed(false);//不收费的
//        criteria.setAccuracy(Criteria.ACCURACY_FINE);//使用精度最准确的
//        criteria.setPowerRequirement(Criteria.POWER_LOW);//使用耗电量最低的
//        String providername = locationManager.getBestProvider(criteria, true);//获取最佳的Location名称
//        locationName.setText(providername);
//    }
//private void addMarkerToMap() {
//    LatLng latLng = new LatLng(39.9081728469, 116.3867845961);
//    MarkerOptions markerOption = new MarkerOptions();
//    markerOption.position(latLng);
//    markerOption.draggable(true);
//    markerOption.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_launcher_background));
//    Marker marker = aMap.addMarker(markerOption);
//    marker.setRotateAngle(30);
//}
//    /**
//     * /获取系统所有的LocationProvide的名称
//     */
//    private void getlocationName() {
//        locationName = findViewById(R.id.provider);
//        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
////第一种
////        List<String> proname=locationManager.getAllProviders();
////        //Iterator迭代器
////        StringBuilder stringBuilder=new StringBuilder();//字符串构建器
////       for (Iterator<String> iterator = proname.iterator(); iterator.hasNext();){
////            stringBuilder.append(iterator.next()+"\n");
////       }
////       provider.setText(stringBuilder.toString());//显示名称
//        /**第二种
//         * 基于GPS的locationProvide
//         */
////        LocationProvider locationManagerProvider = locationManager.getProvider(LocationManager.GPS_PROVIDER);
////        provider.setText(locationManagerProvider.getName());//获取LocationProvider的名称
//        /**
//         * 第三种 获取最佳的LocationProvider
//         */
//        Criteria criteria = new Criteria();//创建一个过滤条件对象
//        criteria.setCostAllowed(false);//不收费的
//        criteria.setAccuracy(Criteria.ACCURACY_FINE);//使用精度最准确的
//        criteria.setPowerRequirement(Criteria.POWER_LOW);//使用耗电量最低的
//        String providername = locationManager.getBestProvider(criteria, true);//获取最佳的Location名称
//        locationName.setText(providername);
//    }
}
