package redstonedude.programs.projectboaty.src;

import redstonedude.programs.projectboaty.control.ControlHandler;
import redstonedude.programs.projectboaty.graphics.GraphicsHandler;
import redstonedude.programs.projectboaty.graphics.TextureHandler;
import redstonedude.programs.projectboaty.physics.PendulumPhysicsHandler;
import redstonedude.programs.projectboaty.physics.PhysicsHandler;
import redstonedude.programs.projectboaty.raft.TileHandler;

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
				PendulumPhysicsHandler.physicsUpdate();
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