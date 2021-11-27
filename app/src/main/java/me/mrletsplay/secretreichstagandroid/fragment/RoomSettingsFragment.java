package me.mrletsplay.secretreichstagandroid.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.Arrays;

import me.mrletsplay.secretreichstagandroid.ExtendedRoomSettings;
import me.mrletsplay.secretreichstagandroid.R;
import me.mrletsplay.srweb.game.GameMode;
import me.mrletsplay.srweb.game.RoomSettings;

public class RoomSettingsFragment extends Fragment {

	private ExtendedRoomSettings roomSettings;
	private GameMode selectedGameMode;
	private EditText roomName;

	public RoomSettingsFragment(ExtendedRoomSettings roomSettings) {
		this.roomSettings = roomSettings;
	}

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

	public boolean applySettings(boolean strict) {
		String name = roomName.getText().toString().trim();

		if(name.isEmpty()) {
			if(strict) {
				Toast.makeText(getContext(), "You need to enter a room name", Toast.LENGTH_LONG).show();
				return false;
			}
		}else {
			roomSettings.setRoomName(name);
		}

		RoomSettings r = roomSettings.getRoomSettings();
		r.setMode(selectedGameMode.name());
		if(r.getLiberalCardCount() == 0) r.setLiberalCardCount(selectedGameMode == GameMode.SECRET_REICHSTAG ? 9 : 6);
		if(r.getFascistCardCount() == 0) r.setFascistCardCount(11);
		if(r.getCommunistCardCount() == 0) r.setCommunistCardCount(11);
		return true;
	}

}
