package me.mrletsplay.srweb.game.state;

import me.mrletsplay.srweb.game.state.GameParty;

public enum GameRole {

	LIBERAL(GameParty.LIBERAL),
	STALIN(GameParty.COMMUNIST),
	COMMUNIST(GameParty.COMMUNIST),
	HITLER(GameParty.FASCIST),
	FASCIST(GameParty.FASCIST),
	;

	private GameParty party;

	private GameRole(GameParty party) {
		this.party = party;
	}

	public GameParty getParty() {
		return this.party;
	}

}