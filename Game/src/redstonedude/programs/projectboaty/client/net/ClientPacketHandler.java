package redstonedude.programs.projectboaty.client.net;

import java.util.ArrayList;

import redstonedude.programs.projectboaty.server.physics.VectorDouble;
import redstonedude.programs.projectboaty.shared.raft.Tile;

public class ClientPacketHandler {
	
	public static int portNumber = 49555;
	public static String hostName = "";

	private static synchronized void handlePacketUnpacked(ClientPacketListener connection, ArrayList<String> data) {
		if (data.size() > 0) {
			/*if (data.get(0).equals("ADDPLAYER")) {
				if (data.size() == 5) {
					// Add the player with the data received
					// PlayerHandler.addPlayer(data.get(1), data.get(2), "",
					// Integer.parseInt(data.get(3)), true, "", Boolean.valueOf(data.get(4)));
				} else {
					// There is insufficient data - the packet is malformed.
					// Logger.log("Malformed join packet");
				}
			}*/
		}

	}
	
	public static synchronized void handlePacket(ClientPacketListener connection, String data) {
		// deserialize the data into a list of parsed data
		int index;
		ArrayList<String> parsedData = new ArrayList<String>();
		while ((index = data.indexOf(";")) != -1) {
			String numString = data.substring(0, index);
			Integer len = Integer.parseInt(numString);
			data = data.substring(index + 1);
			String extract = data.substring(0, len);
			parsedData.add(extract);
			data = data.substring(len);
		}
		// Handle the unpacked packet
		handlePacketUnpacked(connection, parsedData);
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
		ClientPacketListener.send(tiles);
	}

	
	public static void sendPacket(String data) {
		ClientPacketListener.send(data.length() + ";" + data);
	}

	public static void sendPacket(String... data) {
		String toSend = "";
		for (String s : data) {
			toSend += s.length() + ";" + s;
		}
		ClientPacketListener.send(toSend);
	}

}
