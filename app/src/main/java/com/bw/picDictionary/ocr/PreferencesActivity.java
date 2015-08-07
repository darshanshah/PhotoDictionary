package com.bw.picDictionary.ocr;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

import com.bw.picDictionary.ocr.lang.LanguageSelector;

/**
 * Class to handle preferences that are saved across sessions of the app. Shows
 * a hierarchy of preferences to the user, organized into sections. These
 * preferences are displayed in the options menu that is shown when the user
 * presses the MENU button.
 * <p/>
 * The code for this class was adapted from the ZXing project: http://code.google.com/p/zxing
 */
public class PreferencesActivity extends PreferenceActivity implements
        OnSharedPreferenceChangeListener {

    // Preference keys not carried over from ZXing project
    public static final String KEY_SOURCE_LANGUAGE_PREFERENCE = "sourceLanguageCodeOcrPref";
    public static final String KEY_TARGET_LANGUAGE_PREFERENCE = "targetLanguageCodeTranslationPref";
    public static final String KEY_TOGGLE_TRANSLATION = "preference_translation_toggle_translation";
    //public static final String KEY_CONTINUOUS_PREVIEW = "preference_capture_continuous";
    public static final String KEY_DICTIONARY_MODE = "dict_mode";
    public static final String KEY_PAGE_SEGMENTATION_MODE = "preference_page_segmentation_mode";
    public static final String KEY_OCR_ENGINE_MODE = "preference_ocr_engine_mode";
    public static final String KEY_CHARACTER_BLACKLIST = "preference_character_blacklist";
    public static final String KEY_CHARACTER_WHITELIST = "preference_character_whitelist";
    public static final String KEY_TOGGLE_LIGHT = "preference_toggle_light";
    public static final String KEY_TRANSLATOR = "preference_translator";

    // Preference keys carried over from ZXing project
    public static final String KEY_HELP_VERSION_SHOWN = "preferences_help_version_shown";
    public static final String KEY_NOT_OUR_RESULTS_SHOWN = "preferences_not_our_results_shown";
    public static final String KEY_REVERSE_IMAGE = "preferences_reverse_image";
    public static final String KEY_PLAY_BEEP = "preferences_play_beep";
    public static final String KEY_VIBRATE = "preferences_vibrate";

    public static final String TRANSLATOR_BING = "Bing Translator";
    public static final String TRANSLATOR_GOOGLE = "Google Translate";
    private static SharedPreferences sharedPreferences;
    //private ListPreference listPreferenceContinuousPreview;
    private ListPreference listPreferenceDictionaryMode;
    private ListPreference listPreferenceSourceLanguage;
    //private CheckBoxPreference checkBoxTranslate;
    private ListPreference listPreferenceTargetLanguage;
    private ListPreference listPreferenceTranslator;
    private ListPreference listPreferenceOcrEngineMode;
    //private CheckBoxPreference checkBoxBeep;
    private EditTextPreference editTextPreferenceCharacterBlacklist;
    private EditTextPreference editTextPreferenceCharacterWhitelist;
    //private CheckBoxPreference checkBoxReversedImage;
    private ListPreference listPreferencePageSegmentationMode;

    /**
     * Set the default preference values.
     *
     * @param Bundle savedInstanceState the current Activity's state, as passed by
     *               Android
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        listPreferenceDictionaryMode = (ListPreference) getPreferenceScreen().findPreference(KEY_DICTIONARY_MODE);
        listPreferenceSourceLanguage = (ListPreference) getPreferenceScreen().findPreference(KEY_SOURCE_LANGUAGE_PREFERENCE);
        //checkBoxTranslate = (CheckBoxPreference) getPreferenceScreen().findPreference(KEY_TOGGLE_TRANSLATION);
        listPreferenceTargetLanguage = (ListPreference) getPreferenceScreen().findPreference(KEY_TARGET_LANGUAGE_PREFERENCE);
        listPreferenceTranslator = (ListPreference) getPreferenceScreen().findPreference(KEY_TRANSLATOR);
        listPreferenceOcrEngineMode = (ListPreference) getPreferenceScreen().findPreference(KEY_OCR_ENGINE_MODE);
        //checkBoxBeep = (CheckBoxPreference) getPreferenceScreen().findPreference(KEY_PLAY_BEEP);
        editTextPreferenceCharacterBlacklist = (EditTextPreference) getPreferenceScreen().findPreference(KEY_CHARACTER_BLACKLIST);
        editTextPreferenceCharacterWhitelist = (EditTextPreference) getPreferenceScreen().findPreference(KEY_CHARACTER_WHITELIST);
        listPreferencePageSegmentationMode = (ListPreference) getPreferenceScreen().findPreference(KEY_PAGE_SEGMENTATION_MODE);
        //checkBoxReversedImage = (CheckBoxPreference) getPreferenceScreen().findPreference(KEY_REVERSE_IMAGE);

        // Create the entries/entryvalues for the translation target language list.
        //initTranslationTargetList();

    }

    /**
     * Interface definition for a callback to be invoked when a shared
     * preference is changed. Sets summary text for the app's preferences. Summary text values show the
     * current settings for the values.
     *
     * @param sharedPreferences the Android.content.SharedPreferences that received the change
     * @param key               the key of the preference that was changed, added, or removed
     */

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        // Update preference summary values to show current preferences
        if (key.equals(KEY_TRANSLATOR)) {
            listPreferenceTranslator.setSummary(sharedPreferences.getString(key, ImageCaptureActivity.DEFAULT_TRANSLATOR));
        } else if (key.equals(KEY_SOURCE_LANGUAGE_PREFERENCE)) {

            // Set the summary text for the source language name
            listPreferenceSourceLanguage.setSummary(LanguageSelector.getOcrLanguageName(getBaseContext(), sharedPreferences.getString(key, ImageCaptureActivity.DEFAULT_SOURCE_LANGUAGE_CODE)));

            // Retrieve the character blacklist/whitelist for the new language
            String blacklist = OcrCharacterConfig.getBlacklist(sharedPreferences, listPreferenceSourceLanguage.getValue());
            String whitelist = OcrCharacterConfig.getWhitelist(sharedPreferences, listPreferenceSourceLanguage.getValue());

            // Save the character blacklist/whitelist to preferences
            sharedPreferences.edit().putString(KEY_CHARACTER_BLACKLIST, blacklist).commit();
            sharedPreferences.edit().putString(KEY_CHARACTER_WHITELIST, whitelist).commit();

            // Set the blacklist/whitelist summary text
            editTextPreferenceCharacterBlacklist.setSummary(blacklist);
            editTextPreferenceCharacterWhitelist.setSummary(whitelist);

        } else if (key.equals(KEY_TARGET_LANGUAGE_PREFERENCE)) {
            listPreferenceTargetLanguage.setSummary(LanguageSelector.getTranslationLanguageName(this, sharedPreferences.getString(key, ImageCaptureActivity.DEFAULT_TARGET_LANGUAGE_CODE)));
        } else if (key.equals(KEY_PAGE_SEGMENTATION_MODE)) {
            listPreferencePageSegmentationMode.setSummary(sharedPreferences.getString(key, ImageCaptureActivity.DEFAULT_PAGE_SEGMENTATION_MODE));
        } else if (key.equals(KEY_OCR_ENGINE_MODE)) {
            listPreferenceOcrEngineMode.setSummary(sharedPreferences.getString(key, ImageCaptureActivity.DEFAULT_OCR_ENGINE_MODE));
        } else if (key.equals(KEY_CHARACTER_BLACKLIST)) {

            // Save a separate, language-specific character blacklist for this language
            OcrCharacterConfig.setBlacklist(sharedPreferences,
                    listPreferenceSourceLanguage.getValue(),
                    sharedPreferences.getString(key, OcrCharacterConfig.getDefaultBlacklist(listPreferenceSourceLanguage.getValue())));

            // Set the summary text
            editTextPreferenceCharacterBlacklist.setSummary(sharedPreferences.getString(key, OcrCharacterConfig.getDefaultBlacklist(listPreferenceSourceLanguage.getValue())));

        } else if (key.equals(KEY_CHARACTER_WHITELIST)) {

            // Save a separate, language-specific character blacklist for this language
            OcrCharacterConfig.setWhitelist(sharedPreferences,
                    listPreferenceSourceLanguage.getValue(),
                    sharedPreferences.getString(key, OcrCharacterConfig.getDefaultWhitelist(listPreferenceSourceLanguage.getValue())));

            // Set the summary text
            editTextPreferenceCharacterWhitelist.setSummary(sharedPreferences.getString(key, OcrCharacterConfig.getDefaultWhitelist(listPreferenceSourceLanguage.getValue())));

        } else if (key.equals(KEY_DICTIONARY_MODE)) {
            listPreferenceDictionaryMode.setSummary(sharedPreferences.getString(key, ImageCaptureActivity.DEFAULT_DICTIONARY_MODE));
        }

        // Update the languages available for translation based on the current translator selected.
        //if (key.equals(KEY_TRANSLATOR)) {
        // initTranslationTargetList();
        // }

    }

    /**
     * Sets up initial preference summary text
     * values and registers the OnSharedPreferenceChangeListener.
     */
    @Override
    protected void onResume() {
        super.onResume();

        listPreferenceDictionaryMode.setSummary(sharedPreferences.getString(KEY_DICTIONARY_MODE, ImageCaptureActivity.DEFAULT_DICTIONARY_MODE));
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    /**
     * Called when Activity is about to lose focus. Unregisters the
     * OnSharedPreferenceChangeListener.
     */
    @Override
    protected void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }
}