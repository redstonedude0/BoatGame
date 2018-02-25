package redstonedude.programs.projectboaty.shared.raft;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;

import redstonedude.programs.projectboaty.client.net.ClientPacketHandler;
import redstonedude.programs.projectboaty.shared.entity.EntityResource;
import redstonedude.programs.projectboaty.shared.entity.EntityResource.ResourceType;
import redstonedude.programs.projectboaty.shared.net.UserData;
import redstonedude.programs.projectboaty.shared.net.serverbound.PacketRequestTileState;
import redstonedude.programs.projectboaty.shared.physics.Location;
import redstonedude.programs.projectboaty.shared.physics.VectorDouble;
import redstonedude.programs.projectboaty.shared.task.Task;
import redstonedude.programs.projectboaty.shared.task.TaskRepair;
import redstonedude.programs.projectboaty.shared.world.WorldHandler;
import redstonedude.programs.projectboaty.shared.world.WorldHandler.TerrainType;

public class Tile implements Serializable {

	private static final long serialVersionUID = 2L;

	private VectorDouble pos = new VectorDouble();
	public double mass = 10;
	public double hp = 100;
	public ResourceStorage storage = new ResourceStorage();
	public static enum TileType {
		WoodFloor(10,100,Arrays.asList(new EntityResource(ResourceType.Wood,1),new EntityResource(ResourceType.Wood,1)),"Wood",Tile.class),
		Thruster(50,500,Arrays.asList(new EntityResource(ResourceType.Scrap,1)),"Thruster",TileThruster.class),
		AnchorSmall(200,500,Arrays.asList(new EntityResource(ResourceType.Bricks,1)),"AnchorSmall",TileAnchorSmall.class);
		
		public Class<? extends Tile> clazz;
		public final double mass;
		public final double maxHP;
		public final Collection<EntityResource> requiredResources;
		public final String textureName;
		TileType(double mass, double maxHP, Collection<EntityResource> requiredResources, String textureName, Class<? extends Tile> clazz) {
			this.mass = mass;
			this.maxHP = maxHP;
			this.requiredResources = requiredResources;
			this.textureName = textureName;
			this.clazz = clazz;
		}
	}
	public TileType tileType;
	
	public Tile() {
		for (TileType tt: TileType.values()) {
			if (tt.clazz.equals(this.getClass())) {
				tileType = tt;
				hp = tt.maxHP;
				mass = tt.mass;
				return;
			}
		}
		System.out.println("Tile initiated with null tile type. This will not go well...");
	}

	public void damage(double dmg) {
		hp -= dmg;
		// if damage is non-0 sent packet
		if (dmg != 0) {
			PacketRequestTileState prts = new PacketRequestTileState(this);
			ClientPacketHandler.sendPacket(prts);
		}
		checkState();
	}

	public void checkState() {
		UserData ud = ClientPacketHandler.getCurrentUserData();
		if (ud != null && ud.raft != null && ud.raft.getTiles().contains(this)) {
			Raft raft = ud.raft;
			if (hp < 75) {
				// if its dropped below 75, needs repair
				for (Task t : raft.getAllTasks()) {
					if (t instanceof TaskRepair) {
						TaskRepair tr = (TaskRepair) t;
						if (tr.getTarget().getPos().equals(getPos())) {
							// repairing us
							return;
						}
					}
				} // not being repaired
				Location target = new Location();
				target.setPos(this.getPos());
				target.isAbsolute = false;
				target.raftUUID = ud.uuid;
				TaskRepair tr = new TaskRepair(target);
				ud.raft.addTask(tr);
			}
		}
	}
	
	public Location getLocation(String raftUUID) {
		Location l = new Location();
		l.isAbsolute = false;
		l.raftUUID = raftUUID;
		l.setPos(getPos());
		return l;
	}
	
	public Location getLocation(UserData ud) {
		return getLocation(ud.uuid);
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

		//System.out.println("relative speed: " + Math.sqrt(vr.getSquaredLength()));
		return vr;
	}

	public VectorDouble getAbsoluteFrictionVector(Raft parent) {
		VectorDouble motion = getAbsoluteMotion(parent);
		// this is total motion, now multiply by friction coefficients (negative since
		// friction acts against motion)
		// see if tile is over land or water (use COM of tile)
		// System.out.println(getTerrain(parent).frictionCoefficient);
		motion = motion.multiply(-getTerrain(parent).frictionCoefficient);
		return motion;
	}

	public double getDamage(Raft parent) {
		VectorDouble motion = getAbsoluteMotion(parent);
		double dmg = motion.getSquaredLength();
		dmg = Math.sqrt(dmg);
		dmg *= getTerrain(parent).damageCoefficient;
		return dmg;
	}

	public TerrainType getTerrain(Raft parent) {
		VectorDouble pos = getPos().add(new VectorDouble(0.5, 0.5)).getAbsolute(parent.getUnitX(), parent.getUnitY()).add(parent.getPos());
		return WorldHandler.getTerrainType(pos.x, pos.y);
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
