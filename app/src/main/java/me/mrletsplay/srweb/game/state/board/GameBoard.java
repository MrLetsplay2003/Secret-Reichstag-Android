package me.mrletsplay.srweb.game.state.board;

import java.util.List;
import me.mrletsplay.srweb.game.state.board.GameBoardActionField;

public class GameBoard {

	private int numCards;
	private int maxCards;
	private List<GameBoardActionField> actionFields;
	private boolean isCustom;

	public GameBoard() {}

	public int getNumCards() {
		return this.numCards;
	}

	public int getMaxCards() {
		return this.maxCards;
	}

	public List<GameBoardActionField> getActionFields() {
		return this.actionFields;
	}

	public boolean isCustom() {
		return this.isCustom;
	}

}