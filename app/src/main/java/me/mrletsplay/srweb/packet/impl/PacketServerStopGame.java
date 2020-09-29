package me.mrletsplay.srweb.packet.impl;

import org.json.JSONObject;

import me.mrletsplay.srweb.packet.PacketData;
import me.mrletsplay.srweb.game.state.GameParty;

public class PacketServerStopGame extends PacketData {

	private GameParty winner;
	private JSONObject roles;

	public PacketServerStopGame() {}

	public GameParty getWinner() {
		return this.winner;
	}

	public JSONObject getRoles() {
		return this.roles;
	}

}