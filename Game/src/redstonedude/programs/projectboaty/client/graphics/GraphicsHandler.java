package redstonedude.programs.projectboaty.client.graphics;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Polygon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import redstonedude.programs.projectboaty.client.control.ControlHandler;
import redstonedude.programs.projectboaty.client.control.ControlHandler.ClickMode;
import redstonedude.programs.projectboaty.client.net.ClientPacketHandler;
import redstonedude.programs.projectboaty.client.net.ClientPacketListener;
import redstonedude.programs.projectboaty.client.physics.ClientPhysicsHandler;
import redstonedude.programs.projectboaty.shared.entity.Entity;
import redstonedude.programs.projectboaty.shared.entity.EntityBarrel;
import redstonedude.programs.projectboaty.shared.entity.EntityCharacter;
import redstonedude.programs.projectboaty.shared.entity.EntityResource;
import redstonedude.programs.projectboaty.shared.entity.WrappedEntity;
import redstonedude.programs.projectboaty.shared.net.UserData;
import redstonedude.programs.projectboaty.shared.physics.VectorDouble;
import redstonedude.programs.projectboaty.shared.raft.Tile;
import redstonedude.programs.projectboaty.shared.raft.Tile.TileType;
import redstonedude.programs.projectboaty.shared.raft.TileHandler;
import redstonedude.programs.projectboaty.shared.raft.TileThruster;
import redstonedude.programs.projectboaty.shared.task.Task;
import redstonedude.programs.projectboaty.shared.task.TaskWander;
import redstonedude.programs.projectboaty.shared.world.WorldHandler;
import redstonedude.programs.projectboaty.shared.world.WorldHandler.TerrainType;

public class GraphicsHandler {

	public static JFrame frame;
	public static JPanel graphicsPanel;
	public static JPanel menuPanel;

	public static JPanel escapeMenuContainer;

	public static Graphics2D g2d;
	public static BufferedImage backbuffer;
	public static BufferedImage worldMap = new BufferedImage(300, 300, BufferedImage.TYPE_INT_ARGB);

	public static void graphicsUpdate() {
		/*
		 * drawing occurs in a 1920*1080 virtual screen, now it needs to be scaled to the actual screen
		 */
		g2d.setColor(Color.BLACK);
		g2d.fillRect(0, 0, 1920, 1080);
		g2d.setColor(Color.WHITE);

		// cameraPosition*100 needs to line up with 960, 540

		switch (ControlHandler.mode) {
		case MainMenu:
			graphicsUpdateMenu();
			break;
		case Playing:
			// scale for standard AR, crop for nonstandard
			// System.out.println(ClientPhysicsHandler.cameraPosition.x + ":" +
			// ClientPhysicsHandler.cameraPosition.y);
			double screenHeight = frame.getHeight();
			double screenWidth = frame.getWidth();
			double gHeight = 1080;
			double gWidth = 1920;
			// Scale for cropping mechanics - the largest scalar needs to be used, so excess
			// is cut off in the other direction
			double scaleForWidth = screenWidth / gWidth;
			double scaleForHeight = screenHeight / gHeight;
			double scale = scaleForHeight > scaleForWidth ? scaleForHeight : scaleForWidth;
			// screen needs to be multipled by scale, and the cameraposition(midpoint?) of g
			// needs to correspond with the midpoint of the screen.

			// graphics are stretched to fill 0,0 to width,height
			// scale to ensure that midpoint of g lines up to camera
			double midX = screenWidth / 2;
			double midY = screenHeight / 2;
			midX /= scale;
			midY /= scale;
			AffineTransform translate = new AffineTransform();
			// need to scale here to overcome the stretching effects of the window
			translate.scale(scale / scaleForWidth, scale / scaleForHeight);
			// check for nulls
			UserData ud = ClientPacketHandler.getCurrentUserData();
			if (ud != null && ud.raft != null) {
				// System.out.println(ClientPhysicsHandler.cameraTheta);
				// Get the vector to get to the middle of the screen (Y is negative because the rafts reference frame has negative Y in relation to screen)
				VectorDouble vd = new VectorDouble(midX, -midY);
				// Get camera position, invert as you need to slide g2d in opposite direction
				VectorDouble cam = ClientPhysicsHandler.cameraPosition.multiply(-100);
				// rotate the canvas (rotation is always about original origin anyway?)
				translate.rotate(-ClientPhysicsHandler.cameraTheta); // rotate about the origin of the G2D? translate so CAM is back at middle
				// translate so CAM is in the middle (uses standard reference frame even though rotated? :/
				translate.translate(cam.x, cam.y);
				// get the unixX and unitY of the cameras reference frame (thetaCam != raftTheta)
				double sin = Math.sin(ClientPhysicsHandler.cameraTheta);
				double cos = Math.cos(ClientPhysicsHandler.cameraTheta);
				VectorDouble unitX = new VectorDouble(cos, sin);
				VectorDouble unitY = new VectorDouble(sin, -cos);
				// Get the relative position of the middle of the screen, and translate to it
				vd = vd.getRelative(unitX, unitY);
				translate.translate(vd.x, vd.y);
			}
			g2d.transform(translate);
			graphicsUpdatePlaying();
			try {
				g2d.transform(translate.createInverse());
			} catch (NoninvertibleTransformException e) {
				e.printStackTrace();
			}
			// gameplay screen has been drawn, draw menus
			// since screen will be stretched from 0,0 to width,height, we need to unscale
			// the g2d first to compensate.
			// this is not the true unscale account for cropping, it is the raw scale to
			// make text the same size
			// AffineTransform trans = new AffineTransform();
			// trans.scale(1 / scaleForWidth, 1 / scaleForHeight);
			// g2d.transform(trans);
			// // doing GUI in bottom left, so translate as well
			// float transX = 0;
			// float transY = screenHeight - gHeight;
			// g2d.translate(transX, transY);
			// g2d.setColor(new Color(0, 0, 0, 127));
			// g2d.fillRect(0, 1060, 100, 20);
			// g2d.setColor(Color.WHITE);
			// g2d.drawString("[B]uild/Assign", 10, 1070);
			// // now undo the menu transform so it can be drawn normally again
			// g2d.translate(-transX, -transY);
			// try {
			// g2d.transform(trans.createInverse());
			// } catch (NoninvertibleTransformException e) {
			// e.printStackTrace();
			// }
			break;
		case Connecting:
			graphicsUpdateConnecting();
			break;
		}
		// g2d.drawImage(frame.getGlassPane().getGraphics(), 0,0,frame);
		// graphicsPanel.getGraphics().drawImage(backbuffer, 0, 0,
		// graphicsPanel.getWidth(), graphicsPanel.getHeight(), graphicsPanel);
		// frame.getLayeredPane().moveToBack(graphicsPanel);
		// frame.getLayeredPane().validate();
		// frame.getGraphics().drawImage(backbuffer, 0, 0, frame.getWidth(),
		// frame.getHeight(), frame);
		frame.repaint();
	}

	public static void graphicsUpdateMenu() {
		// no need for menu graphics - perhaps do animated background here?
	}

	public static void graphicsUpdateConnecting() {
		g2d.drawString("Connecting to server...", 50, 50);
		g2d.drawString("Please wait", 50, 100);
	}

	public static void graphicsUpdatePlaying() {
		// tesselate with water
		g2d.setColor(Color.BLUE);
		int index = ClientPhysicsHandler.tickCount % 8;
		int approxX = (int) ClientPhysicsHandler.cameraPosition.x;
		// System.out.println(approxX);
		int approxY = (int) ClientPhysicsHandler.cameraPosition.y;
		// used to be 11 horizontal and 7 vertical. Do 12 in each direction incase orientation is at 90, or a diagonal (hence +1)
		for (int i = approxX - 12; i < approxX + 12; i++) {
			for (int j = approxY - 12; j < approxY + 12; j++) {
				int x = 100 * i;
				int y = 100 * j;
				TerrainType tt = WorldHandler.getTerrainType(i, j);
				switch (tt) {
				case Land:
					g2d.drawImage(TextureHandler.getTexture("Island"), x, y, x + 100, y + 100, 0, 0, 32, 32, frame);
					break;
				case Water:
					g2d.drawImage(TextureHandler.getTexture("Water_" + index), x, y, x + 100, y + 100, 0, 0, 32, 32, frame);
					g2d.drawRect(x, y, 100, 100);
					break;
				}
			}
		}
		g2d.setColor(Color.WHITE);
		g2d.drawString("local phys tick " + ClientPhysicsHandler.tickCount, 50, 50);
		for (UserData ud : ClientPacketHandler.userData) {
			if (ud.raft != null) {
				VectorDouble unitx = ud.raft.getUnitX();
				VectorDouble unity = ud.raft.getUnitY();
				for (Tile tile : ud.raft.getTiles()) {
					double x = tile.getAbsoluteX(ud.raft);
					double y = tile.getAbsoluteY(ud.raft);
					// using graphics instead of colors
					AffineTransform rotator = new AffineTransform();
					rotator.translate(100 * x, 100 * y);
					rotator.rotate(ud.raft.theta);
					if (tile instanceof TileThruster) {
						rotator.translate(50, -50);
						rotator.rotate(-((TileThruster) tile).thrustAngle);
						rotator.translate(-50, 50);
					}
					g2d.transform(rotator);
					g2d.drawImage(TextureHandler.getTexture(TileHandler.getTextureName(tile)), 0, -100, 100, 0, 0, 0, 32, 32, frame);
					try {
						g2d.transform(rotator.createInverse());
					} catch (NoninvertibleTransformException e) {
						e.printStackTrace();
					}
					//undo thruster rotation to draw non-rotated graphics
					if (tile instanceof TileThruster) {
						rotator = new AffineTransform();
						rotator.translate(100 * x, 100 * y);
						rotator.rotate(ud.raft.theta);
					}
					// draw damage as well
					if (tile.hp < 75) {
						int damage = 25;
						if (tile.hp < 50) {
							damage += 25;
						}
						if (tile.hp < 25) {
							damage += 25;
						}
						g2d.transform(rotator);
						g2d.drawImage(TextureHandler.getTexture("TileDamage_" + damage), 0, -100, 100, 0, 0, 0, 32, 32, frame);
						try {
							g2d.transform(rotator.createInverse());
						} catch (NoninvertibleTransformException e) {
							e.printStackTrace();
						}
					}
					//draw storage as well
					if (tile.storage.maxNumberOfStacks == 1) {
						if (tile.storage.resources.size() == 1) {
							g2d.transform(rotator);
							g2d.drawImage(TextureHandler.getTexture("Resource_" + tile.storage.resources.peek().resourceType.textureName), 0, -100, 100, 0, 0, 0, 32, 32, frame);
							try {
								g2d.transform(rotator.createInverse());
							} catch (NoninvertibleTransformException e) {
								e.printStackTrace();
							}
						}
					}
					
//					VectorDouble absoluteForce = tile.getAbsoluteMotion(ud.raft);
//					absoluteForce = absoluteForce.multiply(5000);
//					g2d.setColor(Color.RED);
//					g2d.drawLine((int) (100*tile.getAbsoluteX(ud.raft)),(int) (100*tile.getAbsoluteY(ud.raft)),(int) (100*tile.getAbsoluteX(ud.raft)+absoluteForce.x),(int) (100*tile.getAbsoluteY(ud.raft)+absoluteForce.y));
				}
				g2d.setColor(Color.WHITE);
				g2d.drawLine((int) (100 * ud.raft.getPos().x), (int) (100 * ud.raft.getPos().y), (int) (100 * ud.raft.getPos().x + 100 * ud.raft.getUnitX().x), (int) (100 * ud.raft.getPos().y + 100 * ud.raft.getUnitX().y));
				g2d.drawLine((int) (100 * ud.raft.getPos().x), (int) (100 * ud.raft.getPos().y), (int) (100 * ud.raft.getPos().x + 100 * ud.raft.getUnitY().x), (int) (100 * ud.raft.getPos().y + 100 * ud.raft.getUnitY().y));
				int x = (int) (100 * (ud.raft.getPos().x + ud.raft.getCOMPos().x * unitx.x + ud.raft.getCOMPos().y * unity.x));
				int y = (int) (100 * (ud.raft.getPos().y + ud.raft.getCOMPos().x * unitx.y + ud.raft.getCOMPos().y * unity.y));
				g2d.drawOval(x - 10, y - 10, 20, 20);
			}
		}
		// draw construction tile and task tiles
		UserData cud = ClientPacketHandler.getCurrentUserData();
		if (cud != null && cud.raft != null) {
			ControlHandler.updateConstructionTile();
			Tile constructionTile = cud.raft.getConstructionTile();
			if (constructionTile != null) {
				double x = constructionTile.getAbsoluteX(cud.raft);
				double y = constructionTile.getAbsoluteY(cud.raft);
				// g2d.drawLine(0, 0, (int) (x*100),(int) (y*100));
				// using graphics instead of colors
				AffineTransform rotator = new AffineTransform();
				rotator.translate(100 * x, 100 * y);
				rotator.rotate(cud.raft.theta);
				if (constructionTile instanceof TileThruster) {
					rotator.translate(50, -50);
					rotator.rotate(-((TileThruster) constructionTile).thrustAngle);
					rotator.translate(-50, 50);
				}
				g2d.transform(rotator);
				if (constructionTile.hp != 0) { //if not a non-build tile
					g2d.drawImage(TextureHandler.getTexture(TileHandler.getTextureName(constructionTile)), 0, -100, 100, 0, 0, 0, 32, 32, frame);
				}
				g2d.drawImage(TextureHandler.getTexture("TileConstruction"), 0, -100, 100, 0, 0, 0, 32, 32, frame);
				try {
					g2d.transform(rotator.createInverse());
				} catch (NoninvertibleTransformException e) {
					e.printStackTrace();
				}
			}
			// draw tasks
			ArrayList<Task> tasks = cud.raft.getAllTasks();
			for (Task t : tasks) {
				if (!(t instanceof TaskWander)) {
					t.draw(g2d);
				}
			}
		}

		for (WrappedEntity we : ClientPhysicsHandler.getWrappedEntities()) {
			Entity e = we.entity;
			// System.out.println("entity");
			VectorDouble pos = e.getLoc().getPos();
			if (e.isAbsolute()) {
				g2d.drawImage(TextureHandler.getTexture(TileHandler.getTextureName(e.entityTypeID, e)), (int) (pos.x * 100), (int) (pos.y * 100), (int) (pos.x * 100 + 100), (int) (pos.y * 100 + 100), 0, 0, 32, 32, frame);
				if (e instanceof EntityBarrel) {
					g2d.drawImage(TextureHandler.getTexture("Resource_" + ((EntityBarrel)e).resource.resourceType.textureName), (int) (pos.x * 100), (int) (pos.y * 100), (int) (pos.x * 100 + 100), (int) (pos.y * 100 + 100), 0, 0, 32, 32, frame);
				}
			} else {
				UserData ud = ClientPacketHandler.getUserData(e.getLoc().raftUUID);
				if (ud != null && ud.raft != null) {
					pos = pos.getAbsolute(ud.raft.getUnitX(), ud.raft.getUnitY());
					pos = pos.add(ud.raft.getPos());
					AffineTransform rotator = new AffineTransform();
					rotator.translate(100 * pos.x, 100 * pos.y);
					rotator.rotate(ud.raft.theta);
					g2d.transform(rotator);
					g2d.drawImage(TextureHandler.getTexture(TileHandler.getTextureName(e.entityTypeID, e)), 0, -100, 100, 0, 0, 0, 32, 32, frame);
					if (e instanceof EntityBarrel) {
						g2d.drawImage(TextureHandler.getTexture("Resource_" + ((EntityBarrel)e).resource.resourceType.textureName), 0, -100, 100, 0, 0, 0, 32, 32, frame);
					}
					try {
						g2d.transform(rotator.createInverse());
					} catch (NoninvertibleTransformException e1) {
						e1.printStackTrace();
					}
				}
			}
		}

		// DEBUG
		// for (DebugVector dv : DebugHandler.debugVectors) {
		// g2d.setColor(dv.color);
		// g2d.drawLine((int) (100 * dv.pos.x), (int) (100 * dv.pos.y), (int) (100 *
		// dv.pos.x + 100 * dv.vector.x), (int) (100 * dv.pos.y + 100 * dv.vector.y));

		// }
		//
		g2d.setColor(Color.RED);
		g2d.drawOval((int) (100 * ClientPhysicsHandler.cameraPosition.x - 10), (int) (100 * ClientPhysicsHandler.cameraPosition.y - 10), 20, 20);

		if (ControlHandler.debug_menu) { // very. Very. Simple
			int x = (int) ClientPhysicsHandler.cameraPosition.multiply(100).x;
			int y = (int) ClientPhysicsHandler.cameraPosition.multiply(100).y;
			g2d.setColor(new Color(0, 0, 0, 127));
			g2d.fillRect(x, y, 200, 200);
			g2d.setColor(Color.WHITE);
			g2d.drawString("1. lock position", x + 50, y + 70);
			g2d.drawString("2. spawn character", x + 50, y + 90);
			// int i = 0;
			// for (WrappedEntity e: ClientPhysicsHandler.getWrappedEntities()) {
			// if (e.entity != null && e.entity instanceof EntityCharacter) {
			// EntityCharacter ec = (EntityCharacter) e.entity;
			// if (!ec.ownerUUID.equals("")) {
			// g2d.drawString(ec.uuid + ":" + ec.ownerUUID + ":" + ec.loc.getPos(), x + 50, y + 110 + 20*i);
			// i++;
			// }
			// }
			// }
		}
	}

	public static void updateWorldMap() {
		int w = worldMap.getWidth();
		int h = worldMap.getHeight();
		UserData cud = ClientPacketHandler.getCurrentUserData();
		int Ox = 0;
		int Oy = 0;
		if (cud != null && cud.raft != null) {
			VectorDouble pos = cud.raft.getPos();
			Ox = (int) Math.floor(pos.x);
			Oy = (int) Math.floor(pos.y);
		}
		int x = Ox - w / 2;
		int y = Oy - h / 2;
		for (int i = 0; i < w; i++) {
			for (int j = 0; j < h; j++) {
				TerrainType tt = WorldHandler.getTerrainType(x + i, y + j);
				int rgb = Color.BLACK.getRGB();
				switch (tt) {
				case Land:
					rgb = Color.YELLOW.getRGB();
					break;
				case Water:
					rgb = Color.BLUE.getRGB();
					break;
				}
				// use solid circle radius 5 to show player
				if (Math.pow(x + i - Ox, 2) + Math.pow(y + j - Oy, 2) <= 25) {
					rgb = Color.WHITE.getRGB();
				}
				//circularise
				if (Math.pow(i-w/2,2)+Math.pow(j-h/2, 2) > Math.pow(w/2, 2)/*assume square*/) {
					rgb = new Color(0,0,0,0).getRGB();
				}
				worldMap.setRGB(i, j, rgb);
				
			}
		}
		// now add players
		for (UserData ud : ClientPacketHandler.userData) {
			if (ud != null && ud.raft != null && !ud.uuid.equals(ClientPacketHandler.currentUserUUID)) {
				int Rx = 0;
				int Ry = 0;
				VectorDouble Rpos = ud.raft.getPos();
				Rx = (int) Math.floor(Rpos.x);
				Ry = (int) Math.floor(Rpos.y);
				// convert to relative positions
				int Ri = Rx + w / 2 - Ox;
				int Rj = Ry + h / 2 - Oy;
				for (int Mi = -5; Mi <= 5; Mi++) {
					for (int Mj = -5; Mj <= 5; Mj++) {
						int i = Ri + Mi;
						int j = Rj + Mj;
						if (i >= 0 && i < w && j >= 0 && j < h) {
							if (Math.pow(Mi, 2) + Math.pow(Mj, 2) <= 25) {
								worldMap.setRGB(i, j, Color.RED.getRGB());
							}
						}
					}
				}
			}
		}
	}

	public static void drawSlantedRect(double absx, double absy, VectorDouble unitx, VectorDouble unity) {
		Polygon p = new Polygon();
		p.addPoint((int) (100 * absx), (int) (100 * absy));
		p.addPoint((int) (100 * absx + 100 * unitx.x), (int) (100 * absy + 100 * unitx.y));
		p.addPoint((int) (100 * absx + 100 * unitx.x + 100 * unity.x), (int) (100 * absy + 100 * unitx.y + 100 * unity.y));
		p.addPoint((int) (100 * absx + 100 * unity.x), (int) (100 * absy + 100 * unity.y));
		g2d.fillPolygon(p);
	}

	public static void drawSlantedLineOffset(double absx, double absy, double x, double y, VectorDouble unitx, VectorDouble unity, VectorDouble absoluteline) {
		g2d.drawLine((int) (100 * (absx + x * unitx.x + y * unity.x)), (int) (100 * (absy + x * unitx.y + y * unity.y)), (int) (100 * (absx + x * unitx.x + y * unity.x + absoluteline.x)), (int) (100 * (absy + x * unitx.y + y * unity.y + absoluteline.y)));
	}

	public static void init() {
		backbuffer = new BufferedImage(1920, 1080, BufferedImage.TYPE_INT_RGB);
		g2d = backbuffer.createGraphics();
		frame = new JFrame("Raft Game");

		/**
		 * GUI Building notes: Note: Alot of sizes dynamically set when JFrame resizes use .setLayout(new LayoutManagerStrictSizes()); to keep things in place and the right size buttons need .setFocusable(false); otherwise they interfere with KeyListener
		 * 
		 * 
		 */

		// Obtain LayeredPane to draw layers onto
		JLayeredPane jlp = frame.getLayeredPane();

		// Setup Graphics panel, with paint method
		graphicsPanel = new JPanel() {
			private static final long serialVersionUID = 1L;

			@Override
			public void paint(Graphics g) {
				g.drawImage(backbuffer, 0, 0, graphicsPanel.getWidth(), graphicsPanel.getHeight(), graphicsPanel);
			}
		};
		graphicsPanel.setLocation(0, 0);

		// Setup menuPanel
		menuPanel = new JPanel();
		menuPanel.setLocation(0, 0);
		menuPanel.setBackground(new Color(0, 0, 0, 0)); // Transparent
		menuPanel.setLayout(null); // Menupanel doesn't shuffle around its internal components here, it's done on
									// frame resize.

		Color menuGray = new Color(127, 127, 127);
		// entire bottomBar container (bar+popups)
		JPanel bottomBarContainer = new JPanel();
		bottomBarContainer.setPreferredSize(new Dimension(400, 230));
		bottomBarContainer.setBackground(new Color(0, 0, 0, 0));
		bottomBarContainer.setLayout(new LayoutManagerStrictSizes());
		JPanel topBarContainer = new JPanel();
		topBarContainer.setPreferredSize(new Dimension(400, 400));
		topBarContainer.setBackground(new Color(0, 0, 0, 0));
		topBarContainer.setLayout(new LayoutManagerStrictSizes());
		JPanel bottomBarButtonContainer = new JPanel();
		bottomBarButtonContainer.setPreferredSize(new Dimension(400, 20));
		bottomBarButtonContainer.setLocation(0, 210);
		bottomBarButtonContainer.setBackground(menuGray);
		bottomBarButtonContainer.setLayout(new LayoutManagerStrictSizes());
		bottomBarContainer.add(bottomBarButtonContainer);
		bottomBarContainer.setVisible(false); // hidden by default
		topBarContainer.setVisible(false);
		menuPanel.add(bottomBarContainer);
		menuPanel.add(topBarContainer);

		ArrayList<Component> popups = new ArrayList<Component>();

		final Runnable closePopups = new Runnable() {
			@Override
			public void run() {
				for (Component c : popups) {
					c.setVisible(false);
				}
			}
		};

		doBuildAssignGUI: {
			JButton buildGUIButton = new JButton("Build or Assign");
			buildGUIButton.setLocation(0, 0);
			buildGUIButton.setLayout(new LayoutManagerStrictSizes());
			buildGUIButton.setHorizontalAlignment(SwingConstants.CENTER);// Text align center
			buildGUIButton.setPreferredSize(new Dimension(100, 20));
			buildGUIButton.setMargin(new Insets(0, 0, 0, 0));// No spacing around text
			buildGUIButton.setFocusable(false);
			bottomBarButtonContainer.add(buildGUIButton);

			JPanel buildGUIPopup = new JPanel();
			buildGUIPopup.setLocation(0, 10);
			buildGUIPopup.setLayout(new LayoutManagerStrictSizes());
			buildGUIPopup.setPreferredSize(new Dimension(150, 200));// height of container-barheight
			buildGUIPopup.setBackground(menuGray);
			buildGUIPopup.setVisible(false);
			bottomBarContainer.add(buildGUIPopup);

			JButton buildGUIBuild = new JButton("Build");
			buildGUIBuild.setLocation(0, 0);
			buildGUIBuild.setLayout(new LayoutManagerStrictSizes());
			buildGUIBuild.setPreferredSize(new Dimension(150, 40));
			buildGUIBuild.setFocusable(false);
			buildGUIPopup.add(buildGUIBuild);
			JButton buildGUIBarrel = new JButton("Collect Barrels");
			buildGUIBarrel.setLocation(0, 40);
			buildGUIBarrel.setLayout(new LayoutManagerStrictSizes());
			buildGUIBarrel.setPreferredSize(new Dimension(150, 40));
			buildGUIBarrel.setFocusable(false);
			buildGUIPopup.add(buildGUIBarrel);
			JButton buildGUIDeconstruct = new JButton("Deconstruct");
			buildGUIDeconstruct.setLocation(0, 80);
			buildGUIDeconstruct.setLayout(new LayoutManagerStrictSizes());
			buildGUIDeconstruct.setPreferredSize(new Dimension(150, 40));
			buildGUIDeconstruct.setFocusable(false);
			buildGUIPopup.add(buildGUIDeconstruct);
			JButton buildGUIRecruit = new JButton("Recruit");
			buildGUIRecruit.setLocation(0, 120);
			buildGUIRecruit.setLayout(new LayoutManagerStrictSizes());
			buildGUIRecruit.setPreferredSize(new Dimension(150, 40));
			buildGUIRecruit.setFocusable(false);
			buildGUIPopup.add(buildGUIRecruit);
			JButton buildGUIDestroy = new JButton("Destroy Resource");
			buildGUIDestroy.setLocation(0, 160);
			buildGUIDestroy.setLayout(new LayoutManagerStrictSizes());
			buildGUIDestroy.setPreferredSize(new Dimension(150, 40));
			buildGUIDestroy.setFocusable(false);
			buildGUIPopup.add(buildGUIDestroy);
			popups.add(buildGUIPopup);
			
			JPanel buildGUIBuildsPopups = new JPanel();

			buildGUIButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if (buildGUIPopup.isVisible()) {
						buildGUIPopup.setVisible(false);
					} else {
						closePopups.run();
						buildGUIPopup.setVisible(true);
					}
					// make button depressed?
				}
			});
			
			ArrayList<Component> buildsPopups = new ArrayList<Component>();
			buildsPopups.add(buildGUIBuildsPopups);

			final Runnable closeBuildsPopups = new Runnable() {
				@Override
				public void run() {
					for (Component c : buildsPopups) {
						c.setVisible(false);
					}
				}
			};
			
			buildGUIBuild.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					buildGUIBuildsPopups.setVisible(!buildGUIBuildsPopups.isVisible());
				}
			});
			buildGUIBarrel.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					closeBuildsPopups.run();
					ControlHandler.clickMode = ClickMode.Collection;
					// make button depressed?
				}
			});
			buildGUIDeconstruct.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					closeBuildsPopups.run();
					ControlHandler.clickMode = ClickMode.Deconstruct;
					// make button depressed?
				}
			});
			buildGUIRecruit.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					closeBuildsPopups.run();
					ControlHandler.clickMode = ClickMode.Recruiting;
					// make button depressed?
				}
			});
			buildGUIDestroy.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					closeBuildsPopups.run();
					ControlHandler.clickMode = ClickMode.DestroyingResource;
					// make button depressed?
				}
			});
			
			doBuildGUI: {
				buildGUIBuildsPopups.setLocation(150, 50);
				buildGUIBuildsPopups.setLayout(new LayoutManagerStrictSizes());
				buildGUIBuildsPopups.setPreferredSize(new Dimension(150, 160));
				buildGUIBuildsPopups.setBackground(menuGray);
				buildGUIBuildsPopups.setVisible(false);
				popups.add(buildGUIBuildsPopups);
				bottomBarContainer.add(buildGUIBuildsPopups);
				
				int yval = 0;
				for (TileType tt: Tile.TileType.values()) {
					JButton buildGUITile = new JButton("Build " + tt.textureName);
					buildGUITile.setLocation(0, yval);
					buildGUITile.setLayout(new LayoutManagerStrictSizes());
					buildGUITile.setPreferredSize(new Dimension(150, 20));
					buildGUITile.setFocusable(false);
					buildGUIBuildsPopups.add(buildGUITile);
					
					buildGUITile.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							ControlHandler.clickMode = ClickMode.Building;
							ControlHandler.buildingType = tt;
							// make button depressed?
						}
					});
					yval += 20;
				}
				
				
//				JButton buildGUIWood = new JButton("Build Wooden Floor");
//				buildGUIWood.setLocation(0, 0);
//				buildGUIWood.setLayout(new LayoutManagerStrictSizes());
//				buildGUIWood.setPreferredSize(new Dimension(150, 20));
//				buildGUIWood.setFocusable(false);
//				buildGUIBuildsPopups.add(buildGUIWood);
//				JButton buildGUIThruster = new JButton("Build Thruster");
//				buildGUIThruster.setLocation(0, 20);
//				buildGUIThruster.setLayout(new LayoutManagerStrictSizes());
//				buildGUIThruster.setPreferredSize(new Dimension(150, 20));
//				buildGUIThruster.setFocusable(false);
//				buildGUIBuildsPopups.add(buildGUIThruster);
//				
//				buildGUIWood.addActionListener(new ActionListener() {
//					@Override
//					public void actionPerformed(ActionEvent e) {
//						ControlHandler.clickMode = ClickMode.Building;
//						ControlHandler.buildingType = TileType.Wood;
//						// make button depressed?
//					}
//				});
//				buildGUIThruster.addActionListener(new ActionListener() {
//					@Override
//					public void actionPerformed(ActionEvent e) {
//						ControlHandler.clickMode = ClickMode.Building;
//						ControlHandler.buildingType = TileType.Thruster;
//						// make button depressed?
//					}
//				});
			}
		}
		final JPanel mapGUIPopin = new JPanel();
		doMapGUI: {
			JButton mapGUIButton = new JButton("World Map");
			mapGUIButton.setLocation(100, 0);
			mapGUIButton.setLayout(new LayoutManagerStrictSizes());
			mapGUIButton.setHorizontalAlignment(SwingConstants.CENTER);// Text align center
			mapGUIButton.setPreferredSize(new Dimension(100, 20));
			mapGUIButton.setMargin(new Insets(0, 0, 0, 0));// No spacing around text
			mapGUIButton.setFocusable(false);
			bottomBarButtonContainer.add(mapGUIButton);

			mapGUIPopin.setLocation(150, 0);
			mapGUIPopin.setLayout(new LayoutManagerStrictSizes());
			mapGUIPopin.setPreferredSize(new Dimension(400, 400));
			mapGUIPopin.setBackground(menuGray);
			mapGUIPopin.setVisible(false);
			menuPanel.add(mapGUIPopin);
			popups.add(mapGUIPopin);

			JLabel mapGUImap = new JLabel(new Icon() {
				int frameCount = 0;
				@Override
				public void paintIcon(Component c, Graphics g, int x, int y) {
					g.drawImage(worldMap, x, y, frame);
					frameCount++;
					if (frameCount%20 == 0) {
						updateWorldMap();
					}
				}

				@Override
				public int getIconWidth() {
					return worldMap.getWidth();
				}

				@Override
				public int getIconHeight() {
					return worldMap.getHeight();
				}
			});
			mapGUImap.setLocation(50, 50);
			mapGUImap.setLayout(new LayoutManagerStrictSizes());
			mapGUImap.setPreferredSize(new Dimension(300, 300));
			mapGUIPopin.add(mapGUImap);

			mapGUIButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if (mapGUIPopin.isVisible()) {
						mapGUIPopin.setVisible(false);
					} else {
						closePopups.run();
						updateWorldMap();
						mapGUIPopin.setVisible(true);
					}
					// make button depressed?
				}
			});
		}
		doCharacterGUI: {
			JButton characterGUIButton = new JButton("Characters");
			characterGUIButton.setLocation(200, 0);
			characterGUIButton.setLayout(new LayoutManagerStrictSizes());
			characterGUIButton.setHorizontalAlignment(SwingConstants.CENTER);// Text align center
			characterGUIButton.setPreferredSize(new Dimension(100, 20));
			characterGUIButton.setMargin(new Insets(0, 0, 0, 0));// No spacing around text
			characterGUIButton.setFocusable(false);
			bottomBarButtonContainer.add(characterGUIButton);

			JPanel characterGUIPopup = new JPanel();
			characterGUIPopup.setLocation(0, 0);
			characterGUIPopup.setLayout(new LayoutManagerStrictSizes());
			characterGUIPopup.setPreferredSize(new Dimension(400, 130));// height of container-barheight
			characterGUIPopup.setBackground(menuGray);
			characterGUIPopup.setVisible(false);
			bottomBarContainer.add(characterGUIPopup);
			popups.add(characterGUIPopup);

			JLabel characterGUITitle = new JLabel("Characters:");
			characterGUITitle.setLocation(10, 0);
			characterGUITitle.setLayout(new LayoutManagerStrictSizes());
			characterGUITitle.setPreferredSize(new Dimension(390, 20));// height of container-barheight
			characterGUITitle.setBackground(menuGray);
			characterGUIPopup.add(characterGUITitle);

			CustomScrollablePane characterGUIList = new CustomScrollablePane();
			characterGUIList.setPreferredSize(new Dimension(380, 100));
			characterGUIList.setLayout(new LayoutManagerStrictSizes());// must be called after size
			characterGUIList.setLocation(10, 20);// must be called after size
			// characterGUIList.setVisible(false);
			characterGUIPopup.add(characterGUIList.scrollbar);
			characterGUIPopup.add(characterGUIList.viewport);

			characterGUIList.update = new Runnable() {
				@Override
				public void run() {
					int y = 0;
					int index = 1;
					int maxComponentIndex = characterGUIList.getComponents().length - 1;
					UserData ud = ClientPacketHandler.getCurrentUserData();
					if (ud != null && ud.raft != null) {
						for (WrappedEntity we : ClientPhysicsHandler.getWrappedEntities()) {
							Entity ent = we.entity;
							if (ent instanceof EntityCharacter) {
								EntityCharacter ec = (EntityCharacter) ent;
								if (ec.ownerUUID.equals(ClientPacketHandler.currentUserUUID)) {
									// do this EC
									String ecstring = "";
									if (ec.currentTask.taskTypeID.equals("TaskWander")) {
										ecstring += "Idle-";
									} else {
										ecstring += "Busy-";
									}
									if (ec.isAbsolute()) {
										int distance = (int) Math.sqrt(ec.getLoc().getPos().subtract(ud.raft.getPos().add(new VectorDouble(0.5, 0.5))).getSquaredLength());
										ecstring += distance + "m from raft";
									} else {
										VectorDouble pos = ec.getLoc().getPos();
										ecstring += "Aboard raft(" + ((int) Math.floor(pos.x)) + "," + ((int) Math.floor(pos.y)) + ")";
									}
									if (index - 1 <= maxComponentIndex) {
										JLabel lab = (JLabel) characterGUIList.getComponent(index - 1);
										lab.setText("Character " + index + ": " + ecstring);
									} else {
										JLabel lab = new JLabel("Task " + index + ": " + ecstring);
										lab.setLocation(0, y - characterGUIList.getValue());
										lab.setLayout(new LayoutManagerStrictSizes());
										lab.setPreferredSize(new Dimension(400, 20));
										lab.setBackground(menuGray);
										characterGUIList.add(lab);
									}
									y += 20;
									index++;
								}
							}
						}
						while (index - 1 <= maxComponentIndex) {
							characterGUIList.remove(index - 1);// remove this component
							index++;
						}
						characterGUIList.setMaximum(y - 80);
					}
				}
			};
			characterGUIButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if (characterGUIPopup.isVisible()) {
						characterGUIPopup.setVisible(false);
					} else {
						closePopups.run();
						int y = 0;
						int index = 1;
						characterGUIList.removeAll();
						UserData ud = ClientPacketHandler.getCurrentUserData();
						if (ud != null && ud.raft != null) {
							for (WrappedEntity we : ClientPhysicsHandler.getWrappedEntities()) {
								Entity ent = we.entity;
								if (ent instanceof EntityCharacter) {
									EntityCharacter ec = (EntityCharacter) ent;
									if (ec.ownerUUID.equals(ClientPacketHandler.currentUserUUID)) {
										// do this EC
										String ecstring = "";
										if (ec.currentTask.taskTypeID.equals("TaskWander")) {
											ecstring += "Idle-";
										} else {
											ecstring += "Busy-";
										}
										if (ec.isAbsolute()) {
											int distance = (int) Math.sqrt(ec.getLoc().getPos().subtract(ud.raft.getPos()).getSquaredLength());
											ecstring += distance + "m from raft";
										} else {
											VectorDouble pos = ec.getLoc().getPos();
											ecstring += "Aboard raft(" + ((int) Math.floor(pos.x)) + "," + ((int) Math.floor(pos.y)) + ")";
										}
										JLabel lab = new JLabel("Character " + index + ": " + ecstring);
										lab.setLocation(0, y - characterGUIList.getValue());
										lab.setLayout(new LayoutManagerStrictSizes());
										lab.setPreferredSize(new Dimension(400, 20));
										lab.setBackground(menuGray);
										characterGUIList.add(lab);
										y += 20;
										index++;
									}
								}
							}
						}
						characterGUIList.setMaximum(y - 80);// heightofVP-heightoftextline?
						characterGUIPopup.setVisible(true);
						// visibility changed, reset scrollbar
						characterGUIList.scrollbar.setValue(0);
						characterGUIList.scrollbar.setMinimum(0);
					}
				}
			});
		}

		doTaskGUI: {
			JButton taskGUIButton = new JButton("Tasks");
			taskGUIButton.setLocation(300, 0);
			taskGUIButton.setLayout(new LayoutManagerStrictSizes());
			taskGUIButton.setHorizontalAlignment(SwingConstants.CENTER);// Text align center
			taskGUIButton.setPreferredSize(new Dimension(100, 20));
			taskGUIButton.setMargin(new Insets(0, 0, 0, 0));// No spacing around text
			taskGUIButton.setFocusable(false);
			bottomBarButtonContainer.add(taskGUIButton);

			JPanel taskGUIPopup = new JPanel();
			taskGUIPopup.setLocation(0, 0);
			taskGUIPopup.setLayout(new LayoutManagerStrictSizes());
			taskGUIPopup.setPreferredSize(new Dimension(400, 130));// height of container-barheight
			taskGUIPopup.setBackground(menuGray);
			taskGUIPopup.setVisible(false);
			bottomBarContainer.add(taskGUIPopup);
			popups.add(taskGUIPopup);

			JLabel taskGUITitle = new JLabel("Tasks:");
			taskGUITitle.setLocation(10, 0);
			taskGUITitle.setLayout(new LayoutManagerStrictSizes());
			taskGUITitle.setPreferredSize(new Dimension(390, 20));// height of container-barheight
			taskGUITitle.setBackground(menuGray);
			taskGUIPopup.add(taskGUITitle);

			CustomScrollablePane taskGUIList = new CustomScrollablePane();
			taskGUIList.setPreferredSize(new Dimension(380, 100));
			taskGUIList.setLayout(new LayoutManagerStrictSizes());// must be called after size
			taskGUIList.setLocation(10, 20);// must be called after size
			// characterGUIList.setVisible(false);
			taskGUIPopup.add(taskGUIList.scrollbar);
			taskGUIPopup.add(taskGUIList.viewport);

			taskGUIList.update = new Runnable() {
				@Override
				public void run() {
					int y = 0;
					int index = 1;
					int maxComponentIndex = taskGUIList.getComponents().length - 1;
					UserData ud = ClientPacketHandler.getCurrentUserData();
					if (ud != null && ud.raft != null) {
						for (Task t : ud.raft.getAllTasksNotWander()) {
							// do this Task
							String ecstring = t.taskTypeID + " - ";
							if (t.isInProgress) {
								ecstring += "In progress";
							} else {
								ecstring += "Queued";
							}
							if (index - 1 <= maxComponentIndex) {
								JLabel lab = (JLabel) taskGUIList.getComponent(index - 1);
								lab.setText("Task " + index + ": " + ecstring);
							} else {
								JLabel lab = new JLabel("Task " + index + ": " + ecstring);
								lab.setLocation(0, y - taskGUIList.getValue());
								lab.setLayout(new LayoutManagerStrictSizes());
								lab.setPreferredSize(new Dimension(400, 20));
								lab.setBackground(menuGray);
								taskGUIList.add(lab);
							}
							index++;
							y += 20;
						}
						while (index - 1 <= maxComponentIndex) {
							try {
								taskGUIList.remove(index - 1);// remove this component
							} catch (IndexOutOfBoundsException e) {
								//index out of bounds, something weird happened but its happened before
								e.printStackTrace();
								//just go on anyway
							}
							index++;
						}
						taskGUIList.setMaximum(y - 80);
					}
				}
			};
			taskGUIButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if (taskGUIPopup.isVisible()) {
						taskGUIPopup.setVisible(false);
					} else {
						closePopups.run();
						int y = 0;
						int index = 1;
						taskGUIList.removeAll();
						UserData ud = ClientPacketHandler.getCurrentUserData();
						if (ud != null && ud.raft != null) {
							for (Task t : ud.raft.getAllTasksNotWander()) {
								// do this Task
								String ecstring = t.taskTypeID + " - ";
								ecstring += "Loading";
								JLabel lab = new JLabel("Task " + index + ": " + ecstring);
								lab.setLocation(0, y - taskGUIList.getValue());
								lab.setLayout(new LayoutManagerStrictSizes());
								lab.setPreferredSize(new Dimension(400, 20));
								lab.setBackground(menuGray);
								taskGUIList.add(lab);
								y += 20;
								index++;
							}
						}
						taskGUIList.setMaximum(y - 80);// heightofVP-heightoftextline?
						taskGUIPopup.setVisible(true);
						// visibility changed, reset scrollbar
						taskGUIList.scrollbar.setValue(0);
						taskGUIList.scrollbar.setMinimum(0);
					}
				}
			});
		}

		// JPanel chatContainer = new JPanel();
		/*
		 * doChat: { // entire chat container (typing+fields) chatContainer.setPreferredSize(new Dimension(400, 500)); chatContainer.setBackground(new Color(0, 0, 0, 0)); chatContainer.setLayout(new LayoutManagerStrictSizes());
		 * 
		 * JPanel chatMessagesContainer = new JPanel(); chatMessagesContainer.setPreferredSize(new Dimension(380, 450)); chatMessagesContainer.setLocation(10, 10); chatMessagesContainer.setBackground(new Color(menuGray.getRed(),menuGray.getGreen(),menuGray.getBlue(),127)); chatMessagesContainer.setLayout(new LayoutManagerStrictSizes()); chatContainer.add(chatMessagesContainer); chatContainer.setVisible(false); // hidden by default menuPanel.add(chatContainer);
		 * 
		 * JTextField chatLine = new JTextField(); chatLine.setPreferredSize(new Dimension(380, 20)); chatLine.setLocation(10, 470); chatLine.setBackground(new Color(menuGray.getRed(),menuGray.getGreen(),menuGray.getBlue(),127)); chatLine.setLayout(new LayoutManagerStrictSizes()); chatLine.setFocusable(false); chatContainer.add(chatLine); }
		 */

		// MainMenu
		JPanel mainMenuContainer = new JPanel();
		doMainMenu: {
			mainMenuContainer.setPreferredSize(new Dimension(300, 300));
			mainMenuContainer.setBackground(new Color(0, 0, 0, 0));
			mainMenuContainer.setLayout(new LayoutManagerStrictSizes());
			JLabel mainMenuText = new JLabel("Main Menu");
			mainMenuText.setPreferredSize(new Dimension(300, 100));
			mainMenuText.setLayout(new LayoutManagerStrictSizes());
			mainMenuText.setLocation(0, 0);
			mainMenuText.setForeground(Color.WHITE);
			mainMenuText.setHorizontalAlignment(SwingConstants.CENTER);
			mainMenuText.setFont(mainMenuText.getFont().deriveFont(Font.BOLD, 30));// big, bold
			mainMenuContainer.add(mainMenuText);
			menuPanel.add(mainMenuContainer);
			JButton mainMenuJoin = new JButton("Join Server");
			mainMenuJoin.setPreferredSize(new Dimension(300, 100));
			mainMenuJoin.setLayout(new LayoutManagerStrictSizes());
			mainMenuJoin.setLocation(0, 100);
			mainMenuJoin.setFocusable(false);
			mainMenuJoin.setHorizontalAlignment(SwingConstants.CENTER);
			mainMenuJoin.setFont(mainMenuJoin.getFont().deriveFont(Font.BOLD, 30));// big, bold
			mainMenuContainer.add(mainMenuJoin);
			mainMenuJoin.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					bottomBarContainer.setVisible(true);
					topBarContainer.setVisible(true);
					// chatContainer.setVisible(true);
					mainMenuContainer.setVisible(false);
					ControlHandler.startPlaying();
				}
			});
		}

		// EscapeMenu
		escapeMenuContainer = new JPanel();
		doEscapeMenu: {
			escapeMenuContainer.setPreferredSize(new Dimension(300, 300));
			escapeMenuContainer.setBackground(new Color(0, 0, 0, 127));
			escapeMenuContainer.setLayout(new LayoutManagerStrictSizes());
			escapeMenuContainer.setVisible(false);
			JLabel escapeMenuText = new JLabel("Escape Menu");
			escapeMenuText.setPreferredSize(new Dimension(300, 100));
			escapeMenuText.setLayout(new LayoutManagerStrictSizes());
			escapeMenuText.setLocation(0, 0);
			escapeMenuText.setForeground(Color.WHITE);
			escapeMenuText.setHorizontalAlignment(SwingConstants.CENTER);
			escapeMenuText.setFont(escapeMenuText.getFont().deriveFont(Font.BOLD, 30));// big, bold
			escapeMenuContainer.add(escapeMenuText);
			menuPanel.add(escapeMenuContainer);

			JButton escapeMenuDisconnect = new JButton("Disconnect");
			escapeMenuDisconnect.setPreferredSize(new Dimension(300, 100));
			escapeMenuDisconnect.setLayout(new LayoutManagerStrictSizes());
			escapeMenuDisconnect.setLocation(0, 100);
			escapeMenuDisconnect.setFocusable(false);
			escapeMenuDisconnect.setHorizontalAlignment(SwingConstants.CENTER);
			escapeMenuDisconnect.setFont(escapeMenuDisconnect.getFont().deriveFont(Font.BOLD, 30));// big, bold
			escapeMenuContainer.add(escapeMenuDisconnect);
			escapeMenuDisconnect.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					ClientPacketListener.disconnect();
				}
			});
			JButton escapeMenuResume = new JButton("Resume");
			escapeMenuResume.setPreferredSize(new Dimension(300, 100));
			escapeMenuResume.setLayout(new LayoutManagerStrictSizes());
			escapeMenuResume.setLocation(0, 200);
			escapeMenuResume.setFocusable(false);
			escapeMenuResume.setHorizontalAlignment(SwingConstants.CENTER);
			escapeMenuResume.setFont(escapeMenuResume.getFont().deriveFont(Font.BOLD, 30));// big, bold
			escapeMenuContainer.add(escapeMenuResume);
			escapeMenuResume.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					escapeMenuContainer.setVisible(false);
				}
			});
		}
		//Do resources GUI
		final JPanel resourcesGUI = new JPanel() {
			private static final long serialVersionUID = 1L;
			@Override
			public void paint(Graphics g) {
				int index = 0;
				UserData ud = ClientPacketHandler.getCurrentUserData();
				if (ud != null && ud.raft != null) {
					g.setColor(Color.BLACK);
					for (EntityResource er: ud.raft.getTotalResources()) {
						if (er.quantity != 0) {
							int x = 0;
							int y = 15+index*20;
							g.drawString(er.quantity + " " + er.resourceType.textureName, 20, 30+index*20);
							g.drawImage(TextureHandler.getTexture("Resource_"+er.resourceType.textureName),x,y,20,20, frame);
							index++;
						}
					}
				}
			}
		};
		doResourcesGUI: {
			resourcesGUI.setLocation(0, 0);
			resourcesGUI.setLayout(new LayoutManagerStrictSizes());
			resourcesGUI.setPreferredSize(new Dimension(400, 400));
			resourcesGUI.setVisible(true);
			topBarContainer.add(resourcesGUI);
		}

		// Add layers to pane, Add menuPanel first so menuPanel is on top
		jlp.add(menuPanel);
		jlp.add(graphicsPanel);
		// Note: click and key listeners are attached to frame not layer, so no need to
		// worry about that
		frame.addWindowListener(new java.awt.event.WindowAdapter() {
			@Override
			public void windowClosing(java.awt.event.WindowEvent windowEvent) {
				System.out.println("exiting");
				System.exit(0);
			}
		});
		frame.addComponentListener(new ComponentListener() {
			@Override
			public void componentShown(ComponentEvent e) {
			}

			@Override
			public void componentResized(ComponentEvent e) {
				// Set to layered pane size (frame size is actually larger
				Dimension size = frame.getLayeredPane().getSize();
				graphicsPanel.setSize(size);
				menuPanel.setSize(size);
				// set the location for components that hug the sides
				//resourcesGUI.setLocation(0, 0);
				topBarContainer.setLocation(0, 0);
				bottomBarContainer.setLocation(0, size.height - bottomBarContainer.getHeight());
				// chatContainer.setLocation(size.width-chatContainer.getWidth(), size.height - chatContainer.getHeight());
				mainMenuContainer.setLocation((size.width - mainMenuContainer.getWidth()) / 2, (size.height - mainMenuContainer.getHeight()) / 2);
				mapGUIPopin.setLocation((size.width - mapGUIPopin.getWidth()) / 2, (size.height - mapGUIPopin.getHeight()) / 2);
				escapeMenuContainer.setLocation((size.width - mainMenuContainer.getWidth()) / 2, (size.height - mainMenuContainer.getHeight()) / 2);

			}

			@Override
			public void componentMoved(ComponentEvent e) {
			}

			@Override
			public void componentHidden(ComponentEvent e) {
			}
		});
		frame.setSize(960, 540);
		frame.setLocation(480, 270);
		frame.setVisible(true);
	}

}
