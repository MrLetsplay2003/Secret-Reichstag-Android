package me.mrletsplay.srweb.packet.impl;

import java.util.List;
import me.mrletsplay.srweb.game.Room;
import me.mrletsplay.srweb.packet.PacketData;

public class PacketServerRoomList extends PacketData {

	private List<Room> rooms;

	public PacketServerRoomList() {}

	public List<Room> getRooms() {
		return this.rooms;
	}

}