package redstonedude.programs.projectboaty.shared.src;

import redstonedude.programs.projectboaty.client.audio.MusicHandler;
import redstonedude.programs.projectboaty.client.control.ControlHandler;
import redstonedude.programs.projectboaty.client.graphics.GraphicsHandler;
import redstonedude.programs.projectboaty.client.graphics.TextureHandler;
import redstonedude.programs.projectboaty.client.net.ClientPacketHandler;
import redstonedude.programs.projectboaty.client.physics.ClientPhysicsHandler;
import redstonedude.programs.projectboaty.shared.raft.TileHandler;

public class Client implements Runnable {

	public static Thread thread;
	public static Client c;

	public static void init() {
		c = new Client();
	}

	public Client() {
		ClientPacketHandler.hostName = "25.95.178.83";
		TextureHandler.init();
		TileHandler.init();
		GraphicsHandler.init();
		ControlHandler ch = new ControlHandler();
		GraphicsHandler.frame.addKeyListener(ch);
		GraphicsHandler.frame.addMouseListener(ch);
		GraphicsHandler.frame.addMouseMotionListener(ch);
		MusicHandler.musicTester();
		start();
		/*try {
		    if (!SteamAPI.init()) {
		        // Steamworks initialization error, e.g. Steam client not running
		    	System.out.println("Steam client dead");
		    }
		    System.out.println("bool: " + SteamAPI.isSteamRunning());
		    SteamUser su = new SteamUser(new SteamUserCallback() {
				
				@Override
				public void onValidateAuthTicket(SteamID arg0, AuthSessionResponse arg1, SteamID arg2) {
				}
				
				@Override
				public void onMicroTxnAuthorization(int arg0, long arg1, boolean arg2) {
				}
				
				@Override
				public void onEncryptedAppTicket(SteamResult arg0) {
				}
			});
		    System.out.println("su: " + su.getSteamID());
		} catch (SteamException e) {
		    // Error extracting or loading native libraries
			e.printStackTrace();
		}//steam JNI code   */
	}

	public void run() {
		try {
			while (true) {
				ClientPhysicsHandler.physicsUpdate();
				GraphicsHandler.graphicsUpdate();
				Thread.sleep(20);// 50fps
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