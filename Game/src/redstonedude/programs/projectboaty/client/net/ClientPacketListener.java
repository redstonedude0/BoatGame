package redstonedude.programs.projectboaty.client.net;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import redstonedude.programs.projectboaty.client.control.ControlHandler;
import redstonedude.programs.projectboaty.client.control.ControlHandler.Mode;
import redstonedude.programs.projectboaty.server.net.ServerPacketHandler;
import redstonedude.programs.projectboaty.server.net.ServerQueuedPacket;
import redstonedude.programs.projectboaty.shared.net.Packet;
import redstonedude.programs.projectboaty.shared.src.Logger;

public class ClientPacketListener implements Runnable {

	private static ObjectOutputStream oos;
	public static Socket sock;
	
	public void start(int portNumber, String hostName) {
		try (Socket socket = new Socket(hostName, portNumber); ObjectOutputStream out2 = new ObjectOutputStream(socket.getOutputStream()); ObjectInputStream in = new ObjectInputStream(socket.getInputStream());) {
			oos = out2;
			sock = socket;
			Object inputObject;
			while ((inputObject = in.readObject()) != null) {
				//ClientPacketHandler.handlePacket(this, (Packet) inputObject);
				ClientPacketHandler.queuedPackets.add((Packet)inputObject);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static synchronized void send(Packet data) {
		try {
			oos.writeObject(data);
			oos.flush();
			//this slows the stream down
			//DO NOT REMOVE before reading the note in ServerPacketListener
			oos.reset();
		} catch (IOException e) {
			e.printStackTrace();
			disconnect();
			System.exit(1);
		}
	}
	
	public static synchronized void disconnect() {
		try {
			sock.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		//ClientPhysicsHandler.reset(); //redundancy
		// disconnect properly
		ControlHandler.reset();
		ControlHandler.mode = Mode.MainMenu;
	}
	
	@Override
	public void run() {
		Logger.log("Starting client packet listener");
		start(ClientPacketHandler.portNumber,ClientPacketHandler.hostName);	
	}

}
