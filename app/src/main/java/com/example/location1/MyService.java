package com.example.location1;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.AMapOptions;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.services.core.LatLonPoint;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MyService extends Service {
    private  int count;;//充当服务的状态
    private boolean stop;;//确定是否停止count计数

    //定义onBinder方法返回的对象
    private MyBinder binder=new MyBinder();
    public class MyBinder extends Binder {
        public int getCount(){
            //获取Service的运行状态
            return count;
        }
    }

    public IBinder onBind(Intent intent) {
        Log.v("MyService", "onBind:绑定服务成功 ");

        return binder;
    }
    //Service被创建时回调
    public void onCreate(){
        super.onCreate();
        Log.v("MyService","服务创建成功");
        //启动一条线程，动态修改count的状态值
        new Thread(){
            public void run(){
                while(!stop){
                    try{
                        Thread.sleep(1000);
//                        Intent intent=new Intent(getApplicationContext(),MainActivity.class);
//                        startActivity(intent);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    count++;
                }
            }
        }.start();

    }
    public boolean onUnbind(Intent intent){
        Log.v("MyService","服务解除绑定");
        return true;
    }
    //Service被关闭时回调
    public void onDestroy(){
        super.onDestroy();
        this.stop=true;
        Log.v("MyService","服务解除");
    }




}
