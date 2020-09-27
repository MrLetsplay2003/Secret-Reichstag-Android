package me.mrletsplay.srweb.packet.impl;

import java.util.List;
import me.mrletsplay.srweb.packet.PacketData;
import me.mrletsplay.srweb.game.state.GamePolicyCard;

public class PacketServerPickCards extends PacketData {

	private List<GamePolicyCard> cards;
	private boolean vetoBlocked;

	public PacketServerPickCards() {}

	public List<GamePolicyCard> getCards() {
		return this.cards;
	}

	public boolean isVetoBlocked() {
		return this.vetoBlocked;
	}

}