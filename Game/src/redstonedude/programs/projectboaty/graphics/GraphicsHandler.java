package redstonedude.programs.projectboaty.graphics;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;

import redstonedude.programs.projectboaty.control.ControlHandler;
import redstonedude.programs.projectboaty.physics.PhysicsHandler;
import redstonedude.programs.projectboaty.physics.VectorDouble;
import redstonedude.programs.projectboaty.raft.Tile;
import redstonedude.programs.projectboaty.raft.TileHandler;
import redstonedude.programs.projectboaty.raft.TileThruster;

public class GraphicsHandler {

	public static JFrame frame;
	public static Graphics2D g2d;
	public static BufferedImage backbuffer;

	public static void graphicsUpdate() {
		/*
		 * drawing occurs in a 1920*1080 virtual screen, now it needs to be scaled to the actual screen
		 */
		g2d.setColor(Color.BLACK);
		g2d.fillRect(0, 0, 1920, 1080);
		g2d.setColor(Color.WHITE);
		
		//cameraPosition*100 needs to line up with 960, 540
		
		switch (ControlHandler.mode) {
		case MainMenu:
			graphicsUpdateMenu();
			break;
		case Playing:
			VectorDouble offset = new VectorDouble(960,540).subtract(PhysicsHandler.cameraPosition.multiply(100));
			AffineTransform translate = new AffineTransform();
			translate.translate(offset.x, offset.y);
			g2d.transform(translate);
			graphicsUpdatePlaying();
			try {
				g2d.transform(translate.createInverse());
			} catch (NoninvertibleTransformException e) {
				e.printStackTrace();
			}
			break;
		}
		
		frame.getGraphics().drawImage(backbuffer, 0, 0, frame.getWidth(), frame.getHeight(), frame);
	}

	public static void graphicsUpdateMenu() {
		g2d.drawString("Main menu", 50, 50);
		g2d.drawString("Press Enter to start", 50, 100);
	}

	public static void graphicsUpdatePlaying() {
		// tesselate with water
		g2d.setColor(Color.BLUE);
		int index = PhysicsHandler.c % 4;
		for (int i = 0; i < 15; i++) {
			for (int j = 0; j < 10; j++) {
				int x = 100 * i;
				int y = 100 * j;
				g2d.drawImage(TextureHandler.getTexture("Water_" + index), x, y, x + 100, y + 100, 0, 0, 32, 32, frame);
				g2d.drawRect(x, y, 100, 100);
			}
		}
		g2d.setColor(Color.WHITE);
		g2d.drawString("phys tick " + PhysicsHandler.c, 50, 50);
		if (PhysicsHandler.raft != null) {
			VectorDouble unitx = PhysicsHandler.raft.getUnitX();
			VectorDouble unity = PhysicsHandler.raft.getUnitY();
			for (Tile tile : PhysicsHandler.raft.tiles) {
				double x = tile.getAbsoluteX(PhysicsHandler.raft);
				double y = tile.getAbsoluteY(PhysicsHandler.raft);
				// using graphics instead of colors
				AffineTransform rotator = new AffineTransform();
				rotator.translate(100 * x, 100 * y);
				rotator.rotate(PhysicsHandler.raft.theta);
				if (tile instanceof TileThruster) {

					rotator.translate(50, -50);
					rotator.rotate(-((TileThruster)tile).thrustAngle);
					rotator.translate(-50, 50);
				}
				g2d.transform(rotator);
				g2d.drawImage(TextureHandler.getTexture(TileHandler.getTextureName(tile)), 0, -100, 100, 0, 0, 0, 32, 32, frame);
				try {
					g2d.transform(rotator.createInverse());
				} catch (NoninvertibleTransformException e) {
					e.printStackTrace();
				}
				
				//DEBUG
				//if (tile instanceof TileThruster) {
					// also draw thrust vector, in the direction of force
					//g2d.setColor(Color.GREEN);
					//VectorDouble force = ((TileThruster) tile).getAbsoluteThrustVector(PhysicsHandler.raft);
					//force = force.multiply(10);
					//drawSlantedLineOffset(x, y, 0.5, 0.5, unitx, unity, force);
				//}
				//draw drag vector
				//g2d.setColor(Color.RED);
				//VectorDouble displacement = tile.getPos().add(new VectorDouble(0.5, 0.5)).subtract(PhysicsHandler.raft.getCOMPos());
				//VectorDouble rotationalVelocity = new VectorDouble(displacement).rotate(-Math.PI/2).setMagnitude(PhysicsHandler.raft.dtheta * Math.sqrt(displacement.getSquaredLength()));
				
				//drawSlantedLineOffset(x, y, 0.5, 0.5, unitx, unity, displacement);
				//g2d.setColor(Color.GREEN);
				//drawSlantedLineOffset(x, y, 0.5, 0.5, unitx, unity, displacement.rotate(-Math.PI/2));
				//g2d.setColor(Color.BLUE);
				//drawSlantedLineOffset(x, y, 0.5, 0.5, unitx, unity, rotationalVelocity);
				//VectorDouble drag = tile.getAbsoluteFrictionVector(PhysicsHandler.raft);
				//drag = drag.multiply(10);
				//drawSlantedLineOffset(x, y, 0.5, 0.5, unitx, unity, drag);
				
				/*g2d.setColor(Color.BLUE);
				VectorDouble dpos = new VectorDouble(tile.getPos());
				dpos.add(new VectorDouble(0.5, 0.5));
				dpos.subtract(PhysicsHandler.raft.getCOMPos());
				VectorDouble absDpos = new VectorDouble();
				absDpos.x = dpos.x*PhysicsHandler.raft.getUnitX().x+dpos.y*PhysicsHandler.raft.getUnitY().x;
				absDpos.y = dpos.x*PhysicsHandler.raft.getUnitX().y+dpos.y*PhysicsHandler.raft.getUnitY().y;
				drawSlantedLineOffset(x, y, 0.5, 0.5, unitx, unity, absDpos);
				*/
				/*g2d.setColor(Color.BLUE);
				VectorDouble displacement = tile.getPos();
				displacement.add(new VectorDouble(0.5, 0.5));
				displacement.subtract(PhysicsHandler.raft.getCOMPos());
				//need to get vector at 90 clockwise rotation to it.
				VectorDouble rotationalVelocity = new VectorDouble(displacement);
				rotationalVelocity.rotate(-Math.PI/2);
				rotationalVelocity.setMagnitude(PhysicsHandler.raft.dtheta * Math.sqrt(displacement.getSquaredLength()));
				rotationalVelocity.multiply(20); //now transform
				VectorDouble absRot = new VectorDouble();
				absRot.x = rotationalVelocity.x*PhysicsHandler.raft.getUnitX().x+rotationalVelocity.y*PhysicsHandler.raft.getUnitY().x;
				absRot.y = rotationalVelocity.x*PhysicsHandler.raft.getUnitX().y+rotationalVelocity.y*PhysicsHandler.raft.getUnitY().y;
				drawSlantedLineOffset(x, y, 0.5, 0.5, unitx, unity, absRot);*/
				
				/*VectorDouble dpos = new VectorDouble(tile.getPos());
				dpos.add(new VectorDouble(0.5, 0.5));
				dpos.subtract(PhysicsHandler.raft.getCOMPos());//this is relative dpos, calculate absolute
				VectorDouble absDpos = new VectorDouble();
				absDpos.x = dpos.x*PhysicsHandler.raft.getUnitX().x+dpos.y*PhysicsHandler.raft.getUnitY().x;
				absDpos.y = dpos.x*PhysicsHandler.raft.getUnitX().y+dpos.y*PhysicsHandler.raft.getUnitY().y;
				
				if (tile instanceof TileThruster) {
					TileThruster thruster = (TileThruster) tile;
					VectorDouble force = new VectorDouble(thruster.getRelativeThrustVector());
					drawSlantedLineOffset(x, y, 0.5, 0.5, unitx, unity, force);
				}
				//do drag also
				VectorDouble drag2 = tile.getRelativeFrictionVector(PhysicsHandler.raft);
				drag2.multiply(5);
				drawSlantedLineOffset(x, y, 0.5, 0.5, unitx, unity, drag2);*/
			}
			g2d.setColor(Color.WHITE);
			g2d.drawLine((int) (100 * PhysicsHandler.raft.getPos().x), (int) (100 * PhysicsHandler.raft.getPos().y), (int) (100 * PhysicsHandler.raft.getPos().x + 100 * PhysicsHandler.raft.getUnitX().x), (int) (100 * PhysicsHandler.raft.getPos().y + 100 * PhysicsHandler.raft.getUnitX().y));
			g2d.drawLine((int) (100 * PhysicsHandler.raft.getPos().x), (int) (100 * PhysicsHandler.raft.getPos().y), (int) (100 * PhysicsHandler.raft.getPos().x + 100 * PhysicsHandler.raft.getUnitY().x), (int) (100 * PhysicsHandler.raft.getPos().y + 100 * PhysicsHandler.raft.getUnitY().y));
			int x = (int) (100 * (PhysicsHandler.raft.getPos().x + PhysicsHandler.raft.getCOMPos().x * unitx.x + PhysicsHandler.raft.getCOMPos().y * unity.x));
			int y = (int) (100 * (PhysicsHandler.raft.getPos().y + PhysicsHandler.raft.getCOMPos().x * unitx.y + PhysicsHandler.raft.getCOMPos().y * unity.y));
			g2d.drawOval(x - 10, y - 10, 20, 20);
		}
		
		//DEBUG
		//for (DebugVector dv : DebugHandler.debugVectors) {
		//	g2d.setColor(dv.color);
		//	g2d.drawLine((int) (100 * dv.pos.x), (int) (100 * dv.pos.y), (int) (100 * dv.pos.x + 100 * dv.vector.x), (int) (100 * dv.pos.y + 100 * dv.vector.y));
			
		//}
		//
		g2d.setColor(Color.RED);
		g2d.drawOval((int) (100*PhysicsHandler.cameraPosition.x - 10), (int) (100*PhysicsHandler.cameraPosition.y - 10), 20, 20);
		
		if (ControlHandler.escape_menu) {
			g2d.setColor(new Color(0,0,0,127));
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
