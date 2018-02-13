package redstonedude.programs.projectboaty.client.control;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import redstonedude.programs.projectboaty.client.net.ClientPacketHandler;
import redstonedude.programs.projectboaty.client.net.ClientPacketListener;
import redstonedude.programs.projectboaty.client.physics.ClientPhysicsHandler;
import redstonedude.programs.projectboaty.server.physics.VectorDouble;
import redstonedude.programs.projectboaty.shared.entity.Entity;
import redstonedude.programs.projectboaty.shared.net.PacketRequestNewCharacter;
import redstonedude.programs.projectboaty.shared.net.PacketRequestRaft;
import redstonedude.programs.projectboaty.shared.net.PacketRequestSetControl;
import redstonedude.programs.projectboaty.shared.net.UserData;
import redstonedude.programs.projectboaty.shared.raft.Tile;
import redstonedude.programs.projectboaty.shared.raft.TileThruster;
import redstonedude.programs.projectboaty.shared.task.TaskCollect;
import redstonedude.programs.projectboaty.shared.task.TaskConstruct;

public class ControlHandler implements KeyListener, MouseListener, MouseMotionListener {

	public static boolean control_left_rotate = false;
	public static boolean control_right_rotate = false;
	public static boolean control_left_translate = false;
	public static boolean control_right_translate = false;
	public static boolean control_forward = false;
	public static boolean control_backward = false;

	public static boolean debug_menu = false;
	public static boolean debug_lockpos = false;
	
	public static boolean build_menu = false;

	public static enum Mode {
		MainMenu, Playing, Connecting
	}
	public static boolean escape_menu = false;
	
	public static boolean clickmode_collection = true;
	public static boolean clickmode_building_wood = false;

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
	
	public static void doBuildMenuKeyPress(KeyEvent e) {
		switch (e.getKeyCode()) {
		case KeyEvent.VK_B:
			build_menu = !build_menu;
			break;
		case KeyEvent.VK_W://shit system. replace it.
			clickmode_collection = false;
			clickmode_building_wood = true;
			break;
		case KeyEvent.VK_T:
			clickmode_collection = false;
			clickmode_building_wood = false;
			break;
		case KeyEvent.VK_C:
			clickmode_collection = true;
			break;
		}
	}

	public static void doPlayingKeyPressed(KeyEvent e) {
		if (build_menu) {
			doBuildMenuKeyPress(e);
			return;
		}
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
				ClientPacketListener.disconnect();
			}
			break;
		case KeyEvent.VK_B:
			build_menu = !build_menu;
			break;
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
		//see if any barrels were clicked on
		doPlayingPress(getAbsoluteVectorFromScreenCoordinates(screenx, screeny));
	}
	
	public static VectorDouble getAbsoluteVectorFromScreenCoordinates(int screenx, int screeny) {
		VectorDouble offset = new VectorDouble(960, 540).subtract(ClientPhysicsHandler.cameraPosition.multiply(100));
		VectorDouble clicked = new VectorDouble(screenx, screeny);
		clicked = clicked.subtract(offset).divide(100);
		return clicked;
	}
	
	public static void doPlayingPress(VectorDouble clicked) {
		if (clickmode_collection) {
			doBarrelPress(clicked);
		} else {
			doBuildingPress(clicked);
		}
	}
	
	public static void doBarrelPress(VectorDouble clicked) {
		for (Entity ent: ClientPhysicsHandler.getEntities()) {
			if (ent.entityTypeID.equals("EntityBarrel")) {
				if (ent.absolutePosition) {
					VectorDouble vd = ent.getPos();
					VectorDouble diff = clicked.subtract(vd);
					if (diff.x >= 0 && diff.x <= 1) {
						if (diff.y >= 0 && diff.y <= 1) {
							//ent.setPos(new VectorDouble(0,0));
							UserData ud = ClientPacketHandler.getCurrentUserData();
							if (ud != null && ud.raft != null) {
								TaskCollect t = new TaskCollect();
								t.collectionUUID = ent.uuid;
								t.targetLoc = ent.getPos();
								t.targetLoc_absolute = true;
								t.targetLoc_raftuuid = "";
								ud.raft.addTask(t);
							}
						}
					}
				}
			}
		}
	}
	
	public static void doBuildingPress(VectorDouble clicked) {
		//convert to relative coordinates
		UserData ud = ClientPacketHandler.getCurrentUserData();
		VectorDouble blockPos = getBlockPosFromScreenCoordinates(mouseX, mouseY,ud);
		Tile tile;
		if (clickmode_building_wood) {
			tile = new Tile();
		} else {
			tile = new TileThruster();
		}
		tile.setPos(blockPos);
		TaskConstruct t = new TaskConstruct();
		t.resultantTile = tile;
		t.targetLoc = blockPos;
		t.targetLoc_absolute = false;
		t.targetLoc_raftuuid = ud.uuid;
		ud.raft.addTask(t);
	}
	
	public static VectorDouble getBlockPosFromScreenCoordinates(int screenx, int screeeny, UserData currentUserData) {
		VectorDouble absolute = getAbsoluteVectorFromScreenCoordinates(mouseX, mouseY);
		VectorDouble relative = absolute.subtract(currentUserData.raft.getPos()).getRelative(currentUserData.raft.getUnitX(), currentUserData.raft.getUnitY());
		VectorDouble blockPos = new VectorDouble(Math.floor(relative.x), Math.floor(relative.y));
		return blockPos;
	}
	
	public static void updateConstructionTile() {
		UserData ud = ClientPacketHandler.getCurrentUserData();
		if (!clickmode_collection) {
			Tile t = new Tile();
			t.setPos(getBlockPosFromScreenCoordinates(mouseX, mouseY,ud));
			ud.raft.setConstructionTile(t);
		} else {
			ud.raft.setConstructionTile(null);
		}
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

	@Override
	public void mouseDragged(MouseEvent e) {
	}

	public static int mouseX = 0;
	public static int mouseY = 0;
	
	@Override
	public void mouseMoved(MouseEvent e) {
		mouseX = e.getX();
		mouseY = e.getY();
	}

}
