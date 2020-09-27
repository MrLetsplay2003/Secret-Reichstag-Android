package me.mrletsplay.secretreichstagandroid.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import me.mrletsplay.secretreichstagandroid.R;

public class SelectUsernameFragment extends Fragment {

	private EditText username;

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.select_username, container, false);
		username = v.findViewById(R.id.username);
		return v;
	}

	public String getUsername() {
		return username.getText().toString();
	}

}
