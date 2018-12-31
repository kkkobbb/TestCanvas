package com.example.koba.testcanvas;

import android.content.Context;
import android.content.SharedPreferences;

class SettingManager {
    static final String SHARED_PREFERENCES_NAME = "settings";

    /**
     * 設定の取得 （起動時に前回の状態を復元する）
     * @param context コンテキスト
     * @return 設定値
     */
    static boolean getStartActionLoad(Context context) {
        final SharedPreferences sp = context.getSharedPreferences(
                SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        final String key = context.getString(R.string.setting_key_st_load);
        // (取得できなかった場合、xmlのデフォルト値に関係なくfalseを返す)
        return sp.getBoolean(key, false);
    }

    /**
     * 設定の取得 (「移動」時、対象の図形を強調する)
     * @param context コンテキスト
     * @return 設定値
     */
    static boolean getShapeAppearanceTransfer(Context context) {
        final SharedPreferences sp = context.getSharedPreferences(
                SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        final String key = context.getString(R.string.setting_key_sa_transfer);
        // (取得できなかった場合、xmlのデフォルト値に関係なくfalseを返す)
        return sp.getBoolean(key, false);
    }

    /**
     * 設定の取得 (「戻る」した図形も表示する)
     * @param context コンテキスト
     * @return 設定値
     */
    static boolean getShapeAppearanceUndo(Context context) {
        final SharedPreferences sp = context.getSharedPreferences(
                SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        final String key = context.getString(R.string.setting_key_sa_undo);
        // (取得できなかった場合、xmlのデフォルト値に関係なくfalseを返す)
        return sp.getBoolean(key, false);
    }
}
