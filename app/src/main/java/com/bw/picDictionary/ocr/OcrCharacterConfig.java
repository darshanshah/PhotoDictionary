
package com.bw.picDictionary.ocr;

import android.content.SharedPreferences;

public class OcrCharacterConfig {

    public static final String KEY_CHARACTER_BLACKLIST_ENGLISH = "preference_character_blacklist_english";



    public static final String KEY_CHARACTER_WHITELIST_ENGLISH = "preference_character_whitelist_english";


    private OcrCharacterConfig() {
    }

    public static String getDefaultBlacklist(String languageCode) {

        if (languageCode.equals("eng")) {
            return "";
        } // English
        else {
            throw new IllegalArgumentException();
        }
    }

    public static String getDefaultWhitelist(String languageCode) {
        if (languageCode.equals("eng")) {
            return "!?@#$%()<>_-+=/.,:;'\"ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        } // English
        else {
            throw new IllegalArgumentException();
        }
    }

    public static String getBlacklist(SharedPreferences prefs, String languageCode) {
        if (languageCode.equals("eng")) {
            return prefs.getString(KEY_CHARACTER_BLACKLIST_ENGLISH, getDefaultBlacklist(languageCode));
        } else {
            throw new IllegalArgumentException();
        }
    }

    public static String getWhitelist(SharedPreferences prefs, String languageCode) {
        if (languageCode.equals("eng")) {
            return prefs.getString(KEY_CHARACTER_WHITELIST_ENGLISH, getDefaultWhitelist(languageCode));
        } else {
            throw new IllegalArgumentException();
        }
    }

    public static void setBlacklist(SharedPreferences prefs, String languageCode, String blacklist) {
        if (languageCode.equals("eng")) {
            prefs.edit().putString(KEY_CHARACTER_BLACKLIST_ENGLISH, blacklist).commit();
        }  else {
            throw new IllegalArgumentException();
        }
    }

    public static void setWhitelist(SharedPreferences prefs, String languageCode, String whitelist) {
        if (languageCode.equals("eng")) {
            prefs.edit().putString(KEY_CHARACTER_WHITELIST_ENGLISH, whitelist).commit();
        } else {
            throw new IllegalArgumentException();
        }
    }
}
