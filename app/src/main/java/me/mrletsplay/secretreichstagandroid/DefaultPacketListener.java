package me.mrletsplay.secretreichstagandroid;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.util.Consumer;
import androidx.core.util.Predicate;
import androidx.fragment.app.Fragment;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import me.mrletsplay.secretreichstagandroid.fragment.GameFragment;
import me.mrletsplay.secretreichstagandroid.ui.ActionDialog;
import me.mrletsplay.secretreichstagandroid.ui.ChancellorAdapter;
import me.mrletsplay.secretreichstagandroid.ui.MovableFloatingActionButton;
import me.mrletsplay.srweb.game.Player;
import me.mrletsplay.srweb.game.state.GameMoveState;
import me.mrletsplay.srweb.game.state.GamePolicyCard;
import me.mrletsplay.srweb.game.state.GameState;
import me.mrletsplay.srweb.game.state.board.action.ActionBlockPlayer;
import me.mrletsplay.srweb.game.state.board.action.ActionExamineTopCards;
import me.mrletsplay.srweb.game.state.board.action.ActionExamineTopCardsOther;
import me.mrletsplay.srweb.game.state.board.action.ActionInspectPlayer;
import me.mrletsplay.srweb.game.state.board.action.ActionInspectPlayerResult;
import me.mrletsplay.srweb.game.state.board.action.ActionKillPlayer;
import me.mrletsplay.srweb.game.state.board.action.ActionPickPresident;
import me.mrletsplay.srweb.packet.Packet;
import me.mrletsplay.srweb.packet.PacketData;
import me.mrletsplay.srweb.packet.impl.PacketClientDiscardCard;
import me.mrletsplay.srweb.packet.impl.PacketClientDrawCards;
import me.mrletsplay.srweb.packet.impl.PacketClientPerformAction;
import me.mrletsplay.srweb.packet.impl.PacketClientSelectChancellor;
import me.mrletsplay.srweb.packet.impl.PacketClientVeto;
import me.mrletsplay.srweb.packet.impl.PacketClientVote;
import me.mrletsplay.srweb.packet.impl.PacketServerEventLogEntry;
import me.mrletsplay.srweb.packet.impl.PacketServerPauseGame;
import me.mrletsplay.srweb.packet.impl.PacketServerPickCards;
import me.mrletsplay.srweb.packet.impl.PacketServerPlayerAction;
import me.mrletsplay.srweb.packet.impl.PacketServerPlayerJoined;
import me.mrletsplay.srweb.packet.impl.PacketServerPlayerLeft;
import me.mrletsplay.srweb.packet.impl.PacketServerStartGame;
import me.mrletsplay.srweb.packet.impl.PacketServerStopGame;
import me.mrletsplay.srweb.packet.impl.PacketServerUnpauseGame;
import me.mrletsplay.srweb.packet.impl.PacketServerUpdateGameState;
import me.mrletsplay.srweb.packet.impl.PacketServerVeto;
import me.mrletsplay.srweb.packet.impl.PacketServerVoteResults;

public class DefaultPacketListener implements PacketListener {

	private AlertDialog currentAlert;
	private ActionDialog currentActionDialog;
	private Snackbar currentSnackbar;

	@Override
	public void onPacketReceived(Packet packet) {
		PacketData d = packet.getData();
		System.out.println(d);
		GameFragment fr = (GameFragment) MainActivity.getCurrentFragment();

		if(d instanceof PacketServerPlayerJoined) {
			PacketServerPlayerJoined j = (PacketServerPlayerJoined) d;
			if(!j.isRejoin()) {
				MainActivity.getRoom().getPlayers().add(j.getPlayer());
			}else {
				for(Player pl : MainActivity.getRoom().getPlayers()) {
					if(pl.getID().equals(j.getPlayer().getID())) pl.setOnline(true); // TODO: test fix: online doesn't update
				}
			}
			fr.addOrUpdatePlayer(j.getPlayer());
			fr.showStartDialogIfNeeded();
		}else if(d instanceof PacketServerPlayerLeft) {
			PacketServerPlayerLeft l = (PacketServerPlayerLeft) d;
			if(l.isHardLeave()) {
				Iterator<Player> plI = MainActivity.getRoom().getPlayers().iterator();
				while(plI.hasNext()) {
					Player pl = plI.next();
					if(pl.getID().equals(l.getPlayer().getID())) {
						plI.remove();
						break;
					}
				}

				fr.removePlayer(l.getPlayer());
			}else {
				for(Player pl : MainActivity.getRoom().getPlayers()) {
					if(pl.getID().equals(l.getPlayer().getID())) {
						pl.setOnline(false);
						break;
					}
				}

				fr.addOrUpdatePlayer(l.getPlayer());
			}
		}else if(d instanceof PacketServerStartGame) {
			System.out.println("START GAME");
			MainActivity.getRoom().setGameRunning(true);

			PacketServerStartGame s = (PacketServerStartGame) d;
			MainActivity.setLeader(s.getLeader());
			MainActivity.setSelfRole(s.getRole());
			MainActivity.setTeammates(s.getTeammates());
			MainActivity.setPreviousRoles(null);
			MainActivity.setSelfVoted(false);

			fr.updateAll();
		}else if(d instanceof PacketServerUpdateGameState) {
			GameState newState = ((PacketServerUpdateGameState) d).getNewState();
			MainActivity.getRoom().setGameState(newState);
			fr.updateAll();

			if(newState.getMoveState() != GameMoveState.VOTE) MainActivity.setSelfVoted(false);

			if(newState.getMoveState() == GameMoveState.VOTE && !MainActivity.isSelfVoted() && !MainActivity.isPlayerDead(MainActivity.getSelfPlayer())) {
				runOnUiThread(() -> {
					LayoutInflater inf = fr.getLayoutInflater();
					View v = inf.inflate(R.layout.vote, null);
					AlertDialog voteDialog = new AlertDialog.Builder(fr.getContext())
							.setView(v)
							.setCancelable(false)
							.create();

					TextView u1 = v.findViewById(R.id.vote_username_president);
					u1.setText(newState.getPresident().getName());

					TextView u2 = v.findViewById(R.id.vote_username_chancellor);
					u2.setText(newState.getChancellor().getName());

					ActionDialog dl = showActionDialog(voteDialog, v);

					Button voteYes = v.findViewById(R.id.vote_yes);
					voteYes.setOnClickListener(view -> {
						dl.dismiss();
						currentActionDialog = null;
						MainActivity.setSelfVoted(true);
						PacketClientVote vote = new PacketClientVote();
						vote.setYes(true);
						Networking.sendPacket(Packet.of(vote));
					});

					Button voteNo = v.findViewById(R.id.vote_no);
					voteNo.setOnClickListener(view -> {
						dl.dismiss();
						currentAlert = null;
						MainActivity.setSelfVoted(true);
						PacketClientVote vote = new PacketClientVote();
						vote.setYes(false);
						Networking.sendPacket(Packet.of(vote));
					});

					if(!MainActivity.isGamePaused()) voteDialog.show();
				});
			}else if(newState.getMoveState() == GameMoveState.SELECT_CHANCELLOR && newState.getPresident().getID().equals(MainActivity.getSelfPlayer().getID())) {
				showPickPlayerDialog("Select a player to be the next chancellor",  pl ->
						!MainActivity.isPlayerDead(pl)
						&& (newState.getPreviousPresident() == null || !newState.getPreviousPresident().getID().equals(pl.getID()))
						&& (newState.getPreviousChancellor() == null || !newState.getPreviousChancellor().getID().equals(pl.getID()))
						&& !pl.getID().equals(MainActivity.getSelfPlayer().getID())
						&& !(newState.getBlockedPlayer() != null && newState.getBlockedPlayer().getID().equals(pl.getID())),
						player -> {
							PacketClientSelectChancellor ch = new PacketClientSelectChancellor();
							ch.setPlayerID(player.getID());
							Networking.sendPacket(Packet.of(ch));
						});
			}else if(newState.getMoveState() == GameMoveState.DRAW_CARDS && newState.getPresident().getID().equals(MainActivity.getSelfPlayer().getID())) {
				/*runOnUiThread(() -> {
					AlertDialog drawDialog = new AlertDialog.Builder(fr.getContext())
							.setTitle("Draw cards")
							.setMessage("You need to draw some cards")
							.setPositiveButton("Draw", (dl, which) -> {
								dl.dismiss();
								currentAlert = null;
								Networking.sendPacket(Packet.of(new PacketClientDrawCards()));
							})
							.create();

					currentAlert = drawDialog;

					drawDialog.show();
				});*/

				runOnUiThread(() -> {
					LayoutInflater inf = fr.getLayoutInflater();
					View v = inf.inflate(R.layout.draw_cards, null);
					AlertDialog drawDialog = new AlertDialog.Builder(fr.getContext())
							.setView(v)
							.setCancelable(false)
							.create();

					ActionDialog dl = showActionDialog(drawDialog, v);

					Button confirm = v.findViewById(R.id.draw_confirm);
					confirm.setOnClickListener(view -> {
						dl.dismiss();
						currentActionDialog = null;
						PacketClientDrawCards draw = new PacketClientDrawCards();
						Networking.sendPacket(Packet.of(draw));
					});

					if(!MainActivity.isGamePaused()) drawDialog.show();
				});
			}
		}else if(d instanceof PacketServerVoteResults) {
			System.out.println("VOTE RESULTS");
			PacketServerVoteResults vr = (PacketServerVoteResults) d;
			MainActivity.setVoteResults(vr.getVotes());
			fr.updateAll();

			if(currentSnackbar != null) currentSnackbar.dismiss();
			currentSnackbar = Snackbar.make(fr.getView().findViewById(R.id.player_list_container), "Vote results are shown", Snackbar.LENGTH_INDEFINITE)
				.setBackgroundTint(Color.argb(128, 64, 64, 64))
				.setAction("Dismiss", v -> {
					currentSnackbar = null;
					MainActivity.setVoteResults(null);
					fr.updateAll();
				});

			currentSnackbar.show();
		}else if(d instanceof PacketServerPickCards) {
			System.out.println("PICK CARDS");
			showCardsDialog("Select the card  you want to dismiss", ((PacketServerPickCards) d).getCards(), true, index -> {
				PacketClientDiscardCard dc = new PacketClientDiscardCard();
				dc.setDiscardIndex(index);
				Networking.sendPacket(Packet.of(dc));
			});
		}else if(d instanceof PacketServerPlayerAction) {
			PacketServerPlayerAction a = (PacketServerPlayerAction) d;
			switch(a.getAction()) {
				case EXAMINE_TOP_CARDS:
				{
					ActionExamineTopCards tc = (ActionExamineTopCards) a.getData();
					showCardsDialog("These are the top three cards on the card pile", tc.getCards(), false, null);
					Networking.sendPacket(Packet.of(new PacketClientPerformAction()));
					break;
				}
				case EXAMINE_TOP_CARDS_OTHER:
				{
					showPickPlayerDialog("Select a player to view the top three cards", pl ->
							!MainActivity.getSelfPlayer().getID().equals(pl.getID())
							&& !MainActivity.isPlayerDead(pl), pl -> {
						PacketClientPerformAction pa = new PacketClientPerformAction();
						ActionExamineTopCardsOther k = new ActionExamineTopCardsOther();
						k.setPlayerID(pl.getID());
						pa.setData(k);
						Networking.sendPacket(Packet.of(pa));
					});
					break;
				}
				case KILL_PLAYER:
				{
					showPickPlayerDialog("Select a player to be killed", pl ->
							!MainActivity.getSelfPlayer().getID().equals(pl.getID())
							&& !MainActivity.isPlayerDead(pl), pl -> {
						PacketClientPerformAction pa = new PacketClientPerformAction();
						ActionKillPlayer k = new ActionKillPlayer();
						k.setPlayerID(pl.getID());
						pa.setData(k);
						Networking.sendPacket(Packet.of(pa));
					});
					break;
				}
				case PICK_PRESIDENT:
				{
					showPickPlayerDialog("Select a player to be the next president", pl ->
							!MainActivity.getSelfPlayer().getID().equals(pl.getID())
							&& !MainActivity.isPlayerDead(pl), pl -> {
						PacketClientPerformAction pa = new PacketClientPerformAction();
						ActionPickPresident k = new ActionPickPresident();
						k.setPlayerID(pl.getID());
						pa.setData(k);
						Networking.sendPacket(Packet.of(pa));
					});
					break;
				}
				case INSPECT_PLAYER: // TODO: test INSPECT_PLAYER action
				{
					showPickPlayerDialog("Select the player you want to inspect", pl ->
							!MainActivity.getSelfPlayer().getID().equals(pl.getID())
							&& !MainActivity.isPlayerDead(pl), pl -> {
						PacketClientPerformAction pa = new PacketClientPerformAction();
						ActionInspectPlayer k = new ActionInspectPlayer();
						k.setPlayerID(pl.getID());
						pa.setData(k);
						Networking.sendPacket(Packet.of(pa)).thenAccept(res -> {
							ActionInspectPlayerResult r = (ActionInspectPlayerResult) res.getData();
							runOnUiThread(() -> {
								LayoutInflater inf = fr.getLayoutInflater();
								View v = inf.inflate(R.layout.inspect_player, null);
								AlertDialog inspectDialog = new AlertDialog.Builder(fr.getContext())
										.setView(v)
										.setCancelable(false)
										.create();

								ActionDialog ad = showActionDialog(inspectDialog, v);

								TextView u1 = v.findViewById(R.id.inspect_username);
								u1.setText(pl.getName());

								TextView u2 = v.findViewById(R.id.inspect_role);
								u2.setText(r.getParty().getFriendlyNameSingular());

								Button okay = v.findViewById(R.id.inspect_okay);
								okay.setOnClickListener(view -> {
									ad.dismiss();
									currentActionDialog = null;
								});

								if(!MainActivity.isGamePaused()) inspectDialog.show();
							});
						});
					});
					break;
				}
				case BLOCK_PLAYER:
				{
					GameState state = MainActivity.getRoom().getGameState();
					showPickPlayerDialog("Select a player to be unelectable the next turn", pl ->
							!MainActivity.getSelfPlayer().getID().equals(pl.getID())
							&& !state.getChancellor().getID().equals(pl.getID())
							&& !(state.getPresident().getID().equals(pl.getID()) && MainActivity.getRoom().getPlayers().size() >= 8)
							&& !MainActivity.isPlayerDead(pl), pl -> {
						PacketClientPerformAction pa = new PacketClientPerformAction();
						ActionBlockPlayer k = new ActionBlockPlayer();
						k.setPlayerID(pl.getID());
						pa.setData(k);
						Networking.sendPacket(Packet.of(pa));
					});
					break;
				}
			}
		}else if(d instanceof PacketServerStopGame) {
			MainActivity.setVoteResults(null);
			MainActivity.setSelfRole(null);
			MainActivity.setSelfVoted(false);
			MainActivity.setTeammates(null);
			MainActivity.setLeader(null);
			MainActivity.getRoom().setGameRunning(false);
			if(currentAlert != null) currentAlert.dismiss();
			if(currentSnackbar != null) currentSnackbar.dismiss();
			MainActivity.setPreviousRoles(((PacketServerStopGame) d).getRoles());
			// TODO: test winner dialog
			runOnUiThread(() -> {
				LayoutInflater inf = fr.getLayoutInflater();
				View v = inf.inflate(R.layout.winner, null);
				AlertDialog winnerDialog = new AlertDialog.Builder(fr.getContext())
						.setView(v)
						.setCancelable(false)
						.create();

				TextView w = v.findViewById(R.id.winner_party);
				w.setText(((PacketServerStopGame) d).getWinner().getFriendlyName());

				Button winnerOkay = v.findViewById(R.id.winner_okay);
				winnerOkay.setOnClickListener(view -> {
					winnerDialog.dismiss();
					currentAlert = null;
					fr.showStartDialogIfNeeded();
				});

				currentAlert = winnerDialog;
				winnerDialog.show();
			});
		}else if(d instanceof PacketServerVeto) {
			runOnUiThread(() -> {
				LayoutInflater inf = fr.getLayoutInflater();
				View v = inf.inflate(R.layout.veto, null);
				AlertDialog vetoDialog = new AlertDialog.Builder(fr.getContext())
						.setView(v)
						.setCancelable(false)
						.create();

				ActionDialog ad = showActionDialog(vetoDialog, v);

				TextView u1 = v.findViewById(R.id.veto_username);
				u1.setText(MainActivity.getRoom().getGameState().getChancellor().getName());

				Button vetoYes = v.findViewById(R.id.veto_yes);
				vetoYes.setOnClickListener(view -> {
					ad.dismiss();
					currentActionDialog = null;
					PacketClientVeto veto = new PacketClientVeto();
					veto.setAcceptVeto(true);
					Networking.sendPacket(Packet.of(veto));
				});

				Button vetoNo = v.findViewById(R.id.veto_no);
				vetoNo.setOnClickListener(view -> {
					ad.dismiss();
					currentActionDialog = null;
					PacketClientVeto veto = new PacketClientVeto();
					veto.setAcceptVeto(false);
					Networking.sendPacket(Packet.of(veto));
				});

				if(!MainActivity.isGamePaused()) vetoDialog.show();
			});
		}else if(d instanceof PacketServerPauseGame) {
			MainActivity.setGamePaused(true);
			if(currentActionDialog != null) currentActionDialog.getDialog().dismiss();
		}else if(d instanceof PacketServerUnpauseGame) {
			MainActivity.setGamePaused(false);
		}else if(d instanceof PacketServerEventLogEntry) {
			MainActivity.addEventLogEntry(((PacketServerEventLogEntry) d).getMessage());
			fr.addEventLogEntry(((PacketServerEventLogEntry) d).getMessage());
		}
	}

	private ActionDialog showActionDialog(AlertDialog dialog, View view) {
		Fragment f = MainActivity.getCurrentFragment();

		if(currentActionDialog != null) currentActionDialog.dismiss();

		MovableFloatingActionButton mfab = new MovableFloatingActionButton(f.getContext());
		mfab.setImageDrawable(ResourcesCompat.getDrawable(f.getResources(), R.drawable.ic_exclamation, null));
		mfab.setBackgroundTintList(ColorStateList.valueOf(ResourcesCompat.getColor(f.getResources(), R.color.alert, null)));
		ViewGroup root = f.getView().getRootView().findViewById(R.id.root_container);
		root.addView(mfab);

		mfab.post(() -> {
			mfab.animate().setDuration(100).x(f.getResources().getDisplayMetrics().widthPixels - mfab.getWidth());
			mfab.animate().setDuration(100).y(f.getResources().getDisplayMetrics().heightPixels / 2 - mfab.getHeight() / 2);
		});

		mfab.setOnClickListener(v -> {
			if(!MainActivity.isGamePaused()) dialog.show();
		});

		Button btn = view.findViewById(R.id.dialog_hide);
		btn.setOnClickListener(v -> dialog.dismiss());

		ActionDialog ad = new ActionDialog(mfab, dialog);
		currentActionDialog = ad;
		return ad;
	}

	private void showPickPlayerDialog(@NonNull String message, @Nullable Predicate<Player> playerFilter, @NonNull Consumer<Player> callback) {
		runOnUiThread(() -> {
			LayoutInflater inf = MainActivity.getCurrentFragment().getLayoutInflater();
			View v2 = inf.inflate(R.layout.pick_player, null);
			AlertDialog selectDialog = new AlertDialog.Builder(MainActivity.getCurrentFragment().getContext())
					.setView(v2)
					.setCancelable(false)
					.create();

			ActionDialog ad = showActionDialog(selectDialog, v2);

			ListView lv = v2.findViewById(R.id.chancellor_players);

			List<Player> players = new ArrayList<>();
			for(Player pl : MainActivity.getRoom().getPlayers()) {
				if(playerFilter != null && !playerFilter.test(pl)) continue;
				players.add(pl);
			}

			lv.setAdapter(new ChancellorAdapter(MainActivity.getCurrentFragment().getContext(), players, player -> {
				ad.dismiss();
				callback.accept(player);
			}));

			TextView tv = v2.findViewById(R.id.pick_text);
			tv.setText(message);

			selectDialog.show();
		});
	}

	private void showCardsDialog(@NonNull String message, List<GamePolicyCard> cards, boolean dismiss, @Nullable Consumer<Integer> callback) {
		runOnUiThread(() -> {
			LayoutInflater inf = MainActivity.getCurrentFragment().getLayoutInflater();
			View v = inf.inflate(R.layout.pick_cards, null);
			AlertDialog pickDialog = new AlertDialog.Builder(MainActivity.getCurrentFragment().getContext())
					.setView(v)
					.setCancelable(false)
					.create();

			ActionDialog ad = showActionDialog(pickDialog, v);

			ImageView card1 = v.findViewById(R.id.pick_card1);
			ImageView card2 = v.findViewById(R.id.pick_card2);
			ImageView card3 = v.findViewById(R.id.pick_card3);

			card1.setTag(R.string.card_active, true);
			card2.setTag(R.string.card_active, true);
			card3.setTag(R.string.card_active, true);

			View.OnClickListener onClick = view -> {
				if((boolean) view.getTag(R.string.card_active)) {
					view.setTag(R.string.card_active, false);
					((ImageView) view).setImageBitmap(GameAsset.ARTICLE_BACK.getBitmap());
				}else {
					view.setTag(R.string.card_active, true);
					((ImageView) view).setImageBitmap(GameAsset.valueOf(cards.get((int) view.getTag(R.string.card_index)).getParty().name() + "_ARTICLE").getBitmap());
				}
			};

			card1.setImageBitmap(GameAsset.valueOf(cards.get(0).getParty().name() + "_ARTICLE").getBitmap());
			card1.setTag(R.string.card_index, 0);
			if(dismiss) card1.setOnClickListener(onClick);

			card2.setImageBitmap(GameAsset.valueOf(cards.get(1).getParty().name() + "_ARTICLE").getBitmap());
			card2.setTag(R.string.card_index, 1);
			if(dismiss) card2.setOnClickListener(onClick);

			if(cards.size() < 3) {
				((ViewGroup) card3.getParent()).removeView(card3);
			}else {
				card3.setImageBitmap(GameAsset.valueOf(cards.get(2).getParty().name() + "_ARTICLE").getBitmap());
				card3.setTag(R.string.card_index, 2);
				if(dismiss) card3.setOnClickListener(onClick);
			}

			TextView pickCardsText = v.findViewById(R.id.pick_cards_text);
			pickCardsText.setText(message);

			Button confirmButton = v.findViewById(R.id.pick_cards_confirm);

			confirmButton.setOnClickListener(view -> {
				if(dismiss) {
					List<ImageView> vs = new ArrayList<>(Arrays.asList(card1, card2, card3));
					List<Integer> sel = new ArrayList<>();
					for (int i = 0; i < vs.size(); i++) {
						if (!(boolean) vs.get(i).getTag(R.string.card_active)) sel.add(i);
					}

					if (sel.size() != 1) {
						confirmButton.post(() -> Toast.makeText(MainActivity.getCurrentFragment().getContext(), "You need to select exactly 1 card to dismiss", Toast.LENGTH_LONG).show());
						return;
					}

					Objects.requireNonNull(callback).accept(sel.get(0));
				}

				ad.dismiss();
				currentActionDialog = null;
			});

			if(!MainActivity.isGamePaused()) pickDialog.show();
		});
	}

	public void quit() {
		if(currentSnackbar != null) currentSnackbar.dismiss();
		if(currentAlert != null) currentAlert.dismiss();
		if(currentActionDialog != null) currentActionDialog.dismiss();
		// TODO: test quit
	}

	private void runOnUiThread(Runnable run) {
		new Handler(Looper.getMainLooper()).post(run);
	}
}
