package me.mrletsplay.secretreichstagandroid;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.PreferenceManager;

import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import me.mrletsplay.secretreichstagandroid.fragment.AdvancedRoomSettingsFragment;
import me.mrletsplay.secretreichstagandroid.fragment.CreditsFragment;
import me.mrletsplay.secretreichstagandroid.fragment.GameFragment;
import me.mrletsplay.secretreichstagandroid.fragment.JoinRoomFragment;
import me.mrletsplay.secretreichstagandroid.fragment.MainMenuFragment;
import me.mrletsplay.secretreichstagandroid.fragment.RoomSettingsFragment;
import me.mrletsplay.secretreichstagandroid.fragment.SelectUsernameFragment;
import me.mrletsplay.secretreichstagandroid.fragment.SettingsFragment;
import me.mrletsplay.srweb.game.Player;
import me.mrletsplay.srweb.game.Room;
import me.mrletsplay.srweb.game.state.GameRole;
import me.mrletsplay.srweb.packet.Packet;
import me.mrletsplay.srweb.packet.impl.PacketClientConnect;
import me.mrletsplay.srweb.packet.impl.PacketServerJoinError;
import me.mrletsplay.srweb.packet.impl.PacketServerRoomInfo;

public class MainActivity extends AppCompatActivity {

	// TODO: test quit game
	// TODO: test no duplicate votes on rejoin
	// TODO: test custom drawing display size

	private static Room room;
	private static Player selfPlayer;
	private static Fragment currentFragment;
	private static GameRole selfRole;
	private static List<Player> teammates;
	private static Player leader;
	private static boolean selfVoted;
	private static JSONObject voteResults;
	private static JSONObject previousRoles;
	private static boolean gamePaused;
	private static List<String> eventLog = new ArrayList<>();

	private String roomID;

	private ExtendedRoomSettings roomSettings;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);

		requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.container);

		loadFragment(new MainMenuFragment());

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

		if(!prefs.contains("cache_assets")) {
			new AlertDialog.Builder(this)
					.setTitle("Cache")
					.setMessage("Cache assets?")
					.setPositiveButton("Yes", (dialog, which) -> {
						prefs.edit().putBoolean("cache_assets", true).apply();
						loadAssets();
					})
					.setNegativeButton("No", (dialog, which) -> {
						prefs.edit().putBoolean("cache_assets", false).apply();
						loadAssets();
					})
					.setCancelable(false)
					.show();
		}else {
			loadAssets();
		}

		RoomSettingsDefaults.load(this);

		getSupportFragmentManager().addOnBackStackChangedListener(() -> currentFragment = getSupportFragmentManager().findFragmentById(R.id.root_container));
	}

	private void loadAssets() {
		new Thread(() -> {
			List<Thread> ts = new ArrayList<>();
			AtomicBoolean errorOccurred = new AtomicBoolean();
			for(GameAsset a : GameAsset.values()) {
				Thread t = new Thread(() -> {
					try {
						a.load(MainActivity.this, getCacheDir());
					}catch(Exception e) {
						errorOccurred.set(true);
						e.printStackTrace();
					}
				});
				ts.add(t);
				t.start();
			}

			try {
				for(Thread t : ts) t.join();
			} catch (InterruptedException e) {
				return;
			}

			if(errorOccurred.get()) {
				runOnUiThread(() -> Snackbar.make(findViewById(R.id.root_container), "Failed to load assets (No internet?)", Snackbar.LENGTH_SHORT).show());
			}else {
				//runOnUiThread(() -> Toast.makeText(this, "Assets loaded!", Toast.LENGTH_SHORT).show());
				runOnUiThread(() -> Snackbar.make(findViewById(R.id.root_container), "Assets loaded!", Snackbar.LENGTH_SHORT).show());
			}
		}).start();
	}

	private final Handler mHideHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			System.out.println("HIDE SYSTEM UI");
			hideSystemUI();
		}
	};

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		System.out.println("FOCUS CHANGED: " + hasFocus);
		if(hasFocus) {
			mHideHandler.removeMessages(0);
			mHideHandler.sendEmptyMessageDelayed(0, 300);
			//hideSystemUI();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		delayedHide();
	}

	private void hideSystemUI() {
		getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
				| View.SYSTEM_UI_FLAG_FULLSCREEN
				| View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
				| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
				| View.SYSTEM_UI_FLAG_LAYOUT_STABLE
				| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
	}

	private void delayedHide() {
		mHideHandler.removeMessages(0);
		mHideHandler.sendEmptyMessageDelayed(0, 300);
	}

	public void newRoom(View v) {
		roomSettings = new ExtendedRoomSettings();
		loadFragment(new RoomSettingsFragment(roomSettings));
	}

	public void joinRoom(View v) {
		roomSettings = null;
		loadFragment(new JoinRoomFragment());
	}

	public void rejoinRoom(View v) {
		SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(this);
		String lastSession = p.getString("last_session", null);
		if(lastSession == null) {
			Toast.makeText(this, "No previous session available", Toast.LENGTH_SHORT).show();
			return;
		}

		PacketClientConnect con = new PacketClientConnect();
		con.setCreateRoom(false);
		con.setSessionID(lastSession);
		joinServer(con);
	}

	public void settings(View v) {
		loadFragment(new SettingsFragment());
	}

	public void menu(View v) {
		((GameFragment) currentFragment).loadMenu();
	}

	public void chat(View v) {
		((GameFragment) currentFragment).loadChat();
	}

	public void chatSend(View v) {
		((GameFragment) currentFragment).sendChat();
	}

	public void playerList(View v) {
		((GameFragment) currentFragment).loadPlayerList();
	}

	public void roomSettingsAdvanced(View v) {
		RoomSettingsFragment fr = (RoomSettingsFragment) currentFragment;
		fr.applySettings(false);
		loadFragment(new AdvancedRoomSettingsFragment(roomSettings));
	}

	public void roomSettingsConfirmAdvanced(View v) {
		AdvancedRoomSettingsFragment fr = (AdvancedRoomSettingsFragment) currentFragment;
		if(!fr.applySettings()) return;
		onBackPressed();
	}

	public void roomSettingsConfirm(View v) {
		RoomSettingsFragment fr = (RoomSettingsFragment) currentFragment;
		if(!fr.applySettings(true)) return;
		loadFragment(new SelectUsernameFragment());
	}

	public void roomIDConfirm(View v) {
		JoinRoomFragment fr = (JoinRoomFragment) currentFragment;

		if(fr.getRoomID().isEmpty()) {
			Toast.makeText(this, "You need to input a room id", Toast.LENGTH_LONG).show();
			return;
		}

		roomID = fr.getRoomID();

		loadFragment(new SelectUsernameFragment());
	}

	public void usernameConfirm(View v) {
		if(!GameAsset.isEverythingLoaded()) {
			Toast.makeText(this, "Still waiting for resources to load!", Toast.LENGTH_LONG).show();
			return;
		}

		SelectUsernameFragment fr = (SelectUsernameFragment) currentFragment;

		if(fr.getUsername().isEmpty()) {
			Toast.makeText(this, "You need to enter a username", Toast.LENGTH_LONG).show();
			return;
		}

		PacketClientConnect con = new PacketClientConnect();
		con.setPlayerName(fr.getUsername());
		if(roomSettings != null) {
			con.setCreateRoom(true);
			con.setRoomName(roomSettings.getRoomName());
			con.setRoomSettings(roomSettings.getRoomSettings());
		}else {
			con.setCreateRoom(false);
			con.setRoomID(roomID);
		}

		joinServer(con);
	}

	public void credits(View v) {
		loadFragment(new CreditsFragment());
	}

	public void back(View v) {
		onBackPressed();
	}

	public void addServer(View v) {
		View view = getLayoutInflater().inflate(R.layout.add_server, null);
		EditText name = view.findViewById(R.id.add_server_name);
		EditText url = view.findViewById(R.id.add_server_url);
		new AlertDialog.Builder(this)
				.setTitle("Add Server")
				.setView(view)
				.setPositiveButton("Add", (dialog, which) -> {
					String n = name.getText().toString().trim();
					String u = url.getText().toString().trim();
					if(n.isEmpty() || u.isEmpty()) {
						new AlertDialog.Builder(this)
								.setTitle("Error")
								.setMessage("You need to input name, host, and port")
								.setPositiveButton(R.string.okay, null)
								.show();
						return;
					}

					if(!u.startsWith("ws://") && !u.startsWith("wss://")) {
						new AlertDialog.Builder(this)
								.setTitle("Error")
								.setMessage("The server URL must start with ws:// or wss://")
								.setPositiveButton(R.string.okay, null)
								.show();
						return;
					}

					SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
					try {
						JSONArray arr = new JSONArray(prefs.getString("servers", "[]"));

						if(n.equals("Official Server")) {
							new AlertDialog.Builder(this)
									.setTitle("Error")
									.setMessage("A server with that name already exists")
									.setPositiveButton(R.string.okay, null)
									.show();
							return;
						}

						for(int i = 0; i < arr.length(); i++) {
							if(arr.getJSONObject(i).getString("name").equals(n)) {
								new AlertDialog.Builder(this)
										.setTitle("Error")
										.setMessage("A server with that name already exists")
										.setPositiveButton(R.string.okay, null)
										.show();
								return;
							}
						}

						JSONObject srv = new JSONObject();
						srv.put("name", n);
						srv.put("url", u);
						arr.put(srv);
						prefs.edit().putString("servers", arr.toString()).apply();
					} catch (JSONException e) {
						e.printStackTrace();
					}
				})
				.setNegativeButton(R.string.cancel, null)
				.show();
	}

	public void removeServer(View v) throws JSONException {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		JSONArray arr = new JSONArray(prefs.getString("servers", "[]"));
		View view = getLayoutInflater().inflate(R.layout.remove_server, null);
		ListView servers = view.findViewById(R.id.servers_list);
		List<String> els = new ArrayList<>();
		for(int i = 0; i < arr.length(); i++) {
			els.add(arr.getJSONObject(i).getString("name"));
		}
		servers.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_multiple_choice, els));
		new AlertDialog.Builder(this)
				.setTitle("Remove Servers")
				.setView(view)
				.setPositiveButton("Remove", (dialog, which) -> {
					for(int i = arr.length() - 1; i >= 0; i--) {
						if(servers.getCheckedItemPositions().get(i)) arr.remove(i);
					}
					prefs.edit().putString("servers", arr.toString()).apply();
				})
				.show();
	}

	private void joinServer(PacketClientConnect connectPacket) {
		eventLog = new ArrayList<>();

		new Thread(() -> {
			try {
				Networking.init(this);

				Packet packet = Packet.of(connectPacket);
				Networking.sendPacket(packet).thenAccept(p -> {
					if(p.getData() instanceof PacketServerJoinError) {
						PacketServerJoinError joinError = (PacketServerJoinError) p.getData();
						System.out.println(joinError.getMessage());
						runOnUiThread(() -> new AlertDialog.Builder(this)
								.setTitle("Error")
								.setMessage("Failed to connect to server:\n" + joinError.getMessage())
								.setCancelable(true)
								.setPositiveButton(R.string.okay, (dialog, id) -> dialog.cancel())
								.create().show());
						return;
					}

					roomSettings = null;

					PacketServerRoomInfo roomInfo = (PacketServerRoomInfo) p.getData();
					room = roomInfo.getRoom();
					selfPlayer = roomInfo.getSelfPlayer();
					selfVoted = roomInfo.isVoteDone(); // Relevant when rejoining
					GameFragment gameFragment = new GameFragment();
					loadFragment(gameFragment);
					SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
					prefs.edit().putString("last_session", roomInfo.getSessionID()).apply();
					Networking.setPacketListener(new DefaultPacketListener());
				});
			} catch (Exception e) {
				runOnUiThread(() -> new AlertDialog.Builder(this)
						.setTitle("Error")
						.setMessage("Failed to connect to server:\n" + e.getMessage())
						.setCancelable(true)
						.setPositiveButton(R.string.okay, (dialog, id) -> dialog.cancel())
						.create().show());
			}
		}).start();
	}

	private void loadFragment(Fragment frag) {
		FragmentManager man = getSupportFragmentManager();
		FragmentTransaction t = man.beginTransaction();

		Fragment f = man.findFragmentById(R.id.root_container);
		if(f == null) {
			t.add(R.id.root_container, frag);
		}else {
			t.replace(R.id.root_container, frag);
		}

		currentFragment = frag;

		t.addToBackStack(null);
		t.commit();

		mHideHandler.removeMessages(0);
		mHideHandler.sendEmptyMessageDelayed(0, 300);
	}

	public static Room getRoom() {
		return room;
	}

	public static Player getSelfPlayer() {
		return selfPlayer;
	}

	public static void setSelfRole(GameRole selfRole) {
		MainActivity.selfRole = selfRole;
	}

	public static GameRole getSelfRole() {
		return selfRole;
	}

	public static void setTeammates(List<Player> teammates) {
		MainActivity.teammates = teammates;
	}

	public static List<Player> getTeammates() {
		return teammates;
	}

	public static boolean isTeammate(Player player) {
		if(teammates == null) return false;
		for(Player p : teammates) {
			if(p.getID().equals(player.getID())) return true;
		}
		return false;
	}

	public static void setLeader(Player leader) {
		MainActivity.leader = leader;
	}

	public static Player getLeader() {
		return leader;
	}

	public static Fragment getCurrentFragment() {
		return currentFragment;
	}

	public static void setSelfVoted(boolean selfVoted) {
		MainActivity.selfVoted = selfVoted;
	}

	public static boolean isSelfVoted() {
		return selfVoted;
	}

	public static boolean isPlayerDead(Player player) {
		for(Player d : room.getGameState().getDeadPlayers()) {
			if(d.getID().equals(player.getID())) return true;
		}
		return false;
	}

	public static boolean isPlayerNotHitler(Player player) {
		for(Player d : room.getGameState().getNotHitlerConfirmed()) {
			if(d.getID().equals(player.getID())) return true;
		}
		return false;
	}

	public static boolean isPlayerNotStalin(Player player) {
		for(Player d : room.getGameState().getNotStalinConfirmed()) {
			if(d.getID().equals(player.getID())) return true;
		}
		return false;
	}

	public static void setVoteResults(JSONObject voteResults) {
		MainActivity.voteResults = voteResults;
	}

	public static JSONObject getVoteResults() {
		return voteResults;
	}

	public static void setPreviousRoles(JSONObject previousRoles) {
		MainActivity.previousRoles = previousRoles;
	}

	public static JSONObject getPreviousRoles() {
		return previousRoles;
	}

	public static void setGamePaused(boolean gamePaused) {
		MainActivity.gamePaused = gamePaused;
	}

	public static boolean isGamePaused() {
		return gamePaused;
	}

	public static void addEventLogEntry(String entry) {
		eventLog.add(entry);
		if(eventLog.size() > 100) eventLog.remove(0);
	}

	public static List<String> getEventLog() {
		return eventLog;
	}

	@Override
	public void onBackPressed() {
		if(currentFragment instanceof GameFragment) {
			new AlertDialog.Builder(this)
					.setTitle("Quit")
					.setMessage("Do you really want to quit?")
					.setPositiveButton("Yes", (dialog, which) -> {
						dialog.dismiss();
						Networking.stop();
						((GameFragment) currentFragment).quit();
						loadFragment(new MainMenuFragment());
					})
					.setNegativeButton("No", (dialog, which) -> dialog.dismiss())
					.show();
			return;
		}else if(currentFragment instanceof MainMenuFragment) return;

		super.onBackPressed();
	}
}