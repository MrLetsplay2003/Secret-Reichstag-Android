package me.mrletsplay.srweb.packet.impl;

import me.mrletsplay.srweb.packet.PacketData;
import me.mrletsplay.srweb.game.Player;

public class PacketServerPlayerLeft extends PacketData {

	private Player player;
	private boolean hardLeave;

	public PacketServerPlayerLeft() {}

	public Player getPlayer() {
		return this.player;
	}

	public boolean isHardLeave() {
		return this.hardLeave;
	}

}