package redstonedude.programs.projectboaty.client.net;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import redstonedude.programs.projectboaty.shared.src.Logger;

public class ClientPacketListener implements Runnable {

	private static ObjectOutputStream oos;

	public void start(int portNumber, String hostName) {
		try (Socket socket = new Socket(hostName, portNumber); ObjectOutputStream out2 = new ObjectOutputStream(socket.getOutputStream()); ObjectInputStream in = new ObjectInputStream(socket.getInputStream());) {
			oos = out2;
			Object inputLine;
			while ((inputLine = in.readObject()) != null) {
				//ClientPacketHandler.handlePacket(this, inputLine);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static synchronized void send(Object data) {
		try {
			oos.writeObject(data);
			oos.flush();
			Logger.log("data flushed");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void run() {
		Logger.log("Starting client packet listener");
		start(ClientPacketHandler.portNumber,ClientPacketHandler.hostName);	
	}

}
