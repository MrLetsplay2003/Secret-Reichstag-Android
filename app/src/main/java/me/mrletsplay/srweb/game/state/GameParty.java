package me.mrletsplay.srweb.game.state;


public enum GameParty {

	LIBERAL("Liberals", "Liberal", null),
	COMMUNIST("Communists", "Communist", "Stalin"),
	FASCIST("Fascists", "Fascist", "Hitler"),
	;

	private String friendlyName;
	private String friendlyNameSingular;
	private String leaderName;

	private GameParty(String friendlyName, String friendlyNameSingular, String leaderName) {
		this.friendlyName = friendlyName;
		this.friendlyNameSingular = friendlyNameSingular;
		this.leaderName = leaderName;
	}

	public String getFriendlyName() {
		return this.friendlyName;
	}

	public String getFriendlyNameSingular() {
		return this.friendlyNameSingular;
	}

	public String getLeaderName() {
		return this.leaderName;
	}

}