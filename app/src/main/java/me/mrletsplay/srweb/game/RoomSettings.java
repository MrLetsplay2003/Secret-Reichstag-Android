package me.mrletsplay.srweb.game;

import java.util.List;
import me.mrletsplay.srweb.game.state.board.GameBoardActionField;

public class RoomSettings {

	private String mode;
	private int liberalCardCount;
	private int communistCardCount;
	private int fascistCardCount;
	private List<GameBoardActionField> liberalBoard;
	private List<GameBoardActionField> fascistBoard;
	private List<GameBoardActionField> communistBoard;

	public RoomSettings() {}

	public void setMode(String value) {
		this.mode = value;
	}

	public String getMode() {
		return this.mode;
	}

	public void setLiberalCardCount(int value) {
		this.liberalCardCount = value;
	}

	public int getLiberalCardCount() {
		return this.liberalCardCount;
	}

	public void setCommunistCardCount(int value) {
		this.communistCardCount = value;
	}

	public int getCommunistCardCount() {
		return this.communistCardCount;
	}

	public void setFascistCardCount(int value) {
		this.fascistCardCount = value;
	}

	public int getFascistCardCount() {
		return this.fascistCardCount;
	}

	public void setLiberalBoard(List<GameBoardActionField> value) {
		this.liberalBoard = value;
	}

	public List<GameBoardActionField> getLiberalBoard() {
		return this.liberalBoard;
	}

	public void setFascistBoard(List<GameBoardActionField> value) {
		this.fascistBoard = value;
	}

	public List<GameBoardActionField> getFascistBoard() {
		return this.fascistBoard;
	}

	public void setCommunistBoard(List<GameBoardActionField> value) {
		this.communistBoard = value;
	}

	public List<GameBoardActionField> getCommunistBoard() {
		return this.communistBoard;
	}

}