package redstonedude.programs.projectboaty.server.net;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;

import redstonedude.programs.projectboaty.shared.net.Packet;
import redstonedude.programs.projectboaty.shared.src.Logger;

public class ServerPacketListener implements Runnable {

	//private PrintWriter out;
	private ObjectOutputStream oos;
	public String listener_uuid = "";
	public InetAddress IP;
	private Socket sock;
	
	public void start(int portNumber) {
		try (Socket clientSocket = ServerPacketHandler.serverSocket.accept(); ObjectOutputStream out2 = new ObjectOutputStream(clientSocket.getOutputStream()); ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());) {
			// Start a new listener to handle the next client
			//ServerPacketHandler.startNewListener(); - MOVED TO playerJoin in ServerPacketHandler
			IP = clientSocket.getInetAddress();
			oos = out2;
			sock = clientSocket;
			Object inputObject;
			ServerPacketHandler.queuedPackets.add(new ServerQueuedPacket(null,this));//represent player join with null packet for now
			//ServerPacketHandler.playerJoin(this);
			while ((inputObject = in.readObject()) != null) {
				ServerPacketHandler.queuedPackets.add(new ServerQueuedPacket((Packet)inputObject,this));
				//ServerPacketHandler.handlePacket(this, (Packet) inputObject);
			}
		} catch (Exception e) {
			//error occured, disconnect the user
			if (e.getMessage() == null || (!e.getMessage().equalsIgnoreCase("Connection Reset") && !e.getMessage().equalsIgnoreCase("Socket Closed"))) {
				e.printStackTrace();
			}
			Logger.log("Disconnection: " + e.getMessage());
			ServerPacketHandler.playerDisconnect(this);
		}
	}

	public synchronized void send(Packet data) {
		if (oos != null) {
			try {
				oos.writeObject(data);
				oos.flush();
				//If at any point data is going slow its cos of this reset.
				//The stream caches objects so if we change a variable it wont send properly
				//so we need to reset the stream for this. The alternative is to manually serialize
				//or clone objects before sending them and reset less often to clear the cache
				//consider writeUnshared()
				oos.reset();
			} catch (IOException e) {
				e.printStackTrace();
				Logger.log("Disconnection: " + e.getMessage());
				ServerPacketHandler.queuedPackets.add(new ServerQueuedPacket(null,this));//represent player disconnect with null packet for now
				//ServerPacketHandler.playerDisconnect(this);
				oos = null;//close this connection
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
