package redstonedude.programs.projectboaty.server.physics;

import java.util.ArrayList;
import java.util.Random;
import java.util.UUID;

import redstonedude.programs.projectboaty.client.graphics.DebugHandler;
import redstonedude.programs.projectboaty.client.physics.ClientPhysicsHandler;
import redstonedude.programs.projectboaty.server.net.ServerPacketHandler;
import redstonedude.programs.projectboaty.shared.entity.Entity;
import redstonedude.programs.projectboaty.shared.entity.EntityBarrel;
import redstonedude.programs.projectboaty.shared.entity.EntityCharacter;
import redstonedude.programs.projectboaty.shared.net.clientbound.PacketNewEntity;
import redstonedude.programs.projectboaty.shared.raft.Raft;
import redstonedude.programs.projectboaty.shared.raft.Tile;
import redstonedude.programs.projectboaty.shared.raft.TileThruster;

public class ServerPhysicsHandler {

	public static int c = 0;
	
	private static ArrayList<Entity> entities = new ArrayList<Entity>();
	
	public synchronized static void addEntity(Entity e) {
		entities.add(e);
	}

	public synchronized static ArrayList<Entity> getEntities() {
		return (ArrayList<Entity>) entities.clone();
	}
	
	public synchronized static void setEntities(ArrayList<Entity> e) {
		entities = e;
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
		ServerPacketHandler.handlePackets();
		DebugHandler.clear();
		c++;
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
			double xFar1 = pos.x-24;
			double xClose1 = pos.x-12;
			double yFar1 = pos.y-16;
			double yClose1 = pos.y-8;
			double xFar2 = pos.x+24;
			double xClose2 = pos.x+12;
			double yFar2 = pos.y+16;
			double yClose2 = pos.y+8;
			double count = 0;
			for (Entity e: getEntities()) {
				if (e instanceof EntityBarrel) {
					VectorDouble epos = e.getPos();
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
				int x = (int) (rand.nextInt(48)+Math.floor(pos.x));
				int y = (int) (rand.nextInt(32)+Math.floor(pos.y));
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
		eb.setPos(new VectorDouble(x, y));
		eb.uuid = UUID.randomUUID().toString();
		eb.absolutePosition = true;
		for (Entity ent: ServerPhysicsHandler.getEntities()) {
			if (ent.entityTypeID.equals("EntityBarrel")) {
				if (ent.absolutePosition) {
					if (ent.getPos().equals(eb.getPos())) {
						return;//already a barrel here
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
			ec.setPos(new VectorDouble(0,0));
			ec.uuid = UUID.randomUUID().toString();
			ec.ownerUUID = clientuuid;
			ec.raftUUID = clientuuid;
			ec.absolutePosition = false;
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
