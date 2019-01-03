package com.example.koba.testcanvas;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * 設定値取得用
 */
class SettingManager {
    static final String SHARED_PREFERENCES_NAME = "settings";

    /**
     * 設定の取得 （起動時に前回の状態を復元する）
     * @param context コンテキスト
     * @return 設定値
     */
    static boolean getStartActionLoad(Context context) {
        final String key = context.getString(R.string.setting_key_st_load);
        return getBoolean(context, key);
    }

    /**
     * 設定の取得 (「移動」時、対象の図形を強調する)
     * @param context コンテキスト
     * @return 設定値
     */
    static boolean getShapeAppearanceTransfer(Context context) {
        final String key = context.getString(R.string.setting_key_sa_transfer);
        return getBoolean(context, key);
    }

    /**
     * 設定の取得 (「戻る」した図形も表示する)
     * @param context コンテキスト
     * @return 設定値
     */
    static boolean getShapeAppearanceUndo(Context context) {
        final String key = context.getString(R.string.setting_key_sa_undo);
        return getBoolean(context, key);
    }

    /**
     * 設定の取得 (新規として読み込む)
     * @param context コンテキスト
     * @return 設定値
     */
    static boolean getLoadSvgClean(Context context) {
        final String key = context.getString(R.string.setting_key_ls_clean);
        return getBoolean(context, key);
    }

    /**
     * 指定されたキーの値を返す (真偽値用)
     * @param context コンテキスト
     * @param key 値を取得するキー
     * @return 設定値 (取得に失敗した場合、false)
     */
    private static boolean getBoolean(Context context, String key) {
        final SharedPreferences sp = context.getSharedPreferences(
                SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        // (取得できなかった場合、xmlのデフォルト値に関係なくfalseを返す)
        return sp.getBoolean(key, false);
    }
}
