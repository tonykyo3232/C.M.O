package com.example.cmo.Home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.example.cmo.R;

public class HomeFragment extends Fragment {
    private static final String TAG = "HomeFragment";

    public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
       View view = inflater.inflate(R.layout.activity_main, container, false);


       return view;
    }

}
