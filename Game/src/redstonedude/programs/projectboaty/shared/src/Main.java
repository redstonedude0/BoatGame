package redstonedude.programs.projectboaty.shared.src;

import redstonedude.programs.projectboaty.client.control.ControlHandler;
import redstonedude.programs.projectboaty.client.graphics.GraphicsHandler;
import redstonedude.programs.projectboaty.client.graphics.TextureHandler;
import redstonedude.programs.projectboaty.server.physics.PhysicsHandler;
import redstonedude.programs.projectboaty.shared.raft.TileHandler;

public class Main implements Runnable {
	
	public static Thread thread;
	public static Main m;
	
	public static void main(String[] args) {
		m = new Main();
	}
	
	public Main() {
		TextureHandler.init();
		TileHandler.init();
		GraphicsHandler.init();
		GraphicsHandler.frame.addKeyListener(new ControlHandler());
		start();
	}

	public void run() {
		try {
			while (true) {
				PhysicsHandler.physicsUpdate();
				GraphicsHandler.graphicsUpdate();
				Thread.sleep(20);//50fps
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