package com.example.location1.user;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RatingBar;
import android.widget.Toast;

import com.example.location1.R;
import com.example.location1.fragment.Fragment1;
import com.example.location1.fragment.Fragment2;
import com.example.location1.fragment.Fragment3;
import com.example.location1.user.user;

public class user_Activity extends AppCompatActivity {
public static Context context;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_x);
        context=this;
        try {
            Intent intent = getIntent();
            FragmentManager fragmentManager = getSupportFragmentManager();
            //开启事务
            FragmentTransaction beginTransaction = fragmentManager.beginTransaction();
            if (intent.getExtras().getString("imv").equals("p")) {//评价
                final Fragment1 fragment = new Fragment1();
                beginTransaction.replace(android.R.id.content, fragment);
                beginTransaction.commit();
            }
            if (intent.getExtras().getString("imv").equals("u")) {//user资料
                Fragment2 fragment = new Fragment2();
                beginTransaction.replace(android.R.id.content, fragment);
                beginTransaction.commit();
            }
            if (intent.getExtras().getString("imv").equals("s")) {//设置
                Fragment3 fragment = new Fragment3();
                beginTransaction.replace(android.R.id.content, fragment);
                beginTransaction.commit();
            }
        } catch (Exception e) {
            Toast.makeText(this, "出现异常", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
}
