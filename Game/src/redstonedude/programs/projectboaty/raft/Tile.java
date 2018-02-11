package redstonedude.programs.projectboaty.raft;

import redstonedude.programs.projectboaty.physics.PhysicsHandler;
import redstonedude.programs.projectboaty.physics.VectorDouble;

public class Tile {
	
	private VectorDouble pos = new VectorDouble();
	public double mass = 10;
	
	public double getAbsoluteX(Raft parent) {
		return parent.getPos().x + pos.x*parent.getUnitX().x+pos.y*parent.getUnitY().x;
	}
	
	public double getAbsoluteY(Raft parent) {
		return parent.getPos().y + pos.x*parent.getUnitX().y+pos.y*parent.getUnitY().y;
	}
	
	public void setPos(VectorDouble pos) {
		this.pos = pos;
	}
	
	public VectorDouble getPos() {
		return new VectorDouble(pos);
	}
	
	public VectorDouble getAbsoluteFrictionVector(Raft parent) {
		//need to calculate velocity vector
		VectorDouble linearVelocity = parent.getVelocity();
		//need to calculate tangent to circular motion and it will have magnitude r*omega
		VectorDouble displacement = getPos();
		displacement = displacement.add(new VectorDouble(0.5, 0.5));
		displacement = displacement.subtract(parent.getCOMPos());
		//need to get vector at 90 clockwise rotation to it.
		VectorDouble rotationalVelocity = new VectorDouble(displacement);
		rotationalVelocity = rotationalVelocity.rotate(-Math.PI/2); //don't ask why it's negative. It just is.
		rotationalVelocity = rotationalVelocity.setMagnitude(parent.dtheta * Math.sqrt(displacement.getSquaredLength()));
		VectorDouble absRot = new VectorDouble();
		absRot.x = rotationalVelocity.x*PhysicsHandler.raft.getUnitX().x+rotationalVelocity.y*PhysicsHandler.raft.getUnitY().x;
		absRot.y = rotationalVelocity.x*PhysicsHandler.raft.getUnitX().y+rotationalVelocity.y*PhysicsHandler.raft.getUnitY().y;
		VectorDouble motion = new VectorDouble(linearVelocity);
		motion = motion.add(absRot);
		//motion.subtract(linearVelocity);
		//this is total motion, now multiply by friction coefficients (negative since friction acts against motion)
		motion = motion.multiply(-0.2); //now provide as force
		return motion;
	}
	
	public VectorDouble getRelativeFrictionVector(Raft parent) {
		//need to calculate velocity vector
		VectorDouble absFriction = getAbsoluteFrictionVector(parent);
		//convert to relative friction.
		VectorDouble friction = new VectorDouble();
		double a = PhysicsHandler.raft.getUnitX().x;
		double b = PhysicsHandler.raft.getUnitY().x;
		double c = PhysicsHandler.raft.getUnitX().y;
		double d = PhysicsHandler.raft.getUnitY().y;
		double determinant = a*d-b*c;
		VectorDouble unitX = new VectorDouble(d, -c);
		VectorDouble unitY = new VectorDouble(-b, a);
		unitX = unitX.divide(determinant);
		unitY = unitY.divide(determinant);
		friction.x = absFriction.x*unitX.x+absFriction.y*unitY.x;
		friction.y = absFriction.x*unitX.y+absFriction.y*unitY.y;
		return friction;
	}
	
}
