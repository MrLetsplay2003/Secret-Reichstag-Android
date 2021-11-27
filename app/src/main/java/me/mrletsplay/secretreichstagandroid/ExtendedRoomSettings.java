package me.mrletsplay.secretreichstagandroid;

import me.mrletsplay.srweb.game.RoomSettings;

public class ExtendedRoomSettings {

	private String roomName;
	private RoomSettings roomSettings;

	public ExtendedRoomSettings() {
		this.roomSettings = new RoomSettings();
	}

	public void setRoomName(String roomName) {
		this.roomName = roomName;
	}

	public String getRoomName() {
		return roomName;
	}

	public RoomSettings getRoomSettings() {
		return roomSettings;
	}
}
