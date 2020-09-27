package me.mrletsplay.srweb.packet.impl;

import me.mrletsplay.srweb.packet.PacketData;

public class PacketClientDiscardCard extends PacketData {

	private int discardIndex;

	public PacketClientDiscardCard() {}

	public void setDiscardIndex(int value) {
		this.discardIndex = value;
	}

}