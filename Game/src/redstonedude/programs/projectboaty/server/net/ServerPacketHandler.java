package redstonedude.programs.projectboaty.server.net;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.UUID;

import redstonedude.programs.projectboaty.server.physics.ServerPhysicsHandler;
import redstonedude.programs.projectboaty.server.physics.ServerUserData;
import redstonedude.programs.projectboaty.shared.net.Packet;
import redstonedude.programs.projectboaty.shared.net.PacketConnect;
import redstonedude.programs.projectboaty.shared.net.PacketNewRaft;
import redstonedude.programs.projectboaty.shared.net.PacketNewUser;
import redstonedude.programs.projectboaty.shared.net.PacketRequestRaft;
import redstonedude.programs.projectboaty.shared.net.UserData;
import redstonedude.programs.projectboaty.shared.src.Logger;

public class ServerPacketHandler {

	public static ArrayList<ServerPacketListener> listeners = new ArrayList<ServerPacketListener>();
	public static ArrayList<ServerUserData> userData = new ArrayList<ServerUserData>();
	public static int portNumber = 49555;

	public static ServerUserData getUserData(String uuid) {
		for (ServerUserData sud: userData) {
			if (sud.uuid.equalsIgnoreCase(uuid)) {
				return sud;
			}
		}
		return null;
	}
	
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
		switch (packet.packetID) {
		//case "PacketDebug":
		//	PacketDebug packetDebug = (PacketDebug) packet;
		//	ArrayList<Tile> t = (ArrayList<Tile>) packetDebug.data;
		//	Logger.log(t.get(1).mass + ":");
		//	break;
		case "PacketUserData":
			//ignore for now, no user data to set
			break;
		case "PacketRequestRaft":
			PacketRequestRaft prr = (PacketRequestRaft) packet;
			//generate a new raft for this boik
			
			ServerUserData sud = getUserData(connection.listener_uuid);
			ServerPhysicsHandler.createRaft(prr.raftID, sud);
			PacketNewRaft pnr = new PacketNewRaft(connection.listener_uuid, sud.raft);
			broadcastPacket(pnr);
			
			break;
		default:
			Logger.log("Invalid packet received: " + packet.packetID);

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
	
	public static synchronized void playerJoin(ServerPacketListener spl) {
		//let the client know they have connected
		ServerUserData ud = new ServerUserData();
		ud.uuid = spl.listener_uuid;
		userData.add(ud);
		broadcastPacket(new PacketNewUser(spl.listener_uuid));
		spl.send(new PacketConnect(ud.uuid));
	}

}
