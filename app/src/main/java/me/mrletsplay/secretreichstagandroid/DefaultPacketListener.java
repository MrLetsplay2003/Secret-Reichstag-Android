package me.mrletsplay.secretreichstagandroid;

import android.drm.DrmStore;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.util.Consumer;
import androidx.core.util.Predicate;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import me.mrletsplay.secretreichstagandroid.fragment.GameFragment;
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
import me.mrletsplay.srweb.packet.impl.PacketServerPickCards;
import me.mrletsplay.srweb.packet.impl.PacketServerPlayerAction;
import me.mrletsplay.srweb.packet.impl.PacketServerPlayerJoined;
import me.mrletsplay.srweb.packet.impl.PacketServerPlayerLeft;
import me.mrletsplay.srweb.packet.impl.PacketServerStartGame;
import me.mrletsplay.srweb.packet.impl.PacketServerStopGame;
import me.mrletsplay.srweb.packet.impl.PacketServerUpdateGameState;
import me.mrletsplay.srweb.packet.impl.PacketServerVeto;
import me.mrletsplay.srweb.packet.impl.PacketServerVoteResults;

public class DefaultPacketListener implements PacketListener {

	private AlertDialog currentAlert;
	private Snackbar currentSnackbar;

	@Override
	public void onPacketReceived(Packet packet) {
		PacketData d = packet.getData();
		System.out.println(d);
		GameFragment fr = (GameFragment) MainActivity.getCurrentFragment();

		if(d instanceof PacketServerPlayerJoined) {
			PacketServerPlayerJoined j = (PacketServerPlayerJoined) d;
			if(!j.isRejoin()) MainActivity.getRoom().getPlayers().add(j.getPlayer());
			fr.addPlayer(j.getPlayer(), true);
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

				fr.addPlayer(l.getPlayer(), false);
			}
		}else if(d instanceof PacketServerStartGame) {
			MainActivity.getRoom().setGameRunning(true);

			PacketServerStartGame s = (PacketServerStartGame) d;
			MainActivity.setLeader(s.getLeader());
			MainActivity.setSelfRole(s.getRole());
			MainActivity.setTeammates(s.getTeammates());

			fr.updateAll();
		}else if(d instanceof PacketServerUpdateGameState) {
			GameState newState = ((PacketServerUpdateGameState) d).getNewState();
			MainActivity.getRoom().setGameState(newState);
			fr.updateAll();

			if(newState.getMoveState() != GameMoveState.VOTE) MainActivity.setSelfVoted(false);

			if(newState.getMoveState() == GameMoveState.VOTE && !MainActivity.isSelfVoted() && !MainActivity.isPlayerDead(MainActivity.getSelfPlayer())) {
				runOnUiThread(() -> {
					LayoutInflater inf = MainActivity.getCurrentFragment().getLayoutInflater();
					View v = inf.inflate(R.layout.vote, null);
					AlertDialog voteDialog = new AlertDialog.Builder(MainActivity.getCurrentFragment().getContext())
							.setView(v)
							.setCancelable(false)
							.create();

					currentAlert = voteDialog;

					TextView u1 = v.findViewById(R.id.vote_username_president);
					u1.setText(newState.getPresident().getName());

					TextView u2 = v.findViewById(R.id.vote_username_chancellor);
					u2.setText(newState.getChancellor().getName());

					Button voteYes = v.findViewById(R.id.vote_yes);
					voteYes.setOnClickListener(view -> {
						voteDialog.dismiss();
						currentAlert = null;
						PacketClientVote vote = new PacketClientVote();
						vote.setYes(true);
						Networking.sendPacket(Packet.of(vote));
					});

					Button voteNo = v.findViewById(R.id.vote_no);
					voteNo.setOnClickListener(view -> {
						voteDialog.dismiss();
						currentAlert = null;
						PacketClientVote vote = new PacketClientVote();
						vote.setYes(false);
						Networking.sendPacket(Packet.of(vote));
					});

					voteDialog.show();
				});
			}else if(newState.getMoveState() == GameMoveState.SELECT_CHANCELLOR && newState.getPresident().getID().equals(MainActivity.getSelfPlayer().getID())) {
				showPickPlayerDialog("Select a player to be the next chancellor",  pl ->
						!MainActivity.isPlayerDead(pl)
						&& (newState.getPreviousPresident() == null || !newState.getPreviousPresident().getID().equals(pl.getID()))
						&& (newState.getPreviousChancellor() == null || !newState.getPreviousChancellor().getID().equals(pl.getID()))
						&& !pl.getID().equals(MainActivity.getSelfPlayer().getID()),
						player -> {
							PacketClientSelectChancellor ch = new PacketClientSelectChancellor();
							ch.setPlayerID(player.getID());
							Networking.sendPacket(Packet.of(ch));
						});
			}else if(newState.getMoveState() == GameMoveState.DRAW_CARDS && newState.getPresident().getID().equals(MainActivity.getSelfPlayer().getID())) {
				runOnUiThread(() -> {
					AlertDialog drawDialog = new AlertDialog.Builder(MainActivity.getCurrentFragment().getContext())
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
				});
			}
		}else if(d instanceof PacketServerVoteResults) {
			System.out.println("VOTE RESULTS");
			PacketServerVoteResults vr = (PacketServerVoteResults) d;
			MainActivity.setVoteResults(vr.getVotes());
			fr.updateAll();

			currentSnackbar = Snackbar.make(MainActivity.getCurrentFragment().getView().findViewById(R.id.player_list_container), "Vote results are shown", Snackbar.LENGTH_INDEFINITE)
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
					showPickPlayerDialog("Select a player to view the top three cards", pl -> !MainActivity.getSelfPlayer().getID().equals(pl.getID()), pl -> {
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
					showPickPlayerDialog("Select a player to be killed", pl -> !MainActivity.getSelfPlayer().getID().equals(pl.getID()), pl -> {
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
					showPickPlayerDialog("Select a player to be the next president", pl -> !MainActivity.getSelfPlayer().getID().equals(pl.getID()), pl -> {
						PacketClientPerformAction pa = new PacketClientPerformAction();
						ActionPickPresident k = new ActionPickPresident();
						k.setPlayerID(pl.getID());
						pa.setData(k);
						Networking.sendPacket(Packet.of(pa));
					});
					break;
				}
				case INSPECT_PLAYER: // TODO: test
				{
					showPickPlayerDialog("Select the player you want to inspect", pl -> !MainActivity.getSelfPlayer().getID().equals(pl.getID()), pl -> {
						PacketClientPerformAction pa = new PacketClientPerformAction();
						ActionInspectPlayer k = new ActionInspectPlayer();
						k.setPlayerID(pl.getID());
						pa.setData(k);
						Networking.sendPacket(Packet.of(pa)).thenAccept(res -> {
							ActionInspectPlayerResult r = (ActionInspectPlayerResult) res.getData();
							runOnUiThread(() -> {
								LayoutInflater inf = MainActivity.getCurrentFragment().getLayoutInflater();
								View v = inf.inflate(R.layout.inspect_player, null);
								AlertDialog inspectDialog = new AlertDialog.Builder(MainActivity.getCurrentFragment().getContext())
										.setView(v)
										.setCancelable(false)
										.create();

								currentAlert = inspectDialog;

								TextView u1 = v.findViewById(R.id.inspect_username);
								u1.setText(pl.getName());

								TextView u2 = v.findViewById(R.id.inspect_role);
								u2.setText(r.getParty().getFriendlyNameSingular());

								Button okay = v.findViewById(R.id.inspect_okay);
								okay.setOnClickListener(view -> {
									inspectDialog.dismiss();
									currentAlert = null;
								});

								inspectDialog.show();
							});
						});
					});
					break;
				}
				case BLOCK_PLAYER:
				{
					// TODO: check if president/chancellor (blocked next round anyway)
					showPickPlayerDialog("Select a player to be unelectable the next turn", pl -> !MainActivity.getSelfPlayer().getID().equals(pl.getID()), pl -> {
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
			if(currentAlert != null) currentAlert.dismiss();
			if(currentSnackbar != null) currentSnackbar.dismiss();
		}else if(d instanceof PacketServerVeto) {
			runOnUiThread(() -> {
				LayoutInflater inf = MainActivity.getCurrentFragment().getLayoutInflater();
				View v = inf.inflate(R.layout.veto, null);
				AlertDialog vetoDialog = new AlertDialog.Builder(MainActivity.getCurrentFragment().getContext())
						.setView(v)
						.setCancelable(false)
						.create();

				currentAlert = vetoDialog;

				TextView u1 = v.findViewById(R.id.veto_username);
				u1.setText(MainActivity.getRoom().getGameState().getChancellor().getName());

				Button vetoYes = v.findViewById(R.id.veto_yes);
				vetoYes.setOnClickListener(view -> {
					vetoDialog.dismiss();
					currentAlert = null;
					PacketClientVeto veto = new PacketClientVeto();
					veto.setAcceptVeto(true);
					Networking.sendPacket(Packet.of(veto));
				});

				Button vetoNo = v.findViewById(R.id.veto_no);
				vetoNo.setOnClickListener(view -> {
					vetoDialog.dismiss();
					currentAlert = null;
					PacketClientVeto veto = new PacketClientVeto();
					veto.setAcceptVeto(false);
					Networking.sendPacket(Packet.of(veto));
				});

				vetoDialog.show();
			});
		}

		// TODO: (Un-)PauseGame
	}

	private void showPickPlayerDialog(@NonNull String message, @Nullable Predicate<Player> playerFilter, @NonNull Consumer<Player> callback) {
		runOnUiThread(() -> {
			LayoutInflater inf = MainActivity.getCurrentFragment().getLayoutInflater();
			View v2 = inf.inflate(R.layout.pick_player, null);
			AlertDialog selectDialog = new AlertDialog.Builder(MainActivity.getCurrentFragment().getContext())
					.setView(v2)
					.setCancelable(false)
					.create();

			ListView lv = v2.findViewById(R.id.chancellor_players);

			List<Player> players = new ArrayList<>();
			for(Player pl : MainActivity.getRoom().getPlayers()) {
				if(playerFilter != null && !playerFilter.test(pl)) continue;
				players.add(pl);
			}

			lv.setAdapter(new ChancellorAdapter(MainActivity.getCurrentFragment().getContext(), players, player -> {
				selectDialog.dismiss();
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
			View v2 = inf.inflate(R.layout.pick_cards, null);
			AlertDialog pickDialog = new AlertDialog.Builder(MainActivity.getCurrentFragment().getContext())
					.setView(v2)
					.setCancelable(false)
					.create();

			ImageView card1 = v2.findViewById(R.id.pick_card1);
			ImageView card2 = v2.findViewById(R.id.pick_card2);
			ImageView card3 = v2.findViewById(R.id.pick_card3);

			card1.setTag(R.string.card_active, true);
			card2.setTag(R.string.card_active, true);
			card3.setTag(R.string.card_active, true);

			View.OnClickListener onClick = v -> {
				if((boolean) v.getTag(R.string.card_active)) {
					v.setTag(R.string.card_active, false);
					((ImageView) v).setImageBitmap(GameAsset.ARTICLE_BACK.getBitmap());
				}else {
					v.setTag(R.string.card_active, true);
					((ImageView) v).setImageBitmap(GameAsset.valueOf(cards.get((int) v.getTag(R.string.card_index)).getParty().name() + "_ARTICLE").getBitmap());
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

			TextView pickCardsText = v2.findViewById(R.id.pick_cards_text);
			pickCardsText.setText(message);

			Button confirmButton = v2.findViewById(R.id.pick_cards_confirm);

			confirmButton.setOnClickListener(v -> {
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

				pickDialog.dismiss();
			});

			pickDialog.show();
		});
	}

	private void runOnUiThread(Runnable run) {
		new Handler(Looper.getMainLooper()).post(run);
	}
}
