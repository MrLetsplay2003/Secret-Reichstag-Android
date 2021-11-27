package me.mrletsplay.srweb.game.state.board;

import me.mrletsplay.srweb.game.state.board.GameBoardAction;

public class GameBoardActionField {

	private int fieldIndex;
	private GameBoardAction action;

	public GameBoardActionField() {}

	public void setFieldIndex(int value) {
		this.fieldIndex = value;
	}

	public int getFieldIndex() {
		return this.fieldIndex;
	}

	public void setAction(GameBoardAction value) {
		this.action = value;
	}

	public GameBoardAction getAction() {
		return this.action;
	}

}