package com.example.cmo.Home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.example.cmo.R;

public class PostFragment extends Fragment {

    private static final String TAG = "PostFragment";


    public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.fragment_add_post, container, false);
        return view;
    }
}
