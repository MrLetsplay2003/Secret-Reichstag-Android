package me.mrletsplay.srweb.game;


public class Player {

	private String id;
	private String name;

	private boolean online;

	public Player() {}

	public String getID() {
		return this.id;
	}

	public String getName() {
		return this.name;
	}

	public void setOnline(boolean value) {
		this.online = value;
	}

	public boolean isOnline() {
		return this.online;
	}

}