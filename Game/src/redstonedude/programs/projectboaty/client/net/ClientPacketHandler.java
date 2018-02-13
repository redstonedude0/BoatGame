package redstonedude.programs.projectboaty.client.net;

import java.util.ArrayList;

import redstonedude.programs.projectboaty.client.control.ControlHandler;
import redstonedude.programs.projectboaty.client.control.ControlHandler.Mode;
import redstonedude.programs.projectboaty.client.physics.ClientPhysicsHandler;
import redstonedude.programs.projectboaty.shared.net.Packet;
import redstonedude.programs.projectboaty.shared.net.PacketConnect;
import redstonedude.programs.projectboaty.shared.net.PacketDelUser;
import redstonedude.programs.projectboaty.shared.net.PacketMoveRaft;
import redstonedude.programs.projectboaty.shared.net.PacketNewEntity;
import redstonedude.programs.projectboaty.shared.net.PacketNewRaft;
import redstonedude.programs.projectboaty.shared.net.PacketNewUser;
import redstonedude.programs.projectboaty.shared.net.PacketSetControl;
import redstonedude.programs.projectboaty.shared.net.PacketUserData;
import redstonedude.programs.projectboaty.shared.net.UserData;
import redstonedude.programs.projectboaty.shared.src.Logger;
import redstonedude.programs.projectboaty.shared.world.WorldHandler;

public class ClientPacketHandler {
	
	public static int portNumber = 49555;
	public static String hostName = "";
	public static ArrayList<UserData> userData = new ArrayList<UserData>();
	public static String currentUserUUID = "";
	
	public static UserData getCurrentUserData() {
		for (UserData ud: userData) {
			if (ud.uuid.equalsIgnoreCase(currentUserUUID)) {
				return ud;
			}
		}
		return null;
	}
	
	public static UserData getUserData(String uuid) {
		for (UserData ud: userData) {
			if (ud.uuid.equalsIgnoreCase(uuid)) {
				return ud;
			}
		}
		return null;
	}
	
	public static synchronized void handlePacket(ClientPacketListener connection, Packet packet) {
		//Logger.log(" packet received: " + packet.packetID);
		switch (packet.packetID) {
		case "PacketConnect":
			//connected, send user data and set graphics variables, also store our UUID
			PacketConnect pc = (PacketConnect) packet;
			sendPacket(new PacketUserData());
			//sendPacket(new PacketRequestRaft(1));
			currentUserUUID = pc.uuid;
			WorldHandler.key = pc.key;
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
			if (ud != null) {
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
