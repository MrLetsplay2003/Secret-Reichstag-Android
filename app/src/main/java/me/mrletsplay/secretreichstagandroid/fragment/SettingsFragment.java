package me.mrletsplay.secretreichstagandroid.fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import me.mrletsplay.secretreichstagandroid.R;

public class SettingsFragment extends PreferenceFragmentCompat {

	@Override
	public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
		setPreferencesFromResource(R.xml.settings, rootKey);
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		getListView().setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.reichstag, null));
		// getListView().setBackgroundColor(Color.parseColor("#dbd7d7"));
	}

	@Override
	public boolean onPreferenceTreeClick(Preference preference) {
		if(preference.getKey() != null && preference.getKey().equals("server")) {
			ListPreference lp = (ListPreference) preference;
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
			List<String> options = new ArrayList<>();
			options.add("Official Server");
			try {
				JSONArray arr = new JSONArray(prefs.getString("servers", "[]"));
				for(int i = 0; i < arr.length(); i++) {
					JSONObject o = arr.getJSONObject(i);
					options.add(o.getString("name"));
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
			lp.setEntries(options.toArray(new String[0]));
			lp.setEntryValues(options.toArray(new String[0]));
		}
		return super.onPreferenceTreeClick(preference);
	}

}
