package me.mrletsplay.srweb.packet.impl;

import me.mrletsplay.srweb.packet.PacketData;

public class PacketClientVeto extends PacketData {

	private boolean acceptVeto;

	public PacketClientVeto() {}

	public void setAcceptVeto(boolean value) {
		this.acceptVeto = value;
	}

	public boolean isAcceptVeto() {
		return this.acceptVeto;
	}

}