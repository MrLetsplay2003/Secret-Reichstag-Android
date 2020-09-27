package me.mrletsplay.srweb.packet.impl;

import org.json.JSONObject;

import me.mrletsplay.srweb.packet.PacketData;

public class PacketServerVoteResults extends PacketData {

	private JSONObject votes;

	public PacketServerVoteResults() {}

	public JSONObject getVotes() {
		return this.votes;
	}

}