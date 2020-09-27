package me.mrletsplay.srweb.game;

import java.util.List;
import me.mrletsplay.srweb.game.state.GameState;
import me.mrletsplay.srweb.game.state.GameParty;
import me.mrletsplay.srweb.game.RoomSettings;
import me.mrletsplay.srweb.game.GameMode;
import me.mrletsplay.srweb.game.Player;

public class Room {

	private String id;
	private String name;
	private GameMode mode;
	private List<Player> players;
	private GameState gameState;
	private boolean gameRunning;
	private boolean gamePaused;
	private RoomSettings settings;
	private GameParty winner;

	public Room() {}

	public String getID() {
		return this.id;
	}

	public String getName() {
		return this.name;
	}

	public GameMode getMode() {
		return this.mode;
	}

	public List<Player> getPlayers() {
		return this.players;
	}

	public void setGameState(GameState value) {
		this.gameState = value;
	}

	public GameState getGameState() {
		return this.gameState;
	}

	public void setGameRunning(boolean value) {
		this.gameRunning = value;
	}

	public boolean isGameRunning() {
		return this.gameRunning;
	}

	public void setGamePaused(boolean value) {
		this.gamePaused = value;
	}

	public boolean isGamePaused() {
		return this.gamePaused;
	}

	public RoomSettings getSettings() {
		return this.settings;
	}

	public GameParty getWinner() {
		return this.winner;
	}

}