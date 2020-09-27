package me.mrletsplay.srweb.packet.impl;

import me.mrletsplay.srweb.packet.PacketData;

public class PacketClientSelectChancellor extends PacketData {

	private String playerID;

	public PacketClientSelectChancellor() {}

	public void setPlayerID(String value) {
		this.playerID = value;
	}

}