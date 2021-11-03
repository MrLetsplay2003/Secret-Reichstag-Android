package me.mrletsplay.secretreichstagandroid;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFactory;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import java8.util.concurrent.CompletableFuture;
import me.mrletsplay.srweb.packet.Packet;

public class Networking {

	private static Map<Packet, CompletableFuture<Packet>> packetQueue = new HashMap<>();
	private static PacketListener listener;
	private static WebSocket webSocket;

	public static void init(Context context) throws Exception {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		String selectedServer = prefs.getString("server", "Official Server");
		JSONArray arr = new JSONArray(prefs.getString("servers", "[]"));

		String srvURL = null;
		for(int i = 0; i < arr.length(); i++) {
			JSONObject o = arr.getJSONObject(i);
			if(o.getString("name").equals(selectedServer)) {
				srvURL = o.getString("url");
				break;
			}
		}

		if(srvURL == null) srvURL = "ws://repo.graphite-official.com:34642";

		webSocket = new WebSocketFactory().createSocket(srvURL);
		webSocket.addListener(new WebSocketAdapter() {

			@Override
			public void onConnected(WebSocket websocket, Map<String, List<String>> headers) throws Exception {
				System.out.println("CONNECTED");
			}

			@Override
			public void onConnectError(WebSocket websocket, WebSocketException exception) throws Exception {
				exception.printStackTrace();
			}

			@Override
			public void onUnexpectedError(WebSocket websocket, WebSocketException cause) throws Exception {
				cause.printStackTrace();
			}

			@Override
			public void onTextMessage(WebSocket websocket, String text) throws Exception {
				try {
					JSONObject obj = new JSONObject(text);
					if (obj.has("init") && obj.getBoolean("init")) {
						return;
					}

					Packet packet = SerializationUtils.cast(obj);
					if(packet.getReferrerID() != null) {
						Iterator<Packet> pI = packetQueue.keySet().iterator();
						while(pI.hasNext()) {
							Packet p = pI.next();
							if(!p.getID().equals(packet.getReferrerID())) continue;
							CompletableFuture<Packet> f = packetQueue.get(p);
							pI.remove();
							f.complete(packet);
							break;
						}
					}

					if(listener != null) listener.onPacketReceived(packet);
				}catch(Exception e) {
					e.printStackTrace();
				}
			}

			@Override
			public void onError(WebSocket websocket, WebSocketException cause) throws Exception {
				cause.printStackTrace();
			}
		});

		webSocket.connect();
	}

	public static void stop() {
		if(webSocket != null) {
			webSocket.disconnect();
			((DefaultPacketListener) listener).quit();
			webSocket = null;
			listener = null;
			packetQueue.clear();
		}
	}

	public static CompletableFuture<Packet> sendPacket(Packet packet) {
		CompletableFuture<Packet> pF = new CompletableFuture<>();
		packetQueue.put(packet, pF);
		webSocket.sendText(SerializationUtils.serialize(packet).toString());
		return pF;
	}

	public static void setPacketListener(PacketListener listener) {
		Networking.listener = listener;
	}
}
