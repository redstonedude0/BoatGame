package redstonedude.programs.projectboaty.server.physics;

import java.util.ArrayList;
import java.util.Random;
import java.util.UUID;

import redstonedude.programs.projectboaty.client.graphics.DebugHandler;
import redstonedude.programs.projectboaty.server.net.ServerPacketHandler;
import redstonedude.programs.projectboaty.shared.entity.Entity;
import redstonedude.programs.projectboaty.shared.entity.EntityBarrel;
import redstonedude.programs.projectboaty.shared.entity.EntityCharacter;
import redstonedude.programs.projectboaty.shared.net.PacketNewEntity;
import redstonedude.programs.projectboaty.shared.raft.Raft;
import redstonedude.programs.projectboaty.shared.raft.Tile;
import redstonedude.programs.projectboaty.shared.raft.TileThruster;

public class ServerPhysicsHandler {

	public static int c = 0;
	
	public static ArrayList<Entity> entities = new ArrayList<Entity>();
	
	public static void physicsUpdate() {
		DebugHandler.clear();
		c++;
		for (ServerUserData sud:ServerPacketHandler.userData) {
			physicsUpdate(sud);
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
			for (Entity e: entities) {
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
		entities.add(eb);
		ServerPacketHandler.broadcastPacket(new PacketNewEntity(eb));
	}
	
	public static void newCharacter(String clientuuid) {
		ServerUserData sud = ServerPacketHandler.getUserData(clientuuid);
		if (sud != null && sud.raft != null) {
			EntityCharacter ec = new EntityCharacter();
			ec.setPos(new VectorDouble(0,0));
			ec.ownerUUID = clientuuid;
			ec.raftUUID = clientuuid;
			ec.absolutePosition = false;
			entities.add(ec);
			ServerPacketHandler.broadcastPacket(new PacketNewEntity(ec));
		}
	}

	public static void createRaft(int id, ServerUserData sud) {
		Raft raft = new Raft();
		raft.setPos(new VectorDouble(4, 3));
		//sud.cameraPosition = new VectorDouble(4,3);
		raft.theta = 0;
		switch (id) {
		case 1:
			Tile tile = new Tile();
			tile.setPos(new VectorDouble(1, 0));
			raft.tiles.add(tile);
			tile = new Tile();
			tile.setPos(new VectorDouble(0, 1));
			raft.tiles.add(tile);
			tile = new Tile();
			tile.setPos(new VectorDouble(1, 1));
			raft.tiles.add(tile);
			tile = new Tile();
			tile.setPos(new VectorDouble(2, 1));
			raft.tiles.add(tile);
			TileThruster thruster = new TileThruster();
			thruster.setPos(new VectorDouble(0, 0));
			raft.tiles.add(thruster);
			thruster = new TileThruster();
			thruster.setPos(new VectorDouble(2, 0));
			raft.tiles.add(thruster);
			break;
		case 2:
			tile = new Tile();
			tile.setPos(new VectorDouble(1, 0));
			raft.tiles.add(tile);
			tile = new Tile();
			tile.setPos(new VectorDouble(2, 0));
			raft.tiles.add(tile);
			tile = new Tile();
			tile.setPos(new VectorDouble(0, 1));
			raft.tiles.add(tile);
			tile = new Tile();
			tile.setPos(new VectorDouble(1, 1));
			raft.tiles.add(tile);
			thruster = new TileThruster();
			thruster.setPos(new VectorDouble(0, 0));
			raft.tiles.add(thruster);
			thruster = new TileThruster();
			thruster.setPos(new VectorDouble(2, 1));
			thruster.thrustAngle = Math.PI;
			raft.tiles.add(thruster);
			break;
		case 3:
			tile = new Tile();
			tile.setPos(new VectorDouble(0, 1));
			raft.tiles.add(tile);
			tile = new Tile();
			tile.setPos(new VectorDouble(1, 1));
			raft.tiles.add(tile);
			tile = new Tile();
			tile.setPos(new VectorDouble(2, 1));
			raft.tiles.add(tile);
			tile = new Tile();
			tile.setPos(new VectorDouble(3, 1));
			raft.tiles.add(tile);
			tile = new Tile();
			tile.setPos(new VectorDouble(4, 1));
			raft.tiles.add(tile);
			tile = new Tile();
			tile.setPos(new VectorDouble(5, 1));
			raft.tiles.add(tile);
			thruster = new TileThruster();
			thruster.setPos(new VectorDouble(0, 0));
			raft.tiles.add(thruster);
			thruster = new TileThruster();
			thruster.setPos(new VectorDouble(4, 0));
			raft.tiles.add(thruster);
			thruster = new TileThruster();
			thruster.setPos(new VectorDouble(5, 0));
			raft.tiles.add(thruster);
			break;
		case 4:
			tile = new Tile();
			tile.setPos(new VectorDouble(1, 0));
			raft.tiles.add(tile);
			tile = new Tile();
			tile.setPos(new VectorDouble(1, 1));
			raft.tiles.add(tile);
			thruster = new TileThruster();
			thruster.setPos(new VectorDouble(0, 0));
			raft.tiles.add(thruster);
			thruster = new TileThruster();
			thruster.setPos(new VectorDouble(0, 1));
			thruster.thrustAngle = -Math.PI/2;
			raft.tiles.add(thruster);
			thruster = new TileThruster();
			thruster.setPos(new VectorDouble(2, 0));
			raft.tiles.add(thruster);
			thruster = new TileThruster();
			thruster.setPos(new VectorDouble(2, 1));
			thruster.thrustAngle = Math.PI/2;
			raft.tiles.add(thruster);
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
