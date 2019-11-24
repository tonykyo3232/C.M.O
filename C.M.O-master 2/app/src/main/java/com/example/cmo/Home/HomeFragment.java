package com.example.cmo.Home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.support.v4.app.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.cmo.R;

public class HomeFragment extends Fragment {
    private static final String TAG = "HomeFragment";

    public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
       View view = inflater.inflate(R.layout.fragment_home, container, false);
       return view;
    }

}
