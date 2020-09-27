package me.mrletsplay.srweb.packet.impl;

import me.mrletsplay.srweb.game.state.board.GameBoardAction;
import me.mrletsplay.srweb.packet.PacketData;
import me.mrletsplay.srweb.game.state.board.action.GameActionData;

public class PacketServerPlayerAction extends PacketData {

	private GameBoardAction action;
	private GameActionData data;

	public PacketServerPlayerAction() {}

	public GameBoardAction getAction() {
		return this.action;
	}

	public GameActionData getData() {
		return this.data;
	}

}