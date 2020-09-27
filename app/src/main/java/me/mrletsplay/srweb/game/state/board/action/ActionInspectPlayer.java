package me.mrletsplay.srweb.game.state.board.action;

import me.mrletsplay.srweb.game.state.board.action.GameActionData;

public class ActionInspectPlayer extends GameActionData {

	private String playerID;

	public ActionInspectPlayer() {}

	public void setPlayerID(String value) {
		this.playerID = value;
	}

}