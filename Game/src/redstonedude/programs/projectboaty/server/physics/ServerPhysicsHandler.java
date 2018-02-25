package redstonedude.programs.projectboaty.server.physics;

import java.util.ArrayList;
import java.util.Random;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Predicate;

import redstonedude.programs.projectboaty.client.graphics.DebugHandler;
import redstonedude.programs.projectboaty.server.net.ServerPacketHandler;
import redstonedude.programs.projectboaty.shared.entity.Entity;
import redstonedude.programs.projectboaty.shared.entity.EntityBarrel;
import redstonedude.programs.projectboaty.shared.entity.EntityCharacter;
import redstonedude.programs.projectboaty.shared.entity.EntityResource;
import redstonedude.programs.projectboaty.shared.entity.EntityResource.ResourceType;
import redstonedude.programs.projectboaty.shared.entity.WrappedEntity;
import redstonedude.programs.projectboaty.shared.net.clientbound.PacketDelEntity;
import redstonedude.programs.projectboaty.shared.net.clientbound.PacketEntityState;
import redstonedude.programs.projectboaty.shared.net.clientbound.PacketNewEntity;
import redstonedude.programs.projectboaty.shared.net.clientbound.PacketTileState;
import redstonedude.programs.projectboaty.shared.physics.Location;
import redstonedude.programs.projectboaty.shared.physics.VectorDouble;
import redstonedude.programs.projectboaty.shared.raft.Raft;
import redstonedude.programs.projectboaty.shared.raft.Tile;
import redstonedude.programs.projectboaty.shared.raft.TileAnchorSmall;
import redstonedude.programs.projectboaty.shared.raft.TileThruster;
import redstonedude.programs.projectboaty.shared.world.WorldHandler;
import redstonedude.programs.projectboaty.shared.world.WorldHandler.TerrainType;

public class ServerPhysicsHandler {

	public static int c = 0;

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

	public synchronized static void removeEntity(String uuid) {
		WrappedEntity del = null;
		for (WrappedEntity e : entities) {
			if (e.entity.uuid.equals(uuid)) {
				del = e;
				break;
			}
		}
		if (del != null) {
			del.entity = null;// nullify the entity so it isn't used anywhere else???
			entities.remove(del);
		}
	}

	public synchronized static void removeAllEntities(ArrayList<Entity> ents) {
		ents.forEach(new Consumer<Entity>() {
			@Override
			public void accept(Entity e) {
				removeEntity(e.uuid);
			}
		});
	}

	public static void physicsUpdate() {
		// before we do anything handle all packets from this last tick
		ServerPacketHandler.handlePackets();
		DebugHandler.clear();
		c++;
		// do barrel despawning
		ArrayList<Entity> toDespawn = new ArrayList<Entity>();
		for (WrappedEntity we : getWrappedEntities()) {
			Entity e = we.entity;
			Predicate<Entity> shouldDespawn = new Predicate<Entity>() {
				@Override
				public boolean test(Entity e) {
					double shortestSquareDistance = -1;
					for (ServerUserData sud : ServerPacketHandler.userData) {
						if (sud.raft != null) {
							double squareDistance = sud.raft.getPos().subtract(e.getLoc().getPos()).getSquaredLength();
							if (squareDistance < shortestSquareDistance || shortestSquareDistance == -1) {
								shortestSquareDistance = squareDistance;
							}
						}
					}
					if (shortestSquareDistance > 2500) {
						// over 50 blocks from any raft
						return true;// delete, too far away
					}
					return false;
				}
			};

			if (e instanceof EntityBarrel) {
				if (shouldDespawn.test(e)) {
					toDespawn.add(e);
				} else {
					// not despawning, do physics
					EntityBarrel eb = (EntityBarrel) e;
					VectorDouble pos = eb.getLoc().getPos();
					VectorDouble vel = eb.getVel();
					TerrainType tt = WorldHandler.getTerrainType(pos.x + 0.5, pos.y + 0.5);
					// it will have some intial velocity, apply drag
					VectorDouble drag = vel.multiply(-tt.frictionCoefficient);
					// F=ma, a=F/m
					vel = vel.add(drag.divide(10/* mass */));
					// System.out.println("here: " + vel.x);
					if (tt == TerrainType.Water) {
						vel = vel.add(WorldHandler.getWind().divide(10/* mass */));
					}
					// also see if barrel is inside a raft, if so it needs to be ejected immediately.
					checkAllUsers: for (ServerUserData sud : ServerPacketHandler.userData) {
						if (sud != null && sud.raft != null) {
							// calculate relative position of the barrel
							VectorDouble relPos = pos.add(new VectorDouble(0.5, 0.5)).subtract(sud.raft.getPos()).getRelative(sud.raft.getUnitX(), sud.raft.getUnitY());
							ArrayList<Tile> ts = sud.raft.getTiles();
							for (Tile t : ts) {
								VectorDouble tPos = t.getPos();
								// -0.5 and +1 cos both coordinates are in the bottom left. Just go with it.
								if (relPos.x > tPos.x && relPos.x < tPos.x + 1) {
									if (relPos.y > tPos.y && relPos.y < tPos.y + 1) {
										// inside this tile, add the relevant velocity and then return
										VectorDouble velo = t.getAbsoluteMotion(sud.raft);
										vel = velo.multiply(2);// ensure it goes away

										// also apply damage to this tile
										/**
										 * This is a really bad workaround and needs to be fixed properly for beta
										 */
										double dmg = Math.sqrt(velo.getSquaredLength());
										dmg *= 50;// some damage coefficient
										t.hp -= dmg;
										PacketTileState pts = new PacketTileState(t);
										pts.uuid = sud.uuid;
										ServerPacketHandler.broadcastPacket(pts);
										break checkAllUsers;
									}
								}
							}
						}
					}

					// add the velocity to position and send packet
					eb.setVel(vel);
					Location loc = eb.getLoc();
					loc.setPos(pos.add(vel));
					eb.setLoc(loc);
					PacketEntityState pes = new PacketEntityState(eb);
					ServerPacketHandler.broadcastPacket(pes);
				}
			} else if (e instanceof EntityCharacter) {
				EntityCharacter ec = (EntityCharacter) e;
				if (ec.ownerUUID.equals("")) {
					if (shouldDespawn.test(e)) {
						toDespawn.add(e);
					} else {
						// Do wandering or AI or something here, for now they stand still
					}
				}
			}
		}
		removeAllEntities(toDespawn);
		for (Entity e : toDespawn) {
			PacketDelEntity pde = new PacketDelEntity();
			pde.uuid = e.uuid;
			ServerPacketHandler.broadcastPacket(pde);
		}
		for (ServerUserData sud : ServerPacketHandler.userData) {
			physicsUpdate(sud); // todo comod safety
		}
	}

	public static void physicsUpdate(ServerUserData sud) {
		// nothing needed for now! XD
		if (sud.raft != null) {
			// do barrels for this raft
			// count barrels around here
			VectorDouble pos = sud.raft.getPos();
			double xFar1 = pos.x - 24; // screen is no longer 12*8, consider it as 13*13 now.
			double xClose1 = pos.x - 13;
			double yFar1 = pos.y - 24;
			double yClose1 = pos.y - 13;
			double xFar2 = pos.x + 24;
			double xClose2 = pos.x + 13;
			double yFar2 = pos.y + 24;
			double yClose2 = pos.y + 13;
			double barrelCount = 0;
			double characterCount = 0;
			for (WrappedEntity we : getWrappedEntities()) {
				Entity e = we.entity;
				VectorDouble epos = e.getLoc().getPos();
				if (epos.x > xFar1 && epos.x < xFar2) {
					if (epos.y > yFar1 && epos.y < yFar2) {
						if (e instanceof EntityBarrel) {
							barrelCount++;
						} else if (e instanceof EntityCharacter) {
							EntityCharacter ec = (EntityCharacter) e;
							if (ec.ownerUUID.equals("")) {
								characterCount++;
							}
						}
					}
				}
			}
			if (barrelCount < 50) {
				// spawn another barrel
				Random rand = new Random();
				int x = (int) (rand.nextInt(48) + Math.floor(pos.x) - 24);
				int y = (int) (rand.nextInt(32) + Math.floor(pos.y) - 16);
				if (x > xClose1 && x < xClose2) {
					if (y > yClose1 && y < yClose2) {
						// too close, don't spawn
						return;
					}
				}
				// spawn
				spawnBarrel(x, y);
			}
			if (characterCount < 10) {
				// spawn another character?
				Random rand = new Random();
				int x = (int) (rand.nextInt(48) + Math.floor(pos.x) - 24);
				int y = (int) (rand.nextInt(32) + Math.floor(pos.y) - 16);
				if (x > xClose1 && x < xClose2) {
					if (y > yClose1 && y < yClose2) {
						// too close, don't spawn
						return;
					}
				}
				if (WorldHandler.getTerrainType(x, y) != TerrainType.Land) {
					return;//not land, don't spawn
				}
				// spawn
				spawnCharacter(x, y);
			}

		}
	}

	public static void spawnBarrel(int x, int y) {
		EntityBarrel eb = new EntityBarrel();
		Location loc = new Location();
		loc.setPos(new VectorDouble(x, y));
		eb.uuid = UUID.randomUUID().toString();
		loc.isAbsolute = true;
		eb.setLoc(loc);
		for (WrappedEntity we : ServerPhysicsHandler.getWrappedEntities()) {
			Entity ent = we.entity;
			if (ent.entityTypeID.equals("EntityBarrel")) {
				if (ent.isAbsolute()) {
					VectorDouble entPos = ent.getLoc().getPos();
					VectorDouble ebPos = eb.getLoc().getPos();
					if (ebPos.x > entPos.x && ebPos.x < entPos.x + 1) {
						if (ebPos.y > entPos.y && ebPos.y < entPos.y + 1) {
							return;// already a barrel here
						}
					}
				}
			}
		}
		addEntity(eb);
		ServerPacketHandler.broadcastPacket(new PacketNewEntity(eb));
	}
	
	public static void spawnCharacter(int x, int y) {
		EntityCharacter ec = new EntityCharacter();
		Location loc = new Location();
		loc.setPos(new VectorDouble(x, y));
		ec.uuid = UUID.randomUUID().toString();
		loc.isAbsolute = true;
		ec.ownerUUID = "";
		loc.raftUUID = "";
		ec.setLoc(loc);
		addEntity(ec);
		ServerPacketHandler.broadcastPacket(new PacketNewEntity(ec));
	}

	public static void newCharacter(String clientuuid) {
		ServerUserData sud = ServerPacketHandler.getUserData(clientuuid);
		if (sud != null && sud.raft != null) {
			EntityCharacter ec = new EntityCharacter();
			Location loc = new Location();
			loc.setPos(new VectorDouble(0, 0));
			ec.uuid = UUID.randomUUID().toString();
			ec.ownerUUID = clientuuid;
			loc.raftUUID = clientuuid;
			loc.isAbsolute = false;
			ec.setLoc(loc);
			addEntity(ec);
			ServerPacketHandler.broadcastPacket(new PacketNewEntity(ec));
		}
	}

	public static void createRaft(int id, ServerUserData sud) {
		// System.out.println("RIIIIIIIGHT WE ARE ACTUALLY MAKING A RAFT RIGHT NOW");
		Raft raft = new Raft();
		raft.setPos(new VectorDouble(4, 3));
		// sud.cameraPosition = new VectorDouble(4,3);
		raft.theta = 0;
		switch (id) {
		case 1:
			Tile tile = new Tile();
			tile.setPos(new VectorDouble(1, 0));
			raft.addTile(tile);
			tile = new Tile();
			tile.setPos(new VectorDouble(0, 1));
			raft.addTile(tile);
			tile = new Tile();
			tile.setPos(new VectorDouble(1, 1));
			raft.addTile(tile);
			tile = new Tile();
			tile.setPos(new VectorDouble(2, 1));
			raft.addTile(tile);
			TileThruster thruster = new TileThruster();
			thruster.setPos(new VectorDouble(0, 0));
			raft.addTile(thruster);
			thruster = new TileThruster();
			thruster.setPos(new VectorDouble(2, 0));
			raft.addTile(thruster);
			break;
		case 2:
			tile = new Tile();
			tile.setPos(new VectorDouble(0, 2));
			raft.addTile(tile);
			tile = new Tile();
			tile.setPos(new VectorDouble(0, 3));
			raft.addTile(tile);
			tile = new Tile();
			tile.setPos(new VectorDouble(1, 2));
			raft.addTile(tile);
			tile = new Tile();
			tile.setPos(new VectorDouble(1, 3));
			raft.addTile(tile);
			tile = new Tile();
			tile.setPos(new VectorDouble(2, 2));
			raft.addTile(tile);
			tile = new Tile();
			tile.setPos(new VectorDouble(2, 3));
			raft.addTile(tile);
			tile = new Tile();
			tile.setPos(new VectorDouble(1, 0));
			raft.addTile(tile);
			tile = new Tile();
			tile.setPos(new VectorDouble(1, 1));
			raft.addTile(tile);
			TileAnchorSmall anchorSmall = new TileAnchorSmall();
			anchorSmall.setPos(new VectorDouble(1,2));
			raft.addTile(anchorSmall);
			thruster = new TileThruster();
			thruster.setPos(new VectorDouble(0, 0));
			raft.addTile(thruster);
			thruster = new TileThruster();
			thruster.setPos(new VectorDouble(0, 1));
			raft.addTile(thruster);
			thruster = new TileThruster();
			thruster.setPos(new VectorDouble(2, 0));
			raft.addTile(thruster);
			thruster = new TileThruster();
			thruster.setPos(new VectorDouble(2, 1));
			raft.addTile(thruster);
			break;
		case 3:
			tile = new Tile();
			tile.setPos(new VectorDouble(0, 1));
			raft.addTile(tile);
			tile = new Tile();
			tile.setPos(new VectorDouble(1, 1));
			raft.addTile(tile);
			tile = new Tile();
			tile.setPos(new VectorDouble(2, 1));
			raft.addTile(tile);
			tile = new Tile();
			tile.setPos(new VectorDouble(3, 1));
			raft.addTile(tile);
			tile = new Tile();
			tile.setPos(new VectorDouble(4, 1));
			EntityResource er = new EntityResource(ResourceType.Gold);
			tile.storage.resources.add(er);
			raft.addTile(tile);
			tile = new Tile();
			tile.setPos(new VectorDouble(5, 1));
			raft.addTile(tile);
			thruster = new TileThruster();
			thruster.setPos(new VectorDouble(0, 0));
			raft.addTile(thruster);
			thruster = new TileThruster();
			thruster.setPos(new VectorDouble(4, 0));
			raft.addTile(thruster);
			thruster = new TileThruster();
			thruster.setPos(new VectorDouble(5, 0));
			raft.addTile(thruster);
			break;
		case 4:
			// tile = new Tile();
			// tile.setPos(new VectorDouble(1, 0));
			// raft.addTile(tile);
			// tile = new Tile();
			// tile.setPos(new VectorDouble(1, 1));
			// raft.addTile(tile);
			// thruster = new TileThruster();
			// thruster.setPos(new VectorDouble(0, 0));
			// raft.addTile(thruster);
			// thruster = new TileThruster();
			// thruster.setPos(new VectorDouble(0, 1));
			// thruster.thrustAngle = -Math.PI/2;
			// raft.addTile(thruster);
			// thruster = new TileThruster();
			// thruster.setPos(new VectorDouble(2, 0));
			// raft.addTile(thruster);
			// thruster = new TileThruster();
			// thruster.setPos(new VectorDouble(2, 1));
			// thruster.thrustAngle = Math.PI/2;
			// raft.addTile(thruster);

			// Code for 1-thruster debug
			thruster = new TileThruster();
			thruster.setPos(new VectorDouble(0, 0));
			raft.addTile(thruster);
			break;
		}
		sud.raft = raft;
	}

	public static void populateRaft(int id, ServerUserData sud) {
		int characters = 0;
		switch (id) {
		case 1:
			characters = 6;
			break;
		case 2:
			characters = 8;
			break;
		case 3:
			characters = 8;
			break;
		case 4:
			characters = 1;
			break;
		}
		getWrappedEntities().forEach(new Consumer<WrappedEntity>() {
			@Override
			public void accept(WrappedEntity we) {
				if (we.entity instanceof EntityCharacter) {
					EntityCharacter ec = (EntityCharacter) we.entity;
					if (ec.ownerUUID.equals(sud.uuid)) {
						removeEntity(ec.uuid);
						PacketDelEntity pde = new PacketDelEntity();
						pde.uuid = ec.uuid;
						ServerPacketHandler.broadcastPacket(pde);
					}
				}
			}
		});
		for (int i = 0; i < characters; i++) {
			newCharacter(sud.uuid);
		}
	}

	public static void reset() {
		c = 0;
		ServerPacketHandler.userData.clear();
		// raft = null;
		// cameraPosition = new VectorDouble(0,0);
	}

}
