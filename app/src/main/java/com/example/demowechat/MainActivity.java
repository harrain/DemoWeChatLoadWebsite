package com.example.demowechat;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    ConverFragment converf ;
    WebFragment webFragment;
    FragmentManager fm;
    FragmentTransaction fragmentTransaction;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.tb);
        setSupportActionBar(toolbar);

        converf = new ConverFragment();
        webFragment = new WebFragment();
         fm = getSupportFragmentManager();
         fragmentTransaction = fm.beginTransaction();

        fragmentTransaction.add(R.id.fl, converf);
        fragmentTransaction.show(converf);
        fragmentTransaction.commit();


    }

    public void front(View v){
        fragmentTransaction = fm.beginTransaction();
        fragmentTransaction.replace(R.id.fl,converf);
        fragmentTransaction.show(converf);
        fragmentTransaction.commit();
    }

    public void search(View v){
        fragmentTransaction = fm.beginTransaction();
        fragmentTransaction.replace(R.id.fl,webFragment);
        fragmentTransaction.show(webFragment);
        fragmentTransaction.commit();
    }
}
