package redstonedude.programs.projectboaty.physics;

import java.util.Vector;

import redstonedude.programs.projectboaty.control.ControlHandler;
import redstonedude.programs.projectboaty.raft.Raft;
import redstonedude.programs.projectboaty.raft.Tile;
import redstonedude.programs.projectboaty.raft.TileThruster;
import redstonedude.programs.projectboaty.raft.TileThruster.ControlType;

public class PhysicsHandler {

	public static int c = 0;

	public static Raft raft;

	public static void physicsUpdate() {
		c++;
		if (raft == null) {
			createRaft();
		}

		// calculate non-rotational physics, as well as updating thruster control values
		double thrustX = 0;
		double thrustY = 0;
		double mass = 0;
		for (Tile tile : raft.tiles) {
			mass += tile.mass;
			if (tile instanceof TileThruster) {
				TileThruster thruster = (TileThruster) tile;
				if (thruster.controlType == ControlType.Left) {
					thruster.thrustStrength = ControlHandler.control_left ? thruster.maxThrustStrength : 0;
				} else if (thruster.controlType == ControlType.Right) {
					thruster.thrustStrength = ControlHandler.control_right ? thruster.maxThrustStrength : 0;
				}
				if (ControlHandler.control_reverse) {
					thruster.thrustStrength = -thruster.thrustStrength;
				}
				Vector<Double> force = thruster.getAbsoluteThrustVector(raft);
				thrustX += force.get(0);
				thrustY += force.get(1);
			}
		}
		// TODO implement a resistance to acceleration/motion
		// F=ma, a = F/m
		double ax = thrustX / mass;
		double ay = thrustY / mass;
		raft.dx += ax;
		raft.dy += ay;
		raft.x += raft.dx;
		raft.y += raft.dy;

		// calculate rotational physics
		// a = Fr/mrr
		// centre of mass must first be located.
		double massmomentsX = 0;
		double massmomentsY = 0;
		for (Tile tile : raft.tiles) {
			massmomentsX += tile.mass * (tile.x + 0.5);
			massmomentsY += tile.mass * (tile.y + 0.5);
		}
		double centreofmassX = massmomentsX / mass;
		double centreofmassY = massmomentsY / mass;
		raft.comx = centreofmassX;
		raft.comy = centreofmassY;

		// calculate moments of inertia about this point
		double forcemoments = 0;
		double squareradiusofgyration = 0;
		for (Tile tile : raft.tiles) {
			double dx = (tile.x + 0.5) - centreofmassX;
			double dy = (tile.y + 0.5) - centreofmassY;
			double squaredistance = dx * dx + dy * dy;
			squareradiusofgyration += squaredistance;// squaredistance;
			if (tile instanceof TileThruster) {
				TileThruster thruster = (TileThruster) tile;
				Vector<Double> force = thruster.getRelativeThrustVector();
				// x component of force * perpendicular distance
				// x component * relative dy of origin
				forcemoments += force.get(0) * -dy; // subtract because dy and dx are making it anticlockwise
				forcemoments += force.get(1) * -dx;
				// System.out.println("Moment added: " + (force.get(0)*-dy + force.get(1)*-dx));
			}
		}
		double masssquaremoments = squareradiusofgyration * mass;
		double atheta = forcemoments / masssquaremoments;
		raft.dtheta += atheta;
		// rotation needs to be about the COM so translaton needs to occur, needs to
		// rotate by dtheta about COM
		// calculate how much COM moves and move to compensate
		// [cos -sin] [comx] = [coscomx-sincomy]
		// [sin cos] [comy] = [sincomx+coscomy]
		Vector<Double> unitx = raft.getUnitX();
		Vector<Double> unity = raft.getUnitY();
		double comx_initial = centreofmassX * unitx.get(0) + centreofmassY * unity.get(0);
		double comy_initial = centreofmassX * unitx.get(1) + centreofmassY * unity.get(1);
		// double comx_initial = raft.cos*centreofmassX-raft.sin*centreofmassY;
		// double comy_initial = raft.sin*centreofmassX+raft.cos*centreofmassY;
		raft.theta += raft.dtheta;
		raft.sin = Math.sin(raft.theta);
		raft.cos = Math.cos(raft.theta);
		// double comx_after = raft.cos*centreofmassX-raft.sin*centreofmassY;
		// double comy_after = raft.sin*centreofmassX+raft.cos*centreofmassY;
		unitx = raft.getUnitX();
		unity = raft.getUnitY();
		double comx_after = centreofmassX * unitx.get(0) + centreofmassY * unity.get(0);
		double comy_after = centreofmassX * unitx.get(1) + centreofmassY * unity.get(1);
		double dcomx = comx_after - comx_initial;
		double dcomy = comy_after - comy_initial;
		//System.out.println(comx_after + ":" + comx_initial);
		if (ControlHandler.debug_lock) {
			raft.x = 5;
			raft.y = 5;
		}
		// System.out.println(dcomx + ":" + dcomy);
		raft.x -= dcomx;
		raft.y -= dcomy;
	}

	public static void createRaft() {
		createRaft(1);
	}

	public static void createRaft(int id) {
		raft = new Raft();
		raft.x = 4;
		raft.y = 3;
		raft.theta = 0;
		switch (id) {
		case 1:
			Tile tile = new Tile();
			tile.x = 1;
			tile.y = 0;
			raft.tiles.add(tile);
			tile = new Tile();
			tile.x = 0;
			tile.y = 1;
			raft.tiles.add(tile);
			tile = new Tile();
			tile.x = 1;
			tile.y = 1;
			raft.tiles.add(tile);
			tile = new Tile();
			tile.x = 2;
			tile.y = 1;
			raft.tiles.add(tile);
			TileThruster thruster = new TileThruster();
			thruster.x = 0;
			thruster.y = 0;
			thruster.controlType = ControlType.Left;
			raft.tiles.add(thruster);
			thruster = new TileThruster();
			thruster.x = 2;
			thruster.y = 0;
			thruster.controlType = ControlType.Right;
			raft.tiles.add(thruster);
			break;
		case 2:
			tile = new Tile();
			tile.x = 1;
			tile.y = 0;
			raft.tiles.add(tile);
			tile = new Tile();
			tile.x = 2;
			tile.y = 0;
			raft.tiles.add(tile);
			tile = new Tile();
			tile.x = 0;
			tile.y = 1;
			raft.tiles.add(tile);
			tile = new Tile();
			tile.x = 1;
			tile.y = 1;
			raft.tiles.add(tile);
			thruster = new TileThruster();
			thruster.x = 0;
			thruster.y = 0;
			thruster.controlType = ControlType.Left;
			raft.tiles.add(thruster);
			thruster = new TileThruster();
			thruster.x = 2;
			thruster.y = 1;
			thruster.controlType = ControlType.Right;
			thruster.thrustAngle = Math.PI;
			raft.tiles.add(thruster);
			break;
		case 3:
			tile = new Tile();
			tile.x = 0;
			tile.y = 1;
			raft.tiles.add(tile);
			tile = new Tile();
			tile.x = 1;
			tile.y = 1;
			raft.tiles.add(tile);
			tile = new Tile();
			tile.x = 2;
			tile.y = 1;
			raft.tiles.add(tile);
			tile = new Tile();
			tile.x = 3;
			tile.y = 1;
			raft.tiles.add(tile);
			tile = new Tile();
			tile.x = 4;
			tile.y = 1;
			raft.tiles.add(tile);
			tile = new Tile();
			tile.x = 5;
			tile.y = 1;
			raft.tiles.add(tile);
			thruster = new TileThruster();
			thruster.x = 0;
			thruster.y = 0;
			thruster.controlType = ControlType.Left;
			raft.tiles.add(thruster);
			thruster = new TileThruster();
			thruster.x = 4;
			thruster.y = 0;
			thruster.controlType = ControlType.Right;
			raft.tiles.add(thruster);
			thruster = new TileThruster();
			thruster.x = 5;
			thruster.y = 0;
			thruster.controlType = ControlType.Right;
			raft.tiles.add(thruster);
			break;

		}
	}
	
	public static void reset() {
		c = 0;
		raft = null;
	}

}
