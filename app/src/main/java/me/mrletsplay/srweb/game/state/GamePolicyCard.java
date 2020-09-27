package me.mrletsplay.srweb.game.state;

import me.mrletsplay.srweb.game.state.GameParty;

public enum GamePolicyCard {

	LIBERAL(GameParty.LIBERAL),
	COMMUNIST(GameParty.COMMUNIST),
	FASCIST(GameParty.FASCIST),
	;

	private GameParty party;

	private GamePolicyCard(GameParty party) {
		this.party = party;
	}

	public GameParty getParty() {
		return this.party;
	}

}