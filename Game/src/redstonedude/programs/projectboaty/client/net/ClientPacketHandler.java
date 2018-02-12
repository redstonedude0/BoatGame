package redstonedude.programs.projectboaty.client.net;

import java.util.ArrayList;

import redstonedude.programs.projectboaty.client.control.ControlHandler;
import redstonedude.programs.projectboaty.client.control.ControlHandler.Mode;
import redstonedude.programs.projectboaty.client.physics.ClientPhysicsHandler;
import redstonedude.programs.projectboaty.server.physics.VectorDouble;
import redstonedude.programs.projectboaty.shared.net.Packet;
import redstonedude.programs.projectboaty.shared.net.PacketDebug;
import redstonedude.programs.projectboaty.shared.net.PacketNewRaft;
import redstonedude.programs.projectboaty.shared.net.PacketNewUser;
import redstonedude.programs.projectboaty.shared.net.PacketRequestRaft;
import redstonedude.programs.projectboaty.shared.net.PacketUserData;
import redstonedude.programs.projectboaty.shared.net.UserData;
import redstonedude.programs.projectboaty.shared.raft.Tile;
import redstonedude.programs.projectboaty.shared.src.Logger;

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
		Logger.log(" packet received: " + packet.packetID);
		switch (packet.packetID) {
		case "PacketConnect":
			//connected, send user data and set graphics variables, also store our UUID
			sendPacket(new PacketUserData());
			ControlHandler.mode = Mode.Playing;
			sendPacket(new PacketRequestRaft(1));
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
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		Tile t = new Tile();
		t.setPos(new VectorDouble(1, 2));
		t.mass = 60;
		ArrayList<Tile> tiles = new ArrayList<Tile>();
		tiles.add(t);
		t = new Tile();
		t.setPos(new VectorDouble(3, 4));
		t.mass = 70;
		tiles.add(t);
		
		ClientPacketListener.send(new PacketDebug(tiles));
	}

}
