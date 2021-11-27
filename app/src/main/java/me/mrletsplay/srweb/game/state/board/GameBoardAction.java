package me.mrletsplay.srweb.game.state.board;


public enum GameBoardAction {

	EXAMINE_TOP_CARDS("Examine top cards", "%s must inspect the top three cards"),
	EXAMINE_TOP_CARDS_OTHER("Propose another player to inspect top cards", "%s must pick a player to inspect the top three cards"),
	KILL_PLAYER("Kill a player", "%s must pick a player to die"),
	PICK_PRESIDENT("Pick the next president", "%s must pick a player to become the next president"),
	INSPECT_PLAYER("Inspect a player", "%s must examine a players identity"),
	BLOCK_PLAYER("Block a player", "%s must choose a player to be unelectable the next turn"),
	WIN(null, null),
	;

	private String friendlyName;
	private String eventLogMessage;

	private GameBoardAction(String friendlyName, String eventLogMessage) {
		this.friendlyName = friendlyName;
		this.eventLogMessage = eventLogMessage;
	}

	public String getFriendlyName() {
		return this.friendlyName;
	}

	public String getEventLogMessage() {
		return this.eventLogMessage;
	}

}