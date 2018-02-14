package redstonedude.programs.projectboaty.client.physics;

import java.util.ArrayList;

import redstonedude.programs.projectboaty.client.control.ControlHandler;
import redstonedude.programs.projectboaty.client.control.ControlHandler.Mode;
import redstonedude.programs.projectboaty.client.net.ClientPacketHandler;
import redstonedude.programs.projectboaty.server.net.ServerPacketHandler;
import redstonedude.programs.projectboaty.server.physics.VectorDouble;
import redstonedude.programs.projectboaty.shared.entity.Entity;
import redstonedude.programs.projectboaty.shared.entity.EntityCharacter;
import redstonedude.programs.projectboaty.shared.net.UserData;
import redstonedude.programs.projectboaty.shared.net.serverbound.PacketRequestCharacterState;
import redstonedude.programs.projectboaty.shared.net.serverbound.PacketRequestMoveCharacter;
import redstonedude.programs.projectboaty.shared.net.serverbound.PacketRequestMoveRaft;
import redstonedude.programs.projectboaty.shared.raft.Raft;
import redstonedude.programs.projectboaty.shared.raft.Tile;
import redstonedude.programs.projectboaty.shared.raft.TileThruster;
import redstonedude.programs.projectboaty.shared.task.Task;
import redstonedude.programs.projectboaty.shared.task.TaskHandler;

public class ClientPhysicsHandler {
	// consider making this do all the physics for local boats perhaps?

	public static VectorDouble cameraPosition = new VectorDouble(0, 0);
	public static int c = 0;
	private static ArrayList<Entity> entities = new ArrayList<Entity>();

	public synchronized static void addEntity(Entity e) {
		entities.add(e);
	}

	public synchronized static ArrayList<Entity> getEntities() {
		return (ArrayList<Entity>) entities.clone();
	}

	public synchronized static Entity getEntity(String uuid) {
		for (Entity e : entities) {
			if (e.uuid.equals(uuid)) {
				return e;
			}
		}
		return null;
	}

	public synchronized static void removeEntity(String uuid) {
		Entity del = null;
		for (Entity e : entities) {
			if (e.uuid.equals(uuid)) {
				del = e;
				break;
			}
		}
		if (del != null) {
			entities.remove(del);
		}
	}

	public static void physicsUpdate() {
		//before we do anything handle all packets from this last tick
		ClientPacketHandler.handlePackets();
		if (ControlHandler.mode == Mode.Playing) {
			c++;
			UserData currentUser = ClientPacketHandler.getCurrentUserData();
			for (UserData ud : ClientPacketHandler.userData) {
				if (ud.uuid.equalsIgnoreCase(currentUser.uuid)) {
					exactPhysicsUpdate(ud);
				} else {
					approxPhysicsUpdate(ud);
				}
			}
			for (Entity e : getEntities()) {
				physicsUpdate(e);
			}
			// move camera accordingly
			if (currentUser != null && currentUser.raft != null) {
				VectorDouble posDiff = currentUser.raft.getCOMPos().getAbsolute(currentUser.raft.getUnitX(), currentUser.raft.getUnitY()).add(currentUser.raft.getPos()).subtract(ClientPhysicsHandler.cameraPosition);
				posDiff = posDiff.divide(10);// do it slower
				ClientPhysicsHandler.cameraPosition = ClientPhysicsHandler.cameraPosition.add(posDiff);
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
					if (ec.currentTask == null || ec.currentTask.completed) {
						if (ec.currentTask != null) {
							//compeleted, should've already been removed when it was taken up
							//ud.raft.tasks.remove(ec.currentTask);
							//System.out.println("removed " + ec.currentTask.taskTypeID);
						}
						Task t = TaskHandler.getTask(ud.raft,ec);
						ec.currentTask = t;
						ec.currentTask.init();
						ec.sendState();
					}
					ec.currentTask.execute();
				} else {
					//System.out.println("e1");
				}
				PacketRequestMoveCharacter prmc = new PacketRequestMoveCharacter();
				prmc.uuid = ec.uuid;
				prmc.pos = ec.getPos();
				prmc.absolutePos = ec.absolutePosition;
				prmc.raftPosID = ec.raftUUID;
				ClientPacketHandler.sendPacket(prmc);
			} else {
				//System.out.println("other player");
			}
			
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
			raft.setPos(raft.getPos().add(raft.getVelocity()));
			raft.theta += raft.dtheta;
			raft.sin = Math.sin(raft.theta);
			raft.cos = Math.cos(raft.theta);
		}
	}

	public static void exactPhysicsUpdate(UserData sud) {
		Raft raft = sud.raft;
		if (raft == null) {
			// allow it, it'll be created shortly
			return;
		}

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

			// DebugVector dv = new DebugVector();
			// dv.pos = raft.getPos();
			// dv.vector = tile.getAbsoluteFrictionVector(raft).multiply(100);
			// dv.color = Color.RED;
			// DebugHandler.debugVectors.add(dv);
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

	}

}
