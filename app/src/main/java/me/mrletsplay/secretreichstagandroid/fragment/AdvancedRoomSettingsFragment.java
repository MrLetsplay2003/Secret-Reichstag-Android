package me.mrletsplay.secretreichstagandroid.fragment;

import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import me.mrletsplay.secretreichstagandroid.ExtendedRoomSettings;
import me.mrletsplay.secretreichstagandroid.R;
import me.mrletsplay.secretreichstagandroid.RoomSettingsDefaults;
import me.mrletsplay.secretreichstagandroid.ui.ActionAdapter;
import me.mrletsplay.srweb.game.GameMode;
import me.mrletsplay.srweb.game.RoomSettings;
import me.mrletsplay.srweb.game.state.GameParty;

public class AdvancedRoomSettingsFragment extends Fragment {

	private ExtendedRoomSettings roomSettings;
	private EditText liberalCards;
	private EditText fascistCards;
	private EditText communistCards;
	private ActionAdapter actionAdapter;

	public AdvancedRoomSettingsFragment(ExtendedRoomSettings roomSettings) {
		this.roomSettings = roomSettings;
	}

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		if(container != null) container.removeAllViews();
		View v = inflater.inflate(R.layout.advanced_room_settings, container, false);

		liberalCards = initCards(v, R.id.liberal_cards, GameParty.LIBERAL);
		fascistCards = initCards(v, R.id.fascist_cards, GameParty.FASCIST);
		communistCards = initCards(v, R.id.communist_cards, GameParty.COMMUNIST);

		actionAdapter = new ActionAdapter(getContext(), roomSettings.getRoomSettings());
		Button b = v.findViewById(R.id.advanced_customize_actions);
		b.setOnClickListener(bt -> {
			ExpandableListView elv = (ExpandableListView) inflater.inflate(R.layout.advanced_settings_actions, container, false);
			elv.setAdapter(actionAdapter);

			new AlertDialog.Builder(getContext())
					.setView(elv)
					.setPositiveButton(R.string.okay, null)
					.show();
		});

		Button b2 = v.findViewById(R.id.advanced_load_defaults);
		b2.setOnClickListener(bt -> {
			ListView lv = new ListView(getContext());
			Map<String, RoomSettings> presets = RoomSettingsDefaults.getDefaults(GameMode.valueOf(roomSettings.getRoomSettings().getMode()));
			List<String> pr = new ArrayList<>(presets.keySet());
			ArrayAdapter<String> ad = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, pr);
			lv.setAdapter(ad);

			AlertDialog d = new AlertDialog.Builder(getContext())
					.setView(lv)
					.setNegativeButton(R.string.cancel, null)
					.create();

			lv.setOnItemClickListener((parent, view, position, id) -> {
				String preset = pr.get(position);
				RoomSettings p = presets.get(preset);
				load(p);
				d.dismiss();
			});

			d.show();
		});

		return v;
	}

	private void load(RoomSettings settings) {
		actionAdapter.load(settings);
		liberalCards.setText(String.valueOf(settings.getLiberalCardCount()));
		fascistCards.setText(String.valueOf(settings.getFascistCardCount()));
		communistCards.setText(String.valueOf(settings.getCommunistCardCount()));
	}

	private EditText initCards(View v, int resID, GameParty party) {
		LinearLayout ll = v.findViewById(resID);
		TextView tv = ll.findViewById(R.id.advanced_card_label);
		tv.setText(getResources().getString(R.string.advanced_cards_label, party.getFriendlyNameSingular()));
		EditText et = ll.findViewById(R.id.advanced_card_input);
		et.setInputType(InputType.TYPE_CLASS_NUMBER);

		RoomSettings settings = roomSettings.getRoomSettings();
		int count = 5;
		switch(party) {
			case LIBERAL:
				count = settings.getLiberalCardCount();
				if(count == 0) count = settings.getMode().equals("SECRET_REICHSTAG") ? 9 : 6;
				break;
			case FASCIST:
				count = settings.getFascistCardCount();
				if(count == 0) count = 11;
				break;
			case COMMUNIST:
				count = settings.getCommunistCardCount();
				if(count == 0) count = 11;
				break;
		}
		et.setText(String.valueOf(count));

		if(party == GameParty.COMMUNIST && !roomSettings.getRoomSettings().getMode().equals("SECRET_REICHSTAG")) {
			ll.setVisibility(View.GONE);
		}else {
			ll.setVisibility(View.VISIBLE);
		}

		Button inc = ll.findViewById(R.id.advanced_card_increase);
		inc.setOnClickListener(b -> {
			try {
				int val = Integer.parseInt(et.getText().toString());
				et.setText(String.valueOf(Math.min(val + 1, 15)));
			}catch(Exception ignored) {}
		});
		Button dec = ll.findViewById(R.id.advanced_card_decrease);
		dec.setOnClickListener(b -> {
			try {
				int val = Integer.parseInt(et.getText().toString());
				et.setText(String.valueOf(Math.max(val - 1, 5)));
			}catch(Exception ignored) {}
		});

		return et;
	}

	private int getCardCount(EditText et) {
		try {
			int val = Integer.parseInt(et.getText().toString());
			System.out.println(et + "/" + val);
			if(val > 15 || val < 5) {
				Toast.makeText(getContext(), "The card count needs to be between 5 and 15", Toast.LENGTH_SHORT).show();
				return -1;
			}
			return val;
		}catch(Exception ignored) {
			Toast.makeText(getContext(), "The card count needs to be between 5 and 15", Toast.LENGTH_SHORT).show();
			return -1;
		}
	}

	public boolean applySettings() {
		int lc = getCardCount(liberalCards);
		if(lc == -1) return false;

		int fc = getCardCount(fascistCards);
		if(fc == -1) return false;

		int cc = 0;
		if(roomSettings.getRoomSettings().getMode().equals("SECRET_REICHSTAG")) {
			cc = getCardCount(communistCards);
			if (cc == -1) return false;
		}

		roomSettings.getRoomSettings().setLiberalCardCount(lc);
		roomSettings.getRoomSettings().setFascistCardCount(fc);
		roomSettings.getRoomSettings().setCommunistCardCount(cc);
		roomSettings.getRoomSettings().setLiberalBoard(actionAdapter.collectActions(GameParty.LIBERAL));
		roomSettings.getRoomSettings().setFascistBoard(actionAdapter.collectActions(GameParty.FASCIST));
		roomSettings.getRoomSettings().setCommunistBoard(actionAdapter.collectActions(GameParty.COMMUNIST));
		return true;
	}

}
