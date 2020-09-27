package me.mrletsplay.secretreichstagandroid;

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

import androidx.appcompat.app.AlertDialog;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import me.mrletsplay.secretreichstagandroid.fragment.GameFragment;
import me.mrletsplay.srweb.game.Player;
import me.mrletsplay.srweb.game.state.GameMoveState;
import me.mrletsplay.srweb.game.state.GamePolicyCard;
import me.mrletsplay.srweb.game.state.GameState;
import me.mrletsplay.srweb.packet.Packet;
import me.mrletsplay.srweb.packet.PacketData;
import me.mrletsplay.srweb.packet.impl.PacketClientDiscardCard;
import me.mrletsplay.srweb.packet.impl.PacketClientDrawCards;
import me.mrletsplay.srweb.packet.impl.PacketClientSelectChancellor;
import me.mrletsplay.srweb.packet.impl.PacketClientVote;
import me.mrletsplay.srweb.packet.impl.PacketServerPickCards;
import me.mrletsplay.srweb.packet.impl.PacketServerPlayerJoined;
import me.mrletsplay.srweb.packet.impl.PacketServerPlayerLeft;
import me.mrletsplay.srweb.packet.impl.PacketServerStartGame;
import me.mrletsplay.srweb.packet.impl.PacketServerUpdateGameState;
import me.mrletsplay.srweb.packet.impl.PacketServerVoteResults;

public class DefaultPacketListener implements PacketListener {

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
				new Handler(Looper.getMainLooper()).post(() -> {
					LayoutInflater inf = MainActivity.getCurrentFragment().getLayoutInflater();
					View v = inf.inflate(R.layout.vote, null);
					AlertDialog voteDialog = new AlertDialog.Builder(MainActivity.getCurrentFragment().getContext())
							.setView(v)
							.setCancelable(false)
							.create();

					TextView u1 = v.findViewById(R.id.vote_username_president);
					u1.setText(newState.getPresident().getName());

					TextView u2 = v.findViewById(R.id.vote_username_chancellor);
					u2.setText(newState.getChancellor().getName());

					Button voteYes = v.findViewById(R.id.vote_yes);
					voteYes.setOnClickListener(view -> {
						voteDialog.dismiss();
						PacketClientVote vote = new PacketClientVote();
						vote.setYes(true);
						Networking.sendPacket(Packet.of(vote));
					});

					Button voteNo = v.findViewById(R.id.vote_no);
					voteNo.setOnClickListener(view -> {
						voteDialog.dismiss();
						PacketClientVote vote = new PacketClientVote();
						vote.setYes(false);
						Networking.sendPacket(Packet.of(vote));
					});

					voteDialog.show();
				});
			}else if(newState.getMoveState() == GameMoveState.SELECT_CHANCELLOR && newState.getPresident().getID().equals(MainActivity.getSelfPlayer().getID())) {
				new Handler(Looper.getMainLooper()).post(() -> {
					LayoutInflater inf = MainActivity.getCurrentFragment().getLayoutInflater();
					View v2 = inf.inflate(R.layout.pick_chancellor, null);
					AlertDialog selectDialog = new AlertDialog.Builder(MainActivity.getCurrentFragment().getContext())
							.setView(v2)
							.setCancelable(false)
							.create();

					ListView lv = v2.findViewById(R.id.chancellor_players);

					List<Player> players = new ArrayList<>();
					for(Player pl : MainActivity.getRoom().getPlayers()) {
						if(MainActivity.isPlayerDead(pl)
							|| (newState.getPreviousPresident() != null && newState.getPreviousPresident().getID().equals(pl.getID()))
							|| (newState.getPreviousChancellor() != null && newState.getPreviousChancellor().getID().equals(pl.getID()))
							|| pl.getID().equals(MainActivity.getSelfPlayer().getID())) {
							continue;
						}

						players.add(pl);
					}

					lv.setAdapter(new ChancellorAdapter(MainActivity.getCurrentFragment().getContext(), players, player -> {
						selectDialog.dismiss();
						PacketClientSelectChancellor ch = new PacketClientSelectChancellor();
						ch.setPlayerID(player.getID());
						Networking.sendPacket(Packet.of(ch));
					}));

					lv.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
						@Override
						public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
							System.out.println("SELECTED " + position);
						}

						@Override
						public void onNothingSelected(AdapterView<?> parent) {

						}
					});

					selectDialog.show();
				});
			}else if(newState.getMoveState() == GameMoveState.DRAW_CARDS && newState.getPresident().getID().equals(MainActivity.getSelfPlayer().getID())) {
				new Handler(Looper.getMainLooper()).post(() -> {
					AlertDialog drawDialog = new AlertDialog.Builder(MainActivity.getCurrentFragment().getContext())
							.setTitle("Draw cards")
							.setMessage("You need to draw some cards")
							.setPositiveButton("Draw", (dl, which) -> {
								dl.dismiss();
								Networking.sendPacket(Packet.of(new PacketClientDrawCards()));
							})
							.create();

					drawDialog.show();
				});
			}
		}else if(d instanceof PacketServerVoteResults) {
			PacketServerVoteResults vr = (PacketServerVoteResults) d;
			MainActivity.setVoteResults(vr.getVotes());
			fr.updateAll();

			Snackbar.make(MainActivity.getCurrentFragment().getView().findViewById(R.id.player_list_container), "Vote results are shown", Snackbar.LENGTH_INDEFINITE)
				.setAction("Dismiss", v -> {
					MainActivity.setVoteResults(null);
					fr.updateAll();
				})
				.show();
		}else if(d instanceof PacketServerPickCards) {
			new Handler(Looper.getMainLooper()).post(() -> {
				System.out.println("PICK CARDS");
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

				List<GamePolicyCard> cards = ((PacketServerPickCards) d).getCards();

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
				card1.setOnClickListener(onClick);

				card2.setImageBitmap(GameAsset.valueOf(cards.get(1).getParty().name() + "_ARTICLE").getBitmap());
				card2.setTag(R.string.card_index, 1);
				card2.setOnClickListener(onClick);

				if(cards.size() < 3) {
					((ViewGroup) card3.getParent()).removeView(card3);
				}else {
					card3.setImageBitmap(GameAsset.valueOf(cards.get(2).getParty().name() + "_ARTICLE").getBitmap());
					card3.setTag(R.string.card_index, 2);
					card3.setOnClickListener(onClick);
				}

				Button confirmButton = v2.findViewById(R.id.pick_cards_confirm);
				confirmButton.setOnClickListener(v -> {
					List<ImageView> vs = new ArrayList<>(Arrays.asList(card1, card2, card3));
					List<Integer> sel = new ArrayList<>();
					for(int i = 0; i < vs.size(); i++) {
						if(!(boolean) vs.get(i).getTag(R.string.card_active)) sel.add(i);
					}

					if(sel.size() != 1) {
						confirmButton.post(() -> Toast.makeText(MainActivity.getCurrentFragment().getContext(), "You need to select exactly 1 card to dismiss", Toast.LENGTH_LONG).show());
						return;
					}

					pickDialog.dismiss();
					PacketClientDiscardCard dc = new PacketClientDiscardCard();
					dc.setDiscardIndex(sel.get(0));
					Networking.sendPacket(Packet.of(dc));
				});

				pickDialog.show();
			});
		}
	}
}
