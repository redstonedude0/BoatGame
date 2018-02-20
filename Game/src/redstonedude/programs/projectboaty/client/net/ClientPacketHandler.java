package redstonedude.programs.projectboaty.client.net;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;

import redstonedude.programs.projectboaty.client.control.ControlHandler;
import redstonedude.programs.projectboaty.client.control.ControlHandler.Mode;
import redstonedude.programs.projectboaty.client.physics.ClientPhysicsHandler;
import redstonedude.programs.projectboaty.server.net.ServerQueuedPacket;
import redstonedude.programs.projectboaty.server.physics.ServerPhysicsHandler;
import redstonedude.programs.projectboaty.shared.entity.Entity;
import redstonedude.programs.projectboaty.shared.entity.EntityCharacter;
import redstonedude.programs.projectboaty.shared.net.Packet;
import redstonedude.programs.projectboaty.shared.net.UserData;
import redstonedude.programs.projectboaty.shared.net.clientbound.PacketCharacterState;
import redstonedude.programs.projectboaty.shared.net.clientbound.PacketConnect;
import redstonedude.programs.projectboaty.shared.net.clientbound.PacketDelEntity;
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
import redstonedude.programs.projectboaty.shared.net.serverbound.PacketRequestTileState;
import redstonedude.programs.projectboaty.shared.net.serverbound.PacketUserData;
import redstonedude.programs.projectboaty.shared.src.Logger;
import redstonedude.programs.projectboaty.shared.world.WorldHandler;

public class ClientPacketHandler {

	public static int portNumber = 49555;
	public static String hostName = "";
	public static ArrayList<UserData> userData = new ArrayList<UserData>();
	public static String currentUserUUID = "";

	public static ConcurrentLinkedQueue<Packet> queuedPackets = new ConcurrentLinkedQueue<Packet>();

	public static void handlePackets() {
		while (!queuedPackets.isEmpty()) {
			Packet p = queuedPackets.remove();
			try {
				handlePacket(p);
			} catch (Exception e) {
				// error occured in that packet, be very careful now.
				e.printStackTrace();
				// kill the connection with the server
				try {
					ClientPacketListener.disconnect(); // disconnect
				} catch (Exception e2) {
					Logger.log("FATAL?: Error disconnecting from server");
				}
			}
		}
	}

	public static UserData getCurrentUserData() {
		for (UserData ud : userData) {
			if (ud.uuid.equalsIgnoreCase(currentUserUUID)) {
				return ud;
			}
		}
		return null;
	}

	public static UserData getUserData(String uuid) {
		for (UserData ud : userData) {
			if (ud.uuid.equalsIgnoreCase(uuid)) {
				return ud;
			}
		}
		return null;
	}

	public static synchronized void handlePacket(Packet packet) {
		switch (packet.packetID) {
		case "PacketConnect":
			// connected, send user data and set graphics variables, also store our UUID
			PacketConnect pc = (PacketConnect) packet;
			sendPacket(new PacketUserData());
			// sendPacket(new PacketRequestRaft(1));
			currentUserUUID = pc.uuid;
			// System.out.println("JOINED AS USER: " + currentUserUUID);
			WorldHandler.key = pc.key;
			WorldHandler.setWind(pc.wind);
			ControlHandler.mode = Mode.Playing;
			break;
		case "PacketNewRaft":
			PacketNewRaft pnr = (PacketNewRaft) packet;
			UserData ud = getUserData(pnr.uuid);
			ud.raft = pnr.raft;
			if (pnr.uuid.equalsIgnoreCase(currentUserUUID)) {
				ClientPhysicsHandler.cameraPosition = ud.raft.getCOMPos().getAbsolute(ud.raft.getUnitX(), ud.raft.getUnitY());
			}
			break;
		case "PacketNewUser":
			PacketNewUser pnu = (PacketNewUser) packet;
			ud = new UserData();
			ud.uuid = pnu.uuid;
			userData.add(ud);
			break;
		case "PacketMoveRaft":
			PacketMoveRaft pmr = (PacketMoveRaft) packet;
			ud = getUserData(pmr.uuid);
			if (ud != null && ud.raft != null) {
				ud.raft.setPos(pmr.pos);
				ud.raft.theta = pmr.theta;
				ud.raft.setVelocity(pmr.velocity);
				ud.raft.dtheta = pmr.dtheta;
				ud.raft.sin = pmr.sin;
				ud.raft.cos = pmr.cos;
				ud.raft.setCOMPos(pmr.COMPos);
			}
			break;
		case "PacketSetControl":
			PacketSetControl psc = (PacketSetControl) packet;
			ud = getUserData(psc.uuid);
			ud.requiredClockwiseRotation = psc.requiredClockwiseRotation;
			ud.requiredForwardTranslation = psc.requiredForwardTranslation;
			ud.requiredRightwardTranslation = psc.requiredRightwardTranslation;
			break;
		case "PacketDelUser":
			PacketDelUser pdu = (PacketDelUser) packet;
			ud = getUserData(pdu.uuid);
			userData.remove(ud);
			break;
		case "PacketNewEntity":
			PacketNewEntity pne = (PacketNewEntity) packet;
			ClientPhysicsHandler.addEntity(pne.entity);
			break;
		case "PacketMoveCharacter":
			PacketMoveCharacter pmc = (PacketMoveCharacter) packet;
			Entity e = ClientPhysicsHandler.getEntity(pmc.uuid);
			if (e != null) {
				e.loc.isAbsolute = pmc.absolutePos;
				e.loc.setPos(pmc.pos);
				e.loc.raftUUID = pmc.raftPosID;
			}//we may not have received entity data yet
			break;
		case "PacketRaftTiles":
			PacketRaftTiles prt = (PacketRaftTiles) packet;
			ud = getUserData(prt.uuid);
			if (ud != null && ud.raft != null) {
				ud.raft.setTiles(prt.tiles);
			}
			break;
		case "PacketCharacterState":
			PacketCharacterState pcs = (PacketCharacterState) packet;
			e = ClientPhysicsHandler.getEntity(pcs.characterUUID);
			if (e instanceof EntityCharacter) {
				EntityCharacter ec = (EntityCharacter) e;
				ec.carryingBarrel = pcs.carryingBarrel;
				ec.currentTask = pcs.currentTask;
			}
			break;
		case "PacketTileState":
			PacketTileState pts = (PacketTileState) packet;
			ud = getUserData(pts.uuid);
			if (ud != null && ud.raft != null) {
				// System.out.println("pts c " + pts.tile.hp);
				// Tile t = sud.raft.set
				ud.raft.setTileAt(pts.tile);
			}
			break;
		case "PacketDelEntity":
			PacketDelEntity pde = (PacketDelEntity) packet;
			ClientPhysicsHandler.removeEntity(pde.uuid);
			break;
		case "PacketEntityState":
			PacketEntityState pes = (PacketEntityState) packet;
			ClientPhysicsHandler.setEntity(pes.entity);
			break;
		default:
			Logger.log("Invalid packet received: " + packet.packetID);

		}
	}

	public static synchronized void sendPacket(Packet packet) {
		ClientPacketListener.send(packet);
	}

	public static synchronized void startListener() {
		Thread newListenerThread = new Thread(new ClientPacketListener(), "NetListenerThread");
		newListenerThread.start();
	}

}
