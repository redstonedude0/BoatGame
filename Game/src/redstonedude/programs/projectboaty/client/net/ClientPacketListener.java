package redstonedude.programs.projectboaty.client.net;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import redstonedude.programs.projectboaty.client.control.ControlHandler;
import redstonedude.programs.projectboaty.client.control.ControlHandler.Mode;
import redstonedude.programs.projectboaty.shared.net.Packet;
import redstonedude.programs.projectboaty.shared.src.Logger;

public class ClientPacketListener implements Runnable {

	private static ObjectOutputStream oos;
	public static Socket sock;
	
	public void start(int portNumber, String hostName) {
		try (Socket socket = new Socket(hostName, portNumber); ObjectOutputStream out2 = new ObjectOutputStream(/*new BufferedOutputStream(*/socket.getOutputStream()); ObjectInputStream in = new ObjectInputStream(new BufferedInputStream( socket.getInputStream()));) {
			oos = out2;
			sock = socket;
			Object inputObject;
			long lastTime = System.currentTimeMillis();
			while ((inputObject = in.readObject()) != null) {
				System.out.println("time between pckts: " + (System.currentTimeMillis()-lastTime));
				//ClientPacketHandler.handlePacket(this, (Packet) inputObject);
				ClientPacketHandler.queuedPackets.add((Packet)inputObject);
				lastTime = System.currentTimeMillis();
			}
			System.out.println("This should never run");
		} catch (IOException | ClassNotFoundException e) {
			// IOException - client closed connected. This is expected.
		} catch (Exception e) { //This should never actually occur, since packet handling isn't done in this thread.
			System.out.println("Listener general exception");
			// actual error occured (Possibly cast error), display error message
			e.printStackTrace();
		}
	}
	
	public static synchronized void send(Packet data) {
		try {
//			while (oos == null) {
//				System.out.println("Null wait");
//				Thread.sleep(1);
//			}
			oos.writeObject(data);
			oos.flush();
			//this slows the stream down
			//DO NOT REMOVE before reading the note in ServerPacketListener
			oos.reset();
		} catch (Exception e) {
			e.printStackTrace();
			disconnect();
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
		//this is not all the resets, shutdown for now
		System.exit(1);
	}
	
	@Override
	public void run() {
		Logger.log("Starting client packet listener");
		start(ClientPacketHandler.portNumber,ClientPacketHandler.hostName);	
	}

}
