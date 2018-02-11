package redstonedude.programs.projectboaty.physics;

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
		VectorDouble thrust = new VectorDouble();
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
				thrust.add(thruster.getAbsoluteThrustVector(raft));
			}
			//tiles will apply drag to the object
			thrust.add(tile.getAbsoluteFrictionVector(raft));
		}
		// TODO implement a resistance to acceleration/motion
		// F=ma, a = F/m
		VectorDouble acceleration = thrust;
		acceleration.divide(mass);
		raft.setVelocity(raft.getVelocity().add(acceleration));
		raft.setPos(raft.getPos().add(raft.getVelocity()));

		// calculate rotational physics
		// a = Fr/mrr
		// centre of mass must first be located.
		VectorDouble massMoments = new VectorDouble();
		for (Tile tile : raft.tiles) {
			VectorDouble moment = new VectorDouble(tile.getPos());
			moment.add(new VectorDouble(0.5, 0.5));
			moment.multiply(tile.mass);
			massMoments.add(moment);
		}
		VectorDouble centreOfMass = new VectorDouble(massMoments);
		centreOfMass.divide(mass);
		raft.setCOMPos(centreOfMass);

		// calculate moments of inertia about this point
		double forcemoments = 0;
		double squareradiusofgyration = 0;
		for (Tile tile : raft.tiles) {
			VectorDouble dpos = new VectorDouble(tile.getPos());
			dpos.add(new VectorDouble(0.5, 0.5));
			dpos.subtract(centreOfMass);//this is relative dpos, calculate absolute
			VectorDouble absDpos = new VectorDouble();
			absDpos.x = dpos.x*PhysicsHandler.raft.getUnitX().x+dpos.y*PhysicsHandler.raft.getUnitY().x;
			absDpos.y = dpos.x*PhysicsHandler.raft.getUnitX().y+dpos.y*PhysicsHandler.raft.getUnitY().y;
			
			double squaredistance = absDpos.getSquaredLength();
			squareradiusofgyration += squaredistance;// squaredistance;
			if (tile instanceof TileThruster) {
				TileThruster thruster = (TileThruster) tile;
				VectorDouble force = new VectorDouble(thruster.getRelativeThrustVector());
				// x component of force * perpendicular distance
				// x component * relative dy of origin
				forcemoments += force.x*-dpos.y; // subtract because dy and dx are making it anticlockwise
				forcemoments += force.y*-dpos.x; //consider taking a cross product or something
				// System.out.println("Moment added: " + (force.get(0)*-dy + force.get(1)*-dx));
			}
			//do drag also
			VectorDouble drag = tile.getRelativeFrictionVector(raft);
			drag.multiply(5);
			forcemoments += drag.x*-dpos.y;
			forcemoments += drag.y*-dpos.x;
		}
		double masssquaremoments = squareradiusofgyration * mass;
		double atheta = forcemoments / masssquaremoments;
		raft.dtheta += atheta;
		// rotation needs to be about the COM so translaton needs to occur, needs to
		// rotate by dtheta about COM
		// calculate how much COM moves and move to compensate
		// [cos -sin] [comx] = [coscomx-sincomy]
		// [sin cos] [comy] = [sincomx+coscomy]
		VectorDouble unitx = raft.getUnitX();
		VectorDouble unity = raft.getUnitY();
		double comx_initial = centreOfMass.x * unitx.x + centreOfMass.y * unity.x;
		double comy_initial = centreOfMass.x * unitx.y + centreOfMass.y * unity.y;
		//VectorDouble com_initial = 
		// double comx_initial = raft.cos*centreofmassX-raft.sin*centreofmassY;
		// double comy_initial = raft.sin*centreofmassX+raft.cos*centreofmassY;
		raft.theta += raft.dtheta;
		raft.sin = Math.sin(raft.theta);
		raft.cos = Math.cos(raft.theta);
		// double comx_after = raft.cos*centreofmassX-raft.sin*centreofmassY;
		// double comy_after = raft.sin*centreofmassX+raft.cos*centreofmassY;
		unitx = raft.getUnitX();
		unity = raft.getUnitY();
		double comx_after = centreOfMass.x * unitx.x + centreOfMass.y * unity.x;
		double comy_after = centreOfMass.x * unitx.y + centreOfMass.y * unity.y;
		double dcomx = comx_after - comx_initial;
		double dcomy = comy_after - comy_initial;
		//System.out.println(comx_after + ":" + comx_initial);
		if (ControlHandler.debug_lock) {
			raft.setPos(new VectorDouble(5,5));
		}
		// System.out.println(dcomx + ":" + dcomy);
		raft.setPos(raft.getPos().subtract(new VectorDouble(dcomx, dcomy)));
	}

	public static void createRaft() {
		createRaft(1);
	}

	public static void createRaft(int id) {
		raft = new Raft();
		raft.setPos(new VectorDouble(4, 3));
		raft.theta = 0;
		switch (id) {
		case 1:
			Tile tile = new Tile();
			tile.setPos(new VectorDouble(1, 0));
			raft.tiles.add(tile);
			tile = new Tile();
			tile.setPos(new VectorDouble(0, 1));
			raft.tiles.add(tile);
			tile = new Tile();
			tile.setPos(new VectorDouble(1, 1));
			raft.tiles.add(tile);
			tile = new Tile();
			tile.setPos(new VectorDouble(2, 1));
			raft.tiles.add(tile);
			TileThruster thruster = new TileThruster();
			thruster.setPos(new VectorDouble(0, 0));
			thruster.controlType = ControlType.Left;
			raft.tiles.add(thruster);
			thruster = new TileThruster();
			thruster.setPos(new VectorDouble(2, 0));
			thruster.controlType = ControlType.Right;
			raft.tiles.add(thruster);
			break;
		case 2:
			tile = new Tile();
			tile.setPos(new VectorDouble(1, 0));
			raft.tiles.add(tile);
			tile = new Tile();
			tile.setPos(new VectorDouble(2, 0));
			raft.tiles.add(tile);
			tile = new Tile();
			tile.setPos(new VectorDouble(0, 1));
			raft.tiles.add(tile);
			tile = new Tile();
			tile.setPos(new VectorDouble(1, 1));
			raft.tiles.add(tile);
			thruster = new TileThruster();
			thruster.setPos(new VectorDouble(0, 0));
			thruster.controlType = ControlType.Left;
			raft.tiles.add(thruster);
			thruster = new TileThruster();
			thruster.setPos(new VectorDouble(2, 1));
			thruster.controlType = ControlType.Right;
			thruster.thrustAngle = Math.PI;
			raft.tiles.add(thruster);
			break;
		case 3:
			tile = new Tile();
			tile.setPos(new VectorDouble(0, 1));
			raft.tiles.add(tile);
			tile = new Tile();
			tile.setPos(new VectorDouble(1, 1));
			raft.tiles.add(tile);
			tile = new Tile();
			tile.setPos(new VectorDouble(2, 1));
			raft.tiles.add(tile);
			tile = new Tile();
			tile.setPos(new VectorDouble(3, 1));
			raft.tiles.add(tile);
			tile = new Tile();
			tile.setPos(new VectorDouble(4, 1));
			raft.tiles.add(tile);
			tile = new Tile();
			tile.setPos(new VectorDouble(5, 1));
			raft.tiles.add(tile);
			thruster = new TileThruster();
			thruster.setPos(new VectorDouble(0, 0));
			thruster.controlType = ControlType.Left;
			raft.tiles.add(thruster);
			thruster = new TileThruster();
			thruster.setPos(new VectorDouble(4, 0));
			thruster.controlType = ControlType.Right;
			raft.tiles.add(thruster);
			thruster = new TileThruster();
			thruster.setPos(new VectorDouble(5, 0));
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
