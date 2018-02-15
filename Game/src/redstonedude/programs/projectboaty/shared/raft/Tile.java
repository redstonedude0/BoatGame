package redstonedude.programs.projectboaty.shared.raft;

import java.io.Serializable;

import redstonedude.programs.projectboaty.client.net.ClientPacketHandler;
import redstonedude.programs.projectboaty.server.physics.VectorDouble;
import redstonedude.programs.projectboaty.shared.net.serverbound.PacketRequestTileState;
import redstonedude.programs.projectboaty.shared.world.WorldHandler;
import redstonedude.programs.projectboaty.shared.world.WorldHandler.TerrainType;

public class Tile implements Serializable {

	private static final long serialVersionUID = 2L;

	private VectorDouble pos = new VectorDouble();
	public double mass = 10;
	public float hp = 100;

	public void damage(float dmg) {
		hp -= dmg;
		//if damage is non-0 sent packet
		if (dmg != 0) {
			PacketRequestTileState prts = new PacketRequestTileState(this);
			//prts.tile = this;
			//prts.tile = new Tile();
			//prts.tile.pos = new VectorDouble(pos);
			//System.out.println("PRTSC DATA: (" + prts.uniqueTestingID + ")");
			//System.out.println("  " + prts.tile.hp);
			//System.out.println("  " + prts.tile.mass);
			//System.out.println("  " + prts.tile.getPos().x + ":" + prts.tile.getPos().y);
			//prts.tile.hp = Float.parseFloat(""+hp);
			//System.out.println("1.  " + prts.tile.hp);
			//hp = hp+100;
			//System.out.println("2.  " + prts.tile.hp);
			//System.out.println("HP now set to " + prts.tile.hp);
			ClientPacketHandler.sendPacket(prts);
		}
	}

	public double getAbsoluteX(Raft parent) {
		return parent.getPos().x + pos.x * parent.getUnitX().x + pos.y * parent.getUnitY().x;
	}

	public double getAbsoluteY(Raft parent) {
		return parent.getPos().y + pos.x * parent.getUnitX().y + pos.y * parent.getUnitY().y;
	}

	public void setPos(VectorDouble pos) {
		this.pos = pos;
	}

	public VectorDouble getPos() {
		return new VectorDouble(pos);
	}

	public VectorDouble getAbsoluteMotion(Raft parent) {
		// need to calculate velocity vector
		VectorDouble linearVelocity = parent.getVelocity();
		// need to calculate tangent to circular motion and it will have magnitude
		// r*omega
		VectorDouble displacement = getPos().add(new VectorDouble(0.5, 0.5)).subtract(parent.getCOMPos());
		// need to get vector at 90 clockwise rotation to it.
		// don't ask why it's negative. It just is.
		// long story short its because the y axis on the boat is flipped or something.
		// Not sure. It works.
		// since the basis is flipped it will be flipped a -90 before is the same as a
		// +90 after.
		VectorDouble rotationalVelocity = new VectorDouble(displacement).rotate(-Math.PI / 2).setMagnitude(parent.dtheta * Math.sqrt(displacement.getSquaredLength()));
		VectorDouble absRot = new VectorDouble();
		absRot.x = rotationalVelocity.x * parent.getUnitX().x + rotationalVelocity.y * parent.getUnitY().x;
		absRot.y = rotationalVelocity.x * parent.getUnitX().y + rotationalVelocity.y * parent.getUnitY().y;
		VectorDouble vr = new VectorDouble(linearVelocity).add(absRot);
		
		return vr;
	}

	public VectorDouble getAbsoluteFrictionVector(Raft parent) {
		VectorDouble motion = getAbsoluteMotion(parent);
		// this is total motion, now multiply by friction coefficients (negative since
		// friction acts against motion)
		// see if tile is over land or water (use COM of tile)
		//System.out.println(getTerrain(parent).frictionCoefficient);
		motion = motion.multiply(-getTerrain(parent).frictionCoefficient);
		return motion;
	}

	public float getDamage(Raft parent) {
		VectorDouble motion = getAbsoluteMotion(parent);
		float dmg = (float) motion.getSquaredLength();
		dmg = (float) Math.sqrt(dmg);
		dmg *= getTerrain(parent).damageCoefficient;
		return dmg;
	}

	public TerrainType getTerrain(Raft parent) {
		VectorDouble pos = getPos().add(new VectorDouble(0.5, 0.5)).getAbsolute(parent.getUnitX(), parent.getUnitY()).add(parent.getPos());
		return WorldHandler.getTerrainType(Math.floor(pos.x), Math.floor(pos.y));
	}

	public VectorDouble getRelativeFrictionVector(Raft parent) {
		// need to calculate velocity vector
		VectorDouble absFriction = getAbsoluteFrictionVector(parent);
		// convert to relative friction.
		VectorDouble friction = new VectorDouble();
		double a = parent.getUnitX().x;
		double b = parent.getUnitY().x;
		double c = parent.getUnitX().y;
		double d = parent.getUnitY().y;
		double determinant = a * d - b * c;
		VectorDouble unitX = new VectorDouble(d, -c).divide(determinant);
		VectorDouble unitY = new VectorDouble(-b, a).divide(determinant);
		friction.x = absFriction.x * unitX.x + absFriction.y * unitY.x;
		friction.y = absFriction.x * unitX.y + absFriction.y * unitY.y;
		
		return friction;
	}

}
