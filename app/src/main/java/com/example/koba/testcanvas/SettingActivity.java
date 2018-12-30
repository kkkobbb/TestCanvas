package com.example.koba.testcanvas;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceScreen;

public class SettingActivity extends AppCompatActivity implements PreferenceFragmentCompat.OnPreferenceStartScreenCallback {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
    }

    @Override
    public boolean onPreferenceStartScreen(PreferenceFragmentCompat preferenceFragmentCompat, PreferenceScreen preferenceScreen) {
        // Fragmentの切り替えと、addToBackStackで戻るボタンを押した時に前のFragmentに戻るようにする
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.setting_preferenceFragment, new SettingPreferenceFragment())
                .addToBackStack(null)
                .commit();
        return true;
    }
}
