package com.android.example.sunshine.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;

import com.android.example.sunshine.R;

/**
 * A {@link PreferenceActivity} that presents a set of application settings.
 * <p/>
 * See <a href="http://developer.android.com/design/patterns/settings.html"> Android Design: Settings</a> for design
 * guidelines and the <a href="http://developer.android.com/guide/topics/ui/settings.html">Settings API Guide</a> for
 * more information on developing a Settings UI.
 */
public class SettingsActivity extends PreferenceActivity implements Preference.OnPreferenceChangeListener {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.pref_general);

		bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_key_location)));
		bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_key_units)));
	}

	/**
	 * Attaches a listener so the summary is always updated with the preference value. Also fires the listener once, to
	 * initialize the summary (so it shows up before the value is changed.)
	 */
	private void bindPreferenceSummaryToValue(Preference preference) {
		preference.setOnPreferenceChangeListener(this);

		final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(preference.getContext());
		onPreferenceChange(preference, preferences.getString(preference.getKey(), ""));
	}

	@Override
	public boolean onPreferenceChange(Preference preference, Object value) {
		String stringValue = value.toString();

		if (preference instanceof ListPreference) {
			// For list preferences, look up the correct display value in
			// the preference's 'entries' list (since they have separate labels/values).
			ListPreference listPreference = (ListPreference) preference;
			int prefIndex = listPreference.findIndexOfValue(stringValue);
			if (prefIndex >= 0) {
				preference.setSummary(listPreference.getEntries()[prefIndex]);
			}
		}
		else {
			// For other preferences, set the summary to the value's simple string representation.
			preference.setSummary(stringValue);
		}
		return true;
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);


	}

	@Nullable
	@Override
	public Intent getParentActivityIntent() {
		Intent parentActivityIntent = super.getParentActivityIntent();
		if (parentActivityIntent != null) {
			parentActivityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		}
		return parentActivityIntent;
	}
}