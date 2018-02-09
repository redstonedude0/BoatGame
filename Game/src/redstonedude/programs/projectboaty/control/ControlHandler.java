package redstonedude.programs.projectboaty.control;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import redstonedude.programs.projectboaty.physics.PhysicsHandler;

public class ControlHandler implements KeyListener {

	public static boolean control_left = false;
	public static boolean control_right = false;
	public static boolean control_reverse = false;
	
	public static boolean debug_lock = false;
	
	public void keyPressed(KeyEvent e) {
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
			//warp
			debug_lock = true;
			break;
		case KeyEvent.VK_1:
		case KeyEvent.VK_2:
		case KeyEvent.VK_3:
			PhysicsHandler.createRaft(Integer.parseInt(""+e.getKeyChar()));
			break;
		}
	}

	public void keyReleased(KeyEvent e) {
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
			//warp
			debug_lock = false;
			break;
		}
	}

	public void keyTyped(KeyEvent e) {
	}

}
