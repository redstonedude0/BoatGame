package redstonedude.programs.projectboaty.shared.raft;

import redstonedude.programs.projectboaty.client.control.ControlHandler;
import redstonedude.programs.projectboaty.shared.physics.VectorDouble;

public class TileAnchorSmall extends Tile {

	private static final long serialVersionUID = 1L;
	public int deploymentPercentage = 0;

	public TileAnchorSmall() {
		super();
	}

	@Override
	public VectorDouble getAbsoluteFrictionVector(Raft parent) {
		VectorDouble motion = getAbsoluteMotion(parent);
		// this is total motion, now multiply by friction coefficients (negative since
		// friction acts against motion)
		// see if tile is over land or water (use COM of tile)
		// System.out.println(getTerrain(parent).frictionCoefficient);
		VectorDouble friction = motion.multiply(-getTerrain(parent).frictionCoefficient);
		if (ControlHandler.control_brake) {
			friction = friction.add(motion.multiply(-50));
			//friction = friction.add(motion.setMagnitude(parent.getMass()*-1));//Ma
		}
		return friction;
	}
	
//	@Override
//	public VectorDouble getRelativeFrictionVector(Raft parent) {
//		// need to calculate velocity vector
//		VectorDouble absFriction = getAbsoluteFrictionVector(parent);
//		// convert to relative friction.
//		VectorDouble friction = new VectorDouble();
//		double a = parent.getUnitX().x;
//		double b = parent.getUnitY().x;
//		double c = parent.getUnitX().y;
//		double d = parent.getUnitY().y;
//		double determinant = a * d - b * c;
//		VectorDouble unitX = new VectorDouble(d, -c).divide(determinant);
//		VectorDouble unitY = new VectorDouble(-b, a).divide(determinant);
//		friction.x = absFriction.x * unitX.x + absFriction.y * unitY.x;
//		friction.y = absFriction.x * unitX.y + absFriction.y * unitY.y;
//
//		return friction;
//	}

}
