package redstonedude.programs.projectboaty.server.net;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.UUID;

import redstonedude.programs.projectboaty.shared.raft.Tile;
import redstonedude.programs.projectboaty.shared.src.Logger;

public class ServerPacketHandler {

	public static ArrayList<ServerPacketListener> listeners = new ArrayList<ServerPacketListener>();
	public static int portNumber = 49555;
	
	private static synchronized void handlePacketUnpacked(ServerPacketListener connection, ArrayList<String> data) {
		if (data.size() > 0) {
			// Switch based on packet type - "JOIN", "TEXT", etc
			if (data.get(0).equals("JOIN")) {
				// If the packet is correctly formed
				// A malformed packet occurred
			}
		}
	}

	public static void broadcastPacketExcept(ServerPacketListener ignore, String... data) {
		// For each client, send the packet if they are not the ignored client
		for (ServerPacketListener spl : listeners) {
			if (!spl.listener_uuid.equals(ignore.listener_uuid)) {
				sendPacket(spl, data);
			}
		}
	}

	public static void broadcastPacket(String... data) {
		try {
			// For every client, send them the packet
			for (ServerPacketListener spl : listeners) {
				sendPacket(spl, data);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static ServerPacketListener getServerPacketListenerByUUID(String uuid) {
		for (ServerPacketListener spl : listeners) {
			if (spl.listener_uuid.equals(uuid)) {
				return spl;
			}
		}
		return null;
	}

	
	//do not use, used internally
	@Deprecated
	private static void sendPacket(String data, ServerPacketListener spl) {
		spl.send(data.length() + ";" + data);
	}

	
	public static void sendPacket(ServerPacketListener spl, String... data) {
		String toSend = "";
		for (String s : data) {
			toSend += s.length() + ";" + s;
		}
		spl.send(toSend);
	}

	
	public static synchronized void handlePacket(ServerPacketListener connection, Object data) {
		/*int index;
		ArrayList<String> parsedData = new ArrayList<String>();
		while ((index = data.indexOf(";")) != -1) {
			String numString = data.substring(0, index);
			Integer len = Integer.parseInt(numString);
			data = data.substring(index + 1);
			String datum = data.substring(0, len);
			parsedData.add(datum);
			data = data.substring(len);
		}
		handlePacketUnpacked(connection, parsedData);*/
		if (data instanceof ArrayList<?>) {
			ArrayList<Tile> t = (ArrayList<Tile>) data;
			Logger.log(t.get(1).mass + ":");
		} else {
			Logger.log("cunt");
		}
	}

	public static synchronized void startNewListener() {
		ServerPacketListener spl = new ServerPacketListener();
		spl.listener_uuid = UUID.randomUUID().toString();
		listeners.add(spl);
		Thread newListenerThread = new Thread(spl);
		newListenerThread.start();
	}
	
	public static ServerSocket serverSocket;
	
	public static void init() {
		try {
			serverSocket = new ServerSocket(portNumber);
			startNewListener();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
