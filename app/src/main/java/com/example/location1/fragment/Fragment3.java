package com.example.location1.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.location1.MainActivity;
import com.example.location1.R;
import com.example.location1.user.user;

public class Fragment3 extends Fragment {
    public static Button  btn_gy, exit_user;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.layout_fragment3,null);
        btn_gy = view.findViewById(R.id.btn_gy);
        exit_user = view.findViewById(R.id.exit_user);
        btn_gy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(),"抱歉，暂时没有",Toast.LENGTH_SHORT).show();
                getActivity().finish();
            }
        });
        exit_user.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dialog();

            }
        });
        return view;
    }
    public void Dialog(){
        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setIcon(R.drawable.exit)//设置标题的图片
                .setTitle("是否销户")//设置对话框的标题
//                .setView(view)
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();//取消dialog
                        try {
                            Log.e("Fragment3", "onClick: "+user.REC);
                            //进行user判断是否登录
                            if(user.REC==1) {
                                user.exit(user.phonenum,user.passwd,0);
                                Log.e("Fragment3", "onClick: "+user.phonenum+user.passwd );
                            }else{
                                Toast.makeText(getContext(), "请先登录", Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();

                        }
                    }
                }).create();
        dialog.show();
    }
}