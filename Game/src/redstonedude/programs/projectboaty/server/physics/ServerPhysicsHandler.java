package redstonedude.programs.projectboaty.server.physics;

import java.util.ArrayList;
import java.util.Random;
import java.util.UUID;
import java.util.function.Consumer;

import redstonedude.programs.projectboaty.client.graphics.DebugHandler;
import redstonedude.programs.projectboaty.server.net.ServerPacketHandler;
import redstonedude.programs.projectboaty.shared.entity.Entity;
import redstonedude.programs.projectboaty.shared.entity.EntityBarrel;
import redstonedude.programs.projectboaty.shared.entity.EntityCharacter;
import redstonedude.programs.projectboaty.shared.entity.WrappedEntity;
import redstonedude.programs.projectboaty.shared.net.clientbound.PacketDelEntity;
import redstonedude.programs.projectboaty.shared.net.clientbound.PacketEntityState;
import redstonedude.programs.projectboaty.shared.net.clientbound.PacketNewEntity;
import redstonedude.programs.projectboaty.shared.net.clientbound.PacketTileState;
import redstonedude.programs.projectboaty.shared.physics.VectorDouble;
import redstonedude.programs.projectboaty.shared.raft.Raft;
import redstonedude.programs.projectboaty.shared.raft.Tile;
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
			del.entity = null;//nullify the entity so it isn't used anywhere else???
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
		//before we do anything handle all packets from this last tick
		ServerPacketHandler.handlePackets();
		DebugHandler.clear();
		c++;
		//do  barrel despawning
		ArrayList<Entity> toDespawn = new ArrayList<Entity>();
		for (WrappedEntity we: entities) {
			Entity e = we.entity;
			if (e instanceof EntityBarrel) {
				//it's a barrel boiks, see if its near any raft
				double shortestSquareDistance = -1;
				for (ServerUserData sud: ServerPacketHandler.userData) {
					if (sud.raft != null) {
						double squareDistance = sud.raft.getPos().subtract(e.loc.getPos()).getSquaredLength();
						if (squareDistance < shortestSquareDistance || shortestSquareDistance == -1) {
							shortestSquareDistance = squareDistance;
						}
					}
				}
				if (shortestSquareDistance > 2500) {
					//over 50 blocks from any raft
					toDespawn.add(e);
				} else {
					//not despawning, do physics
					EntityBarrel eb = (EntityBarrel) e;
					VectorDouble pos = eb.loc.getPos();
					VectorDouble vel = eb.getVel();
					TerrainType tt = WorldHandler.getTerrainType(pos.x+0.5, pos.y+0.5);
					//it will have some intial velocity, apply drag 
					VectorDouble drag = vel.multiply(-tt.frictionCoefficient);
					//F=ma, a=F/m
					vel = vel.add(drag.divide(10/*mass*/));
					//System.out.println("here: " + vel.x);
					if (tt == TerrainType.Water) {
						vel = vel.add(WorldHandler.getWind().divide(10/*mass*/));
					}
					//also see if barrel is inside a raft, if so it needs to be ejected immediately.
					checkAllUsers: for (ServerUserData sud: ServerPacketHandler.userData) {
						if (sud != null && sud.raft != null) {
							//calculate relative position of the barrel
							VectorDouble relPos = pos.add(new VectorDouble(0.5,0.5)).subtract(sud.raft.getPos()).getRelative(sud.raft.getUnitX(), sud.raft.getUnitY());
							ArrayList<Tile> ts = sud.raft.getTiles();
							for (Tile t: ts) {
								VectorDouble tPos = t.getPos();
								//-0.5 and +1 cos both coordinates are in the bottom left. Just go with it.
								if (relPos.x > tPos.x && relPos.x < tPos.x+1) {
									if (relPos.y > tPos.y && relPos.y < tPos.y+1) {
										//inside this tile, add the relevant velocity and then return
										VectorDouble velo = t.getAbsoluteMotion(sud.raft);
										vel = velo.multiply(2);//ensure it goes away
										
										//also apply damage to this tile
										/**
										 * This is a really bad workaround and needs to be fixed properly for beta
										 */
										double dmg = Math.sqrt(velo.getSquaredLength());
										dmg *= 50;//some damage coefficient
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
					
					//add the velocity to position and send packet
					eb.setVel(vel);
					eb.loc.setPos(pos.add(vel));
					PacketEntityState pes = new PacketEntityState(eb);
					ServerPacketHandler.broadcastPacket(pes);
				}
			}
		}
		removeAllEntities(toDespawn);
		for (Entity e: toDespawn) {
			PacketDelEntity pde = new PacketDelEntity();
			pde.uuid = e.uuid;
			ServerPacketHandler.broadcastPacket(pde);
		}
		for (ServerUserData sud:ServerPacketHandler.userData) {
			physicsUpdate(sud); //todo comod safety
		}
	}
	
	public static void physicsUpdate(ServerUserData sud) {
		//nothing needed for now! XD
		if (sud.raft != null) {
			//do barrels for this raft
			//count barrels around here
			VectorDouble pos = sud.raft.getPos();
			double xFar1 = pos.x-24; //screen is no longer 12*8, consider it as 13*13 now.
			double xClose1 = pos.x-13;
			double yFar1 = pos.y-24;
			double yClose1 = pos.y-13;
			double xFar2 = pos.x+24;
			double xClose2 = pos.x+13;
			double yFar2 = pos.y+24;
			double yClose2 = pos.y+13;
			double count = 0;
			for (WrappedEntity we: getWrappedEntities()) {
				Entity e = we.entity;
				if (e instanceof EntityBarrel) {
					VectorDouble epos = e.loc.getPos();
					if (epos.x > xFar1 && epos.x < xFar2) {
						if (epos.y > yFar1 && epos.y < yFar2) {
							count++;
						}
					}
				}
			}
			if (count < 50) {
				//spawn another barrel
				Random rand = new Random();
				int x = (int) (rand.nextInt(48)+Math.floor(pos.x)-24);
				int y = (int) (rand.nextInt(32)+Math.floor(pos.y)-16);
				if (x > xClose1 && x < xClose2) {
					if (y > yClose1 && y < yClose2) {
						//too close, don't spawn
						return;
					}
				}
				//spawn
				spawnBarrel(x,y);
			}
			
		}
	}
	
	public static void spawnBarrel(int x, int y) {
		EntityBarrel eb = new EntityBarrel();
		eb.loc.setPos(new VectorDouble(x, y));
		eb.uuid = UUID.randomUUID().toString();
		eb.loc.isAbsolute = true;
		for (WrappedEntity we: ServerPhysicsHandler.getWrappedEntities()) {
			Entity ent = we.entity;
			if (ent.entityTypeID.equals("EntityBarrel")) {
				if (ent.loc.isAbsolute) {
					VectorDouble entPos = ent.loc.getPos();
					VectorDouble ebPos = eb.loc.getPos();
					if (ebPos.x > entPos.x && ebPos.x < entPos.x+1) {
						if (ebPos.y > entPos.y && ebPos.y < entPos.y+1) {
							return;//already a barrel here
						}
					}
				}
			}
		}
		addEntity(eb);
		ServerPacketHandler.broadcastPacket(new PacketNewEntity(eb));
	}
	
	public static void newCharacter(String clientuuid) {
		ServerUserData sud = ServerPacketHandler.getUserData(clientuuid);
		if (sud != null && sud.raft != null) {
			EntityCharacter ec = new EntityCharacter();
			ec.loc.setPos(new VectorDouble(0,0));
			ec.uuid = UUID.randomUUID().toString();
			ec.ownerUUID = clientuuid;
			ec.loc.raftUUID = clientuuid;
			ec.loc.isAbsolute = false;
			addEntity(ec);
			ServerPacketHandler.broadcastPacket(new PacketNewEntity(ec));
		}
	}

	public static void createRaft(int id, ServerUserData sud) {
		//System.out.println("RIIIIIIIGHT WE ARE ACTUALLY MKAING A RAFT RIGHT NOW");
		Raft raft = new Raft();
		raft.setPos(new VectorDouble(4, 3));
		//sud.cameraPosition = new VectorDouble(4,3);
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
			tile.setPos(new VectorDouble(1, 0));
			raft.addTile(tile);
			tile = new Tile();
			tile.setPos(new VectorDouble(2, 0));
			raft.addTile(tile);
			tile = new Tile();
			tile.setPos(new VectorDouble(0, 1));
			raft.addTile(tile);
			tile = new Tile();
			tile.setPos(new VectorDouble(1, 1));
			raft.addTile(tile);
			thruster = new TileThruster();
			thruster.setPos(new VectorDouble(0, 0));
			raft.addTile(thruster);
			thruster = new TileThruster();
			thruster.setPos(new VectorDouble(2, 1));
			thruster.thrustAngle = Math.PI;
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
			tile = new Tile();
			tile.setPos(new VectorDouble(1, 0));
			raft.addTile(tile);
			tile = new Tile();
			tile.setPos(new VectorDouble(1, 1));
			raft.addTile(tile);
			thruster = new TileThruster();
			thruster.setPos(new VectorDouble(0, 0));
			raft.addTile(thruster);
			thruster = new TileThruster();
			thruster.setPos(new VectorDouble(0, 1));
			thruster.thrustAngle = -Math.PI/2;
			raft.addTile(thruster);
			thruster = new TileThruster();
			thruster.setPos(new VectorDouble(2, 0));
			raft.addTile(thruster);
			thruster = new TileThruster();
			thruster.setPos(new VectorDouble(2, 1));
			thruster.thrustAngle = Math.PI/2;
			raft.addTile(thruster);
			break;
		}
		sud.raft = raft;
	}
	
	public static void reset() {
		c = 0;
		ServerPacketHandler.userData.clear();
		//raft = null;
		//cameraPosition = new VectorDouble(0,0);
	}

}
