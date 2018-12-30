package com.example.koba.testcanvas;

import android.os.Bundle;
import android.support.v7.preference.PreferenceFragmentCompat;

public class SettingPreferenceFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        getPreferenceManager().setSharedPreferencesName(SettingManager.SHARED_PREFERENCES_NAME);
        addPreferencesFromResource(R.xml.preference);
    }
}
