package redstonedude.programs.projectboaty.client.net;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import redstonedude.programs.projectboaty.shared.net.Packet;
import redstonedude.programs.projectboaty.shared.src.Logger;

public class ClientPacketListener implements Runnable {

	private static ObjectOutputStream oos;

	public void start(int portNumber, String hostName) {
		try (Socket socket = new Socket(hostName, portNumber); ObjectOutputStream out2 = new ObjectOutputStream(socket.getOutputStream()); ObjectInputStream in = new ObjectInputStream(socket.getInputStream());) {
			oos = out2;
			Object inputObject;
			while ((inputObject = in.readObject()) != null) {
				ClientPacketHandler.handlePacket(this, (Packet) inputObject);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static synchronized void send(Packet data) {
		try {
			oos.writeObject(data);
			oos.flush();
		} catch (IOException e) {
			e.printStackTrace();
			//socket probably closed, disconnect here
			
		}
	}
	
	@Override
	public void run() {
		Logger.log("Starting client packet listener");
		start(ClientPacketHandler.portNumber,ClientPacketHandler.hostName);	
	}

}
