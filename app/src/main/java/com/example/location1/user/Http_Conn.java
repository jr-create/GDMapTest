package com.example.location1.user;

import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Http_Conn {
    //连接的方法
    public boolean gotoConn(String phonenum, String password, String connectUrl) {
        String result; // 用来取得返回的String
        boolean isLoginSucceed = false;
        HttpClient httpClient = new DefaultHttpClient();
        // 发送post请求
        HttpPost httpRequest = new HttpPost(connectUrl);
        // Post运作传送变数必须用NameValuePair[]阵列储存
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        //BasicNameValuePair存储键值对的类
        params.add(new BasicNameValuePair("phonenum", phonenum));
        params.add(new BasicNameValuePair("passwd", password));
        try {
            // 发出HTTP请求转为带参数的HTTP网络地址
            httpRequest.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
            // 取得HTTP response
            HttpResponse httpResponse = httpClient.execute(httpRequest);
            result = EntityUtils.toString(httpResponse.getEntity(), "GBK");
            if (connectUrl.matches("http://192.168.0.105/user_login.php")) {
                String res[] = result.split(" ");
                Log.e("TAG", "gotoConn: " + "a" + phonenum + password + connectUrl + result);
                Log.e("PHP返回结果", "gotoConn: " + res.length);
                Log.e("PHP返回结果", "gotoConn: " + " " + res[1] + " " + res[2] + " " + res[3] + res[4] + res[5]);
                user.res = res;
            }
            String result1 = result.replaceAll(" ", "");//去掉所有空格，包括首尾、中间
            result1 = result1.charAt(0) + "";
            Log.e("TAG", "gotoConn: " + "s" + result1);

            // 判断返回的数据是否为php中成功登入是时输出的success
            if (result1.equals("1")) {
                isLoginSucceed = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return isLoginSucceed;
    }

    /**
     * update
     *
     * @param phonenum
     * @param
     * @param connectUrl
     * @return
     */
    public boolean gotoupdate(String phonenum, String name, String age, String sex, String phone, String connectUrl) {
        String result; // 用来取得返回的String
        boolean isLoginSucceed = false;
        HttpClient httpClient = new DefaultHttpClient();
        // 发送post请求
        HttpPost httpRequest = new HttpPost(connectUrl);
        // Post运作传送变数必须用NameValuePair[]阵列储存
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        //BasicNameValuePair存储键值对的类
        params.add(new BasicNameValuePair("phonenum", phonenum));
        params.add(new BasicNameValuePair("name", name));
        params.add(new BasicNameValuePair("age", age));
        params.add(new BasicNameValuePair("sex", sex));
        params.add(new BasicNameValuePair("phone", phone));
        try {
            // 发出HTTP请求转为带参数的HTTP网络地址
            httpRequest.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
            // 取得HTTP response
            HttpResponse httpResponse = httpClient.execute(httpRequest);
            result = EntityUtils.toString(httpResponse.getEntity(), "GBK");

            Log.e("TAG", "gotoConn: " + "a" + phonenum + name + connectUrl + result);
            String result1 = result.replaceAll(" ", "");//去掉所有空格，包括首尾、中间

            Log.e("TAG", "gotoConn: " + "s" + result1);

            // 判断返回的数据是否为php中成功登入是时输出的success
            if (result1.equals("1")) {
                isLoginSucceed = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return isLoginSucceed;
    }
}
