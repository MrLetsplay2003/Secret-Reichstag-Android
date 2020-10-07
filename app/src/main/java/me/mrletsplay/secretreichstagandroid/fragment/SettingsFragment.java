package me.mrletsplay.secretreichstagandroid.fragment;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.preference.PreferenceFragmentCompat;

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
		//getListView().setBackgroundColor(Color.WHITE);
		//getPreferenceScreen().getContext().setTheme(R.style.SettingsTheme);
	}

}
