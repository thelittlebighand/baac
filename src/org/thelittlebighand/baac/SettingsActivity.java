package org.thelittlebighand.baac;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import org.bordylek.baac.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SettingsActivity extends PreferenceActivity {

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.layout.preferences);

		ListPreference pref = (ListPreference) findPreference("rulefiles");
		List<CharSequence> entries = new ArrayList<>(Arrays.asList(pref.getEntries()));
		List<CharSequence> values = new ArrayList<>(Arrays.asList(pref.getEntryValues()));
		entries.add("Custom 1");
		values.add("file://custom1.mrf.json");
		pref.setEntries(entries.toArray(new CharSequence[entries.size()]));
		pref.setEntryValues(values.toArray(new CharSequence[values.size()]));
	}

}
