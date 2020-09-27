package me.mrletsplay.srweb.game.state.board.action;

import me.mrletsplay.srweb.game.state.board.action.GameActionData;

public class ActionBlockPlayer extends GameActionData {

	private String playerID;

	public ActionBlockPlayer() {}

	public void setPlayerID(String value) {
		this.playerID = value;
	}

}