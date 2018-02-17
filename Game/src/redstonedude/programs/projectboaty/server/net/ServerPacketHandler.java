package redstonedude.programs.projectboaty.server.net;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

import redstonedude.programs.projectboaty.server.data.ServerDataHandler;
import redstonedude.programs.projectboaty.server.physics.ServerPhysicsHandler;
import redstonedude.programs.projectboaty.server.physics.ServerUserData;
import redstonedude.programs.projectboaty.shared.entity.Entity;
import redstonedude.programs.projectboaty.shared.entity.EntityCharacter;
import redstonedude.programs.projectboaty.shared.entity.WrappedEntity;
import redstonedude.programs.projectboaty.shared.net.Packet;
import redstonedude.programs.projectboaty.shared.net.clientbound.PacketCharacterState;
import redstonedude.programs.projectboaty.shared.net.clientbound.PacketConnect;
import redstonedude.programs.projectboaty.shared.net.clientbound.PacketDelUser;
import redstonedude.programs.projectboaty.shared.net.clientbound.PacketEntityState;
import redstonedude.programs.projectboaty.shared.net.clientbound.PacketMoveCharacter;
import redstonedude.programs.projectboaty.shared.net.clientbound.PacketMoveRaft;
import redstonedude.programs.projectboaty.shared.net.clientbound.PacketNewEntity;
import redstonedude.programs.projectboaty.shared.net.clientbound.PacketNewRaft;
import redstonedude.programs.projectboaty.shared.net.clientbound.PacketNewUser;
import redstonedude.programs.projectboaty.shared.net.clientbound.PacketRaftTiles;
import redstonedude.programs.projectboaty.shared.net.clientbound.PacketSetControl;
import redstonedude.programs.projectboaty.shared.net.clientbound.PacketTileState;
import redstonedude.programs.projectboaty.shared.net.serverbound.PacketRequestCharacterState;
import redstonedude.programs.projectboaty.shared.net.serverbound.PacketRequestEntityState;
import redstonedude.programs.projectboaty.shared.net.serverbound.PacketRequestMoveCharacter;
import redstonedude.programs.projectboaty.shared.net.serverbound.PacketRequestMoveRaft;
import redstonedude.programs.projectboaty.shared.net.serverbound.PacketRequestRaft;
import redstonedude.programs.projectboaty.shared.net.serverbound.PacketRequestRaftTiles;
import redstonedude.programs.projectboaty.shared.net.serverbound.PacketRequestSetControl;
import redstonedude.programs.projectboaty.shared.net.serverbound.PacketRequestSetTaskList;
import redstonedude.programs.projectboaty.shared.net.serverbound.PacketRequestTileState;
import redstonedude.programs.projectboaty.shared.src.Logger;
import redstonedude.programs.projectboaty.shared.src.Server;
import redstonedude.programs.projectboaty.shared.world.WorldHandler;

public class ServerPacketHandler {

	public static ArrayList<ServerPacketListener> listeners = new ArrayList<ServerPacketListener>();
	public static ArrayList<ServerUserData> userData = new ArrayList<ServerUserData>();
	public static int portNumber = 49555;
	public static ConcurrentLinkedQueue<ServerQueuedPacket> queuedPackets = new ConcurrentLinkedQueue<ServerQueuedPacket>();

	public static void handlePackets() {
		while (!queuedPackets.isEmpty()) {
			ServerQueuedPacket sqp = queuedPackets.remove();
			try {
				handlePacket(sqp.spl, sqp.packet);
			} catch (Exception e) {
				// error occured in that packet, be very careful now.
				// kill the connection with the player
				if (sqp.spl != null) {
					sqp.spl.killConnection();
					try {
						playerDisconnect(sqp.spl); // disconnect the player
					} catch (Exception e2) {
						Logger.log("FATAL?: Error disconnecting player");
					}
				} else {
					Logger.log("FATAL?: Null packet listener");
				}
			}
		}
	}

	public static ServerUserData getUserData(String uuid) {
		for (ServerUserData sud : userData) {
			if (sud.uuid.equalsIgnoreCase(uuid)) {
				return sud;
			}
		}
		return null;
	}

	public static synchronized void broadcastPacketExcept(ServerPacketListener ignore, Packet packet) {
		// For each client, send the packet if they are not the ignored client
		for (ServerPacketListener spl : listeners) {
			if (!spl.listener_uuid.equals(ignore.listener_uuid)) {
				spl.send(packet);
			}
		}
	}

	public static synchronized void broadcastPacket(Packet packet) {
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
		// case "PacketDebug":
		// PacketDebug packetDebug = (PacketDebug) packet;
		// ArrayList<Tile> t = (ArrayList<Tile>) packetDebug.data;
		// Logger.log(t.get(1).mass + ":");
		// break;
		case "PacketUserData":
			// ignore for now, no user data to set
			break;
		case "PacketRequestRaft":
			PacketRequestRaft prr = (PacketRequestRaft) packet;
			// generate a new raft for this boik
			ServerUserData sud = getUserData(connection.listener_uuid);
			ServerPhysicsHandler.createRaft(prr.raftID, sud);
			PacketNewRaft pnr = new PacketNewRaft(connection.listener_uuid, sud.raft);
			broadcastPacket(pnr);
			break;
		case "PacketRequestMoveRaft":
			PacketRequestMoveRaft prmr = (PacketRequestMoveRaft) packet;
			sud = getUserData(connection.listener_uuid);
			if (sud != null) {
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
			}
			break;
		case "PacketRequestSetControl":
			PacketRequestSetControl prsc = (PacketRequestSetControl) packet;
			sud = getUserData(connection.listener_uuid);
			if (sud != null) {
				sud.requiredClockwiseRotation = prsc.requiredClockwiseRotation;
				sud.requiredForwardTranslation = prsc.requiredForwardTranslation;
				sud.requiredRightwardTranslation = prsc.requiredRightwardTranslation;
				PacketSetControl psc = new PacketSetControl();
				psc.requiredClockwiseRotation = prsc.requiredClockwiseRotation;
				psc.requiredForwardTranslation = prsc.requiredForwardTranslation;
				psc.requiredRightwardTranslation = prsc.requiredRightwardTranslation;
				psc.uuid = connection.listener_uuid;
				broadcastPacketExcept(connection, psc);
			}
			break;
		case "PacketRequestNewCharacter":
			ServerPhysicsHandler.newCharacter(connection.listener_uuid);
			break;
		case "PacketRequestMoveCharacter":
			PacketRequestMoveCharacter prmc = (PacketRequestMoveCharacter) packet;
			for (WrappedEntity we : ServerPhysicsHandler.getWrappedEntities()) {
				Entity e = we.entity;
				if (e.uuid.equals(prmc.uuid)) {
					e.loc.setPos(prmc.pos);
					e.loc.isAbsolute = prmc.absolutePos;
					e.loc.raftUUID = prmc.raftPosID;
				}
			}
			PacketMoveCharacter pmc = new PacketMoveCharacter();
			pmc.uuid = prmc.uuid;
			pmc.pos = prmc.pos;
			pmc.absolutePos = prmc.absolutePos;
			pmc.raftPosID = prmc.raftPosID;
			broadcastPacketExcept(connection, pmc);
			break;
		case "PacketRequestRaftTiles":
			PacketRequestRaftTiles prrt = (PacketRequestRaftTiles) packet;
			sud = getUserData(connection.listener_uuid);
			if (sud != null && sud.raft != null) {
				sud.raft.setTiles(prrt.tiles);
			}
			PacketRaftTiles prt = new PacketRaftTiles();
			prt.uuid = connection.listener_uuid;
			prt.tiles = prrt.tiles;
			broadcastPacketExcept(connection, prt);
			break;
		case "PacketRequestCharacterState":
			PacketRequestCharacterState prcs = (PacketRequestCharacterState) packet;
			Entity e = ServerPhysicsHandler.getEntity(prcs.characterUUID);
			if (e instanceof EntityCharacter) {
				EntityCharacter ec = (EntityCharacter) e;
				// System.out.println(" PRCSU:" + prcs.currentTask.assignedEntityID);
				// System.out.println(" PRCST:" + prcs.currentTask.taskTypeID);
				// System.out.println(" PRCSC:" + prcs.currentTask.completed);
				// System.out.println(" PRCSB:" + prcs.carryingBarrel);
				// System.out.println(" PRCSU:" + prcs.characterUUID);
				ec.carryingBarrel = prcs.carryingBarrel;
				ec.currentTask = prcs.currentTask; // does it still break in transport? who knows
				// ec.currentTask.assignedEntity = (EntityCharacter)
				// ServerPhysicsHandler.getEntity(prcs.characterUUID);// idk if it still breaks
				// in transport
				PacketCharacterState pcs = new PacketCharacterState();
				pcs.characterUUID = prcs.characterUUID;
				pcs.carryingBarrel = prcs.carryingBarrel;
				pcs.currentTask = prcs.currentTask;
				broadcastPacketExcept(connection, pcs);
			}
			break;
		case "PacketRequestSetTaskList":
			PacketRequestSetTaskList prstl = (PacketRequestSetTaskList) packet;
			sud = getUserData(connection.listener_uuid);
			if (sud != null && sud.raft != null) {
				sud.raft.setTasks(prstl.tasks);
			}
			break;
		case "PacketRequestTileState":
			PacketRequestTileState prts = (PacketRequestTileState) packet;
			sud = getUserData(connection.listener_uuid);
			if (sud != null && sud.raft != null) {
				// System.out.println("PRTSS DATA: (" + prts.uniqueTestingID + ")");
				// System.out.println(" " + prts.tile.hp);
				// System.out.println(" " + prts.tile.mass);
				// System.out.println(" " + prts.tile.getPos().x + ":" + prts.tile.getPos().y);
				// Tile t = sud.raft.set
				sud.raft.setTileAt(prts.tile);
				PacketTileState pts = new PacketTileState(prts.tile);
				pts.uuid = connection.listener_uuid;
				// pts.tile = prts.tile;
				broadcastPacketExcept(connection, pts);
			}
			break;
		case "PacketRequestEntityState":
			PacketRequestEntityState pres = (PacketRequestEntityState) packet;
			ServerPhysicsHandler.setEntity(pres.entity);
			PacketEntityState pes = new PacketEntityState(pres.entity);
			broadcastPacketExcept(connection, pes);
		default:
			Logger.log("Invalid packet received: " + packet.packetID);

		}
	}

	public static synchronized void startNewListener() {
		if (serverSocket.isClosed()) {
			Logger.log("Socket closed. Not listening for clients.");
		} else {
			ServerPacketListener spl = new ServerPacketListener();
			spl.listener_uuid = UUID.randomUUID().toString();
			listeners.add(spl);
			Thread newListenerThread = new Thread(spl);
			newListenerThread.start();
		}
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
		// synchronized so should no throw comod exceptions?
		// add new listener since another player might be joining soon
		//startNewListener();
		// let the client know they have connected
		ServerUserData ud = new ServerUserData();
		ud.IP = spl.IP;
		for (ServerUserData sud : ServerDataHandler.savedUsers) {
			if (sud.IP.equals(spl.IP)) {
				// REMOVE THE FOLLOWING 2 LINES TO TEST MULTIPLAYER FROM THE SAME IP, REMEMBER
				// TO ADD THEM BACK THOUGH
				// ud = sud;
				// spl.listener_uuid = ud.uuid; //last chance to change the UUID, so change it
				// to what it was before
			}
		}
		ud.uuid = spl.listener_uuid;
		userData.add(ud);
		if (ud.raft == null) {
			ServerPhysicsHandler.createRaft(1, ud);
		}
		// tell all users about this new user, including their raft
		broadcastPacket(new PacketNewUser(spl.listener_uuid));
		broadcastPacketExcept(spl, new PacketNewRaft(ud.uuid, ud.raft));
		spl.send(new PacketConnect(ud.uuid, WorldHandler.key, WorldHandler.getWind()));
		for (ServerUserData sud : userData) {
			if (!sud.uuid.equalsIgnoreCase(spl.listener_uuid)) {
				// tell the new user about all other new users
				spl.send(new PacketNewUser(sud.uuid));
			} // the connecting player needs to know about everyones raft
			spl.send(new PacketNewRaft(sud.uuid, sud.raft));
		}
		for (WrappedEntity we : ServerPhysicsHandler.getWrappedEntities()) {
			Entity e = we.entity;
			spl.send(new PacketNewEntity(e));
		}

	}

//	public static void playerJoinOrDisconnect(ServerPacketListener spl) {
//		if (getUserData(spl.listener_uuid) == null) {
//			playerJoin(spl);
//		} else {
//			playerDisconnect(spl);
//		}
//	}

	public static synchronized void playerDisconnect(ServerPacketListener spl) {
		ServerUserData sud = getUserData(spl.listener_uuid);
		if (sud != null) {
			userData.remove(sud);
			listeners.remove(spl);
			broadcastPacket(new PacketDelUser(spl.listener_uuid));
			// clear away old profile
			ServerUserData toRemove = null;
			for (ServerUserData d : ServerDataHandler.savedUsers) {
				if (d.IP.equals(sud.IP)) {
					toRemove = d;
				}
			}
			if (toRemove != null) {
				ServerDataHandler.savedUsers.remove(toRemove);
			}
			ServerDataHandler.savedUsers.add(sud);
			if (listeners.size() == 1) {// is now empty (barring the open listener) and wasn't before - the last user
										// has disconnected
				Logger.log("shutdown caused by disconnection of :" + spl.IP.getHostAddress());
				Server.shutdown();
			}
		}
	}

}
