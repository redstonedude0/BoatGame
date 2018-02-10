package redstonedude.programs.projectboaty.control;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import redstonedude.programs.projectboaty.physics.PhysicsHandler;

public class ControlHandler implements KeyListener {

	public static boolean control_left = false;
	public static boolean control_right = false;
	public static boolean control_reverse = false;

	public static boolean debug_lock = false;

	public static enum Mode {
		MainMenu, Playing
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

	public static void doPlayingKeyPressed(KeyEvent e) {
		switch (e.getKeyCode()) {
		case KeyEvent.VK_LEFT:
			control_left = true;
			break;
		case KeyEvent.VK_RIGHT:
			control_right = true;
			break;
		case KeyEvent.VK_DOWN:
			control_reverse = true;
			break;
		case KeyEvent.VK_W:
			// warp
			debug_lock = true;
			break;
		case KeyEvent.VK_1:
		case KeyEvent.VK_2:
		case KeyEvent.VK_3:
			PhysicsHandler.createRaft(Integer.parseInt("" + e.getKeyChar()));
			break;
		case KeyEvent.VK_ESCAPE:
			escape_menu = !escape_menu;
			break;
		case KeyEvent.VK_ENTER:
			if (escape_menu) {
				PhysicsHandler.reset(); //redundancy
				reset();
				mode = Mode.MainMenu;
			}
		}
	}

	public static void doPlayingKeyReleased(KeyEvent e) {
		switch (e.getKeyCode()) {
		case KeyEvent.VK_LEFT:
			control_left = false;
			break;
		case KeyEvent.VK_RIGHT:
			control_right = false;
			break;
		case KeyEvent.VK_DOWN:
			control_reverse = false;
			break;
		case KeyEvent.VK_W:
			// warp
			debug_lock = false;
			break;
		}
	}
	
	public static void startPlaying() {
		PhysicsHandler.reset();
		reset();
		mode = Mode.Playing;
	}
	
	public static void reset() {
		control_left = false;
		control_right = false;
		control_reverse = false;
		debug_lock = false;
		escape_menu = false;
	}

}
