package com.example.cmo.Profile;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.content.Context;

import com.example.cmo.R;
import com.twitter.sdk.android.core.models.TwitterCollection;

import java.util.ArrayList;

public class AccountSettingsActivity extends AppCompatActivity{


    private Context mContext = AccountSettingsActivity.this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(AccountSettingsActivity.class.getSimpleName(), "AccountSettingsActivity-onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accountsettings);
        setupSettingList();

        ImageView backArrow = (ImageView) findViewById(R.id.backArrow);
        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void setupSettingList(){
        ListView listView = (ListView) findViewById(R.id.lvAccountSettings);
        ArrayList<String> options = new ArrayList<>();
        options.add("Edit Profile");
        options.add("Sign Out");

        ArrayAdapter adapter = new ArrayAdapter(mContext, android.R.layout.simple_list_item_1, options);
        listView.setAdapter(adapter);
    }
}
