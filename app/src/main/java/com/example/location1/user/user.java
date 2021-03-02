package com.example.location1.user;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.location1.MainActivity;
import com.example.location1.R;

public class user extends AppCompatActivity {
    public static Boolean flag = false;
    private EditText phone_edit, paswd_edit;//用户密码
    private Button login_btn;//登录
    private Button btn_regist;//注册
    static String connectURL;
    public static int REC =0;
    public static String phonenum;
    public static String passwd;
    public static String[] res=new String[6];
    static Context context;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        context=this;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);
        init();
    }
    //更新个人资料功能
    public static void update(final String phonenum, final String name, final String age, final String sex, final String phone, int j) {
        if (j == 0) {
            connectURL = "http://192.168.0.105/user_update.php";
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    Http_Conn httpconn = new Http_Conn();

                    // 连接到服务器的地址
                    flag = httpconn.gotoupdate(phonenum,name,age,sex,phone, connectURL);
                    Log.e("成功码", "run: " + flag);
                    if (flag) {
                        //传入手机号用来在me_layout界面显示
                        //成功后显示消息
                        Looper.prepare();
                        Toast.makeText(context, "成功更新", Toast.LENGTH_SHORT).show();

                        Looper.loop();
                    } else {
                        Looper.prepare();
                        Toast.makeText(context, "失败，请重新开始", Toast.LENGTH_SHORT).show();
                        Looper.loop();
                    }
                }
            });
            thread.start();
        }
    }
    //销户功能
    public static void exit(String phone, String pass, int j) {
        if (j == 0) {
            connectURL = "http://192.168.0.105/user_delete.php";
            phonenum = phone;
            passwd = pass;
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    Http_Conn httpconn = new Http_Conn();

                    // 连接到服务器的地址
                    flag = httpconn.gotoConn(phonenum, passwd, connectURL);
                    Log.e("成功码", "run: " + flag);
                    if (flag) {
                        //传入手机号用来在me_layout界面显示
                        //成功后显示消息
                        Looper.prepare();
                        Toast.makeText(context, "成功销户", Toast.LENGTH_SHORT).show();
                        REC = 1;
                        Looper.loop();
                    } else {
                        Looper.prepare();
                        Toast.makeText(context, "失败，请重新开始", Toast.LENGTH_SHORT).show();
                        Looper.loop();
                    }
                }
            });
            thread.start();
        }
    }

    //组件初始化方法
    public void init() {
        phone_edit = findViewById(R.id.edit_user);
        paswd_edit = findViewById(R.id.edit_passwd);
        login_btn = findViewById(R.id.btn_login);
        btn_regist = findViewById(R.id.btn_regist);

        login_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                connectURL = "http://192.168.0.105/user_login.php";
                phonenum = phone_edit.getText().toString();
                passwd = paswd_edit.getText().toString();
                //启动线程
                Thread thread = new Thread(runnable);
                thread.start();
            }
        });
        btn_regist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connectURL = "http://192.168.0.105/user_register.php";
                phonenum = phone_edit.getText().toString();
                passwd = paswd_edit.getText().toString();
                Thread thread = new Thread(runnable);
                thread.start();
            }
        });

    }
    //启动一个新的线程用来登录进行耗时操作
  Runnable runnable = new Runnable() {
        @Override
        public void run() {
            Http_Conn httpconn = new Http_Conn();

            // 连接到服务器的地址
            flag = httpconn.gotoConn(phonenum, passwd, connectURL);
            Log.e("成功码", "run: " + flag);
            if (flag) {
                //传入手机号用来在me_layout界面显示
                //成功后显示消息
                Looper.prepare();
                Toast.makeText(getApplicationContext(), "成功", Toast.LENGTH_SHORT).show();
                REC = 1;
                finish_activity(phonenum);
                Looper.loop();

            } else {
                Looper.prepare();
                Toast.makeText(getApplicationContext(), "失败，请重新开始", Toast.LENGTH_SHORT).show();
                Looper.loop();
            }
        }
    };
    private void finish_activity(String phonenum) {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.putExtra("user", phonenum);
        intent.putExtra("age", res[2]);
        intent.putExtra("sex", res[3]);
        intent.putExtra("phone", res[4]);
//                startActivity(intent);
        // 这里的OtherActivity.this是为了得到OtherActivity类的对象，因为现在在button的内部类里面，this指向的是OnClickListener
        user.this.setResult(Activity.RESULT_OK, intent);
        user.this.finish(); // 结束自己
    }
}
