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

public class JoinRoomFragment extends Fragment {

	private EditText roomID;

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.join_room, container, false);
		roomID = v.findViewById(R.id.room_id);
		return v;
	}

	public String getRoomID() {
		return roomID.getText().toString();
	}

}
