package me.mrletsplay.secretreichstagandroid.fragment;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.mrletsplay.secretreichstagandroid.GameAsset;
import me.mrletsplay.secretreichstagandroid.MainActivity;
import me.mrletsplay.secretreichstagandroid.Networking;
import me.mrletsplay.secretreichstagandroid.SerializationUtils;
import me.mrletsplay.secretreichstagandroid.ui.UIGameBoard;
import me.mrletsplay.secretreichstagandroid.R;
import me.mrletsplay.secretreichstagandroid.ui.UIGameSurface;
import me.mrletsplay.srweb.game.Player;
import me.mrletsplay.srweb.game.state.GameParty;
import me.mrletsplay.srweb.game.state.GameRole;
import me.mrletsplay.srweb.game.state.GameState;
import me.mrletsplay.srweb.packet.Packet;
import me.mrletsplay.srweb.packet.impl.PacketClientStartGame;

public class GameFragment extends Fragment {

	private LinearLayout playerList;
	private LinearLayout gameBoardContainer;
	private List<UIGameBoard> gameBoards = new ArrayList<>();
	private Map<String, LinearLayout> playerElements = new HashMap<>();
	private AlertDialog startGameAlert;
	private UIGameSurface gameSurface;

	@Override
	public void onAttach(@NonNull Context context) {
		super.onAttach(context);
	}

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.game_layout, container, false);

		gameBoardContainer = v.findViewById(R.id.game_board_container);

		UIGameBoard lib = new UIGameBoard(getContext(), GameParty.LIBERAL);
		gameBoards.add(lib);
		gameBoardContainer.addView(lib);

		UIGameBoard fasc = new UIGameBoard(getContext(), GameParty.FASCIST);
		gameBoards.add(fasc);
		gameBoardContainer.addView(fasc);

		if(MainActivity.getRoom().getGameState().getCommunistBoard() != null) {
			UIGameBoard comm = new UIGameBoard(getContext(), GameParty.COMMUNIST);
			gameBoards.add(comm);
			gameBoardContainer.addView(comm);
		}

		playerList = v.findViewById(R.id.player_list);
		gameSurface = v.findViewById(R.id.game_surface);

		addOrUpdatePlayer(MainActivity.getSelfPlayer());
		for(Player pl : MainActivity.getRoom().getPlayers()) {
			addOrUpdatePlayer(pl);
		}
		return v;
	}

	public void addOrUpdatePlayer(Player player) {
		playerList.post(() -> {
			LinearLayout ll = playerElements.get(player.getID());
			if(ll == null) {
				ll = new LinearLayout(getContext());
				ll.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
				ll.setBackgroundColor(Color.DKGRAY);
				ll.setLayoutMode(LinearLayout.HORIZONTAL);
				TextView n = new TextView(getContext());
				ll.addView(n);
				playerList.addView(ll);
				playerElements.put(player.getID(), ll);
			}

			TextView tv = (TextView) ll.getChildAt(0);
			tv.setPadding(10, 5, 10, 5);

			if(MainActivity.getSelfPlayer().getID().equals(player.getID())) {
				tv.setTextColor(ResourcesCompat.getColor(getResources(), R.color.self, null));
			}else if(MainActivity.getSelfRole() != null) {
				GameRole r = MainActivity.getSelfRole();
				if (MainActivity.isTeammate(player)) {
					int col = getColor("teammate_" + r.getParty().name().toLowerCase());
					tv.setTextColor(col);
				}
			}else {
				tv.setTextColor(Color.WHITE);
			}

			boolean dead = false;
			for(Player p : MainActivity.getRoom().getGameState().getDeadPlayers()) {
				if(p.getID().equals(player.getID())) {
					dead = true;
					break;
				}
			}

			if(dead) {
				tv.setPaintFlags(tv.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
			}else {
				tv.setPaintFlags(tv.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
			}

			tv.setTypeface(ResourcesCompat.getFont(getContext(), R.font.germania_one_regular));
			tv.setText(player.getName());
			tv.setTextSize(25f);
			Paint.FontMetrics fm = tv.getPaint().getFontMetrics();
			int s = (int) (fm.descent - fm.ascent);
			tv.setMaxHeight(s + 10);

			// Icons
			while(ll.getChildCount() > 1) ll.removeViewAt(1);

			if(!player.isOnline()) addIcon(ll, GameAsset.ICON_CONNECTION, s);

			GameState st = MainActivity.getRoom().getGameState();

			if(st.getPresident() != null && st.getPresident().getID().equals(player.getID()))
				addIcon(ll, GameAsset.ICON_PRESIDENT, s);

			if(st.getChancellor() != null && st.getChancellor().getID().equals(player.getID()))
				addIcon(ll, GameAsset.ICON_CHANCELLOR, s);

			if(st.getPreviousPresident() != null && st.getPreviousPresident().getID().equals(player.getID()))
				addIcon(ll, GameAsset.ICON_PREVIOUS_PRESIDENT, s);

			if(st.getPreviousChancellor() != null && st.getPreviousChancellor().getID().equals(player.getID()))
				addIcon(ll, GameAsset.ICON_PREVIOUS_CHANCELLOR, s);

			if(st.getBlockedPlayer() != null && st.getBlockedPlayer().getID().equals(player.getID()))
				addIcon(ll, GameAsset.ICON_PLAYER_BLOCKED, s);

			if(MainActivity.isPlayerDead(player))
				addIcon(ll, GameAsset.ICON_DEAD, s);

			if(MainActivity.isPlayerNotHitler(player))
				addIcon(ll, GameAsset.ICON_NOT_HITLER, s);

			if(MainActivity.isPlayerNotStalin(player))
				addIcon(ll, GameAsset.ICON_NOT_STALIN, s);

			if(MainActivity.getVoteResults() != null && MainActivity.getVoteResults().has(player.getID())) {
				try {
					boolean vote = MainActivity.getVoteResults().getBoolean(player.getID());
					addIcon(ll, vote ? GameAsset.ICON_YES : GameAsset.ICON_NO, s);
				}catch(JSONException e) {
					e.printStackTrace();
				}
			}

			// TODO: was role

			if(MainActivity.getPreviousRoles() != null && MainActivity.getPreviousRoles().has(player.getID())) {
				try {
					GameRole role = SerializationUtils.cast(MainActivity.getPreviousRoles().getJSONObject(player.getID()));
					addIcon(ll, GameAsset.valueOf("ICON_ROLE_" + role.name()), s);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}

			playerList.invalidate();
		});
	}

	public void showStartDialogIfNeeded() {
		new Handler(Looper.getMainLooper()).post(() -> {
			if(!MainActivity.getRoom().isGameRunning()
					&& MainActivity.getRoom().getPlayers().size() == MainActivity.getRoom().getSettings().getPlayerCount()
					&& MainActivity.getSelfPlayer().getID().equals(MainActivity.getRoom().getPlayers().get(0).getID())) {
				AlertDialog d = new AlertDialog.Builder(getContext())
						.setMessage("Start the game?")
						.setPositiveButton("START", (dialog, which) -> {
							dialog.cancel();
							Networking.sendPacket(Packet.of(new PacketClientStartGame()));
						})
						.setCancelable(false)
						.create();
				startGameAlert = d;
				d.show();
			}
		});
	}

	private void addIcon(LinearLayout ll, GameAsset icon, int size) {
		// TODO: space between icons
		ImageView iv = new ImageView(getContext());
		iv.setMaxHeight(size);
		iv.setAdjustViewBounds(true);
		iv.setImageBitmap(icon.getBitmap());
		ll.addView(iv);
	}

	public void removePlayer(Player player) {
		playerList.post(() -> {
			if(startGameAlert != null) {
				startGameAlert.cancel();
				startGameAlert = null;
			}

			View child = playerElements.get(player.getID());
			playerList.removeView(child);
		});
	}

	private int getColor(String name) {
		return getResources().getColor(getResources().getIdentifier(name, "color", "me.mrletsplay.secretreichstagandroid"));
	}

	public void updateAll() {
		for(Player pl : MainActivity.getRoom().getPlayers()) {
			addOrUpdatePlayer(pl);
		}

		gameBoardContainer.invalidate();
		for(UIGameBoard b : gameBoards) b.invalidate();
		gameSurface.invalidate();
	}

}
