package redstonedude.programs.projectboaty.shared.src;

import java.io.IOException;

import redstonedude.programs.projectboaty.server.data.ServerDataHandler;
import redstonedude.programs.projectboaty.server.net.ServerPacketHandler;
import redstonedude.programs.projectboaty.server.physics.ServerPhysicsHandler;
import redstonedude.programs.projectboaty.server.world.ServerWorldHandler;

public class Server implements Runnable {

	public static Thread thread;
	public static Server s;

	public static void init() {
		s = new Server();
	}

	public Server() {
		ServerDataHandler.loadData();
		ServerWorldHandler.init();
		ServerPacketHandler.init();
		start();
	}

	public void run() {
		try {
			while (true) {
				ServerPhysicsHandler.physicsUpdate();
				Thread.sleep(20);// 50 pups
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void start() {
		thread = new Thread(this);
		thread.start();
	}
	
	public static void shutdown() {
		Logger.log("Closing socket");
		try {
			ServerPacketHandler.serverSocket.close();
		} catch (IOException e) {
			//e.printStackTrace();
		}
		Logger.log("Saving data");
		ServerDataHandler.saveData();
		Logger.log("Server Close");
		System.exit(0);
	}
	
}