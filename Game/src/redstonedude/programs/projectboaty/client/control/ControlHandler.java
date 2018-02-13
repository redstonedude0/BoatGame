package redstonedude.programs.projectboaty.client.control;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import redstonedude.programs.projectboaty.client.net.ClientPacketHandler;
import redstonedude.programs.projectboaty.client.physics.ClientPhysicsHandler;
import redstonedude.programs.projectboaty.server.physics.VectorDouble;
import redstonedude.programs.projectboaty.shared.entity.Entity;
import redstonedude.programs.projectboaty.shared.net.PacketRequestNewCharacter;
import redstonedude.programs.projectboaty.shared.net.PacketRequestRaft;
import redstonedude.programs.projectboaty.shared.net.PacketRequestSetControl;
import redstonedude.programs.projectboaty.shared.net.UserData;

public class ControlHandler implements KeyListener, MouseListener {

	public static boolean control_left_rotate = false;
	public static boolean control_right_rotate = false;
	public static boolean control_left_translate = false;
	public static boolean control_right_translate = false;
	public static boolean control_forward = false;
	public static boolean control_backward = false;

	public static boolean debug_menu = false;
	public static boolean debug_lockpos = false;

	public static enum Mode {
		MainMenu, Playing, Connecting
	}
	public static boolean escape_menu = false;

	public static Mode mode = Mode.MainMenu;

	public void keyPressed(KeyEvent e) {
		switch (mode) {
		case MainMenu:
			doMenuKeyPressed(e);
			break;
		case Playing:
			doPlayingKeyPressed(e);
			break;
		case Connecting:
			//do nothing for now
			break;
		}
	}

	public void keyReleased(KeyEvent e) {
		switch (mode) {
		case MainMenu:
			doMenuKeyReleased(e);
			break;
		case Playing:
			doPlayingKeyReleased(e);
			break;
		case Connecting:
			//do nothing for now
			break;
		}
	}

	public void keyTyped(KeyEvent e) {
	}

	public static void doMenuKeyPressed(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_ENTER) {
			startPlaying();
		}
	}

	public static void doMenuKeyReleased(KeyEvent e) {
		
	}
	
	public static void doDebugButton(int i) {
		switch (i) {
		case 1://warp lock
			debug_lockpos = !debug_lockpos;
			break;
		case 2://summon character
			ClientPacketHandler.sendPacket(new PacketRequestNewCharacter());
			break;
		}
	}

	public static void doPlayingKeyPressed(KeyEvent e) {
		switch (e.getKeyCode()) {
		case KeyEvent.VK_A:
			control_left_rotate = true;
			break;
		case KeyEvent.VK_D:
			control_right_rotate = true;
			break;
		case KeyEvent.VK_S:
			control_backward = true;
			break;
		case KeyEvent.VK_W:
			control_forward = true;
			break;
		case KeyEvent.VK_Q:
			control_left_translate = true;
			break;
		case KeyEvent.VK_E:
			control_right_translate = true;
			break;
		case KeyEvent.VK_F3:
			debug_menu = !debug_menu;
			break;
		case KeyEvent.VK_1:
		case KeyEvent.VK_2:
		case KeyEvent.VK_3:
		case KeyEvent.VK_4:
			if (debug_menu) {
				doDebugButton(Integer.parseInt("" + e.getKeyChar()));
			} else {
				ClientPacketHandler.sendPacket(new PacketRequestRaft(Integer.parseInt("" + e.getKeyChar())));
			}
			break;
		case KeyEvent.VK_ESCAPE:
			escape_menu = !escape_menu;
			break;
		case KeyEvent.VK_ENTER:
			if (escape_menu) {
				//PhysicsHandler.reset(); //redundancy
				//TODO disconnect properly
				//reset();
				//mode = Mode.MainMenu;
			}
		}
	}

	public static void doPlayingKeyReleased(KeyEvent e) {
		switch (e.getKeyCode()) {
		case KeyEvent.VK_A:
			control_left_rotate = false;
			break;
		case KeyEvent.VK_D:
			control_right_rotate = false;
			break;
		case KeyEvent.VK_S:
			control_backward = false;
			break;
		case KeyEvent.VK_W:
			control_forward = false;
			break;
		case KeyEvent.VK_Q:
			control_left_translate = false;
			break;
		case KeyEvent.VK_E:
			control_right_translate = false;
			break;
		}
	}
	
	public static void startPlaying() {
		//PhysicsHandler.reset();
		//reset();
		//mode = Mode.Playing;
		ClientPacketHandler.startListener();
	}
	
	public static void reset() {
		control_left_rotate = false;
		control_right_rotate = false;
		control_left_translate = false;
		control_right_translate = false;
		control_forward = false;
		control_backward = false;
		debug_menu = false;
		escape_menu = false;
	}
	
	public static double requiredForwardTranslation = 0;
	public static double requiredClockwiseRotation = 0;
	public static double requiredRightwardTranslation = 0;
	
	public static void setControlDoubles() {
		//w alone will set up thrusts to thrust forward
		//d alone will set up thrusts to clockwise rotation
		//w and d will set up thrusts to forward and clockwise
		//e alone will set up thrusts for rightward translation
		requiredForwardTranslation = 0;//0 unrequired, 1 forward, -1 backward
		requiredClockwiseRotation = 0;
		requiredRightwardTranslation = 0;
		
		if (control_forward) {
			requiredForwardTranslation++;
		}
		if (control_backward) {
			requiredForwardTranslation--;
		}
		if (control_right_rotate) {
			requiredClockwiseRotation++;
		}
		if (control_left_rotate) {
			requiredClockwiseRotation--;
		}
		if (control_right_translate) {
			requiredRightwardTranslation++;
		}
		if (control_left_translate) {
			requiredRightwardTranslation--;
		}
		UserData ud = ClientPacketHandler.getCurrentUserData();
		if (ud != null) {
			ud.requiredClockwiseRotation = requiredClockwiseRotation;
			ud.requiredForwardTranslation = requiredForwardTranslation;
			ud.requiredRightwardTranslation = requiredRightwardTranslation;
		}
		PacketRequestSetControl prsc = new PacketRequestSetControl();
		prsc.requiredClockwiseRotation = requiredClockwiseRotation;
		prsc.requiredForwardTranslation = requiredForwardTranslation;
		prsc.requiredRightwardTranslation = requiredRightwardTranslation;
		ClientPacketHandler.sendPacket(prsc);
	}
	
	public static void doMenuPress(MouseEvent e) {
		//do nothing for now
	}
	
	public static void doPlayingPress(MouseEvent e) {
		
		int screenx = e.getX();
		int screeny = e.getY();
		VectorDouble offset = new VectorDouble(960, 540).subtract(ClientPhysicsHandler.cameraPosition.multiply(100));
		VectorDouble clicked = new VectorDouble(screenx, screeny);
		clicked = clicked.subtract(offset).divide(100);
		//see if any barrels were clicked on
		for (Entity ent: ClientPhysicsHandler.getEntities()) {
			if (ent.entityTypeID.equals("EntityBarrel")) {
				if (ent.absolutePosition) {
					VectorDouble vd = ent.getPos();
					VectorDouble diff = clicked.subtract(vd);
					if (diff.x >= 0 && diff.x <= 1) {
						if (diff.y >= 0 && diff.y <= 1) {
							//ent.setPos(new VectorDouble(0,0));
						}
					}
				}
			}
		}
		
		//EntityBarrel eb = new EntityBarrel();
		//b.absolutePosition = true;
		//eb.setPos(clicked);
		//ClientPhysicsHandler.addEntity(eb);
	}
	

	@Override
	public void mouseClicked(MouseEvent e) {
		
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}

	@Override
	public void mousePressed(MouseEvent e) {
		switch (mode) {
		case MainMenu:
			doMenuPress(e);
			break;
		case Playing:
			doPlayingPress(e);
			break;
		case Connecting:
			//do nothing for now
			break;
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {
	}

}
