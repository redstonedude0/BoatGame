package redstonedude.programs.projectboaty.server.net;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.UUID;

import redstonedude.programs.projectboaty.server.physics.ServerPhysicsHandler;
import redstonedude.programs.projectboaty.server.physics.ServerUserData;
import redstonedude.programs.projectboaty.shared.net.Packet;
import redstonedude.programs.projectboaty.shared.net.PacketConnect;
import redstonedude.programs.projectboaty.shared.net.PacketDelUser;
import redstonedude.programs.projectboaty.shared.net.PacketMoveRaft;
import redstonedude.programs.projectboaty.shared.net.PacketNewRaft;
import redstonedude.programs.projectboaty.shared.net.PacketNewUser;
import redstonedude.programs.projectboaty.shared.net.PacketRequestMoveRaft;
import redstonedude.programs.projectboaty.shared.net.PacketRequestRaft;
import redstonedude.programs.projectboaty.shared.net.PacketRequestSetControl;
import redstonedude.programs.projectboaty.shared.net.PacketSetControl;
import redstonedude.programs.projectboaty.shared.src.Logger;
import redstonedude.programs.projectboaty.shared.world.WorldHandler;

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
		case "PacketRequestMoveRaft":
			PacketRequestMoveRaft prmr = (PacketRequestMoveRaft) packet;
			sud = getUserData(connection.listener_uuid);
			sud.raft.setPos(prmr.pos);
			sud.raft.theta = prmr.theta;
			sud.raft.setVelocity(prmr.velocity);
			sud.raft.dtheta = prmr.dtheta;
			sud.raft.sin = prmr.sin;
			sud.raft.cos = prmr.cos;
			sud.raft.setCOMPos(prmr.COMPos);
			PacketMoveRaft pmr = new PacketMoveRaft();
			pmr.uuid = connection.listener_uuid;
			pmr.pos = prmr.pos;
			pmr.theta = prmr.theta;
			pmr.velocity = prmr.velocity;
			pmr.dtheta = prmr.dtheta;
			pmr.sin = prmr.sin;
			pmr.cos = prmr.cos;
			pmr.COMPos = prmr.COMPos;
			broadcastPacketExcept(connection, pmr);
			break;
		case "PacketRequestSetControl":
			PacketRequestSetControl prsc = (PacketRequestSetControl) packet;
			sud = getUserData(connection.listener_uuid);
			sud.requiredClockwiseRotation = prsc.requiredClockwiseRotation;
			sud.requiredForwardTranslation = prsc.requiredForwardTranslation;
			sud.requiredRightwardTranslation = prsc.requiredRightwardTranslation;
			PacketSetControl psc = new PacketSetControl();
			psc.requiredClockwiseRotation = prsc.requiredClockwiseRotation;
			psc.requiredForwardTranslation = prsc.requiredForwardTranslation;
			psc.requiredRightwardTranslation = prsc.requiredRightwardTranslation;
			psc.uuid = connection.listener_uuid;
			broadcastPacketExcept(connection, psc);
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
		spl.send(new PacketConnect(ud.uuid, WorldHandler.key));
		for (ServerUserData sud: userData) {
			if (!sud.uuid.equalsIgnoreCase(spl.listener_uuid)) {
				spl.send(new PacketNewUser(sud.uuid));
				spl.send(new PacketNewRaft(sud.uuid, sud.raft));
			}
		}
	}
	
	public static synchronized void playerDisconnect(ServerPacketListener spl) {
		ServerUserData sud = getUserData(spl.listener_uuid);
		userData.remove(sud);
		listeners.remove(spl);
		broadcastPacket(new PacketDelUser(spl.listener_uuid));
	}

}
