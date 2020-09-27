package me.mrletsplay.srweb.packet.impl;

import me.mrletsplay.srweb.packet.PacketData;

public class PacketServerEventLogEntry extends PacketData {

	private String message;
	private boolean chatMessage;

	public PacketServerEventLogEntry() {}

	public String getMessage() {
		return this.message;
	}

	public boolean isChatMessage() {
		return this.chatMessage;
	}

}