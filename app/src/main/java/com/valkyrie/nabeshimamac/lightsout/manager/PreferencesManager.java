package com.valkyrie.nabeshimamac.lightsout.manager;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.valkyrie.nabeshimamac.lightsout.model.ThemeColors;

/**
 * SharedPreferences(保存部分)のManager
 */
public class PreferencesManager {
    private static PreferencesManager instance;
    private SharedPreferences appPreferences;
    private SharedPreferences defaultPreferences;
    //ShardPreferencesの保存部分

    public static Intent createIntent(Context context) {
        return new Intent(context, PreferencesManager.class);
    }

    private PreferencesManager(Context context) {
        appPreferences = context.getSharedPreferences("LightsOuts", Context.MODE_PRIVATE);
        defaultPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static PreferencesManager getInstance(Context context) {
        if (instance == null) {
            instance = new PreferencesManager(context);
        }
        return instance;
    }

    public boolean isTutorialEnd() {
        return appPreferences.getBoolean("tutorial", false);
    }
    //２回目以降チュートリアル画面に行かないように

    public void checkTutorialEnd() {
        appPreferences.edit().putBoolean("tutorial", true).apply();
    }

    public int getClearCount(GameClientManager.Ranking ranking) {
        String key = getRankingKey(ranking);
        return appPreferences.getInt(key, 0);
    }

    public int addClearCount(GameClientManager.Ranking ranking) {
        int clearCount = getClearCount(ranking);
        clearCount++;
        String key = getRankingKey(ranking);
        appPreferences.edit().putInt(key, clearCount).apply();
        return clearCount;
    }

    public boolean isSound() {
        return defaultPreferences.getBoolean("sound", true);
    }
    //タップ音のミュートしているかどうかの保存部分

    public ThemeColors getThemeColor() {
        final String color = defaultPreferences.getString("color", null);
        if (TextUtils.isEmpty(color)) {
            return ThemeColors.PinkBlue;
            // インストール時何も入らないから、デフォルト（何も入ってない状態）をピンクブルーに設定
        } else {
            return ThemeColors.getThemeColor(color);
            // 他色を選択した時にこっちに入る
        }
    }

    private String getRankingKey(GameClientManager.Ranking ranking) {
        switch (ranking) {
            case Easy:
                return "easy_count";
            case Normal:
                return "normal_count";
            case Hard:
                return "hard_count";
            case Original:
                return "original_count";
            default:
                return null;
        }
    }
    // クリア数をカウントする保存場所
}
