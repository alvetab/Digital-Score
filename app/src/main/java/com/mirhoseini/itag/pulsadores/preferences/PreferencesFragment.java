package com.mirhoseini.itag.pulsadores.preferences;

import android.app.Activity;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import com.mirhoseini.itag.pulsadores.Preferences;
import com.mirhoseini.itag.R;

/**
 * Created by sylvek on 18/05/2015.
 */
public class PreferencesFragment extends PreferenceFragment {

    private OnPreferencesListener presenter;

    public static PreferencesFragment instance()
    {
        final PreferencesFragment dashboardFragment = new PreferencesFragment();
        Bundle arguments = new Bundle();
        dashboardFragment.setArguments(arguments);
        dashboardFragment.setRetainInstance(true);
        return dashboardFragment;
    }

    public void setForegroundBackground(final boolean checked)
    {
        final CheckBoxPreference preference = (CheckBoxPreference) findPreference(Preferences.FOREGROUND);
        preference.setChecked(checked);
        preference.setEnabled(!checked);
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        this.addPreferencesFromResource(R.xml.global_preferences);
        findPreference(Preferences.FOREGROUND).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference)
            {
                final boolean checked = ((CheckBoxPreference) preference).isChecked();
                presenter.onForegroundChecked(checked);
                return true;
            }
        });
    }

    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
        if (activity instanceof OnPreferencesListener) {
            this.presenter = (OnPreferencesListener) activity;
        } else {
            throw new ClassCastException("must implement OnPreferencesListener");
        }
    }

    @Override
    public void onStart()
    {
        super.onStart();
        this.presenter.onPreferencesStarted();
    }

    @Override
    public void onStop()
    {
        super.onStop();
        this.presenter.onPreferencesStopped();
    }

    public interface OnPreferencesListener {

        void onPreferencesStarted();

        void onPreferencesStopped();

        void onForegroundChecked(boolean checked);
    }
}
