package me.mrletsplay.srweb.packet.impl;

import me.mrletsplay.srweb.packet.PacketData;
import me.mrletsplay.srweb.game.Player;

public class PacketServerPlayerJoined extends PacketData {

	private Player player;
	private boolean rejoin;

	public PacketServerPlayerJoined() {}

	public Player getPlayer() {
		return this.player;
	}

	public boolean isRejoin() {
		return this.rejoin;
	}

}