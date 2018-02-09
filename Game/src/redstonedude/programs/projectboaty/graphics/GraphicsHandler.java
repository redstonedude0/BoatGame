package redstonedude.programs.projectboaty.graphics;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.image.BufferedImage;
import java.util.Vector;

import javax.swing.JFrame;

import redstonedude.programs.projectboaty.physics.PhysicsHandler;
import redstonedude.programs.projectboaty.raft.Tile;
import redstonedude.programs.projectboaty.raft.TileHandler;
import redstonedude.programs.projectboaty.raft.TileThruster;

public class GraphicsHandler {

	public static JFrame frame;
	public static Graphics2D g2d;
	public static BufferedImage backbuffer;
	
	public static void graphicsUpdate() {
		g2d.setColor(Color.BLACK);
		g2d.fillRect(0,0,1920,1080);
		g2d.setColor(Color.WHITE);
		/*pendulum physics [append a /] *
		
		g2d.drawString("phys tick "+PendulumPhysicsHandler.c,50,50);
		g2d.drawString("angle: "+PendulumPhysicsHandler.theta,50,70);
		int x = 100;
		for (double d: PendulumPhysicsHandler.measuredPeriods) {
			g2d.drawString("Period: " + d,50,x);
			x+= 20;
			if (x>300) {
				PendulumPhysicsHandler.measuredPeriods.remove(0);
				break;
			}
		}
		g2d.setColor(Color.RED);
		g2d.drawLine(PendulumPhysicsHandler.mount_x, PendulumPhysicsHandler.mount_y, PendulumPhysicsHandler.x, PendulumPhysicsHandler.y);
		g2d.fillOval(PendulumPhysicsHandler.x-10,PendulumPhysicsHandler.y-10,20,20);
		
		/**/
		
		/*boat physics [append a /] */
		//tesselate with water
		int index = PhysicsHandler.c%4;
		for (int i = 0; i < 15; i++) {
			for (int j = 0; j < 10; j++) {
				int x = 100*i;
				int y = 100*j;
				g2d.drawImage(TextureHandler.getTexture("Water_"+index), x,y,x+100,y+100, 0, 0, 32, 32, frame);
			}
		}
		
		g2d.drawString("phys tick "+PhysicsHandler.c,50,50);
		if (PhysicsHandler.raft != null) {
			Vector<Double> unitx = PhysicsHandler.raft.getUnitX();
			Vector<Double> unity = PhysicsHandler.raft.getUnitY();
			for (Tile tile: PhysicsHandler.raft.tiles) {
				double x = tile.getAbsoluteX(PhysicsHandler.raft);
				double y = tile.getAbsoluteY(PhysicsHandler.raft);
				//g2d.setColor(Color.BLUE);
				if (tile instanceof TileThruster) {
					g2d.setColor(Color.GREEN);
					//also draw thrust vector, in the direction of force
					drawSlantedLineOffset(x, y, 0.5,0.5,unitx, unity, ((TileThruster) tile).getDrawnThrustVector(PhysicsHandler.raft));
					//g2d.setColor(Color.RED);
				}
				//drawSlantedRect(x, y, unitx, unity);
				//using graphics instead of colors
				AffineTransform rotator = new AffineTransform();

				rotator.translate(100*x, 100*y);
				rotator.rotate(PhysicsHandler.raft.theta);
				g2d.transform(rotator);
				g2d.drawImage(TextureHandler.getTexture(TileHandler.getTextureName(tile)), 0,-100,100,0, 0, 0, 32, 32, frame);
				try {
					g2d.transform(rotator.createInverse());
				} catch (NoninvertibleTransformException e) {
					e.printStackTrace();
				}
			}
			g2d.setColor(Color.WHITE);
			g2d.drawLine((int) (100*PhysicsHandler.raft.x),(int) (100*PhysicsHandler.raft.y),(int) (100*PhysicsHandler.raft.x+100*PhysicsHandler.raft.getUnitX().get(0)),(int) (100*PhysicsHandler.raft.y+100*PhysicsHandler.raft.getUnitX().get(1)));
			g2d.drawLine((int) (100*PhysicsHandler.raft.x),(int) (100*PhysicsHandler.raft.y),(int) (100*PhysicsHandler.raft.x+100*PhysicsHandler.raft.getUnitY().get(0)),(int) (100*PhysicsHandler.raft.y+100*PhysicsHandler.raft.getUnitY().get(1)));
			int x = (int) (100*(PhysicsHandler.raft.x+PhysicsHandler.raft.comx*unitx.get(0)+PhysicsHandler.raft.comy*unity.get(0)));
			int y = (int) (100*(PhysicsHandler.raft.y+PhysicsHandler.raft.comx*unitx.get(1)+PhysicsHandler.raft.comy*unity.get(1)));
			g2d.drawOval(x-10,y-10,20,20);
			//g2d.drawOval(arg0, arg1, arg2, arg3)
		}
		
		
		/**/
		frame.getGraphics().drawImage(backbuffer, 0, 0, 1920, 1080, frame);
	}
	
	public static void drawSlantedRect(double absx, double absy, Vector<Double> unitx, Vector<Double> unity) {
		//g2d.fillRect(x, y, , (int) (100*PhysicsHandler.raft.getUnitY()));
		Polygon p = new Polygon();
		p.addPoint((int) (100*absx), (int) (100*absy));
		p.addPoint((int) (100*absx+100*unitx.get(0)), (int) (100*absy+100*unitx.get(1)));
		p.addPoint((int) (100*absx+100*unitx.get(0)+100*unity.get(0)), (int) (100*absy+100*unitx.get(1)+100*unity.get(1)));
		p.addPoint((int) (100*absx+100*unity.get(0)), (int) (100*absy+100*unity.get(1)));
		g2d.fillPolygon(p);
	}
	
	public static void drawSlantedLineOffset(double absx, double absy, double x, double y, Vector<Double> unitx, Vector<Double> unity, Vector<Double> absoluteline) {
		g2d.drawLine((int)(100*(absx+x*unitx.get(0)+y*unity.get(0))), (int)(100*(absy+x*unitx.get(1)+y*unity.get(1))), (int)(100*(absx+x*unitx.get(0)+y*unity.get(0)+absoluteline.get(0))), (int)(100*(absy+x*unitx.get(1)+y*unity.get(1)+absoluteline.get(1))));
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
