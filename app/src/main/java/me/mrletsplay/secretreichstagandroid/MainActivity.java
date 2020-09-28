package me.mrletsplay.secretreichstagandroid;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import me.mrletsplay.secretreichstagandroid.fragment.JoinRoomFragment;
import me.mrletsplay.secretreichstagandroid.fragment.MainMenuFragment;
import me.mrletsplay.secretreichstagandroid.fragment.GameFragment;
import me.mrletsplay.secretreichstagandroid.fragment.RoomSettingsFragment;
import me.mrletsplay.secretreichstagandroid.fragment.SelectUsernameFragment;
import me.mrletsplay.srweb.game.GameMode;
import me.mrletsplay.srweb.game.Player;
import me.mrletsplay.srweb.game.Room;
import me.mrletsplay.srweb.game.RoomSettings;
import me.mrletsplay.srweb.game.state.GameRole;
import me.mrletsplay.srweb.packet.Packet;
import me.mrletsplay.srweb.packet.impl.PacketClientConnect;
import me.mrletsplay.srweb.packet.impl.PacketServerJoinError;
import me.mrletsplay.srweb.packet.impl.PacketServerRoomInfo;

public class MainActivity extends AppCompatActivity {

	private static Room room;
	private static Player selfPlayer;
	private static Fragment currentFragment;
	private static GameRole selfRole;
	private static List<Player> teammates;
	private static Player leader;
	private static boolean selfVoted;
	private static JSONObject voteResults;

	private String roomID;

	private String roomName;
	private RoomSettings roomSettings;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.container);

		loadFragment(new MainMenuFragment());

		new Thread(() -> {
			List<Thread> ts = new ArrayList<>();
			AtomicInteger i = new AtomicInteger(0);
			AtomicBoolean errorOccurred = new AtomicBoolean();
			for(GameAsset a : GameAsset.values()) {
				Thread t = new Thread(() -> {
					try {
						a.load();
						if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) { // To prevent toast stacking on older Android
							runOnUiThread(() -> Toast.makeText(this, "Loading assets (" + i.incrementAndGet() + "/" + GameAsset.values().length + ")", Toast.LENGTH_SHORT).show());
						}
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

		getSupportFragmentManager().addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
			@Override
			public void onBackStackChanged() {
				currentFragment = getSupportFragmentManager().findFragmentById(R.id.root_container);
			}
		});
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		if(hasFocus) hideSystemUI();
	}

	private void hideSystemUI() {
		getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
				| View.SYSTEM_UI_FLAG_FULLSCREEN
				| View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
				| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
				| View.SYSTEM_UI_FLAG_LAYOUT_STABLE
				| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		/*System.out.println(event.getX());
		System.out.println(event.getY());*/
		//Toast.makeText(this, "TOUCH", Toast.LENGTH_LONG).show();
		/*switch(event.getAction()) {
			case MotionEvent.ACTION_MOVE:
			{
				System.out.println("MOVE ME");
			}
		}*/

		return true;
	}

	public void newRoom(View v) {
		loadFragment(new RoomSettingsFragment());
	}

	public void joinRoom(View v) {
		// loadFragment(new JoinRoomFragment());

	}

	public void roomSettingsConfirm(View v) {
		RoomSettingsFragment fr = (RoomSettingsFragment) currentFragment;

		if(fr.getRoomName().isEmpty()) {
			Snackbar.make(findViewById(R.id.root_container), "You need to enter a room name", Snackbar.LENGTH_LONG).show();
			return;
		}

		if(fr.getMaxPlayers() < 3 /* TODO */ || fr.getMaxPlayers() > 14) {
			Snackbar.make(findViewById(R.id.root_container), "You need to enter a number of players between 5 and 14", Snackbar.LENGTH_LONG).show();
			return;
		}

		roomSettings = new RoomSettings();
		roomSettings.setMode(fr.getSelectedGameMode().name());
		switch(fr.getSelectedGameMode()) {
			case SECRET_HITLER:
				roomSettings.setCommunistCardCount(0);
				roomSettings.setFascistCardCount(11);
				roomSettings.setLiberalCardCount(6);
				break;
			case SECRET_REICHSTAG:
				roomSettings.setCommunistCardCount(11);
				roomSettings.setFascistCardCount(11);
				roomSettings.setLiberalCardCount(9);
				break;
		}

		roomSettings.setPlayerCount(fr.getMaxPlayers());
		roomName = fr.getRoomName();
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

		new Thread(() -> {
			try {
				Networking.init(false);

				PacketClientConnect con = new PacketClientConnect();
				con.setPlayerName(fr.getUsername());
				if(roomSettings != null) {
					con.setCreateRoom(true);
					con.setRoomName(roomName);
					con.setRoomSettings(roomSettings);
				}else {
					con.setCreateRoom(false);
					con.setRoomID(roomID);
				}

				Packet packet = Packet.of(con);
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

					PacketServerRoomInfo roomInfo = (PacketServerRoomInfo) p.getData();
					room = roomInfo.getRoom();
					selfPlayer = roomInfo.getSelfPlayer();
					GameFragment gameFragment = new GameFragment();
					loadFragment(gameFragment);
					// TODO: Session ID

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

	@Override
	public void onBackPressed() {
		if(currentFragment instanceof GameFragment) {
			// TODO: show confirmation alert?
			return;
		}

		super.onBackPressed();
	}
}