package redstonedude.programs.projectboaty.shared.net;

import java.io.Serializable;
import java.util.ArrayList;

import redstonedude.programs.projectboaty.shared.raft.Tile;

public class PacketRequestRaftTiles extends Packet implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	public ArrayList<Tile> tiles;
	
	public PacketRequestRaftTiles() {
		super();
		packetID = "PacketRequestRaftTiles";
	}

}
