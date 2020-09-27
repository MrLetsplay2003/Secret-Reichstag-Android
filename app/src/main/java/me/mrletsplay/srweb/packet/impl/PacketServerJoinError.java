package me.mrletsplay.srweb.packet.impl;

import me.mrletsplay.srweb.packet.PacketData;

public class PacketServerJoinError extends PacketData {

	private String message;

	public PacketServerJoinError() {}

	public String getMessage() {
		return this.message;
	}

}