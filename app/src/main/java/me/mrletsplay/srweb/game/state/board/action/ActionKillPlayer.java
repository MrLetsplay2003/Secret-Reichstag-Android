package me.mrletsplay.srweb.game.state.board.action;

import me.mrletsplay.srweb.game.state.board.action.GameActionData;

public class ActionKillPlayer extends GameActionData {

	private String playerID;

	public ActionKillPlayer() {}

	public void setPlayerID(String value) {
		this.playerID = value;
	}

}