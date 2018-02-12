package redstonedude.programs.projectboaty.shared.src;

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
	
}