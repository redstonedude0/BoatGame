package redstonedude.programs.projectboaty.shared.src;

import java.awt.Image;
import java.awt.image.ImageObserver;

import javax.swing.SwingUtilities;

import redstonedude.programs.projectboaty.client.audio.MusicHandler;
import redstonedude.programs.projectboaty.client.control.ControlHandler;
import redstonedude.programs.projectboaty.client.graphics.GraphicsHandler;
import redstonedude.programs.projectboaty.client.graphics.TextureHandler;
import redstonedude.programs.projectboaty.client.net.ClientPacketHandler;
import redstonedude.programs.projectboaty.client.physics.ClientPhysicsHandler;
import redstonedude.programs.projectboaty.shared.event.EventRegistry;
import redstonedude.programs.projectboaty.shared.raft.TileHandler;
import redstonedude.programs.projectboaty.shared.task.TaskRepair;

public class Client implements Runnable, ImageObserver {

	public static Thread thread;
	public static Client c;

	public static void init() {
		c = new Client();
	}

	public Client() {
		ClientPacketHandler.hostName = "10.1.11.7";//"25.95.178.83";
		TextureHandler.init();
		TileHandler.init();
		GraphicsHandler.init();
		ControlHandler ch = new ControlHandler();
		GraphicsHandler.frame.addKeyListener(ch);
		GraphicsHandler.frame.addMouseListener(ch);
		GraphicsHandler.frame.addMouseMotionListener(ch);
		MusicHandler.musicTester();
		registerEvents();
		start();
		/*
		 * try { if (!SteamAPI.init()) { // Steamworks initialization error, e.g. Steam client not running System.out.println("Steam client dead"); } System.out.println("bool: " + SteamAPI.isSteamRunning()); SteamUser su = new SteamUser(new SteamUserCallback() {
		 * 
		 * @Override public void onValidateAuthTicket(SteamID arg0, AuthSessionResponse arg1, SteamID arg2) { }
		 * 
		 * @Override public void onMicroTxnAuthorization(int arg0, long arg1, boolean arg2) { }
		 * 
		 * @Override public void onEncryptedAppTicket(SteamResult arg0) { } }); System.out.println("su: " + su.getSteamID()); } catch (SteamException e) { // Error extracting or loading native libraries e.printStackTrace(); }//steam JNI code
		 */
	}
	
	public static void registerEvents() {
		EventRegistry.addListener(TaskRepair.class);
	}

	public void run() {
		try {
			// while (false) {
			// long time = System.currentTimeMillis();
			// ClientPhysicsHandler.physicsUpdate();
			// GraphicsHandler.graphicsUpdate();
			// long elapsedTime = System.currentTimeMillis()-time;
			// if (elapsedTime >= 20) {
			// elapsedTime = 20; //allow atleast 5ms of sleep for the graphics to be ready
			// }
			// Thread.sleep(20-elapsedTime);// 50fps
			// frames++;
			// if (System.currentTimeMillis() >= nextTime) {
			// nextTime += 1000;
			// System.out.println(frames + " fps");
			// frames = 0;
			// }
			// }
			update();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static long nextTime = System.currentTimeMillis();
	public static int frames = 0;
	public static long lastTime = System.currentTimeMillis();
	
	public static void update() {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					lastTime = System.currentTimeMillis();
					ClientPhysicsHandler.physicsUpdate();
					GraphicsHandler.graphicsUpdate();
					frames++;
					if (System.currentTimeMillis() >= nextTime) {
						nextTime += 1000;
						//System.out.println(frames + " fps");
						frames = 0;
					}
					long currTime = System.currentTimeMillis();
					long elapsedTime = currTime - lastTime;
					if (elapsedTime >= 20) {
						elapsedTime = 20;// don't allow negative sleep times but aim for 50 FPS
					}
					Thread.sleep(20 - elapsedTime);
					update();
				} catch (InterruptedException e) {
					e.printStackTrace();
				} // 50fps
			}
		});
	}

	public void start() {
		thread = new Thread(this);
		thread.start();
	}

	@Override
	public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height) {
		ClientPhysicsHandler.physicsUpdate();
		GraphicsHandler.graphicsUpdate();
		return true;
	}
}