package com.lyullin.grisha.magicball;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

public class SettingsActivity extends PreferenceActivity {

    private SharedPreferences settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        settings = getSharedPreferences(Constants.APP_PREFERENCES,
                SettingsActivity.MODE_PRIVATE);

        addPreferencesFromResource(R.xml.settings);

        Preference vibrationPref = findPreference("vibrationPref");
        Preference accelerometerSensPref = findPreference("accelerometerSensPref");
        Preference shakeCountPref = findPreference("shakeCountPref");

        vibrationPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                SharedPreferences.Editor editor = settings.edit();
                editor.putBoolean(Constants.VIBRATE_ON, (Boolean) newValue);
                editor.apply();
                return true;
            }
        });

        accelerometerSensPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                Float value = Float.valueOf(String.valueOf(newValue));
                if ((value <= 5.0) && (value > 0.0)) {
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putFloat(Constants.SHAKE_FORCE, value);
                    editor.apply();
                    return true;
                } else {
                    Toast.makeText(SettingsActivity.this, getString(R.string.accelerometer_error), Toast.LENGTH_LONG).show();
                    return false;
                }
            }
        });

        shakeCountPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                Integer value = Integer.valueOf(String.valueOf(newValue));
                if ((value <= 10) && (value >= 1)) {
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putInt(Constants.SHAKE_COUNT, value);
                    editor.apply();
                    return true;
                } else {
                    Toast.makeText(SettingsActivity.this, getString(R.string.shakeCount_error), Toast.LENGTH_LONG).show();
                    return false;
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_back) {
            super.onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}