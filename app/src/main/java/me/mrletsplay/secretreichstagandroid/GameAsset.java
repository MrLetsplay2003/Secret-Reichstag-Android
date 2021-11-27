package me.mrletsplay.secretreichstagandroid;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import com.caverock.androidsvg.SVG;
import com.caverock.androidsvg.SVGParseException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public enum GameAsset {

	LIBERAL_ARTICLE("article/liberal.svg"),
	FASCIST_ARTICLE("article/fascist.svg"),
	COMMUNIST_ARTICLE("article/communist.svg"),
	ARTICLE_BACK("article/back.svg"),

	ACTION_EXAMINE_TOP_CARDS_LIBERAL("action/top-cards-l.svg"),
	ACTION_EXAMINE_TOP_CARDS_FASCIST("action/top-cards-f.svg"),
	ACTION_EXAMINE_TOP_CARDS_COMMUNIST("action/top-cards-c.svg"),
	ACTION_EXAMINE_TOP_CARDS_OTHER_LIBERAL("action/top-cards-other-l.svg"),
	ACTION_EXAMINE_TOP_CARDS_OTHER_FASCIST("action/top-cards-other-f.svg"),
	ACTION_EXAMINE_TOP_CARDS_OTHER_COMMUNIST("action/top-cards-other-c.svg"),
	ACTION_KILL_PLAYER_LIBERAL("action/kill-l.svg"),
	ACTION_KILL_PLAYER_FASCIST("action/kill-f.svg"),
	ACTION_KILL_PLAYER_COMMUNIST("action/kill-c.svg"),
	ACTION_PICK_PRESIDENT_LIBERAL("action/pick-president-l.svg"),
	ACTION_PICK_PRESIDENT_FASCIST("action/pick-president-f.svg"),
	ACTION_PICK_PRESIDENT_COMMUNIST("action/pick-president-c.svg"),
	ACTION_INSPECT_PLAYER_LIBERAL("action/inspect-l.svg"),
	ACTION_INSPECT_PLAYER_FASCIST("action/inspect-f.svg"),
	ACTION_INSPECT_PLAYER_COMMUNIST("action/inspect-c.svg"),
	ACTION_BLOCK_PLAYER_LIBERAL("action/block-l.svg"),
	ACTION_BLOCK_PLAYER_FASCIST("action/block-f.svg"),
	ACTION_BLOCK_PLAYER_COMMUNIST("action/block-c.svg"),

	ICON_WIN_LIBERAL("action/win-l.svg"),
	ICON_WIN_FASCIST("action/win-f.svg"),
	ICON_WIN_COMMUNIST("action/win-c.svg"),

	ICON_PLAYER_BLOCKED("player/blocked.svg"),
	ICON_PREVIOUS_PRESIDENT("player/previous-president.svg"),
	ICON_PREVIOUS_CHANCELLOR("player/previous-chancellor.svg"),
	ICON_PRESIDENT("player/president.svg"),
	ICON_CHANCELLOR("player/chancellor.svg"),
	ICON_YES("player/vote-yes.svg"),
	ICON_NO("player/vote-no.svg"),
	ICON_DEAD("player/dead.svg"),
	ICON_NOT_HITLER("player/not-hitler.svg"),
	ICON_NOT_STALIN("player/not-stalin.svg"),
	ICON_CONNECTION("player/connection.svg"),

	ICON_ROLE_LIBERAL("player/role-liberal.svg"),
	ICON_ROLE_FASCIST("player/role-fascist.svg"),
	ICON_ROLE_HITLER("player/role-hitler.svg"),
	ICON_ROLE_COMMUNIST("player/role-communist.svg"),
	ICON_ROLE_STALIN("player/role-stalin.svg"),
	;

	public static final String ASSETS_URL = "https://sr.graphite-official.com/assets/";

	private final String path;
	private SVG svg;

	GameAsset(String path) {
		this.path = path;
	}

	public String getPath() {
		return path;
	}

	public String getURL() {
		return ASSETS_URL + path;
	}

	public boolean load(Context c, File filesDir) {
		try {
			File cachedFile = new File(filesDir, name() + ".svg");
			if(cachedFile.exists()) {
				try(FileInputStream fIn = new FileInputStream(cachedFile)) {
					this.svg = SVG.getFromInputStream(fIn);
				}

				return false;
			}else {
				ByteArrayOutputStream bOut = new ByteArrayOutputStream();

				int len;
				byte[] buf = new byte[1024];
				try(InputStream in = new URL(getURL()).openStream()) {
					while((len = in.read(buf)) > 0) {
						bOut.write(buf, 0, len);
					}
				}

				byte[] bytes = bOut.toByteArray();
				this.svg = SVG.getFromInputStream(new ByteArrayInputStream(bytes));

				SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.getCurrentFragment().getContext());
				if(prefs.getBoolean("cache_assets", true)) {
					try (FileOutputStream fOut = new FileOutputStream(cachedFile)) {
						fOut.write(bytes);
					}
				}

				return true;
			}
		}catch(IOException | SVGParseException e) {
			throw new RuntimeException(e);
		}
	}

	public SVG getSVG() {
		return svg;
	}

	public static boolean isEverythingLoaded() {
		for(GameAsset as : values()) {
			if(as.getSVG() == null) return false;
		}
		return true;
	}
}
