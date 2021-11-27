package me.mrletsplay.secretreichstagandroid.fragment;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.PictureDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Space;
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
import me.mrletsplay.secretreichstagandroid.ui.UICardStack;
import me.mrletsplay.srweb.game.Player;
import me.mrletsplay.srweb.game.state.GameParty;
import me.mrletsplay.srweb.game.state.GameRole;
import me.mrletsplay.srweb.game.state.GameState;
import me.mrletsplay.srweb.packet.Packet;
import me.mrletsplay.srweb.packet.impl.PacketClientChatMessage;
import me.mrletsplay.srweb.packet.impl.PacketClientStartGame;

public class GameFragment extends Fragment {

	private LinearLayout gameBoardContainer;
	private List<UIGameBoard> gameBoards = new ArrayList<>();
	private Map<String, LinearLayout> playerElements = new HashMap<>();
	private AlertDialog startGameAlert;
	private UICardStack gameSurface;
	private TextView roleText;
	private boolean loadFinished;
	private FrameLayout loaderContainer;

	private LinearLayout playerList;
	private TextView eventLog;
	private ScrollView eventLogScroll;
	private EditText chatInput;
	private TextView menuRoomID;

	@Override
	public void onAttach(@NonNull Context context) {
		super.onAttach(context);
	}

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.game_layout, container, false);

		gameBoardContainer = v.findViewById(R.id.game_board_container);

		UIGameBoard lib = new UIGameBoard(getContext(), GameParty.LIBERAL, true);
		gameBoards.add(lib);
		gameBoardContainer.addView(lib);

		if(MainActivity.getRoom().getGameState().getCommunistBoard() != null) {
			UIGameBoard comm = new UIGameBoard(getContext(), GameParty.COMMUNIST, false);
			gameBoards.add(comm);
			gameBoardContainer.addView(comm);
		}

		UIGameBoard fasc = new UIGameBoard(getContext(), GameParty.FASCIST, false);
		gameBoards.add(fasc);
		gameBoardContainer.addView(fasc);

		playerList = v.findViewById(R.id.player_list);
		gameSurface = v.findViewById(R.id.card_stack);
		roleText = v.findViewById(R.id.role_text);
		loaderContainer = v.findViewById(R.id.loader_container);

		addOrUpdatePlayer(MainActivity.getSelfPlayer());
		for(Player pl : MainActivity.getRoom().getPlayers()) {
			addOrUpdatePlayer(pl);
		}

		return v;
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		loadFinished = true;
		updateAll();
	}

	public void addOrUpdatePlayer(Player player) {
		if(!loadFinished) return;
		if(playerList == null) return;
		playerList.post(() -> {
			LinearLayout ll = playerElements.get(player.getID());
			if(ll == null) {
				ll = new LinearLayout(getContext());
				ll.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
				ll.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.player_background, null));
				ll.setLayoutMode(LinearLayout.HORIZONTAL);
				TextView n = new TextView(getContext());
				ll.addView(n);
				playerList.addView(ll);
				playerElements.put(player.getID(), ll);
			}

			ll.setVisibility(View.INVISIBLE); // We need to make it invisible at first because Android does weird stuff and briefly shows some kind of box at the end of our TextView otherwise

			TextView tv = (TextView) ll.getChildAt(0);
			tv.setPadding(10, 5, 10, 5);

			if(MainActivity.getSelfPlayer().getID().equals(player.getID())) {
				tv.setTextColor(ResourcesCompat.getColor(getResources(), R.color.self, null));
			}else if(MainActivity.getSelfRole() != null) {
				GameRole r = MainActivity.getSelfRole();
				if (MainActivity.isTeammate(player)) {
					int col = getColor("teammate_" + r.getParty().name().toLowerCase());
					tv.setTextColor(col);
				}else if(MainActivity.getLeader() != null && MainActivity.getLeader().getID().equals(player.getID())) {
					int col = getColor("leader_" + r.getParty().name().toLowerCase());
					tv.setTextColor(col);
				}else {
					tv.setTextColor(Color.WHITE);
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
			tv.setGravity(Gravity.CENTER_VERTICAL);

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

			if(MainActivity.getPreviousRoles() != null && MainActivity.getPreviousRoles().has(player.getID())) {
				try {
					GameRole role = SerializationUtils.cast(MainActivity.getPreviousRoles().getJSONObject(player.getID()));
					addIcon(ll, GameAsset.valueOf("ICON_ROLE_" + role.name()), s);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}

			ll.setVisibility(View.VISIBLE);
			playerList.invalidate();
		});
	}

	public void addEventLogEntry(String entry) {
		if(eventLog == null) return;
		eventLog.post(() -> {
			eventLog.append("\n" + entry);
			// eventLog.requestLayout();

			eventLogScroll.post(() -> eventLogScroll.fullScroll(View.FOCUS_DOWN));
		});
	}

	public void showStartDialogIfNeeded() {
		new Handler(Looper.getMainLooper()).post(() -> {
			if(!MainActivity.getRoom().isGameRunning()
					&& MainActivity.getRoom().getPlayers().size() >= MainActivity.getRoom().getMode().getMinPlayers()
					&& MainActivity.getSelfPlayer().getID().equals(MainActivity.getRoom().getPlayers().get(0).getID())) {
				if(startGameAlert != null) return;
				AlertDialog d = new AlertDialog.Builder(getContext())
						.setMessage("Start the game?")
						.setPositiveButton("START", (dialog, which) -> {
							dialog.cancel();
							startGameAlert = null;
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
		Space sp = new Space(getContext());
		sp.setLayoutParams(new LinearLayout.LayoutParams(10, ViewGroup.LayoutParams.MATCH_PARENT));
		ll.addView(sp);

		ImageView iv = new ImageView(getContext());
		iv.setMaxHeight(size);
		LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT);
		llp.gravity = Gravity.CENTER_VERTICAL;
		iv.setLayoutParams(llp);
		iv.setAdjustViewBounds(true);
		iv.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
		iv.setImageDrawable(new PictureDrawable(icon.getSVG().renderToPicture()));
		ll.addView(iv);
	}

	public void removePlayer(Player player) {
		playerList.post(() -> {
			if(startGameAlert != null && MainActivity.getRoom().getPlayers().size() < MainActivity.getRoom().getMode().getMinPlayers()) {
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
		if(!loadFinished) return;
		new Handler(Looper.getMainLooper()).post(() -> {
			for(Player pl : MainActivity.getRoom().getPlayers()) {
				addOrUpdatePlayer(pl);
			}

			gameBoardContainer.invalidate();
			for(UIGameBoard b : gameBoards) b.invalidate();
			gameSurface.invalidate();

			if(MainActivity.getSelfRole() != null) {
				roleText.setTextColor(getColor("role_" + MainActivity.getSelfRole().name().toLowerCase()));
				roleText.setText(MainActivity.getSelfRole().name());
			}else {
				roleText.setTextColor(Color.BLACK);
				roleText.setText(R.string.ingame_menu_game_not_running);
			}
		});
	}

	public void loadChat() {
		if(eventLog != null) return;

		clearAll();

		View v = getLayoutInflater().inflate(R.layout.chat, loaderContainer);
		eventLog = v.findViewById(R.id.chat_box);

		for(String entry : MainActivity.getEventLog()) {
			eventLog.append("\n" + entry);
		}

		eventLogScroll = v.findViewById(R.id.chat_scroll);
		eventLogScroll.post(() -> eventLogScroll.fullScroll(View.FOCUS_DOWN));

		chatInput = v.findViewById(R.id.chat_input);
	}

	public void sendChat() {
		String txt = chatInput.getText().toString().trim();
		if(txt.isEmpty()) return;
		PacketClientChatMessage cm = new PacketClientChatMessage();
		cm.setMessage(txt);
		Networking.sendPacket(Packet.of(cm));
		chatInput.getText().clear();
	}

	public void loadPlayerList() {
		if(playerList != null) return;

		clearAll();

		View v = getLayoutInflater().inflate(R.layout.player_list, loaderContainer);
		playerList = v.findViewById(R.id.player_list);

		playerList.post(() -> {
			for(Player pl : MainActivity.getRoom().getPlayers()) {
				addOrUpdatePlayer(pl);
			}
		});
	}

	public void loadMenu() {
		if(menuRoomID != null) return;

		clearAll();

		View v = getLayoutInflater().inflate(R.layout.ingame_menu, loaderContainer);
		menuRoomID = v.findViewById(R.id.menu_room_id);

		menuRoomID.setText(getString(R.string.ingame_menu_room_id, MainActivity.getRoom().getID()));
	}

	private void clearAll() {
		while(loaderContainer.getChildCount() > 0) loaderContainer.removeViewAt(0);
		playerList = null;
		playerElements.clear();
		eventLog = null;
		eventLogScroll = null;
		menuRoomID = null;
	}

	public void quit() {

	}

}
