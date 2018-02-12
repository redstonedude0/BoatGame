package redstonedude.programs.projectboaty.shared.raft;

import java.io.Serializable;

import redstonedude.programs.projectboaty.server.physics.PhysicsHandler;
import redstonedude.programs.projectboaty.server.physics.VectorDouble;

public class Tile implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
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
		VectorDouble displacement = getPos().add(new VectorDouble(0.5, 0.5)).subtract(parent.getCOMPos());
		//need to get vector at 90 clockwise rotation to it.
		//don't ask why it's negative. It just is.
		//long story short its because the y axis on the boat is flipped or something. Not sure. It works.
		//since the basis is flipped it will be flipped a -90 before is the same as a +90 after.
		VectorDouble rotationalVelocity = new VectorDouble(displacement).rotate(-Math.PI/2).setMagnitude(parent.dtheta * Math.sqrt(displacement.getSquaredLength()));
		VectorDouble absRot = new VectorDouble();
		absRot.x = rotationalVelocity.x*PhysicsHandler.raft.getUnitX().x+rotationalVelocity.y*PhysicsHandler.raft.getUnitY().x;
		absRot.y = rotationalVelocity.x*PhysicsHandler.raft.getUnitX().y+rotationalVelocity.y*PhysicsHandler.raft.getUnitY().y;
		VectorDouble motion = new VectorDouble(linearVelocity).add(absRot).multiply(-0.2);
		//this is total motion, now multiply by friction coefficients (negative since friction acts against motion)
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
		VectorDouble unitX = new VectorDouble(d, -c).divide(determinant);
		VectorDouble unitY = new VectorDouble(-b, a).divide(determinant);
		friction.x = absFriction.x*unitX.x+absFriction.y*unitY.x;
		friction.y = absFriction.x*unitX.y+absFriction.y*unitY.y;
		return friction;
	}
	
}
