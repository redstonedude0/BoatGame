package redstonedude.programs.projectboaty.client.physics;

import java.util.ArrayList;
import java.util.function.Consumer;

import redstonedude.programs.projectboaty.client.control.ControlHandler;
import redstonedude.programs.projectboaty.client.control.ControlHandler.Mode;
import redstonedude.programs.projectboaty.client.net.ClientPacketHandler;
import redstonedude.programs.projectboaty.shared.entity.Entity;
import redstonedude.programs.projectboaty.shared.entity.EntityCharacter;
import redstonedude.programs.projectboaty.shared.entity.WrappedEntity;
import redstonedude.programs.projectboaty.shared.net.UserData;
import redstonedude.programs.projectboaty.shared.net.serverbound.PacketRequestMoveCharacter;
import redstonedude.programs.projectboaty.shared.net.serverbound.PacketRequestMoveRaft;
import redstonedude.programs.projectboaty.shared.net.serverbound.PacketRequestRaftTiles;
import redstonedude.programs.projectboaty.shared.physics.VectorDouble;
import redstonedude.programs.projectboaty.shared.raft.Raft;
import redstonedude.programs.projectboaty.shared.raft.Tile;
import redstonedude.programs.projectboaty.shared.raft.TileThruster;
import redstonedude.programs.projectboaty.shared.task.Task;
import redstonedude.programs.projectboaty.shared.task.TaskHandler;

public class ClientPhysicsHandler {
	// consider making this do all the physics for local boats perhaps?

	public static VectorDouble cameraPosition = new VectorDouble(0, 0);
	public static double cameraTheta = 0;
	public static int tickCount = 0;
	private static ArrayList<WrappedEntity> entities = new ArrayList<WrappedEntity>();

	public synchronized static void addEntity(Entity e) {
		entities.add(new WrappedEntity(e));
	}

	public synchronized static ArrayList<WrappedEntity> getWrappedEntities() {
		return (ArrayList<WrappedEntity>) entities.clone();
	}
	
	public synchronized static void setEntities(ArrayList<WrappedEntity> e) {
		entities = e; 
	}
	
	public synchronized static void setEntity(Entity ent) {
		for (WrappedEntity e : entities) {
			if (e.entity.uuid.equals(ent.uuid)) {
				e.entity = ent;
				return;
			}
		}
	}

	public synchronized static Entity getEntity(String uuid) {
		WrappedEntity ew = getEntityWrapper(uuid);
		if (ew != null) {
			return ew.entity;
		}
		return null;
	}
	
	public synchronized static WrappedEntity getEntityWrapper(String uuid) {
		for (WrappedEntity e : entities) {
			if (e.entity.uuid.equals(uuid)) {
				return e;
			}
		}
		return null;
	}

	/**
	 * return true if an entity was removed
	 * @param uuid
	 * @return
	 */
	public synchronized static boolean removeEntity(String uuid) {
		WrappedEntity del = null;
		for (WrappedEntity e : entities) {
			if (e.entity.uuid.equals(uuid)) {
				del = e;
				break;
			}
		}
		if (del != null) {
			entities.remove(del);
			del.entity = null;//nullify the entity so it isn't used anywhere else???
			//System.out.println("Removed we");
			return true;
		}
		return false;
	}
	
	public synchronized static void removeAllEntities(ArrayList<Entity> ents) {
		ents.forEach(new Consumer<Entity>() {
			@Override
			public void accept(Entity e) {
				removeEntity(e.uuid);
			}
		});
	}
	
//	public synchronized static boolean removeEntity(String uuid) {
//		Entity del = null;
//		for (Entity e : entities) {
//			if (e.uuid.equals(uuid)) {
//				del = e;
//				break;
//			}
//		}
//		if (del != null) {
//			entities.remove(del);
//			return true;
//		} else {
//			return false;
//		}
//	}

	public static void physicsUpdate() {
		//before we do anything handle all packets from this last tick
		ClientPacketHandler.handlePackets();
		if (ControlHandler.mode == Mode.Playing) {
			tickCount++;
			UserData currentUser = ClientPacketHandler.getCurrentUserData();
			for (UserData ud : ClientPacketHandler.userData) {
				if (ud.uuid.equalsIgnoreCase(currentUser.uuid)) {
					exactPhysicsUpdate(ud);
				} else {
					approxPhysicsUpdate(ud);
				}
			}
			//update entities
			for (WrappedEntity we : getWrappedEntities()) {
				if (we != null && we.entity != null) { //one entity update can make an entity null :/
					physicsUpdate(we.entity);
				}
			}
			//nullcheck
			if (currentUser != null && currentUser.raft != null) {
				//update tasks
				for (Task t: currentUser.raft.getTasks()) {
					t.passiveUpdate();
					if (tickCount%50 == 0) { //every 50 ticks (1 second)
						t.slowPassiveUpdate();
					}
				}
				// move camera accordingly
				if (!Double.isNaN(currentUser.raft.getPos().x)) {
					VectorDouble posDiff = currentUser.raft.getCOMPos().getAbsolute(currentUser.raft.getUnitX(), currentUser.raft.getUnitY()).add(currentUser.raft.getPos()).subtract(ClientPhysicsHandler.cameraPosition);
					//System.out.println(posDiff.x + ":" + currentUser.raft.getCOMPos().x + ":" + currentUser.raft.getUnitX().x + ":" + currentUser.raft.getPos().x + ":" + cameraPosition.x);
					posDiff = posDiff.divide(10);// do it slower
					cameraPosition = cameraPosition.add(posDiff);
					double thetaDiff = currentUser.raft.theta-cameraTheta;
					thetaDiff /= 10;
					cameraTheta += thetaDiff;
				}
			}
		}
	}

	public static void physicsUpdate(Entity e) {
		switch (e.entityTypeID) {
		case "EntityCharacter":
			EntityCharacter ec = (EntityCharacter) e;
			//System.out.println(ec.ownerUUID + ":" + ClientPacketHandler.currentUserUUID);
			if (ec.ownerUUID.equals(ClientPacketHandler.currentUserUUID)) {
				UserData ud = ClientPacketHandler.getUserData(ec.ownerUUID);
				if (ud != null && ud.raft != null) {
					if (ec.currentTask == null || ec.currentTask.isCompleted) {
						if (ec.currentTask != null) {
							//compeleted, should've already been removed when it was taken up though, dead code.
						}
						TaskHandler.assignTask(ud.raft,ec);
						ec.currentTask.init();
						ec.sendState();
					}
					if (tickCount%50 == 0) { //every 50 ticks (1 second)
						ec.currentTask.slowUpdate();
					}
					try {
						ec.currentTask.execute();
					} catch (Exception e1) { //error handling, if there's an error during execution effectively cancel this task.
						e1.printStackTrace();
						ec.currentTask.isCompleted = true;
					}
				} else {
					//System.out.println("e1");
				}
				PacketRequestMoveCharacter prmc = new PacketRequestMoveCharacter();
				prmc.uuid = ec.uuid;
				prmc.pos = ec.loc.getPos();
				prmc.absolutePos = ec.loc.isAbsolute;
				prmc.raftPosID = ec.loc.raftUUID;
				ClientPacketHandler.sendPacket(prmc);
			} // else System.out.println("other player");
			
			//do not try to use other players characters
			break;
		}
	}

	public static void approxPhysicsUpdate(UserData sud) {
		Raft raft = sud.raft;
		if (raft != null) {
			for (Tile tile : raft.getTiles()) {
				if (tile instanceof TileThruster) {
					TileThruster thruster = (TileThruster) tile;
					thruster.setThrustStrength(raft, sud.requiredClockwiseRotation, sud.requiredForwardTranslation, sud.requiredRightwardTranslation);
				}
			}
			//still need physics to account for rotation to be about COM
			raft.setPos(raft.getPos().add(raft.getVelocity()));
			VectorDouble unitx = raft.getUnitX();
			VectorDouble unity = raft.getUnitY();
			VectorDouble centreOfMass = raft.getCOMPos();
			double comx_initial = centreOfMass.x * unitx.x + centreOfMass.y * unity.x;
			double comy_initial = centreOfMass.x * unitx.y + centreOfMass.y * unity.y;
			raft.theta += raft.dtheta;
			raft.sin = Math.sin(raft.theta);
			raft.cos = Math.cos(raft.theta);
			unitx = raft.getUnitX();
			unity = raft.getUnitY();
			double comx_after = centreOfMass.x * unitx.x + centreOfMass.y * unity.x;
			double comy_after = centreOfMass.x * unitx.y + centreOfMass.y * unity.y;
			double dcomx = comx_after - comx_initial;
			double dcomy = comy_after - comy_initial;
			raft.setPos(raft.getPos().subtract(new VectorDouble(dcomx, dcomy)));
		}
	}

	public static void exactPhysicsUpdate(UserData ud) {
		Raft raft = ud.raft;
		if (raft == null) {
			// allow it, it'll be created shortly
			return;
		}
		//System.out.println("Raft output:");
		//System.out.println("  TRG:" + raft.cos + "," + raft.sin);
		//System.out.println("  THE:" + raft.theta + "," + raft.dtheta);
		//System.out.println("  POS:" + raft.getPos().x + "," + raft.getPos().y);
		//System.out.println("  COM:" + raft.getCOMPos().x + "," + raft.getCOMPos().y);
		//System.out.println("  UNX:" + raft.getUnitX().x + "," + raft.getUnitX().y);
		//System.out.println("  UNY:" + raft.getUnitY().x + "," + raft.getUnitY().y);

		ControlHandler.setControlDoubles();

		// calculate non-rotational physics, as well as updating thruster control values
		VectorDouble thrust = new VectorDouble();
		double mass = 0;
		for (Tile tile : raft.getTiles()) {
			mass += tile.mass;
			if (tile instanceof TileThruster) {
				TileThruster thruster = (TileThruster) tile;
				thruster.setThrustStrength(raft, ControlHandler.requiredClockwiseRotation, ControlHandler.requiredForwardTranslation, ControlHandler.requiredRightwardTranslation);
				thrust = thrust.add(thruster.getAbsoluteThrustVector(raft));
			}
			// tiles will apply drag to the object
			thrust = thrust.add(tile.getAbsoluteFrictionVector(raft));
			
			
			
		}
		// F=ma, a = F/m
		VectorDouble acceleration = thrust.divide(mass);
		raft.setVelocity(raft.getVelocity().add(acceleration));
		raft.setPos(raft.getPos().add(raft.getVelocity()));

		// calculate rotational physics
		// a = Fr/mrr
		// centre of mass must first be located.
		VectorDouble massMoments = new VectorDouble();
		for (Tile tile : raft.getTiles()) {
			VectorDouble moment = tile.getPos().add(new VectorDouble(0.5, 0.5)).multiply(tile.mass);
			massMoments = massMoments.add(moment);
		}
		VectorDouble centreOfMass = new VectorDouble(massMoments);
		centreOfMass = centreOfMass.divide(mass);
		raft.setCOMPos(centreOfMass);
		//System.out.println("COM at: " + centreOfMass.x + ":" + centreOfMass.y);

		// calculate moments of inertia about this point
		double forcemoments = 0;
		double squareradiusofgyration = 0;
		for (Tile tile : raft.getTiles()) {
			VectorDouble dpos = tile.getPos().add(new VectorDouble(0.5, 0.5)).subtract(centreOfMass);// this is relative dpos, calculate absolute

			double squaredistance = dpos.getSquaredLength();
			squareradiusofgyration += squaredistance;// squaredistance;
			if (tile instanceof TileThruster) {
				TileThruster thruster = (TileThruster) tile;
				VectorDouble force = new VectorDouble(thruster.getRelativeThrustVector());
				// x component of force * perpendicular distance
				// x component * relative dy of origin
				forcemoments += force.x * -dpos.y; // subtract because dy and dx are making it anticlockwise
				forcemoments += force.y * -dpos.x; // consider taking a cross product or something
				// System.out.println("Moment added: " + (force.get(0)*-dy + force.get(1)*-dx));
			}
			// do drag also
			VectorDouble drag = tile.getRelativeFrictionVector(raft);
			drag = drag.multiply(5); // multiply drag by 5 so it doesn't feel like ice.
			// arbitrary number chosen since calulating hydrodynamics is boring.
			forcemoments += drag.x * -dpos.y;
			forcemoments += drag.y * -dpos.x;
		}
		double masssquaremoments = squareradiusofgyration * mass;
		double atheta = forcemoments / masssquaremoments;
		if (masssquaremoments == 0) {
			atheta = 0;//COM is massmoment - only 1 tile left. There should be no net rotational acceleration.
			//technically there should be but its harder to model
		}
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
		// VectorDouble com_initial =
		// double comx_initial = raft.cos*centreofmassX-raft.sin*centreofmassY;
		// double comy_initial = raft.sin*centreofmassX+raft.cos*centreofmassY;
		raft.theta += raft.dtheta;
		raft.sin = Math.sin(raft.theta);
		raft.cos = Math.cos(raft.theta);
		// double comx_after = raft.cos*centreofmassX-raft.sin*centreofmassY;
		// double comy_after = raft.sin*centreofmassX+raft.cos*centreofmassY;
		unitx = raft.getUnitX();
		unity = raft.getUnitY();
		//System.out.println("UNI at: " + unitx.x + "," + unitx.y + " : " + unity.x + "," + unity.y);
		double comx_after = centreOfMass.x * unitx.x + centreOfMass.y * unity.x;
		double comy_after = centreOfMass.x * unitx.y + centreOfMass.y * unity.y;
		double dcomx = comx_after - comx_initial;
		double dcomy = comy_after - comy_initial;
		// System.out.println(comx_after + ":" + comx_initial);
		if (ControlHandler.debug_lockpos) {
			raft.setPos(new VectorDouble(5, 5));
		}
		// System.out.println(dcomx + ":" + dcomy);
		raft.setPos(raft.getPos().subtract(new VectorDouble(dcomx, dcomy)));
		PacketRequestMoveRaft prmr = new PacketRequestMoveRaft();
		prmr.pos = raft.getPos();
		prmr.theta = raft.theta;
		prmr.velocity = raft.getVelocity();
		prmr.dtheta = raft.dtheta;
		prmr.sin = raft.sin;
		prmr.cos = raft.cos;
		prmr.COMPos = raft.getCOMPos();
		ClientPacketHandler.sendPacket(prmr);
		
		//do damage to the tiles
		ArrayList<Tile> brokenTiles = new ArrayList<Tile>();
		for (Tile tile : raft.getTiles()) {
			tile.damage(tile.getDamage(raft));
			if (tile.hp < 0) {
				brokenTiles.add(tile);
			}
		}
		if (!brokenTiles.isEmpty()) {
			//some change occurred
			raft.removeAllTiles(brokenTiles);
			PacketRequestRaftTiles prrt = new PacketRequestRaftTiles();
			prrt.tiles = raft.getTiles();
			ClientPacketHandler.sendPacket(prrt); //update the server on this
		}
	}
	
	public void reset() {
		entities.clear();
		tickCount = 0;
		cameraPosition = new VectorDouble(0,0);
	}

}
