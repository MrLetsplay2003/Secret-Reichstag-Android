package me.mrletsplay.srweb.game.state.board;


public enum GameBoardAction {

	EXAMINE_TOP_CARDS("%s must inspect the top three cards"),
	EXAMINE_TOP_CARDS_OTHER("%s must pick a player to inspect the top three cards"),
	KILL_PLAYER("%s must pick a player to die"),
	PICK_PRESIDENT("%s must pick a player to become the next president"),
	INSPECT_PLAYER("%s must examine a players identity"),
	BLOCK_PLAYER("%s must choose a player to be unelectable the next turn"),
	WIN(null),
	;

	private String eventLogMessage;

	private GameBoardAction(String eventLogMessage) {
		this.eventLogMessage = eventLogMessage;
	}

	public String getEventLogMessage() {
		return this.eventLogMessage;
	}

}