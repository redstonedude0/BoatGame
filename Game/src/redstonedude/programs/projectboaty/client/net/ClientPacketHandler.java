//This class is in the directory redstonedude/programs/wager/client/net inside the JAR file
package redstonedude.programs.projectboaty.client.net;

//Import ArrayList to allow lists of objects to be stored
import java.util.ArrayList;

/**
 * Handler to handle all packets received by the client.
 */
public class ClientPacketHandler {

	/**
	 * Synchronized class (so that it can be called from multiple threads without
	 * collisions occurring).
	 * 
	 * Handle an unpacked packet (called by handlePacke once the data has been
	 * unpacked)
	 * 
	 * @param connection
	 *            The instance of ClientPacketListener associated with the client.
	 * @param data
	 *            The data received in the packet, in unpacked format (where each
	 *            entry in the array is 1 line of received data)
	 */
	private static synchronized void handlePacketUnpacked(ClientPacketListener connection, ArrayList<String> data) {
		// If there is data associated with this packet
		if (data.size() > 0) {
			// If the 1st piece of data, the packet type, is "ADDPLAYER"
			if (data.get(0).equals("ADDPLAYER")) {
				// If there is sufficient data to add another player to the game
				if (data.size() == 5) {
					// Add the player with the data received
					// PlayerHandler.addPlayer(data.get(1), data.get(2), "",
					// Integer.parseInt(data.get(3)), true, "", Boolean.valueOf(data.get(4)));
				} else {
					// There is insufficient data - the packet is malformed.
					// Logger.log("Malformed join packet");
				}
			}
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
