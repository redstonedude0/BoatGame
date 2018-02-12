package redstonedude.programs.projectboaty.physics;

import redstonedude.programs.projectboaty.control.ControlHandler;
import redstonedude.programs.projectboaty.graphics.DebugHandler;
import redstonedude.programs.projectboaty.raft.Raft;
import redstonedude.programs.projectboaty.raft.Tile;
import redstonedude.programs.projectboaty.raft.TileThruster;

public class PhysicsHandler {

	public static int c = 0;

	public static Raft raft;
	public static VectorDouble cameraPosition = new VectorDouble(0,0);

	public static void physicsUpdate() {
		DebugHandler.clear();
		
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
				thruster.setThrustStrength(raft);
				thrust = thrust.add(thruster.getAbsoluteThrustVector(raft));
			}
			//tiles will apply drag to the object
			thrust = thrust.add(tile.getAbsoluteFrictionVector(raft));
			
			//DebugVector dv = new DebugVector();
			//dv.pos = raft.getPos();
			//dv.vector = tile.getAbsoluteFrictionVector(raft).multiply(100);
			//dv.color = Color.RED;
			//DebugHandler.debugVectors.add(dv);
		}
		// F=ma, a = F/m
		VectorDouble acceleration = thrust.divide(mass);
		raft.setVelocity(raft.getVelocity().add(acceleration));
		raft.setPos(raft.getPos().add(raft.getVelocity()));

		// calculate rotational physics
		// a = Fr/mrr
		// centre of mass must first be located.
		VectorDouble massMoments = new VectorDouble();
		for (Tile tile : raft.tiles) {
			VectorDouble moment = tile.getPos().add(new VectorDouble(0.5, 0.5)).multiply(tile.mass);
			massMoments = massMoments.add(moment);
		}
		VectorDouble centreOfMass = new VectorDouble(massMoments);
		centreOfMass = centreOfMass.divide(mass);
		raft.setCOMPos(centreOfMass);

		// calculate moments of inertia about this point
		double forcemoments = 0;
		double squareradiusofgyration = 0;
		for (Tile tile : raft.tiles) {
			VectorDouble dpos = tile.getPos().add(new VectorDouble(0.5, 0.5)).subtract(centreOfMass);//this is relative dpos, calculate absolute
			
			double squaredistance = dpos.getSquaredLength();
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
			drag = drag.multiply(5); //multiply drag by 5 so it doesn't feel like ice.
			//arbitrary number chosen since calulating hydrodynamics is boring.
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
		if (ControlHandler.debug_menu) {
			raft.setPos(new VectorDouble(5,5));
		}
		// System.out.println(dcomx + ":" + dcomy);
		raft.setPos(raft.getPos().subtract(new VectorDouble(dcomx, dcomy)));
		
		//move camera accordingly
		VectorDouble posDiff = raft.getCOMPos().getAbsolute(unitx, unity).add(raft.getPos()).subtract(cameraPosition);
		posDiff = posDiff.divide(10);//do it slower
		cameraPosition = cameraPosition.add(posDiff);
		
	}

	public static void createRaft() {
		createRaft(1);
	}

	public static void createRaft(int id) {
		raft = new Raft();
		raft.setPos(new VectorDouble(4, 3));
		cameraPosition = new VectorDouble(4,3);
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
			raft.tiles.add(thruster);
			thruster = new TileThruster();
			thruster.setPos(new VectorDouble(2, 0));
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
			raft.tiles.add(thruster);
			thruster = new TileThruster();
			thruster.setPos(new VectorDouble(2, 1));
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
			raft.tiles.add(thruster);
			thruster = new TileThruster();
			thruster.setPos(new VectorDouble(4, 0));
			raft.tiles.add(thruster);
			thruster = new TileThruster();
			thruster.setPos(new VectorDouble(5, 0));
			raft.tiles.add(thruster);
			break;
		case 4:
			tile = new Tile();
			tile.setPos(new VectorDouble(1, 0));
			raft.tiles.add(tile);
			tile = new Tile();
			tile.setPos(new VectorDouble(1, 1));
			raft.tiles.add(tile);
			thruster = new TileThruster();
			thruster.setPos(new VectorDouble(0, 0));
			raft.tiles.add(thruster);
			thruster = new TileThruster();
			thruster.setPos(new VectorDouble(0, 1));
			thruster.thrustAngle = -Math.PI/2;
			raft.tiles.add(thruster);
			thruster = new TileThruster();
			thruster.setPos(new VectorDouble(2, 0));
			raft.tiles.add(thruster);
			thruster = new TileThruster();
			thruster.setPos(new VectorDouble(2, 1));
			thruster.thrustAngle = Math.PI/2;
			raft.tiles.add(thruster);
			break;
		}
	}
	
	public static void reset() {
		c = 0;
		raft = null;
		cameraPosition = new VectorDouble(0,0);
	}

}
