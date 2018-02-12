package redstonedude.programs.projectboaty.server.physics;

import redstonedude.programs.projectboaty.client.graphics.DebugHandler;
import redstonedude.programs.projectboaty.server.net.ServerPacketHandler;
import redstonedude.programs.projectboaty.shared.raft.Raft;
import redstonedude.programs.projectboaty.shared.raft.Tile;
import redstonedude.programs.projectboaty.shared.raft.TileThruster;

public class ServerPhysicsHandler {

	public static int c = 0;
	
	public static void physicsUpdate() {
		DebugHandler.clear();
		c++;
		//for (ServerUserData sud:ServerPacketHandler.userData) {
		//	physicsUpdate(sud);
		//}
	}
	
	public static void physicsUpdate(ServerUserData sud) {
		//nothing needed for now! XD
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
