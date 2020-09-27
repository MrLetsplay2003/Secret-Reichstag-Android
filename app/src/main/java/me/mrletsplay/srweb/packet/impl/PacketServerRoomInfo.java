package me.mrletsplay.srweb.packet.impl;

import me.mrletsplay.srweb.game.Room;
import me.mrletsplay.srweb.packet.PacketData;
import me.mrletsplay.srweb.game.Player;

public class PacketServerRoomInfo extends PacketData {

	private String sessionID;
	private Player selfPlayer;
	private Room room;
	private boolean voteDone;

	public PacketServerRoomInfo() {}

	public String getSessionID() {
		return this.sessionID;
	}

	public Player getSelfPlayer() {
		return this.selfPlayer;
	}

	public Room getRoom() {
		return this.room;
	}

	public boolean isVoteDone() {
		return this.voteDone;
	}

}