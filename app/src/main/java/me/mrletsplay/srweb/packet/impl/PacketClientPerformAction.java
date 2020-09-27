package me.mrletsplay.srweb.packet.impl;

import me.mrletsplay.srweb.packet.PacketData;
import me.mrletsplay.srweb.game.state.board.action.GameActionData;

public class PacketClientPerformAction extends PacketData {

	private GameActionData data;

	public PacketClientPerformAction() {}

	public void setData(GameActionData value) {
		this.data = value;
	}

}