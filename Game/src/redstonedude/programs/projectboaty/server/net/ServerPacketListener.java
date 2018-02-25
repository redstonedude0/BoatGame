package redstonedude.programs.projectboaty.server.net;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;

import redstonedude.programs.projectboaty.shared.net.Packet;
import redstonedude.programs.projectboaty.shared.src.Logger;

public class ServerPacketListener implements Runnable {

	// private PrintWriter out;
	private ObjectOutputStream oos;
	public String listener_uuid = "";
	public InetAddress IP;
	private Socket sock;

	public void start(int portNumber) {
		try (Socket clientSocket = ServerPacketHandler.serverSocket.accept(); ObjectOutputStream out2 = new ObjectOutputStream(new BufferedOutputStream(clientSocket.getOutputStream())); ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(clientSocket.getInputStream()));) {
			// Start a new listener to handle the next client
			ServerPacketHandler.startNewListener();// - MOVED TO playerJoin in ServerPacketHandler
			IP = clientSocket.getInetAddress();
			oos = out2;
			sock = clientSocket;
			Object inputObject;
			ServerPacketHandler.playerJoin(this);
			// ServerPacketHandler.queuedPackets.add(new ServerQueuedPacket(null,this));//represent player join with null packet for now
			// ServerPacketHandler.playerJoin(this);
			while ((inputObject = in.readObject()) != null) {
				ServerPacketHandler.queuedPackets.add(new ServerQueuedPacket((Packet) inputObject, this));
				// ServerPacketHandler.handlePacket(this, (Packet) inputObject);
			}
		} catch (IOException | ClassNotFoundException e) {
			// IOException - client closed connected. Disconnet them.
			Logger.log("Disconnection: " + e.getMessage());
			ServerPacketHandler.playerDisconnect(this);
		} catch (Exception e) { //This should never actually occur, since packet handling isn't done in this thread.
			// actual error occured (Possibly cast error), crash the user
			e.printStackTrace();
			if (e.getMessage() != null) {
				Logger.log("Disconnection: " + e.getMessage());
			}
			// disconnect
			ServerPacketHandler.playerDisconnect(this);
		}
	}

	public synchronized void send(Packet data) {
		if (oos != null) {
			try {
				//oos.reset();
				oos.writeObject(data);
				oos.reset();//reset to fix sharing, automatically flushes?
				oos.flush();//flush anyway?
				// If at any point data is going slow its cos of this reset.
				// The stream caches objects so if we change a variable it wont send properly
				// so we need to reset the stream for this. The alternative is to manually serialize
				// or clone objects before sending them and reset less often to clear the cache
				// consider writeUnshared()
				
				//if (System.nanoTime()%5000 == 0) {
				//}
			} catch (IOException e) {
				if (ServerPacketHandler.listeners.contains(this)) {//if not disconnected
					Logger.log("Disconnection: " + e.getMessage());
					ServerPacketHandler.playerDisconnect(this);
					// ServerPacketHandler.queuedPackets.add(new ServerQueuedPacket(null,this));//represent player disconnect with null packet for now
					// ServerPacketHandler.playerDisconnect(this);
					oos = null;// close this connection
				}
			}
		}
	}

	@Override
	public void run() {
		// Start the listener on the correct port
		Logger.log("Starting server listener on port " + ServerPacketHandler.portNumber);
		start(ServerPacketHandler.portNumber);
	}

	public void killConnection() {
		try {
			sock.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
