package me.mrletsplay.secretreichstagandroid.ui;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import androidx.core.util.Consumer;

import java.util.List;

import me.mrletsplay.secretreichstagandroid.MainActivity;
import me.mrletsplay.secretreichstagandroid.R;
import me.mrletsplay.srweb.game.Player;

public class PlayerAdapter extends BaseAdapter {

	private Context context;
	private List<Player> players;
	private Consumer<Player> onSelect;

	public PlayerAdapter(Context context, List<Player> players, Consumer<Player> onSelect) {
		this.context = context;
		this.players = players;
		this.onSelect = onSelect;
	}

	@Override
	public int getCount() {
		return players.size();
	}

	@Override
	public Player getItem(int position) {
		return players.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if(convertView == null) {
			convertView = MainActivity.getCurrentFragment().getLayoutInflater().inflate(R.layout.player_item, parent, false);
		}

		TextView tv = convertView.findViewById(R.id.player_item_name);
		tv.setText(getItem(position).getName());

		convertView.setOnClickListener(view -> onSelect.accept(getItem(position)));

		return convertView;
	}
}
