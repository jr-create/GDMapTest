package com.example.location1;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MultiPointItem;
import com.amap.api.maps.model.MultiPointOverlay;
import com.amap.api.maps.model.MultiPointOverlayOptions;
import com.amap.api.maps.model.Polyline;
import com.amap.api.maps.model.PolylineOptions;
import com.amap.api.navi.view.PoiInputResItemWidget;
import com.amap.api.services.busline.BusLineItem;
import com.amap.api.services.busline.BusLineQuery;
import com.amap.api.services.busline.BusLineResult;
import com.amap.api.services.busline.BusLineSearch;
import com.amap.api.services.busline.BusStationItem;
import com.amap.api.services.busline.BusStationQuery;
import com.amap.api.services.busline.BusStationResult;
import com.amap.api.services.busline.BusStationSearch;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;
import com.amap.api.services.route.BusPath;
import com.amap.api.services.route.BusRouteResult;
import com.amap.api.services.route.BusStep;
import com.amap.api.services.route.DriveRouteResult;
import com.amap.api.services.route.RideRouteResult;
import com.amap.api.services.route.RouteBusLineItem;
import com.amap.api.services.route.RouteBusWalkItem;
import com.amap.api.services.route.RouteSearch;
import com.amap.api.services.route.WalkRouteResult;

import java.util.ArrayList;
import java.util.List;

public class BusActivity extends AppCompatActivity {
    private ListView mBusResultList;
    private Button city_bus;
    EditText select_city, edit_search;

    Resources res;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bus);
        Log.i("BusActivity公交", "onCreate: " + MainActivity.mylatlng);
        mBusResultList = findViewById(R.id.mBusResultList);

        select_city = findViewById(R.id.select_city);
        edit_search = findViewById(R.id.edit_search);
        city_bus = findViewById(R.id.city_bus);

        res = this.getResources(); //这句放在onCreate中

        setSelect_city();
    }

    /**
     * 选择市区
     */
    AlertDialog alertDialog1;

    public void setSelect_city() {
        try {
            select_city.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    String[] items = res.getStringArray(R.array.city);
                    Dialog_show("城市名",items, null, 1);
                }
            });
            city_bus.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MainActivity.aMap.clear(true);
                    edit_search.clearFocus();
                    MainActivity.hideSoftKeyboard(BusActivity.this);
                    // 第一个查询关键字 第二个传入null或空字符串则为“全国”
                    final BusStationQuery busStationQuery = new BusStationQuery(edit_search.getText().toString(), select_city.getText().toString());
                    BusStationSearch busStationSearch = new BusStationSearch(getApplicationContext(), busStationQuery);
                    busStationSearch.setOnBusStationSearchListener(//解析result获取公交站点信息
                            new BusStationSearch.OnBusStationSearchListener() {
                                @Override
                                public void onBusStationSearched(BusStationResult busStationResult, int i) {
                                    //解析result获取算路结果，可参考官方demo
                                    busStationQuery.getCity();
                                    Log.e("公交站点的前:" + busStationQuery.getCity() + busStationResult.getBusStations(), i + "");
                                    final List<BusLineItem> list1 = busStationResult.getBusStations().get(0).getBusLineItems();//公交站的路线
                                    Log.e("公交站站点", "onBusStationSearched: " + busStationResult.getBusStations().get(0).getBusLineItems());
                                    ArrayAdapter<BusLineItem> adapter = new ArrayAdapter<BusLineItem>(getApplicationContext(), android.R.layout.simple_list_item_1, list1);
                                    mBusResultList.setAdapter(adapter);
                                    mBusResultList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                        @Override
                                        public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                                            Toast.makeText(BusActivity.this, list1.get(position).getBusLineName(), Toast.LENGTH_SHORT).show();
                                            new Thread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    Bus_Line(position, list1);
                                                }
                                            }).start();

                                        }
                                    });
                                }
                            });
                    busStationSearch.searchBusStationAsyn();
                }
            });
        } catch (Exception e) {
            Toast.makeText(this, "公交线路出错", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }

    }
//公交线路
    private void Bus_Line(int position, List<BusLineItem> list1) {
        //第一个参数为公交线路，第二个参数是公交线路搜索类型（不用变），第三个参数是城市名或区号
        BusLineQuery busLineQuery = new BusLineQuery(list1.get(position).getBusLineName(), BusLineQuery.SearchType.BY_LINE_NAME, select_city.getText().toString());
        busLineQuery.setPageSize(20);
        busLineQuery.setPageNumber(0);
        BusLineSearch busLineSearch = new BusLineSearch(getApplicationContext(), busLineQuery);

        Log.e("公交线路", "onBusLineSearched: 开始" + list1.get(position).getBusLineName());
        busLineSearch.setOnBusLineSearchListener(new BusLineSearch.OnBusLineSearchListener() {
            @Override
            public void onBusLineSearched(BusLineResult busLineResult, int i) {
                Log.e("公交线路1", "onBusLineSearched: bus开始");
                List<BusLineItem> busLines = busLineResult.getBusLines();
                if(busLines.get(0).getBusStations().size() != 0) {
                    final String item[] = new String[busLines.get(0).getBusStations().size()];
                    Log.e("公交线路", "onBusLineSearched: " + busLines.get(0).getBusStations().get(0).getBusStationName());
                    for (int x = 0; x < busLines.get(0).getBusStations().size(); x++) {//公交站名
                        bus[x] = busLines.get(0).getBusStations().get(x).getBusStationName();
                        item[x] = busLines.get(0).getBusStations().get(x).getBusStationName() + "站点";
                    }
                    Log.e("公交线路", "onBusLineSearched: " + busLines.get(0).getBusStations());
                    Dialog_show(busLines.get(0).getBusLineName() + "", item, busLines, 0);//在列表框中送入公交线路集合
                }else{
                    Toast.makeText(BusActivity.this, "没有线路", Toast.LENGTH_SHORT).show();
                }
            }
        });
        busLineSearch.searchBusLineAsyn();
    }

    /**
     * 列表框
     */
    private void Dialog_show(String title,final String[] items, final List<BusLineItem> busLines, final int code) {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
        alertBuilder.setTitle(title);
        alertBuilder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Toast.makeText(getApplicationContext(), items[i], Toast.LENGTH_SHORT).show();
                if (code == 1) {
                    select_city.setText(items[i]);
                    select_city.clearFocus();
                } else {
                    MainActivity.aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(busLines.get(0).getBusStations().get(i).getLatLonPoint().getLatitude(),busLines.get(0).getBusStations().get(i).getLatLonPoint().getLongitude()), 16f));
                    List<MultiPointItem> list = new ArrayList<>();
                    List<LatLng> latlngList = new ArrayList<>();

                    List<BusStationItem> busStations = busLines.get(0).getBusStations();
                    Log.e("公交线路", "onBusLineSearched: " + busLines.get(0).getBusStations());
                    for (BusStationItem station : busStations) {
                        LatLonPoint point = station.getLatLonPoint();
                        LatLng latLng = new LatLng(point.getLatitude(), point.getLongitude());
                        latlngList.add(latLng);
                        list.add(new MultiPointItem(latLng));
                    }
                    showLineOnMap(latlngList); // 显示Line
                    showMarkerOnMap(list); // 显示Marker
                }
                alertDialog1.dismiss();
            }
        });
        alertDialog1 = alertBuilder.create();
        alertDialog1.show();
    }

    String bus[] = new String[100];
    // 显示Line
    private void showLineOnMap(List<LatLng> list) {
        Polyline polyline = MainActivity.aMap.addPolyline((new PolylineOptions())
                .addAll(list)
                .width(8)
                .setDottedLine(false)
                .color(Color.RED));
    }

    // 显示Marker
    private void showMarkerOnMap(final List<MultiPointItem> list) {
        final MultiPointOverlayOptions overlayOptions = new MultiPointOverlayOptions();
        overlayOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.amap_through));
        overlayOptions.anchor(0.5f, 0.5f);

        final MultiPointOverlay multiPointOverlay = MainActivity.aMap.addMultiPointOverlay(overlayOptions);

        multiPointOverlay.setItems(list);//描点设置海量点，

        //点击事件显示站台
        MainActivity.aMap.setOnMultiPointClickListener(new AMap.OnMultiPointClickListener() {
            @Override
            public boolean onPointClick(MultiPointItem pointItem) {
                int i;
                for (i = 0; i < bus.length; i++) {
                    if (list.get(i).getIPoint().equals(pointItem.getIPoint())) {
                        Log.e("TAG", "站台为=" +bus[i]);
                        break;
                    }
                }
                Toast.makeText(getApplicationContext(), bus[i] + "站", Toast.LENGTH_SHORT).show();
                Log.e("TAG", "getIPoint=" + list.get(i).getIPoint() + pointItem.getIPoint());
                return true;
            }
        });
    }
}