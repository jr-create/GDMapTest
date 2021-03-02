package com.example.location1;


import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.util.Log;
import android.widget.Toast;

import com.amap.api.maps.AMap;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.Polyline;
import com.amap.api.maps.model.PolylineOptions;
import com.amap.api.navi.enums.PathPlanningStrategy;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.route.BusPath;
import com.amap.api.services.route.BusRouteResult;
import com.amap.api.services.route.BusStep;
import com.amap.api.services.route.DrivePath;
import com.amap.api.services.route.DriveRouteResult;
import com.amap.api.services.route.DriveStep;
import com.amap.api.services.route.RidePath;
import com.amap.api.services.route.RideRouteResult;
import com.amap.api.services.route.RideStep;
import com.amap.api.services.route.RouteBusLineItem;
import com.amap.api.services.route.RouteBusWalkItem;
import com.amap.api.services.route.RouteSearch;
import com.amap.api.services.route.WalkPath;
import com.amap.api.services.route.WalkRouteResult;
import com.amap.api.services.route.WalkStep;

import java.util.ArrayList;
import java.util.List;

public class BusResultListAdapter implements RouteSearch.OnRouteSearchListener {

    //    private BusRouteResult busRouteResult;// 公交模式查询结果
    private RouteSearch routeSearch;

    private static ProgressDialog progDialog = null;// 搜索时进度条
    LatLonPoint endPoint;

    public void searchRouteResult(Context context, LatLonPoint startPoint, LatLonPoint endPoint, int type) {
        routeSearch = new RouteSearch(context);
        routeSearch.setRouteSearchListener(this);
        this.endPoint = endPoint;
        final RouteSearch.FromAndTo fromAndTo = new RouteSearch.FromAndTo(startPoint, endPoint);
        Log.e("公交路线起止位置", "searchRouteResult: " + startPoint + "," + endPoint);
        showProgressDialog();
        if (type == 0) {
            RouteSearch.DriveRouteQuery query = new RouteSearch.DriveRouteQuery(fromAndTo, PathPlanningStrategy.DRIVING_DEFAULT, null, null, "");
            routeSearch.calculateDriveRouteAsyn(query);
        } else if (type == 1) {
            RouteSearch.BusRouteQuery query = new RouteSearch.BusRouteQuery(fromAndTo, RouteSearch.BusLeaseWalk, "全国", 0);
            // 第一个参数表示路径规划的起点和终点，第二个参数表示公交查询模式，第三个参数表示公交查询城市区号，第四个参数表示是否计算夜班车，0表示不计算
            routeSearch.calculateBusRouteAsyn(query);// 异步路径规划公交模式查询异步处理。----根据指定的参数来计算公交换乘路径的异步处理。只支持市内公交换乘。
        } else if (type == 2) {
            //步行：SDK提供两种模式：RouteSearch.WALK_DEFAULT 和 RouteSearch.WALK_MULTI_PATH（注意：过时）
            RouteSearch.WalkRouteQuery query2 = new RouteSearch.WalkRouteQuery(fromAndTo);
            routeSearch.calculateWalkRouteAsyn(query2);
        } else if (type == 3) {
            //骑行：（默认推荐路线及最快路线综合模式，可以接二参同上）
            RouteSearch.RideRouteQuery query3 = new RouteSearch.RideRouteQuery(fromAndTo);
            routeSearch.calculateRideRouteAsyn(query3);
        }
        System.out.println("========searchRouteResult()=========");
    }

    /**
     * 公交
     *
     * @param result
     * @param i
     * @peo:wangjingren
     */
    @Override
    public void onBusRouteSearched(BusRouteResult result, int i) {
        dissmissProgressDialog();
//rCode返回结果成功或者失败的响应码。1000为成功，其他为失败（详细信息参见网站开发指南-错误码对照表）
        Log.e("响应码", "onBusRouteSearched: " + i);
        try {
            if (result != null && result.getPaths() != null && result.getPaths().size() > 0) {
                MainActivity.aMap.clear();
                Log.e("公交路线", "onBusRouteSearched: " + result.getPaths().get(0));
                //几种公交路线
                List<BusPath> busPathList = result.getPaths();
                //选择第一条
                List<BusStep> busSteps = busPathList.get(0).getSteps();
                String routeInfo = "公交路线长度：" + busPathList.get(0).getBusDistance()
                        + "米  步行长度" + busPathList.get(0).getWalkDistance() + "米  线路总长度："
                        + busPathList.get(0).getDistance() + "米  公交换乘费用："
                        + busPathList.get(0).getCost() + "元 \n";

                for (BusStep bs : busSteps) {
                    //获取该条路线某段公交路程步行的点
                    RouteBusWalkItem routeBusWalkItem = bs.getWalk();
                    if (routeBusWalkItem != null) {
                        List<WalkStep> wsList = routeBusWalkItem.getSteps();
                        ArrayList<LatLng> walkPoint = new ArrayList<>();

                        RouteBusWalkItem walkPath = bs.getWalk();
                        if (bs.getRailway() != null)//火车信息
                            routeInfo += "从:" + bs.getRailway().getDeparturestop().getName()
                                    + "乘坐 " + bs.getRailway().getTrip()
                                    + "号列车 到" + bs.getRailway().getArrivalstop().getName()
                                    + "下车 价格：" + bs.getRailway().getSpaces().get(1).getCost() + "左右\n";
                        if (walkPath.getDistance() != 0)
                            routeInfo = routeInfo + "需要步行大约"
                                    + Math.round(walkPath.getDuration() / 60)
                                    + "分钟，步行" + walkPath.getDistance() + "米\n";

                        if (bs.getBusLine() != null) {
                            RouteBusLineItem busLineItem = bs.getBusLine();
                            routeInfo += "从" + busLineItem.getDepartureBusStation().getBusStationName()
                                    + "上车，乘坐"
                                    + busLineItem.getBusLineName()
                                    + "需要大约"
                                    + Math.round(busLineItem.getDuration() / 60)
                                    + "分钟，大约"
                                    + busLineItem.getDistance()
                                    + "米，经过"
                                    + busLineItem.getPassStationNum()
                                    + "站，从"
                                    + busLineItem.getArrivalBusStation()
                                    .getBusStationName() + "下车\n";
                        }
                        for (WalkStep ws : wsList) {
                            List<LatLonPoint> points = ws.getPolyline();
                            for (LatLonPoint lp : points) {
                                walkPoint.add(new LatLng(lp.getLatitude(), lp.getLongitude()));
                            }
                        }
                        show_Marker();//显示终点
                        //添加步行点
                        MainActivity.aMap.addPolyline(new PolylineOptions()
                                .addAll(walkPoint)
                                .width(40)
                                //是否开启纹理贴图
                                .setUseTexture(true)
                                //绘制成大地线
                                .geodesic(false)
                                //设置纹理样式
                                .setCustomTexture(BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(MainActivity.context.getResources()
                                        , R.drawable.ai)))
                                //设置画线的颜色
                                .color(Color.argb(200, 0, 245, 255)));//argb透明度
                        Log.e("添加步行点", "onBusRouteSearched: 成功");
                    }

                    //获取该条路线某段公交路路程的点

                    List<RouteBusLineItem> rbli = bs.getBusLines();
                    ArrayList<LatLng> busPoint = new ArrayList<>();

                    for (RouteBusLineItem one : rbli) {

                        List<LatLonPoint> points = one.getPolyline();
//                        List<LatLonPoint> points = bs.getBusLines().get(0).getPolyline();
                        for (LatLonPoint lp : points) {
                            busPoint.add(new LatLng(lp.getLatitude(), lp.getLongitude()));
                        }
                    }

                    //添加公交路线点
                    MainActivity.aMap.addPolyline(new PolylineOptions()
                            .addAll(busPoint)
                            .width(40)
                            //是否开启纹理贴图
                            .setUseTexture(true)
                            //绘制成大地线
                            .geodesic(false)
                            //设置纹理样式
                            .setCustomTexture(BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(MainActivity.context.getResources()
                                    , R.drawable.bus)))
                            //设置画线的颜色
                            .color(Color.argb(200, 0, 245, 255)));
                }
                MainActivity.text.setText(routeInfo);//路线
                Toast.makeText(MainActivity.context, "查询成功", Toast.LENGTH_SHORT).show();
                Log.e("BusResultListAdapter", "BusLine1: " + routeInfo);
            } else {
                Toast.makeText(MainActivity.context, "对不起，没查询到结果", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(MainActivity.context, "公交路线出现异常", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 显示标点
     */
    private void show_Marker() {

        MarkerOptions markerOption = new MarkerOptions();
        markerOption.position(new LatLng(endPoint.getLatitude(), endPoint.getLongitude()));
        markerOption.draggable(false);
        markerOption.icon(BitmapDescriptorFactory.fromResource(R.drawable.amap_end));
        LatLng latLng = new LatLng(endPoint.getLatitude(), endPoint.getLongitude());
        MainActivity.aMap.addMarker(markerOption.position(latLng).title(MainActivity.mark_end).snippet("这是目的地"));
    }
    /**
     * 驾车
     *
     * @param driveRouteResult
     * @param i
     */
    @Override
    public void onDriveRouteSearched(DriveRouteResult driveRouteResult, int i) {
        dissmissProgressDialog();
        Log.e("CF", "onDriveRouteSearched: " + i);

        List<DrivePath> pathList = driveRouteResult.getPaths();
        List<LatLng> driverPath = new ArrayList<>();

        String routeInfo = "驾车线路长度：" + pathList.get(0).getDistance() + "\n";
        routeInfo = routeInfo + "\n" + "预计时间：" + pathList.get(0).getDuration() / 60 + "分钟" + "\n"
                + "收费价格:" + pathList.get(0).getTolls() + "元";
        MainActivity.text.setText(routeInfo);
        for (DrivePath dp : pathList) {

            List<DriveStep> stepList = dp.getSteps();
            for (DriveStep ds : stepList) {

                List<LatLonPoint> points = ds.getPolyline();
                for (LatLonPoint llp : points) {
                    driverPath.add(new LatLng(llp.getLatitude(), llp.getLongitude()));
                }
            }
        }
        int icon = R.drawable.route_drive_select;

        root_Line(driverPath, icon);
        show_Marker();
    }


    /**
     * 步行
     *
     * @param walkRouteResult
     * @param i
     */
    @Override
    public void onWalkRouteSearched(WalkRouteResult walkRouteResult, int i) {
        dissmissProgressDialog();
        List<WalkPath> pathList = walkRouteResult.getPaths();
        List<LatLng> walkPaths = new ArrayList<>();
        String routeInfo = "步行线路长度：" + pathList.get(0).getDistance() + "\n";
        MainActivity.text.setText(routeInfo);
        for (WalkPath dp : pathList) {
            List<WalkStep> stepList = dp.getSteps();
            for (WalkStep ds : stepList) {
                List<LatLonPoint> points = ds.getPolyline();
                for (LatLonPoint llp : points) {
                    walkPaths.add(new LatLng(llp.getLatitude(), llp.getLongitude()));
                }
            }
        }
        int icon = R.drawable.ai;
        root_Line(walkPaths, icon);
        show_Marker();//显示标点
    }

    private void root_Line(List<LatLng> walkPaths, int icon) {
        MainActivity.aMap.clear(true);
        MainActivity.aMap.addPolyline(new PolylineOptions()
                .addAll(walkPaths)
                .width(40)
                //是否开启纹理贴图
                .setUseTexture(true)
                //绘制成大地线
                .geodesic(false)
                //设置纹理样式
                .setCustomTexture(BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(MainActivity.context.getResources()
                        , icon)))
                //设置画线的颜色
                .color(Color.argb(200, 0, 245, 255)));
    }

    @Override
    public void onRideRouteSearched(RideRouteResult rideRouteResult, int i) {
        dissmissProgressDialog();
        List<RidePath> pathList = rideRouteResult.getPaths();
        List<LatLng> walkPaths = new ArrayList<>();
        String routeInfo = "最快线路长度：" + pathList.get(0).getDistance() + "\n";
        MainActivity.text.setText(routeInfo);
        for (RidePath dp : pathList) {
            List<RideStep> stepList = dp.getSteps();
            for (RideStep ds : stepList) {
                List<LatLonPoint> points = ds.getPolyline();
                for (LatLonPoint llp : points) {
                    walkPaths.add(new LatLng(llp.getLatitude(), llp.getLongitude()));
                }
            }
        }
        int icon = R.drawable.route_drive_select;

        root_Line(walkPaths, icon);
        show_Marker();
    }

    /**
     * 显示进度框
     */
    public static void showProgressDialog() {
        if (progDialog == null)
            progDialog = new ProgressDialog(MainActivity.context);
        progDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progDialog.setIndeterminate(false);
        progDialog.setCancelable(true);
        progDialog.setMessage("正在搜索");
        progDialog.show();
    }

    /**
     * 隐藏进度框
     */
    public static void dissmissProgressDialog() {
        if (progDialog != null) {
            progDialog.dismiss();
        }
    }

}

