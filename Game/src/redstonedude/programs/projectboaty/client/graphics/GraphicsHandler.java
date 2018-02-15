package redstonedude.programs.projectboaty.client.graphics;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import javax.swing.JFrame;

import redstonedude.programs.projectboaty.client.control.ControlHandler;
import redstonedude.programs.projectboaty.client.net.ClientPacketHandler;
import redstonedude.programs.projectboaty.client.physics.ClientPhysicsHandler;
import redstonedude.programs.projectboaty.server.physics.VectorDouble;
import redstonedude.programs.projectboaty.shared.entity.Entity;
import redstonedude.programs.projectboaty.shared.net.UserData;
import redstonedude.programs.projectboaty.shared.raft.Tile;
import redstonedude.programs.projectboaty.shared.raft.TileHandler;
import redstonedude.programs.projectboaty.shared.raft.TileThruster;
import redstonedude.programs.projectboaty.shared.task.Task;
import redstonedude.programs.projectboaty.shared.task.TaskCollect;
import redstonedude.programs.projectboaty.shared.task.TaskConstruct;
import redstonedude.programs.projectboaty.shared.world.WorldHandler;
import redstonedude.programs.projectboaty.shared.world.WorldHandler.TerrainType;

public class GraphicsHandler {

	public static JFrame frame;

	public static Graphics2D g2d;
	public static BufferedImage backbuffer;

	public static void graphicsUpdate() {
		/*
		 * drawing occurs in a 1920*1080 virtual screen, now it needs to be scaled to
		 * the actual screen
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
			//System.out.println(ClientPhysicsHandler.cameraPosition.x + ":" + ClientPhysicsHandler.cameraPosition.y);
			float screenHeight = frame.getHeight();
			float screenWidth = frame.getWidth();
			float gHeight = 1080;
			float gWidth = 1920;
			// Scale for cropping mechanics - the largest scalar needs to be used, so excess
			// is cut off in the other direction
			float scaleForWidth = screenWidth / gWidth;
			float scaleForHeight = screenHeight / gHeight;
			float scale = scaleForHeight > scaleForWidth ? scaleForHeight : scaleForWidth;
			// screen needs to be multipled by scale, and the cameraposition(midpoint?) of g
			// needs to correspond with the midpoint of the screen.

			// graphics are stretched to fill 0,0 to width,height

			// scale to ensure that midpoint of g lines up to camera
			float midX = screenWidth / 2;
			float midY = screenHeight / 2;
			midX /= scale;
			midY /= scale;
			VectorDouble offset = new VectorDouble(midX, midY).subtract(ClientPhysicsHandler.cameraPosition.multiply(100));
			AffineTransform translate = new AffineTransform();
			// need to scale here to overcome the stretching effects of the window
			// translate.scale(scaleForHeight/scale, scaleForWidth/scale);
			translate.scale(scale / scaleForWidth, scale / scaleForHeight);
			// translate.scale(1/scale, 1/scale);
			translate.translate(offset.x, offset.y);
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
			AffineTransform trans = new AffineTransform();
			trans.scale(1 / scaleForWidth, 1 / scaleForHeight);
			g2d.transform(trans);
			// doing GUI in bottom left, so translate as well
			float transX = 0;
			float transY = screenHeight - gHeight;
			g2d.translate(transX, transY);
			if (ControlHandler.build_menu) {
				g2d.setColor(new Color(0, 0, 0, 127));
				g2d.fillRect(0, 1000, 100, 60);
				g2d.setColor(Color.WHITE);
				g2d.drawString("[W]ooden floor", 10, 1010);
				g2d.drawString("[T]hruster", 10, 1030);
				g2d.drawString("[C]ollect Barrel", 10, 1050);
			}
			g2d.setColor(new Color(0, 0, 0, 127));
			g2d.fillRect(0, 1060, 100, 20);
			g2d.setColor(Color.WHITE);
			g2d.drawString("[B]uild/Assign", 10, 1070);
			// now undo the menu transform so it can be drawn normally again
			g2d.translate(-transX, -transY);
			try {
				g2d.transform(trans.createInverse());
			} catch (NoninvertibleTransformException e) {
				e.printStackTrace();
			}
			break;
		case Connecting:
			graphicsUpdateConnecting();
			break;
		}
		frame.getGraphics().drawImage(backbuffer, 0, 0, frame.getWidth(), frame.getHeight(), frame);
	}

	public static void graphicsUpdateMenu() {
		g2d.drawString("Main menu", 50, 50);
		g2d.drawString("Press Enter to start", 50, 100);
	}

	public static void graphicsUpdateConnecting() {
		g2d.drawString("Connecting to server...", 50, 50);
		g2d.drawString("Please wait", 50, 100);
	}

	public static void graphicsUpdatePlaying() {
		// tesselate with water
		g2d.setColor(Color.BLUE);
		int index = ClientPhysicsHandler.c % 8;
		int approxX = (int) ClientPhysicsHandler.cameraPosition.x;
		int approxY = (int) ClientPhysicsHandler.cameraPosition.y;
		for (int i = approxX - 11; i < approxX + 11; i++) {
			for (int j = approxY - 7; j < approxY + 7; j++) {
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
		g2d.drawString("local phys tick " + ClientPhysicsHandler.c, 50, 50);
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
					// draw damage as well
					if (tile.hp < 75) {
						int damage = 25;
						if (tile.hp < 50) {
							damage += 25;
						}
						if (tile.hp < 25) {
							damage += 25;
						}
						if (tile instanceof TileThruster) {
							rotator = new AffineTransform();
							rotator.translate(100 * x, 100 * y);
							rotator.rotate(ud.raft.theta);
						}
						g2d.transform(rotator);
						g2d.drawImage(TextureHandler.getTexture("TileDamage_"+damage), 0, -100, 100, 0, 0, 0, 32, 32, frame);
						try {
							g2d.transform(rotator.createInverse());
						} catch (NoninvertibleTransformException e) {
							e.printStackTrace();
						}
					}
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
				//g2d.drawLine(0, 0, (int) (x*100),(int) (y*100));
				// using graphics instead of colors
				AffineTransform rotator = new AffineTransform();
				rotator.translate(100 * x, 100 * y);
				rotator.rotate(cud.raft.theta);
				g2d.transform(rotator);
				//g2d.drawImage(TextureHandler.getTexture(TileHandler.getTextureName(constructionTile)), 0, -100, 100, 0, 0, 0, 32, 32, frame);
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
				if (t instanceof TaskCollect) {
					TaskCollect tc = (TaskCollect) t;
					if (!tc.collected) {
						VectorDouble pos = tc.targetLoc;
						g2d.drawImage(TextureHandler.getTexture("TileConstruction"), (int) (100 * pos.x), (int) (100 * pos.y), (int) (100 * pos.x + 100), (int) (100 * pos.y + 100), 0, 0, 32, 32, frame);
					}
				} else if (t instanceof TaskConstruct) {
					TaskConstruct tc = (TaskConstruct) t;
					if (!tc.constructed) {
						double x = tc.resultantTile.getAbsoluteX(cud.raft);
						double y = tc.resultantTile.getAbsoluteY(cud.raft);
						// using graphics instead of colors
						AffineTransform rotator = new AffineTransform();
						rotator.translate(100 * x, 100 * y);
						rotator.rotate(cud.raft.theta);
						g2d.transform(rotator);
						g2d.drawImage(TextureHandler.getTexture("TileConstruction"), 0, -100, 100, 0, 0, 0, 32, 32, frame);
						try {
							g2d.transform(rotator.createInverse());
						} catch (NoninvertibleTransformException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}

		for (Entity e : ClientPhysicsHandler.getEntities()) {
			// System.out.println("entity");
			VectorDouble pos = e.getPos();
			if (e.absolutePosition) {
				g2d.drawImage(TextureHandler.getTexture(TileHandler.getTextureName(e.entityTypeID, e)), (int) (pos.x * 100), (int) (pos.y * 100), (int) (pos.x * 100 + 100), (int) (pos.y * 100 + 100), 0, 0, 32, 32, frame);
			} else {
				UserData ud = ClientPacketHandler.getUserData(e.raftUUID);
				if (ud != null && ud.raft != null) {
					pos = pos.getAbsolute(ud.raft.getUnitX(), ud.raft.getUnitY());
					pos = pos.add(ud.raft.getPos());
					AffineTransform rotator = new AffineTransform();
					rotator.translate(100 * pos.x, 100 * pos.y);
					rotator.rotate(ud.raft.theta);
					g2d.transform(rotator);
					g2d.drawImage(TextureHandler.getTexture(TileHandler.getTextureName(e.entityTypeID, e)), 0, -100, 100, 0, 0, 0, 32, 32, frame);
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

		if (ControlHandler.debug_menu) {
			g2d.setColor(Color.WHITE);
			g2d.drawString("1. lock position", 50, 70);
			g2d.drawString("2. spawn character", 50, 90);

		}

		if (ControlHandler.escape_menu) {
			g2d.setColor(new Color(0, 0, 0, 127));
			g2d.fillRect(480, 270, 960, 540);
			g2d.setColor(Color.WHITE);
			g2d.drawString("Press enter to return to main menu", 500, 300);
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
		// panel for graphics
		// panel for button overlay
		// buttons
		// add buttons to panel
		// add graphics to panel
		// add panels to frame
		frame.addWindowListener(new java.awt.event.WindowAdapter() {
			@Override
			public void windowClosing(java.awt.event.WindowEvent windowEvent) {
				System.out.println("exiting");
				System.exit(0);
			}
		});
		frame.setSize(960, 540);
		frame.setLocation(480, 270);
		frame.setVisible(true);
	}

}
