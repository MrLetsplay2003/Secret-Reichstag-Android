package me.mrletsplay.srweb.game;

public enum GameMode {

	SECRET_HITLER(5, 10),
	SECRET_REICHSTAG(7, 14),
	;

	private int minPlayers;
	private int maxPlayers;

	private GameMode(int minPlayers, int maxPlayers) {
		this.minPlayers = minPlayers;
		this.maxPlayers = maxPlayers;
	}

	public int getMinPlayers() {
		return this.minPlayers;
	}

	public int getMaxPlayers() {
		return this.maxPlayers;
	}

}