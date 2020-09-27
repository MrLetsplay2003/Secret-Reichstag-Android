package me.mrletsplay.srweb.packet.impl;

import me.mrletsplay.srweb.packet.PacketData;
import me.mrletsplay.srweb.game.state.GameParty;

public class PacketServerStopGame extends PacketData {

	private GameParty winner;

	public PacketServerStopGame() {}

	public GameParty getWinner() {
		return this.winner;
	}

}