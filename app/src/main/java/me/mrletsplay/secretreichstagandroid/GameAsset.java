package me.mrletsplay.secretreichstagandroid;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;

public enum GameAsset {

	LIBERAL_ARTICLE("article/liberal.png"),
	FASCIST_ARTICLE("article/fascist.png"),
	COMMUNIST_ARTICLE("article/communist.png"),
	ARTICLE_BACK("article/back.png"),

	ACTION_EXAMINE_TOP_CARDS_FASCIST("icon/icon-top-cards-f.png"),
	ACTION_EXAMINE_TOP_CARDS_COMMUNIST("icon/icon-top-cards-c.png"),
	ACTION_EXAMINE_TOP_CARDS_OTHER_FASCIST("icon/icon-top-cards-other-f.png"),
	ACTION_EXAMINE_TOP_CARDS_OTHER_COMMUNIST("icon/icon-top-cards-other-c.png"),
	ACTION_KILL_PLAYER_FASCIST("icon/icon-kill-f.png"),
	ACTION_KILL_PLAYER_COMMUNIST("icon/icon-kill-c.png"),
	ACTION_PICK_PRESIDENT_FASCIST("icon/icon-pick-president-f.png"),
	ACTION_PICK_PRESIDENT_COMMUNIST("icon/icon-pick-president-c.png"),
	ACTION_INSPECT_PLAYER_FASCIST("icon/icon-inspect-f.png"),
	ACTION_INSPECT_PLAYER_COMMUNIST("icon/icon-inspect-c.png"),
	ACTION_BLOCK_PLAYER_FASCIST("icon/icon-block-f.png"),
	ACTION_BLOCK_PLAYER_COMMUNIST("icon/icon-block-c.png"),

	ICON_WIN_FASCIST("icon/icon-win-f.png"),
	ICON_WIN_COMMUNIST("icon/icon-win-c.png"),

	ICON_PLAYER_BLOCKED("icon/icon-player-blocked.png"),
	ICON_PREVIOUS_PRESIDENT("icon/icon-player-previous-president.png"),
	ICON_PREVIOUS_CHANCELLOR("icon/icon-player-previous-chancellor.png"),
	ICON_PRESIDENT("icon/icon-president.png"),
	ICON_CHANCELLOR("icon/icon-chancellor.png"),
	ICON_YES("icon/icon-yes.png"),
	ICON_NO("icon/icon-no.png"),
	ICON_DEAD("icon/icon-dead.png"),
	ICON_NOT_HITLER("icon/icon-not-hitler.png"),
	ICON_NOT_STALIN("icon/icon-not-stalin.png"),
	ICON_CONNECTION("icon/connection.png"),
	;

	public static final String ASSETS_URL = "https://graphite-official.com/projects/ss/assets/";

	private final String path;
	private Bitmap bitmap;

	GameAsset(String path) {
		this.path = path;
	}

	public String getPath() {
		return path;
	}

	public String getURL() {
		return ASSETS_URL + path;
	}

	public void load() {
		try {
			this.bitmap = BitmapFactory.decodeStream(new URL(getURL()).openStream());
		}catch(IOException e) {
			throw new RuntimeException(e);
		}
	}

	public Bitmap getBitmap() {
		return bitmap;
	}

	public static boolean isEverythingLoaded() {
		for(GameAsset as : values()) {
			if(as.getBitmap() == null) return false;
		}
		return true;
	}
}
