package redstonedude.programs.projectboaty.shared.src;

import java.io.IOException;

import redstonedude.programs.projectboaty.client.graphics.GraphicsHandler;
import redstonedude.programs.projectboaty.client.physics.ClientPhysicsHandler;
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
	
	public static long nextTime = System.currentTimeMillis();
	public static int frames = 0;
	public static long lastTime = System.currentTimeMillis();

	public void run() {
		try {
			while (true) {
				lastTime = System.currentTimeMillis();
				ServerPhysicsHandler.physicsUpdate();
				frames++;
				if (System.currentTimeMillis() >= nextTime) {
					nextTime += 1000;
					System.out.println(frames + " pups");
					frames = 0;
				}
				long currTime = System.currentTimeMillis();
				long elapsedTime = currTime - lastTime;
				if (elapsedTime >= 20) {
					elapsedTime = 20;// don't allow negative sleep times but aim for 50 FPS
				}
				Thread.sleep(20 - elapsedTime);
//				Thread.sleep(20);// 50 pups
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