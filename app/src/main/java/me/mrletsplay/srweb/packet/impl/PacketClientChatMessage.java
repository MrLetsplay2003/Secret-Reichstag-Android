package me.mrletsplay.srweb.packet.impl;

import me.mrletsplay.srweb.packet.PacketData;

public class PacketClientChatMessage extends PacketData {

	private String message;

	public PacketClientChatMessage() {}

	public void setMessage(String value) {
		this.message = value;
	}

}