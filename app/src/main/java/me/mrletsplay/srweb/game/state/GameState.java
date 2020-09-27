package me.mrletsplay.srweb.game.state;

import java.util.List;
import me.mrletsplay.srweb.game.state.board.GameBoard;
import me.mrletsplay.srweb.game.state.board.GameBoardAction;
import me.mrletsplay.srweb.game.state.GameMoveState;
import me.mrletsplay.srweb.game.Player;

public class GameState {

	private GameBoard liberalBoard;
	private GameBoard communistBoard;
	private GameBoard fascistBoard;
	private GameMoveState moveState;
	private Player president;
	private Player chancellor;
	private Player previousPresident;
	private Player previousChancellor;
	private Player blockedPlayer;
	private Player actionPerformer;
	private GameBoardAction action;
	private List<Player> deadPlayers;
	private int failedElections;
	private boolean vetoPowerUnlocked;
	private boolean vetoRequested;
	private boolean vetoBlocked;
	private List<Player> notStalinConfirmed;
	private List<Player> notHitlerConfirmed;

	private int drawPileSize;

	public GameState() {}

	public GameBoard getLiberalBoard() {
		return this.liberalBoard;
	}

	public GameBoard getCommunistBoard() {
		return this.communistBoard;
	}

	public GameBoard getFascistBoard() {
		return this.fascistBoard;
	}

	public GameMoveState getMoveState() {
		return this.moveState;
	}

	public Player getPresident() {
		return this.president;
	}

	public Player getChancellor() {
		return this.chancellor;
	}

	public Player getPreviousPresident() {
		return this.previousPresident;
	}

	public Player getPreviousChancellor() {
		return this.previousChancellor;
	}

	public Player getBlockedPlayer() {
		return this.blockedPlayer;
	}

	public Player getActionPerformer() {
		return this.actionPerformer;
	}

	public GameBoardAction getAction() {
		return this.action;
	}

	public List<Player> getDeadPlayers() {
		return this.deadPlayers;
	}

	public int getFailedElections() {
		return this.failedElections;
	}

	public boolean isVetoPowerUnlocked() {
		return this.vetoPowerUnlocked;
	}

	public boolean isVetoRequested() {
		return this.vetoRequested;
	}

	public boolean isVetoBlocked() {
		return this.vetoBlocked;
	}

	public List<Player> getNotStalinConfirmed() {
		return this.notStalinConfirmed;
	}

	public List<Player> getNotHitlerConfirmed() {
		return this.notHitlerConfirmed;
	}

	public int getDrawPileSize() {
		return this.drawPileSize;
	}

}