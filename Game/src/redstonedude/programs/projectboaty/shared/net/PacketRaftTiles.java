package redstonedude.programs.projectboaty.shared.net;

import java.io.Serializable;
import java.util.ArrayList;

import redstonedude.programs.projectboaty.shared.raft.Tile;

public class PacketRaftTiles extends Packet implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	public ArrayList<Tile> tiles;
	public String uuid;
	
	public PacketRaftTiles() {
		super();
		packetID = "PacketRaftTiles";
	}

}
