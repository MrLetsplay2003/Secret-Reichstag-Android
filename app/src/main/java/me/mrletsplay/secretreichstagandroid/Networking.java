package me.mrletsplay.secretreichstagandroid;

import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFactory;

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

	public static void init(boolean isBeta) throws Exception {
		System.out.println("INIT " + (isBeta ? "BETA" : "NON-BETA") + " NETWORKING");
		webSocket = new WebSocketFactory().createSocket(isBeta ? "ws://192.168.0.13:34642" : "ws://repo.graphite-official.com:34642");
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

					System.out.println(obj.toString(2));

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
