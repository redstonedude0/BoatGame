package redstonedude.programs.projectboaty.client.net;

import java.util.ArrayList;

public class ClientPacketHandler {

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

	/**
	 * Handle a raw packet
	 * 
	 * @param connection
	 *            The connection the packet came from (the server)
	 * @param data
	 *            The data in the packet
	 */
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

	/**
	 * Start the packet listener
	 */
	public static synchronized void startListener() {
		// Start the packet listener in a new thread
		Thread newListenerThread = new Thread(new ClientPacketListener(), "NetListenerThread");
		newListenerThread.start();
	}

	/**
	 * Send data to the server
	 * 
	 * @param data
	 *            Send a single string of data to the server
	 */
	public static void sendPacket(String data) {
		ClientPacketListener.send(data.length() + ";" + data);
	}

	/**
	 * Send data to the server
	 * 
	 * @param data
	 *            Send a list of strings of data to the server
	 */
	public static void sendPacket(String... data) {
		// Serialize and send the data
		String toSend = "";
		for (String s : data) {
			toSend += s.length() + ";" + s;
		}
		ClientPacketListener.send(toSend);
	}

}
