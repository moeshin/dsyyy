package site.littlehands.dsyyy;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.File;

import site.littlehands.app.DirectorySelectorDialog;

public class SettingFragment extends PreferenceFragment
        implements Preference.OnPreferenceChangeListener {

    private static final String TAG = "SettingFragment";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.setting);

        SharedPreferences sharedPreferences = getPreferenceManager().getSharedPreferences();

        String path = sharedPreferences.getString(SettingUtils.KEY_PATH, null);

        if (path == null) {
            path = Environment.getExternalStorageDirectory().getPath();

            SharedPreferences.Editor editor = getPreferenceScreen().getEditor();
            editor.putString(SettingUtils.KEY_PATH, path);
            editor.commit();
        }

        Preference preference;

        preference = findPreference(SettingUtils.KEY_PATH);
        preference.setSummary(path);
        preference.setOnPreferenceChangeListener(this);

        preference = findPreference(SettingUtils.KEY_FORMAT);
        preference.setOnPreferenceChangeListener(this);
        preference.setSummary(
                sharedPreferences.getString(SettingUtils.KEY_FORMAT, SettingUtils.FORMAT_DEFAULT)
        );

        preference = findPreference("version");
        preference.setSummary(BuildConfig.VERSION_NAME);

    }

    @Override
    public boolean onPreferenceTreeClick(
            final PreferenceScreen preferenceScreen,
            final Preference preference
    ) {
        if (SettingUtils.KEY_PATH.equals(preference.getKey())) {
            DirectorySelectorDialog.show(getActivity(),
                    new DirectorySelectorDialog.onSelectListener() {
                @Override
                public void onDirectorySelected(File directory) {
                    String path = directory.getPath();
                    preference.setSummary(path);

                    SharedPreferences.Editor editor = preferenceScreen.getEditor();
                    editor.putString(SettingUtils.KEY_PATH, path);
                    editor.commit();
                }
            });
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        preference.setSummary((String) newValue);
        return true;
    }

}
