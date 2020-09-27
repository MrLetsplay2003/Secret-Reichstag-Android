package me.mrletsplay.srweb.packet;

import java.util.UUID;

public class Packet {
	
	private String id;
	private String referrerID;
	private boolean success;
	private PacketData data;
	private String errorMessage;

	public Packet() {}

	public Packet(String id, String referrerID, boolean success, PacketData data, String errorMessage) {
		this.id = id;
		this.referrerID = referrerID;
		this.success = success;
		this.data = data;
		this.errorMessage = errorMessage;
	}
	
	public Packet(String referrerID, PacketData data) {
		this(randomID(), referrerID, true, data, null);
	}
	
	public Packet(String referrerID, String errorMessage) {
		this(randomID(), referrerID, false, null, errorMessage);
	}
	
	public Packet(PacketData data) {
		this(randomID(), null, true, data, null);
	}
	
	public String getID() {
		return id;
	}
	
	public String getReferrerID() {
		return referrerID;
	}
	
	public boolean isSuccess() {
		return success;
	}
	
	public PacketData getData() {
		return data;
	}
	
	public String getErrorMessage() {
		return errorMessage;
	}
	
	private static String randomID() {
		return UUID.randomUUID().toString().replace("-", "");
	}

	public static Packet of(PacketData data) {
		return new Packet(data);
	}

}
