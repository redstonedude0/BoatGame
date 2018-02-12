package redstonedude.programs.projectboaty.server.net;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.UUID;

import redstonedude.programs.projectboaty.shared.net.Packet;
import redstonedude.programs.projectboaty.shared.raft.Tile;
import redstonedude.programs.projectboaty.shared.src.Logger;

public class ServerPacketHandler {

	public static ArrayList<ServerPacketListener> listeners = new ArrayList<ServerPacketListener>();
	public static int portNumber = 49555;

	public static void broadcastPacketExcept(ServerPacketListener ignore, Packet packet) {
		// For each client, send the packet if they are not the ignored client
		for (ServerPacketListener spl : listeners) {
			if (!spl.listener_uuid.equals(ignore.listener_uuid)) {
				spl.send(packet);
			}
		}
	}

	public static void broadcastPacket(Packet packet) {
		try {
			// For every client, send them the packet
			for (ServerPacketListener spl : listeners) {
				spl.send(packet);
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

	public static synchronized void handlePacket(ServerPacketListener connection, Packet packet) {
		if (packet.data instanceof ArrayList<?>) {
			ArrayList<Tile> t = (ArrayList<Tile>) packet.data;
			Logger.log(t.get(1).mass + ":");
		} else {
			Logger.log("didnt work");
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
