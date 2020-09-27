package me.mrletsplay.srweb.game.state.board.action;

import java.util.List;
import me.mrletsplay.srweb.game.state.GamePolicyCard;
import me.mrletsplay.srweb.game.state.board.action.GameActionData;

public class ActionExamineTopCards extends GameActionData {

	private List<GamePolicyCard> cards;

	public ActionExamineTopCards() {}

	public List<GamePolicyCard> getCards() {
		return this.cards;
	}

}