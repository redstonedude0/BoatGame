package redstonedude.programs.projectboaty.control;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import redstonedude.programs.projectboaty.physics.PhysicsHandler;

public class ControlHandler implements KeyListener {

	public static boolean control_left_rotate = false;
	public static boolean control_right_rotate = false;
	public static boolean control_left_translate = false;
	public static boolean control_right_translate = false;
	public static boolean control_forward = false;
	public static boolean control_backward = false;

	public static boolean debug_menu = false;

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
		PhysicsHandler.reset();
		reset();
		mode = Mode.Playing;
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

}
