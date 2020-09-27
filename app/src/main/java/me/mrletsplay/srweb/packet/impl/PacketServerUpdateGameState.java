package me.mrletsplay.srweb.packet.impl;

import me.mrletsplay.srweb.game.state.GameState;
import me.mrletsplay.srweb.packet.PacketData;

public class PacketServerUpdateGameState extends PacketData {

	private GameState newState;

	public PacketServerUpdateGameState() {}

	public GameState getNewState() {
		return this.newState;
	}

}