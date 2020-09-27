package me.mrletsplay.srweb.packet.impl;

import me.mrletsplay.srweb.game.state.GameRole;
import java.util.List;
import me.mrletsplay.srweb.packet.PacketData;
import me.mrletsplay.srweb.game.Player;

public class PacketServerStartGame extends PacketData {

	private GameRole role;
	private Player leader;
	private List<Player> teammates;

	public PacketServerStartGame() {}

	public GameRole getRole() {
		return this.role;
	}

	public Player getLeader() {
		return this.leader;
	}

	public List<Player> getTeammates() {
		return this.teammates;
	}

}