package redstonedude.programs.projectboaty.client.control;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.function.Consumer;

import javax.swing.SwingUtilities;

import redstonedude.programs.projectboaty.client.graphics.GraphicsHandler;
import redstonedude.programs.projectboaty.client.net.ClientPacketHandler;
import redstonedude.programs.projectboaty.client.physics.ClientPhysicsHandler;
import redstonedude.programs.projectboaty.shared.entity.Entity;
import redstonedude.programs.projectboaty.shared.entity.EntityCharacter;
import redstonedude.programs.projectboaty.shared.entity.WrappedEntity;
import redstonedude.programs.projectboaty.shared.net.UserData;
import redstonedude.programs.projectboaty.shared.net.serverbound.PacketRequestNewCharacter;
import redstonedude.programs.projectboaty.shared.net.serverbound.PacketRequestRaft;
import redstonedude.programs.projectboaty.shared.net.serverbound.PacketRequestSetControl;
import redstonedude.programs.projectboaty.shared.physics.Location;
import redstonedude.programs.projectboaty.shared.physics.VectorDouble;
import redstonedude.programs.projectboaty.shared.raft.Tile;
import redstonedude.programs.projectboaty.shared.raft.Tile.TileType;
import redstonedude.programs.projectboaty.shared.raft.TileAnchorSmall;
import redstonedude.programs.projectboaty.shared.raft.TileThruster;
import redstonedude.programs.projectboaty.shared.task.Task;
import redstonedude.programs.projectboaty.shared.task.TaskCollect;
import redstonedude.programs.projectboaty.shared.task.TaskConstruct;
import redstonedude.programs.projectboaty.shared.task.TaskDeconstruct;
import redstonedude.programs.projectboaty.shared.task.TaskDestroyResource;
import redstonedude.programs.projectboaty.shared.task.TaskRecruit;

public class ControlHandler implements KeyListener, MouseListener, MouseMotionListener {

	public static boolean control_left_rotate = false;
	public static boolean control_right_rotate = false;
	public static boolean control_left_translate = false;
	public static boolean control_right_translate = false;
	public static boolean control_forward = false;
	public static boolean control_backward = false;
	public static boolean control_brake = false;

	public static boolean debug_menu = false;
	public static boolean debug_lockpos = false;

	public static enum Mode {
		MainMenu, Playing, Connecting
	}

	public static int clickmode_roation_index = 0;

	public static enum ClickMode {
		Collection, Building, Deconstruct, Recruiting, DestroyingResource
	}

	public static ClickMode clickMode = ClickMode.Collection;
	public static TileType buildingType;

	public static Mode mode = Mode.MainMenu;

	public void keyPressed(KeyEvent e) {
		switch (mode) {
		case MainMenu:
			// do nothing for now
			break;
		case Playing:
			doPlayingKeyPressed(e);
			break;
		case Connecting:
			// do nothing for now
			break;
		}
	}

	public void keyReleased(KeyEvent e) {
		switch (mode) {
		case MainMenu:
			// do nothing for now
			break;
		case Playing:
			doPlayingKeyReleased(e);
			break;
		case Connecting:
			// do nothing for now
			break;
		}
	}
	
	public static void doDebugButton(int i) {
		switch (i) {
		case 1:// warp lock
			debug_lockpos = !debug_lockpos;
			break;
		case 2:// summon character
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
		case KeyEvent.VK_SPACE:
			control_brake = true;
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
		case KeyEvent.VK_ESCAPE:// Invert visibility
			GraphicsHandler.escapeMenuContainer.setVisible(!GraphicsHandler.escapeMenuContainer.isVisible());
			break;
		case KeyEvent.VK_R:
			clickmode_roation_index++;
			if (clickmode_roation_index >= 4) {
				clickmode_roation_index = 0;
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
		case KeyEvent.VK_SPACE:
			control_brake = false;
			break;
		}
	}

	public static void startPlaying() {
		// PhysicsHandler.reset();
		// reset();
		// mode = Mode.Playing;
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
	}

	public static double requiredForwardTranslation = 0;
	public static double requiredClockwiseRotation = 0;
	public static double requiredRightwardTranslation = 0;

	public static void setControlDoubles() {
		// w alone will set up thrusts to thrust forward
		// d alone will set up thrusts to clockwise rotation
		// w and d will set up thrusts to forward and clockwise
		// e alone will set up thrusts for rightward translation
		requiredForwardTranslation = 0;// 0 unrequired, 1 forward, -1 backward
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
		// do nothing for now
	}

	public static void doPlayingPress(MouseEvent e) {
		int screenx = e.getX();
		int screeny = e.getY();
		// see if any barrels were clicked on
		doPlayingPress(getAbsoluteVectorFromScreenCoordinates(screenx, screeny), e);
	}

	public static VectorDouble getAbsoluteVectorFromScreenCoordinates(int screenx, int screeny) {
		// VectorDouble vd = new VectorDouble(midX, -midY);
		// // Get camera position, invert as you need to slide g2d in opposite direction
		// VectorDouble cam = ClientPhysicsHandler.cameraPosition.multiply(-100);
		// // rotate the canvas (rotation is always about original origin anyway?)
		// translate.rotate(-ClientPhysicsHandler.cameraTheta); // rotate about the
		// origin of the G2D? translate so CAM is back at middle
		// //translate so CAM is in the middle (uses standard reference frame even
		// though rotated? :/
		// cam = ClientPhysicsHandler.cameraPosition.multiply(-100);
		// translate.translate(cam.x, cam.y);
		// // get the unixX and unitY of the cameras reference frame (thetaCam !=
		// raftTheta)
		// double sin = Math.sin(ClientPhysicsHandler.cameraTheta);
		// double cos = Math.cos(ClientPhysicsHandler.cameraTheta);
		// VectorDouble unitX = new VectorDouble(cos, sin);
		// VectorDouble unitY = new VectorDouble(sin, -cos);
		// //Get the relative position of the middle of the screen, and translate to it
		// vd = vd.getRelative(unitX, unitY);
		// translate.translate(vd.x, vd.y);

		double screenHeight = GraphicsHandler.frame.getHeight();
		double screenWidth = GraphicsHandler.frame.getWidth();
		double gHeight = 1080;
		double gWidth = 1920;
		// Scale for cropping mechanics - the largest scalar needs to be used, so excess
		// is cut off in the other direction
		double scaleForWidth = screenWidth / gWidth;
		double scaleForHeight = screenHeight / gHeight;
		double scale = scaleForHeight > scaleForWidth ? scaleForHeight : scaleForWidth;
		double midX = screenWidth / 2;
		double midY = screenHeight / 2;
		midX /= scale;
		midY /= scale;
		VectorDouble clicked = new VectorDouble(screenx, screeny);
		clicked.x = clicked.x * 1920 / screenWidth; // undo stretching
		clicked.y = clicked.y * 1080 / screenHeight;
		// now undo scaling
		// translate.scale(scale/scaleForWidth,scale/scaleForHeight);
		clicked.x = (clicked.x * scaleForWidth) / scale;
		clicked.y = (clicked.y * scaleForHeight) / scale;

		// finally undo location, do opposite of what was done, in reverse
		VectorDouble vd = new VectorDouble(midX, -midY);
		double sin = Math.sin(ClientPhysicsHandler.cameraTheta);
		double cos = Math.cos(ClientPhysicsHandler.cameraTheta);
		VectorDouble unitX = new VectorDouble(cos, sin);
		VectorDouble unitY = new VectorDouble(sin, -cos);
		vd = vd.getRelative(unitX, unitY);

		VectorDouble cam = ClientPhysicsHandler.cameraPosition.multiply(-100);

		// translate.rotate(-ClientPhysicsHandler.cameraTheta);
		clicked = clicked.rotate(ClientPhysicsHandler.cameraTheta);
		clicked = clicked.subtract(cam);
		clicked = clicked.subtract(vd);
		clicked = clicked.divide(100);
		// clicked = clicked.subtract(offset);// .divide(100);
		// clicked = clicked.divide(100);// convert from screen cords to absolute
		// coordinates
		return clicked;
	}

	public static void doPlayingPress(VectorDouble clicked, MouseEvent e) {
		if (SwingUtilities.isLeftMouseButton(e)) {
			if (clickMode == ClickMode.Collection) {
				doBarrelPress(clicked);
			} else if (clickMode == ClickMode.Deconstruct) {
				doDeconstructPress(clicked);
			} else if (clickMode == ClickMode.Recruiting) {
				doRecruitingPress(clicked);
			} else if (clickMode == ClickMode.DestroyingResource) {
				doDestroyingResourcePress(clicked);
			} else {
				doBuildingPress(clicked);
			}
		} else if (SwingUtilities.isRightMouseButton(e)) {
			doCancellingPress(clicked);
		} else if (SwingUtilities.isMiddleMouseButton(e)) {
			doCameraPress(clicked);
		}
	}

	public static synchronized void doBarrelPress(VectorDouble clicked) {
		entityLoop: for (WrappedEntity we : ClientPhysicsHandler.getWrappedEntities()) {
			Entity ent = we.entity;
			if (ent.entityTypeID.equals("EntityBarrel")) {
				if (ent.isAbsolute()) {
					VectorDouble vd = ent.getLoc().getPos();
					VectorDouble diff = clicked.subtract(vd);
					if (diff.x >= 0 && diff.x <= 1) {
						if (diff.y >= 0 && diff.y <= 1) {
							// ent.setPos(new VectorDouble(0,0));
							UserData ud = ClientPacketHandler.getCurrentUserData();
							if (ud != null && ud.raft != null) {
								WrappedEntity targetEntity = ClientPhysicsHandler.getEntityWrapper(ent.uuid);
								for (Task t2 : ud.raft.getAllTasks()) {
									if (t2 instanceof TaskCollect) {
										TaskCollect tc = (TaskCollect) t2;
										if (tc.getTarget() != null && tc.getTarget().entity != null) {
											if (tc.getTarget().entity.uuid.equals(targetEntity.entity.uuid)) {
												continue entityLoop; // this barrel is already being collected,
												// try the next entity however
											}
										}
									}
								}
								TaskCollect t = new TaskCollect(targetEntity);
								ud.raft.addTask(t);
							}
						}
					}
				}
			}
		}
	}

	public static synchronized void doRecruitingPress(VectorDouble clicked) {
		entityLoop: for (WrappedEntity we : ClientPhysicsHandler.getWrappedEntities()) {
			Entity ent = we.entity;
			if (ent.entityTypeID.equals("EntityCharacter")) {
				// EntityCharacter ec = (EntityCharacter) ent;
				if (ent.isAbsolute() /* && ec.ownerUUID.equals("") */) {
					VectorDouble vd = ent.getLoc().getPos();
					VectorDouble diff = clicked.subtract(vd);
					if (diff.x >= 0 && diff.x <= 1) {
						if (diff.y >= 0 && diff.y <= 1) {
							UserData ud = ClientPacketHandler.getCurrentUserData();
							if (ud != null && ud.raft != null) {
								// tr.target = new Location();
								// tr.target.setPos(ent.loc.getPos());
								// tr.target.isAbsolute = true;
								// tr.target = ent.loc;
								WrappedEntity targetEntity = ClientPhysicsHandler.getEntityWrapper(ent.uuid);
								for (Task t2 : ud.raft.getAllTasks()) {
									if (t2 instanceof TaskRecruit) {
										TaskRecruit tr2 = (TaskRecruit) t2;
										if (tr2.getTarget() != null && tr2.getTarget().entity != null) {
											if (tr2.getTarget().entity.uuid.equals(targetEntity.entity.uuid)) {
												continue entityLoop; // this entity is already being recruited,
												// try the next entity however
											}
										}
									}
								}
								TaskRecruit tr = new TaskRecruit(targetEntity);
								ud.raft.addTask(tr);
							}
						}
					}
				}
			}
		}
	}

	public static synchronized void doCancellingPress(VectorDouble clicked) {
		UserData ud = ClientPacketHandler.getCurrentUserData();
		if (ud != null && ud.raft != null) {
			ud.raft.getTasks().forEach(new Consumer<Task>() {
				@Override
				public void accept(Task t) {
					if (t.shouldCancel(clicked)) {
						ud.raft.removeTask(t);
					}
				}
			});
			for (WrappedEntity we: ClientPhysicsHandler.getWrappedEntities()) {
				if (we != null && we.entity != null && we.entity instanceof EntityCharacter) {
					EntityCharacter ec = (EntityCharacter) we.entity;
					if (ec.ownerUUID.equals(ClientPacketHandler.currentUserUUID)) {
						if (ec.currentTask != null && ec.currentTask.shouldCancel(clicked)) {
							ec.currentTask = null;//delete task
						}
					}
				}
			}
		}
	}

	public static synchronized void doCameraPress(VectorDouble clicked) {
		for (WrappedEntity we : ClientPhysicsHandler.getWrappedEntities()) {
			VectorDouble wePos = we.entity.getLoc().getPos();
			if (!we.entity.isAbsolute()) {
				UserData udTarget = ClientPacketHandler.getUserData(we.entity.getLoc().raftUUID);
				if (udTarget != null && udTarget.raft != null) {
					wePos = wePos.add(new VectorDouble(0.5, 0.5))
							.getAbsolute(udTarget.raft.getUnitX(), udTarget.raft.getUnitY()).add(udTarget.raft.getPos())
							.subtract(new VectorDouble(0.5, 0.5));
				}
			}
			if (clicked.x > wePos.x && clicked.x < wePos.x + 1) {
				if (clicked.y > wePos.y && clicked.y < wePos.y + 1) {
					// do this entity
					ClientPhysicsHandler.cameraTarget = we;
					return;
				}
			}
		} // target raft if not targetting entity
		ClientPhysicsHandler.cameraTarget = null;
	}

	public static void doBuildingPress(VectorDouble clicked) {
		// convert to relative coordinates
		UserData ud = ClientPacketHandler.getCurrentUserData();
		updateConstructionTile();
		Tile tile = ud.raft.getConstructionTile();
		if (tile == null) {
			return;
		}
		Tile resultantTile = tile;
		Location target = new Location();
		target.setPos(tile.getPos());
		target.isAbsolute = false;
		target.raftUUID = ud.uuid;
		for (Task t2 : ud.raft.getAllTasks()) {
			if (t2 instanceof TaskConstruct) {
				TaskConstruct tc = (TaskConstruct) t2;
				if (tc.getTarget().getPos().equals(target.getPos())) {
					return;
				}
			}
		}
		for (Tile til : ud.raft.getTiles()) {
			if (resultantTile.getPos().equals(til.getPos())) {
				return;
			}
		}
		TaskConstruct t = new TaskConstruct(resultantTile, ud);
		ud.raft.addTask(t);

	}

	public static void doDeconstructPress(VectorDouble clicked) {
		// convert to relative coordinates
		UserData ud = ClientPacketHandler.getCurrentUserData();
		updateConstructionTile();
		Tile tile = ud.raft.getConstructionTile();
		if (tile == null) {
			return;
		}
		Location target = new Location();
		target.setPos(tile.getPos());
		target.isAbsolute = false;
		target.raftUUID = ud.uuid;
		for (Task t2 : ud.raft.getAllTasks()) {
			if (t2 instanceof TaskDeconstruct) {
				TaskDeconstruct tc = (TaskDeconstruct) t2;
				if (tc.getTarget().getPos().equals(target.getPos())) {
					return;
				}
			}
		}
		for (Tile til : ud.raft.getTiles()) {
			if (target.getPos().equals(til.getPos())) {
				TaskDeconstruct t = new TaskDeconstruct(tile, ud);
				ud.raft.addTask(t);
				return;// actually a tile here so do it
			}
		}

	}

	public static void doDestroyingResourcePress(VectorDouble clicked) {
		// convert to relative coordinates
		UserData ud = ClientPacketHandler.getCurrentUserData();
		updateConstructionTile();
		Tile tile = ud.raft.getConstructionTile();
		if (tile == null) {
			return;
		}
		Location target = new Location();
		target.setPos(tile.getPos());
		target.isAbsolute = false;
		target.raftUUID = ud.uuid;
		for (Task t2 : ud.raft.getAllTasks()) {
			if (t2 instanceof TaskDestroyResource) {
				TaskDestroyResource tdr = (TaskDestroyResource) t2;
				if (tdr.getTarget().getPos().equals(target.getPos())) {
					return;
				}
			}
		}
		for (Tile til : ud.raft.getTiles()) {
			if (target.getPos().equals(til.getPos())) {
				TaskDestroyResource t = new TaskDestroyResource(tile, ud);
				ud.raft.addTask(t);
				return;// actually a tile here so do it
			}
		}

	}

	public static VectorDouble getBlockPosFromScreenCoordinates(int screenx, int screeeny, UserData currentUserData) {
		VectorDouble absolute = getAbsoluteVectorFromScreenCoordinates(mouseX, mouseY);
		VectorDouble relative = absolute.subtract(currentUserData.raft.getPos())
				.getRelative(currentUserData.raft.getUnitX(), currentUserData.raft.getUnitY());
		VectorDouble blockPos = new VectorDouble(Math.floor(relative.x), Math.floor(relative.y));
		return blockPos;
	}

	public static void updateConstructionTile() {
		UserData ud = ClientPacketHandler.getCurrentUserData();
		if (clickMode == ClickMode.Building || clickMode == ClickMode.Deconstruct
				|| clickMode == ClickMode.DestroyingResource) {
			Tile t;
			if (buildingType == TileType.WoodFloor) {
				t = new Tile();
			} else if (buildingType == TileType.Thruster) {
				t = new TileThruster();
				TileThruster tr = (TileThruster) t;
				tr.thrustAngle = (Math.PI * ((double) clickmode_roation_index)) / 2F;
			} else if (buildingType == TileType.AnchorSmall) {
				t = new TileAnchorSmall();
			} else {// deconstruct or destroy resource
				t = new Tile();
				t.hp = 0;
			}
			t.setPos(getBlockPosFromScreenCoordinates(mouseX, mouseY, ud));
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
			// do nothing for now
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

	@Override
	public void keyTyped(KeyEvent e) {
	}

}
