package com.example.location1.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.location1.MainActivity;
import com.example.location1.R;
import com.example.location1.user.user;

public class Fragment2 extends Fragment {
    private Button  btn_ok ;
    private EditText ed1,ed2, ed3;//fragment2
    private RadioGroup group;//fragment2
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view=inflater.inflate(R.layout.layout_fragment2,null);
        btn_ok =view.findViewById(R.id.btn_ok);
        group = view.findViewById(R.id.group);
        final RadioButton rb = view.findViewById(group.getCheckedRadioButtonId());
        ed1 =  view.findViewById(R.id.ed1);
        ed2 = view.findViewById(R.id.ed2);
        ed3 = view.findViewById(R.id.ed3);
        ed1.setText(user.res[5]);
        ed2.setText(MainActivity.tv_age.getText().toString());
        ed3.setText(MainActivity.tv_phone.getText().toString());
        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(user.REC==1) {
                    user.update(user.phonenum,ed1.getText().toString(),ed2.getText().toString(),rb.getText().toString(),ed3.getText().toString(),0);
                    Log.e("Fragment3", "onClick: "+user.phonenum+ed3.getText().toString());
                }else{
                    Toast.makeText(getContext(), "请先登录", Toast.LENGTH_SHORT).show();
                }
                Intent intent = new Intent(getContext(), MainActivity.class);
                intent.putExtra("sex", rb.getText().toString());
                intent.putExtra("age", ed2.getText().toString());
                intent.putExtra("phone", ed3.getText().toString());
                getActivity().setResult(Activity.RESULT_OK, intent);
                getActivity().finish(); // 结束自己
            }
        });
        return view;
    }
}
