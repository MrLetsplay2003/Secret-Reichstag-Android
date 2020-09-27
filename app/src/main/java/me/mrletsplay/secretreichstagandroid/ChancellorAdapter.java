package me.mrletsplay.secretreichstagandroid;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import androidx.core.util.Consumer;

import java.util.List;

import me.mrletsplay.srweb.game.Player;

public class ChancellorAdapter extends BaseAdapter {

	private Context context;
	private List<Player> players;
	private Consumer<Player> onSelect;

	public ChancellorAdapter(Context context, List<Player> players, Consumer<Player> onSelect) {
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
			convertView = MainActivity.getCurrentFragment().getLayoutInflater().inflate(R.layout.chancellor_item, parent, false);
		}

		TextView tv = convertView.findViewById(R.id.chancellor_item_name);
		tv.setText(getItem(position).getName());

		convertView.setOnClickListener(view -> onSelect.accept(getItem(position)));

		return convertView;
	}
}
