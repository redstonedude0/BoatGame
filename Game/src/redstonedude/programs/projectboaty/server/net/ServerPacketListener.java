package redstonedude.programs.projectboaty.server.net;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import redstonedude.programs.projectboaty.shared.src.Logger;

public class ServerPacketListener implements Runnable {

	//private PrintWriter out;
	private ObjectOutputStream oos;
	public String listener_uuid = "";

	public void start(int portNumber) {
		try (Socket clientSocket = ServerPacketHandler.serverSocket.accept(); ObjectOutputStream out2 = new ObjectOutputStream(clientSocket.getOutputStream()); ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());) {
			// Start a new listener to handle the next client
			ServerPacketHandler.startNewListener();
			oos = out2;
			Object inputObject;
			Logger.log("data start");
			while ((inputObject = in.readObject()) != null) {
				Logger.log("data in");
				ServerPacketHandler.handlePacket(this, inputObject);
			}
			Logger.log("data ended");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public synchronized void send(Object data) {
		if (oos != null) {
			try {
				oos.writeObject(data);
				oos.flush();
			} catch (IOException e) {
				e.printStackTrace();
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
