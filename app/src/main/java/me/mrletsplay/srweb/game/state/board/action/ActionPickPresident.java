package me.mrletsplay.srweb.game.state.board.action;

import me.mrletsplay.srweb.game.state.board.action.GameActionData;

public class ActionPickPresident extends GameActionData {

	private String playerID;

	public ActionPickPresident() {}

	public void setPlayerID(String value) {
		this.playerID = value;
	}

}