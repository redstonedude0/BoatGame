package redstonedude.programs.projectboaty.server.net;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Random;
import java.util.UUID;

/**
 * Handle server packets
 * 
 */
public class ServerPacketHandler {

	/**
	 * Handle an unpacked packet
	 * 
	 * @param connection
	 *            The connection the packet was received from
	 * @param data
	 *            The data in the packet
	 */
	private static synchronized void handlePacketUnpacked(ServerPacketListener connection, ArrayList<String> data) {
		if (data.size() > 0) {
			// Switch based on packet type - "JOIN", "TEXT", etc
			if (data.get(0).equals("JOIN")) {
				// If the packet is correctly formed
				if (data.size() == 5) {
					// Create the new player
					// boolean isHost = false;
					// if (PlayerHandler.players.size() == 0) {
					// isHost = true;
					// }
					// PlayerHandler.addPlayer(data.get(1), data.get(2), data.get(3),
					// Integer.parseInt(data.get(4)), true, connection.listener_uuid, isHost);
					// ArrayList<String> plys = new ArrayList<String>();
					// plys.add("LOBBY");
					// for (PlayerData pd : PlayerHandler.players) {
					// plys.add(pd.playername);
					// plys.add(pd.uuid);
					// plys.add(pd.version + "");
					// plys.add(Boolean.toString(pd.isHost));
					// }
					// Send updated lobby packet, and ADDPLAYER packet, and add
					// to the chat
					// sendPacket(connection, plys.toArray(new String[0]));
					// broadcastPacketExcept(connection, "ADDPLAYER", data.get(1), data.get(2),
					// data.get(4), Boolean.toString(isHost));
					// ServerChatHelper.chat(ChatType.PlayerJoin, connection.listener_uuid);
				} else {
					// A malformed packet occurred
					// Logger.log("Malformed join packet");
				}
			}
		}
	}

	/**
	 * Broadcast a packet to all clients ignoring one
	 * 
	 * @param ignore
	 *            The ServerPacketListener of the client to ignore
	 * @param data
	 *            The data to send
	 */
	public static void broadcastPacketExcept(ServerPacketListener ignore, String... data) {
		// For each client, send the packet if they are not the ignored client
		for (ServerPacketListener spl : listeners) {
			if (!spl.listener_uuid.equals(ignore.listener_uuid)) {
				sendPacket(spl, data);
			}
		}
	}

	/**
	 * Broadcast a packet to all clients
	 * 
	 * @param data
	 *            The data to send
	 */
	public static void broadcastPacket(String... data) {
		try {
			// For every client, send them the packet
			for (ServerPacketListener spl : listeners) {
				sendPacket(spl, data);
			}
			// If an error occurs then print the error
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Get the ServerPacketListener by the listener UUID
	 * 
	 * @param uuid
	 *            The UUID of the listener (as a String)
	 * @return The ServerPacketListener with this UUID, or null if none exists
	 */
	public static ServerPacketListener getServerPacketListenerByUUID(String uuid) {
		// For every client, if their listener has the correct UUID then return it
		for (ServerPacketListener spl : listeners) {
			if (spl.listener_uuid.equals(uuid)) {
				return spl;
			}
		}
		// Else return null
		return null;
	}

	/**
	 * Send a packet to a client
	 * 
	 * @param data
	 *            The serialized form of the data
	 * @param spl
	 *            The ServerPacketListener representing the client
	 */
	@Deprecated
	private static void sendPacket(String data, ServerPacketListener spl) {
		// Send the length of the data, and the data, to the client
		spl.send(data.length() + ";" + data);
	}

	/**
	 * Send a packet to a client
	 * 
	 * @param spl
	 *            The ServerPacketListener representing the client
	 * @param data
	 *            The unserialized data
	 */
	public static void sendPacket(ServerPacketListener spl, String... data) {
		String toSend = "";
		// For each piece of data, add the length of the string, and the string itself
		for (String s : data) {
			toSend += s.length() + ";" + s;
		}
		// Send the data
		spl.send(toSend);
	}

	/**
	 * Handle an incoming packet
	 * 
	 * @param connection
	 *            The ServerPacketListener of the client who sent the packet
	 * @param data
	 *            The serialized data
	 */
	public static synchronized void handlePacket(ServerPacketListener connection, String data) {
		int index;
		ArrayList<String> parsedData = new ArrayList<String>();
		// While the string is not empty, and has a ';'
		while ((index = data.indexOf(";")) != -1) {
			// Find how long the next piece of data is
			String numString = data.substring(0, index);
			Integer len = Integer.parseInt(numString);
			// Remove the length and ';' character
			data = data.substring(index + 1);
			// Extract the datum by its length
			String datum = data.substring(0, len);
			// Add the datum to the list of parsed data
			parsedData.add(datum);
			// Remove the datum from the string
			data = data.substring(len);
		}
		// Handle the unpacked packed with the parsed(unserialized) data
		handlePacketUnpacked(connection, parsedData);
	}

	// ArrayList of listeners to hold each client connection
	public static ArrayList<ServerPacketListener> listeners = new ArrayList<ServerPacketListener>();

	/**
	 * Start a new listener, when a client connects
	 */
	public static synchronized void startNewListener() {
		//Logger.log("starting new listener");
		// Creature a new listener, with a random uuid, and add it to the thread
		ServerPacketListener spl = new ServerPacketListener();
		spl.listener_uuid = UUID.randomUUID().toString();
		listeners.add(spl);
		// Generate a new thread to run the listener
		Random rand = new Random();
		Thread newListenerThread = new Thread(spl, "NetListenerThread" + rand.nextInt());
		newListenerThread.start();
	}

	// The ServerSocket, used to handle incoming connection requests
	public static ServerSocket serverSocket;

	/**
	 * Initialize the ServerSocket to handle connections
	 * 
	 * @param portNumber
	 *            The port to open the socket on
	 */
	public static void init(int portNumber) {
		try {
			// Create the socket
			serverSocket = new ServerSocket(portNumber);
			// If an error occurs then print the error
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
