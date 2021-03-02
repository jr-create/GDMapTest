package com.example.location1.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.location1.R;

public class Fragment1 extends Fragment {
    private Button btn_sugg;
    private RatingBar rating_bar;
private TextView tv_fen;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_fragment1, null);
        btn_sugg = view.findViewById(R.id.btn_sugg);
        rating_bar = view.findViewById(R.id.rating_bar);//fragment1
        tv_fen = view.findViewById(R.id.tv_fen);
        rating_bar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                tv_fen.setText(Integer.parseInt(rating_bar.getProgress()+"")+"分");
//                Toast.makeText(getContext(), "评价" + rating_bar.getProgress() + "分", Toast.LENGTH_SHORT).show();
            }
        });
        btn_sugg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(), "提交成功", Toast.LENGTH_SHORT).show();
                getActivity().finish();
            }
        });
        return view;
    }
}
