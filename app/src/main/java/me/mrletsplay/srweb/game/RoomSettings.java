package me.mrletsplay.srweb.game;


public class RoomSettings {

	private String mode;
	private int playerCount;
	private int liberalCardCount;
	private int communistCardCount;
	private int fascistCardCount;

	public RoomSettings() {}

	public void setMode(String value) {
		this.mode = value;
	}

	public String getMode() {
		return this.mode;
	}

	public void setPlayerCount(int value) {
		this.playerCount = value;
	}

	public int getPlayerCount() {
		return this.playerCount;
	}

	public void setLiberalCardCount(int value) {
		this.liberalCardCount = value;
	}

	public int getLiberalCardCount() {
		return this.liberalCardCount;
	}

	public void setCommunistCardCount(int value) {
		this.communistCardCount = value;
	}

	public int getCommunistCardCount() {
		return this.communistCardCount;
	}

	public void setFascistCardCount(int value) {
		this.fascistCardCount = value;
	}

	public int getFascistCardCount() {
		return this.fascistCardCount;
	}

}