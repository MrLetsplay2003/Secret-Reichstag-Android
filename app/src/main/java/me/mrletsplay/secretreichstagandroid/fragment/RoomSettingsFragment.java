package me.mrletsplay.secretreichstagandroid.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.Arrays;

import me.mrletsplay.secretreichstagandroid.R;
import me.mrletsplay.srweb.game.GameMode;

public class RoomSettingsFragment extends Fragment {

	private GameMode selectedGameMode;
	private EditText
			roomName;

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		if(container != null) container.removeAllViews();
		View v = inflater.inflate(R.layout.room_settings, container, false);

		Spinner gameMode = v.findViewById(R.id.game_mode);
		GameMode[] gameModes = {GameMode.SECRET_HITLER, GameMode.SECRET_REICHSTAG};
		gameMode.setAdapter(new ArrayAdapter<>(getContext(), R.layout.spinner_item, Arrays.asList("Secret Hitler", "Secret Reichstag")));
		gameMode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				selectedGameMode = gameModes[position];
				System.out.println(selectedGameMode);
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) { }
		});
		gameMode.setSelection(0);

		roomName = v.findViewById(R.id.room_name);

		return v;
	}

	public GameMode getSelectedGameMode() {
		return selectedGameMode;
	}

	public String getRoomName() {
		return roomName.getText().toString();
	}

}
