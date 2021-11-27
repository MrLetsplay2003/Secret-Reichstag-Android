package me.mrletsplay.secretreichstagandroid.ui;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.mrletsplay.secretreichstagandroid.MainActivity;
import me.mrletsplay.secretreichstagandroid.R;
import me.mrletsplay.srweb.game.GameMode;
import me.mrletsplay.srweb.game.RoomSettings;
import me.mrletsplay.srweb.game.state.GameParty;
import me.mrletsplay.srweb.game.state.board.GameBoardAction;
import me.mrletsplay.srweb.game.state.board.GameBoardActionField;

public class ActionAdapter extends BaseExpandableListAdapter {

	private Context context;
	private List<GameParty> actionTitles;
	private Map<GameParty, Boolean> actionsEnabled;
	private Map<GameParty, List<GameBoardAction>> actions;
	private GameMode mode;

	public ActionAdapter(Context context, RoomSettings settings) {
		this.context = context;
		this.mode = GameMode.valueOf(settings.getMode());

		load(settings);
	}

	public void load(RoomSettings settings) {
		this.actionTitles = new ArrayList<>();
		this.actions = new HashMap<>();
		this.actionsEnabled = new HashMap<>();
		for(GameParty party : GameParty.values()) {
			if(party == GameParty.COMMUNIST && mode != GameMode.SECRET_REICHSTAG) continue;

			List<GameBoardActionField> setActions = null;
			switch(party) {
				case LIBERAL:
					setActions = settings.getLiberalBoard();
					break;
				case FASCIST:
					setActions = settings.getFascistBoard();
					break;
				case COMMUNIST:
					setActions = settings.getCommunistBoard();
					break;
			}

			actionsEnabled.put(party, false);
			actionTitles.add(party);
			int c = party == GameParty.LIBERAL ? 4 : 5;
			List<GameBoardAction> fs = new ArrayList<>();
			for(int i = 0; i < c; i++) {
				fs.add(null);
			}

			if(setActions != null) {
				actionsEnabled.put(party, true);
				for(GameBoardActionField f : setActions) fs.set(f.getFieldIndex(), f.getAction());
			}

			actions.put(party, fs);
		}
	}

	public List<GameBoardActionField> collectActions(GameParty party) {
		List<GameBoardActionField> fields = new ArrayList<>();
		if(!actionsEnabled.containsKey(party) || !actionsEnabled.get(party)) return null;
		List<GameBoardAction> unfiltered = actions.get(party);
		int idx = 0;
		for(GameBoardAction a : unfiltered) {
			if(a == null) continue;
			GameBoardActionField f = new GameBoardActionField();
			f.setFieldIndex(idx);
			f.setAction(a);
			fields.add(f);
			idx++;
		}
		return fields;
	}

	@Override
	public int getGroupCount() {
		return actions.size();
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		return actions.get(actionTitles.get(groupPosition)).size() + 1;
	}

	@Override
	public Object getGroup(int groupPosition) {
		return actionTitles.get(groupPosition);
	}

	@Override
	public Object getChild(int groupPosition, int childPosition) {
		GameParty p = actionTitles.get(groupPosition);
		if(childPosition == 0) return actionsEnabled.get(p);
		return actions.get(p).get(childPosition - 1);
	}

	@Override
	public long getGroupId(int groupPosition) {
		return groupPosition;
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return childPosition;
	}

	@Override
	public boolean hasStableIds() {
		return false;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
		if (convertView == null) {
			LayoutInflater layoutInflater = MainActivity.getCurrentFragment().getLayoutInflater();
			convertView = layoutInflater.inflate(R.layout.advanced_settings_action_title, null);
		}

		TextView listTitleTextView = convertView.findViewById(R.id.advanced_settings_action_title_text);
		listTitleTextView.setTypeface(null, Typeface.BOLD);
		listTitleTextView.setText(actionTitles.get(groupPosition).getFriendlyName());
		return convertView;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
		GameParty p = actionTitles.get(groupPosition);

		if(childPosition == 0) {
			if(convertView == null) {
				LayoutInflater layoutInflater = MainActivity.getCurrentFragment().getLayoutInflater();
				convertView = layoutInflater.inflate(R.layout.advanced_settings_action_enable, null);
			}

			CheckBox cb = (CheckBox) convertView;
			cb.setOnCheckedChangeListener((buttonView, isChecked) -> {
				actionsEnabled.put(p, isChecked);
			});
			cb.setChecked(actionsEnabled.get(p));

			return convertView;
		}

		// Load from field object, save to field object
		if (convertView == null) {
			LayoutInflater layoutInflater = MainActivity.getCurrentFragment().getLayoutInflater();
			convertView = layoutInflater.inflate(R.layout.advanced_settings_action, null);
		}

		List<GameBoardAction> acs = actions.get(p);

		Spinner s = (Spinner) convertView;
		List<GameBoardAction> as = new ArrayList<>();
		as.add(null);
		for(GameBoardAction a : GameBoardAction.values()) {
			if(a != GameBoardAction.WIN) as.add(a);
		}
		List<String> friendlyNames = new ArrayList<>();
		for(GameBoardAction a : as) friendlyNames.add(a == null ? "No action" : a.getFriendlyName());
		s.setAdapter(new ArrayAdapter<>(context, R.layout.spinner_item, friendlyNames));
		s.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				GameBoardAction action = as.get(position);
				acs.set(childPosition - 1, action);
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) { }
		});
		GameBoardAction sel = acs.get(childPosition - 1);
		s.setSelection(sel == null ? 0 : as.indexOf(sel));

		return convertView;
	}

	@Override
	public int getChildTypeCount() {
		return 2;
	}

	@Override
	public int getChildType(int groupPosition, int childPosition) {
		return childPosition == 0 ? 0 : 1;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return false;
	}
}
