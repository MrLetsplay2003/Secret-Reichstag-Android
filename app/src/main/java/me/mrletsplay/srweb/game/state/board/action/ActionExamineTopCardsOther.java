package me.mrletsplay.srweb.game.state.board.action;

import me.mrletsplay.srweb.game.state.board.action.GameActionData;

public class ActionExamineTopCardsOther extends GameActionData {

	private String playerID;

	public ActionExamineTopCardsOther() {}

	public void setPlayerID(String value) {
		this.playerID = value;
	}

}