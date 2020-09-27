package me.mrletsplay.srweb.game.state.board;

import me.mrletsplay.srweb.game.state.board.GameBoardAction;

public class GameBoardActionField {

	private int fieldIndex;
	private GameBoardAction action;

	public GameBoardActionField() {}

	public int getFieldIndex() {
		return this.fieldIndex;
	}

	public GameBoardAction getAction() {
		return this.action;
	}

}