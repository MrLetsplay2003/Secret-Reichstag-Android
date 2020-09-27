package me.mrletsplay.srweb.packet.impl;

import me.mrletsplay.srweb.packet.PacketData;
import me.mrletsplay.srweb.game.RoomSettings;

public class PacketClientConnect extends PacketData {

	private String playerName;
	private boolean createRoom;
	private String roomID;
	private String roomName;
	private RoomSettings roomSettings;
	private String sessionID;

	public PacketClientConnect() {}

	public void setPlayerName(String value) {
		this.playerName = value;
	}

	public void setCreateRoom(boolean value) {
		this.createRoom = value;
	}

	public void setRoomID(String value) {
		this.roomID = value;
	}

	public void setRoomName(String value) {
		this.roomName = value;
	}

	public void setRoomSettings(RoomSettings value) {
		this.roomSettings = value;
	}

	public void setSessionID(String value) {
		this.sessionID = value;
	}

}