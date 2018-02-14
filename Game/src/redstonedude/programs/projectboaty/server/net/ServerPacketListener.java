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
	
	public void start(int portNumber) {
		try (Socket clientSocket = ServerPacketHandler.serverSocket.accept(); ObjectOutputStream out2 = new ObjectOutputStream(clientSocket.getOutputStream()); ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());) {
			// Start a new listener to handle the next client
			ServerPacketHandler.startNewListener();
			IP = clientSocket.getInetAddress();
			oos = out2;
			Object inputObject;
			ServerPacketHandler.playerJoin(this);
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
			} catch (IOException e) {
				e.printStackTrace();
				Logger.log("Disconnection: " + e.getMessage());
				ServerPacketHandler.playerDisconnect(this);
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

}
