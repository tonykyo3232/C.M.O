package com.example.cmo.Profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.cmo.Account.LoginActivity;
import com.example.cmo.Home.MainActivity;
import com.example.cmo.R;
import com.google.firebase.auth.FirebaseAuth;


public class SignOutFragment extends Fragment {

    private TextView tvSignOut;
    private FirebaseAuth mAuth;

    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_signout, container, false);
        tvSignOut = (TextView) view.findViewById(R.id.tvConfirmSignout);
        Button btnConfirmSignout = (Button) view.findViewById(R.id.btnConfirmSignout);
        mAuth = FirebaseAuth.getInstance();

        btnConfirmSignout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();
                Intent loginIntent = new Intent(getActivity(), LoginActivity.class);
                loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(loginIntent);
                getActivity().finish();
            }
        });
        return  view;
    }
}
