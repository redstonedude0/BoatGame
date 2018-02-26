package redstonedude.programs.projectboaty.client.physics;

import java.awt.Color;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Consumer;

import com.sun.org.apache.bcel.internal.generic.L2D;

import redstonedude.programs.projectboaty.client.control.ControlHandler;
import redstonedude.programs.projectboaty.client.control.ControlHandler.Mode;
import redstonedude.programs.projectboaty.client.graphics.DebugHandler;
import redstonedude.programs.projectboaty.client.graphics.DebugVector;
import redstonedude.programs.projectboaty.client.net.ClientPacketHandler;
import redstonedude.programs.projectboaty.server.net.ServerPacketHandler;
import redstonedude.programs.projectboaty.shared.entity.Entity;
import redstonedude.programs.projectboaty.shared.entity.EntityBarrel;
import redstonedude.programs.projectboaty.shared.entity.EntityCharacter;
import redstonedude.programs.projectboaty.shared.entity.WrappedEntity;
import redstonedude.programs.projectboaty.shared.event.EventCharacterDespawn;
import redstonedude.programs.projectboaty.shared.net.UserData;
import redstonedude.programs.projectboaty.shared.net.clientbound.PacketTileState;
import redstonedude.programs.projectboaty.shared.net.serverbound.PacketRequestEntityState;
import redstonedude.programs.projectboaty.shared.net.serverbound.PacketRequestMoveCharacter;
import redstonedude.programs.projectboaty.shared.net.serverbound.PacketRequestMoveRaft;
import redstonedude.programs.projectboaty.shared.net.serverbound.PacketRequestRaftTiles;
import redstonedude.programs.projectboaty.shared.physics.Location;
import redstonedude.programs.projectboaty.shared.physics.PhysicsHandler;
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
	public static WrappedEntity cameraTarget = null;//defualts to raft if null entity
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
			if (e.entity != null && e.entity.uuid.equals(uuid)) {
				del = e;
				break;
			}
		}
		if (del != null) {
			entities.remove(del);
//			if (del.entity instanceof EntityCharacter) {
//				EventCharacterDespawn ecd = new EventCharacterDespawn(del.entity);
//			}
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
			//nullcheck
			if (currentUser != null && currentUser.raft != null) {
				//update tasks
				for (Task t: currentUser.raft.getTasks()) {
					t.passiveUpdate();
					if (tickCount%50 == 0) { //every 50 ticks (1 second)
						t.slowPassiveUpdate();
					}
					if (t.isCompleted) {
						currentUser.raft.removeTask(t);//complete; remove it
					}
				}
				// move camera accordingly, target raft by default
				VectorDouble targetPos = currentUser.raft.getCOMPos().getAbsolute(currentUser.raft.getUnitX(), currentUser.raft.getUnitY()).add(currentUser.raft.getPos());
				if (cameraTarget != null && cameraTarget.entity == null) {
					cameraTarget = null;//nullify cameraTarget if the entity is null
				}
				if (cameraTarget != null) {//target entity
					if (!cameraTarget.entity.isAbsolute()) {
						UserData targetRaft = ClientPacketHandler.getUserData(cameraTarget.entity.getLoc().raftUUID);
						if (targetRaft != null && targetRaft.raft != null) {
							targetPos = cameraTarget.entity.getLoc().getPos().add(new VectorDouble(0.5, 0.5)).getAbsolute(targetRaft.raft.getUnitX(), targetRaft.raft.getUnitY()).add(targetRaft.raft.getPos());
						}
					} else {//get absolute position COM
						targetPos = cameraTarget.entity.getLoc().getPos().add(new VectorDouble(0.5, 0.5));
					}
				}				
				VectorDouble posDiff = targetPos.subtract(ClientPhysicsHandler.cameraPosition);
				//System.out.println(posDiff.x + ":" + currentUser.raft.getCOMPos().x + ":" + currentUser.raft.getUnitX().x + ":" + currentUser.raft.getPos().x + ":" + cameraPosition.x);
				posDiff = posDiff.divide(10);// do it slower
				cameraPosition = cameraPosition.add(posDiff);
				double thetaDiff = currentUser.raft.theta-cameraTheta;
				thetaDiff /= 10;
				cameraTheta += thetaDiff;
			}
			//update entities
			for (WrappedEntity we : getWrappedEntities()) {
				if (we != null && we.entity != null) { //one entity update can make an entity null :/
					physicsUpdate(we.entity);
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
					if (ec.currentTask != null && ec.currentTask.isOnHold) {
						//unassign, and assign new task instead
						ud.raft.addTask(ec.currentTask);
						TaskHandler.assignTask(ud.raft,ec);
						ec.currentTask.init(ec);
						ec.sendState();
					}
					if (ec.currentTask == null || ec.currentTask.isCompleted) {
						if (ec.currentTask != null) {
							//compeleted, should've already been removed when it was taken up though, dead code.
						}
						TaskHandler.assignTask(ud.raft,ec);
						ec.currentTask.init(ec);
						ec.sendState();
					}
					if (tickCount%50 == 0) { //every 50 ticks (1 second)
						ec.currentTask.slowUpdate();
					}
					try {
						ec.currentTask.execute(ec);
					} catch (Exception e1) { //error handling, if there's an error during execution effectively cancel this task.
						e1.printStackTrace();
						ec.currentTask.isCompleted = true;
					}
				} else {
					//System.out.println("e1");
				}
				PacketRequestMoveCharacter prmc = new PacketRequestMoveCharacter();
				prmc.uuid = ec.uuid;
				prmc.loc = ec.getLoc();
				ClientPacketHandler.sendPacket(prmc);
			} // else System.out.println("other player");
			
			//do not try to use other players characters
			break;
		case "EntityBarrel":
			EntityBarrel eb = (EntityBarrel) e;
			UserData ud = ClientPacketHandler.getCurrentUserData();
			if (ud != null && ud.raft != null) {
				// calculate relative position of the barrel
				VectorDouble pos = eb.getLoc().getPos();
				VectorDouble vel = eb.getVel();
				VectorDouble relPos = pos.add(new VectorDouble(0.5, 0.5)).subtract(ud.raft.getPos()).getRelative(ud.raft.getUnitX(), ud.raft.getUnitY());
				ArrayList<Tile> ts = ud.raft.getTiles();
				for (Tile t : ts) {
					VectorDouble tPos = t.getPos();
					// -0.5 and +1 cos both coordinates are in the bottom left. Just go with it.
					if (relPos.x > tPos.x && relPos.x < tPos.x + 1) {
						if (relPos.y > tPos.y && relPos.y < tPos.y + 1) {
							// inside this tile, add the relevant velocity and then return
							Collection<Line2D> boundaryLines = ud.raft.getBoundaryLines();
							VectorDouble tileVel = t.getAbsoluteMotion(ud.raft);
							VectorDouble barrelVel = new VectorDouble(vel);
							VectorDouble velocityDifference = barrelVel.subtract(tileVel);
							
							//VectorDouble start = pos;
							//Point2D angularLineOrigin = new Point2D.Double(start.x,start.y);
							//double angle = Math.atan2(relativeVel.y, relativeVel.x);
							//double length = 1;//vel wont be more than 1
							//boundary lines are all relative - make velocity and position absolute
							VectorDouble relativeVel = velocityDifference.getRelative(ud.raft.getUnitX(), ud.raft.getUnitY());
							Line2D passedLine = null;
							for (Line2D l2d: boundaryLines) {
								Point2D start = l2d.getP1();
								Point2D end = l2d.getP2();
								
//								DebugVector dv = new DebugVector();
//								dv.color = Color.RED;
//								dv.pos = new VectorDouble(start.getX(),start.getY());
//								dv.vector = new VectorDouble(end.getX(),end.getY()).subtract(new VectorDouble(start.getX(),start.getY()));
//								dv.pos = dv.pos.getAbsolute(ud.raft.getUnitX(), ud.raft.getUnitY()).add(ud.raft.getPos());
//								dv.vector = dv.vector.getAbsolute(ud.raft.getUnitX(), ud.raft.getUnitY());
//								DebugHandler.debugVectors.add(dv);
								
//								DebugVector  dv = new DebugVector();
//								dv.color = Color.RED;
//								dv.pos = new VectorDouble(relPos);
//								dv.vector = new VectorDouble(relativeVel);
//								dv.pos = dv.pos.getAbsolute(ud.raft.getUnitX(), ud.raft.getUnitY()).add(ud.raft.getPos());
//								dv.vector = dv.vector.getAbsolute(ud.raft.getUnitX(), ud.raft.getUnitY());
//								DebugHandler.debugVectors.add(dv);
								
								double scalar = PhysicsHandler.crossVectors(relativeVel.x, relativeVel.y, end.getX()-start.getX(),end.getY()-start.getY());
						        //the lines are parallel
						        if (scalar == 0) {
						        	continue;
						        }
						        //Get the values of u and t
						        //v is how much the velocity line needs to be scaled to collide with l2d
						        //u is how much the line l2d needs to be scaled to collide with the line of velocity
						        double v = PhysicsHandler.crossVectors(start.getX() - relPos.x, start.getY() - relPos.y, end.getX()-start.getX(), end.getY()-start.getY()) / scalar;
						        double u = PhysicsHandler.crossVectors(start.getX() - relPos.x, start.getY() - relPos.y, relativeVel.x, relativeVel.y) / scalar;

						        //If line l2d would normally pass through the common endpoint
						        if (0 <= u && u <= 1) {
						        	//line of velocity points to or away from this side
						        	if (v <= 0) {
						        		//velocity is pointing away from this side
						        		//use this side
						        		passedLine = l2d;
						        		break;
						        	}//the line is the opposite side of the raft...
						            continue;
						        } else {
						            continue;//the line of velocity misses
						        }
							}
							if (passedLine == null) {
								System.out.println("null passed line. Oh dear. " + boundaryLines.size());
								continue;
							}
							//get components of velocity parallel and perpendicular to surface
							VectorDouble surface = new VectorDouble(passedLine.getX2()-passedLine.getX1(),passedLine.getY2()-passedLine.getY1());
							VectorDouble parallelVel = relativeVel.dot(surface).divide(Math.sqrt(surface.getSquaredLength()));
							VectorDouble perpendicularVel = relativeVel.subtract(parallelVel);
							//perpendicular vel is flipped
							VectorDouble newVel = perpendicularVel.multiply(-1);// go away
							newVel = newVel.add(parallelVel);
							newVel = newVel.getAbsolute(ud.raft.getUnitX(), ud.raft.getUnitY());
							newVel = newVel.add(tileVel);// add to make absolute vel
							eb.setVel(newVel);
							Location loc = eb.getLoc();
							loc.setPos(loc.getPos().add(eb.getVel()));
							eb.setLoc(loc);
							PacketRequestEntityState pres = new PacketRequestEntityState(e);
							ClientPacketHandler.sendPacket(pres);
						}
					}
				}
			}
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
		
		assert !Double.isNaN(raft.getPos().x) : "NaN asserted";
	}
	
	public void reset() {
		entities.clear();
		tickCount = 0;
		cameraPosition = new VectorDouble(0,0);
	}

}
