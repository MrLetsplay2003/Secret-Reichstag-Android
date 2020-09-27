package me.mrletsplay.srweb.game.state.board.action;

import me.mrletsplay.srweb.packet.PacketData;
import me.mrletsplay.srweb.game.state.GameParty;

public class ActionInspectPlayerResult extends PacketData {

	private GameParty party;

	public ActionInspectPlayerResult() {}

	public GameParty getParty() {
		return this.party;
	}

}