package me.mrletsplay.srweb.packet.impl;

import me.mrletsplay.srweb.packet.PacketData;

public class PacketClientVote extends PacketData {

	private boolean yes;

	public PacketClientVote() {}

	public void setYes(boolean value) {
		this.yes = value;
	}

}