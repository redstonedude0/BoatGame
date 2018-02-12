package redstonedude.programs.projectboaty.client.net;

import java.util.ArrayList;

import redstonedude.programs.projectboaty.server.physics.VectorDouble;
import redstonedude.programs.projectboaty.shared.net.Packet;
import redstonedude.programs.projectboaty.shared.raft.Tile;

public class ClientPacketHandler {
	
	public static int portNumber = 49555;
	public static String hostName = "";
	
	public static synchronized void handlePacket(ClientPacketListener connection, Packet packet) {
		
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
		
		ClientPacketListener.send(new Packet("TEST",tiles));
	}

}
